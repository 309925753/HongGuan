package com.redchamber.bean;

import com.google.gson.annotations.SerializedName;

/**
 * 消息类型
 */
public class SysteMessageBean {

    /**
     * alreadyRead : true
     * messageId : 5eba6ba332f5386d17d74c12
     * messageTime : 1589275555954
     * messageType : 2
     * nickName : 路上
     * oauthId : 5eba6ba332f5386d17d74c11
     * oauthUserId : 10000034
     * userId : 10000007
     */

    private boolean alreadyRead;
    private String messageId;
    private long messageTime;
    private int messageType;
    private String nickName;
    private String oauthId;
    private int oauthUserId;
    /**
     * comment : 5
     * comments : 高冷
     * commentsId : 5ebe67dbf8e08d57ac5ac02c
     * commentsTime : 1589536731503
     * otherName : 路线了解
     * othersId : 10000034
     * userId : 10000054
     */

    private int comment;
    private String comments;
    private String commentsId;
    private long commentsTime;
    private String otherName;
    private int othersId;
    /**
     * applyTime : 1589882081433
     * applyUserId : 10000034
     * auditTime : 1589942452725
     * flag : 1
     * imageUrl : http://file57.quyangapp.com/u/34/10000034/202005/o/99663f14813d46c8996b31b002131120.jpg
     * nickname : 无心法师
     * userId : 10000041
     */

    private long applyTime;
    private int applyUserId;
    private long auditTime;
    private int flag;
    private String imageUrl;
    private String nickname;

    public int getNeType() {
        return neType;
    }

    public void setNeType(int neType) {
        this.neType = neType;
    }

    private int neType;


    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }

    private long time;
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    private String msg;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    /**
     * time : 1588727469928
     * message : 测试用户点赞了你的动态
     * type : dianzan
     */

    private String message;
    private String type;
    /**
     * content : 报名了你的节目(08月23号黄焖鸡米饭),快去看看吧
     * id : 5eaaa389fe259804603350f8
     * joinNickName : 测试用户
     * joinTime : 1588241264833
     * joinUserId : 10000007
     * programId : 5eaaa335fe259804603350f7
     * type : 2
     * userId : 10000007
     */

    private String content;
    private String id;
    private String joinNickName;

    public long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Long joinTime) {
        this.joinTime = joinTime;
    }

    private long joinTime;
    private int joinUserId;
    private String programId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String userId;
    public int getNotReady() {
        return notReady;
    }

    public void setNotReady(int notReady) {
        this.notReady = notReady;
    }

    private int notReady;



    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJoinNickName() {
        return joinNickName;
    }

    public void setJoinNickName(String joinNickName) {
        this.joinNickName = joinNickName;
    }



    public int getJoinUserId() {
        return joinUserId;
    }

    public void setJoinUserId(int joinUserId) {
        this.joinUserId = joinUserId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }


    public boolean isAlreadyRead() {
        return alreadyRead;
    }

    public void setAlreadyRead(boolean alreadyRead) {
        this.alreadyRead = alreadyRead;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getOauthId() {
        return oauthId;
    }

    public void setOauthId(String oauthId) {
        this.oauthId = oauthId;
    }

    public int getOauthUserId() {
        return oauthUserId;
    }

    public void setOauthUserId(int oauthUserId) {
        this.oauthUserId = oauthUserId;
    }

    public int getComment() {
        return comment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCommentsId() {
        return commentsId;
    }

    public void setCommentsId(String commentsId) {
        this.commentsId = commentsId;
    }

    public long getCommentsTime() {
        return commentsTime;
    }

    public void setCommentsTime(long commentsTime) {
        this.commentsTime = commentsTime;
    }

    public String getOtherName() {
        return otherName;
    }

    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }

    public int getOthersId() {
        return othersId;
    }

    public void setOthersId(int othersId) {
        this.othersId = othersId;
    }

    public long getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(long applyTime) {
        this.applyTime = applyTime;
    }

    public int getApplyUserId() {
        return applyUserId;
    }

    public void setApplyUserId(int applyUserId) {
        this.applyUserId = applyUserId;
    }

    public long getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(long auditTime) {
        this.auditTime = auditTime;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }


}
