package com.redchamber.lib.net.rx;


import com.redchamber.lib.base.response.BaseResponse;
import com.redchamber.lib.mvp.BaseView;

public abstract class RxObservableListener<T extends BaseResponse> implements ObservableListener<T> {

    private BaseView mView;
    private String mErrorMsg;
    public String tag;

    protected RxObservableListener(BaseView view) {
        this.mView = view;
    }

    protected RxObservableListener(BaseView view, String errorMsg) {
        this.mView = view;
        this.mErrorMsg = errorMsg;
    }

    /**
     * 重写这个方法设置tag,区分网络请求
     *
     * @return
     */
    public String getTag() {
        return tag;
    }

    @Override
    public void onNetStart(String msg) {
        if (mView == null) {
            return;
        }
//        mView.onNetStart(getTag(), msg);
    }

    @Override
    public void onComplete() {
        if (mView == null) {
            return;
        }
//        mView.onNetFinish(getTag(), null);
    }

    @Override
    public void onNext(T result) {
    }

    @Override
    public void onNetError(NetWorkCodeException.ResponseThrowable e) {
//        if (mView == null) {
//            return;
//        }
//        if (mErrorMsg != null && !TextUtils.isEmpty(mErrorMsg)) {
//            mView.onNetError(getTag(), mErrorMsg);
//        } else {
//            mView.onNetError(getTag(), (e == null || TextUtils.isEmpty(e.message)) ? "" : e.message);
//        }
    }

}
