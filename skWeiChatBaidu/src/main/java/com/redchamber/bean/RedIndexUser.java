package com.redchamber.bean;

import java.io.Serializable;

public class RedIndexUser implements Serializable {

    //ageConstellation = "34;金牛座";
    //    albumType = 0;
    //    cityId = 310100;
    //    cityName = "上海市";
    //    createTime = 1587719722;
    //    distance = 16;
    //    nickname = yy;
    //    onlineStatus = "离线";
    //    photoAlbum =     {
    //        coin = 0;
    //        photos =         (
    //        );
    //        type = 0;
    //        userId = 10000006;
    //    };
    //    photoCount = 0;
    //    photoNum = 0;
    //    userId = 10000006;
    //    userLevel = 01111;

    public String ageConstellation;
    public String albumType;
    public String cityId;
    public String cityName;
    public String createTime;
    public String distance;
    public String nickname;
    public String onlineStatus;
    public int photoNum;
    public String position;
    /**
     * 0女 1男
     * 全部为五位，男士没有男神认证，默认第三位为0
     * 如男士即开通了VIP又进行了真人认证则为 11010
     * 女士开通VIP，进行了女神和真人认证则为 01110
     * <p>
     * 用户级别 第一位为性别、第二位为是否VIP认证、第三位为是否女神男神认证、第四位为是否真人认证、第五位为是否有徽章
     */
    public String userLevel;
    public String userId;
    public int collectStatus;//收藏状态 0未收藏 1已收藏
    public int showBadge;//默认显示徽章
    public PhotoAlbum photoAlbum;

    public class PhotoAlbum implements Serializable {
        public String userId;
        public String userLevel;
        public int type;
    }

}
