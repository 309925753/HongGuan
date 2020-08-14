package com.sk.weichat.ui.circle;

/**
 * 收到XMPP协议，刷新发现页面
 */
public class MessageEventNotifyDynamic {
    public final int number;

    public MessageEventNotifyDynamic(int number) {
        this.number = number;
    }
}