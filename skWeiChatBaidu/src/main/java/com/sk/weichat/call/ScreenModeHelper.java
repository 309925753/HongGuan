package com.sk.weichat.call;

import android.content.Intent;

import androidx.annotation.Nullable;

import java.lang.reflect.Method;

public class ScreenModeHelper {
    @Nullable
    private static Method getMethod(String name, Class<?>... parameterTypes) {
        try {
            Class<?> clazz = getClazz();
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (Exception e) {
            return null;
        }
    }

    private static Class<?> getClazz() throws ClassNotFoundException {
        return Class.forName("com.oney.WebRTCModule.VideoCaptureController");
    }

    private static Object invoke(String name, Class[] argsClass, Object... args) {
        try {
            Method method = getMethod(name, argsClass);
            return method.invoke(null, args);
        } catch (Exception e) {
            return null;
        }
    }
    public static boolean isEnable() {
        return getMethod("stopScreenMode") != null;
    }

    public static void startScreenMode(Runnable requestPermission) {
        invoke("startScreenMode", new Class[]{Runnable.class}, requestPermission);
    }

    public static void startScreenMode(Intent data) {
        invoke("startScreenMode", new Class[]{Intent.class}, data);
    }

    public static void stopScreenMode() {
        invoke("stopScreenMode", new Class[0]);
    }

}
