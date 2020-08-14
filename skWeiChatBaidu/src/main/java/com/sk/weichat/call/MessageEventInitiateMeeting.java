package com.sk.weichat.call;

import java.util.List;

public class MessageEventInitiateMeeting {
    public final int type;
    public final List<String> list;

    public MessageEventInitiateMeeting(int type, List<String> list) {
        this.type = type;
        this.list = list;
    }
}
