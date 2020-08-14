package com.sk.weichat.util.input;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class KeyboardPanelUtil {
    private static final String TAG = "KeyboardPanelUtil";
    private static final String KEY_KEYBOARD_HEIGHT = "KEY_KEYBOARD_HEIGHT";
    private static int mKeyboardHeight = -1;

    private KeyboardPanelUtil() {
    }

    public static void setKeyboardListener(Window window) {
        SoftKeyBoardListener.setListener(window);
    }

    public static void updateSoftInputMethod(Window window, int softInputMode) {
        WindowManager.LayoutParams p = window.getAttributes();
        if (p.softInputMode == softInputMode) {
            return;
        }
        p.softInputMode = softInputMode;
        window.setAttributes(p);
    }

    public static int getKeyboardHeight(Context context) {
        int height = mKeyboardHeight;
        // 如果是默认的-1，就从本地获取，否则不管是什么都返回出去，包括0的情况，不重复读preference,
        if (height < 0) {
            height = PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_KEYBOARD_HEIGHT, 0);
            mKeyboardHeight = height;
        }
        Log.i(TAG, "getKeyboardHeight() returned: " + height);
        return mKeyboardHeight;
    }

    static void setKeyboardHeight(Context context, int height) {
        Log.i(TAG, "setKeyboardHeight() called with: context = [" + context + "], height = [" + height + "]");
        mKeyboardHeight = height;
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putInt(KEY_KEYBOARD_HEIGHT, height)
                .apply();
    }
}
