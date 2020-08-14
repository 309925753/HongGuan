package com.sk.weichat.bean.event;

/**
 * 文件上传进度通知
 */
public class EventUploadFileRate {
    private String packetId;
    private int rate;

    public EventUploadFileRate(String packetId, int rate) {
        this.packetId = packetId;
        this.rate = rate;
    }

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
}
