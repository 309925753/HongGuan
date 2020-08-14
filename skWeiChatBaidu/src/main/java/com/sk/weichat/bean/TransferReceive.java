package com.sk.weichat.bean;

public class TransferReceive {

    /**
     * id : 5c6e8ed28782c8326847a86c
     * money : 100.0
     * sendId : 10000050
     * sendName : 157
     * time : 1550749394
     * traId : 5c6e8ec18782c8326847a867
     * userId : 10000049
     * userName : 187
     */

    private String id;
    private double money;
    private int sendId;
    private String sendName;
    private int time;
    private String traId;
    private int userId;
    private String userName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public int getSendId() {
        return sendId;
    }

    public void setSendId(int sendId) {
        this.sendId = sendId;
    }

    public String getSendName() {
        return sendName;
    }

    public void setSendName(String sendName) {
        this.sendName = sendName;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getTraId() {
        return traId;
    }

    public void setTraId(String traId) {
        this.traId = traId;
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
}
