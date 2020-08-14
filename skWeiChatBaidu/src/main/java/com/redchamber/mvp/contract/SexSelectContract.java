package com.redchamber.mvp.contract;

import com.redchamber.lib.base.response.BaseResponse;
import com.redchamber.lib.mvp.BaseModel;
import com.redchamber.lib.mvp.BasePresenter;
import com.redchamber.lib.mvp.BaseView;

import io.reactivex.Observable;

/**
 * 选择性别
 */
public interface SexSelectContract {

    interface SexSelectM extends BaseModel {

        Observable<BaseResponse> setSex(int sex);

    }

    interface SexSelectV extends BaseView {

        void setSexResult(int resultCode, String resultMsg);

    }

    abstract class SexSelectP extends BasePresenter<SexSelectM, SexSelectV> {

        public abstract void setSex(int sex);

    }

}
