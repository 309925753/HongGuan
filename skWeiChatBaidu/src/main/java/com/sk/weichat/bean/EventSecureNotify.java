package com.sk.weichat.bean;

import com.sk.weichat.bean.message.ChatMessage;

/**
 * Created by Administrator on 2018/1/13 0013.
 */

public class EventSecureNotify {

    public static final int SINGLE_SNED_KEY_MSG = 1;
    public static final int MULTI_SNED_KEY_MSG = 2;
    public static final int MULTI_SNED_RESET_KEY_MSG = 3;

    private int type;
    private ChatMessage chatMessage;

    public EventSecureNotify(int type, ChatMessage chatMessage) {
        this.type = type;
        this.chatMessage = chatMessage;
    }

    public int getType() {
        return type;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }
}
