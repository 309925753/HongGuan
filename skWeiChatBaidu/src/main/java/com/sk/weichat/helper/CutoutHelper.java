package com.sk.weichat.helper;

import android.os.Build;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

public class CutoutHelper {
    private static final String TAG = "CutoutHelper";

    private CutoutHelper() {
    }

    public static void setWindowOut(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(lp);
        }
    }

    /**
     * 初始化一个高度等于刘海高度的view,
     */
    public static void initCutoutHolderTop(Window window, View vCutoutHolder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        if (vCutoutHolder == null) {
            Log.w(TAG, "initCutoutHolderTop: vCutoutHolder == null");
            return;
        }
        // 初始化时rootWindowInsets为null, 所以要post,
        vCutoutHolder.post(() -> {
            DisplayCutout displayCutout = window.getDecorView().getRootWindowInsets().getDisplayCutout();
            if (displayCutout == null) {
                return;
            }
            Log.d(TAG, "initCutoutHolderTop: " + displayCutout);
            ViewGroup.LayoutParams lp = vCutoutHolder.getLayoutParams();
            lp.height = displayCutout.getSafeInsetTop();
            vCutoutHolder.setLayoutParams(lp);
        });
    }
}
