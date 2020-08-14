package com.redchamber.mvp.presenter;

import com.redchamber.bean.SmsCodeBean;
import com.redchamber.lib.base.response.BaseResponse;
import com.redchamber.lib.net.rx.NetWorkCodeException;
import com.redchamber.lib.net.rx.RxObservableListener;
import com.redchamber.mvp.contract.PhoneRegisterContract;

import java.util.Map;

public class PhoneRegisterPresenter extends PhoneRegisterContract.PhoneRegisterP {

    @Override
    public void sendSmsCode(Map<String, String> map) {
        getRxManager().addObserver(mModel.sendSmsCode(map), new RxObservableListener<BaseResponse<SmsCodeBean>>(mView) {

            @Override
            public void onNext(BaseResponse<SmsCodeBean> result) {
                super.onNext(result);
                mView.sendSmsCodeResult(result.resultCode, result.resultMsg);
            }

            @Override
            public void onNetError(NetWorkCodeException.ResponseThrowable e) {
                super.onNetError(e);
                mView.sendSmsCodeResult(-1, e.message);
            }
        });
    }

    @Override
    public void userRegister(Map<String, String> map) {
        getRxManager().addObserver(mModel.userRegister(map), new RxObservableListener<BaseResponse>(mView) {

            @Override
            public void onNext(BaseResponse result) {
                super.onNext(result);
                mView.userRegisterResult(result.resultCode, result.resultMsg);
            }

            @Override
            public void onNetError(NetWorkCodeException.ResponseThrowable e) {
                super.onNetError(e);
                mView.userRegisterResult(-1, e.message);
            }
        });
    }

}
