package com.sk.weichat.bean.event;

/**
 * 收到511消息，本地新建手机联系人，通知通讯录页面刷新
 */

public class MessageContactEvent {
    public final String message;

    public MessageContactEvent(String message) {
        this.message = message;
    }
}