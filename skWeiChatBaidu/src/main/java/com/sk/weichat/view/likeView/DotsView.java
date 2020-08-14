package com.sk.weichat.view.likeView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class DotsView extends View {
    //粒子个数
    private static final int DOTS_COUNT = 6;
    //粒子位置角度
    private static final int DOTS_POSITION_ANGLE = 360 / DOTS_COUNT;
    private static final int DOTS_COLOR = 0xFFF85680;
    private float currentProgress = 0f;
    private int centerX;
    private int centerY;
    private float maxDotSize;
    private Paint dotsCiclePaint = new Paint();
    private float currentRadius = 0;
    private float currentDotSize = 0;
    private Paint ciclePaint = new Paint();
    private float maxDotsRadius;

    public DotsView(Context context) {
        super(context);
        init();
    }

    public DotsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public static double mapValueFromRangeToRange(double value, double fromLow, double fromHigh, double toLow, double toHigh) {
        return toLow + ((value - fromLow) / (fromHigh - fromLow) * (toHigh - toLow));
    }

    private void init() {
        ciclePaint.setStyle(Paint.Style.FILL);
        ciclePaint.setColor(DOTS_COLOR);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2;
        centerY = h / 2;
        //粒子半径
        maxDotSize = 4;
        //最大半径
        maxDotsRadius = w / 2 - maxDotSize * 4;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawOuterDotsFrame(canvas);
    }

    private void drawOuterDotsFrame(Canvas canvas) {
        for (int i = 0; i < DOTS_COUNT; i++) {
            int cX = (int) (centerX + currentRadius * Math.cos(i * DOTS_POSITION_ANGLE * Math.PI / 180));
            int cY = (int) (centerY + currentRadius * Math.sin(i * DOTS_POSITION_ANGLE * Math.PI / 180));
            canvas.drawCircle(cX, cY, currentDotSize, ciclePaint);
        }
    }

    public void setCurrentProgress(float currentProgress) {
        this.currentProgress = currentProgress;
        updateOuterDotsPosition();
        postInvalidate();
    }

    private void updateOuterDotsPosition() {
        if (currentProgress < 0.3f) {
            this.currentRadius = (float) mapValueFromRangeToRange(currentProgress, 0.0f, 0.3f, 0, maxDotsRadius * 0.8f);
        } else {
            this.currentRadius = (float) mapValueFromRangeToRange(currentProgress, 0.3f, 1f, 0.8f * maxDotsRadius, maxDotsRadius);
        }

        if (currentProgress < 0.7) {
            this.currentDotSize = maxDotSize;
        } else {
            this.currentDotSize = (float) mapValueFromRangeToRange(currentProgress, 0.7f, 1f, maxDotSize, 0);
        }
    }
}