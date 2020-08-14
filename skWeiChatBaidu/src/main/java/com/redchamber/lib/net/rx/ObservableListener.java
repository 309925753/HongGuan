package com.redchamber.lib.net.rx;

import com.redchamber.lib.base.response.BaseResponse;

public interface ObservableListener<T extends BaseResponse> {

    void onNetStart(String msg);
    void onNext(T result);
    void onComplete();
    void onNetError(NetWorkCodeException.ResponseThrowable responseThrowable);
}
