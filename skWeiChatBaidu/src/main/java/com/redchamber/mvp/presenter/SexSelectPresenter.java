package com.redchamber.mvp.presenter;


import com.redchamber.lib.base.response.BaseResponse;
import com.redchamber.lib.net.rx.NetWorkCodeException;
import com.redchamber.lib.net.rx.RxObservableListener;
import com.redchamber.mvp.contract.SexSelectContract;

public class SexSelectPresenter extends SexSelectContract.SexSelectP {

    @Override
    public void setSex(int sex) {
        getRxManager().addObserver(mModel.setSex(sex), new RxObservableListener<BaseResponse>(mView) {

            @Override
            public void onNext(BaseResponse result) {
                super.onNext(result);
                mView.setSexResult(result.resultCode, result.resultMsg);
            }

            @Override
            public void onNetError(NetWorkCodeException.ResponseThrowable e) {
                super.onNetError(e);
                mView.setSexResult(-1, e.message);
            }
        });
    }

}
