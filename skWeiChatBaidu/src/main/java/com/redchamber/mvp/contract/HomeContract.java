package com.redchamber.mvp.contract;


import com.redchamber.bean.VersionBean;
import com.redchamber.lib.base.response.BaseResponse;
import com.redchamber.lib.mvp.BaseModel;
import com.redchamber.lib.mvp.BasePresenter;
import com.redchamber.lib.mvp.BaseView;

import io.reactivex.Observable;

public interface HomeContract {

    interface HomeM extends BaseModel {

        Observable<BaseResponse<VersionBean>> getVersion(String token);

    }

    interface HomeV extends BaseView {

        void showVersion(VersionBean versionBean);

    }

    abstract class HomeP extends BasePresenter<HomeM, HomeV> {

        public abstract void getVersion(String token);

    }

}
