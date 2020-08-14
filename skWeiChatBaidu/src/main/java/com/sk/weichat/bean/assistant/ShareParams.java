package com.sk.weichat.bean.assistant;

public class ShareParams {
    private String userId;
    private String roomId;
    private String roomJid;

    public ShareParams(String userId, String roomId, String roomJid) {
        this.userId = userId;
        this.roomId = roomId;
        this.roomJid = roomJid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomJid() {
        return roomJid;
    }

    public void setRoomJid(String roomJid) {
        this.roomJid = roomJid;
    }
}
