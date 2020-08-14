package com.sk.weichat.bean.company;

/**
 * Created by Administrator on 2017/5/11.
 */

public class StructBean {

    private String parent_id;
    private String id;

    // 公司创建者Id
    private String CreateUserId;

    private String companyId;
    private String departmentId;
    private String userId;

    private String text;

    private boolean isExpand;// 是否展开
    private int index;// 层级
    private boolean isCompany;
    private boolean isDepartment;
    private boolean isEmployee;

    // 公司公告
    private String notificationDes;
    // 根部门Id
    private String rootDepartmentId;

    // 员工所属公司Id
    private String EmployeeToCompanyId;
    // 身份
    private String identity;
    // 职位
    private int role;

    private boolean selected;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isCompany() {
        return isCompany;
    }

    public void setCompany(boolean company) {
        isCompany = company;
    }

    public boolean isDepartment() {
        return isDepartment;
    }

    public void setDepartment(boolean department) {
        isDepartment = department;
    }

    public boolean isEmployee() {
        return isEmployee;
    }

    public void setEmployee(boolean employee) {
        isEmployee = employee;
    }

    public String getCreateUserId() {
        return CreateUserId;
    }

    public void setCreateUserId(String createUserId) {
        CreateUserId = createUserId;
    }

    public String getNotificationDes() {
        return notificationDes;
    }

    public void setNotificationDes(String notificationDes) {
        this.notificationDes = notificationDes;
    }

    public String getRootDepartmentId() {
        return rootDepartmentId;
    }

    public void setRootDepartmentId(String rootDepartmentId) {
        this.rootDepartmentId = rootDepartmentId;
    }

    public String getEmployeeToCompanyId() {
        return EmployeeToCompanyId;
    }

    public void setEmployeeToCompanyId(String employeeToCompanyId) {
        EmployeeToCompanyId = employeeToCompanyId;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
