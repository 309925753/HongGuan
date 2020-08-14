package com.sk.weichat.view;

import android.view.View;

// 防止点击按钮 过快 || 多次点击 造成多次事件
public abstract class NoDoubleClickListener implements View.OnClickListener {

    private static long lastClickTime = 0;

    private static final int MIN_CLICK_DELAY_TIME = 800;

    public abstract void onNoDoubleClick(View view);

    @Override
    public void onClick(View v) {
        long currentTime =  System.currentTimeMillis();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            onNoDoubleClick(v);
        }
    }
}