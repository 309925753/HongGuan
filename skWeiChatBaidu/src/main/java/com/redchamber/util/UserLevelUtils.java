package com.redchamber.util;

import android.text.TextUtils;

public class UserLevelUtils {

    /**
     * 获取当前用户的userLevel
     *
     * @return arr[0] --> 性别：true 女性 false 男性
     * arr[1] --> VIP：
     * arr[2] --> 女神：
     * arr[3] --> 真人：
     * arr[4] --> 徽章：
     */
    public static boolean[] getLevels(String userLevel) {
        if (!TextUtils.isEmpty(userLevel) && userLevel.length() == 5) {
            char[] arr = userLevel.toCharArray();
            boolean[] levels = new boolean[arr.length];
            levels[0] = '0' == arr[0];
            levels[1] = '1' == arr[1];
            levels[2] = '1' == arr[2];
            levels[3] = '1' == arr[3];
            levels[4] = '1' == arr[4];
            return levels;
        }
        return new boolean[5];
    }

    /**
     * 获取当前用户的性别
     * @return female-->女性
     *         male-->男性
     */
    public static String getSex(String userLevel) {
        if (!TextUtils.isEmpty(userLevel) && userLevel.length() == 5) {
            char[] arr = userLevel.toCharArray();
            return '0' == arr[0] ? "female" : "male";
        }
        return "male";
    }

}
