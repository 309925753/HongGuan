package com.sk.weichat.ui.xrce;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 仿抖音录制按钮
 *
 * @author xuan
 * @date 2018-11-22 15:46:12
 */
public class RecordButton extends View {

    private Paint mRectPaint;
    private Paint mCirclePaint;

    private float corner;
    private float circleRadius;
    private float circleStrokeWidth;
    private float rectWidth;

    private float mMinCircleRadius;
    private float mMaxCircleRadius;
    private float mMinRectWidth;
    private float mMaxRectWidth;
    private float mMinCorner;
    private float mMaxCorner;
    private float mMinCircleStrokeWidth;
    private float mMaxCircleStrokeWidth;

    private boolean isRecord;

    private RectF mRectF = new RectF();
    private AnimatorSet mBeginAnimatorSet = new AnimatorSet();
    private AnimatorSet mEndAnimatorSet = new AnimatorSet();

    private Xfermode mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);


    public RecordButton(Context context) {
        this(context, null);
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectPaint.setStyle(Paint.Style.FILL);
        mRectPaint.setColor(Color.parseColor("#E9445A"));

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(Color.parseColor("#E9445A"));

        mMinCircleStrokeWidth = dip2px(8);
        mMaxCircleStrokeWidth = dip2px(16);
        circleStrokeWidth = mMinCircleStrokeWidth;
        mCirclePaint.setStrokeWidth(circleStrokeWidth);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int centerX = width / 2;
        int centerY = height / 2;

        mMaxRectWidth = width / 3;
        mMinRectWidth = mMaxRectWidth * 0.7f;

        mMinCircleRadius = mMaxRectWidth / 2 + mMinCircleStrokeWidth + dip2px(5);
        mMaxCircleRadius = width / 2 - mMaxCircleStrokeWidth;

        mMinCorner = dip2px(3);
        mMaxCorner = mMaxRectWidth / 2;

        if (rectWidth == 0) {
            rectWidth = mMaxRectWidth;
        }
        if (circleRadius == 0) {
            circleRadius = mMinCircleRadius;
        }
        if (corner == 0) {
            corner = rectWidth / 2;
        }

        mCirclePaint.setColor(Color.parseColor("#80E9445A"));
        canvas.drawCircle(centerX, centerY, circleRadius, mCirclePaint);
        mCirclePaint.setXfermode(mXfermode);

        mCirclePaint.setColor(Color.parseColor("#000000"));
        canvas.drawCircle(centerX, centerY, circleRadius - circleStrokeWidth, mCirclePaint);
        mCirclePaint.setXfermode(null);

        mRectF.left = centerX - rectWidth / 2;
        mRectF.right = centerX + rectWidth / 2;
        mRectF.top = centerY - rectWidth / 2;
        mRectF.bottom = centerY + rectWidth / 2;
        canvas.drawRoundRect(mRectF, corner, corner, mRectPaint);
    }


    public void record() {

        if (mEndAnimatorSet != null && mEndAnimatorSet.isRunning()) {
            mEndAnimatorSet.cancel();
        }

        long duration = 300;

        AnimatorSet startAnimatorSet = new AnimatorSet();
        ObjectAnimator cornerAnimator = ObjectAnimator.ofFloat(this, "corner",
                mMaxCorner, mMinCorner)
                .setDuration(duration);
        ObjectAnimator rectSizeAnimator = ObjectAnimator.ofFloat(this, "rectWidth",
                mMaxRectWidth, mMinRectWidth)
                .setDuration(duration);
        ObjectAnimator radiusAnimator = ObjectAnimator.ofFloat(this, "circleRadius",
                mMinCircleRadius, mMaxCircleRadius)
                .setDuration(duration);
        startAnimatorSet.playTogether(cornerAnimator, rectSizeAnimator, radiusAnimator);

        ObjectAnimator circleWidthAnimator = ObjectAnimator.ofFloat(this, "circleStrokeWidth",
                mMinCircleStrokeWidth, mMaxCircleStrokeWidth, mMinCircleStrokeWidth)
                .setDuration(1200);
        circleWidthAnimator.setRepeatCount(ObjectAnimator.INFINITE);

        mBeginAnimatorSet.playSequentially(startAnimatorSet, circleWidthAnimator);
        mBeginAnimatorSet.start();
    }

    public void pause() {


        if (mBeginAnimatorSet != null && mBeginAnimatorSet.isRunning()) {
            mBeginAnimatorSet.cancel();
        }

        long duration = 300;
        ObjectAnimator cornerAnimator = ObjectAnimator.ofFloat(this, "corner",
                corner, mMaxCorner)
                .setDuration(duration);
        ObjectAnimator rectSizeAnimator = ObjectAnimator.ofFloat(this, "rectWidth",
                rectWidth, mMaxRectWidth)
                .setDuration(duration);
        ObjectAnimator radiusAnimator = ObjectAnimator.ofFloat(this, "circleRadius",
                circleRadius, mMinCircleRadius)
                .setDuration(duration);
        ObjectAnimator circleWidthAnimator = ObjectAnimator.ofFloat(this, "circleStrokeWidth",
                circleStrokeWidth, mMinCircleStrokeWidth)
                .setDuration(duration);

        mEndAnimatorSet.playTogether(cornerAnimator, rectSizeAnimator, radiusAnimator, circleWidthAnimator);
        mEndAnimatorSet.start();
    }

    public void setCorner(float corner) {
        this.corner = corner;
        invalidate();
    }

    public void setCircleRadius(float circleRadius) {
        this.circleRadius = circleRadius;
    }

    public void setCircleStrokeWidth(float circleStrokeWidth) {
        this.circleStrokeWidth = circleStrokeWidth;
        invalidate();
    }

    public void setRectWidth(float rectWidth) {
        this.rectWidth = rectWidth;
    }


    public float dip2px(int dp) {
        float desity = getResources().getDisplayMetrics().density;
        return desity * dp + 0.5f;
    }

}