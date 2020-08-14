package com.sk.weichat.util;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by Administrator on 2017/10/25.
 */

public class JsonUtils {

    public static String fromAt(JSONObject jObject, String key) {
        if (jObject == null || TextUtils.isEmpty(key)) {
            return "";
        }

        String value = "";
        try {
            value = jObject.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (value == null) {
                value = "";
            }
            return value;
        }
    }

    // URL转换为[]
    public static String initJsonContent(String title, String linkUrl, String url) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"title\":");
        sb.append("\"");
        sb.append(title);
        sb.append("\"");
        sb.append(",");
        sb.append("\"url\":");
        sb.append("\"");
        sb.append(linkUrl);
        sb.append("\"");
        sb.append(",");
        sb.append("\"img\":");
        sb.append("\"");
        sb.append(url);
        sb.append("\"");
        sb.append("}");
        return sb.toString();
    }

    // 进群验证 json 拼接
    public static String initJsonContent(String userIds, String userINames, String roomJid, String isInvite, String reason) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"userIds\":");
        sb.append("\"");
        sb.append(userIds);
        sb.append("\"");
        sb.append(",");
        sb.append("\"userNames\":");
        sb.append("\"");
        sb.append(userINames);
        sb.append("\"");
        sb.append(",");
        sb.append("\"roomJid\":");
        sb.append("\"");
        sb.append(roomJid);
        sb.append("\"");
        sb.append(",");
        sb.append("\"isInvite\":");
        sb.append("\"");
        sb.append(isInvite);// 0为邀请，1为主动加入
        sb.append("\"");
        sb.append(",");
        sb.append("\"reason\":");
        sb.append("\"");
        sb.append(reason);
        sb.append("\"");
        sb.append("}");
        return sb.toString();
    }
}
