package com.redchamber.lib.utils;

import java.lang.reflect.ParameterizedType;

public class ObjectGetByClassUtils {
    /**
     * @param o
     * @param index
     * @param <T>
     *
     * @return
     */
    public static <T> T getClass(Object o, int index) {
        try {
            return ((Class<T>) ((ParameterizedType) o.getClass().getGenericSuperclass()).getActualTypeArguments()[index]).newInstance();
        } catch (InstantiationException e) {
//            e.printStackTrace();
        } catch (IllegalAccessException e) {
//            e.printStackTrace();
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return null;
    }
}
