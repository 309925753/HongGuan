package com.sk.weichat.util;

import android.text.TextUtils;

import com.sk.weichat.MyApplication;

/**
 * 通用utils,主要为封装一些app特定业务逻辑调用的方法
 * create by zq
 */
public class CommonUtils {
    /**
     * 检查是否有针对当前用户设置聊天背景，如设置了，tip类型消息字体颜色变为黑色
     *
     * @param userId
     * @param friend
     * @return
     */
    public static boolean isSetChatBackground(String userId, String friend) {
        String mChatBgPath = PreferenceUtils.getString(MyApplication.getContext(), Constants.SET_CHAT_BACKGROUND_PATH
                + friend + userId, "reset");

        String mChatBg = PreferenceUtils.getString(MyApplication.getContext(), Constants.SET_CHAT_BACKGROUND
                + friend + userId, "reset");

        if (TextUtils.isEmpty(mChatBgPath)
                || mChatBg.equals("reset")) {// 未设置聊天背景或者还原了聊天背景
            return false;
        }
        return true;
    }
}
