package com.sk.weichat.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.sk.weichat.bean.Code;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.junit.Test;

import java.lang.reflect.Type;

public class TypeTest {
    String json = "{\"currentTime\":1563847754449,\"data\":{\"code\":\"asdf\"},\"resultCode\":1}";

    @Test
    public void type() throws Exception {
        System.out.println(getArrayType(String.class).getTypeName());

        ObjectResult<Code> result = JSONObject.parseObject(json, getArrayType(Code.class));
        System.out.println(result.getData().getCode());
    }

    private <T> Type getArrayType(Class<T> clazz) {
        return new TypeReference<ObjectResult<T>>(clazz) {
        }.getType();
    }

    @Test
    public void voidTest() throws Exception {
        System.out.println(getArrayType(Void.class));

        ObjectResult<Void> result = JSONObject.parseObject(json, getArrayType(Void.class));
        System.out.println(result.getData());
    }
}
