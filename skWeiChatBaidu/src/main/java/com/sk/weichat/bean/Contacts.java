package com.sk.weichat.bean;

/**
 * 手机联系人 in 本地
 */
public class Contacts {
    private String name;
    private String telephone;

    // 空来构造方法供fastjson用，
    public Contacts() {
    }

    public Contacts(String name, String telephone) {
        this.name = name;
        this.telephone = telephone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
