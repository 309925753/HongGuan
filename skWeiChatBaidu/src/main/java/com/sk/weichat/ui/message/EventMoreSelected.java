package com.sk.weichat.ui.message;

/**
 * Created by Administrator on 2018/4/24 0024.
 */

public class EventMoreSelected {
    private final String toUserId;
    private boolean isSingleOrMerge;
    private boolean isGroupMsg;

    public EventMoreSelected(String toUserId, boolean isSingleOrMerge, boolean isGroupMsg) {
        this.toUserId = toUserId;
        this.isSingleOrMerge = isSingleOrMerge;
        this.isGroupMsg = isGroupMsg;
    }

    public String getToUserId() {
        return toUserId;
    }

    public boolean isSingleOrMerge() {
        return isSingleOrMerge;
    }

    public boolean isGroupMsg() {
        return isGroupMsg;
    }
}
