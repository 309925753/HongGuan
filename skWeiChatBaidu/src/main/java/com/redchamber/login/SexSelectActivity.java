package com.redchamber.login;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.redchamber.api.GlobalConstants;
import com.redchamber.bean.RegisterInfoBean;
import com.redchamber.info.ImprovePersonalInfoActivity;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.mvp.contract.SexSelectContract;
import com.redchamber.mvp.model.SexSelectModel;
import com.redchamber.mvp.presenter.SexSelectPresenter;
import com.redchamber.view.SexSelectDialog;
import com.sk.weichat.R;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 选择性别
 */
public class SexSelectActivity extends BaseActivity<SexSelectModel, SexSelectPresenter> implements
        SexSelectContract.SexSelectV {

    @BindView(R.id.iv_bg_male)
    ImageView mIvMale;
    @BindView(R.id.iv_bg_female)
    ImageView mIvFemale;

    private RegisterInfoBean mRegisterInfoBean;
    private int mIsMale = -1;

    @Override
    protected int setLayout() {
        return R.layout.activity_sex_select;
    }

    public SexSelectActivity() {
        noLoginRequired();
    }

    @Override
    protected void initView() {
        if (getIntent() != null) {
            mRegisterInfoBean = (RegisterInfoBean) getIntent().getSerializableExtra(GlobalConstants.KEY_REGISTER_INFO);
        }
    }

    @OnClick({R.id.iv_back, R.id.ll_male, R.id.ll_female, R.id.btn_next})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.ll_male:
                mIvMale.setVisibility(View.VISIBLE);
                mIvFemale.setVisibility(View.GONE);
                mIsMale = 1;
                break;
            case R.id.ll_female:
                mIvMale.setVisibility(View.GONE);
                mIvFemale.setVisibility(View.VISIBLE);
                mIsMale = 0;
                break;
            case R.id.btn_next:
                if (mIsMale == -1) {
                    ToastUtils.showToast("请选择性别");
                    return;
                }
                SexSelectDialog dialog = new SexSelectDialog(this);
                dialog.show();
                dialog.setOnConfirmListener(() -> {
                    if (mRegisterInfoBean != null) {
                        mRegisterInfoBean.sex = String.valueOf(mIsMale);
                        ImprovePersonalInfoActivity.startImprovePersonalInfoActivity(SexSelectActivity.this, mRegisterInfoBean);
                    }
                });
                break;
        }
    }

    public static void startSexSelectActivity(Context context, RegisterInfoBean registerInfoBean) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, SexSelectActivity.class);
        intent.putExtra(GlobalConstants.KEY_REGISTER_INFO, (Parcelable) registerInfoBean);
        context.startActivity(intent);
    }

    @Override
    public void setSexResult(int resultCode, String resultMsg) {
        dismissLoading();
        if (1 == resultCode) {
            startActivity(new Intent(SexSelectActivity.this, InvitationCodeActivity.class));
        } else {
            ToastUtils.showToast(resultMsg);
        }
    }

}
