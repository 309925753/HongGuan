package com.redchamber.info;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.redchamber.api.GlobalConstants;
import com.redchamber.api.RequestCode;
import com.redchamber.bean.IndustryJobBean;
import com.redchamber.info.adapter.SelectIndustryAdapter;
import com.redchamber.info.adapter.SelectJobAdapter;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.mvp.contract.JobSelectContract;
import com.redchamber.mvp.model.JobSelectModel;
import com.redchamber.mvp.presenter.JobSelectPresenter;
import com.redchamber.util.GetJsonDataUtil;
import com.sk.weichat.R;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * 职业
 */
public class JobSelectActivity extends BaseActivity<JobSelectModel, JobSelectPresenter> implements
        SelectIndustryAdapter.onIndustryClickListener, SelectJobAdapter.onJobClickListener, JobSelectContract.JobSelectV {

    @BindView(R.id.rv_industry)
    RecyclerView mRvIndustry;
    @BindView(R.id.rv_job)
    RecyclerView mRvJob;

    private List<IndustryJobBean> mIndustryBeans;
    private SelectIndustryAdapter mIndustryAdapter;
    private SelectJobAdapter mJobAdapter;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public JobSelectActivity() {
        noLoginRequired();
    }

    @Override
    protected int setLayout() {
        return R.layout.activity_job_select;
    }

    @Override
    protected void initView() {
        mIndustryAdapter = new SelectIndustryAdapter(null);
        mRvIndustry.setLayoutManager(new LinearLayoutManager(this));
        mRvIndustry.setAdapter(mIndustryAdapter);
        mIndustryAdapter.setOnIndustryClickListener(this);

        mJobAdapter = new SelectJobAdapter(null);
        mRvJob.setLayoutManager(new LinearLayoutManager(this));
        mRvJob.setAdapter(mJobAdapter);
        mJobAdapter.setOnJobClickListener(this);

        getJobList();

    }

    @OnClick(R.id.iv_back)
    void onClick(View view) {
        finish();
    }

    @Override
    public void onIndustryItemClick(int position) {
        mJobAdapter.setNewData(mIndustryBeans.get(position).items);
    }

    @Override
    public void onJobItemClick(String job) {
        Intent i = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(GlobalConstants.KEY_JOB, job);
        setResult(RequestCode.REQUEST_CODE_SELECT_JOB, i.putExtras(bundle));
        finish();
    }

    @Override
    public void getJobListSuccess(List<IndustryJobBean> jobBeanList) {
        mIndustryBeans = jobBeanList;
        mIndustryAdapter.setNewData(mIndustryBeans);
        mJobAdapter.setNewData(mIndustryBeans.get(0).items);
    }

    private void getJobList() {
        showLoading();
        final Observable<List<IndustryJobBean>> observable = Observable.create(new ObservableOnSubscribe<List<IndustryJobBean>>() {

            @Override
            public void subscribe(ObservableEmitter<List<IndustryJobBean>> e) throws Exception {
                String JsonData = GetJsonDataUtil.getJson(JobSelectActivity.this, "jobs.json");
                mIndustryBeans = GetJsonDataUtil.parseJobListBean(JsonData);
                e.onNext(mIndustryBeans);
                e.onComplete();
            }

        });
        DisposableObserver<List<IndustryJobBean>> disposableObserver = new DisposableObserver<List<IndustryJobBean>>() {

            @Override
            public void onNext(List<IndustryJobBean> value) {
                mIndustryBeans = value;
                mIndustryAdapter.setNewData(mIndustryBeans);
                mJobAdapter.setNewData(mIndustryBeans.get(0).items);
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showToast("解析异常");
                dismissLoading();
            }

            @Override
            public void onComplete() {
                dismissLoading();
            }
        };
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(disposableObserver);
        mCompositeDisposable.add(disposableObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }
}
