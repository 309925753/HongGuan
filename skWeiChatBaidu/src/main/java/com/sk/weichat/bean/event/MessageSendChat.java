package com.sk.weichat.bean.event;

import com.sk.weichat.bean.message.ChatMessage;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
public class MessageSendChat {
    public final boolean isGroup;
    public final String toUserId;
    public final ChatMessage chat;

    public MessageSendChat(boolean isGroup, String toUserId, ChatMessage chat) {
        this.isGroup = isGroup;
        this.toUserId = toUserId;
        this.chat = chat;
    }
}