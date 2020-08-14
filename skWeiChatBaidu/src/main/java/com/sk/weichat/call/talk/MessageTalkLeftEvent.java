package com.sk.weichat.call.talk;

import com.sk.weichat.bean.message.ChatMessage;

public class MessageTalkLeftEvent {
    public ChatMessage chatMessage;

    public MessageTalkLeftEvent(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
