package com.redchamber.bean;

import java.io.Serializable;

/**
 * 相册
 */
public class PhotoBean implements Serializable {

    /**
     * 解锁所需要红豆数
     */
    public int coin;
    public String photoId;
    public String photoUrl;
    /**
     * 照片类型 0照片 1视频
     */
    public int type;
    /**
     * 访问方式 0正常 1阅后即焚 2红包
     */
    public int visitType;
    /**
     * 阅后即焚状态  1:已焚毁
     */
    public int status;
    /**
     * 是否本人  1:本人
     */
    public int isSelf;
    public boolean isSelect;


    public PhotoBean(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public PhotoBean() {
    }

}
