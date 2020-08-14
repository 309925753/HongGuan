package com.sk.weichat.bean.event;

public class EventPraiseUpdate {
    public String messageId;
    public boolean isParise;

    public EventPraiseUpdate(String messageId, boolean isParise) {
        this.messageId = messageId;
        this.isParise = isParise;
    }
}
