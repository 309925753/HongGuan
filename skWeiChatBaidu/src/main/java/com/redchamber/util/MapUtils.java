package com.redchamber.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class MapUtils {

    public static Map<String, String> objectToMap(Object obj) {
        Map<String, String> map = new HashMap<>();
        if (obj == null) {
            return map;
        }
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), String.valueOf(field.get(obj)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

}
