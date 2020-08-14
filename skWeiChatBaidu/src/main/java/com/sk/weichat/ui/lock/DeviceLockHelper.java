package com.sk.weichat.ui.lock;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.sk.weichat.MyApplication;
import com.sk.weichat.util.Md5Util;
import com.sk.weichat.util.PreferenceUtils;

import java.util.concurrent.TimeUnit;

public class DeviceLockHelper {
    private static final String TAG = "DeviceLockHelper";
    // 设备锁密码，
    private final static String LOCK_PASSWORD = "LOCK_PASSWORD";
    // 自动上锁开关，
    private final static String AUTO_LOCK = "AUTO_LOCK";

    private static Handler autoLockHandler = new Handler(Looper.getMainLooper());
    /**
     * 用于实现五分钟内免解锁，
     * 没开启时该变量无效，
     * 开启时五分钟自动修改这个变量，
     */
    private static boolean lock = true;
    private static Runnable autoLockRunnable = DeviceLockHelper::lock;
    private static Context ctx = MyApplication.getContext();

    /**
     * 每次上锁时调用，
     */
    public static void lock() {
        Log.i(TAG, "lock: ");
        lock = true;
    }

    /**
     * 发起自动上锁事件，
     */
    private static void autoLock() {
        Log.i(TAG, "autoLock: ");
        // 五分钟后自动上锁，
        autoLockHandler.postDelayed(autoLockRunnable, TimeUnit.MINUTES.toMillis(5));
    }

    /**
     * 每次成功解锁时调用，
     */
    public static void unlock() {
        Log.i(TAG, "unlock: ");
        lock = false;
        autoLockHandler.removeCallbacks(autoLockRunnable);
        if (isAutoLock()) {
            // 五分钟后自动上锁，
            autoLock();
        }
    }

    public static String getPassword() {
        return PreferenceUtils.getString(ctx, LOCK_PASSWORD);
    }

    public static void setPassword(String password) {
        PreferenceUtils.putString(ctx, LOCK_PASSWORD, Md5Util.toMD5(password));
        unlock();
    }

    public static boolean checkPassword(String input) {
        return TextUtils.equals(getPassword(), Md5Util.toMD5(input));
    }

    public static boolean isLocked() {
        // 启用设备锁且已经上锁，
        // isEnabled && ((isAutoLock && lock) || !isAutoLock)
        if (!isEnabled()) {
            return false;
        }
        if (!isAutoLock()) {
            return true;
        }
        return lock;
    }

    public static void clearPassword() {
        PreferenceUtils.putString(ctx, LOCK_PASSWORD, "");
        unlock();
    }

    public static boolean isEnabled() {
        return !TextUtils.isEmpty(getPassword());
    }

    public static boolean isAutoLock() {
        return isEnabled() && PreferenceUtils.getBoolean(ctx, AUTO_LOCK, true);
    }

    public static void setAutoLock(boolean isChecked) {
        PreferenceUtils.putBoolean(ctx, AUTO_LOCK, isChecked);
        if (isChecked) {
            autoLock();
        }
    }
}
