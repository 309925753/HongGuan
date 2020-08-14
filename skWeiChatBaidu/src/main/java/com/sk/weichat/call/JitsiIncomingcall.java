package com.sk.weichat.call;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.broadcast.MsgBroadcast;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.message.ChatActivity;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.xmpp.ListenerManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

import static com.sk.weichat.R.id.call_answer;
import static com.sk.weichat.R.id.call_avatar;
import static com.sk.weichat.R.id.call_hang_up;
import static com.sk.weichat.R.id.call_invite_type;
import static com.sk.weichat.R.id.call_name;

/**
 * 来电显示
 */
public class JitsiIncomingcall extends BaseActivity implements View.OnClickListener {
    Timer timer = new Timer();
    private String mLoginUserId;
    private String mLoginUserName;
    private int mCallType;
    private String call_fromUser;
    private String call_toUser;
    private String call_Name;
    private String meetUrl;
    private AnimationDrawable talkingRippleDrawable;

    private AssetFileDescriptor mAssetFileDescriptor;
    private MediaPlayer mediaPlayer;
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    abort();
                    if (CallConstants.isSingleChat(mCallType)) {//  来电界面显示 三十秒内 不主动响应  发送挂断消息给对方
                        sendHangUpMessage();
                    }
                    JitsistateMachine.reset();
                    finish();
                }
            });
        }
    };
    private ImageView mInviteAvatar;
    private TextView mInviteName;
    private TextView mInviteInfo;
    private ImageButton mAnswer; // 接听
    private ImageButton mHangUp; // 挂断
    private boolean isAllowBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 自动解锁屏幕 | 锁屏也可显示 | Activity启动时点亮屏幕 | 保持屏幕常亮
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.view_call_trying);
        initData();
        initView();
        timer.schedule(timerTask, 30000, 30000);
        EventBus.getDefault().register(this);
        setSwipeBackEnable(false);
    }

    private void initData() {
        mLoginUserId = coreManager.getSelf().getUserId();
        mLoginUserName = coreManager.getSelf().getNickName();

        mCallType = getIntent().getIntExtra(CallConstants.AUDIO_OR_VIDEO_OR_MEET, 0);
        call_fromUser = getIntent().getStringExtra("fromuserid");
        call_toUser = getIntent().getStringExtra("touserid");
        call_Name = getIntent().getStringExtra("name");
        meetUrl = getIntent().getStringExtra("meetUrl");

        JitsistateMachine.isInCalling = true;
        JitsistateMachine.callingOpposite = call_toUser;

        bell();
    }

    private void initView() {
        findViewById(R.id.change).setOnClickListener(this);
        findViewById(R.id.replay_message).setOnClickListener(this);
        AnimationDrawable talkingRippleDrawable = getTalkingRippleDrawable();
        talkingRippleDrawable.start();
        ImageView ivTalkingRipple = findViewById(R.id.ivTalkingRipple);
        ivTalkingRipple.setImageDrawable(talkingRippleDrawable);
        mInviteAvatar = (ImageView) findViewById(call_avatar);
        mInviteName = (TextView) findViewById(call_name);
        mInviteInfo = (TextView) findViewById(call_invite_type);
        mAnswer = (ImageButton) findViewById(call_answer);
        mHangUp = (ImageButton) findViewById(call_hang_up);
        AvatarHelper.getInstance().displayAvatar(call_toUser, mInviteAvatar, true);
        mInviteName.setText(call_Name);
        if (mCallType == CallConstants.Audio) {
            mInviteInfo.setText(getString(R.string.suffix_invite_you_voice));
            findViewById(R.id.rlReplayMessage).setVisibility(View.INVISIBLE);
            findViewById(R.id.rlChange).setVisibility(View.INVISIBLE);
            ImageView ivChange = findViewById(R.id.change);
            ivChange.setBackgroundResource(R.mipmap.switching_video_call);
            TextView tvChange = findViewById(R.id.change_tv);
            tvChange.setText(R.string.btn_meet_type_change_to_video);
        } else if (mCallType == CallConstants.Video) {
            mInviteInfo.setText(getString(R.string.suffix_invite_you_video));
            findViewById(R.id.rlReplayMessage).setVisibility(View.VISIBLE);
            findViewById(R.id.rlChange).setVisibility(View.VISIBLE);
        } else if (mCallType == CallConstants.Screen) {
            mInviteInfo.setText(getString(R.string.suffix_invite_you_screen));
        } else if (mCallType == CallConstants.Audio_Meet) {
            mInviteInfo.setText(getString(R.string.tip_invite_voice_meeting));
        } else if (mCallType == CallConstants.Video_Meet) {
            mInviteInfo.setText(getString(R.string.tip_invite_video_meeting));
        } else if (mCallType == CallConstants.Talk_Meet) {
            mInviteInfo.setText(getString(R.string.tip_invite_talk_meeting));
        } else if (mCallType == CallConstants.Screen_Meet) {
            mInviteInfo.setText(getString(R.string.tip_invite_screen_meeting));
        }
        mAnswer.setOnClickListener(this);
        mHangUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change:// 语音视频切换并接通，
                abort();
                if (coreManager.isLogin()) {
                    if (CallConstants.isSingleChat(mCallType)) {// 通话 接听 告诉 对方，会议不需要
                        sendAnswerMessage(true);
                    }
                    // 语音和视频切换，
                    if (mCallType == CallConstants.Audio) {
                        mCallType = CallConstants.Video;
                    } else if (mCallType == CallConstants.Video) {
                        mCallType = CallConstants.Audio;
                    }
                    Jitsi_connecting_second.start(this, call_fromUser, call_toUser, mCallType, meetUrl, true);
                    finish();
                }
                break;
            case R.id.replay_message:// 快速回复，
                Dialog bottomDialog = new Dialog(this, R.style.BottomDialog);
                View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_meet_replay, null);
                bottomDialog.setContentView(contentView);
                ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
                layoutParams.width = getResources().getDisplayMetrics().widthPixels;
                contentView.setLayoutParams(layoutParams);
                bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
                bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
                bottomDialog.show();
                contentView.findViewById(R.id.dialog_select_cancel).setOnClickListener(v -> bottomDialog.dismiss());
                contentView.findViewById(R.id.tvHangUpChat).setOnClickListener(v -> {
                    ChatActivity.start(this, FriendDao.getInstance().getFriend(mLoginUserId, call_toUser));
                    bottomDialog.dismiss();
                    mHangUp.callOnClick();
                });
                RecyclerView recyclerView = contentView.findViewById(R.id.recyclerView);
                DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
                divider.setDrawable(getResources().getDrawable(R.drawable.full_divider));
                recyclerView.addItemDecoration(divider);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                List<Item> data = new ArrayList<>();
                data.add(new Item(getString(R.string.default_quick_replay_1)));
                data.add(new Item(getString(R.string.default_quick_replay_2)));
                data.add(new Item(getString(R.string.default_quick_replay_3)));
                recyclerView.setAdapter(new MessageReplayAdapter(this, data, item -> {
                    sendReplay(item.message);
                    bottomDialog.dismiss();
                    mHangUp.callOnClick();
                }));
                break;
            case R.id.call_answer:// 接听
                abort();
                if (coreManager.isLogin()) {
                    if (CallConstants.isSingleChat(mCallType)) {// 通话 接听 告诉 对方，会议不需要
                        sendAnswerMessage();
                    }
                    Jitsi_connecting_second.start(this, call_fromUser, call_toUser, mCallType, meetUrl, true);
                    finish();
                }
                break;
            case R.id.call_hang_up:// 拒绝
                abort();
                if (coreManager.isLogin()) {
                    if (CallConstants.isSingleChat(mCallType)) {// 通话 拒绝 告诉 对方，会议不需要
                        sendHangUpMessage();
                    }
                }
                JitsistateMachine.reset();
                finish();
                break;
        }
    }

    private AnimationDrawable getTalkingRippleDrawable() {
        if (talkingRippleDrawable != null) {
            return talkingRippleDrawable;
        }
        talkingRippleDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.talk_btn_frame_busy_ripple);
        return talkingRippleDrawable;
    }

    private void sendReplay(String text) {
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_TEXT);
        message.setContent(text);
        message.setFromUserId(mLoginUserId);
        message.setToUserId(call_toUser);
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        message.setFromUserName(mLoginUserName);
        message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, call_toUser, message)) {
            ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, call_toUser, message, false);
        }
        coreManager.sendChatMessage(call_toUser, message);
        MsgBroadcast.broadcastMsgUiUpdate(this);  // 更新消息界面
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageHangUpPhone message) {// 对方取消了 || 其他端 (接听 || 取消)了
        if (message.chatMessage.getTimeSend()
                < JitsistateMachine.lastTimeInComingCall) {
            Log.e("AVI", "该协议的timeSend小于最后一次拨号唤醒来电界面的timeSend，不处理");
            return;
        }
        if (message.chatMessage.getFromUserId().equals(call_toUser)
                || message.chatMessage.getFromUserId().equals(mLoginUserId)) {
            abort();
            JitsistateMachine.reset();
            finish();
            /*if (isSipback) {// 当app处于关闭状态收到来电，通话结束后终止程序
                ActivityStack.getInstance().exit();
                android.os.Process.killProcess(android.os.Process.myPid());
            }*/
        }
    }

    private void sendAnswerMessage() {
        sendAnswerMessage(false);
    }

    private void sendAnswerMessage(boolean change) {
        ChatMessage message = new ChatMessage();
        if (mCallType == CallConstants.Audio) {
            message.setType(XmppMessage.TYPE_CONNECT_VOICE);
        } else if (mCallType == CallConstants.Video) {
            message.setType(XmppMessage.TYPE_CONNECT_VIDEO);
        } else if (mCallType == CallConstants.Screen) {
            message.setType(XmppMessage.TYPE_CONNECT_SCREEN);
        }
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginUserName);
        message.setToUserId(call_toUser);
        message.setContent(change ? "1" : "0");
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        coreManager.sendChatMessage(call_toUser, message);
    }

    private void sendHangUpMessage() {
        ChatMessage message = new ChatMessage();
        if (mCallType == CallConstants.Audio) {
            message.setType(XmppMessage.TYPE_NO_CONNECT_VOICE);
        } else if (mCallType == CallConstants.Video) {
            message.setType(XmppMessage.TYPE_NO_CONNECT_VIDEO);
        } else if (mCallType == CallConstants.Screen) {
            message.setType(XmppMessage.TYPE_NO_CONNECT_SCREEN);
        }
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginUserName);
        message.setToUserId(call_toUser);
        message.setMySend(true);
        // timeLen非0表示接电话方挂断，
        message.setTimeLen(1);
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        message.setContent(getString(R.string.sip_refused));
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, call_toUser, message)) {
            ListenerManager.getInstance().notifyNewMesssage(mLoginUserId, call_toUser, message, false);
        }

        coreManager.sendChatMessage(call_toUser, message);
        MsgBroadcast.broadcastMsgUiUpdate(this);  // 更新消息界面
    }

    private void bell() {
        try {
            mAssetFileDescriptor = getAssets().openFd("dial.mp3");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mAssetFileDescriptor.getFileDescriptor(), mAssetFileDescriptor.getStartOffset(), mAssetFileDescriptor.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer arg0) {
                    mediaPlayer.start();
                    mediaPlayer.setLooping(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void abort() {
        if (timer != null) {
            timer.cancel();
        }
        try {
            mediaPlayer.stop();
        } catch (Exception e) {
            // 在华为手机上疯狂点击挂断按钮会出现崩溃的情况
            Reporter.post("停止铃声出异常，", e);
        }
        mediaPlayer.release();
    }

    @Override
    public void onBackPressed() {
        if (isAllowBack) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mAssetFileDescriptor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventBus.getDefault().unregister(this);
    }

    interface OnReplayListener {
        void onReplay(Item item);
    }

    static class MessageReplayAdapter extends RecyclerView.Adapter<ViewHolder> {
        private final Context ctx;
        private final OnReplayListener listener;
        private final List<Item> data;

        public MessageReplayAdapter(Context ctx, List<Item> data, OnReplayListener listener) {
            this.ctx = ctx;
            this.listener = listener;
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(ctx).inflate(R.layout.item_dialog_meet_replay, viewGroup, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            Item item = data.get(i);
            viewHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReplay(item);
                }
            });
            viewHolder.tvMessage.setText(item.message);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage = itemView.findViewById(R.id.tvMessage);

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class Item {
        String message;

        public Item(String message) {
            this.message = message;
        }
    }
}
