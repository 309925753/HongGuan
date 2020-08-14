package com.sk.weichat.util;

import android.util.Log;

/**
 * Created by Administrator on 2016/4/7.
 */
public class TanX {
    public static String Tag = "Tanx";
    public static String TAG = "Tanx";
    public static String XMPP = "XMPP";//XMPP打印标示
    public static String AUDIO = "AUDIO";//语音视频即时通讯标示
    public static String BD_PUSH = "BD_PUSH";//百度推送标示
    public static String ACTIVITY_LIFE = "ActivityLife";//百度推送标示

    public static void Log(String s) {
        Log.i(TanX.Tag, s);
    }

    public static void Request(String s) {
        Log.i("request", s);
    }

    public static void LogWithClassName(Class c, String s) {
        Log.i(TanX.Tag, c.getSimpleName() + "" + s);
    }

    public static void LogWithClassWholeName(Class c, String s) {
        Log.i(TanX.Tag, c.getCanonicalName() + "" + s);
    }

    public static void LogXmpp(String s) {
        Log.i(TanX.XMPP, s);
    }

    public static void LogAudio(String s) {
        Log.i(TanX.AUDIO, s);
    }

    public static void LogBdPush(String s) {
        Log.i(TanX.BD_PUSH, s);
    }

    public static void LogAL(String s) {
        Log.i(TanX.ACTIVITY_LIFE, s);
    }
}
