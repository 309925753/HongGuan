package com.sk.weichat.bean.circle;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 商务圈消息本地Id缓存
 */
@DatabaseTable
public class CircleMessage {

    // objectId,fromUserId(转发人或创建人),timeSend(发布时间),content(转发理由,为空则不是转发)
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String msgId;

    @DatabaseField(canBeNull = false)
    private String ownerId;

    @DatabaseField(canBeNull = false)
    private String userId;// 转发人或创建人

    @DatabaseField
    private long time;    // 发布时间

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

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
