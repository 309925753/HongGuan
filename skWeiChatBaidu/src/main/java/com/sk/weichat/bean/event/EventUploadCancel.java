package com.sk.weichat.bean.event;

/**
 * 取消当前上传的文件、视频
 */
public class EventUploadCancel {
    private String packetId;

    public EventUploadCancel(String packetId) {
        this.packetId = packetId;
    }

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }
}
