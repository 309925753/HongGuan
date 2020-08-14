package com.sk.weichat.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * SharedPreferences存储数据类
 */
public class PreferenceUtils {
    private static SharedPreferences sp;

    private static SharedPreferences getPreferences(Context context) {
        if (sp == null) {
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        return sp;
    }

    /**
     * 设置boolean类型的 配置数据
     *
     * @param context
     * @param key
     * @param value
     */
    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = getPreferences(context);
        Editor edit = sp.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    /**
     * 获得boolean类型的信息
     *
     * @param context
     * @param key
     * @param defValue ： 没有时的默认值
     * @return
     */
    public static boolean getBoolean(Context context, String key, boolean defValue) {
        SharedPreferences sp = getPreferences(context);
        return sp.getBoolean(key, defValue);
    }

    /**
     * 获得boolean类型的信息,如果没有返回false
     *
     * @param context
     * @param key
     * @return
     */
    public static boolean getBoolean(Context context, String key) {
        return getBoolean(context, key, false);
    }

    /**
     * 存储String的数据
     */
    public static void putString(Context context, String key, String value) {
        SharedPreferences sp = getPreferences(context);
        Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }

    public static String getString(Context context, String key, String defValue) {
        SharedPreferences sp = getPreferences(context);
        return sp.getString(key, defValue);
    }

    public static String getString(Context context, String key) {
        SharedPreferences sp = getPreferences(context);
        return getString(context, key, null);
    }

    /**
     * 存储int型数据
     *
     * @param context
     * @param key
     * @param value
     */
    public static void putInt(Context context, String key, int value) {
        SharedPreferences sp = getPreferences(context);
        Editor edit = sp.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    public static void putApplyInt(Context context, String key, int value) {
        SharedPreferences sp = getPreferences(context);
        Editor edit = sp.edit();
        edit.putInt(key, value);
        edit.apply();
    }

    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sp = getPreferences(context);
        return sp.getInt(key, defValue);
    }

    public static int getInt(Context context, String key) {
        SharedPreferences sp = getPreferences(context);
        return getInt(context, key, -1);
    }

    /**
     * 存储long型数据
     *
     * @param context
     * @param key
     * @param value
     */
    public static void putLong(Context context, String key, long value) {
        SharedPreferences sp = getPreferences(context);
        Editor edit = sp.edit();
        edit.putLong(key, value);
        edit.commit();
    }

    public static Long getLong(Context context, String key, long defValue) {
        SharedPreferences sp = getPreferences(context);
        return sp.getLong(key, defValue);
    }

    public static Long getLong(Context context, String key) {
        SharedPreferences sp = getPreferences(context);
        return getLong(context, key, 1l);
    }
}

