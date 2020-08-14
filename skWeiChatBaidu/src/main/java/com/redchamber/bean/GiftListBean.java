package com.redchamber.bean;

public class GiftListBean {

    /**
     * giftId : 5eb4ca9c12d185313a6bb47e
     * name : 飞机
     * photo : https://daydayloan.oss-cn-shenzhen.aliyuncs.com/image/1.png
     * price : 100
     * status : 1
     * type : 1
     */

    private String giftId;
    private String name;
    private String photo;
    private int price;
    private int status;
    private int type;

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    private boolean isSelect;

    public String getGiftId() {
        return giftId;
    }

    public void setGiftId(String giftId) {
        this.giftId = giftId;
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
}
