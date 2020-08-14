package com.sk.weichat.bean.redpacket;

import java.io.Serializable;

/**
 * Created by wzw on 2016/9/23.
 */
public class RedListItemRecive implements Serializable {
    /**
     * id : 57e48852f9143ee31ee1f9d1
     * time : 1474594898
     * redId : 57e4884cf9143ee31ee1f9ce
     * userId : 10005921
     * money : 5.72
     * userName : in旅行
     */
    private String id;
    private int time;
    private String redId;
    private String userId;
    private double money;
    private String userName;
    private String sendName;
    private String sendId;

    public String getSendName() {
        return sendName;
    }

    public void setSendName(String sendName) {
        this.sendName = sendName;
    }

    public String getSendId() {
        return sendId;
    }

    public void setSendId(String sendId) {
        this.sendId = sendId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setRedId(String redId) {
        this.redId = redId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getId() {
        return id;
    }

    public int getTime() {
        return time;
    }

    public String getRedId() {
        return redId;
    }

    public String getUserId() {
        return userId;
    }

    public double getMoney() {
        return money;
    }

    public String getUserName() {
        return userName;
    }

}
