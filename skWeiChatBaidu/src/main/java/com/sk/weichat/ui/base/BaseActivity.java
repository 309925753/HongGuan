package com.sk.weichat.ui.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.util.LocaleHelper;

public abstract class BaseActivity extends BaseLoginActivity {

    private View swipeBackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this));
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);// 竖屏
        swipeBackLayout = this.getSwipeBackLayout();
    }

    /**
     * 戳一戳动画
     *
     * @param type
     */
    public void shake(int type) {
        if (swipeBackLayout != null) {
            Animation shake;
            if (type == 0) {
                shake = AnimationUtils.loadAnimation(MyApplication.getContext(), R.anim.shake_from);
            } else {
                shake = AnimationUtils.loadAnimation(MyApplication.getContext(), R.anim.shake_to);
            }
            swipeBackLayout.startAnimation(shake);
        }
    }
}
