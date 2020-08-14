package com.sk.weichat.call;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.sk.weichat.MyApplication;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.AppUtils;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Audio Video Call Meet Controller
 */
public class AudioOrVideoController {
    private static AudioOrVideoController instance;
    private Context mContext;
    private CoreManager mCoreManager;

    private AudioOrVideoController(Context context, CoreManager coreManager) {
        this.mContext = context;
        this.mCoreManager = coreManager;
        EventBus.getDefault().register(this);
    }

    public static AudioOrVideoController init(Context context, CoreManager coreManager) {
        if (instance == null) {
            instance = new AudioOrVideoController(context, coreManager);
        }
        return instance;
    }

    /**
     * 我方取消、挂断通话后发送XMPP消息给对方
     * <p>
     * 在断点调试禅道bug4503、4505时发现，本地居然存在了两个CoreManager，且该CoreManager内的对象都为空，导致取消、挂断的消息发送不出去
     * 暂无法查明出现两个CoreManager的原因，现将该 event事件 移至MainActivity内处理
     */
    /*@Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventCancelOrHangUp event) {
        String mLoginUserId = mCoreManager.getSelf().getUserId();
        ChatMessage message = new ChatMessage();
        if (event.type == 103) {          // 取消 语音通话
            message.setType(XmppMessage.TYPE_NO_CONNECT_VOICE);
        } else if (event.type == 104) {// 取消 视频通话
            message.setType(XmppMessage.TYPE_END_CONNECT_VOICE);
        } else if (event.type == 113) {// 挂断 语音通话
            message.setType(XmppMessage.TYPE_NO_CONNECT_VIDEO);
        } else if (event.type == 114) {// 挂断 视频通话
            message.setType(XmppMessage.TYPE_END_CONNECT_VIDEO);
        }
        message.setMySend(true);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mCoreManager.getSelf().getNickName());
        message.setToUserId(event.toUserId);
        message.setContent(event.content);
        message.setTimeLen(event.callTimeLen);
        message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));

        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, event.toUserId, message)) {
            ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, message.getFromUserId(), message, false);
        }

        mCoreManager.sendChatMessage(event.toUserId, message);
        MsgBroadcast.broadcastMsgUiUpdate(mContext);   // 更新消息界面

        *//*if (isSipback) {// 当app处于关闭状态收到来电，通话结束后终止程序
            ActivityStack.getInstance().exit();
            android.os.Process.killProcess(android.os.Process.myPid());
        }*//*
    }*/

    // 单聊 通话 来电
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventSipEVent messsage) {
        if (messsage.message.getType() == XmppMessage.TYPE_IS_CONNECT_VOICE
                || messsage.message.getType() == XmppMessage.TYPE_IS_CONNECT_VIDEO
                || messsage.message.getType() == XmppMessage.TYPE_IS_CONNECT_SCREEN) {
            if (!JitsistateMachine.isInCalling) {
                Intent intent = new Intent(mContext, JitsiIncomingcall.class);
                if (messsage.message.getType() == XmppMessage.TYPE_IS_CONNECT_VOICE) {
                    intent.putExtra(CallConstants.AUDIO_OR_VIDEO_OR_MEET, CallConstants.Audio);
                } else if (messsage.message.getType() == XmppMessage.TYPE_IS_CONNECT_VIDEO) {
                    intent.putExtra(CallConstants.AUDIO_OR_VIDEO_OR_MEET, CallConstants.Video);
                } else if (messsage.message.getType() == XmppMessage.TYPE_IS_CONNECT_SCREEN) {
                    intent.putExtra(CallConstants.AUDIO_OR_VIDEO_OR_MEET, CallConstants.Screen);
                }
                intent.putExtra("fromuserid", messsage.touserid);
                intent.putExtra("touserid", messsage.touserid);
                intent.putExtra("name", messsage.message.getFromUserName());
                if (!TextUtils.isEmpty(messsage.message.getFilePath())) {
                    intent.putExtra("meetUrl", messsage.message.getFilePath());
                }
                if (!AppUtils.isAppForeground(MyApplication.getContext())) {
                    intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
                }
                mContext.startActivity(intent);
            }
        } else if (messsage.message.getType() == XmppMessage.TYPE_NO_CONNECT_VOICE
                || messsage.message.getType() == XmppMessage.TYPE_NO_CONNECT_VIDEO
                || messsage.message.getType() == XmppMessage.TYPE_NO_CONNECT_SCREEN) {
            Log.e("AVI", "收到对方取消协议");
            if (messsage.message.getTimeLen() == 0) {
                EventBus.getDefault().post(new MessageHangUpPhone(messsage.message));
            }
        }
    }

    // 群聊 会议 邀请
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventMeetingInvited event) {
        if (!JitsistateMachine.isInCalling) {
            Intent intent = new Intent(mContext, JitsiIncomingcall.class);
            intent.putExtra(CallConstants.AUDIO_OR_VIDEO_OR_MEET, event.type);
            intent.putExtra("fromuserid", event.message.getObjectId());
            intent.putExtra("touserid", event.message.getFromUserId());
            intent.putExtra("name", event.message.getFromUserName());
            mContext.startActivity(intent);
        }
    }

    public void release() {
        EventBus.getDefault().unregister(this);
    }
}
