package com.sk.weichat.ui.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sk.weichat.R;
import com.sk.weichat.bean.OrderInfo;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.share.ShareConstant;
import com.sk.weichat.ui.share.ShareLoginActivity;
import com.sk.weichat.ui.tool.WebViewActivity;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.view.PayDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 外部浏览器调起当前界面 支付
 */
public class QuickPay extends BaseActivity {
    private boolean isNeedExecuteLogin;
    private String mShareContent;

    public QuickPay() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_pay);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // 已进入授权界面
        ShareConstant.IS_SHARE_QP_COME = true;

        Uri data = getIntent().getData();
        if (data != null) {
            mShareContent = data.toString();
        }
        if (TextUtils.isEmpty(mShareContent)) {// 外部跳转进入
            mShareContent = ShareConstant.ShareContent;
        } else {// 数据下载页面进入
            ShareConstant.ShareContent = mShareContent;
        }

        // 判断本地登录状态
        int userStatus = LoginHelper.prepareUser(mContext, coreManager);
        switch (userStatus) {
            case LoginHelper.STATUS_USER_FULL:
            case LoginHelper.STATUS_USER_NO_UPDATE:
            case LoginHelper.STATUS_USER_TOKEN_OVERDUE:
                boolean isConflict = PreferenceUtils.getBoolean(this, Constants.LOGIN_CONFLICT, false);
                if (isConflict) {
                    isNeedExecuteLogin = true;
                }
                break;
            case LoginHelper.STATUS_USER_SIMPLE_TELPHONE:
                isNeedExecuteLogin = true;
                break;
            case LoginHelper.STATUS_NO_USER:
            default:
                isNeedExecuteLogin = true;
        }

        if (isNeedExecuteLogin) {// 需要先执行登录操作
            startActivity(new Intent(mContext, ShareLoginActivity.class));
            finish();
            return;
        }
        initActionBar();
        initView();
    }

    private void initActionBar() {
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        TextView mTvTitleLeft = findViewById(R.id.tv_title_left);
        mTvTitleLeft.setText(getString(R.string.close));
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.pay, getString(R.string.app_name)));
        mTvTitleLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        String appId = WebViewActivity.URLRequest(mShareContent).get("appId");
        String prepayId = WebViewActivity.URLRequest(mShareContent).get("prepayId");
        String sign = WebViewActivity.URLRequest(mShareContent).get("sign");

        DialogHelper.showDefaulteMessageProgressDialog(mContext);
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("appId", appId);
        params.put("prepayId", prepayId);
        params.put("sign", sign);

        // 获取订单信息
        HttpUtils.get().url(coreManager.getConfig().PAY_GET_ORDER_INFO)
                .params(params)
                .build()
                .execute(new BaseCallback<OrderInfo>(OrderInfo.class) {

                    @Override
                    public void onResponse(ObjectResult<OrderInfo> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            PayDialog payDialog = new PayDialog(mContext, appId, prepayId, sign, result.getData(), new PayDialog.PayResultListener() {
                                @Override
                                public void payResult(String result) {
                                    Toast.makeText(QuickPay.this, getString(R.string.pay_success), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                            payDialog.show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }
}

