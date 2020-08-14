package com.sk.weichat.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 手机联系人 in 服务端
 */
@DatabaseTable
public class Contact {

    /**
     * id : 5b7a65fa4adfdc2c076820eb
     * registerEd : 1
     * status : 0
     * registerTime : 1506677000
     * telephone : 8618720966659
     * toRemarkName : 李子
     * toTelephone : 8613177917179
     * toUserId : 10009899
     * toUserName : 李无为
     */

    @DatabaseField(generatedId = true)
    private int _id;

    @DatabaseField
    private String id;
    @DatabaseField
    private int registerEd;
    @DatabaseField
    private int registerTime;
    @DatabaseField
    private String telephone;
    @DatabaseField
    private String toTelephone;
    @DatabaseField
    private String toUserId;
    @DatabaseField
    private String toUserName;

    @DatabaseField
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRegisterEd() {
        return registerEd;
    }

    public void setRegisterEd(int registerEd) {
        this.registerEd = registerEd;
    }

    public int getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(int registerTime) {
        this.registerTime = registerTime;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getToTelephone() {
        return toTelephone;
    }

    public void setToTelephone(String toTelephone) {
        this.toTelephone = toTelephone;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    private int checkStatus = 100;// 默认为选中

    public int getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(int checkStatus) {
        this.checkStatus = checkStatus;
    }

    // 0 非好友关系(服务端未开启自动成为好友操作，且之前也不是好友关系)
    // 1 服务端自动加为好友(本地也需要将status为1的联系人加为好友)
    // 2 之前已经是好友关系
    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private String toRemarkName;

    // 联系人在本地通讯录内的名字
    public String getToRemarkName() {
        return toRemarkName;
    }

    public void setToRemarkName(String toRemarkName) {
        this.toRemarkName = toRemarkName;
    }
}
