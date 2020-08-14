package com.sk.weichat.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 本地视频文件的数据库信息
 */
@DatabaseTable
public class VideoFile {
    public VideoFile() {
    }

    @DatabaseField(generatedId = true)
    private int _id;

    @DatabaseField(canBeNull = false)
    private String ownerId; //文件所属者是哪个user

    @DatabaseField(canBeNull = false)
    private String filePath;//文件的本地Uri

    @DatabaseField
    private String desc;      // 文件的描述

    @DatabaseField(canBeNull = false)
    private String createTime;// 文件创建时间

    @DatabaseField
    private long fileSize;    // 文件大小

    @DatabaseField
    private long fileLength;  // 文件时长

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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }
}
