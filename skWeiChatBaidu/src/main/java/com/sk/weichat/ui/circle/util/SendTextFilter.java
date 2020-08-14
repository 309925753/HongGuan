package com.sk.weichat.ui.circle.util;

public class SendTextFilter {
    /**
     * 统一对朋友圈消息文本做限制，
     * 仿wx最多保留两个连续换行，并删除头尾空白符，
     */
    public static String filter(String text) {
        if (text == null) {
            return "";
        }
        return text.trim()
                .replaceAll("\n\n\n+", "\n\n");
    }
}
