package com.redchamber.util;

import java.util.List;

public class CityUtils {

    public static String formatCity(List<String> cityList) {
        if (cityList == null || cityList.size() == 0) {
            return "";
        }
        String cityStr = "";
        for (String string : cityList) {
            cityStr += string + ";";
        }
        return cityStr.substring(0, cityStr.lastIndexOf(";"));
    }

}
