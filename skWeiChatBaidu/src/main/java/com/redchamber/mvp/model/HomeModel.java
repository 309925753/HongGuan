package com.redchamber.mvp.model;


import com.redchamber.bean.VersionBean;
import com.redchamber.lib.base.response.BaseResponse;
import com.redchamber.mvp.contract.HomeContract;

import io.reactivex.Observable;

public class HomeModel implements HomeContract.HomeM {

    @Override
    public Observable<BaseResponse<VersionBean>> getVersion(String token) {
        return null;
    }

}
