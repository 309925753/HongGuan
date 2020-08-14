package com.redchamber.util;

import android.content.Context;
import android.text.TextUtils;

import com.sk.weichat.ui.base.CoreManager;

public class RedAvatarUtils {

    public static String getAvatarUrl(Context context, String userId) {
        if (TextUtils.isEmpty(userId)) {
            return "";
        }
//        "http://file57.quyangapp.com/avatar/o/37/10000037.jpg";
        String baseUrl = CoreManager.getInstance(context).getConfig().downloadAvatarUrl + "avatar/o/";
        int userIdInt = Integer.parseInt(userId) % 10000;
        return baseUrl + userIdInt + "/" + userId + ".jpg";
    }

}
