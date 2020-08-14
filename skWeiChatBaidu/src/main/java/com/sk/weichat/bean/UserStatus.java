package com.sk.weichat.bean;

/**
 * 用户登录状态，
 *
 * @deprecated 不再需要各个接口分别提供access_token了，这个userStatus也不怎么必要了，非要使用时用UserSp代替，
 */
@Deprecated
public class UserStatus {
    /**
     * @deprecated 不再需要各个接口分别提供access_token了，这个userStatus也不怎么必要了，非要使用时用UserSp代替，
     */
    @Deprecated
    public String accessToken;

    public UserStatus(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        return "UserStatus{" +
                "accessToken='" + accessToken + '\'' +
                '}';
    }
}
