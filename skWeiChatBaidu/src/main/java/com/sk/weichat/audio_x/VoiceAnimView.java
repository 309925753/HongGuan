package com.sk.weichat.audio_x;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;

import com.sk.weichat.R;
import com.sk.weichat.bean.circle.PublicMessage;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.downloader.DownloadListener;
import com.sk.weichat.downloader.Downloader;
import com.sk.weichat.downloader.FailReason;
import com.sk.weichat.util.DisplayUtil;
import com.sk.weichat.util.FileUtil;

import java.io.File;

import static com.sk.weichat.audio_x.VoiceManager.STATE_PLAY;

/**
 * Created by xuan on 2017/9/14.
 */

public class VoiceAnimView extends RelativeLayout {
    public static final int SHOW_PRO = 10; // 最少显示进度 刻度
    private static final String TAG = "VoiceAnimView";
    //    private static String playingUrl;
    AudioManager mAudioMgr;
    private ImageView ivAnim;
    private TextView tvTime;
    private XSeekBar mSeekBar;
    private Context mContext;
    private FrameLayout mFlSeek;
    private AnimationDrawable anim;
    private String path;
    private boolean showSeekBar;
    private String content;
    private boolean isDown;
    private boolean clickStart;
    private String voiceMsgId;// 正在播放的语音消息的msgId，当用户撤回语音消息时，如果撤回消息的msgId等于当前id，停止播放
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = null;
    private boolean direction;

    public VoiceAnimView(Context context) {
        this(context, null);
    }

    public VoiceAnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoiceAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_voice_anim, this);
    }

    /**
     * 设置方向
     *
     * @param direction true右边
     */
    private void setChatDirection(boolean direction, boolean isPublicMessage) {
        this.direction = direction;
        mSeekBar = (XSeekBar) findViewById(R.id.voice_seekbar);
        mFlSeek = (FrameLayout) findViewById(R.id.fl_seek);
        if (direction) {
            ivAnim = (ImageView) findViewById(R.id.voice_anim_iv_right);
            tvTime = (TextView) findViewById(R.id.voice_time_tv_right);
        } else if (isPublicMessage) {
            ivAnim = (ImageView) findViewById(R.id.voice_anim_iv_left);
            ImageViewCompat.setImageTintList(ivAnim, ColorStateList.valueOf(getResources().getColor(R.color.dan_gu_lv)));
            tvTime = (TextView) findViewById(R.id.voice_time_tv_left);
        } else {
            ivAnim = (ImageView) findViewById(R.id.voice_anim_iv_left);
            tvTime = (TextView) findViewById(R.id.voice_time_tv_left);
        }
        ivAnim.setVisibility(VISIBLE);
        tvTime.setVisibility(VISIBLE);
    }

    private void bindData(boolean direction, String id,
                          @Nullable String filePath, String url,
                          int length, boolean isPublic) {
        setChatDirection(direction, isPublic);

        voiceMsgId = id;

        path = filePath;
        content = url;

        mFlSeek.setVisibility(VISIBLE);
        tvTime.setText(length + "''");

        if (length >= SHOW_PRO) { // 10秒显示进度条出来
            showSeekBar = true;
        } else {
            showSeekBar = false;
        }

        mSeekBar.setMax(length);
        mSeekBar.setVisibility(GONE);

        LayoutParams params = (LayoutParams) mFlSeek.getLayoutParams();
        params.width = DisplayUtil.getVoiceViewWidth(mContext, length);
        mFlSeek.setLayoutParams(params);

        initListener();
        initAnim();

        isDown = false;
        if (TextUtils.isEmpty(path)) {
            path = FileUtil.getRandomAudioAmrFilePath();
        }
        stopAnim();
        File file = new File(path);
        if (!file.exists()) {
            Downloader.getInstance().addDownload(content, new DownloadListener() {

                @Override
                public void onStarted(String uri, View view) {

                }

                @Override
                public void onFailed(String uri, FailReason failReason, View view) {

                }

                @Override
                public void onComplete(String uri, String filePath, View view) {
                    path = filePath;
                    isDown = true;
                    if (clickStart) {
                        start();
                    } else if (VoiceManager.instance().getState() == STATE_PLAY && TextUtils.equals(path, VoiceManager.instance().mCurrtPath)) {
                        startAnim();
                        VoicePlayer.instance().changeVoice(VoiceAnimView.this);
                    }
                }

                @Override
                public void onCancelled(String uri, View view) {

                }
            });
        } else {
            isDown = true;
            if (VoiceManager.instance().getState() == STATE_PLAY && TextUtils.equals(path, VoiceManager.instance().mCurrtPath)) {
                startAnim();
                VoicePlayer.instance().changeVoice(VoiceAnimView.this);
            }
        }
    }

    public void fillData(PublicMessage message) {
        bindData(
                false,
                message.getEmojiId(),
                null,
                message.getFirstAudio(),
                (int) message.getBody().getAudios().get(0).getLength(),
                true
        );
    }

    public void fillData(ChatMessage chatMessage) {
        bindData(
                chatMessage.isMySend(),
                chatMessage.getPacketId(),
                chatMessage.getFilePath(),
                chatMessage.getContent(),
                chatMessage.getTimeLen(),
                false
        );
    }

    public String getVoiceMsgId() {
        return voiceMsgId;
    }

    private void initAnim() {
        anim = (AnimationDrawable) ivAnim.getBackground();
        mSeekBar.stop();
    }

    private void initListener() {
        mSeekBar.addOnProgressChangeListener(new XSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(int progress) {
                int mesc = progress;
                VoicePlayer.instance().playSeek(mesc, VoiceAnimView.this);
            }
        });
    }

    // 请求焦点
    private void requestAudioFocus() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1) {
            return;
        }
        if (mAudioMgr == null)
            mAudioMgr = (AudioManager) mContext
                    .getSystemService(Context.AUDIO_SERVICE);
        if (mAudioMgr != null) {
            int ret = mAudioMgr.requestAudioFocus(mAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            if (ret != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            }
        }
    }

    // 放弃焦点
    private void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1) {
            return;
        }
        if (mAudioMgr != null) {
            mAudioMgr.abandonAudioFocus(mAudioFocusChangeListener);
            mAudioMgr = null;
        }
    }

    public void start() {
        if (isDown) {
            File file = new File(path);
            if (file.exists()) {
                VoiceManager.instance().play(file);
                VoiceManager.instance().setVoiceAnimView(this);
                startAnim();
            }
        } else {
            clickStart = true;
        }
    }

    public void stop() {
        if (VoiceManager.instance().getState() == STATE_PLAY) {
            VoiceManager.instance().stop();
        }
        stopAnim();
    }

    public void startAnim() {
        requestAudioFocus();
        anim.start();
        if (showSeekBar) {
            mSeekBar.setProgress(VoiceManager.instance().getProgress());
            mSeekBar.start(); // 进度滚动开始
            mSeekBar.setVisibility(VISIBLE);
        }
    }

    public void stopAnim() {
        abandonAudioFocus();
        mSeekBar.setProgress(0);
        resetAnim();
        if (showSeekBar) {
            mSeekBar.stop(); // 进度滚动结束
            mSeekBar.setVisibility(GONE);
        }
    }

    private void resetAnim() {
        anim.stop();
        anim.selectDrawable(0);
        // 光靠stop有可能无法确实停止，干脆新实例化一个动画，
        ivAnim.setBackground(null);
        if (direction) {
            ivAnim.setBackgroundResource(R.drawable.voice_play_right);
        } else {
            ivAnim.setBackgroundResource(R.drawable.voice_play_left);
        }
        anim = (AnimationDrawable) ivAnim.getBackground();
    }

/*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                VoicePlayer.instance().playVoice(this);
                break;
        }
        return super.onTouchEvent(event);
    }
*/

/*
    OnClickListener mListener;

    @Override
    public void setOnClickListener(OnClickListener l) {
        mListener = l;
    }
*/
}
