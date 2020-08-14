package com.redchamber.lib.net.rx;

import io.reactivex.observers.DisposableObserver;

public abstract class RxSubscriber<T> extends DisposableObserver<T> {

    @Override
    public void onNext(T t) {
        _onNext(t);
    }

    @Override
    public void onError(Throwable e) {
        _onError(NetWorkCodeException.getResponseThrowable(e));
    }

    @Override
    public void onComplete() {
        _onComplete();
    }

    public abstract void _onNext(T t);
    public abstract void _onError(NetWorkCodeException.ResponseThrowable e);
    public abstract void _onComplete();
}
