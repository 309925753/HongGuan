package com.sk.weichat.bean.company;

import java.util.List;

/**
 * Created by Administrator on 2017/5/17 0017.
 * {"currentTime":1494991134,"data":{"companyName":"s东莞市","createTime":1494991134,"createUserId":100114,"deleteTime":0,"deleteUserId":0,"empNum":1,
 * "id":"591bc11ef6769eb6269784cb","noticeContent":"","noticeTime":0,"rootDpartId":["591bc11ef6769eb6269784cd"]},"resultCode":1}
 */

public class Companys {

    private String companyName;         // 公司名
    private long createTime;            // 创建时间
    private int createUserId;           // 创建者ID
    private long deleteTime;            // 删除时间
    private int deleteUserId;           // 删除ID
    private int empNum;                 // 公司人数
    private String id;                  // 公司ID
    private String noticeContent;       // 公告
    private long noticeTime;            // 发布公告时间
    private List<String> rootDpartId;   // 根部门ID

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(long deleteTime) {
        this.deleteTime = deleteTime;
    }

    public int getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(int createUserId) {
        this.createUserId = createUserId;
    }

    public int getDeleteUserId() {
        return deleteUserId;
    }

    public void setDeleteUserId(int deleteUserId) {
        this.deleteUserId = deleteUserId;
    }

    public int getEmpNum() {
        return empNum;
    }

    public void setEmpNum(int empNum) {
        this.empNum = empNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNoticeContent() {
        return noticeContent;
    }

    public void setNoticeContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }

    public long getNoticeTime() {
        return noticeTime;
    }

    public void setNoticeTime(long noticeTime) {
        this.noticeTime = noticeTime;
    }

    public List<String> getRootDpartId() {
        return rootDpartId;
    }

    public void setRootDpartId(List<String> rootDpartId) {
        this.rootDpartId = rootDpartId;
    }
}
