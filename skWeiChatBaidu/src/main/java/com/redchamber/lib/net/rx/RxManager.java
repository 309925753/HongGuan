package com.redchamber.lib.net.rx;

import com.redchamber.lib.base.response.BaseResponse;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class RxManager {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public <T extends BaseResponse> DisposableObserver<T> addObserver(final Observable<T> netWorkObservable,
                                                                      final RxObservableListener<T> rxObservableListener) {
        DisposableObserver<T> observer = netWorkObservable.compose(RxSchedulers.<T>io_main())

                .subscribeWith(new RxSubscriber<T>() {
                    @Override
                    protected void onStart() {
                        super.onStart();
                        rxObservableListener.onNetStart(null);
                    }

                    @Override

                    public void _onNext(T t) {
//                        if (t != null && "99".equals(t.code)) {
//                            //身份过期
////                            Object navigation = ARouter.getInstance().build(ARoutePath.App.UserLoginServiceImpl).navigation();
////                            if (navigation != null) {
////                                ((UserLoginService) navigation).userInfoExpire();
////                            } else {
//                            ToastUtils.showToast("登录过期");
////                            }
//                        }
                        rxObservableListener.onNext(t);
                    }

                    @Override
                    public void _onError(NetWorkCodeException.ResponseThrowable e) {
                        rxObservableListener.onNetError(e);
                    }

                    @Override
                    public void _onComplete() {
                        rxObservableListener.onComplete();
                    }
                });

        if (observer != null) {
            compositeDisposable.add(observer);
        }
        return observer;
    }

    public void clear() {
        compositeDisposable.dispose();
    }


    public static RxManager getInstance() {
        return new RxManager();
    }

}
