package com.sk.weichat.bean.redpacket;

public class RedDialogBean {
    private String userId;
    private String userName;
    private String words;
    private String redId;

    public RedDialogBean(String userId, String userName, String words, String redId) {
        this.userId = userId;
        this.userName = userName;
        this.words = words;
        this.redId = redId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public String getRedId() {
        return redId;
    }

    public void setRedId(String redId) {
        this.redId = redId;
    }
}
