package com.redchamber.lib.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

public class PreferenceUtils {

    private static Context sContext;

    private static final String USER_ID = "user_id";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String MOBILE_PHONE = "mobile_phone";
    private static final String KEY_SEX = "key_sex";

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    public static String getAccessToken() {
        return getString(ACCESS_TOKEN, "");
    }

    public static void saveToken(String token) {
        saveString(ACCESS_TOKEN, token);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return getPreferences().getBoolean(key, defValue);
    }

    public static void saveBoolean(String key, boolean value) {
        getPreferences().edit().putBoolean(key, value).apply();
    }

    private static int getInt(String key, int defValue) {
        return getPreferences().getInt(key, defValue);
    }

    private static void saveInt(String key, int value) {
        getPreferences().edit().putInt(key, value).apply();
    }

    private static long getLong(String key, long defValue) {
        return getPreferences().getLong(key, defValue);
    }

    private static void saveLong(String key, long value) {
        getPreferences().edit().putLong(key, value).apply();
    }

    private static String getString(String key, @Nullable String defValue) {
        return getPreferences().getString(key, defValue);
    }

    private static void saveString(String key, @Nullable String value) {
        getPreferences().edit().putString(key, value).apply();
    }

    private static void saveFloat(String key, @Nullable float value) {
        getPreferences().edit().putFloat(key, value).apply();
    }

    private static float getFloat(String key, @Nullable float defValue) {
        return getPreferences().getFloat(key, defValue);
    }

    private static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(sContext);
    }

    public static String getUserId() {
        return getString(USER_ID, "");
    }

    public static void saveUserId(String userId) {
        saveString(USER_ID, userId);
    }

    public static void saveMobilePhone(String mobile) {
        saveString(MOBILE_PHONE, mobile);
    }

    public static String getMobilePhone() {
        return getString(MOBILE_PHONE, "");
    }

    private static String getPreferencesKeyWithUserId(String key) {
        return key + PreferenceUtils.getUserId();
    }


    public static void saveSex(int sex) {
        saveInt(KEY_SEX, sex);
    }

    public static int getSex() {
        return getInt(KEY_SEX, 0);
    }

}
