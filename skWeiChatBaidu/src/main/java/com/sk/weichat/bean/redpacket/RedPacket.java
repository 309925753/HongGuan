package com.sk.weichat.bean.redpacket;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * Created by 魏正旺 on 2016/9/19.
 */
public class RedPacket implements Serializable {
    @JSONField(name = "count")
    private int count;

    @JSONField(name = "userId")
    private String userId;

    @JSONField(name = "money")
    private double money;

    @JSONField(name = "greetings")
    private String greetings;

    @JSONField(name = "id")
    private String id;

    @JSONField(name = "outTime")
    private String outTime;

    @JSONField(name = "sendTime")
    private String sendTime;

    @JSONField(name = "over")
    private int over;

    @JSONField(name = "receiveCount")
    private int receiveCount;

    @JSONField(name = "status")
    private int status;

    @JSONField(name = "type")
    private int type;

    @JSONField(name = "userIds")
    private String userIds;

    @JSONField(name = "userName")
    private String userName;


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public String getGreetings() {
        return greetings;
    }

    public void setGreetings(String greetings) {
        this.greetings = greetings;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public int getOver() {
        return over;
    }

    public void setOver(int over) {
        this.over = over;
    }

    public int getReceiveCount() {
        return receiveCount;
    }

    public void setReceiveCount(int receiveCount) {
        this.receiveCount = receiveCount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserIds() {
        return userIds;
    }

    public void setUserIds(String userIds) {
        this.userIds = userIds;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}

