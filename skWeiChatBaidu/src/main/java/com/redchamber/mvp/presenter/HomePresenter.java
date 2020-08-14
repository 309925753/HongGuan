package com.redchamber.mvp.presenter;


import com.redchamber.bean.VersionBean;
import com.redchamber.lib.base.response.BaseResponse;
import com.redchamber.lib.net.rx.NetWorkCodeException;
import com.redchamber.lib.net.rx.RxObservableListener;
import com.redchamber.mvp.contract.HomeContract;

public class HomePresenter extends HomeContract.HomeP {

    @Override
    public void getVersion(String token) {
        getRxManager().addObserver(mModel.getVersion(token), new RxObservableListener<BaseResponse<VersionBean>>(mView) {

            @Override
            public void onNext(BaseResponse<VersionBean> result) {
                super.onNext(result);
                mView.showVersion(result.data);
            }

            @Override
            public void onNetError(NetWorkCodeException.ResponseThrowable e) {
                super.onNetError(e);
            }
        });
    }

}
