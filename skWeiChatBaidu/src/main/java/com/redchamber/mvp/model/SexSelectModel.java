package com.redchamber.mvp.model;


import com.redchamber.api.RequestApi;
import com.redchamber.lib.base.response.BaseResponse;
import com.redchamber.lib.net.RetrofitManager;
import com.redchamber.mvp.contract.SexSelectContract;

import io.reactivex.Observable;

public class SexSelectModel implements SexSelectContract.SexSelectM {

    @Override
    public Observable<BaseResponse> setSex(int sex) {
        return RetrofitManager.getApiService(RequestApi.class).setSex(sex);
    }

}
