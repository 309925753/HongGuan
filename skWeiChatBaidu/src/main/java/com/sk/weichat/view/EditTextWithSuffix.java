package com.sk.weichat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

import com.sk.weichat.R;

public class EditTextWithSuffix extends AppCompatEditText {
    TextPaint textPaint;
    private String suffix = "";
    private float mSuffixWidth;

    public EditTextWithSuffix(Context context) {
        super(context);
        textPaint = super.getPaint();
    }

    public EditTextWithSuffix(Context context, AttributeSet attrs) {
        super(context, attrs);
        textPaint = super.getPaint();
        getAttributes(context, attrs, 0);
    }

    public EditTextWithSuffix(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        textPaint = super.getPaint();
        getAttributes(context, attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (!TextUtils.isEmpty(getText())) {
            int suffixXPosition = (int) textPaint.measureText(getText().toString()) + getPaddingLeft();
            c.drawText(suffix, suffixXPosition, getBaseline(), textPaint);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (suffix != null)
            mSuffixWidth = textPaint.measureText(suffix);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public int getCompoundPaddingRight() {
        return super.getCompoundPaddingRight() + (int) Math.ceil(mSuffixWidth);
    }

    private void getAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditTextWithSuffix, defStyleAttr, 0);
        if (a != null) {
            suffix = a.getString(R.styleable.EditTextWithSuffix_suffix);
            if (suffix == null) {
                suffix = "";
            }
        }
        a.recycle();
    }
}