package com.sk.weichat.call.talk;

import com.sk.weichat.bean.message.ChatMessage;

public class MessageTalkRequestEvent {
    public ChatMessage chatMessage;

    public MessageTalkRequestEvent(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
