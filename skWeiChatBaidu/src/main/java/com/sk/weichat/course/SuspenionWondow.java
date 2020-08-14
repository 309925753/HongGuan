package com.sk.weichat.course;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.sk.weichat.MyApplication;
import com.sk.weichat.util.DisplayUtil;

/**
 * Created by Administrator on 2017/11/9.
 */

public class SuspenionWondow {
    Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mView;
    private boolean isVisible;

    private int mLastX = 0, mLastY = 0;
    private int mStartX = 0, mStartY = 0;
    private View.OnTouchListener mTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            if (MotionEvent.ACTION_DOWN == action) {
                mStartX = mLastX = (int) event.getRawX();
                mStartY = mLastY = (int) event.getRawY();
            } else if (MotionEvent.ACTION_MOVE == action) {
                int dx = (int) event.getRawX() - mLastX;
                int dy = (int) event.getRawY() - mLastY;
                mLayoutParams.x = mLayoutParams.x + dx;
                mLayoutParams.y = mLayoutParams.y + dy;
                mWindowManager.updateViewLayout(mView, mLayoutParams);
                mLastX = (int) event.getRawX();
                mLastY = (int) event.getRawY();
            } else if (MotionEvent.ACTION_UP == action) {
                int dx = (int) event.getRawX() - mStartX;
                int dy = (int) event.getRawY() - mStartY;
                if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                    return true;
                }
            }
            return false;
        }
    };

    public SuspenionWondow(Context content) {
        mContext = content;
        mWindowManager = (WindowManager) MyApplication.getContext().getSystemService(MyApplication.getContext().WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {// 8.0或8.0以上
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayoutParams.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.width = DisplayUtil.dip2px(mContext, 100);
        mLayoutParams.height = DisplayUtil.dip2px(mContext, 120);
    }

    public void show(View view) {
        if (!isVisible) {
            isVisible = true;
            mView = view;
            if (mWindowManager != null) {
                mWindowManager.addView(view, mLayoutParams);
            }
            mView.setOnTouchListener(mTouch);
        }
    }

    public void hide() {
        if (isVisible) {
            isVisible = false;
            mWindowManager.removeView(mView);
        }
    }
}
