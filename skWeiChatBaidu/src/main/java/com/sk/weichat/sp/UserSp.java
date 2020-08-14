package com.sk.weichat.sp;

import android.content.Context;
import android.content.SharedPreferences;

import com.sk.weichat.Reporter;
import com.sk.weichat.bean.LoginAuto;
import com.sk.weichat.bean.LoginRegisterResult;

/**
 * 保存当前登录用户的一些简单快捷使用的基本信息 userId ,userName,userPassword进入了MainActivity一定是正常的，可以随意取
 */
public class UserSp extends CommonSp {
    private static final String SP_NAME = "login_user_info";// FILE_NAME
    /* known key */
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_LOGGED = "logged";// 用于判断退出登录的情况，退出后为默认false,
    private static final String KEY_UPDATE = "update";// 本地资料有没有更新，防止切换手机出现数据断层
    private static final String KEY_LOGIN_TOKEN = "LOGIN_TOKEN";
    private static final String KEY_LOGIN_KEY = "LOGIN_KEY";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String KEY_HTTP_KEY = "HTTP_KEY";
    private static final String KEY_MESSAGE_KEY = "MESSAGE_KEY";
    private static final String KEY_PAY_KEY = "PAY_KEY";
    private static final String KEY_QR_KEY = "QR_KEY";
    private static UserSp instance;

    private UserSp(Context context) {
        super(context, SP_NAME);
    }

    public static final UserSp getInstance(Context context) {
        if (instance == null) {
            synchronized (UserSp.class) {
                if (instance == null) {
                    instance = new UserSp(context);
                }
            }
        }
        return instance;
    }

    public String getLoginToken() {
        return getValue(KEY_LOGIN_TOKEN, (String) null);
    }

    public String getPayKey() {
        return getValue(KEY_PAY_KEY, (String) null);
    }

    public String getLoginKey() {
        return getValue(KEY_LOGIN_KEY, (String) null);
    }

    public String getAccessToken() {
        return getValue(KEY_ACCESS_TOKEN, (String) null);
    }

    public void setAccessToken(String value) {
        setValue(KEY_ACCESS_TOKEN, value);
    }

    public String getHttpKey() {
        return getValue(KEY_HTTP_KEY, (String) null);
    }

    public String getMessageKey() {
        return getValue(KEY_MESSAGE_KEY, (String) null);
    }

    // access_token
    public String getAccessToken(String defaultValue) {
        return getValue(KEY_ACCESS_TOKEN, defaultValue);
    }

    // user_id
    public String getUserId(String defaultValue) {
        return getValue(KEY_USER_ID, defaultValue);
    }

    public void setUserId(String value) {
        setValue(KEY_USER_ID, value);
    }

    public String getQrKey() {
        return getValue(KEY_QR_KEY, (String) null);
    }

    public void setQrKey(String value) {
        setValue(KEY_QR_KEY, value);
    }

    public boolean isLogged() {
        return getValue(KEY_LOGGED, false);
    }

    public void setLogged(boolean value) {
        setValue(KEY_LOGGED, value);
    }

    // update
    public boolean isUpdate(boolean defaultValue) {
        return getValue(KEY_UPDATE, defaultValue);
    }

    public void setUpdate(boolean value) {
        setValue(KEY_UPDATE, value);
    }

    public void saveLoginResult(LoginRegisterResult result) {
        setValue(KEY_LOGIN_TOKEN, result.getLoginToken());
        setValue(KEY_LOGIN_KEY, result.getLoginKey());
        saveAutoLoginResult(result);
    }

    public void saveAutoLoginResult(LoginAuto result) {
        setValue(KEY_ACCESS_TOKEN, result.getAccessToken());
        setValue(KEY_HTTP_KEY, result.getHttpKey());
        setValue(KEY_MESSAGE_KEY, result.getMessageKey());
        setValue(KEY_PAY_KEY, result.getPayKey());
        Reporter.putUserData("accessToken", result.getAccessToken());
    }

    /* 注销登录时，将其他数据清空，只保留UserId这一个 */
    public void clearUserInfo() {
        SharedPreferences sp = getSharePreference();
        SharedPreferences.Editor edit = sp.edit();
        for (String key : sp.getAll().keySet()) {
            if (key.equals(KEY_USER_ID)) {
                continue;
            }
            edit.remove(key);
        }
        edit.apply();
    }
}
