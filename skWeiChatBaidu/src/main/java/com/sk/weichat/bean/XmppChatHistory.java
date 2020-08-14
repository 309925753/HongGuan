package com.sk.weichat.bean;

public class XmppChatHistory {
    private String roomJid;
    private int time;

    public String getRoomJid() {
        return roomJid;
    }

    public void setRoomJid(String roomJid) {
        this.roomJid = roomJid;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "XmppChatHistory{" +
                "roomJid='" + roomJid + '\'' +
                ", time=" + time +
                '}';
    }
}
