package com.xuan.xuanhttplibrary.okhttp.callback;

import androidx.annotation.NonNull;

import okhttp3.Call;

/**
 * @deprecated 所有接口返回格式都是data, resultCode, resultMsg, 使用BaseCallback等封装转换成bean类，
 */
@Deprecated
public abstract class JsonCallback extends AbstractCallback<String> {
    @NonNull
    @Override
    String parseResponse(Call call, String body) {
        return body;
    }
}
