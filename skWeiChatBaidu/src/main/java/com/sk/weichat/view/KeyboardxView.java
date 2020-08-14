package com.sk.weichat.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.sk.weichat.util.DisplayUtil;


/**
 * 仿微信键盘
 *
 * @author xuan
 * @date 2018-11-22 15:46:12
 */
public class KeyboardxView extends View {

    @ColorInt// 键盘文字颜色
    private static final int TEXT_COLOR = Color.parseColor("#000000");
    @ColorInt// 左下角和佑下角的特殊色块
    private static final int OTHER_COLOR = Color.parseColor("#E0E0E0");
    @ColorInt // 按下的颜色
    private static final int PRESS_COLOR = Color.parseColor("#8b8a8a");
    @ColorInt// 键盘分割线颜色
    private static final int LINE_COLOR = Color.parseColor("#BDBDBD");
    String[] texts = {
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            ".", "0", "←",
    };
    int maxLength = 12;
    private Paint mRectPaint;
    private float mGridRectWidth;
    private float mGridRectHeight;
    private float mWidth;
    private float mHeight;
    private boolean isPress;
    private boolean isDecimal;
    private int downX;
    private int downY;
    private Paint mTextPaint;
    private StringBuilder mCurrentInput;
    private OnInputTextListener mListener;

    public KeyboardxView(Context context) {
        this(context, null);
    }

    public KeyboardxView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyboardxView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setColor(LINE_COLOR);
        mRectPaint.setStrokeWidth(0.5f);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(TEXT_COLOR);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(DisplayUtil.dip2px(context, 26));
        mTextPaint.setTypeface(Typeface.DEFAULT);

        mCurrentInput = new StringBuilder();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mGridRectWidth = mWidth / 3;
        mGridRectHeight = mHeight / 4;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // 特殊色块
        changeRectColor(0, 3, canvas, OTHER_COLOR);
        changeRectColor(2, 3, canvas, OTHER_COLOR);

        // 按下变色
        if (isPress) {
            changeRectColor(downX, downY, canvas, PRESS_COLOR);
        }


        // 画两根竖线
        mRectPaint.setColor(LINE_COLOR);
        mRectPaint.setStyle(Paint.Style.STROKE);
        for (int i = 1; i < 3; i++) {
            canvas.drawLine(i * mGridRectWidth, 0, i * mGridRectWidth, mHeight, mRectPaint);
        }

        // 画四根横线
        for (int i = 0; i < 4; i++) {
            canvas.drawLine(0, i * mGridRectHeight + 0.75f, mWidth, i * mGridRectHeight, mRectPaint);
        }

        // 键盘文字绘制
        float baseLineY = Math.abs(mTextPaint.ascent() + mTextPaint.descent()) / 2;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                int num = i * 3 + j;
                int x = (int) (mGridRectWidth * (j + 0.5f));
                int y = (int) (mGridRectHeight * (i + 0.5f));
                canvas.drawText(texts[num], x, y + baseLineY, mTextPaint);
            }
        }
    }

    private void changeRectColor(int x, int y, Canvas canvas, int color) {
        mRectPaint.setColor(color);
        mRectPaint.setStyle(Paint.Style.FILL);
        float top = y * mGridRectHeight;
        float left = x * mGridRectWidth;
        float bottom = top + mGridRectHeight;
        float right = left + mGridRectWidth;
        canvas.drawRect(left, top, right, bottom, mRectPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPress = true;
                downX = (int) (event.getX() / mGridRectWidth);
                downY = (int) (event.getY() / mGridRectHeight);

                //                Log.e("xuan", "onTouchEvent: " + downX + " , " + downY);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                isPress = false;
                if (mListener != null) {
                    int num = downY * 3 + downX;
                    if (num == texts.length - 1) { // 退格
                        if (mCurrentInput.length() > 0) {
                            mCurrentInput.delete(mCurrentInput.length() - 1, mCurrentInput.length());
                        }
                    } else if (num == 9) { // 小数点
                        if (!isDecimal && mCurrentInput.length() < maxLength) {
                            isDecimal = true;
                            if (mCurrentInput.length() == 0) {
                                mCurrentInput.append("0");
                            }
                            mCurrentInput.append(texts[num]);
                        }
                    } else {
                        if ("0".equals(mCurrentInput.toString())) {
                            mCurrentInput.delete(0, 1);
                        }

                        int decimal = 0;
                        if (isDecimal && mCurrentInput.length() > 2) {
                            decimal = mCurrentInput.length() - mCurrentInput.lastIndexOf(".");
                            Log.e("xuan", "onTouchEvent: " + decimal);
                        }

                        if (decimal < 3 && mCurrentInput.length() < maxLength) { // 控制小数位
                            mCurrentInput.append(texts[num]);
                        }
                    }

                    isDecimal = mCurrentInput.lastIndexOf(".") > -1;
                    mListener.onInputChange(mCurrentInput.toString());
                }
                invalidate();
                break;
        }

        return true;
    }

    public void addOnInputTextListener(OnInputTextListener listener) {
        mListener = listener;
    }

    public void reset() {
        isDecimal = false;
        mCurrentInput.delete(0, mCurrentInput.length());
        mListener.onInputChange("");
    }

    public void setMaxLength(int max) {
        maxLength = max;
    }

    public interface OnInputTextListener {
        void onInputChange(String text);
    }
}