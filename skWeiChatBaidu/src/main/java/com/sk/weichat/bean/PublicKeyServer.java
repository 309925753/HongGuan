package com.sk.weichat.bean;

import java.util.List;

public class PublicKeyServer {
    private String userId;

    private List<PublicKeyList> publicKeyList;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<PublicKeyList> getPublicKeyList() {
        return publicKeyList;
    }

    public void setPublicKeyList(List<PublicKeyList> publicKeyList) {
        this.publicKeyList = publicKeyList;
    }

    public class PublicKeyList {
        private String key;
        private long time;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
    }
}
