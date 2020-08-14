package com.sk.weichat.bean.company;

/**
 * Created by Administrator on 2017/5/17 0017.
 */

public class Department {

    private String departmentId;
    private String departmentName;
    private String belongToCompany;

    public String getBelongToCompany() {
        return belongToCompany;
    }

    public void setBelongToCompany(String belongToCompany) {
        this.belongToCompany = belongToCompany;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }
}
