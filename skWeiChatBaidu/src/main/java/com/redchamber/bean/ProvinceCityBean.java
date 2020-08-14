package com.redchamber.bean;

import java.util.List;

public class ProvinceCityBean {

    public String name;
    public List<CityBean> city;
    public boolean isSelect;

    public static class CityBean {

        public String name;
        public boolean isSelect;

    }

}
