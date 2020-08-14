package com.sk.weichat.bean;

public class BindInfo {
    private int createTime;
    private String loginInfo;
    private String id;
    private int type;
    private int userId;

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public String getLoginInfo() {
        return loginInfo;
    }

    public void setLoginInfo(String loginInfo) {
        this.loginInfo = loginInfo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return
                "BindInfo{" +
                        "createTime = '" + createTime + '\'' +
                        ",loginInfo = '" + loginInfo + '\'' +
                        ",id = '" + id + '\'' +
                        ",type = '" + type + '\'' +
                        ",userId = '" + userId + '\'' +
                        "}";
    }
}
