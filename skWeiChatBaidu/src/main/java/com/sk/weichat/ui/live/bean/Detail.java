package com.sk.weichat.ui.live.bean;

/**
 * Created by Administrator on 2017/7/19 0019.
 */

// 直播间详情
public class Detail {
    /**
     * createTime : 1500456265
     * jid : a4d6429db804407d82080b69273a5f63
     * name : asf
     * nickName : 执迷不悟
     * numbers : -3
     * roomId : 596f25494eac3a068452e358
     * url : rtmp://live.hkstv.hk.lxdns.com:1935/live/10008295_1500456265
     * userId : 10008295
     */
    private int createTime;
    private String jid;
    private String name;
    private String nickName;
    private int numbers;
    private String roomId;
    private String url;
    private int userId;

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
}
