package com.redchamber.util;

import android.text.TextUtils;

public class SplitUtils {

    public static String splitBySemicolon(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        String[] arr = string.split(";");
        StringBuilder result = new StringBuilder();
        for (String str : arr) {
            result.append(str).append(".");
        }
        result.delete(result.length() - 1, result.length());
        return result.toString();
    }


    public static String splitAgeConstellation(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        String[] arr = string.split(";");
        StringBuilder result = new StringBuilder();
        result.append(arr[0]).append("岁.");
        result.append(arr[1]).append("");
        return result.toString();
    }

}
