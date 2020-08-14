package com.sk.weichat.audio_x;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.sk.weichat.R;
import com.sk.weichat.util.DisplayUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/14.
 */

public class XSeekBar extends View {
    private static Map<String, ListenerTaskRunnable> mSeekBarThreadMap = new HashMap<>();// 用来处理listView刷新，会创建多个线程来同时更新SeekBar进度的问题
    private int mWidth;
    private int mHeight;
    private Paint mBgPaint;
    private Paint mBroPaint;
    private int mProgressHeight;
    private int max = 100;
    private volatile float right;
    private OnProgressChangeListener onProgressChangeListener;
    private boolean isRun;

    public XSeekBar(Context context) {
        this(context, null);
    }

    public XSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    private void init(Context context) {
        mProgressHeight = DisplayUtil.dip2px(context, 2f);
        mBgPaint = new Paint();
        mBgPaint.setColor(getResources().getColor(R.color.Grey_400));     // 设置背景进度条的颜色
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setAntiAlias(true);

        mBroPaint = new Paint();
        mBroPaint.setColor(getResources().getColor(R.color.color_role3)); // 设置背景进度条的颜色
        mBroPaint.setStyle(Paint.Style.FILL);
        mBroPaint.setAntiAlias(true);

        right = 0.0f;
    }

    public void addOnProgressChangeListener(OnProgressChangeListener listener) {
        onProgressChangeListener = listener;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setProgress(int progress) {
        if (progress > max) {
            progress = max;
        } else if (progress < 0) {
            progress = 0;
        }

        // right = (float) (progress / (float) max * 100.0) * mWidth;  //  max * 100 ??? 莫名其妙
        right = (((float) progress / (float) max) * mWidth);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 画背景
        RectF rectF = new RectF();
        rectF.top = (mHeight / 2) - (mProgressHeight / 2);
        rectF.left = 0;
        rectF.bottom = rectF.top + mProgressHeight;
        rectF.right = mWidth;
        canvas.drawRect(rectF, mBgPaint);
        // 画进度
        RectF pro = new RectF();
        pro.top = (mHeight / 2) - (mProgressHeight / 2);
        pro.left = 0;
        pro.bottom = rectF.top + mProgressHeight;
        pro.right = right;
        canvas.drawRect(pro, mBroPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                right = event.getX();
                int pro = (int) (right / mWidth * max);
                invalidate();
                if (onProgressChangeListener != null) {
                    onProgressChangeListener.onProgressChanged(pro);
                }
                break;
        }

        return true;
    }

    public void start() {
        if (max >= VoiceAnimView.SHOW_PRO) {
            ListenerTaskRunnable oldThread = mSeekBarThreadMap.remove(VoicePlayer.instance().getVoiceMsgId());
            if (oldThread != null) {
                oldThread.threadStop = true;
            }
            if (!isRun) {
                isRun = true;
                if (!mSeekBarThreadMap.containsKey(VoicePlayer.instance().getVoiceMsgId())) {
                    ListenerTaskRunnable listenerTask = new ListenerTaskRunnable();
                    new Thread(listenerTask).start();
                    mSeekBarThreadMap.put(VoicePlayer.instance().getVoiceMsgId(), listenerTask);
                }
            }
        }
    }

    public void stop() {
        if (max >= VoiceAnimView.SHOW_PRO) {
            isRun = false;
        }
    }

    public interface OnProgressChangeListener {
        void onProgressChanged(int progress);
    }

    private class ListenerTaskRunnable implements Runnable {
        private boolean threadStop;

        @Override
        public void run() {
            while (isRun && !threadStop) {
                if (Float.isNaN(right)) {
                    right = 0;
                }
                float kk = mWidth - right; // 剩余的刻度
                float pro = kk / mWidth * max; // 剩余的进度
                pro = kk / (pro * 10f);
                right += pro;
                postInvalidate();
                SystemClock.sleep(100);
            }
        }
    }

}
