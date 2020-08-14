package com.sk.weichat.ui.live.bean;

/**
 * Created by Administrator on 2017/7/24 0024.
 */

public class GiftItem {
    /**
     * name : 鲜花
     * photo : http://file.xxx.co/image/gift/yipitiezhi001.png
     * price : 100
     */
    private String giftUi;
    private String giftUn;
    private String name;
    private String photo;
    private int price;
    private int giftNum;

    public String getGiftUi() {
        return giftUi;
    }

    public void setGiftUi(String giftUi) {
        this.giftUi = giftUi;
    }

    public String getGiftUn() {
        return giftUn;
    }

    public void setGiftUn(String giftUn) {
        this.giftUn = giftUn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getGiftNum() {
        return giftNum;
    }

    public void setGiftNum(int giftNum) {
        this.giftNum = giftNum;
    }
}
