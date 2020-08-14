package com.sk.weichat.bean;

public class IpInfo {

    /**
     * ip : 116.30.7.38
     * city : Shenzhen
     * region : Guangdong
     * country : CN
     * loc : 22.5333,114.1330
     */

    private String ip;
    private String city;
    private String region;
    private String country;
    private String loc;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }
}
