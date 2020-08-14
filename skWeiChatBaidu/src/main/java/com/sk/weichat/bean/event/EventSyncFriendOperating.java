package com.sk.weichat.bean.event;

public class EventSyncFriendOperating {
    private String toUserId;
    private int type;

    public EventSyncFriendOperating(String toUserId, int type) {
        this.toUserId = toUserId;
        this.type = type;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
