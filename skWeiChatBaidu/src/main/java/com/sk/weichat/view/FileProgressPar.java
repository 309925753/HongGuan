package com.sk.weichat.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.sk.weichat.util.ViewPiexlUtil;

/**
 * 文件上传进度条
 * Created by xuan on 2018-12-18 12:16:01
 */

public class FileProgressPar extends View {

    private int max = 100;
    private float cur = 50f, centy;
    private int width, height;
    private Paint bgPaint, cPaint;
    private boolean isProgress;

    public FileProgressPar(Context context) {
        this(context, null);
    }

    public FileProgressPar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FileProgressPar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        cPaint = new Paint();
        cPaint.setAntiAlias(true);
        cPaint.setColor(Color.parseColor("#7AD637"));
        cPaint.setStyle(Paint.Style.FILL);

        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(Color.parseColor("#E3E3E3"));
        cPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        centy = (height - ViewPiexlUtil.dp2px(getContext(), 0.8f)) * 0.5f;
        setMeasuredDimension(width, height);
    }

    @Override
    public void onDraw(Canvas canvas) {
        // 背景
        if (isProgress) {
            float movex = cur / max * width;
            canvas.drawRect(0, 0, width, height, bgPaint);
            canvas.drawRect(0, 0, movex, height, cPaint);
        } else {
            canvas.drawRect(0, centy, width, height - centy, bgPaint);
        }
    }

    public void visibleMode(boolean progress) {
        this.isProgress = progress;
        invalidate();
    }

    public void update(int value) {
        Log.e("xuan", "update: " + value);
        cur = value; // (int) Math.floor(value / max * 100);
        invalidate();
    }
}
