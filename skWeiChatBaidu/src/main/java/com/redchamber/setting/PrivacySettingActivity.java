package com.redchamber.setting;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.CheckBox;

import com.redchamber.bean.AppSettingBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.request.AppSettingRequest;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 隐私设置
 */
public class PrivacySettingActivity extends BaseActivity {

    @BindView(R.id.cb_hide)
    CheckBox mCbHide;
    @BindView(R.id.cb_distance)
    CheckBox mCbDistance;
    @BindView(R.id.cb_time)
    CheckBox mCbTime;

    private AppSettingBean mAppSettingBean;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_privacy_setting;
    }

    @Override
    protected void initView() {
        addCheckboxListener();
        queryAppSetting();
    }

    @OnClick(R.id.iv_back)
    void onClick(View view) {
        finish();
    }

    private void queryAppSetting() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.post().url(coreManager.getConfig().RED_QUERY_APP_SETTING)
                .params(params)
                .build()
                .execute(new BaseCallback<AppSettingBean>(AppSettingBean.class) {

                    @Override
                    public void onResponse(ObjectResult<AppSettingBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (1 == result.getResultCode() && result.getData() != null) {
                            mAppSettingBean = result.getData();
                            mCbHide.setChecked(1 == mAppSettingBean.hiddenIndex);
                            mCbDistance.setChecked(1 == mAppSettingBean.hiddenDistance);
                            mCbTime.setChecked(1 == mAppSettingBean.hiddenOnlineTime);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtils.showToast(e.getMessage());
                    }
                });
    }


    public static void startPrivacySettingActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, PrivacySettingActivity.class));
    }

    private void addCheckboxListener() {
        mCbHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAppSettingBean == null) {
                    return;
                }
                int openFlag = mAppSettingBean.hiddenIndex == 1 ? 0 : 1;
                AppSettingRequest.getInstance().set(PrivacySettingActivity.this, "3", openFlag,
                        new AppSettingRequest.AppSettingCallBack() {
                            @Override
                            public void onSuccess() {
                                mAppSettingBean.hiddenIndex = openFlag;
                                mCbHide.setChecked(openFlag == 1);
                            }

                            @Override
                            public void onFail(String error) {
                                ToastUtils.showToast(error);
                            }
                        });
            }
        });

        mCbDistance.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mAppSettingBean == null) {
                    return;
                }
                int openFlag = mAppSettingBean.hiddenDistance == 1 ? 0 : 1;
                AppSettingRequest.getInstance().set(PrivacySettingActivity.this, "4", openFlag,
                        new AppSettingRequest.AppSettingCallBack() {
                            @Override
                            public void onSuccess() {
                                mAppSettingBean.hiddenDistance = openFlag;
                                mCbDistance.setChecked(openFlag == 1);
                            }

                            @Override
                            public void onFail(String error) {
                                ToastUtils.showToast(error);
                            }
                        });
            }
        });

        mCbTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mAppSettingBean == null) {
                    return;
                }
                int openFlag = mAppSettingBean.hiddenOnlineTime == 1 ? 0 : 1;
                AppSettingRequest.getInstance().set(PrivacySettingActivity.this, "5", openFlag,
                        new AppSettingRequest.AppSettingCallBack() {
                            @Override
                            public void onSuccess() {
                                mAppSettingBean.hiddenOnlineTime = openFlag;
                                mCbTime.setChecked(openFlag == 1);
                            }

                            @Override
                            public void onFail(String error) {
                                ToastUtils.showToast(error);
                            }
                        });
            }
        });
    }

}
