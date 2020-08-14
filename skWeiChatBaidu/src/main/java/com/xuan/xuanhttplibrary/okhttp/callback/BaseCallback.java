package com.xuan.xuanhttplibrary.okhttp.callback;

import com.alibaba.fastjson.TypeReference;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

public abstract class BaseCallback<T> extends TypeCallback<ObjectResult<T>> {

    public BaseCallback(Class<T> clazz) {
        this(clazz, true);
    }

    /**
     * @param mainThreadCallback true表示切到主线程再回调子类方法，
     */
    public BaseCallback(Class<T> clazz, boolean mainThreadCallback) {
        super(new TypeReference<ObjectResult<T>>(clazz == Void.class ? null : clazz) {
        }.getType(), mainThreadCallback);
    }
}
