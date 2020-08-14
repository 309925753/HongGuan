package com.sk.weichat.util.secure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.util.Base64;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ParameterTest {
    private Random r = new Random();

    @Test
    public void joinObject() {
        String json = "{\"fromUserName\":\"lel1\",\"fileName\":\"\",\"deleteTime\":-1,\"fromUserId\":\"10017133\",\"timeSend\":1.564038298066E9,\"location_x\":\"\",\"messageId\":\"07c365cbfc1842959763410daec2552f\",\"location_y\":\"\",\"type\":1,\"toUserId\":\"10018579\",\"content\":\"2333\",\"objectId\":\"\"}";
        Map<String, Object> map = JSON.parseObject(json);
        System.out.println(map.remove("mac"));
        System.out.println(map);
        String value = Parameter.joinObjectValues(map);
        assertEquals("2333-110017133lel107c365cbfc1842959763410daec2552f1564038298100185791", value);
    }

    @Test
    public void joinMessage() {
        String json = "{\"fromUserName\":\"lel1\",\"fileName\":\"\",\"deleteTime\":-1,\"fromUserId\":\"10017133\",\"timeSend\":1.564038298066E9,\"location_x\":\"\",\"messageId\":\"07c365cbfc1842959763410daec2552f\",\"location_y\":\"\",\"type\":1,\"toUserId\":\"10018579\",\"content\":\"2333\",\"objectId\":\"\"}";
        Map<String, Object> map = new ChatMessage(json).toJsonObject(null);
        System.out.println(map.remove("mac"));
        System.out.println(map);
        String value = Parameter.joinObjectValues(map);
        assertEquals("2333-110017133lel107c365cbfc1842959763410daec2552f1564038298100185791", value);
    }

    @Test
    public void joinJson() {
        String json = "{\"serial\":\"D843AA79BD7142D9B2283B96F7AC7AD4\",\"longitude\":\"112.9578\",\"mac\":\"MoN4YA7aVkCbAdSt3w0aww==\",\"grant_type\":\"client_credentials\",\"latitude\":\"22.7702\",\"appId\":\"com.shiku.coolim.push1\",\"xmppVersion\":\"1\",\"loginType\":\"0\"}";
        Map<String, String> map = JSON.parseObject(json, new TypeReference<Map<String, String>>() {
        }.getType());
        System.out.println(map.remove("mac"));
        System.out.println(map);
        String value = Parameter.joinValues(map);
        assertEquals("com.shiku.coolim.push1client_credentials22.77020112.9578D843AA79BD7142D9B2283B96F7AC7AD41", value);
    }

    @Test
    public void joinValues() {
        HashMap<String, String> map = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            String key = random();
            map.put(key, key.substring(0, 1));
        }
        System.out.println(map);
        System.out.println(Parameter.joinValues(map));
    }

    private String random() {
        byte[] buf = new byte[6];
        r.nextBytes(buf);
        return Base64.encode(buf);
    }
}