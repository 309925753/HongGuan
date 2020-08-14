package com.sk.weichat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.sk.weichat.util.CrashHandler;
import com.tencent.bugly.crashreport.CrashReport;

import java.net.UnknownHostException;

/**
 * 封装异常上报，
 * 当前使用，腾讯的bugly,
 * <p>
 * 要在Application.onCreate中调用init方法初始化，
 *
 * @author linenlian
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class Reporter {
    private static final String TAG = "Reporter";
    @SuppressLint("StaticFieldLeak")
    private static Context ctx;

    private Reporter() {
    }

    public static void init(Context ctx) {
        Reporter.ctx = ctx.getApplicationContext();
        // 后初始化bugly, 崩溃发生时会先执行bugly的操作，然后bugly会调用先初始化的CrashHandler的Handler,
        // 如果反过来，CrashHandler不会调用先初始化的Handler，
        initLocalLog();
        initBugly();
    }

    private static void initLocalLog() {
        // 将错误日志写入本地
        CrashHandler.getInstance().init(ctx);
    }

    private static void initBugly() {
        // 初始化，第三个参数为SDK调试模式开关，打开会导致开发机上报异常，
        CrashReport.initCrashReport(ctx, BuildConfig.BUGLY_APP_ID, BuildConfig.DEBUG && Log.isLoggable("Bugly", Log.DEBUG));
        // 貌似设置了开发设备就不上报了，不是很靠得住，依然可能上报，
        CrashReport.setIsDevelopmentDevice(ctx, BuildConfig.DEBUG);
        @SuppressLint("HardwareIds")
        String androidId = android.provider.Settings.Secure.getString(ctx.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        // 登录前是设备唯一码，登录后是手机号，
        CrashReport.setUserId(androidId);
        CrashReport.setAppChannel(ctx, BuildConfig.BUGLY_APP_CHANNEL);
        CrashReport.setAppVersion(ctx, BuildConfig.VERSION_NAME + BuildConfig.VERSION_NAME_SUFFIX);
    }

    /**
     * 设置上报到bugly用户ID,
     * 登录前是设备唯一码，登录后是手机号，
     */
    public static void setUserId(String userId) {
        CrashReport.setUserId(userId);
    }

    /**
     * 保存用户信息，最多9对，
     * <p>
     * Bugly对键值对的限制，
     * 最多可以有9对自定义的key-value（超过则添加失败）；
     * key限长50字节，value限长200字节，过长截断；
     * key必须匹配正则：[a-zA-Z[0-9]]+。
     */
    public static void putUserData(String key, String value) {
        CrashReport.putUserData(ctx, key, value);
        CrashHandler.getInstance().putUserData(key, value);
    }

    private static void debug(String message, Throwable t) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, message, t);
        }
    }

    /**
     * 对不可空的参数调用该方法，上报参数异常并抛出，
     */
    public static <E> E notNullOrReport(E e, String value) {
        if (e == null) {
            String message = value + "不可空，";
            RuntimeException t = new IllegalArgumentException(message);
            post(message, t);
            throw t;
        }
        return e;
    }

    /**
     * 无法到达的代码块调用这个方法，
     * 以防万一到达了可以看到，
     */
    public static void unreachable() {
        post("不可到达，");
    }

    /**
     * 无法到达的代码块调用这个方法，
     * 以防万一到达了可以看到，
     */
    public static void unreachable(Throwable t) {
        post("不可到达，", t);
    }

    public static void post(String message) {
        if (message == null) {
            message = "null";
        }
        Throwable t = new IllegalStateException(message);
        debug(message, t);
        postException(t);
    }

    public static void post(String message, Throwable t) {
        if (message == null) {
            message = "null";
        }
        debug(message, t);
        postException(new IllegalStateException(message, t));
    }

    private static void postException(Throwable t) {
        if (t == null) {
            return;
        }
        // 开发过程不要上报，
        if (BuildConfig.DEBUG) {
            return;
        }
        Throwable cause = t;
        while (cause != null) {
            if (isNoInternetException(cause)) {
                // 没有网络连接导致的异常不上报，
                return;
            }
            // 以防万一，虽然应该不会出现cause就是本身导致死循环，
            if (cause.getCause() == cause) {
                break;
            }
            cause = cause.getCause();
        }
        CrashReport.postCatchedException(t);
    }

    /**
     * 判断这个异常是不是没有网络导致的，
     * 不准确，只是过滤部分明显的情况，
     *
     * @return 是断网导致的异常则返回true,
     */
    private static boolean isNoInternetException(Throwable t) {
        if (t == null) {
            return false;
        }
        if (t instanceof UnknownHostException) {
            return true;
        }
        //noinspection RedundantIfStatement
        if (t.getMessage() != null && t.getMessage().contains("No address associated with hostname")) {
            // 有的设备报的不是UnknownHostException，原因不明，
            // android_getaddrinfo failed: EAI_NODATA (No address associated with hostname)
            return true;
        }
        return false;
    }

    /**
     * 对不可空的参数调用该方法，上报参数异常并抛出，
     */
    public <E> E notNullOrReport(E e) {
        return notNullOrReport(e, "value");
    }
}
