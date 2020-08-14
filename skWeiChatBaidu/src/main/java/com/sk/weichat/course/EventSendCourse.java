package com.sk.weichat.course;

public class EventSendCourse {
    private String toUserId;
    private boolean isGroup;

    public EventSendCourse(String toUserId, boolean isGroup) {
        this.toUserId = toUserId;
        this.isGroup = isGroup;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }
}
