package com.sk.weichat.bean;

public class UserRandomStr {
    private String userRandomStr;

    private int isSupportSecureChat;// 是否有dh公钥，忘记密码内调用接口返回的，直接写在这里，就不创建一个新类了

    public String getUserRandomStr() {
        return userRandomStr;
    }

    public void setUserRandomStr(String userRandomStr) {
        this.userRandomStr = userRandomStr;
    }

    public int getIsSupportSecureChat() {
        return isSupportSecureChat;
    }

    public void setIsSupportSecureChat(int isSupportSecureChat) {
        this.isSupportSecureChat = isSupportSecureChat;
    }
}
