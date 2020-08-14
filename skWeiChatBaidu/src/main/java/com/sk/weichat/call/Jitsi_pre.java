package com.sk.weichat.call;

import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.redchamber.util.GlideUtils;
import com.sk.weichat.R;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.ToastUtil;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * 单聊 拨号界面
 */
public class Jitsi_pre extends BaseActivity {
    Timer timer = new Timer();
    private String mLoginUserId;
    private int type;
    private String call_toUser;
    private String call_toName;
    private String meetUrl;
    private AssetFileDescriptor mAssetFileDescriptor;
    private MediaPlayer mediaPlayer;
    TimerTask timerTask = new TimerTask() {//  单聊 拨号界面 三十秒内 对方未接听  发送挂断消息 结束当前页面
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    abort();
                    if (type == CallConstants.Audio) {
                        EventBus.getDefault().post(new MessageEventCancelOrHangUp(XmppMessage.TYPE_NO_CONNECT_VOICE, call_toUser,
                                getString(R.string.sip_canceled) + getString(R.string.voice_call), 0));
                    } else if (type == CallConstants.Video) {
                        EventBus.getDefault().post(new MessageEventCancelOrHangUp(XmppMessage.TYPE_NO_CONNECT_VIDEO, call_toUser,
                                getString(R.string.sip_canceled) + getString(R.string.video_call), 0));
                    } else if (type == CallConstants.Screen) {
                        EventBus.getDefault().post(new MessageEventCancelOrHangUp(XmppMessage.TYPE_NO_CONNECT_SCREEN, call_toUser,
                                getString(R.string.sip_canceled) + getString(R.string.screen_call), 0));
                    }
                    JitsistateMachine.reset();
                    finish();
                }
            });
        }
    };
    private AnimationDrawable talkingRippleDrawable;
    private ImageView mCallAvatar;
    private TextView tv_timer;
    private TextView mCallName;
    private ImageButton mHangUp;
    private boolean isAllowBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_call_incall_false);
        initData();
        initView();
        timer.schedule(timerTask, 30000, 30000);// 开启计时器
        new CountDownTimer(31000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv_timer.setText("00:" + (31 - (int) millisUntilFinished / 1000 < 10 ? "0" + (31 - (int) millisUntilFinished / 1000) : (31 - (int) millisUntilFinished / 1000)));
            }

            @Override
            public void onFinish() {

            }
        }.start();
        EventBus.getDefault().register(this);
        setSwipeBackEnable(false);
    }

    private void initData() {
        mLoginUserId = coreManager.getSelf().getUserId();

        type = getIntent().getIntExtra("type", -1);
        call_toUser = getIntent().getStringExtra("touserid");
        call_toName = getIntent().getStringExtra("username");
        meetUrl = getIntent().getStringExtra("meetUrl");

        JitsistateMachine.isInCalling = true;
        JitsistateMachine.callingOpposite = call_toUser;

        bell();// 响铃
    }

    private void initView() {
        mCallAvatar = (ImageView) findViewById(R.id.call_avatar);
        tv_timer = findViewById(R.id.tv_timer);
        /*AnimationDrawable talkingRippleDrawable = getTalkingRippleDrawable();
        talkingRippleDrawable.start();*/
        ImageView ivTalkingRipple = findViewById(R.id.ivTalkingRipple);
        ivTalkingRipple.setImageDrawable(talkingRippleDrawable);
        mCallName = (TextView) findViewById(R.id.call_name);
        mHangUp = (ImageButton) findViewById(R.id.call_hang_up);
        TextView wait_tv = (TextView) findViewById(R.id.call_wait);
        TextView hang_up_tv = (TextView) findViewById(R.id.call_hang_up_tv);
        if (type == CallConstants.Audio) {
            wait_tv.setText(R.string.tip_wait_voice);
        } else if (type == CallConstants.Video) {
            wait_tv.setText(R.string.tip_wait_video);
        } else if (type == CallConstants.Screen) {
            wait_tv.setText(R.string.tip_wait_screen);
        }
        hang_up_tv.setText(R.string.string_endcall);
      //  AvatarHelper.getInstance().displayAvatar(call_toUser, mCallAvatar, true);
        GlideUtils.loadAvatar(Jitsi_pre.this, coreManager.getSelf().getUserId(), mCallAvatar);
        mCallName.setText(call_toName);
        mHangUp.setOnClickListener(new View.OnClickListener() {// 主动挂断
            @Override
            public void onClick(View view) {
                abort();
                if (type == CallConstants.Audio) {
                    EventBus.getDefault().post(new MessageEventCancelOrHangUp(XmppMessage.TYPE_NO_CONNECT_VOICE, call_toUser,
                            getString(R.string.sip_canceled) + getString(R.string.voice_call), 0));
                } else if (type == CallConstants.Video) {
                    EventBus.getDefault().post(new MessageEventCancelOrHangUp(XmppMessage.TYPE_NO_CONNECT_VIDEO, call_toUser,
                            getString(R.string.sip_canceled) + getString(R.string.video_call), 0));
                } else if (type == CallConstants.Screen) {
                    EventBus.getDefault().post(new MessageEventCancelOrHangUp(XmppMessage.TYPE_NO_CONNECT_SCREEN, call_toUser,
                            getString(R.string.sip_canceled) + getString(R.string.screen_call), 0));
                }
                JitsistateMachine.reset();
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageCallingEvent message) {
        if (message.chatMessage.getType() == XmppMessage.TYPE_IS_BUSY) {// 对方忙线
            if (message.chatMessage.getFromUserId().equals(call_toUser)) {
                Toast.makeText(this, R.string.tip_opposite_busy_call, Toast.LENGTH_SHORT).show();
                abort();
                JitsistateMachine.reset();
                finish();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventSipPreview message) {// 对方接听
        abort();
        int type = 0;
        if (message.message.getType() == XmppMessage.TYPE_CONNECT_VOICE) {
            type = CallConstants.Audio;
        } else if (message.message.getType() == XmppMessage.TYPE_CONNECT_VIDEO) {
            type = CallConstants.Video;
        } else if (message.message.getType() == XmppMessage.TYPE_CONNECT_SCREEN) {
            type = CallConstants.Screen;
        }
        if (TextUtils.equals(message.message.getContent(), "1")) {
            if (type == CallConstants.Audio) {
                ToastUtil.showToast(this, getString(R.string.tip_meet_type_change_to_video));
                type = CallConstants.Video;
            } else if (type == CallConstants.Video) {
                ToastUtil.showToast(this, getString(R.string.tip_meet_type_change_to_audio));
                type = CallConstants.Audio;
            }
        }
        Jitsi_connecting_second.start(this, mLoginUserId, call_toUser, type, meetUrl);
        finish();
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);// 淡入淡出动画
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageHangUpPhone message) {// 对方拒接
        if (message.chatMessage.getFromUserId().equals(call_toUser)) {
            abort();
            JitsistateMachine.reset();
            finish();
        }
    }

    private AnimationDrawable getTalkingRippleDrawable() {
        if (talkingRippleDrawable != null) {
            return talkingRippleDrawable;
        }
        talkingRippleDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.talk_btn_frame_busy_ripple);
        return talkingRippleDrawable;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().unregister(this);
    }
}
