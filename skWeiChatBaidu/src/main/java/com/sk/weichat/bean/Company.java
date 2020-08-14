package com.sk.weichat.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sk.weichat.util.StringUtils;

import java.io.Serializable;

@DatabaseTable
public class Company implements Serializable, Cloneable {
    private static final long serialVersionUID = -320509115872200611L;

    @DatabaseField(id = true)
    private int id;// 公司Id

    @DatabaseField
    private String name;// 公司名称

    @DatabaseField
    private int industryId;// 公司行业

    @DatabaseField
    private int natureId;// 公司性质

    @DatabaseField
    private int scale;// 公司规模

    @DatabaseField
    private String description;// 公司说明

    @DatabaseField
    private String website;// 公司主页

    @DatabaseField
    private int countryId;// 国家

    @DatabaseField
    private int provinceId;// 省份

    @DatabaseField
    private int cityId;// 城市

    @DatabaseField
    private int areaId;// 地区

    @DatabaseField
    private int createTime;// 创建时间

    @DatabaseField
    private int isAuth;

    // 用户信息集合
    // @ForeignCollectionField(eager = false)
    // private ForeignCollection<User> users;

    @DatabaseField
    private double longitude;// 经度

    @DatabaseField
    private double latitude;// 纬度

    @DatabaseField
    private String address;// 详细地址

    @DatabaseField
    private float total;// 累计充值金钱的总数

    @DatabaseField
    private float balance;// 可用的金钱总数

    @DatabaseField
    private int payMode;// 支付的方式，

    @DatabaseField
    private long payEndTime;// 包年包月结束时间

    @DatabaseField
    private int status;// 1、正常

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndustryId() {
        return industryId;
    }

    public void setIndustryId(int industryId) {
        this.industryId = industryId;
    }

    public int getNatureId() {
        return natureId;
    }

    public void setNatureId(int natureId) {
        this.natureId = natureId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public int getIsAuth() {
        return isAuth;
    }

    public void setIsAuth(int isAuth) {
        this.isAuth = isAuth;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public int getPayMode() {
        return payMode;
    }

    public void setPayMode(int payMode) {
        this.payMode = payMode;
    }

    public long getPayEndTime() {
        return payEndTime;
    }

    public void setPayEndTime(long payEndTime) {
        this.payEndTime = payEndTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Company)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        Company other = (Company) o;
        boolean equals = true;
        equals &= id == other.id;
        equals &= StringUtils.strEquals(name, other.name);
        equals &= industryId == other.industryId;
        equals &= natureId == other.natureId;
        equals &= scale == other.scale;
        equals &= StringUtils.strEquals(description, other.description);
        equals &= StringUtils.strEquals(website, other.website);
        equals &= countryId == other.countryId;
        equals &= provinceId == other.provinceId;
        equals &= cityId == other.cityId;
        equals &= areaId == other.areaId;
        equals &= createTime == other.createTime;
        equals &= isAuth == other.isAuth;
        equals &= longitude == other.longitude;
        equals &= latitude == other.latitude;
        equals &= StringUtils.strEquals(address, other.address);
        equals &= total == other.total;
        equals &= balance == other.balance;
        equals &= payMode == other.payMode;
        equals &= payEndTime == other.payEndTime;
        equals &= status == other.status;
        return equals;
    }
}
