package com.sk.weichat.bean.redpacket;

public class ScanRecharge {
    /**
     * bankCard :
     * bankName :
     * id : 5de5e2c757dddb20f86cf048
     * name : 小敏
     * payNo : 18871716943
     * type : 2
     * url : http://test.shiku.co:8089/u/0/0/201912/o/688958B7D706E0A2D206CD9B1A5060E2.png
     */

    private String bankCard;
    private String bankName;
    private String id;
    private String name;
    private String payNo;
    private int type;
    private String url;

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPayNo() {
        return payNo;
    }

    public void setPayNo(String payNo) {
        this.payNo = payNo;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}