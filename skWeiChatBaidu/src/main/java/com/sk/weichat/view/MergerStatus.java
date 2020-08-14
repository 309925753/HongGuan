package com.sk.weichat.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.appcompat.widget.Toolbar;

import com.sk.weichat.util.SkinUtils;

/**
 * Created by Administrator on 2017/12/14 0014.
 * 兼容沉浸式状态栏
 */

public class MergerStatus extends Toolbar {

    public MergerStatus(Context context) {
        this(context, null);
    }

    public MergerStatus(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MergerStatus(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    private void setup() {
        int mCompatPaddingTop = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mCompatPaddingTop = getStatusHeight();
        }
        this.setPadding(getPaddingLeft(), getPaddingTop() + mCompatPaddingTop, getPaddingRight(), getPaddingBottom());
        // 更换Toolbar背景颜色
        this.setBackgroundColor(SkinUtils.getSkin(getContext()).getPrimaryColor());
    }

    private int getStatusHeight() {
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    private float px2dp(float pxVal) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (pxVal / scale);
    }
}
