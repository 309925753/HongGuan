package com.sk.weichat.call.talk;

import com.sk.weichat.bean.message.ChatMessage;

public class MessageTalkReleaseEvent {
    public ChatMessage chatMessage;

    public MessageTalkReleaseEvent(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
