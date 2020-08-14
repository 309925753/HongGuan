package com.sk.weichat.bean;

public class QQLoginResult {
    private int ret;
    private String accessToken;
    private String msg;
    private String pfkey;
    private String payToken;
    private String openid;
    private String pf;
    private int queryAuthorityCost;
    private int expiresIn;
    private int loginCost;
    private long expiresTime;
    private int authorityCost;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getPfkey() {
        return pfkey;
    }

    public void setPfkey(String pfkey) {
        this.pfkey = pfkey;
    }

    public String getPayToken() {
        return payToken;
    }

    public void setPayToken(String payToken) {
        this.payToken = payToken;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getPf() {
        return pf;
    }

    public void setPf(String pf) {
        this.pf = pf;
    }

    public int getQueryAuthorityCost() {
        return queryAuthorityCost;
    }

    public void setQueryAuthorityCost(int queryAuthorityCost) {
        this.queryAuthorityCost = queryAuthorityCost;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public int getLoginCost() {
        return loginCost;
    }

    public void setLoginCost(int loginCost) {
        this.loginCost = loginCost;
    }

    public long getExpiresTime() {
        return expiresTime;
    }

    public void setExpiresTime(long expiresTime) {
        this.expiresTime = expiresTime;
    }

    public int getAuthorityCost() {
        return authorityCost;
    }

    public void setAuthorityCost(int authorityCost) {
        this.authorityCost = authorityCost;
    }

    @Override
    public String toString() {
        return
                "QQLoginResult{" +
                        "ret = '" + ret + '\'' +
                        ",access_token = '" + accessToken + '\'' +
                        ",msg = '" + msg + '\'' +
                        ",pfkey = '" + pfkey + '\'' +
                        ",pay_token = '" + payToken + '\'' +
                        ",openid = '" + openid + '\'' +
                        ",pf = '" + pf + '\'' +
                        ",query_authority_cost = '" + queryAuthorityCost + '\'' +
                        ",expires_in = '" + expiresIn + '\'' +
                        ",login_cost = '" + loginCost + '\'' +
                        ",expires_time = '" + expiresTime + '\'' +
                        ",authority_cost = '" + authorityCost + '\'' +
                        "}";
    }
}
