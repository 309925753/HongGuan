package com.sk.weichat.bean.message;

import java.util.List;

public class CourseBean {

    /**
     * courseId : 59e969874eac3a1c10653a75
     * courseName : 6666
     * createTime : 1508469136
     * messageIds : ["794e008ed69749c5b53230ef27f634b2","2f50907944104c94914ad3a86166f607","6bb75be4addb46e48a858a0046662413","8c396bfadee142e49ead822d11a5592c","d073bd8ffa574d98aaf630cb51c03f8e"]
     * roomJid : 0
     * userId : 10009249
     */

    private String courseId;
    private String courseName;
    private long createTime;
    private String roomJid;
    private int updateTime;
    private int userId;
    private List<String> messageIds;

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getRoomJid() {
        return roomJid;
    }

    public void setRoomJid(String roomJid) {
        this.roomJid = roomJid;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<String> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<String> messageIds) {
        this.messageIds = messageIds;
    }
}
