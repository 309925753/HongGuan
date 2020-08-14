package com.sk.weichat.util;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

/**
 * 封装一些日志打印，统一方式打印一些麻烦的对象，
 * 方便崩溃上报后排查问题，
 */
public final class LogUtils {
    private static final String TAG = "LogUtils";

    private LogUtils() {
    }

    public static void log(Object obj) {
        log(TAG, obj);
    }

    public static void log(String tag, Object obj) {
        if (obj instanceof Intent) {
            log(tag, (Intent) obj);
        } else if (obj instanceof Bundle) {
            log(tag, (Bundle) obj);
        } else if (obj instanceof CharSequence) {
            log(tag, obj.toString());
        } else {
            log(tag, JSON.toJSONString(obj));
        }
    }

    private static void log(String tag, Intent intent) {
        StringBuilder sb = new StringBuilder();
        sb.append("intent = ");
        if (intent == null) {
            sb.append("null").append('\n');
        } else {
            sb.append(intent.toString()).append('\n');
            sb.append("action = ").append(intent.getAction()).append('\n');
            sb.append("data = ").append(intent.getDataString()).append('\n');
            sb.append("extra = ");
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                sb.append("null").append('\n');
            } else {
                Map<String, Object> map = new HashMap<>();
                for (String key : bundle.keySet()) {
                    Object value = bundle.get(key);
                    map.put(key, value);
                }
                sb.append(map.toString());
            }
        }
        realLog(tag, sb.toString());
    }

    private static void log(String tag, Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        sb.append("bundle = ");
        Map<String, Object> map = new HashMap<>();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value != null) {
                map.put(key, value);
            }
        }
        sb.append(map.toString());
        realLog(tag, sb.toString());
    }

    private static void log(String tag, String str) {
        realLog(tag, str);
    }

    private static void realLog(String tag, String str) {
        // TAG长度不能超过23，否则崩溃，
        if (tag.length() > 23) {
            tag = tag.substring(0, 23);
        }
        if (Log.isLoggable(tag, Log.WARN)) {
            Log.w(tag, str);
        }
    }
}
