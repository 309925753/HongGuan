package com.redchamber.bean;

import java.io.Serializable;

public class DiscussesBean  implements Serializable {
    /**
     * content : 我是一只小小小小鸟
     * joinTime : 1588727453660
     * nickName : 测试用户
     * userId : 10000007
     */

    private String content;
    private long joinTime;
    private String nickName;
    private int userId;

    public String getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }

    private String userLevel;//用户级别 第一位为性别、第二位为是否VIP认证、第三位为是否女神男神认证、第四位为是否真人认证、第五位为是否有徽章

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

