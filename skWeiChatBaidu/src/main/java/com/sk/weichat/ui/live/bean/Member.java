package com.sk.weichat.ui.live.bean;

/**
 * Created by Administrator on 2017/7/19 0019.
 */

// 直播间成员
public class Member {
    public static final int TYPE_OWNER = 1;
    public static final int TYPE_MANAGER = 2;
    public static final int TYPE_MEMBER = 3;
    /**
     * createTime : 1501043103
     * id : 5978199f7760c91b3ec7cd91
     * nickName : MC子龙
     * number : 0
     * online : 1
     * roomId : 5977ffdc7760c92961655f34
     * state : 0
     * type : 3
     * userId : 10009312
     */
    private long createTime;
    private String id;
    private String nickName;
    private int number;
    private int online;
    private String roomId;
    private int state;
    private int type;
    private String userId;
    private long talkTime;

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTalkTime() {
        return talkTime;
    }

    public void setTalkTime(long talkTime) {
        this.talkTime = talkTime;
    }
}
