package com.sk.weichat.util;


import android.text.TextUtils;

import com.sk.weichat.MyApplication;

/**
 * 获取<a>标签 Pattern.compile("(<a[^<>]*>[^<>]*</a>)")
 */
public class HtmlUtils {

    public static CharSequence transform50SpanString(String msg,
                                                     boolean canClick) {
        CharSequence sequence = transformSpanString(msg, canClick);
        if (sequence.length() > 50) {
            return sequence.subSequence(0, 50);
        } else {
            return sequence;
        }
    }

    public static CharSequence transform200SpanString(String msg, boolean canClick) {
        CharSequence sequence = transformSpanString(msg, canClick);
        return sequence;
    }

    /**
     * @canClick 是不是可以点击 如:在列表展示的时候可以不用点击
     */
    public static CharSequence transformSpanString(String msg, boolean canClick) {
        return addSmileysToMessage(deleteHtml(msg), canClick);
    }

    public static CharSequence addSmileysToMessage(String msg, boolean canClick) {
        if (!TextUtils.isEmpty(msg)) {
            SmileyParser parser = SmileyParser.getInstance(MyApplication.getInstance());
            return parser.addSmileySpans(msg, canClick);
        }
        return "";
    }

    private static String deleteHtml(String msg) {
        if (msg == null) {
            return "";
        }
        return msg;
    }
}
