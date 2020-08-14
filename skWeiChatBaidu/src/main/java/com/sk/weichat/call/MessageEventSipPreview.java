package com.sk.weichat.call;

import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.message.ChatMessage;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
public class MessageEventSipPreview {
    public final String userid;
    public final Friend friend;
    public ChatMessage message;

    public MessageEventSipPreview(String userid, Friend friend, ChatMessage message) {
        this.userid = userid;
        this.friend = friend;
        this.message = message;
    }
}