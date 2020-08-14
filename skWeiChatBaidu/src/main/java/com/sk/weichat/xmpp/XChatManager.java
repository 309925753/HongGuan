package com.sk.weichat.xmpp;

import android.text.TextUtils;
import android.util.Log;

import com.sk.weichat.MyApplication;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.NewFriendMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.login.MachineDao;
import com.sk.weichat.sp.UserSp;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.Base64;
import com.sk.weichat.util.DES;
import com.sk.weichat.util.ThreadManager;
import com.sk.weichat.util.secure.AES;
import com.sk.weichat.util.secure.DH;
import com.sk.weichat.util.secure.chat.SecureChatUtil;
import com.sk.weichat.xmpp.listener.ChatMessageListener;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能：
 * 1、接收消息，包括单聊和群聊的接收消息的监听的绑定、具体处理已转移到 xChatlistener 和 xMucChatlistener
 * 2、发送消息，包括发送单聊和群聊的
 */
public class XChatManager {
    private static final String TAG = "XChatManager";

    private final String mMessageKey;
    private CoreService mService;
    private XMPPConnection mConnection;

    private String mLoginUserId;
    private String mServerName;

    private ChatManager mChatManager;
    private XChatMessageListener mMessageListener;
    private Map<String, Chat> mChatMaps = new HashMap<>();
    // 引入org.jivesoftware.smack.chat.ChatManager对象 原因可至getChatByResource方法下查看
    private org.jivesoftware.smack.chat.ChatManager mMultiLoginChatManager;

    public XChatManager(CoreService coreService, XMPPConnection connection) {
        mService = coreService;
        mConnection = connection;

        mLoginUserId = CoreManager.requireSelf(coreService).getUserId();
        mServerName = CoreManager.requireConfig(MyApplication.getInstance()).XMPPDomain;

        initXChat();

        mMessageKey = UserSp.getInstance(mService).getMessageKey();
    }

    private void initXChat() {
        mChatManager = ChatManager.getInstanceFor(mConnection);
        mChatManager.setXhmtlImEnabled(true);
        mMessageListener = new XChatMessageListener(mService);
        mChatManager.addIncomingListener(mMessageListener);

        mMultiLoginChatManager = org.jivesoftware.smack.chat.ChatManager.getInstanceFor(mConnection);
    }

    public void reset() { // 切换账号的操作
        mChatMaps.clear();
    }

    /**
     * 发送聊天的消息
     *
     * @param toUserId 要发送给的用户
     * @param oMessage 已经存到本地数据库的一条即将发送的消息
     */
    public void sendMessage(final String toUserId, final ChatMessage oMessage) {
        // 加密可能影响到消息对象复用，所以拷贝一份，
        ChatMessage chatMessage = oMessage.clone(false);
        /*
         * 先将自己定义的消息类型转(ChatMessage)换成 smack第三方定义的Message类型
         * 然后通过smack的Chat对象来发送一个msg
         */
        ThreadManager.getPool().execute(new Runnable() {
            Chat chat = getChat(toUserId);

            public void run() {
                try {
                    // 对消息content字段进行加密传输
                    if (!TextUtils.isEmpty(chatMessage.getContent())
                            && !XmppMessage.filter(chatMessage)) {
                        if (!mLoginUserId.equals(toUserId)) {
                            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getToUserId());
                            if (friend == null) {//  明文传输
                                chatMessage.setIsEncrypt(0);
                            } else {
                                if (friend.getEncryptType() == 1) {//  3des, compatible old version, generate key no change
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
                                } else if (friend.getEncryptType() == 2) { // aes
                                    String key = SecureChatUtil.getSymmetricKey(chatMessage.getPacketId());
                                    chatMessage.setContent(AES.encryptBase64(chatMessage.getContent(), Base64.decode(key)));
                                    chatMessage.setIsEncrypt(2);
                                } else if (friend.getEncryptType() == 3) { // dh/aes
                                    if (TextUtils.isEmpty(friend.getPublicKeyDH())) {
                                        // 客户端在开启端到端加密时，需要判断好友是否有dh公钥，如没有是不允许开启的
                                        // 以防万一，如果出现这种情况，将isEncrypt置为0
                                        Log.e(TAG, "好友dh公钥为空");
                                        chatMessage.setIsEncrypt(0);
                                    } else {
                                        try {
                                            String key = DH.getCommonSecretKeyBase64(SecureChatUtil.getDHPrivateKey(mLoginUserId), friend.getPublicKeyDH());
                                            String realKey = SecureChatUtil.getSingleSymmetricKey(chatMessage.getPacketId(), key);
                                            chatMessage.setContent(AES.encryptBase64(chatMessage.getContent(), Base64.decode(realKey)));
                                            chatMessage.setIsEncrypt(3);// attention:这个一定要放在设置签名前面，因为接收方验签时的isEncrypt为3
                                            chatMessage.setSignature(SecureChatUtil.getSignatureSingle(chatMessage.getFromUserId(), chatMessage.getToUserId(),
                                                    chatMessage.getIsEncrypt(), chatMessage.getPacketId(), realKey,
                                                    chatMessage.getContent()));// 对已成型的消息进行签名
                                            // 对数据库内该条消息也进行加密
                                            ChatMessageDao.getInstance().encrypt(mLoginUserId, chatMessage.getToUserId(),
                                                    chatMessage.getPacketId(), chatMessage.getSignature());
                                        } catch (Exception e) {
                                            // 获取对称密钥K失败
                                            Log.e(TAG, "获取对称密钥K失败");
                                            chatMessage.setIsEncrypt(0);
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } else {
                            // 自己给其他端发消息
                        }
                    } else {
                        chatMessage.setIsEncrypt(0);
                    }

                    Message msg = new Message();
                    msg.setType(Message.Type.chat);
                    msg.setBody(chatMessage.toJsonString(mMessageKey));
                    msg.setPacketID(chatMessage.getPacketId());
                    if (MyApplication.IS_OPEN_RECEIPT) {// 在发送消息之前发送回执请求
                        DeliveryReceiptManager.addDeliveryReceiptRequest(msg);
                    }

                    // 发送消息给其他人(一条resource不拼接的消息)
                    if (!mLoginUserId.equals(toUserId)) {// 发送转发消息 || 检测消息 || 给我的设备发消息，会直接往下走
                        try {
                            Log.e(TAG, "发送消息给其他人");
                            chat.send(msg);
                            // 调用消息发送状态监听，将消息发送状态改为发送中...
                            ListenerManager.getInstance().notifyMessageSendStateChange(
                                    mLoginUserId, toUserId, chatMessage.getPacketId(),
                                    ChatMessageListener.MESSAGE_SEND_ING);
                        } catch (InterruptedException e) {
                            // 调用消息发送状态监听，将消息发送状态改为发送失败
                            ListenerManager.getInstance().notifyMessageSendStateChange(
                                    mLoginUserId, toUserId, chatMessage.getPacketId(),
                                    ChatMessageListener.MESSAGE_SEND_FAILED);
                            e.printStackTrace();
                        }
                    }

                    // 给我的设备发消息，不转发，且需要重新获得Chat对象
                    if (!TextUtils.isEmpty(chatMessage.getFromUserId()) && !TextUtils.isEmpty(chatMessage.getToUserId())
                            && chatMessage.getFromUserId().equals(chatMessage.getToUserId())
                            && chatMessage.getType() != XmppMessage.TYPE_SEND_ONLINE_STATUS) {
                        try {
                            if (MyApplication.IsRingId.equals("Empty")) {
                                chat.send(msg);// 理论上不太可能
                            } else {
                                Log.e(TAG, toUserId + "--&&--" + MyApplication.IsRingId);
                                org.jivesoftware.smack.chat.Chat deviceChat = getChatByResource(toUserId, MyApplication.IsRingId);
                                deviceChat.sendMessage(msg);
                                Log.e(TAG, "消息发送成功");
                            }
                        } catch (InterruptedException e) {
                            ListenerManager.getInstance().notifyMessageSendStateChange(
                                    mLoginUserId, toUserId, chatMessage.getPacketId(),
                                    ChatMessageListener.MESSAGE_SEND_FAILED);
                        }
                        return;
                    }

                    if (MyApplication.IS_SUPPORT_MULTI_LOGIN) {// 发送转发消息 || 检测消息
                        Log.e(TAG, "发送转发消息 || 检测消息");
                        sendForwardMessage(msg);
                    }

                } catch (SmackException.NotConnectedException e) {
                    // 发送异常，调用消息发送状态监听，将消息发送状态改为发送失败
                    e.printStackTrace();
                    /*ListenerManager.getInstance().notifyMessageSendStateChange(
                            mLoginUserId, toUserId, chatMessage.getPacketId(),
                            ChatMessageListener.MESSAGE_SEND_FAILED);*/
                }
            }
        });
    }

    /**
     * 发送新朋友消息
     */
    public void sendMessage(final String toUserId, final NewFriendMessage newFriendMessage) {
        ThreadManager.getPool().execute(new Runnable() {
            public void run() {
                Chat chat = getChat(toUserId);
                Log.e(TAG, "sendNewFriendMessage--->toUserId:" + toUserId);
                try {
                    Message msg = new Message();
                    msg.setType(Message.Type.chat);
                    msg.setBody(newFriendMessage.toJsonString());// 新朋友推送消息
                    msg.setPacketID(newFriendMessage.getPacketId());
                    if (MyApplication.IS_OPEN_RECEIPT) {
                        DeliveryReceiptManager.addDeliveryReceiptRequest(msg);
                    }
                    try {
                        chat.send(msg);// 发送消息
                        ListenerManager.getInstance().notifyNewFriendSendStateChange(toUserId, newFriendMessage, ChatMessageListener.MESSAGE_SEND_ING);
                    } catch (InterruptedException e) {
                        ListenerManager.getInstance().notifyNewFriendSendStateChange(toUserId, newFriendMessage, ChatMessageListener.MESSAGE_SEND_FAILED);
                        e.printStackTrace();
                    }

                    // 转发给自己
                    if (MyApplication.IS_SUPPORT_MULTI_LOGIN) {// 多点登录下需要转发
                        sendForwardMessage(msg);
                    }

                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                    ListenerManager.getInstance().notifyNewFriendSendStateChange(toUserId, newFriendMessage, ChatMessageListener.MESSAGE_SEND_FAILED);
                }
            }
        });
    }

    private Chat getChat(String toUserId) {
        String to = toUserId + "@" + mServerName;

        Chat chat = mChatMaps.get(toUserId);
        if (chat != null) {
            return chat;
        }
        EntityBareJid mEntityBareJid = null;
        try {
            mEntityBareJid = JidCreate.entityBareFrom(to);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        chat = mChatManager.chatWith(mEntityBareJid);
        return chat;
    }

    /**
     * bug：给“我的设备”发送消息，本地查看日志，消息明明已经发送出去了，但其他端就是没有收到该条消息
     * 原因：Smack 有过一次重大升级  之前发消息的Chat对象全部变为了Chat2对象，但查看源码发现Chat2对象内有一个lockedResource对象，
     * 该对象导致了toJid只能to到与自己登录时设置的Resource一致
     * 解决方法：‘发消息给“我的设备”，转发消息给其他端，通过Chat对象来发送而非Chat2对象
     */
    private org.jivesoftware.smack.chat.Chat getChatByResource(String toUserId, String resource) {
        String s = toUserId + "@" + mServerName + "/" + resource;
        EntityJid entityJid = null;
        try {
            entityJid = JidCreate.entityFrom(s);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        org.jivesoftware.smack.chat.Chat chat = mMultiLoginChatManager.createChat(entityJid);
        return chat;
    }

    // 发送上、下线，检测(type==200)，转发消息
    private void sendForwardMessage(Message msg) {
        if (MyApplication.IS_SEND_MSG_EVERYONE) {
            Log.e(TAG, "sendMessageToEvery");
            /*
            第一次发送type==200的消息，因为本地其他端的状态都为离线，
            因此不能调用sendMessageToSome去发消息，直接发一条200的消息出去，
            无条件请求回执
             */
            if (!MyApplication.IS_OPEN_RECEIPT) {// 为true的话上面已经请求过回执了，不在重复请求
                DeliveryReceiptManager.addDeliveryReceiptRequest(msg);
            }
            sendMessageToEvery(msg);
        } else {
            sendMessageToSome(msg);
        }
    }

    private void sendMessageToEvery(Message msg) {
        Chat chat = getChat(mLoginUserId);
        try {
            chat.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MyApplication.IS_SEND_MSG_EVERYONE = false;
    }

    private void sendMessageToSome(Message msg) {
        for (String s : MyApplication.machine) {
            if (MachineDao.getInstance().getMachineOnLineStatus(s)) {
                Log.e(TAG, "转发给" + s + "设备");
                org.jivesoftware.smack.chat.Chat chat = getChatByResource(mLoginUserId, s);
                try {
                    Message message = new Message();// 需要重新创建一个Msg，如果引用之前的Msg对象，当第一个Msg或前面的Msg还未发送出去时，可能会出问题
                    message.setType(Message.Type.chat);
                    message.setBody(msg.getBody());
                    message.setPacketID(msg.getPacketID());
                    chat.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
