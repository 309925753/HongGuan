package com.sk.weichat.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.sk.weichat.Reporter;

import java.io.File;

/**
 * 用于记录本机上传的文件的url,
 * 使用时直接读取本地文件，
 */
public class UploadCacheUtils {
    public static final String NAME_UPLOAD_CACHE = "upload_cache";

    public static SharedPreferences getSp(Context ctx) {
        return ctx.getSharedPreferences(NAME_UPLOAD_CACHE, Context.MODE_PRIVATE);
    }

    public static void save(Context ctx, String url, String filePath) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(filePath)) {
            Reporter.unreachable();
            return;
        }
        getSp(ctx).edit()
                .putString(url, filePath)
                .apply();
    }

    /**
     * 获取文件url,
     * 如果没有缓存就直接返回传入的url,
     * 如果有缓存就返回文件的url, file:///开头的，
     */
    public static String get(Context ctx, String url) {
        String local = getOrNull(ctx, url);
        if (TextUtils.isEmpty(local)) {
            return url;
        }
        File file = new File(local);
        if (!file.exists()) {
            return url;
        }
        return file.toURI().toASCIIString();
    }

    @SuppressWarnings("WeakerAccess")
    public static String getOrNull(Context ctx, String url) {
        return getSp(ctx).getString(url, null);
    }
}
