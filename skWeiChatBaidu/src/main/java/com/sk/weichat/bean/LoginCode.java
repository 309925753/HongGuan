package com.sk.weichat.bean;

/**
 * 支付加固的临时密码，
 */
public class LoginCode {

    private String code;
    private String userId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
