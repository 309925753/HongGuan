package com.sk.weichat.sp;

import android.content.Context;

/**
 * 根据UserId，保存用户的数据库某些的表的本地版本
 */
public class TableVersionSp extends CommonSp {
    private static final String SP_NAME = "table_version";// FILE_NAME
    private static TableVersionSp instance;

    /* known key */
    private static final String KEY_FRIEND_TABLE = "friend_";//朋友表的version +userId

    public static final TableVersionSp getInstance(Context context) {
        if (instance == null) {
            synchronized (TableVersionSp.class) {
                if (instance == null) {
                    instance = new TableVersionSp(context);
                }
            }
        }
        return instance;
    }

    private TableVersionSp(Context context) {
        super(context, SP_NAME);
    }

    // friend_
    public int getFriendTableVersion(String userId) {
        return getValue(KEY_FRIEND_TABLE + userId, 0);
    }

    public void setFriendTableVersion(String userId, int value) {
        setValue(KEY_FRIEND_TABLE + userId, value);
    }
}
