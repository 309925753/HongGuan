package com.sk.weichat.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sk.weichat.util.LocaleHelper;

public abstract class BaseLoginFragment extends Fragment implements CoreStatusListener {
    private final String TAG = this.getClass().getSimpleName();
    // 依赖activity里的coreManager，不单独bind服务，防止多次unbind,
    protected CoreManager coreManager = null;

    @Override
    public void onCoreReady() {
        Log.d(TAG, "onCoreReady() called");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity ctx = getActivity();
        LocaleHelper.setLocale(ctx, LocaleHelper.getLanguage(ctx));
        if (ctx instanceof BaseLoginActivity) {
            BaseLoginActivity activity = (BaseLoginActivity) ctx;
            // 只要activity在onCreate里调用了初始化，coreManager就不会空，同步的，
            coreManager = activity.coreManager;
            if (coreManager != null) {
                activity.addCoreStatusListener(this);
            }
        }
    }
}
