package com.sk.weichat.bean.redpacket;

/**
 * Created by Administrator on 2016/9/23.
 */
public class RedListItemSend {

    /**
     * resultCode : 1
     * data : [{"id":"57e4884cf9143ee31ee1f9ce","over":27.28,"count":3,"status":1,"userId":10005921,"receiveCount":1,"greetings":"110","userIds":[10005921],"sendTime":1474594892,"money":33,"userName":"in旅行","outTime":1474681292,"type":3},{"id":"57e3de0ff9143ee31ee1f922","over":60,"count":1,"status":1,"userId":10005921,"receiveCount":0,"greetings":"qq","userIds":[],"sendTime":1474551311,"money":60,"userName":"in旅行","outTime":1474637711,"type":3},{"id":"57e3dd56f9143ee31ee1f8f0","over":313.19,"count":3,"status":1,"userId":10005921,"receiveCount":1,"greetings":"136","userIds":[10005921],"sendTime":1474551126,"money":500,"userName":"in旅行","outTime":1474637526,"type":3},{"id":"57e3b78bf9143ee31ee1f884","over":1.5,"count":2,"status":1,"userId":10005921,"receiveCount":1,"greetings":"噢噢噢","userIds":[10005948],"sendTime":1474541451,"money":3,"userName":"in旅行","outTime":1474627851,"type":1},{"id":"57e3b484f9143ee31ee1f875","over":194.17,"count":3,"status":1,"userId":10005921,"receiveCount":2,"greetings":"恭喜发财","userIds":[10005921,10005948],"sendTime":1474540676,"money":500,"userName":"in旅行","outTime":1474627076,"type":2},{"id":"57e3b267f9143ee31ee1f868","over":1.1102230246251565E-16,"count":2,"status":1,"userId":10005921,"receiveCount":2,"greetings":"112","userIds":[10005921,10005948],"sendTime":1474540135,"money":3,"userName":"in旅行","outTime":1474626535,"type":3},{"id":"57e39f39f9143ee31ee1f81a","over":7.920000000000002,"count":3,"status":1,"userId":10005921,"receiveCount":2,"greetings":"111","userIds":[10005948,10005921],"sendTime":1474535225,"money":26,"userName":"in旅行","outTime":1474621625,"type":3},{"id":"57e39e38f9143ee31ee1f80c","over":1,"count":2,"status":1,"userId":10005921,"receiveCount":1,"greetings":"11","userIds":[10005948],"sendTime":1474534968,"money":2,"userName":"in旅行","outTime":1474621368,"type":1},{"id":"57e39de8f9143ee31ee1f802","over":22.919999999999998,"count":6,"status":1,"userId":10005921,"receiveCount":2,"greetings":"110","userIds":[10005948,10005921],"sendTime":1474534888,"money":26,"userName":"in旅行","outTime":1474621288,"type":3},{"id":"57e39bc8f9143ee31ee1f7f2","over":3,"count":3,"status":1,"userId":10005921,"receiveCount":0,"greetings":"普通红包","userIds":[],"sendTime":1474534344,"money":3,"userName":"in旅行","outTime":1474620744,"type":1}]
     */


    /**
     * id : 57e4884cf9143ee31ee1f9ce
     * over : 27.28
     * count : 3
     * status : 1
     * userId : 10005921
     * receiveCount : 1
     * greetings : 110
     * userIds : [10005921]
     * sendTime : 1474594892
     * money : 33
     * userName : in旅行
     * outTime : 1474681292
     * type : 3
     */
    private String id;
    private double over;
    private int count;
    private int status;
    private String userId;
    private int receiveCount;
    private String greetings;
    private int sendTime;
    private double money;
    private String userName;
    private int outTime;
    private int type;

    public void setId(String id) {
        this.id = id;
    }

    public void setOver(double over) {
        this.over = over;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setReceiveCount(int receiveCount) {
        this.receiveCount = receiveCount;
    }

    public void setGreetings(String greetings) {
        this.greetings = greetings;
    }


    public void setSendTime(int sendTime) {
        this.sendTime = sendTime;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setOutTime(int outTime) {
        this.outTime = outTime;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public double getOver() {
        return over;
    }

    public int getCount() {
        return count;
    }

    public int getStatus() {
        return status;
    }

    public String getUserId() {
        return userId;
    }

    public int getReceiveCount() {
        return receiveCount;
    }

    public String getGreetings() {
        return greetings;
    }


    public int getSendTime() {
        return sendTime;
    }

    public double getMoney() {
        return money;
    }

    public String getUserName() {
        return userName;
    }

    public int getOutTime() {
        return outTime;
    }

    public int getType() {
        return type;
    }

}
