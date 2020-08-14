package com.sk.weichat.call;

import com.sk.weichat.bean.message.ChatMessage;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
public class MessageCallTypeChange {
    public ChatMessage chatMessage;

    public MessageCallTypeChange(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}