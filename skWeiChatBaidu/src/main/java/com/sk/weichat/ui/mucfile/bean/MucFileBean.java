package com.sk.weichat.ui.mucfile.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/4.
 */
public class MucFileBean implements Serializable {

    private String name;     // 文件名称*
    private String url;      // 文件地址*
    private String nickname; // 文件来自人名称
    private String userId;   // 文件来自人ID
    private String roomId;   // 房间id
    private String shareId;  // 文件id
    private long size; // 文件大小*
    private long time; // 文件时间
    private int state; // 状态*
    private int type;  // 类型*
    private long progress = 0;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getProgress() {
        return progress;
    }

    @Override
    public String toString() {
        return "MucFileBean{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", nickname='" + nickname + '\'' +
                ", userId='" + userId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", shareId='" + shareId + '\'' +
                ", size=" + size +
                ", time=" + time +
                ", state=" + state +
                ", type=" + type +
                ", progress=" + progress +
                '}';
    }
}
