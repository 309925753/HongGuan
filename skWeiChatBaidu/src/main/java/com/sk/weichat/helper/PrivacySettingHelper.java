package com.sk.weichat.helper;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.bean.PrivacySetting;

public class PrivacySettingHelper {
    public static final String KEY_PRIVACY_SETTINGS = "KEY_PRIVACY_SETTINGS";
    private static PrivacySetting settings;

    private PrivacySettingHelper() {
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return ctx.getSharedPreferences("privacy_settings", Context.MODE_PRIVATE);
    }

    public static void setPrivacySettings(Context ctx, PrivacySetting settings) {
        PrivacySettingHelper.settings = settings;
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        if (settings == null) {
            editor.putString(KEY_PRIVACY_SETTINGS, "");
        } else {
            editor.putString(KEY_PRIVACY_SETTINGS, JSON.toJSONString(settings));
        }
        editor.apply();
    }

    @NonNull
    public static PrivacySetting getPrivacySettings(Context ctx) {
        if (settings != null) {
            return settings;
        }
        synchronized (PrivacySettingHelper.class) {
            if (settings != null) {
                return settings;
            }
            String settingsString = getSharedPreferences(ctx).getString(KEY_PRIVACY_SETTINGS, null);
            try {
                settings = JSON.parseObject(settingsString, PrivacySetting.class);
            } catch (Exception ignored) {
            }
            if (settings == null) {
                settings = new PrivacySetting();
            }
        }
        return settings;
    }
}
