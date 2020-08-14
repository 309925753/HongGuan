package com.sk.weichat.ui.xrce;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sk.weichat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 修改视频速度的
 * create xuan
 * time 2018-11-21 09:32:21
 */
public class Xspeedbar extends FrameLayout {

    List<TextView> views;
    private int mCurrt; // 当前选择
    private int mWidth;
    private int mRectWidth;
    private OnChangeListener mListener;

    public Xspeedbar(Context context) {
        this(context, null);
    }

    public Xspeedbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Xspeedbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View.inflate(context, R.layout.view_change_speed, this);
        init(context);
    }

    private void init(Context context) {
        views = new ArrayList<>();
        views.add(findViewById(R.id.tv_select0));
        views.add(findViewById(R.id.tv_select1));
        views.add(findViewById(R.id.tv_select2));
        views.add(findViewById(R.id.tv_select3));
        views.add(findViewById(R.id.tv_select4));
        mCurrt = 2;
    }

    public void addOnChangeListener(OnChangeListener listener) {
        this.mListener = listener;
    }

    public void show() {
        setAlpha(1);
    }

    public void hide() {
        setAlpha(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int select = (int) Math.floor(event.getX() / mRectWidth);
            Log.e("xuan", "onTouchEvent: " + select);
            changeSelect(select);
        }
        return true;
    }

    public void changeSelect(int select) {
        if (select == mCurrt) {
            return;
        }

        views.get(mCurrt).setTextColor(getResources().getColor(R.color.white));
        views.get(select).setTextColor(getResources().getColor(R.color.app_black_75));
        views.get(mCurrt).setBackgroundResource(R.color.transparent);
        views.get(select).setBackgroundResource(R.drawable.bg_tip_dialog);
        mCurrt = select;
        if (mListener != null) {
            mListener.change(select);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mRectWidth = mWidth / 5;
    }

    public interface OnChangeListener {

        void change(int select);
    }
}

