package com.sk.weichat.call.talk;

import com.sk.weichat.bean.message.ChatMessage;

public class MessageTalkOnlineEvent {
    public ChatMessage chatMessage;

    public MessageTalkOnlineEvent(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
