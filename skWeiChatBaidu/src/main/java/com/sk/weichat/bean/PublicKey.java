package com.sk.weichat.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class PublicKey {
    @DatabaseField(generatedId = true)
    private int _id;
    @DatabaseField(canBeNull = false)
    private String ownerId;// 表拥有者
    @DatabaseField(canBeNull = false)
    private String userId;// 好友id
    @DatabaseField
    private String publicKey;// 好友公钥
    @DatabaseField
    private long keyCreateTime;// 好友公钥创建时间

    public PublicKey() {
    }

    public PublicKey(String ownerId, String userId, String publicKey, long keyCreateTime) {
        this.ownerId = ownerId;
        this.userId = userId;
        this.publicKey = publicKey;
        this.keyCreateTime = keyCreateTime;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public long getKeyCreateTime() {
        return keyCreateTime;
    }

    public void setKeyCreateTime(long keyCreateTime) {
        this.keyCreateTime = keyCreateTime;
    }
}
