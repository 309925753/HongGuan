package com.xuan.xuanhttplibrary.okhttp.callback;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

import okhttp3.Call;

public abstract class TypeCallback<T> extends AbstractCallback<T> {

    private Type type;

    public TypeCallback(Type type) {
        this.type = type;
    }

    public TypeCallback(Type type, boolean mainThreadCallback) {
        super(mainThreadCallback);
        this.type = type;
    }

    @NonNull
    @Override
    T parseResponse(Call call, String body) {
        return JSONObject.parseObject(body, type);
    }
}
