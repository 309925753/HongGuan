package com.sk.weichat.bean;

/**
 * Created by Administrator on 2017/6/28 0028.
 */

public class SetManager {
    private int role;
    private int createTime;
    private String userId;
    private String nickName;

    public SetManager() {
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
