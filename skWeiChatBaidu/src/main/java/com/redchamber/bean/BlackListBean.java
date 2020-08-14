package com.redchamber.bean;

import java.util.List;

public class BlackListBean {

    public int pageCount;
    public int pageIndex;
    public int pageSize;
    public int start;
    public int total;
    public List<BlackUser> pageData;

    public static class BlackUser {
        public String blacklistId;
        public String friendId;
        public String nickname;
        public String userId;
    }

}
