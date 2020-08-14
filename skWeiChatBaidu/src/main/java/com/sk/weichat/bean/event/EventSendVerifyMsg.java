package com.sk.weichat.bean.event;

/**
 * 发送群验证消息
 */
public class EventSendVerifyMsg {
    private String createUserId;
    private String groupJid;
    private String reason;

    public EventSendVerifyMsg(String createUserId, String groupJid, String reason) {
        this.createUserId = createUserId;
        this.groupJid = groupJid;
        this.reason = reason;
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public String getGroupJid() {
        return groupJid;
    }

    public void setGroupJid(String groupJid) {
        this.groupJid = groupJid;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
