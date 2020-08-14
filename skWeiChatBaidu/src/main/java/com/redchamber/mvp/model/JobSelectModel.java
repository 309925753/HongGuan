package com.redchamber.mvp.model;
import com.redchamber.api.RequestApi;
import com.redchamber.bean.IndustryJobBean;
import com.redchamber.lib.base.response.BaseResponse;
import com.redchamber.lib.net.RetrofitManager;
import com.redchamber.mvp.contract.JobSelectContract;

import java.util.List;

import io.reactivex.Observable;

public class JobSelectModel implements JobSelectContract.JobSelectM {

    @Override
    public Observable<BaseResponse<List<IndustryJobBean>>> getJobList() {
        return RetrofitManager.getApiService(RequestApi.class).getJobList();
    }

}
