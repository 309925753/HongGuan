package com.sk.weichat.bean.collection;

import com.sk.weichat.bean.message.XmppMessage;

/**
 * Created by Administrator on 2017/10/20 0020.
 * 收藏
 */

public class CollectionEvery {

    // 各种type混乱，收藏的type, 朋友圈消息的type, xmpp消息的type, 数值都不一样，
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_FILE = 3;
    public static final int TYPE_VOICE = 4;
    public static final int TYPE_TEXT = 5;

    private long createTime;
    private String emojiId;
    private String msg;
    private String fileName;
    private int fileLength;
    private long fileSize;
    private String url;
    private String collectContent;
    private int type;
    private String userId;

    // type转成xmpp使用的那一套，
    // 项目里有几套type都不同，而且都是用int没有enum, 异常情况没考虑，直接抛异常，
    public int getXmppType() {
        int type;
        switch (getType()) {
            case CollectionEvery.TYPE_TEXT:
                type = XmppMessage.TYPE_TEXT;
                break;
            case CollectionEvery.TYPE_IMAGE:
                type = XmppMessage.TYPE_IMAGE;
                break;
            case CollectionEvery.TYPE_FILE:
                type = XmppMessage.TYPE_FILE;
                break;
            case CollectionEvery.TYPE_VIDEO:
                type = XmppMessage.TYPE_VIDEO;
                break;
            case CollectionEvery.TYPE_VOICE:
                type = XmppMessage.TYPE_VOICE;
                break;
            default:
                throw new IllegalStateException("类型<" + getType() + ">不存在，");
        }
        return type;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getEmojiId() {
        return emojiId;
    }

    public void setEmojiId(String emojiId) {
        this.emojiId = emojiId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileLength() {
        return fileLength;
    }

    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getCollectContent() {
        return collectContent;
    }

    public void setCollectContent(String collectContent) {
        this.collectContent = collectContent;
    }
}
