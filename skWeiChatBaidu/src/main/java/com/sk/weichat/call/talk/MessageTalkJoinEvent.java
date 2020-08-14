package com.sk.weichat.call.talk;

import com.sk.weichat.bean.message.ChatMessage;

public class MessageTalkJoinEvent {
    public ChatMessage chatMessage;

    public MessageTalkJoinEvent(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
