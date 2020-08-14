package com.sk.weichat.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 我的动态
 */
@DatabaseTable
public class MyZan {

    @DatabaseField(id = true)
    private String systemid;

    @DatabaseField
    private String sendtime;

    @DatabaseField
    private String fromUserId;

    @DatabaseField
    private String fromUsername;

    @DatabaseField
    private int type;

    @DatabaseField
    private String cricleuserid;

    @DatabaseField
    private String content;

    @DatabaseField
    private String contenturl;

    @DatabaseField
    private String loginUserId;

    public String getLoginUserId() {
        return loginUserId;
    }

    public void setLoginUserId(String loginUserId) {
        this.loginUserId = loginUserId;
    }

    public String getHuifu() {
        return huifu;
    }

    public void setHuifu(String huifu) {
        this.huifu = huifu;
    }

    @DatabaseField
    private String huifu;

    public int getZanbooleanyidu() {
        return zanbooleanyidu;
    }

    public void setZanbooleanyidu(int zanbooleanyidu) {
        this.zanbooleanyidu = zanbooleanyidu;
    }

    @DatabaseField
    private int zanbooleanyidu;

    public String getTousername() {
        return tousername;
    }

    public void setTousername(String tousername) {
        this.tousername = tousername;
    }

    @DatabaseField
    private String tousername;

    public String getSystemid() {
        return systemid;
    }

    public void setSystemid(String systemid) {
        this.systemid = systemid;
    }

    public String getSendtime() {
        return sendtime;
    }

    public void setSendtime(String sendtime) {
        this.sendtime = sendtime;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCricleuserid() {
        return cricleuserid;
    }

    public void setCricleuserid(String cricleuserid) {
        this.cricleuserid = cricleuserid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContenturl() {
        return contenturl;
    }

    @Override
    public String toString() {
        return "MyZan{" +
                "systemid='" + systemid + '\'' +
                ", sendtime='" + sendtime + '\'' +
                ", fromUserId='" + fromUserId + '\'' +
                ", fromUsername='" + fromUsername + '\'' +
                ", type=" + type +
                ", cricleuserid='" + cricleuserid + '\'' +
                ", content='" + content + '\'' +
                ", contenturl='" + contenturl + '\'' +
                ", huifu='" + huifu + '\'' +
                '}';
    }

    public void setContenturl(String contenturl) {
        this.contenturl = contenturl;
    }
}
