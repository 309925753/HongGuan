package com.sk.weichat.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 群组分页漫游
 */
@DatabaseTable
public class MsgRoamTask {

    @DatabaseField(generatedId = true)
    private int _id;

    @DatabaseField
    private long taskId;

    @DatabaseField
    private String ownerId;// 任务拥有者

    @DatabaseField
    private String userId;// 当前任务属于哪个群组 jid

    @DatabaseField
    private long startTime;// 漫游开始时间

    @DatabaseField
    private String startMsgId;// 起始msgId

    @DatabaseField(defaultValue = "0")
    private long endTime;// 漫游结束时间

    @DatabaseField(defaultValue = "0")
    private int isFinish;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getStartMsgId() {
        return startMsgId;
    }

    public void setStartMsgId(String startMsgId) {
        this.startMsgId = startMsgId;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getIsFinish() {
        return isFinish;
    }

    public void setIsFinish(int isFinish) {
        this.isFinish = isFinish;
    }

    @Override
    public String toString() {
        return "MsgRoamTask{" +
                "_id=" + _id +
                ", taskId=" + taskId +
                ", ownerId='" + ownerId + '\'' +
                ", userId='" + userId + '\'' +
                ", startTime=" + startTime +
                ", startMsgId='" + startMsgId + '\'' +
                ", endTime=" + endTime +
                ", isFinish=" + isFinish +
                '}';
    }
}
