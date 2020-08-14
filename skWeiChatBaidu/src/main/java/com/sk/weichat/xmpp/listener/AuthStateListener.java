package com.sk.weichat.xmpp.listener;

public interface AuthStateListener {
    int AUTH_STATE_NOT = 1;     // 未登录
    int AUTH_STATE_ING = 2;     // 登录中
    int AUTH_STATE_SUCCESS = 3; // 已经认证
    // XMPP的状态的回调
    void onAuthStateChange(int authState);
}
