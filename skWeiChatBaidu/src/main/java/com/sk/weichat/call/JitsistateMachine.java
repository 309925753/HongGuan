package com.sk.weichat.call;

/**
 * Created by ls on 2017/12/19 0019.
 */

//持有jitsi通话状态的类
public class JitsistateMachine {
    public static boolean isInCalling = false;
    public static String callingOpposite = "";// 当前正在通话的对象

    public static boolean isFloating = false; // 是否处于悬浮窗状态

    /**
     * 因为收到来电，挂断消息都是通过eventbus进行通知的，而eventbus是不太可控的，会导致一个问题
     * b离线，a拨号b，a挂断，a在拨号b，b上线，b唤醒的来电界面被挂断的eventbus处理掉了(这个逻辑其实是正常的，
     * 但是第二次拨号的eventbus是和第一次拨号的enemtbus同一时间处理掉了，所以不会再次弹起来电界面)
     * <p>
     * 这里记录最近一次收到来电的时间，如evnetbus挂断消息的时间小于此挂断，不处理
     * todo 这样处理有bug，现在的问题是来电界面未关闭，但是该来电界面是第一个eventbus唤醒的，对不上最后来电，延后处理下吧
     */
    public static long lastTimeInComingCall;// 最近一次收到来电的时间

    public static void reset() {
        isInCalling = false;
        callingOpposite = "";
    }
}
