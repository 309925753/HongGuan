package com.sk.weichat.ui.xrce;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 视频播放进度条
 * create by xuan 2018-11-26 18:10:42
 */
public class Xpreprogressbar extends View {

    private static final int ONECE_TIME = 20;

    private int mWidth;
    private int mHeight;

    private Paint mBgPaint;
    private Paint mProPaint;

    private float max;
    private float currt;


    private Timer UPDATE_TIMER;
    private ProgressTimerTask mProgressTask;

    public Xpreprogressbar(Context context) {
        this(context, null);
    }

    public Xpreprogressbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Xpreprogressbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mProPaint = new Paint();
        mProPaint.setColor(Color.parseColor("#FF4081"));
        mProPaint.setStyle(Paint.Style.FILL);
        mProPaint.setAntiAlias(true);

        mBgPaint = new Paint();
        mBgPaint.setColor(Color.parseColor("#FFFFFF"));
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setAntiAlias(true);

        currt = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, mWidth, mHeight, mBgPaint);
        int pro = (int) (currt / max * mWidth);
        canvas.drawRect(0, 0, pro, mHeight, mProPaint);
    }

    public void clear() {
        this.currt = 0;
        invalidate();
    }

    public void play(long curr, long max) {
        this.currt = curr;
        this.max = max;

        cancelProgressTimer();
        UPDATE_TIMER = new Timer();
        mProgressTask = new ProgressTimerTask();
        UPDATE_TIMER.schedule(mProgressTask, 0, ONECE_TIME);
    }

    public void play(long max) {
        this.max = max;
        cancelProgressTimer();
        UPDATE_TIMER = new Timer();
        mProgressTask = new ProgressTimerTask();
        UPDATE_TIMER.schedule(mProgressTask, 0, ONECE_TIME);
    }

    public void cancelProgressTimer() {
        if (UPDATE_TIMER != null) {
            UPDATE_TIMER.cancel();
        }

        if (mProgressTask != null) {
            mProgressTask.cancel();
        }
    }

    public class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            Xpreprogressbar.this.currt += Xpreprogressbar.ONECE_TIME;
            Xpreprogressbar.this.postInvalidate();
        }
    }
}
