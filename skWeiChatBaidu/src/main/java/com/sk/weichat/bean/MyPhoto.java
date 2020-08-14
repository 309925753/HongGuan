package com.sk.weichat.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 我的相册
 */
@DatabaseTable
public class MyPhoto {

    @DatabaseField(id = true)
    private String photoId;// 图片Id

    @DatabaseField
    private String ownerId;// 图片属于哪个用户的

    @DatabaseField
    @JSONField(name = "oFileName")
    private String originalFileName;//

    @DatabaseField
    private int status;

    @DatabaseField
    @JSONField(name = "tUrl")
    private String thumbUrl;

    @DatabaseField
    @JSONField(name = "oUrl")
    private String originalUrl;

    @DatabaseField
    private long createTime;

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
