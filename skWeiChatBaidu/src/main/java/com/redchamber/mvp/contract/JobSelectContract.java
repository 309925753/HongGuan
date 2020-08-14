package com.redchamber.mvp.contract;

import com.redchamber.bean.IndustryJobBean;
import com.redchamber.lib.base.response.BaseResponse;
import com.redchamber.lib.mvp.BaseModel;
import com.redchamber.lib.mvp.BasePresenter;
import com.redchamber.lib.mvp.BaseView;

import java.util.List;

import io.reactivex.Observable;

public interface JobSelectContract {

    interface JobSelectM extends BaseModel {

        Observable<BaseResponse<List<IndustryJobBean>>> getJobList();

    }

    interface JobSelectV extends BaseView {

        void getJobListSuccess(List<IndustryJobBean> jobBeanList);

    }

    abstract class JobSelectP extends BasePresenter<JobSelectM, JobSelectV> {

        public abstract void getJobList();

    }


}
