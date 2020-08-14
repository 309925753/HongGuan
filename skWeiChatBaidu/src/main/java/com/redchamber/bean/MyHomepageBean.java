package com.redchamber.bean;

import java.util.List;

public class MyHomepageBean {
    public String ageConstellation;
    public String nickname;
    public PhotoAlbum photoAlbum;
    public String position;
    public String userId;
    public String userLevel;
    public String inviteCode;
    public String expectFriend;
    public String program;
    public String residentCity;
    public String weight;
    public String height;
    public long birthday;
    public String description;

    public static class PhotoAlbum {
        public int coin;
        public int photoNum;
        public List<PhotoBean> photos;
        public int type;
        public String userId;
    }
}
