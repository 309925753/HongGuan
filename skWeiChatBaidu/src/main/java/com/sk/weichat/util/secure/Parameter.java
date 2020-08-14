package com.sk.weichat.util.secure;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("WeakerAccess")
public class Parameter {
    public static String joinObjectValues(Map<String, ?> map) {
        TreeMap<String, String> treeMap = new TreeMap<>(String::compareTo);
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof Number) {
                value = ((Number) value).longValue();
            }
            treeMap.put(key, value == null ? null : value.toString());
        }
        return joinSortedMap(treeMap);
    }

    public static String joinValues(Map<String, String> map) {
        TreeMap<String, String> treeMap = new TreeMap<>(String::compareTo);
        treeMap.putAll(map);
        return joinSortedMap(treeMap);
    }

    @NonNull
    public static String joinSortedMap(TreeMap<String, String> treeMap) {
        StringBuilder sb = new StringBuilder();
        for (String value : treeMap.values()) {
            sb.append(value);
        }
        return sb.toString();
    }
}
