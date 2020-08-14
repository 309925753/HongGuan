package com.redchamber.lib.base;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.redchamber.lib.mvp.BaseModel;
import com.redchamber.lib.mvp.BasePresenter;
import com.redchamber.lib.utils.ActivityManager;
import com.redchamber.lib.utils.ObjectGetByClassUtils;
import com.sk.weichat.ui.base.BaseLoginActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseActivity<T extends BaseModel, E extends BasePresenter> extends BaseLoginActivity {

    public T mModel;
    public E mPresenter;

    private ProgressDialog mProgressBar;
    private int mProgressBarCount;

    private Unbinder mBinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setLayout());
        mBinder = ButterKnife.bind(this);

        if (mModel == null) mModel = ObjectGetByClassUtils.getClass(this, 0);
        if (mPresenter == null) mPresenter = ObjectGetByClassUtils.getClass(this, 1);
        if (mModel != null && mPresenter != null) {
            mPresenter.setMV(mModel, this);
        }

        initView();

        ActivityManager.getManager().addActivity(this);

        initProgressDialog();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.onDestroy();
        }
        if (mBinder != null) {
            mBinder.unbind();
        }
        ActivityManager.getManager().finishActivity(this);
    }

    protected abstract int setLayout();

    protected abstract void initView();

    protected void initProgressDialog() {
        if (mProgressBar == null) {
            mProgressBar = new ProgressDialog(this);
            mProgressBar.setTitle("loading...");
        }
    }

    public void dismissLoading() {

        if (--mProgressBarCount <= 0 && mProgressBar != null && mProgressBar.isShowing()) {
            mProgressBarCount = 0;
            mProgressBar.dismiss();
        }

    }

    public void showLoading() {

        initProgressDialog();

        if (!mProgressBar.isShowing()) {
            mProgressBar.show();
        }

        mProgressBarCount++;

    }

    /**
     * 解决系统字体大小更换导致app字体变化布局混乱
     **/
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

}
