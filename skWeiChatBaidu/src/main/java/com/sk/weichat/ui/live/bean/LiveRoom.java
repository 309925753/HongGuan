package com.sk.weichat.ui.live.bean;

/**
 * Created by Administrator on 2017/7/19 0019.
 */

// 直播间列表
public class LiveRoom {
    /**
     * createTime : 1501156136
     * jid : 871b96802df44c5abc0820a6cbe54fe2
     * name : 国
     * nickName : 曹魏主播
     * notice : 进来的先交100块钱
     * numbers : 1
     * roomId : 5979d3287760c95dbbd4ec69
     * url : rtmp://v1.one-tv.com:1935/live/10009331_1501156136
     * userId : 10009331
     * status : 0
     */
    private int createTime;
    private String jid;
    private String name;
    private String nickName;
    private String notice;
    private int numbers;
    private String roomId;
    private String url;
    private int userId;
    private int currentState;// 0 正常 1 直播间已被锁定
    private int status;

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public int getNumbers() {
        return numbers;
    }

    public void setNumbers(int numbers) {
        this.numbers = numbers;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
