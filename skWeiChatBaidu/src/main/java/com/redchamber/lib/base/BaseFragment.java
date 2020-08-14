package com.redchamber.lib.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.redchamber.lib.mvp.BaseModel;
import com.redchamber.lib.mvp.BasePresenter;
import com.redchamber.lib.utils.ObjectGetByClassUtils;
import com.sk.weichat.ui.base.BaseLoginActivity;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.LocaleHelper;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment<T extends BaseModel, E extends BasePresenter> extends Fragment {

    public T mModel;
    public E mPresenter;

    private ProgressDialog mProgressBar;
    private int mProgressBarCount;

    private Unbinder mBinder;

    protected CoreManager coreManager = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(setLayout(), container, false);
        mBinder = ButterKnife.bind(this, rootView);

        if (mModel == null) mModel = ObjectGetByClassUtils.getClass(this, 0);
        if (mPresenter == null) mPresenter = ObjectGetByClassUtils.getClass(this, 1);
        if (mModel != null && mPresenter != null) {
            mPresenter.setMV(mModel, this);
        }
        Activity ctx = getActivity();
        LocaleHelper.setLocale(ctx, LocaleHelper.getLanguage(ctx));
        if (ctx instanceof BaseLoginActivity) {
            BaseLoginActivity activity = (BaseLoginActivity) ctx;
            // 只要activity在onCreate里调用了初始化，coreManager就不会空，同步的，
            coreManager = activity.coreManager;
//            if (coreManager != null) {
//                activity.addCoreStatusListener(this);
//            }
        }

        initView();

        return rootView;
    }

    protected abstract int setLayout();

    protected abstract void initView();

    protected void initProgressDialog() {
        if (mProgressBar == null) {
            mProgressBar = new ProgressDialog(getContext());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBinder != null) {
            mBinder.unbind();
        }
    }

}