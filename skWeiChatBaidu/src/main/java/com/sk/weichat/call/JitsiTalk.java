package com.sk.weichat.call;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.react.modules.core.PermissionListener;
import com.sk.weichat.R;
import com.sk.weichat.bean.VideoFile;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.call.talk.MessageTalkJoinEvent;
import com.sk.weichat.call.talk.MessageTalkLeftEvent;
import com.sk.weichat.call.talk.MessageTalkOnlineEvent;
import com.sk.weichat.call.talk.MessageTalkReleaseEvent;
import com.sk.weichat.call.talk.MessageTalkRequestEvent;
import com.sk.weichat.call.talk.TalkUserAdapter;
import com.sk.weichat.call.talk.Talking;
import com.sk.weichat.db.dao.VideoFileDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.CutoutHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.view.TipDialog;

import org.jitsi.meet.sdk.BuildConfig;
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate;
import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * 2018-2-27 录屏，保存至本地视频
 */
public class JitsiTalk extends BaseActivity implements JitsiMeetActivityInterface {
    private static final String TAG = "JitsiTalk";
    // 屏幕录制
    private static final int RECORD_REQUEST_CODE = 0x01;
    private static final int SEND_ONLINE_STATUS = 756;
    // 计时，给悬浮窗调用
    public static String time = null;
    private String mLocalHostJitsi = "https://meet.jit.si/";// 官网地址
    private String mLocalHost/* = "https://meet.youjob.co/"*/;  // 本地地址,现改为变量
    private String fromUserId;
    private String toUserId;
    private long startTime = System.currentTimeMillis();// 通话开始时间
    private long stopTime; // 通话结束时间
    private FrameLayout mFrameLayout;
    private JitsiMeetView mJitsiMeetView;
    // 标记当前手机版本是否为android 5.0,且为对方挂断
    private boolean isApi21HangUp;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("mm:ss");
    CountDownTimer mCountDownTimer = new CountDownTimer(18000000, 1000) {// 开始计时，用于显示在悬浮窗上，且每隔一秒发送一个广播更新悬浮窗
        @Override
        public void onTick(long millisUntilFinished) {
            time = formatTime();
            JitsiTalk.this.sendBroadcast(new Intent(CallConstants.REFRESH_FLOATING));
        }

        @Override
        public void onFinish() {// 12小时进入Finish

        }
    };
    private boolean isOldVersion = true;// 是否为老版本，如果一次 "通话中" 消息都没有收到，就判断对方使用的为老版本，自己也停止ping且不做检测
    private boolean isEndCallOpposite;// 对方是否结束了通话
    private int mPingReceiveFailCount;// 未收到对方发送 "通话中" 消息的次数
    private View btnHangUp;
    private View llTalkFree;
    private ImageView ivTalkingRipple;
    private ImageView vhCurrentHead;
    private RecyclerView rvUserList;
    private View btnTalk;
    private ImageView ivTalk;
    private TextView tvTip;
    private TalkUserAdapter userAdapter;
    // 当前占线情况，
    @Nullable
    private Talking talking;
    // 加入jitsi前不允许各种操作，
    private boolean talkReady = false;
    // 用于限制抢麦和取消频率最多一秒一次，
    private RequestTalkTimer requestTalkTimer;
    private Handler sendOnlineHandler = new SendOnlineHandler(this);
    /**
     * 用于判断主讲人掉线，
     */
    @Nullable
    private TalkerOnlineTimer talkerOnlineTimer;
    private AnimationDrawable talkingRippleDrawable;

    public static void start(Context ctx, String room, boolean isVideo) {
        Intent intent = new Intent(ctx, JitsiTalk.class);
        if (isVideo) {
            intent.putExtra("type", 2);
        } else {
            intent.putExtra("type", 1);
        }
        intent.putExtra("fromuserid", room);
        intent.putExtra("touserid", room);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CutoutHelper.setWindowOut(getWindow());
        super.onCreate(savedInstanceState);
        // 自动解锁屏幕 | 锁屏也可显示 | Activity启动时点亮屏幕 | 保持屏幕常亮
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_jitsi_talk);
        initData();
        initView();
        initEvent();
        initTalkView();
        EventBus.getDefault().register(this);
        JitsiMeetActivityDelegate.onHostResume(this);
        setSwipeBackEnable(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initTalkView() {
        CutoutHelper.initCutoutHolderTop(getWindow(), findViewById(R.id.vCutoutHolder));
        ivTalkingRipple = findViewById(R.id.ivTalkingRipple);
        llTalkFree = findViewById(R.id.llTalkFree);
        btnHangUp = findViewById(R.id.btnHangUp);
        vhCurrentHead = findViewById(R.id.ivCurrentHead);
        rvUserList = findViewById(R.id.rvUserList);
        rvUserList.setLayoutManager(new GridLayoutManager(this, 3));
        rvUserList.getItemAnimator().setChangeDuration(0);
        userAdapter = new TalkUserAdapter(this);
        rvUserList.setAdapter(userAdapter);

        userAdapter.add(TalkUserAdapter.Item.fromUser(coreManager.getSelf()));

        btnTalk = findViewById(R.id.btnTalk);
        ivTalk = findViewById(R.id.ivTalk);
        tvTip = findViewById(R.id.tvTip);

        btnTalk.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    requestTalk();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    releaseTalk();
                    break;
            }
            return true;
        });

        btnHangUp.setOnClickListener((v) -> {
            leaveJitsi();
        });

        updateTalking();
    }

    private void talkReady() {
        talkReady = true;
        Log.i(TAG, "talkReady() called");
        sendJoinTalkMessage();
        tvTip.setText(R.string.tip_talk_press_in);
    }

    private void talkLeave() {
        if (!talkReady) {
            return;
        }
        talkReady = false;
        Log.i(TAG, "talkLeave() called");
        sendLeftTalkMessage();
    }

    private void releaseTalk() {
        if (!talkReady) {
            return;
        }
        if (requestTalkTimer != null) {
            requestTalkTimer.setStatusReleaseTalk();
            return;
        }
        if (talking != null && TextUtils.equals(talking.userId, getMyUserId())) {
            double releaseTime = TimeUtils.sk_time_current_time_double();
            double talkLength = releaseTime - talking.requestTime;
            // adapter中使用同一个talking对象才能这么用，
            talking.talkLength = talkLength;
            talking = null;
            onTalkFree();
            updateTalking();
            sendReleaseTalkMessage(releaseTime);
            if (requestTalkTimer == null) {
                requestTalkTimer = new RequestTalkTimer(false);
                requestTalkTimer.start();
            }
        } else {
            Log.i(TAG, "releaseTalk: 不是自己占线");
        }
    }

    private void onTalkFree() {
        btnTalk.setBackgroundResource(R.drawable.talk_btn_frame_free);
        ivTalk.setImageResource(R.mipmap.icon_talk_microphone);
        tvTip.setTextColor(getResources().getColor(R.color.black));
        mJitsiMeetView.setAudioMuted();
    }

    private boolean myTalking() {
        return talking != null && TextUtils.equals(talking.userId, getMyUserId());
    }

    private void onTalking() {
        btnTalk.setBackgroundResource(R.drawable.talk_btn_frame_busy);
        ivTalk.setImageResource(R.mipmap.icon_talk_microphone_red);
        tvTip.setTextColor(0xffFF6565);
        mJitsiMeetView.setAudioEnable();
    }

    private void requestTalk() {
        if (!talkReady) {
            return;
        }
        if (requestTalkTimer != null) {
            requestTalkTimer.setStatusTalking();
            return;
        }
        if (talking == null) {
            talking = new Talking(getMyName(), getMyUserId(), TimeUtils.sk_time_current_time_double());
            onTalking();
            updateTalking();
            sendRequestTalkMessage(talking.requestTime);
            if (requestTalkTimer == null) {
                requestTalkTimer = new RequestTalkTimer(true);
                requestTalkTimer.start();
            }
            // 主讲人要每5秒发一次在线消息，以免异常断线，
            sendOnlineHandler.sendEmptyMessage(SEND_ONLINE_STATUS);
        } else if (TextUtils.equals(talking.userId, getMyUserId())) {
            Log.i(TAG, "requestTalk: 已经是抢到麦的状态");
        } else {
            Log.i(TAG, "requestTalk: 占线中不能说话");
            requestFailed();
        }
    }

    private void requestFailed() {
        Log.w(TAG, "requestFailed() called");
        onTalkFree();
    }

    private void sendOnlineMessage() {
        sendMessage(XmppMessage.TYPE_TALK_ONLINE);
    }

    private void sendJoinTalkMessage() {
        sendMessage(XmppMessage.TYPE_TALK_JOIN);
    }

    private void sendLeftTalkMessage() {
        sendMessage(XmppMessage.TYPE_TALK_LEFT);
    }

    private void sendReleaseTalkMessage(double timeSend) {
        sendMessage(XmppMessage.TYPE_TALK_RELEASE, timeSend);
    }

    private void sendReleaseTalkMessage() {
        sendMessage(XmppMessage.TYPE_TALK_RELEASE);
    }

    private void sendRequestTalkMessage(double timeSend) {
        sendMessage(XmppMessage.TYPE_TALK_REQUEST, timeSend);
    }

    private void sendMessage(int type) {
        sendMessage(type, TimeUtils.sk_time_current_time_double());
    }

    private void sendMessage(int type, double timeSend) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(type);

        chatMessage.setFromUserId(coreManager.getSelf().getUserId());
        chatMessage.setFromUserName(coreManager.getSelf().getNickName());
        chatMessage.setToUserId(fromUserId);
        chatMessage.setObjectId(fromUserId);
        chatMessage.setDoubleTimeSend(timeSend);
        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        coreManager.sendMucChatMessage(fromUserId, chatMessage);
    }

    private void talkerFree() {
        Log.i(TAG, "talkerFree() called");
        if (talking == null) {
            Log.w(TAG, "talkerFree: talking == null");
            return;
        }
        userAdapter.offline(talking);
        talking = null;
        updateTalking();
    }

    private void talkerOffline() {
        Log.i(TAG, "talkerOffline() called");
        if (talking == null) {
            Log.w(TAG, "talkerOffline: talking == null");
            return;
        }

        talking.name = getString(R.string.tip_talker_ping_failed);
        updateTalking();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageTalkJoinEvent message) {
        if (!TextUtils.equals(message.chatMessage.getObjectId(), fromUserId)) {
            return;
        }
        userAdapter.add(TalkUserAdapter.Item.fromMessage(message.chatMessage));
        sendOnlineMessage();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageTalkOnlineEvent message) {
        if (!TextUtils.equals(message.chatMessage.getObjectId(), fromUserId)) {
            return;
        }
        ChatMessage chatMessage = message.chatMessage;
        String userId = chatMessage.getFromUserId();
        if (talking != null && TextUtils.equals(userId, talking.userId)) {
            // 主讲人发出的在线消息，
            if (talkerOnlineTimer != null) {
                talkerOnlineTimer.cancel();
            }
            talkerOnlineTimer = new TalkerOnlineTimer();
            talkerOnlineTimer.start();
        }
        userAdapter.add(TalkUserAdapter.Item.fromMessage(message.chatMessage));
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageTalkLeftEvent message) {
        if (!TextUtils.equals(message.chatMessage.getObjectId(), fromUserId)) {
            return;
        }
        userAdapter.remove(TalkUserAdapter.Item.fromMessage(message.chatMessage));
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageTalkRequestEvent message) {
        if (!TextUtils.equals(message.chatMessage.getObjectId(), fromUserId)) {
            return;
        }
        ChatMessage chatMessage = message.chatMessage;
        String name = chatMessage.getFromUserName();
        String userId = chatMessage.getFromUserId();
        double requestTime = chatMessage.getDoubleTimeSend();
        if (talking == null) {
            talking = new Talking(name, userId, requestTime);
            Log.d(TAG, "helloEventBus: 有人说话" + talking);
            updateTalking();
            talkerOnlineTimer = new TalkerOnlineTimer();
            talkerOnlineTimer.start();
        } else if (TextUtils.equals(talking.userId, getMyUserId())) {
            Talking remote = new Talking(name, userId, requestTime);
            Log.i(TAG, "helloEventBus: 抢线, local: " + talking + ", remote: " + remote);
            boolean conflict = requestTime > talking.requestTime;
            if (!conflict && requestTime == talking.requestTime) {
                // 同一毫秒的对策，改对比userId, userId小的优先，
                try {
                    int selfId = Integer.parseInt(coreManager.getSelf().getUserId());
                    int remoteId = Integer.parseInt(userId);
                    conflict = remoteId < selfId;
                } catch (Exception ignored) {
                }
            }
            if (conflict) {
                Log.i(TAG, "helloEventBus: 被挤掉线");
                talking = remote;
                updateTalking();
                requestFailed();
                // 被挤下线也发个下线消息，以防万一互相挤下线又都不释放，
                // 主要是时间是不精确的double类型，发出去和收到的可能不一样，
                sendReleaseTalkMessage();
            } else {
                Log.i(TAG, "helloEventBus: 对方掉线");
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageTalkReleaseEvent message) {
        if (!TextUtils.equals(message.chatMessage.getObjectId(), fromUserId)) {
            return;
        }
        ChatMessage chatMessage = message.chatMessage;
        String name = chatMessage.getFromUserName();
        String userId = chatMessage.getFromUserId();
        if (talking != null && TextUtils.equals(userId, talking.userId)) {
            Log.i(TAG, "helloEventBus: 有人下线, name: " + name);
            // adapter中使用同一个talking对象才能这么用，
            talking.talkLength = chatMessage.getDoubleTimeSend() - talking.requestTime;
            talking = null;
            updateTalking();
            if (talkerOnlineTimer != null) {
                talkerOnlineTimer.cancel();
                talkerOnlineTimer = null;
            } else {
                Log.w(TAG, "helloEventBus: talker release but talkerOnlineTimer == null");
            }
        } else {
            Log.i(TAG, "helloEventBus: 不占线的人下线，name: " + name);
        }
    }

    private AnimationDrawable getTalkingRippleDrawable() {
        if (talkingRippleDrawable != null) {
            return talkingRippleDrawable;
        }
        talkingRippleDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.talk_btn_frame_busy_ripple);
        return talkingRippleDrawable;
    }

    private void updateTalking() {
        if (talking != null) {
            AvatarHelper.getInstance().displayAvatar(talking.name, talking.userId, vhCurrentHead, false);
            vhCurrentHead.setVisibility(View.VISIBLE);
            llTalkFree.setVisibility(View.GONE);
            AnimationDrawable talkingRippleDrawable = getTalkingRippleDrawable();
            talkingRippleDrawable.start();
            ivTalkingRipple.setImageDrawable(talkingRippleDrawable);
            userAdapter.updateTalking(talking);
        } else {
            vhCurrentHead.setVisibility(View.GONE);
            llTalkFree.setVisibility(View.VISIBLE);
            AnimationDrawable talkingRippleDrawable = getTalkingRippleDrawable();
            talkingRippleDrawable.stop();
            ivTalkingRipple.setImageResource(R.mipmap.talk_btn_frame_large_free);
            // displayAvatar避免异步冲突，
            vhCurrentHead.setTag(R.id.key_avatar, null);
            userAdapter.updateTalking(null);
        }
    }

    private String getMyName() {
        return coreManager.getSelf().getNickName();
    }

    private String getMyUserId() {
        return coreManager.getSelf().getUserId();
    }

    private void initActionBar() {
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveJitsi();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.name_talk);
    }

    @Override
    public void onCoreReady() {
        super.onCoreReady();
        sendCallingMessage();// 对方可能一进入就已经挂掉了，我们就会误判对方未老版本，所以一进入就发送一条 "通话中" 消息给对方
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!super.onKeyUp(keyCode, event)
                && BuildConfig.DEBUG
                && keyCode == KeyEvent.KEYCODE_MENU
            /*TODO && ReactInstanceManagerHolder.showDevOptionsDialog()*/) {
            return true;
        }
        return false;
    }

    private void initData() {
        fromUserId = getIntent().getStringExtra("fromuserid");
        toUserId = getIntent().getStringExtra("touserid");

        JitsistateMachine.isInCalling = true;
        JitsistateMachine.callingOpposite = toUserId;

        mLocalHost = coreManager.getConfig().JitsiServer;

        if (TextUtils.isEmpty(mLocalHost)) {
            DialogHelper.tip(mContext, getString(R.string.tip_meet_server_empty));
            finish();
        }

        // mCallingCountDownTimer.start();
    }

    private void leaveJitsi() {
        Log.e(TAG, "leaveJitsi() called ");
        finish();
    }

    /**
     * startWithAudioMuted:是否禁用语音
     * startWithVideoMuted:是否禁用录像
     */
    private void initView() {
        mFrameLayout = (FrameLayout) findViewById(R.id.jitsi_view);
        mJitsiMeetView = new JitsiMeetView(this);
        mFrameLayout.addView(mJitsiMeetView);

        // 配置房间参数
        JitsiMeetConferenceOptions.Builder options = new JitsiMeetConferenceOptions.Builder()
                .setWelcomePageEnabled(false);
//TODO        mJitsiMeetView.setPictureInPictureEnabled(false);
        options.setAudioMuted(true);
        try {
            options.setServerURL(new URL(mLocalHost));
        } catch (MalformedURLException e) {
            throw new IllegalStateException("jitsi地址异常: " + mLocalHost);
        }
        // 群组对讲机添加标识，防止和群组视频进入同一房间地址
        options.setRoom("talk" + fromUserId);
        // 开始加载
        mJitsiMeetView.join(options.build());
    }

    private void initEvent() {
        mJitsiMeetView.setListener(new JitsiMeetViewListener() {

            @Override
            public void onConferenceWillJoin(Map<String, Object> map) {
                Log.e("jitsi", "即将加入会议");
            }

            @Override
            public void onConferenceJoined(Map<String, Object> map) {
                Log.e(TAG, "已加入会议，显示悬浮窗按钮，开始计时");
                // 如果将runOnUiThread放在onConferenceWillJoin内，底部会闪现一条白边，偶尔白边还不会消失
                // 会议开始，记录开始时间
                startTime = System.currentTimeMillis();
                // 开始计时
                mCountDownTimer.start();

                talkReady();
            }

            @Override
            public void onConferenceTerminated(Map<String, Object> map) {
                Log.e(TAG, "5");
                // 即将离开会议
                if (!isApi21HangUp) {
                    stopTime = System.currentTimeMillis();
                    overCall((int) (stopTime - startTime) / 1000);
                }

                Log.e(TAG, "6");
                JitsiTalk.this.sendBroadcast(new Intent(CallConstants.CLOSE_FLOATING));
                finish();
            }
        });
    }

    public void sendCallingMessage() {
        isEndCallOpposite = true;

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(XmppMessage.TYPE_IN_CALLING);

        chatMessage.setFromUserId(coreManager.getSelf().getUserId());
        chatMessage.setFromUserName(coreManager.getSelf().getNickName());
        chatMessage.setToUserId(toUserId);
        chatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        coreManager.sendChatMessage(toUserId, chatMessage);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageCallingEvent message) {
        if (message.chatMessage.getType() == XmppMessage.TYPE_IN_CALLING) {
            if (message.chatMessage.getFromUserId().equals(toUserId)) {
                isOldVersion = false;
                // 收到 "通话中" 的消息，且该消息为当前通话对象发送过来的
                Log.e(TAG, "MessageCallingEvent-->" + TimeUtils.sk_time_current_time());
                mPingReceiveFailCount = 0;// 将count置为0
                isEndCallOpposite = false;
            }
        }
    }

    // 对方挂断
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageHangUpPhone message) {
        if (message.chatMessage.getFromUserId().equals(fromUserId)
                || message.chatMessage.getFromUserId().equals(toUserId)) {// 挂断方为当前通话对象 否则不处理
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                isApi21HangUp = true;
                TipDialog tip = new TipDialog(JitsiTalk.this);
                tip.setmConfirmOnClickListener(getString(R.string.av_hand_hang), new TipDialog.ConfirmOnClickListener() {
                    @Override
                    public void confirm() {
                        hideBottomUIMenu();
                    }
                });
                tip.show();
                return;
            }

            // 关闭悬浮窗
            sendBroadcast(new Intent(CallConstants.CLOSE_FLOATING));
            leaveJitsi();
        }
    }

    /*******************************************
     * Method
     ******************************************/
    // 发送挂断的XMPP消息
    private void overCall(int time) {
        talkLeave();
    }

    private String formatTime() {
        Date date = new Date(new Date().getTime() - startTime);
        return mSimpleDateFormat.format(date);
    }

    // 隐藏虚拟按键
    private void hideBottomUIMenu() {
        View v = this.getWindow().getDecorView();
        v.setSystemUiVisibility(View.GONE);
    }

    /*******************************************
     * 录屏，保存至本地视频
     ******************************************/
    public void saveScreenRecordFile() {
        // 录屏文件路径
        String imNewestScreenRecord = PreferenceUtils.getString(getApplicationContext(), "IMScreenRecord");
        File file = new File(imNewestScreenRecord);
        if (file.exists() && file.getName().trim().toLowerCase().endsWith(".mp4")) {
            VideoFile videoFile = new VideoFile();
            videoFile.setCreateTime(TimeUtils.f_long_2_str(getScreenRecordFileCreateTime(file.getName())));
            videoFile.setFileLength(getScreenRecordFileTimeLen(file.getPath()));
            videoFile.setFileSize(file.length());
            videoFile.setFilePath(file.getPath());
            videoFile.setOwnerId(coreManager.getSelf().getUserId());
            VideoFileDao.getInstance().addVideoFile(videoFile);
        }
    }

    private long getScreenRecordFileCreateTime(String srf) {
        int dot = srf.lastIndexOf('.');
        return Long.parseLong(srf.substring(0, dot));
    }

    private long getScreenRecordFileTimeLen(String srf) {
        long duration;
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(srf);
            player.prepare();
            duration = player.getDuration() / 1000;
        } catch (Exception e) {
            duration = 10;
            e.printStackTrace();
        }
        player.release();
        return duration;
    }

    /*******************************************
     * 生命周期
     ******************************************/
    @Override
    public void onBackPressed() {
        // 不允许按返回键离开房间，也不能finish，
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        JitsiMeetActivityDelegate.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (JitsistateMachine.isFloating) {
            sendBroadcast(new Intent(CallConstants.CLOSE_FLOATING));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // 释放摄像头，
        JitsiMeetActivityDelegate.onHostPause(this);
        JitsistateMachine.reset();

        JitsiMeetActivityDelegate.onBackPressed();
        mJitsiMeetView.dispose();
        JitsiMeetActivityDelegate.onHostDestroy(this);

        EventBus.getDefault().unregister(this);

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        Log.e(TAG, "onDestory");
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode,
            final String[] permissions,
            final int[] grantResults) {
        JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void requestPermissions(String[] permissions, int requestCode, PermissionListener listener) {
        JitsiMeetActivityDelegate.requestPermissions(this, permissions, requestCode, listener);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        Log.d(TAG, "onPointerCaptureChanged() called with: hasCapture = [" + hasCapture + "]");
    }

    private static class SendOnlineHandler extends Handler {
        private final WeakReference<JitsiTalk> weakRef;

        SendOnlineHandler(JitsiTalk r) {
            weakRef = new WeakReference<>(r);
        }

        @Override
        public void handleMessage(Message msg) {
            JitsiTalk r = weakRef.get();
            if (r == null) {
                return;
            }
            if (msg.what == SEND_ONLINE_STATUS) {
                if (r.myTalking()) {
                    r.sendOnlineMessage();
                    r.sendOnlineHandler.sendEmptyMessageDelayed(SEND_ONLINE_STATUS, TimeUnit.SECONDS.toMillis(5));
                }
            }
        }
    }

    private class RequestTalkTimer extends CountDownTimer {

        private boolean statusTalking;

        public RequestTalkTimer(boolean statusTalking) {
            super(1000, 1000);
            this.statusTalking = statusTalking;
        }

        void setStatusReleaseTalk() {
            statusTalking = false;
        }

        void setStatusTalking() {
            statusTalking = true;
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            requestTalkTimer = null;
            if (statusTalking) {
                requestTalk();
            } else {
                releaseTalk();
            }
        }
    }

    private class TalkerOnlineTimer extends CountDownTimer {
        TalkerOnlineTimer() {
            super(TimeUnit.SECONDS.toMillis(15), TimeUnit.SECONDS.toMillis(1));
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // 10秒还没被取消说明talker断线了，
            if (millisUntilFinished > TimeUnit.SECONDS.toMillis(5)) {
                return;
            }
            talkerOffline();
        }

        @Override
        public void onFinish() {
            talkerOnlineTimer = null;
            talkerFree();
        }
    }
}
