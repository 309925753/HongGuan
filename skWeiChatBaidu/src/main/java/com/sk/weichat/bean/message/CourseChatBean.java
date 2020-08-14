package com.sk.weichat.bean.message;

public class CourseChatBean {

    /**
     * courseId : 59e969874eac3a1c10653a75
     * courseMessageId : 59e969874eac3a1c10653a76
     * message :
     * userId : 10009249
     */

    private String courseId;
    private String courseMessageId;
    private String message;
    private int userId;

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseMessageId() {
        return courseMessageId;
    }

    public void setCourseMessageId(String courseMessageId) {
        this.courseMessageId = courseMessageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
