package com.sk.weichat.xmpp;

import android.text.TextUtils;
import android.util.Log;

import com.sk.weichat.MyApplication;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.XmppChatHistory;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.sp.UserSp;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.Base64;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.DES;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.ThreadManager;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.log.LogUtils;
import com.sk.weichat.util.secure.AES;
import com.sk.weichat.util.secure.chat.SecureChatUtil;
import com.sk.weichat.xmpp.listener.ChatMessageListener;

import org.jivesoftware.smack.AsyncButOrdered;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.packet.MUCInitialPresence;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class XMucChatManager {
    private static final boolean ENABLE_AUTO_JOIN_ROOM = true;
    private static final String TAG = "XMucChatManager";
    private static final AsyncButOrdered<XMucChatManager> asyncButOrdered = new AsyncButOrdered<>();
    private final Resourcepart resourcepart;
    private final String mMessageKey;
    private final StanzaListener messageListener;
    private final StanzaListener presenceListener;
    private CoreService mService;
    private XMPPTCPConnection mConnection;
    private String mLoginUserId;
    private long mJoinTimeOut;
    private MultiUserChatManager mMultiUserChatManager;
    private XMuChatMessageListener mXMuChatMessageListener;
    private Jid requestHistoryFrom;
    private Jid requestHistoryTo;

    public XMucChatManager(CoreService service, XMPPTCPConnection connection) {
        mService = service;
        mConnection = connection;

        mLoginUserId = CoreManager.requireSelf(mService).getUserId();
        resourcepart = Resourcepart.fromOrThrowUnchecked(mLoginUserId);
        mJoinTimeOut = 20000;

        mMultiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
        mMessageKey = UserSp.getInstance(mService).getMessageKey();
        mXMuChatMessageListener = new XMuChatMessageListener(service);
        messageListener = packet -> {
            final Message message = (Message) packet;
            asyncButOrdered.performAsyncButOrdered(XMucChatManager.this, () -> {
                mXMuChatMessageListener.processMessage(message);
            });
        };
        connection.addSyncStanzaListener(messageListener, MessageTypeFilter.GROUPCHAT);

        presenceListener = packet -> {
            final Presence presence = (Presence) packet;
            final EntityFullJid from = presence.getFrom().asEntityFullJidIfPossible();
            if (from == null) {
                return;
            }
            asyncButOrdered.performAsyncButOrdered(XMucChatManager.this, () -> {
                Log.v(TAG, "presenceListener: " + packet);
                if (!presence.getFrom().getDomain().toString().startsWith("muc.")) {
                    Log.v(TAG, "非群相关presence消息");
                    return;
                }
                if (!TextUtils.equals(mLoginUserId, presence.getFrom().getResourceOrEmpty().toString())) {
                    Log.v(TAG, "不是关于我的消息");
                    return;
                }
                Localpart local = presence.getFrom().getLocalpartOrNull();
                if (presence.getType() == Presence.Type.available) {
                    Log.d(TAG, "加群成功: " + local);
                } else if (presence.getType() == Presence.Type.unavailable) {
                    Log.d(TAG, "退群成功: " + local);
                }
            });
        };
        connection.addSyncStanzaListener(presenceListener, StanzaTypeFilter.PRESENCE);
        requestHistoryFrom = JidCreate.entityFullFrom(
                Localpart.fromOrThrowUnchecked(mLoginUserId),
                connection.getXMPPServiceDomain(),
                connection.getConfiguration().getResource()
        );
        requestHistoryTo = connection.getXMPPServiceDomain();
    }

    public static String getMucChatServiceName(XMPPConnection connection) {
        return "@muc." + connection.getXMPPServiceDomain();
    }

    /**
     * @param toUserId 要发送消息的房间Id
     * @param oMessage 已经存到本地数据库的一条即将发送的消息
     */
    public void sendMessage(final String toUserId, final ChatMessage oMessage) {
        // 加密可能影响到消息对象复用，所以拷贝一份，
        ChatMessage chatMessage = oMessage.clone(false);

        ThreadManager.getPool().execute(new Runnable() {
            public void run() {
                String roomJid = toUserId + getMucChatServiceName(mConnection);

                // 对消息content字段进行加密传输
                if (!TextUtils.isEmpty(chatMessage.getContent())
                        && !XmppMessage.filter(chatMessage)) {
                    Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, toUserId);
                    if (friend == null) {
                        chatMessage.setIsEncrypt(0);
                    } else {
                        if (friend.getEncryptType() == 1) {
                            String key = SecureChatUtil.getSymmetricKey(chatMessage.getTimeSend(), chatMessage.getPacketId());
                            try {
                                chatMessage.setContent(DES.encryptDES(chatMessage.getContent(), key));
                                chatMessage.setIsEncrypt(1);
                            } catch (Exception e) {
                                // 3des加密失败
                                Log.e(TAG, "3des加密失败");
                                chatMessage.setIsEncrypt(0);
                                e.printStackTrace();
                            }
                        } else if (friend.getEncryptType() == 2) {
                            String key = SecureChatUtil.getSymmetricKey(chatMessage.getPacketId());
                            chatMessage.setContent(AES.encryptBase64(chatMessage.getContent(), Base64.decode(key)));
                            chatMessage.setIsEncrypt(2);
                        }

                        // 私密群组使用第三种加密方式
                        if (friend.getIsSecretGroup() == 1) {
                            String key = SecureChatUtil.decryptChatKey(toUserId, friend.getChatKeyGroup());
                            String realKey = SecureChatUtil.getSingleSymmetricKey(chatMessage.getPacketId(), key);
                            chatMessage.setContent(AES.encryptBase64(chatMessage.getContent(), Base64.decode(realKey)));
                            chatMessage.setIsEncrypt(3);
                            chatMessage.setSignature(SecureChatUtil.getSignatureMulti(chatMessage.getFromUserId(), chatMessage.getToUserId(),
                                    chatMessage.getIsEncrypt(), chatMessage.getPacketId(), realKey,
                                    chatMessage.getContent()));// 对已成型的消息进行签名
                            // 对数据库内该条消息也进行加密
                            ChatMessageDao.getInstance().encrypt(mLoginUserId, chatMessage.getToUserId(),
                                    chatMessage.getPacketId(), chatMessage.getSignature());
                        }
                    }
                }
                Message msg = new Message();
                msg.setType(Message.Type.groupchat);
                msg.setBody(chatMessage.toJsonString(mMessageKey));
                msg.setPacketID(chatMessage.getPacketId());
                msg.setTo(roomJid);
                if (MyApplication.IS_OPEN_RECEIPT) {// 添加回执请求
                    DeliveryReceiptManager.addDeliveryReceiptRequest(msg);
                }
                // int sendStatus = ChatMessageListener.MESSAGE_SEND_FAILED;
                int sendStatus = ChatMessageListener.MESSAGE_SEND_ING;
                // 发送消息
                try {
                    mConnection.sendStanza(msg);
                    // sendStatus = ChatMessageListener.MESSAGE_SEND_ING;
                } catch (NotConnectedException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ListenerManager.getInstance().notifyMessageSendStateChange(mLoginUserId, toUserId, chatMessage.getPacketId(),
                        sendStatus);
            }
        });
    }

    public String createMucRoom(String roomName) {
        try {
            /**
             * randomUUID
             */
            String roomId = UUID.randomUUID().toString().replaceAll("-", "");
            String roomJid = roomId + getMucChatServiceName(mConnection);
            // 创建聊天室
            EntityBareJid mEntityBareJid = null;
            try {
                mEntityBareJid = JidCreate.entityBareFrom(roomJid);
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }

            MultiUserChat multiUserChat = mMultiUserChatManager.getMultiUserChat(mEntityBareJid);
            Resourcepart resourcepart = Resourcepart.fromOrThrowUnchecked(mLoginUserId);
            try {
                multiUserChat.create(resourcepart);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 获得聊天室的配置表单
            Form form = null;
            try {
                form = multiUserChat.getConfigurationForm();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 根据原始表单创建一个要提交的新表单。
            Form submitForm = form.createAnswerForm();
            // 向要提交的表单添加默认答复
            List<FormField> fields = form.getFields();
            for (int i = 0; i < fields.size(); i++) {
                FormField field = (FormField) fields.get(i);
/*
                if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
                    // 设置默认值作为答复
                    submitForm.setDefaultAnswer(field.getVariable());
                }
*/
                submitForm.setDefaultAnswer(field.getVariable());
            }
            // 设置聊天室的新拥有者
            // List owners = new ArrayList();
            // owners.add("liaonaibo2\\40slook.cc");
            // owners.add("liaonaibo1\\40slook.cc");
            // submitForm.setAnswer("muc#roomconfig_roomowners", owners);
            // 设置聊天室的名字
            submitForm.setAnswer("muc#roomconfig_roomname", roomName);
            // 登录房间对话
            submitForm.setAnswer("muc#roomconfig_enablelogging", true);
            // 设置聊天室是持久聊天室，即将要被保存下来
            submitForm.setAnswer("muc#roomconfig_persistentroom", true);
            // 设置聊天室描述
            // if (!TextUtils.isEmpty(roomDesc)) {
            // submitForm.setAnswer("muc#roomconfig_roomdesc", roomDesc);
            // }
            // 允许修改主题
            // submitForm.setAnswer("muc#roomconfig_changesubject", true);
            // 允许占有者邀请其他人
            // submitForm.setAnswer("muc#roomconfig_allowinvites", true);
            // 最大人数
            // List<String> maxusers = new ArrayList<String>();
            // maxusers.add("50");
            // submitForm.setAnswer("muc#roomconfig_maxusers", maxusers);
            // 公开的，允许被搜索到
            // submitForm.setAnswer("muc#roomconfig_publicroom", true);
            // 是否主持腾出空间(加了这个默认游客进去不能发言)
            // submitForm.setAnswer("muc#roomconfig_moderatedroom", true);
            // 房间仅对成员开放
            // submitForm.setAnswer("muc#roomconfig_membersonly", true);
            // 不需要密码
            // submitForm.setAnswer("muc#roomconfig_passwordprotectedroom",false);
            // 房间密码
            // submitForm.setAnswer("muc#roomconfig_roomsecret", "111");
            // 允许主持 能够发现真实 JID
            // List<String> whois = new ArrayList<String>();
            // whois.add("anyone");
            // submitForm.setAnswer("muc#roomconfig_whois", whois);

            // 管理员
            // <field var='muc#roomconfig_roomadmins'>
            // <value>wiccarocks@shakespeare.lit</value>
            // <value>hecate@shakespeare.lit</value>
            // </field>

            // 仅允许注册的昵称登录
            // submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
            // 允许使用者修改昵称
            // submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
            // 允许用户注册房间
            // submitForm.setAnswer("x-muc#roomconfig_registration", false);

            // 发送已完成的表单（有默认值）到服务器来配置聊天室
            try {
                multiUserChat.sendConfigurationForm(submitForm);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // muc.changeSubject(roomSubject);
            return roomId;
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void joinMucChat(final String toUserId, long lastSeconds) {
        String roomJid = toUserId + getMucChatServiceName(mConnection);

        Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, toUserId);
        if (friend != null) {
            // Log.e(TAG, "friend.getGroupStatus:" + friend.getGroupStatus());
            if (friend.getGroupStatus() != 0) {
                // 我已被踢出该群 || 该群已解散 || 该群已被后台锁定，不加入该群
                Log.e(TAG, " 我已被踢出该群 || 该群已解散 || 该群已被后台锁定，Return");
                return;
            }
        }

        boolean isShieldGroupMsg = PreferenceUtils.getBoolean(MyApplication.getContext(),
                Constants.SHIELD_GROUP_MSG + roomJid + mLoginUserId, false);
        if (isShieldGroupMsg) {// 屏蔽了该群组消息
            lastSeconds = 0;
        }

        try {
            Presence joinPresence = new Presence(Presence.Type.available);
            // lastSeconds默认应为-1而不是0，但没什么影响，
            joinPresence.addExtension(new MUCInitialPresence(null, -1, -1, (int) lastSeconds,
                    null));
            EntityFullJid myRoomJid = JidCreate.entityFullFrom(JidCreate.entityBareFrom(roomJid), resourcepart);
            joinPresence.setTo(myRoomJid);
            mConnection.sendStanza(joinPresence);
        } catch (Exception e) {
            Reporter.post("加群失败: " + toUserId, e);
        }
    }

    public void exitMucChat(String toUserId) {
        String roomJid = toUserId + getMucChatServiceName(mConnection);
        try {
            EntityFullJid myRoomJid = JidCreate.entityFullFrom(JidCreate.entityBareFrom(roomJid), resourcepart);
            Presence leavePresence = new Presence(Presence.Type.unavailable);
            leavePresence.setTo(myRoomJid);
            mConnection.sendStanza(leavePresence);
        } catch (Exception e) {
            Reporter.post("退群失败: " + toUserId, e);
        }
    }

    public void reset() {
        String userId = CoreManager.requireSelf(mService).getUserId();
        if (!mLoginUserId.equals(userId)) {
            mLoginUserId = userId;
        }
    }

    // 现在加入了群组分页漫游，群组的离线消息不能立即获取，必须要等到'tigase/getLastChatList'接口调用完毕后在加入群组，获取离线消息记录
    public void joinExistGroup() {
        // 先获取全局的离线-->上线 这个时间段的时间
        int lastSeconds;
        long offlineTime = PreferenceUtils.getLong(MyApplication.getContext(), Constants.OFFLINE_TIME + mLoginUserId, 0);
        if (offlineTime == 0) {
            lastSeconds = 0;
        } else {
            lastSeconds = (int) (TimeUtils.sk_time_current_time() - offlineTime);
            LogUtils.e(TAG, "joinExistGroup-->" + TimeUtils.sk_time_current_time() + "，" + offlineTime + "，" + lastSeconds + "，");
        }

        ExecutorService executorService = Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors());

        AsyncUtils.doAsync(this, e -> {
            Reporter.post("加入群组出异常，", e);
        }, executorService, c -> {
            List<Friend> friends = FriendDao.getInstance().getAllRooms(mLoginUserId);// 获取本地所有群组
            if (friends != null && friends.size() > 0) {
                if (ENABLE_AUTO_JOIN_ROOM) {
                    List<XmppChatHistory> historyList = new ArrayList<>();
                    for (int i = 0; i < friends.size(); i++) {
                        Friend friend = friends.get(i);
                        ChatMessage mLastChatMessage = ChatMessageDao.getInstance().getLastChatMessage(mLoginUserId, friend.getUserId(), offlineTime);
                        XmppChatHistory history = new XmppChatHistory();
                        history.setRoomJid(friend.getUserId());
                        if (mLastChatMessage != null) {// 如果该群组的最后一条消息不为空，将该条消息的timeSend作为当前群组的离线时间，这样比上面全局的离线时间更加准确
                            int lastMessageTimeSend = (int) (TimeUtils.sk_time_current_time() - mLastChatMessage.getTimeSend());
                            history.setTime(lastMessageTimeSend + 30);
                        } else {// 该群组本地无消息记录，取全局的离线时间
                            history.setTime(lastSeconds);
                        }
                        if (friend.getTimeCreate() > 0) {
                            int time = (int) ((TimeUtils.sk_time_current_time() - friend.getTimeCreate()));
                            if (history.getTime() > time) {// 判断获取离线消息传入的时间段是否大于当前时间-入群时间，如大于，time传当前时间-入群时间
                                history.setTime(time);
                            }
                        }
                        if (history.getTime() > 0) {
                            historyList.add(history);
                        }
                    }
                    if (historyList.size() > 0) {
                        sendRequestHistory(historyList);
                    }
                } else {
                    for (int i = 0; i < friends.size(); i++) {
                        Friend friend = friends.get(i);
                        ChatMessage mLastChatMessage = ChatMessageDao.getInstance().getLastChatMessage(mLoginUserId, friend.getUserId());
                        if (mLastChatMessage != null) {// 如果该群组的最后一条消息不为空，将该条消息的timeSend作为当前群组的离线时间，这样比上面全局的离线时间更加准确
                            int lastMessageTimeSend = (int) (TimeUtils.sk_time_current_time() - mLastChatMessage.getTimeSend());
                            joinMucChat(friend.getUserId(), lastMessageTimeSend + 30);
                        } else {// 该群组本地无消息记录，取全局的离线时间
                            joinMucChat(friend.getUserId(), lastSeconds);
                        }
                    }
                }
            }
        });
    }

    private void sendRequestHistory(List<XmppChatHistory> historyList) {
        Log.d(TAG, "sendRequestHistory() called with: historyList = [" + historyList + "]");
        RequestHistory requestHistory = new RequestHistory(historyList);
        try {
            mConnection.sendStanza(requestHistory);
        } catch (Exception e) {
            Log.e(TAG, "请求群组离线消息失败：", e);
        }
    }

    private class RequestHistory extends IQ {
        private static final String ELEMENT = "body";
        private static final String NAMESPACE = "xmpp:shiku:roomAck";
        private List<XmppChatHistory> historyList;

        public RequestHistory(List<XmppChatHistory> historyList) {
            super(ELEMENT, NAMESPACE);
            this.historyList = historyList;
            setFrom(requestHistoryFrom);
            setTo(requestHistoryTo);
            setType(Type.set);
        }

        @Override
        protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
            xml.rightAngleBracket().optAppend(joinHistory());
            return xml;
        }

        private String joinHistory() {
            final Iterator<XmppChatHistory> it = historyList.iterator();
            if (!it.hasNext()) {
                return "";
            }
            final StringBuilder sb = new StringBuilder();
            XmppChatHistory h = it.next();
            sb.append(h.getTime());
            sb.append(',');
            sb.append(h.getRoomJid());
            while (it.hasNext()) {
                sb.append('|');
                h = it.next();
                sb.append(h.getTime());
                sb.append(',');
                sb.append(h.getRoomJid());
            }
            return sb.toString();
        }
    }
}