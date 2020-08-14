package com.xuan.xuanhttplibrary.okhttp.callback;

import com.alibaba.fastjson.TypeReference;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

public abstract class ListCallback<T> extends TypeCallback<ArrayResult<T>> {

    public ListCallback(Class<T> mClazz) {
        super(new TypeReference<ArrayResult<T>>(mClazz) {
        }.getType());
    }
}
