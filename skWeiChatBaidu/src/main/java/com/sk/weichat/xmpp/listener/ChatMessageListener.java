package com.sk.weichat.xmpp.listener;

import com.sk.weichat.bean.message.ChatMessage;

public interface ChatMessageListener {
    int MESSAGE_SEND_ING = 0;     // 发送中
    int MESSAGE_SEND_SUCCESS = 1; // 发送成功
    int MESSAGE_SEND_FAILED = 2;  // 发送失败

    // 消息发送状态的回调
    void onMessageSendStateChange(int messageState, String msgId);

    // 消息来临时的回调
    boolean onNewMessage(String fromUserId, ChatMessage message, boolean isGroupMsg);
}
