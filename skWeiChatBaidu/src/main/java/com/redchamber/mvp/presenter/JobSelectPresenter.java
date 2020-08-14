package com.redchamber.mvp.presenter;

import com.redchamber.bean.IndustryJobBean;
import com.redchamber.lib.base.response.BaseResponse;
import com.redchamber.lib.net.rx.NetWorkCodeException;
import com.redchamber.lib.net.rx.RxObservableListener;
import com.redchamber.mvp.contract.JobSelectContract;

import java.util.List;

public class JobSelectPresenter extends JobSelectContract.JobSelectP {

    @Override
    public void getJobList() {
        getRxManager().addObserver(mModel.getJobList(), new RxObservableListener<BaseResponse<List<IndustryJobBean>>>(mView) {

            @Override
            public void onNext(BaseResponse<List<IndustryJobBean>> result) {
                super.onNext(result);
                mView.getJobListSuccess(result.data);
            }

            @Override
            public void onNetError(NetWorkCodeException.ResponseThrowable e) {
                super.onNetError(e);
            }
        });
    }

}
