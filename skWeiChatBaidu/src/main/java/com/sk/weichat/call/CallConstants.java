package com.sk.weichat.call;

import com.sk.weichat.AppConfig;

/**
 * Created by Administrator on 2018/1/22 0022.
 */

public class CallConstants {
    public static final String AUDIO_OR_VIDEO_OR_MEET = "Audio_Or_Video_Or_Meet";
    // 刷新、关闭悬浮窗
    public static final String REFRESH_FLOATING = AppConfig.sPackageName + "Refresh_Floating";
    public static final String CLOSE_FLOATING = AppConfig.sPackageName + "Close_Floating";
    // 通话、会议
    public static final int Audio = 1;
    public static final int Video = 2;
    public static final int Screen = 7;
    public static final int Audio_Meet = 3;
    public static final int Video_Meet = 4;
    public static final int Talk_Meet = 6;
    public static final int Screen_Meet = 8;

    public static boolean isScreenMode(int type) {
        return type == Screen || type == Screen_Meet;
    }

    public static boolean isAudio(int type) {
        return type == 1 || type == 3;
    }

    public static boolean isSingleChat(int type) {
        return type == 1 || type == 2 || type == 7;
    }

    /**
     * 单聊视频通话和语音通话可以切换，
     */
    public static boolean canChange(int type) {
        return type == 1 || type == 2;
    }
}
