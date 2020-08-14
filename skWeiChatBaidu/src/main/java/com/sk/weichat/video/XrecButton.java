package com.sk.weichat.video;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class XrecButton extends View {

    public float corner;
    private Paint mCenPaint;
    private Paint mProPaint;
    private int radius;
    private int mWidth;
    private float mMaxRectWidth;
    private float mMinRectWidth;
    private float mMinStrokeWidth;
    private float mMaxStrokeWidth;
    private float mMinCorner;
    private float mMaxCorner;
    private float strokeWidth;
    private float rectWidth;
    private AnimatorSet startAnimatorSet = new AnimatorSet();
    private AnimatorSet endAnimatorSet = new AnimatorSet();

    private boolean isRecord;
    private boolean isInit;

    public XrecButton(Context context) {
        this(context, null);
    }

    public XrecButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XrecButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mProPaint = new Paint();
        mProPaint.setColor(Color.parseColor("#E9445A"));
        mProPaint.setStyle(Paint.Style.STROKE);
        mProPaint.setAntiAlias(true);
        mProPaint.setAlpha(153);

        mCenPaint = new Paint();
        mCenPaint.setColor(Color.parseColor("#E9445A"));
        mCenPaint.setStyle(Paint.Style.FILL);
        mCenPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        radius = mWidth >> 1;

        mMaxRectWidth = mWidth * 0.65f;
        mMinRectWidth = mMaxRectWidth * 0.6f;

        mMinStrokeWidth = (mWidth - mMaxRectWidth) * 0.5f - 15f;
        mMaxStrokeWidth = mMinStrokeWidth + 16f;

        mMinCorner = mMaxRectWidth * 0.15f;
        mMaxCorner = mMaxRectWidth * 0.5f;

        if (!isInit) {
            isInit = true;
            strokeWidth = mMinStrokeWidth;
            rectWidth = mMaxRectWidth;
            corner = mMaxCorner;
        }

        setMeasuredDimension(mWidth, mWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mProPaint.setStrokeWidth(strokeWidth);
        float top = (mWidth - rectWidth) * 0.5f;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // 画圆环
            canvas.drawCircle(radius, radius, radius - mProPaint.getStrokeWidth() * 0.5f, mProPaint);
            // 画中心
            if (isRecord) {
                top = (mWidth - mMinRectWidth) * 0.5f;
                canvas.drawRect(top, top, top + mMinRectWidth, top + mMinRectWidth, mCenPaint);
            } else {
                canvas.drawCircle(radius, radius, mMaxCorner, mCenPaint);
            }

        } else {

            if (isRecord) {
                mProPaint.setAlpha(128);
            } else {
                mProPaint.setAlpha(200);
            }

            // 画圆环
            canvas.drawCircle(radius, radius, radius - mProPaint.getStrokeWidth() * 0.5f, mProPaint);
            // 画中心
            canvas.drawRoundRect(top, top, top + rectWidth, top + rectWidth, corner, corner, mCenPaint);
        }
    }

    public void record() {
        isRecord = true;
        ObjectAnimator cornerAnimator = ObjectAnimator.ofFloat(this, "corner",
                mMaxCorner, mMinCorner).setDuration(350);
        ObjectAnimator rectSizeAnimator = ObjectAnimator.ofFloat(this, "rectWidth",
                mMaxRectWidth, mMinRectWidth).setDuration(350);

        ObjectAnimator strokeWidthAnimator = ObjectAnimator.ofFloat(this, "strokeWidth",
                mMinStrokeWidth, mMaxStrokeWidth).setDuration(600);

        strokeWidthAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        strokeWidthAnimator.setRepeatCount(ObjectAnimator.INFINITE);

        startAnimatorSet.playTogether(cornerAnimator, rectSizeAnimator, strokeWidthAnimator);
        startAnimatorSet.start();
    }

    public void pause() {
        isRecord = false;
        if (startAnimatorSet != null && startAnimatorSet.isRunning()) {
            startAnimatorSet.cancel();
        }

        ObjectAnimator cornerAnimator = ObjectAnimator.ofFloat(this, "corner",
                corner, mMaxCorner).setDuration(350);
        ObjectAnimator rectSizeAnimator = ObjectAnimator.ofFloat(this, "rectWidth",
                rectWidth, mMaxRectWidth).setDuration(350);
        ObjectAnimator strokeWidthAnimator = ObjectAnimator.ofFloat(this, "strokeWidth",
                strokeWidth, mMinStrokeWidth).setDuration(350);

        endAnimatorSet.playTogether(strokeWidthAnimator, cornerAnimator, rectSizeAnimator);
        endAnimatorSet.start();
    }

    public void setCorner(float corner) {
        this.corner = corner;
        invalidate();
    }

    public void setRectWidth(float rectWidth) {
        this.rectWidth = rectWidth;
        invalidate();
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        invalidate();
    }

}

