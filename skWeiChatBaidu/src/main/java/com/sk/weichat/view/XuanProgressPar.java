package com.sk.weichat.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.sk.weichat.util.DisplayUtil;

import static android.animation.ValueAnimator.INFINITE;
import static android.animation.ValueAnimator.REVERSE;

/**
 * 仿照QQ制作的 上传进度条
 * Created by xuan on 2018-12-18 12:16:01
 */

public class XuanProgressPar extends View {
    ObjectAnimator lightAnim;
    private int max = 100;
    private int cur = 50;
    private int width, centx;
    private Paint bgPaint, cPaint, tPaint;
    private float shadow;

    public XuanProgressPar(Context context) {
        this(context, null);
    }

    public XuanProgressPar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XuanProgressPar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        cPaint = new Paint();
        cPaint.setAntiAlias(true);
        cPaint.setColor(Color.WHITE);
        cPaint.setStrokeWidth(10);
        cPaint.setStyle(Paint.Style.STROKE);

        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(Color.BLACK);
        bgPaint.setAlpha((int) (255 * 0.7));


        tPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tPaint.setTextSize(DisplayUtil.dip2px(getContext(), 13));
        tPaint.setColor(Color.WHITE);
        tPaint.setTextAlign(Paint.Align.CENTER);

        statr();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        centx = width >> 1;
        setMeasuredDimension(width, width);

        shadow = 5f;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Rect targetRect = new Rect(0, 0, width, width);

        cPaint.setShadowLayer(shadow, 0, 0, Color.WHITE);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, cPaint);
        canvas.drawCircle(centx, centx, centx - 25, cPaint);

        Paint.FontMetricsInt fontMetrics = tPaint.getFontMetricsInt();
        int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText(cur + "%", centx, baseline, tPaint);
    }

    public void statr() {
        long duration = 800;
        lightAnim = ObjectAnimator.ofFloat(this, "shadow", 20, 5).setDuration(duration);
        lightAnim.setRepeatCount(INFINITE);
        lightAnim.setRepeatMode(REVERSE);
        lightAnim.start();
    }

    public void setShadow(float shadow) {
        this.shadow = shadow;
        invalidate();
    }

    public void update(int value) {
        Log.e("xuan", "update: " + value);
        cur = value; // (int) Math.floor(value / max * 100);
        invalidate();
        if (value == 100) {
            lightAnim.cancel();
        }
    }
}
