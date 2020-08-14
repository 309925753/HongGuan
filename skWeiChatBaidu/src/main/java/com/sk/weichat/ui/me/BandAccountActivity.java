package com.sk.weichat.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.sk.weichat.R;
import com.sk.weichat.bean.BindInfo;
import com.sk.weichat.bean.event.EventUpdateBandQqAccount;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.QQHelper;
import com.sk.weichat.ui.account.LoginActivity;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.AppUtils;
import com.sk.weichat.util.EventBusHelper;
import com.sk.weichat.view.SelectionFrame;
import com.sk.weichat.wxapi.EventUpdateBandAccount;
import com.tencent.tauth.Tencent;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.JsonCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.List;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 绑定账号
 */
public class BandAccountActivity extends BaseActivity implements View.OnClickListener {

    private TextView tvBindWx;
    private TextView tvBindQq;
    private boolean isBandWx;
    private boolean isBandQq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_band_account);
        EventBusHelper.register(this);
        initActionBar();
        initView();
        getBindInfo();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.bind_account_set));
    }

    private void initView() {
        tvBindWx = findViewById(R.id.tv_bind_wx);
        tvBindQq = findViewById(R.id.tv_bind_qq);
        findViewById(R.id.wx_band_rl).setOnClickListener(this);
        if (QQHelper.ENABLE) {
            findViewById(R.id.qq_band_rl).setOnClickListener(this);
        } else {
            findViewById(R.id.qq_band_rl).setVisibility(View.GONE);
        }
    }

    private void updateUi() {
        tvBindWx.setText(getString(isBandWx ? R.string.banded : R.string.no_band));
        tvBindQq.setText(getString(isBandQq ? R.string.banded : R.string.no_band));
    }

    // 获取用户的设置状态
    private void getBindInfo() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(coreManager.getConfig().USER_GET_BAND_ACCOUNT)
                .params("access_token", coreManager.getSelfStatus().accessToken)
                .build()
                .execute(new ListCallback<BindInfo>(BindInfo.class) {

                    @Override
                    public void onResponse(ArrayResult<BindInfo> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            List<BindInfo> data = result.getData();
                            for (BindInfo info : data) {
                                if (Integer.parseInt(LoginActivity.THIRD_TYPE_WECHAT) == info.getType()) {
                                    isBandWx = true;
                                } else if (Integer.parseInt(LoginActivity.THIRD_TYPE_QQ) == info.getType()) {
                                    isBandQq = true;
                                }

                            }
                            updateUi();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        updateUi();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wx_band_rl:
                showSelectDialog(LoginActivity.THIRD_TYPE_WECHAT, isBandWx, getString(R.string.wechat));
                break;
            case R.id.qq_band_rl:
                showSelectDialog(LoginActivity.THIRD_TYPE_QQ, isBandQq, getString(R.string.qq));
                break;
        }
    }

    private void showSelectDialog(String type, boolean isBand, String name) {
        String content = isBand ? getResources().getString(R.string.tip_bind_third_place_holder, name) : getResources().getString(R.string.tip_unbind_third_place_holder, name);
        String buttonText = isBand ? getResources().getString(R.string.dialog_Relieve) : getResources().getString(R.string.dialog_go);
        SelectionFrame selectionFrame = new SelectionFrame(mContext);
        selectionFrame.setSomething(null, content, getString(R.string.cancel), buttonText,
                new SelectionFrame.OnSelectionFrameClickListener() {

                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                        if (isBand) {
                            unBindInfo(type);
                        } else {
                            if (TextUtils.equals(LoginActivity.THIRD_TYPE_WECHAT, type)) {
                                if (!AppUtils.isAppInstalled(mContext, "com.tencent.mm")) {
                                    Toast.makeText(mContext, getString(R.string.tip_no_wx_chat), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else if (TextUtils.equals(LoginActivity.THIRD_TYPE_QQ, type)) {
                                if (!QQHelper.qqInstalled(mContext)) {
                                    Toast.makeText(mContext, getString(R.string.tip_no_qq_chat), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                QQHelper.qqBand(BandAccountActivity.this);
                            }
                        }
                    }
                });
        selectionFrame.show();
    }

    // 修改用户绑定
    private void unBindInfo(String type) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(coreManager.getConfig().USER_UN_BAND_ACCOUNT)
                .params("access_token", coreManager.getSelfStatus().accessToken)
                .params("type", type)
                .build()
                .execute(new JsonCallback() {
                    @Override
                    public void onResponse(String result) {
                        DialogHelper.dismissProgressDialog();

                        if (TextUtils.equals(LoginActivity.THIRD_TYPE_WECHAT, type)) {
                            isBandWx = false;
                        } else if (TextUtils.equals(LoginActivity.THIRD_TYPE_QQ, type)) {
                            isBandQq = false;
                        }
                        updateUi();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        updateUi();
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventUpdateBandAccount message) {
        isBandWx = "ok".equals(message.msg);
        updateUi();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventUpdateBandQqAccount message) {
        isBandQq = "ok".equals(message.msg);
        updateUi();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case com.tencent.connect.common.Constants.REQUEST_LOGIN:
            case com.tencent.connect.common.Constants.REQUEST_APPBAR:
                Tencent.onActivityResultData(requestCode, resultCode, data, QQHelper.getLoginListener(mContext));
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
