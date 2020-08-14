package com.sk.weichat.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 用户表用户头像刷新
 */
@DatabaseTable(tableName = "user_avatar")
public class UserAvatar {

    @DatabaseField(id = true)
    private String userId;// 用户id

    @DatabaseField
    private long time;    // 头像更新时间


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
