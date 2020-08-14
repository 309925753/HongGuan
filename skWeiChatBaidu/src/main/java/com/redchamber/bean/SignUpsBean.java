package com.redchamber.bean;

import java.io.Serializable;

public class SignUpsBean implements Serializable {
    /**
     * joinImage : http://111.jpg
     * joinTime : 1588241264833
     * nickName : 测试用户
     * userId : 10000007
     */

    private String joinImage;
    private long joinTime;
    private String nickName;
    private int userId;

    public String getJoinImage() {
        return joinImage;
    }

    public void setJoinImage(String joinImage) {
        this.joinImage = joinImage;
    }

    public long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
