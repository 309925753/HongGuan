package com.sk.weichat.xmpp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.audio.NoticeVoicePlayer;
import com.sk.weichat.bean.EventSecureNotify;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.MsgRoamTask;
import com.sk.weichat.bean.RoomMember;
import com.sk.weichat.bean.event.EventNewNotice;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.broadcast.MsgBroadcast;
import com.sk.weichat.broadcast.MucgroupUpdateUtil;
import com.sk.weichat.call.talk.MessageTalkJoinEvent;
import com.sk.weichat.call.talk.MessageTalkLeftEvent;
import com.sk.weichat.call.talk.MessageTalkOnlineEvent;
import com.sk.weichat.call.talk.MessageTalkReleaseEvent;
import com.sk.weichat.call.talk.MessageTalkRequestEvent;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.MsgRoamTaskDao;
import com.sk.weichat.db.dao.RoomMemberDao;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.live.LiveConstants;
import com.sk.weichat.ui.message.HandleSecureChatMessage;
import com.sk.weichat.ui.mucfile.XfileUtils;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.DateFormatUtil;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.xmpp.listener.ChatMessageListener;
import com.sk.weichat.xmpp.util.XmppStringUtil;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.json.JSONException;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

import static com.sk.weichat.bean.message.XmppMessage.TYPE_SEND_DANMU;

/**
 * Created by Administrator on 2017/11/24.
 */

public class XMuChatMessageListener implements MessageListener {

    private CoreService mService;
    private String mLoginUserId;

    public XMuChatMessageListener(CoreService coreService) {
        mService = coreService;
        mLoginUserId = CoreManager.requireSelf(mService).getUserId();
    }

    @Override
    public void processMessage(Message message) {
        mService.sendReceipt(message.getPacketID());
        Log.e("msg_muc", "from:" + message.getFrom() + " ,to:" + message.getTo());
        if (TextUtils.isEmpty(message.getFrom()) || TextUtils.isEmpty(message.getTo())) {
            Log.e("msg_muc", "Return 1");
            return;
        }
        String from = message.getFrom().toString();
        String to = message.getTo().toString();
        if (!XmppStringUtil.isJID(from) || !XmppStringUtil.isJID(to)) {
            Log.e("msg_muc", "Return 2");
            return;
        }

        String content = message.getBody();
        if (TextUtils.isEmpty(content)) {
            Log.e("msg_muc", "Return 3");
            return;
        }
        Log.e("msg_muc", content);

        // DelayInformation delayInformation = (DelayInformation) message.getExtension("delay", "jabber:x:delay");
        DelayInformation delayInformation = (DelayInformation) message.getExtension("delay", "urn:xmpp:delay");
        if (com.sk.weichat.util.StringUtils.strEquals(message.getPacketID(), "") || message.getPacketID() == null) {
            /**
             * 接收到的packetId可能会为空，为了补上这个错误，我们发送消息的时候在body多发了一个messageId,用于容错
             * 所以我们这里如果查到packetId为空的话，给他补上messageId
             * 添加id位置：
             * @see {@link  ChatMessage#toJsonString(boolean)}
             * @see {@link  com.sk.weichat.bean.message.NewFriendMessage#toJsonString(boolean)}
             * */
            try {
                JSONObject jsonObject = JSONObject.parseObject(message.getBody());
                message.setPacketID(jsonObject.getString("messageId"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (delayInformation != null) {// 这是历史记录
            Log.e("delay", "这是历史记录........" + message.getBody() + "delay:" + delayInformation.getStamp().getTime());
            Date date = delayInformation.getStamp();
            if (date != null) {
                saveGroupMessage(content, from, message.getPacketID(), true);
                return;
            }
        }
        saveGroupMessage(content, from, message.getPacketID(), false);
    }

    /**
     * 保存接收到的聊天信息(群聊)
     */
    private void saveGroupMessage(String body, String from, String packetId, boolean isDelay) {
        String fromId = XmppStringUtil.getRoomJID(from);
        String roomJid = XmppStringUtil.getRoomJIDPrefix(fromId);

        ChatMessage chatMessage = new ChatMessage(body);

        if (TextUtils.equals(chatMessage.getFromUserId(), mLoginUserId)
                && chatMessage.getType() == XmppMessage.TYPE_READ
                && TextUtils.isEmpty(chatMessage.getFromUserName())) {
            chatMessage.setFromUserName(CoreManager.requireSelf(mService).getNickName());
        }

        if (!chatMessage.validate()) {
            return;
        }
        ChatMessageDao.getInstance().decrypt(true, chatMessage);// 解密
        int type = chatMessage.getType();

        chatMessage.setGroup(true);
        chatMessage.setMessageState(ChatMessageListener.MESSAGE_SEND_SUCCESS);
        if (TextUtils.isEmpty(packetId)) {
            if (isDelay) {
                Log.e("msg_muc", "离线消息的packetId为空，漫游任务可能会受到影响，考虑要不要直接Return");
            }
            packetId = UUID.randomUUID().toString().replaceAll("-", "");
        }
        chatMessage.setPacketId(packetId);

        // 生成漫游任务
        if (isDelay) {
            if (chatMessage.isExpired()) {// 该条消息为过期消息，存入本地后直接Return ，不通知
                Log.e("msg_muc", "// 该条消息为过期消息，存入本地后直接Return ，不通知");
                chatMessage.setIsExpired(1);
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomJid, chatMessage);
                return;
            }
            // 离线消息 判断当前群组的实际离线消息是否大于100条，如大于100条，为之前创建的任务的endTime字段赋值，反之则删除任务
            // 判断条件 离线消息内有一条消息的msgId等于当前任务的startMsgId 离线消息小于100条
            MsgRoamTask mLastMsgRoamTask = MsgRoamTaskDao.getInstance().getFriendLastMsgRoamTask(mLoginUserId, roomJid); // 获取该群组最后一个任务
            if (mLastMsgRoamTask == null) {
            } else if (mLastMsgRoamTask.getEndTime() == 0) {// 为该任务的EndTime赋值 理论上只会赋值一次
                MsgRoamTaskDao.getInstance().updateMsgRoamTaskEndTime(mLoginUserId, roomJid, mLastMsgRoamTask.getTaskId(), chatMessage.getTimeSend());
            } else if (packetId.equals(mLastMsgRoamTask.getStartMsgId())) {
                MsgRoamTaskDao.getInstance().deleteMsgRoamTask(mLoginUserId, roomJid, mLastMsgRoamTask.getTaskId());
            }
        }

        boolean isShieldGroupMsg = PreferenceUtils.getBoolean(MyApplication.getContext(), Constants.SHIELD_GROUP_MSG + roomJid + mLoginUserId, false);
        if (isShieldGroupMsg) {// 已屏蔽
            return;
        }

        if (type == XmppMessage.TYPE_TEXT
                && !TextUtils.isEmpty(chatMessage.getObjectId())) {// 判断为@消息
            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, roomJid);
            if (friend != null) {
                if (friend.getIsAtMe() == 0
                        && !TextUtils.equals(MyApplication.IsRingId, roomJid)) {// 本地无@通知 && 收到该条消息时不处于当前群组的聊天界面
                    if (chatMessage.getObjectId().equals(roomJid)) {// @全体成员
                        FriendDao.getInstance().updateAtMeStatus(roomJid, 2);
                    } else if (chatMessage.getObjectId().contains(mLoginUserId)) {// @我
                        FriendDao.getInstance().updateAtMeStatus(roomJid, 1);
                    }
                }
            }
        }

        // 群已读
        if (type == XmppMessage.TYPE_READ) {
            packetId = chatMessage.getContent();
            ChatMessage chat = ChatMessageDao.getInstance().findMsgById(mLoginUserId, roomJid, packetId);
            if (chat != null) {
                String fromUserId = chatMessage.getFromUserId();
                boolean repeat = ChatMessageDao.getInstance().checkRepeatRead(mLoginUserId, roomJid, fromUserId, packetId);
                if (!repeat) {
                    int count = chat.getReadPersons();// 查看人数+1
                    chat.setReadPersons(count + 1);
                    // 覆盖最后时间
                    chat.setReadTime(chatMessage.getTimeSend());
                    // 更新消息数据
                    ChatMessageDao.getInstance().updateMessageRead(mLoginUserId, roomJid, chat);
                    // 保存新消息
                    ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomJid, chatMessage);
                    // 通知刷新
                    MsgBroadcast.broadcastMsgReadUpdate(MyApplication.getInstance(), packetId);
                }
            }
            return;
        }

        // 某个成员领取了红包
        if (type == XmppMessage.TYPE_83) {
            String fromUserId = chatMessage.getFromUserId();// 红包领取方
            String toUserId = chatMessage.getToUserId();// 红包发送方
            String fromName;
            String toName;
            if (TextUtils.equals(fromUserId, mLoginUserId) && TextUtils.equals(toUserId, mLoginUserId)) {// 自己领取了自己的红包
                fromName = MyApplication.getContext().getString(R.string.you);
                toName = MyApplication.getContext().getString(R.string.self);
            } else if (TextUtils.equals(toUserId, mLoginUserId)) {// xx领取了你的红包
                fromName = chatMessage.getFromUserName();
                toName = MyApplication.getContext().getString(R.string.you);
            } else if (TextUtils.equals(fromUserId, mLoginUserId)) {//你领取了xx的红包
                fromName = MyApplication.getContext().getString(R.string.you);
                toName = RoomMemberDao.getInstance().getRoomMemberName(chatMessage.getObjectId(), chatMessage.getFromUserId());
            } else {// xx领取了xx的红包
                // 与我无关的红包领取消息就不显示了吧，直接return
                return;
                // fromName = chatMessage.getFromUserName();
                // toName = RoomMemberDao.getInstance().getRoomMemberName(chatMessage.getObjectId(), chatMessage.getFromUserId());
            }

            String hasBennReceived = "";
            if (chatMessage.getFileSize() == 1) {// 红包是否领完
                try {
                    String sRedSendTime = chatMessage.getFilePath();
                    long redSendTime = Long.parseLong(sRedSendTime);
                    long betweenTime = chatMessage.getTimeSend() - redSendTime;
                    String sBetweenTime;
                    if (betweenTime < TimeUnit.MINUTES.toSeconds(1)) {
                        sBetweenTime = betweenTime + MyApplication.getContext().getString(R.string.second);
                    } else if (betweenTime < TimeUnit.HOURS.toSeconds(1)) {
                        sBetweenTime = TimeUnit.SECONDS.toMinutes(betweenTime) + MyApplication.getContext().getString(R.string.minute);
                    } else {
                        sBetweenTime = TimeUnit.SECONDS.toHours(betweenTime) + MyApplication.getContext().getString(R.string.hour);
                    }
                    hasBennReceived = MyApplication.getContext().getString(R.string.red_packet_has_received_place_holder, sBetweenTime);
                } catch (Exception e) {
                    hasBennReceived = MyApplication.getContext().getString(R.string.red_packet_has_received);
                }
            }
            String str = MyApplication.getContext().getString(R.string.tip_receive_red_packet_place_holder, fromName, toName) + hasBennReceived;

            // 针对红包领取的提示消息 需要做点击事件处理，将红包的type与id存入其他字段内
            chatMessage.setFileSize(XmppMessage.TYPE_83);
            chatMessage.setFilePath(chatMessage.getContent());

            chatMessage.setType(XmppMessage.TYPE_TIP);
            chatMessage.setContent(str);
            fromUserId = chatMessage.getObjectId();
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, fromUserId, chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, fromUserId, chatMessage, true);
            }
            return;
        }

        // 对讲机相关消息130-139
        if (type >= XmppMessage.TYPE_IS_MU_CONNECT_TALK && type <= XmppMessage.TYPE_TALK_KICK) {
            chatTalk(chatMessage);
            return;
        }

        // 消息撤回
        if (type == XmppMessage.TYPE_BACK) {
            // 本地数据库处理
            packetId = chatMessage.getContent();
            if (chatMessage.getFromUserId().equals(mLoginUserId)) {// 自己发的不用处理
                ChatMessageDao.getInstance().updateMessageBack(mLoginUserId, roomJid, packetId, MyApplication.getContext().getString(R.string.you));
            } else {
                ChatMessageDao.getInstance().updateMessageBack(mLoginUserId, roomJid, packetId, chatMessage.getFromUserName(), chatMessage.getFromUserId());
            }

            Intent intent = new Intent();
            intent.putExtra("packetId", packetId);
            intent.setAction(com.sk.weichat.broadcast.OtherBroadcast.MSG_BACK);
            mService.sendBroadcast(intent);

            // 更新UI界面
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(mLoginUserId, roomJid);
            if (chat != null) {
                if (chat.getPacketId().equals(packetId)) {
                    // 要撤回的消息正是朋友表的最后一条消息
                    if (chatMessage.getFromUserId().equals(mLoginUserId)) {// 自己发的不用处理
                        FriendDao.getInstance().updateFriendContent(mLoginUserId, roomJid,
                                MyApplication.getContext().getString(R.string.you) + " " + MyApplication.getInstance().getString(R.string.other_with_draw), XmppMessage.TYPE_TEXT, chatMessage.getTimeSend());
                    } else {
                        FriendDao.getInstance().updateFriendContent(mLoginUserId, roomJid,
                                chatMessage.getFromUserName() + " " + MyApplication.getInstance().getString(R.string.other_with_draw), XmppMessage.TYPE_TEXT, chatMessage.getTimeSend());
                    }
                    MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                }
            }
            return;
        }

        if ((type >= XmppMessage.TYPE_MUCFILE_ADD && type <= XmppMessage.TYPE_MUCFILE_DOWN)
                || (type >= XmppMessage.TYPE_CHANGE_NICK_NAME && type <= XmppMessage.NEW_MEMBER)
                || type == XmppMessage.TYPE_SEND_MANAGER
                || type == XmppMessage.TYPE_EDIT_GROUP_NOTICE
                || (type >= XmppMessage.TYPE_CHANGE_SHOW_READ && type <= XmppMessage.TYPE_GROUP_TRANSFER)) {
            if (TextUtils.isEmpty(chatMessage.getObjectId())) {
                Log.e("msg_muc", "Return 4");
                return;
            }
            if (ChatMessageDao.getInstance().hasSameMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage.getPacketId())) {// 本地已经保存了这条消息，不处理
                Log.e("msg_muc", "Return 5");
                return;
            }
            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, chatMessage.getObjectId());
            if (friend != null) {
                chatGroup(body, chatMessage, friend);
            }
            return;
        }

        // 群聊收到805消息，群成员发送了chatKey给请求者，更新界面
        if (chatMessage.getType() == XmppMessage.TYPE_SECURE_SEND_KEY) {
            ChatMessageDao.getInstance().updateChatMessageReceiptStatus(mLoginUserId, chatMessage.getToUserId(), chatMessage.getContent());
            EventBus.getDefault().post(new EventSecureNotify(EventSecureNotify.MULTI_SNED_KEY_MSG, chatMessage));
            return;
        }

        if (chatMessage.getType() == XmppMessage.TYPE_SECURE_NOTIFY_REFRESH_KEY) {
            HandleSecureChatMessage.distributionChatMessage(chatMessage);
            return;
        }

        if (type == XmppMessage.TYPE_GROUP_UPDATE_MSG_AUTO_DESTROY_TIME) {
            if (TextUtils.isEmpty(chatMessage.getObjectId())) {
                Log.e("msg_muc", "Return 4");
                return;
            }
            if (ChatMessageDao.getInstance().hasSameMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage.getPacketId())) {// 本地已经保存了这条消息，不处理
                Log.e("msg_muc", "Return 5");
                return;
            }
            FriendDao.getInstance().updateChatRecordTimeOut(chatMessage.getObjectId(), Double.parseDouble(chatMessage.getContent()));
            chatMessage.setType(XmppMessage.TYPE_TIP);
            chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_group_owner_update_msg_auto_destroy_time, DateFormatUtil.timeStr(Double.parseDouble(chatMessage.getContent()))));
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
            }
        }

        // 直播消息处理
        if ((type >= XmppMessage.TYPE_SEND_DANMU && type <= XmppMessage.TYPE_SEND_ENTER_LIVE_ROOM)
                || (type >= XmppMessage.TYPE_LIVE_LOCKING && type <= XmppMessage.TYPE_LIVE_SET_MANAGER)) {
            chatLive(body, chatMessage);
            return;
        }

        if (chatMessage.getFromUserId().equals(mLoginUserId) &&
                (chatMessage.getType() == XmppMessage.TYPE_IMAGE || chatMessage.getType() == XmppMessage.TYPE_VIDEO || chatMessage.getType() == XmppMessage.TYPE_FILE)) {
            Log.e("msg_muc", "多点登录，需要显示上传进度的消息");
            chatMessage.setUpload(true);
            chatMessage.setUploadSchedule(100);
        }

        Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, roomJid);
        if (friend != null && friend.getGroupStatus() != 0) {
            // 可能存在离线情况下将你邀请入群>发消息>将你踢出群组>发消息的情况，因为收到907有单独获取离线消息，所以此时不保存不应该显示的消息
            Long timeSend = XChatMessageListener.exitGroupTimeMap.get(friend.getUserId());// 取出被踢出群组消息的timeSend
            if (timeSend != null && timeSend > 0 && chatMessage.getTimeSend() > timeSend) {
                // 接收到被踢出群组之后的群离线消息，不处理
                return;
            }
        }
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomJid, chatMessage)) {
            if (friend != null) {// friend == null 为直播间消息，直接跳过
                if (friend.getOfflineNoPushMsg() == 0
                        && !TextUtils.equals(chatMessage.getFromUserId(), mLoginUserId)) {// 需要考虑多点登录的情况，如多点登录下收到自己的消息也不通知
                    mService.notificationMessage(chatMessage, true);// 消息已存入本地，调用本地通知

                    if (!roomJid.equals(MyApplication.IsRingId)
                            && !chatMessage.getFromUserId().equals(mLoginUserId)) {// 收到该消息时不处于与发送方的聊天界面 && 不是自己发送的消息
                        // 群组铃声通知
                        NoticeVoicePlayer.getInstance().start();
                    }
                } else {
                    Log.e("msg", "已针对该群组开启了消息免打扰 || 其他端发过来的消息，不通知");
                }
            }

            ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, roomJid, chatMessage, true);
        }
    }

    private void chatTalk(ChatMessage chatMessage) {
        if (TextUtils.equals(chatMessage.getFromUserId(), mLoginUserId)) {
            // TODO: 多点登录没有考虑，
            return;
        }
        switch (chatMessage.getType()) {
            case XmppMessage.TYPE_TALK_REQUEST:
                EventBus.getDefault().post(new MessageTalkRequestEvent(chatMessage));
                break;
            case XmppMessage.TYPE_TALK_RELEASE:
                EventBus.getDefault().post(new MessageTalkReleaseEvent(chatMessage));
                break;
            case XmppMessage.TYPE_TALK_JOIN:
                EventBus.getDefault().post(new MessageTalkJoinEvent(chatMessage));
                break;
            case XmppMessage.TYPE_TALK_LEFT:
                EventBus.getDefault().post(new MessageTalkLeftEvent(chatMessage));
                break;
            case XmppMessage.TYPE_TALK_ONLINE:
                EventBus.getDefault().post(new MessageTalkOnlineEvent(chatMessage));
                break;
        }
    }

    private void chatGroup(String body, ChatMessage chatMessage, Friend friend) {
        int type = chatMessage.getType();
        String fromUserId = chatMessage.getFromUserId();
        String fromUserName = chatMessage.getFromUserName();
        String toUserId = chatMessage.getToUserId();
        JSONObject jsonObject = JSONObject.parseObject(body);
        String toUserName = jsonObject.getString("toUserName");

        if (!TextUtils.isEmpty(toUserId)) {
            if (toUserId.equals(mLoginUserId)) {// 针对我的操作，只需要为fromUserName赋值
                String xF = getName(friend, fromUserId);
                if (!TextUtils.isEmpty(xF)) {
                    fromUserName = xF;
                }
            } else {// 针对其他人的操作，fromUserName与toUserName都需要赋值
                String xF = getName(friend, fromUserId);
                if (!TextUtils.isEmpty(xF)) {
                    fromUserName = xF;
                }
                String xT = getName(friend, toUserId);
                if (!TextUtils.isEmpty(xT)) {
                    toUserName = xT;
                }
            }
        }
        chatMessage.setGroup(true);
        chatMessage.setType(XmppMessage.TYPE_TIP);

        /*
        群文件
         */
        if (type == XmppMessage.TYPE_MUCFILE_DEL || type == XmppMessage.TYPE_MUCFILE_ADD) {
            String str;
            if (type == XmppMessage.TYPE_MUCFILE_DEL) {
                // str = chatMessage.getFromUserName() + " 删除了群文件 " + chatMessage.getFilePath();
                str = fromUserName + " " + MyApplication.getInstance().getString(R.string.message_file_delete) + ":" + chatMessage.getFilePath();
            } else {
                // str = chatMessage.getFromUserName() + " 上传了群文件 " + chatMessage.getFilePath();
                str = fromUserName + " " + MyApplication.getInstance().getString(R.string.message_file_upload) + ":" + chatMessage.getFilePath();
            }
            // 更新聊天记录表最后一条消息
            chatMessage.setContent(str);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
            return;
        }

        /*
        群管理
         */
        if (type >= XmppMessage.TYPE_CHANGE_SHOW_READ && type <= XmppMessage.TYPE_GROUP_TRANSFER) {
            if (type == XmppMessage.TYPE_GROUP_VERIFY) {
                // 916协议分为两种
                // 第一种为服务端发送，触发条件为群主在群组信息内 开/关 进群验证按钮，群组内每个人都能收到
                // 第二种为邀请、申请加入该群组，由邀请人或加入方发送给群主的消息，只有群主可以收到
                if (!TextUtils.isEmpty(chatMessage.getContent()) &&
                        (chatMessage.getContent().equals("0") || chatMessage.getContent().equals("1"))) {// 第一种
                    PreferenceUtils.putBoolean(MyApplication.getContext(),
                            Constants.IS_NEED_OWNER_ALLOW_NORMAL_INVITE_FRIEND + chatMessage.getObjectId(), chatMessage.getContent().equals("1"));
                    if (chatMessage.getContent().equals("1")) {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_group_enable_verify));
                    } else {
                        chatMessage.setContent(mService.getString(R.string.tip_group_disable_verify));
                    }
                    // chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                        ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
                    }
                } else {//  群聊邀请确认消息 我收到该条消息 说明我就是该群的群主 待我审核
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(chatMessage.getObjectId());
                        String isInvite = json.getString("isInvite");
                        if (TextUtils.isEmpty(isInvite)) {
                            isInvite = "0";
                        }
                        if (isInvite.equals("0")) {
                            String id = json.getString("userIds");
                            String[] ids = id.split(",");
                            chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_invite_need_verify_place_holder, chatMessage.getFromUserName(), ids.length));
                        } else {
                            chatMessage.setContent(chatMessage.getFromUserName() + MyApplication.getContext().getString(R.string.tip_need_verify_place_holder));
                        }
                        String roomJid = json.getString("roomJid");
                        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomJid, chatMessage)) {
                            ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, roomJid, chatMessage, true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (type == XmppMessage.TYPE_CHANGE_SHOW_READ) {
                    PreferenceUtils.putBoolean(MyApplication.getContext(),
                            Constants.IS_SHOW_READ + chatMessage.getObjectId(), chatMessage.getContent().equals("1"));
                    if (chatMessage.getContent().equals("1")) {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_enable_read));
                    } else {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_disable_read));
                    }
                } else if (type == XmppMessage.TYPE_GROUP_LOOK) {
                    if (chatMessage.getContent().equals("1")) {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_private));
                    } else {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_public));
                    }
                } else if (type == XmppMessage.TYPE_GROUP_SHOW_MEMBER) {
                    if (chatMessage.getContent().equals("1")) {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_enable_member));
                    } else {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_disable_member));
                    }
                } else if (type == XmppMessage.TYPE_GROUP_SEND_CARD) {
                    PreferenceUtils.putBoolean(MyApplication.getContext(),
                            Constants.IS_SEND_CARD + chatMessage.getObjectId(), chatMessage.getContent().equals("1"));
                    if (chatMessage.getContent().equals("1")) {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_enable_chat_privately));
                    } else {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_disable_chat_privately));
                    }
                    MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getContext());
                } else if (type == XmppMessage.TYPE_GROUP_ALL_SHAT_UP) {
                    PreferenceUtils.putBoolean(MyApplication.getContext(),
                            Constants.GROUP_ALL_SHUP_UP + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
                    if (!chatMessage.getContent().equals("0")) {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_now_ban_all));
                    } else {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_now_disable_ban_all));
                    }
                    MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getContext());
                } else if (type == XmppMessage.TYPE_GROUP_ALLOW_NORMAL_INVITE) {
                    if (!chatMessage.getContent().equals("0")) {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_enable_invite));
                        MsgBroadcast.broadcastMsgRoomUpdateInvite(MyApplication.getContext(), 1);
                    } else {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_disable_invite));
                        MsgBroadcast.broadcastMsgRoomUpdateInvite(MyApplication.getContext(), 0);
                    }
                } else if (type == XmppMessage.TYPE_GROUP_ALLOW_NORMAL_UPLOAD) {
                    PreferenceUtils.putBoolean(MyApplication.getContext(),
                            Constants.IS_ALLOW_NORMAL_SEND_UPLOAD + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
                    if (!chatMessage.getContent().equals("0")) {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_enable_upload));
                    } else {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_disable_upload));
                    }
                } else if (type == XmppMessage.TYPE_GROUP_ALLOW_NORMAL_CONFERENCE) {
                    PreferenceUtils.putBoolean(MyApplication.getContext(),
                            Constants.IS_ALLOW_NORMAL_CONFERENCE + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
                    if (!chatMessage.getContent().equals("0")) {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_enable_meeting));
                    } else {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_disable_meeting));
                    }
                } else if (type == XmppMessage.TYPE_GROUP_ALLOW_NORMAL_SEND_COURSE) {
                    PreferenceUtils.putBoolean(MyApplication.getContext(),
                            Constants.IS_ALLOW_NORMAL_SEND_COURSE + chatMessage.getObjectId(), !chatMessage.getContent().equals("0"));
                    if (!chatMessage.getContent().equals("0")) {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_enable_cource));
                    } else {
                        chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_owner_disable_cource));
                    }
                } else if (type == XmppMessage.TYPE_GROUP_TRANSFER) {
                    chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_new_group_owner_place_holder, toUserName));
                    if (friend != null) {
                        FriendDao.getInstance().updateRoomCreateUserId(mLoginUserId,
                                chatMessage.getObjectId(), chatMessage.getToUserId());
                        RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), chatMessage.getToUserId(), 1);
                    }
                }
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
                }
            }
            return;
        }

        /*
        群内其它设置
         */
        if (type == XmppMessage.TYPE_CHANGE_NICK_NAME) { // 修改群内昵称
            String content = chatMessage.getContent();
            if (!TextUtils.isEmpty(toUserId) && toUserId.equals(mLoginUserId)) {// 我修改了昵称
                if (!TextUtils.isEmpty(content)) {
                    friend.setRoomMyNickName(content);
                    FriendDao.getInstance().updateRoomMyNickName(friend.getUserId(), content);
                    ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), toUserId, content);
                    ChatMessageDao.getInstance().updateNickName(mLoginUserId, friend.getUserId(), toUserId, content);
                }
                // 自己改了昵称也要留一条消息，
                chatMessage.setContent(toUserName + " " + MyApplication.getInstance().getString(R.string.message_object_update_nickname) + "‘" + content + "’");
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
            } else {  // 其他人修改了昵称，通知下就可以了
                chatMessage.setContent(toUserName + " " + MyApplication.getInstance().getString(R.string.message_object_update_nickname) + "‘" + content + "’");
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                }
                ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), toUserId, content);
                ChatMessageDao.getInstance().updateNickName(mLoginUserId, friend.getUserId(), toUserId, content);
            }
        } else if (type == XmppMessage.TYPE_CHANGE_ROOM_NAME) {
            // 修改房间名、更新朋友表
            String content = chatMessage.getContent();
            FriendDao.getInstance().updateMucFriendRoomName(friend.getUserId(), content);
            ListenerManager.getInstance().notifyNickNameChanged(friend.getUserId(), "ROOMNAMECHANGE", content);

            chatMessage.setContent(fromUserName + " " + MyApplication.getInstance().getString(R.string.Message_Object_Update_RoomName) + content);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.TYPE_DELETE_ROOM) {// 群主解散该群
            if (fromUserId.equals(toUserId)) {
                // 我为群主
                FriendDao.getInstance().deleteFriend(mLoginUserId, chatMessage.getObjectId());
                // 消息表中删除
                ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, chatMessage.getObjectId());
                RoomMemberDao.getInstance().deleteRoomMemberTable(chatMessage.getObjectId());
                // 通知界面更新
                MsgBroadcast.broadcastMsgNumReset(mService);
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                MucgroupUpdateUtil.broadcastUpdateUi(mService);
            } else {
                mService.exitMucChat(chatMessage.getObjectId());
                // 2 标志该群已被解散  更新朋友表
                FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, friend.getUserId(), 2);
                chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_disbanded));
                if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                    ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
                }
            }
            ListenerManager.getInstance().notifyDeleteMucRoom(chatMessage.getObjectId());
        } else if (type == XmppMessage.TYPE_DELETE_MEMBER) {
            // 群组 退出 || 踢人
            if (toUserId.equals(mLoginUserId)) {
                // 被xx踢出了群组
                // todo 针对自己消息的在XChatMessageListener内已经处理了，此处直接return，
                //  因为XChatListenerManager有一个hasSameMessage的判断，如不return掉可能影响真正处理此消息的地方
                return;
            } else {
                // 其他人退出 || 被踢出
                if (fromUserId.equals(toUserId)) {
                    chatMessage.setContent(toUserName + " " + MyApplication.getInstance().getString(R.string.quit_group));
                } else {
                    chatMessage.setContent(toUserName + " " + MyApplication.getInstance().getString(R.string.kicked_out_group));
                }
                // 更新RoomMemberDao、更新群聊界面
                operatingRoomMemberDao(1, friend.getRoomId(), toUserId, null);
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getContext());
            }

            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.TYPE_NEW_NOTICE
                || type == XmppMessage.TYPE_EDIT_GROUP_NOTICE) { // 发布公告 || 编辑
            EventBus.getDefault().post(new EventNewNotice(chatMessage));
            String content = chatMessage.getContent();
            if (type == XmppMessage.TYPE_NEW_NOTICE) {
                chatMessage.setContent(fromUserName + " " + MyApplication.getInstance().getString(R.string.Message_Object_Add_NewAdv) + content);
            } else {
                chatMessage.setContent(fromUserName + " " + MyApplication.getInstance().getString(R.string.edit_group_notice) + content);
            }
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.TYPE_GAG) {// 禁言
            long time = Long.parseLong(chatMessage.getContent());
            if (toUserId != null && toUserId.equals(mLoginUserId)) {
                // todo 针对自己消息的在XChatMessageListener内已经处理了，为了防止加群后拉群组离线消息又拉到该条消息，针对自己的不处理
            }

            // 为防止其他用户接收不及时，给3s的误差
            if (time > (System.currentTimeMillis() / 1000) + 3) {
                String formatTime = XfileUtils.fromatTime((time * 1000), "MM-dd HH:mm");
                chatMessage.setContent(fromUserName + " " + MyApplication.getInstance().getString(R.string.message_object_yes) + toUserName +
                        MyApplication.getInstance().getString(R.string.Message_Object_Set_Gag_With_Time) + formatTime);
            } else {
                chatMessage.setContent(toUserName + MyApplication.getContext().getString(R.string.tip_been_cancel_ban_place_holder, fromUserName));
            }

            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
            }
        } else if (type == XmppMessage.NEW_MEMBER) {
            String desc = "";
            if (chatMessage.getFromUserId().equals(toUserId)) {
                // 主动加入
                desc = fromUserName + " " + MyApplication.getInstance().getString(R.string.Message_Object_Group_Chat);
            } else {
                // 被邀请加入
                desc = fromUserName + " " + MyApplication.getInstance().getString(R.string.message_object_inter_friend) + toUserName;

                String roomId = jsonObject.getString("fileName");
                if (!toUserId.equals(mLoginUserId)) {// 被邀请人为自己时不能更新RoomMemberDao，如更新了，在群聊界面判断出该表有人而不会在去调用接口获取该群真实的人数了
                    operatingRoomMemberDao(0, roomId, chatMessage.getToUserId(), toUserName);
                }
            }

            // todo 针对自己消息的在XChatMessageListener内已经处理了，为了防止加群后拉群组离线消息又拉到该条消息，针对自己的不处理

            // 更新数据库
            chatMessage.setContent(desc);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, chatMessage.getObjectId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
                MsgBroadcast.broadcastMsgRoomUpdateGetRoomStatus(MyApplication.getContext());
            }
        } else if (type == XmppMessage.TYPE_SEND_MANAGER) {
            String content = chatMessage.getContent();
            int role;
            if (content.equals("1")) {
                role = 2;
                chatMessage.setContent(fromUserName + " " + MyApplication.getInstance().getString(R.string.setting) + toUserName + " " + MyApplication.getInstance().getString(R.string.message_admin));
            } else {
                role = 3;
                chatMessage.setContent(fromUserName + " " + MyApplication.getInstance().getString(R.string.sip_canceled) + toUserName + " " + MyApplication.getInstance().getString(R.string.message_admin));
            }

            RoomMemberDao.getInstance().updateRoomMemberRole(friend.getRoomId(), toUserId, role);
            if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage)) {
                ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, friend.getUserId(), chatMessage, true);
                Intent intent = new Intent();
                intent.putExtra("roomId", friend.getUserId());
                intent.putExtra("toUserId", chatMessage.getToUserId());
                intent.putExtra("isSet", content.equals("1"));
                intent.setAction(com.sk.weichat.broadcast.OtherBroadcast.REFRESH_MANAGER);
                mService.sendBroadcast(intent);
            }
        }
    }

    /**
     * 直播消息处理
     */
    private void chatLive(String body, ChatMessage chatMessage) {
        JSONObject mJSONObject = JSONObject.parseObject(body);
        String toUserId = mJSONObject.getString("toUserId");
        String toUserName = mJSONObject.getString("toUserName");
        int type = chatMessage.getType();
        if (type == TYPE_SEND_DANMU) {// 弹幕
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("danmu", chatMessage.getContent());
            bundle.putString("fromUserId", chatMessage.getFromUserId());
            bundle.putString("fromUserName", chatMessage.getFromUserName());
            intent.putExtras(bundle);
            intent.setAction(LiveConstants.LIVE_DANMU_DRAWABLE);
            mService.sendBroadcast(intent);
        } else if (type == XmppMessage.TYPE_SEND_GIFT) {// 礼物
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("gift", chatMessage.getContent());
            bundle.putString("fromUserId", chatMessage.getFromUserId());
            bundle.putString("fromUserName", chatMessage.getFromUserName());
            intent.putExtras(bundle);
            intent.setAction(LiveConstants.LIVE_SEND_GIFT);
            mService.sendBroadcast(intent);
        } else if (type == XmppMessage.TYPE_SEND_HEART) {// 点赞
            Intent intent = new Intent();
            intent.setAction(LiveConstants.LIVE_SEND_LOVE_HEART);
            mService.sendBroadcast(intent);
        } else if (type == XmppMessage.TYPE_LIVE_LOCKING) {// 锁定直播间
            Intent intent = new Intent();
            intent.setAction(LiveConstants.LIVE_SEND_LOCKED);
            mService.sendBroadcast(intent);

            chatMessage.setType(XmppMessage.TYPE_TIP);
            chatMessage.setContent(toUserName + mService.getString(R.string.suffix_live_room_locked));
            ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
        } else if (type == XmppMessage.TYPE_SEND_ENTER_LIVE_ROOM) {// 加入直播间
            Intent intent = new Intent();
            intent.setAction(LiveConstants.LIVE_MEMBER_ADD);
            mService.sendBroadcast(intent);

            chatMessage.setType(XmppMessage.TYPE_TIP);
            chatMessage.setContent(toUserName + mService.getString(R.string.suffix_join_live_room));
            ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
        } else if (chatMessage.getType() == XmppMessage.TYPE_LIVE_EXIT_ROOM) {// 退出、被提出直播间
            Intent intent = new Intent();
            intent.putExtra("fromUserId", chatMessage.getFromUserId());
            intent.putExtra("toUserId", toUserId);
            intent.setAction(LiveConstants.LIVE_MEMBER_DELETE);
            mService.sendBroadcast(intent);
            chatMessage.setType(XmppMessage.TYPE_TIP);
            if (chatMessage.getFromUserId().equals(toUserId)) {// 退出直播间
                chatMessage.setContent(toUserName + " " + MyApplication.getInstance().getString(R.string.exited_live_room));
            } else {// 被踢出直播间
                chatMessage.setContent(toUserName + " " + MyApplication.getInstance().getString(R.string.live_kicklive));
            }
            ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
        } else if (type == XmppMessage.TYPE_LIVE_SHAT_UP) {// 禁言
            long time = Long.parseLong(chatMessage.getContent());
            // 发送广播到直播间,禁言/取消禁言该人
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("state", chatMessage.getContent());
            bundle.putString("fromUserId", chatMessage.getFromUserId());
            bundle.putString("toUserId", toUserId);
            intent.putExtras(bundle);
            intent.setAction(LiveConstants.LIVE_SEND_SHUT_UP);
            mService.sendBroadcast(intent);

            chatMessage.setType(XmppMessage.TYPE_TIP);
            if (time == 0l) {
                chatMessage.setContent(chatMessage.getFromUserName() + " " + MyApplication.getInstance().getString(R.string.message_object_yes) + toUserName +
                        MyApplication.getInstance().getString(R.string.message_object_cancel_gag));
            } else {
                chatMessage.setContent(toUserName + " " + MyApplication.getInstance().getString(R.string.has_been_banned));
            }
            ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
        } else if (chatMessage.getType() == XmppMessage.TYPE_LIVE_SET_MANAGER) { // 设置/取消管理员
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("toUserId", toUserId);
            intent.putExtras(bundle);
            intent.setAction(LiveConstants.LIVE_SEND_MANAGER);
            mService.sendBroadcast(intent);

            String content = chatMessage.getContent();
            chatMessage.setType(XmppMessage.TYPE_TIP);
            if (content.equals("1")) {
                chatMessage.setContent(chatMessage.getFromUserName() + " " + MyApplication.getInstance().getString(R.string.setting) + toUserName + " " + MyApplication.getInstance().getString(R.string.message_admin));
            } else {
                chatMessage.setContent(chatMessage.getFromUserName() + " " + MyApplication.getInstance().getString(R.string.sip_canceled) + toUserName + " " + MyApplication.getInstance().getString(R.string.message_admin));
            }
            ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, chatMessage.getObjectId(), chatMessage, true);
        }
    }

    private String getName(Friend friend, String userId) {
        if (friend == null) {
            return null;
        }
        RoomMember mRoomMember = RoomMemberDao.getInstance().getSingleRoomMember(friend.getRoomId(), mLoginUserId);
        if (mRoomMember != null && mRoomMember.getRole() == 1) {// 我为群主 Name显示为群内备注
            RoomMember member = RoomMemberDao.getInstance().getSingleRoomMember(friend.getRoomId(), userId);
            if (member != null && !TextUtils.equals(member.getUserName(), member.getCardName())) {
                // 当userName与cardName不一致时，我们认为群主有设置群内备注
                return member.getCardName();
            } else {
                Friend mFriend = FriendDao.getInstance().getFriend(mLoginUserId, userId);
                if (mFriend != null && !TextUtils.isEmpty(mFriend.getRemarkName())) {
                    return mFriend.getRemarkName();
                }
            }
        } else {// 为好友 显示备注
            Friend mFriend = FriendDao.getInstance().getFriend(mLoginUserId, userId);
            if (mFriend != null && !TextUtils.isEmpty(mFriend.getRemarkName())) {
                return mFriend.getRemarkName();
            }
        }
        return null;
    }

    // 更新群成员表
    private void operatingRoomMemberDao(int type, String roomId, String userId, String userName) {
        if (type == 0) {
            RoomMember roomMember = new RoomMember();
            roomMember.setRoomId(roomId);
            roomMember.setUserId(userId);
            roomMember.setUserName(userName);
            roomMember.setCardName(userName);
            roomMember.setRole(3);
            roomMember.setCreateTime(0);
            RoomMemberDao.getInstance().saveSingleRoomMember(roomId, roomMember);
        } else {
            RoomMemberDao.getInstance().deleteRoomMember(roomId, userId);
        }
    }
}
