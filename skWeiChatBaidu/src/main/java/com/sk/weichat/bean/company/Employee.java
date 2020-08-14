package com.sk.weichat.bean.company;

/**
 * Created by Administrator on 2017/6/12 0012.
 */

public class Employee {

    /**
     * companyId : 593e2903cfb99719d40405b9
     * departmentId : 593e2903cfb99719d40405bc
     * id : 593e2914cfb99719d40405bf
     * nickname : 野蜂飞舞
     * role : 0
     * userId : 10006117
     */

    private String companyId;
    private String departmentId;
    private String id;
    private String nickname;
    private int role;
    private int userId;

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
