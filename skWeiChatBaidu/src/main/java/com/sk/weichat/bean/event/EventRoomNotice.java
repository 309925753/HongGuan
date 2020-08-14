package com.sk.weichat.bean.event;

public class EventRoomNotice {
    private String text;

    public EventRoomNotice(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
