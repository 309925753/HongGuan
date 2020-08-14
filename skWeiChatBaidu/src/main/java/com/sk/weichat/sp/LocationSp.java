package com.sk.weichat.sp;

import android.content.Context;

/**
 * 不区分用户，因为即使切换用户，最后一次更新书剑的数据有一些错误也没关系 更新时间都是秒级别的，不是毫秒级别的
 */

/**
 * 通过Sharepreference来保存用户定位信息
 */
public class LocationSp extends CommonSp {
    private static final String SP_NAME = "location_info";// FILE_NAME
    private static LocationSp instance;

    /* known key */
    public static final String KEY_LONGITUDE = "longitude";// 经度
    public static final String KEY_LATITUDE = "latitude";// 纬度
    public static final String KEY_ADDRESS = "address";// 地址
    public static final String KEY_PROVINCE_NAME = "province_name";// 省份
    public static final String KEY_CITY_NAME = "city_name";// 城市名字
    public static final String KEY_DISTRICT_NAME = "district_name";// 区/县

    public static LocationSp getInstance(Context context) {
        if (instance == null) {
            synchronized (LocationSp.class) {
                if (instance == null) {
                    instance = new LocationSp(context);
                }
            }
        }
        return instance;
    }

    private LocationSp(Context context) {
        super(context, SP_NAME);
    }

    public void setLongitude(float value) {
        setValue(KEY_LONGITUDE, value);
    }

    public float getLongitude(float defaultValue) {
        return getValue(KEY_LONGITUDE, defaultValue);
    }

    public void setLatitude(float value) {
        setValue(KEY_LATITUDE, value);
    }

    public float getLatitude(float defaultValue) {
        return getValue(KEY_LATITUDE, defaultValue);
    }

    public void setAddress(String value) {
        setValue(KEY_ADDRESS, value);
    }

    public String getAddress(String defaultValue) {
        return getValue(KEY_ADDRESS, defaultValue);
    }

    public void setProvinceName(String value) {
        setValue(KEY_PROVINCE_NAME, value);
    }

    public String getProvinceName(String defaultValue) {
        return getValue(KEY_PROVINCE_NAME, defaultValue);
    }

    public void setCityName(String value) {
        setValue(KEY_CITY_NAME, value);
    }

    public String getCityName(String defaultValue) {
        return getValue(KEY_CITY_NAME, defaultValue);
    }

    public void setDistrictName(String value) {
        setValue(KEY_DISTRICT_NAME, value);
    }

    public String getDistrictName(String defaultValue) {
        return getValue(KEY_DISTRICT_NAME, defaultValue);
    }

}
