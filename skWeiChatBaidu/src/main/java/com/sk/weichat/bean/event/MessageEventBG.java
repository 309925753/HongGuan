package com.sk.weichat.bean.event;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
public class MessageEventBG {
    public static boolean isAuthenticated;// 长连接是否连接过

    public final boolean flag;// 程序是否到后台
    public final boolean isCloseError;// 长连接是否关闭或断开，如果isCloseError为true，flag要定位为false，因为他与程序切换到后台所有调用的方法一样

    public MessageEventBG(boolean flag, boolean isCloseError) {
        this.flag = flag;
        this.isCloseError = isCloseError;
    }
}