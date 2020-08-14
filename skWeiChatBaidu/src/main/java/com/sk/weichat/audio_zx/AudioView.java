package com.sk.weichat.audio_zx;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import androidx.annotation.Nullable;

import com.sk.weichat.R;
import com.sk.weichat.bean.circle.PublicMessage;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.downloader.DownloadListener;
import com.sk.weichat.downloader.Downloader;
import com.sk.weichat.downloader.FailReason;
import com.sk.weichat.util.FileUtil;
import com.sk.weichat.util.ScreenUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static com.sk.weichat.audio_x.VoiceManager.STATE_PLAY;

/**
 * @author zhaolewei on 2018/8/17.
 */
public class AudioView extends View {

    /**
     * 频谱数量
     */
    private static final int LUMP_COUNT = 80;
    private static final int LUMP_WIDTH = 5;
    private static final int LUMP_SPACE = 10;
    private static final int LUMP_MIN_HEIGHT = 2;
    private static final float LUMP_MAX_HEIGHT = 90;//TODO: HEIGHT
    private static final int LUMP_SIZE = LUMP_WIDTH + LUMP_SPACE;
    private static final int LUMP_COLOR = Color.parseColor("#000000");
    private static final float SCALE = LUMP_MAX_HEIGHT / LUMP_COUNT;
    Path wavePath = new Path();
    //6de8fd 9E9E9E
    private String path;
    private boolean isDown;
    private boolean clickStart;
    private String voiceMsgId;// 正在播放的语音消息的msgId，当用户撤回语音消息时，如果撤回消息的msgId等于当前id，停止播放
    private ShowStyle upShowStyle = ShowStyle.STYLE_HOLLOW_LUMP;
    private Paint lumpPaint;
    private TranslateAnimation translateAnimation;

    private View viewO;
    private ArrayList<String> stringAudio = new ArrayList<>();
    private int durationAni;
    private Context mContext;

    private int right;

    public AudioView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public AudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public AudioView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    private void init() {
        lumpPaint = new Paint();
        lumpPaint.setAntiAlias(true);
        lumpPaint.setColor(LUMP_COLOR);

        lumpPaint.setStrokeWidth(LUMP_WIDTH);
        lumpPaint.setStyle(Paint.Style.FILL);

        if (stringAudio.size() == 0) {
            for (int k = 0; k < 30; k++) {
                stringAudio.add("" + k);
            }
        }
    }

    public void setStyle(ShowStyle upShowStyle) {
        this.upShowStyle = upShowStyle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        wavePath.reset();
        for (int i = 0; i < stringAudio.size(); i++) {
            switch (upShowStyle) {
                case STYLE_HOLLOW_LUMP:
                    drawLump(canvas, i);
                    break;
            }
        }
    }

    /**
     * 绘制矩形条
     */
    private void drawLump(Canvas canvas, int i) {
        //(LUMP_MAX_HEIGHT - (LUMP_MIN_HEIGHT + waveData[i] * SCALE) * minus);
//        float top = LUMP_MAX_HEIGHT - Float.valueOf(stringAudio.get(i));
//        int sum = 0;
//        if (durationAni > 1000 && durationAni < 5000) {
//            for (int j = 0; j < stringAudio.size(); j++) {
//                sum+= Integer.valueOf(stringAudio.get(j));
//            }
//           int c= sum / (durationAni / 1000);
//            for (int j = 0; j < 10; j++) {
//                stringAudio.add(c + 1 + "");
//            }
//        }

        float top = LUMP_MAX_HEIGHT - (LUMP_MIN_HEIGHT + Float.valueOf(stringAudio.get(i)) * (40 / stringAudio.size()));
        canvas.drawRect(LUMP_SIZE * i,
                (float) (top * 0.7),
                LUMP_SIZE * i + LUMP_WIDTH,
                LUMP_MAX_HEIGHT,
                lumpPaint);
    }

    public void fillData(PublicMessage message) {
        bindData(
                message.getEmojiId(),
                null,
                message.getFirstAudio(),
                (int) message.getBody().getAudios().get(0).getLength(), null
        );
    }

    public void fillData(ChatMessage chatMessage) {
        bindData(
                chatMessage.getPacketId(),
                chatMessage.getFilePath(),
                chatMessage.getContent(),
                chatMessage.getTimeLen(),
                chatMessage.getObjectId()
        );
    }

    private void bindData(String packetId, String filePath, String content, int timeLen, String objectId) {
        voiceMsgId = packetId;
        path = filePath;
        if (TextUtils.isEmpty(path)) {
            path = FileUtil.getRandomAudioAmrFilePath();
        }
        File file = new File(path);

        durationAni = timeLen * 1000;
        this.setStyle(AudioView.ShowStyle.STYLE_HOLLOW_LUMP);
        if (objectId == null) return;
        String[] stringsx = objectId.split(",");
        ArrayList strings = new ArrayList(Arrays.asList(stringsx));
        stringAudio = strings;
        Log.e("zx", "bindData: " + stringAudio);
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
                        startAnim(durationAni, null, false);
                    } else if (VoiceManager.instance().getState() == STATE_PLAY && TextUtils.equals(path, VoiceManager.instance().mCurrtPath)) {
                        startAnim(durationAni, null, false);
                        VoicePlayer.instance().changeVoice(AudioView.this);
                    }
                }

                @Override
                public void onCancelled(String uri, View view) {

                }
            });
        } else {
            isDown = true;
            if (VoiceManager.instance().getState() == STATE_PLAY && TextUtils.equals(path, VoiceManager.instance().mCurrtPath)) {
                startAnim(durationAni, null, false);
                VoicePlayer.instance().changeVoice(AudioView.this);
            }
        }
    }

    public void moveView(int offsetX, View viewH) {
        int l = viewH.getLeft() + offsetX;
        int r = viewH.getRight() + offsetX;
        if (r <= AudioView.this.getLeft()) {
            l = AudioView.this.getLeft();
            r = AudioView.this.getLeft() + 2;
        }

        if (r >= this.getRight()) {
            l = AudioView.this.getRight();
            r = AudioView.this.getRight() + 2;
        }
        // 方法一
        viewH.layout(l, viewH.getTop(), r, viewH.getBottom());
    }


    public void setAni(long duration, View viewH, boolean isMySend) {
        viewH.setVisibility(VISIBLE);
        Log.e("zx", "setAni: ");
        viewH.clearAnimation();
        translateAnimation = new TranslateAnimation(0, AudioView.this.getRight(), 0, 0);
        translateAnimation.setFillEnabled(true);//使其可以填充效果从而不回到原地
        translateAnimation.setFillAfter(false);//不回到起始位置
        //如果不添加setFillEnabled和setFillAfter则动画执行结束后会自动回到远点
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
//                if (viewH.getLeft() >= AudioView.this.getRight() - 2 && viewH.getRight() <= AudioView.this.getRight()) {
//                    viewH.setVisibility(GONE);
//                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                if (isMySend) {
//                    viewH.layout(22, viewH.getTop(), 24, viewH.getBottom());
//                } else {
//                    viewH.layout(28, viewH.getTop(), 30, viewH.getBottom());
//                }
//                viewH.layout(AudioView.this.getLeft(), viewH.getTop(), 30, viewH.getBottom());

                if (viewH.getVisibility() == VISIBLE) {
                    viewH.setVisibility(GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        translateAnimation.setDuration(duration);//动画持续的时间
        viewH.setAnimation(translateAnimation);//给imageView添加的动画效果
        translateAnimation.startNow();
    }

    public void startAnim(long durationAni, View view, boolean isMySend) {
        Log.e("zx", "startAnim: " + durationAni);
        viewO = view;
        View view1 = new View(mContext);
        view1.setBackgroundColor(getResources().getColor(R.color.audio_view));
        ScreenUtil.setLayoutWidth(view1, ScreenUtil.dip2px(mContext, 1));
        if (view == null) view = view1;
        setAni(durationAni, view, isMySend);
    }

    public void start() {
        if (isDown) {
            File file = new File(path);
            if (file.exists()) {
                VoiceManager.instance().play(file);
//                startAnim(viewO);
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

    public void stopAnim() {
        if (viewO != null && viewO.getAnimation() != null) {
            viewO.getAnimation().cancel();
            viewO.clearAnimation();
            viewO.setVisibility(GONE);
        } else return;
        onAnimationEnd();
    }

    public String getVoiceMsgId() {
        return voiceMsgId;
    }

    /**
     * 可视化样式
     */
    public enum ShowStyle {
        /**
         * 空心的矩形小块
         */
        STYLE_HOLLOW_LUMP,

        /**
         * 不显示
         */
        STYLE_NOTHING
    }
}

