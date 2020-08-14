package com.sk.weichat.bean.circle;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class Gift implements Serializable {
    private static final long serialVersionUID = -2765871913015265431L;
    private String userId;
    @JSONField(name = "nickname")
    private String nickName;
    private int id;
    private int price;
    private int count;
    private long time;
    private String giftId;//礼物的id

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickname) {
        this.nickName = nickname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getGiftId() {
        return giftId;
    }

    public void setGiftId(String giftId) {
        this.giftId = giftId;
    }
}
