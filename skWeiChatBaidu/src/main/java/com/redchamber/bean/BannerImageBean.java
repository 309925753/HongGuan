package com.redchamber.bean;

public class BannerImageBean {


    /**
     * picture : www.123.png
     * plantingWheelId : 5eb3e97f2b01670cd8b2f9dd
     * position : 首页
     * sort : 6
     * state : 1
     */

    private String picture;
    private String plantingWheelId;
    private String position;
    private int sort;
    private int state;

    public String getJumpUrl() {
        return jumpUrl;
    }

    public void setJumpUrl(String jumpUrl) {
        this.jumpUrl = jumpUrl;
    }

    private String jumpUrl;

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getPlantingWheelId() {
        return plantingWheelId;
    }

    public void setPlantingWheelId(String plantingWheelId) {
        this.plantingWheelId = plantingWheelId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
