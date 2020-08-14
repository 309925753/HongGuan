package com.sk.weichat.video;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;


/**
 * 调节音量的SeekBar
 * create xuan
 * time 2018-11-21 09:32:21
 */

public class XSeekBar extends View {
    private float curr = 50f; // 当前选择
    private float max = 100f; // 总进度

    private int mWidth;
    private int mRectWidth;
    private int centerY;
    private int radius;

    private OnChangeListener mListener;
    private Paint mProPaint;
    private Paint mBgPaint;

    public XSeekBar(Context context) {
        this(context, null);
    }

    public XSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        mProPaint = new Paint();
        mProPaint.setColor(Color.parseColor("#E9445A"));
        mProPaint.setStyle(Paint.Style.FILL);
        mProPaint.setAntiAlias(true);
        mProPaint.setStrokeWidth(6f);

        mBgPaint = new Paint();
        mBgPaint.setColor(Color.parseColor("#FFFFFF"));
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setAntiAlias(true);
        mBgPaint.setStrokeWidth(6f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                curr = Math.round(event.getX() / mRectWidth * max);
                curr = Math.min(Math.max(curr, 0f), 100);
                invalidate();
                if (mListener != null) {
                    mListener.change(XSeekBar.this, (int) curr);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mListener != null) {
                    mListener.change(XSeekBar.this, (int) curr);
                }
                break;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRectWidth = MeasureSpec.getSize(widthMeasureSpec);
        centerY = MeasureSpec.getSize(heightMeasureSpec) >> 1;
        radius = (int) dp2px(8);
        mWidth = mRectWidth - (radius * 2);

    }

    public float dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    public void changeSelect(int select) {
        curr = select;
        invalidate();
    }

    public void addOnChangeListener(OnChangeListener listener) {
        this.mListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            onDrawLows(canvas);
            return;
        }

        float movex = curr / max * mWidth + radius;
        canvas.drawLine(radius, centerY, radius + mWidth, centerY, mBgPaint);
        canvas.drawLine(radius, centerY, movex, centerY, mProPaint);
        canvas.drawCircle(movex, centerY, radius, mBgPaint);
    }

    private void onDrawLows(Canvas canvas) {
        canvas.drawLine(radius, centerY, radius + mWidth, centerY, mBgPaint);
        float movex = curr / max * mWidth + radius;
        canvas.drawLine(radius, centerY, movex, centerY, mProPaint);
        canvas.drawCircle(movex, centerY, radius, mBgPaint);
    }

    public interface OnChangeListener {
        void change(XSeekBar xSeekBar, int curr);
    }
}

