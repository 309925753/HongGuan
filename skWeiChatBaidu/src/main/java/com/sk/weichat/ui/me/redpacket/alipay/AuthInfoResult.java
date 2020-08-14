package com.sk.weichat.ui.me.redpacket.alipay;

public class AuthInfoResult {
    private String authInfo;
    private String aliUserId;


    // Getter Methods

    public String getAuthInfo() {
        return authInfo;
    }

    public String getAliUserId() {
        return aliUserId;
    }

    // Setter Methods

    public void setAuthInfo(String authInfo) {
        this.authInfo = authInfo;
    }

    public void setAliUserId(String aliUserId) {
        this.aliUserId = aliUserId;
    }
}
