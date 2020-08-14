package com.redchamber.bean;

import java.util.List;

public class PacketBean {

    /**
     * count : 1
     * greetings : 小小意思，拿去浪吧
     * id : 5ecb3a8a1f932c0c43e548f4
     * money : 1
     * outTime : 1590463498
     * over : 1
     * receiveCount : 0
     * sendTime : 1590377098
     * status : 1
     * toUserId : 10000034
     * type : 1
     * userId : 10000041
     * userIds : []
     * userName : 无心法师
     */

    private int count;
    private String greetings;
    private String id;
    private int money;
    private int outTime;
    private int over;
    private int receiveCount;
    private int sendTime;
    private int status;
    private int toUserId;
    private int type;
    private int userId;
    private String userName;
    private List<?> userIds;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getOutTime() {
        return outTime;
    }

    public void setOutTime(int outTime) {
        this.outTime = outTime;
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

    public int getSendTime() {
        return sendTime;
    }

    public void setSendTime(int sendTime) {
        this.sendTime = sendTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<?> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<?> userIds) {
        this.userIds = userIds;
    }
}
