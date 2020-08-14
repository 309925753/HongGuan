package com.redchamber.wallet;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.redchamber.bean.AccountIndexBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 钱包
 */
public class WalletActivity extends BaseActivity {

    @BindView(R.id.tv_bean)
    TextView mTvBean;

    private AccountIndexBean mAccountIndexBean;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_wallet;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        getAccountIndex();
    }

    @OnClick({R.id.iv_back, R.id.tv_detail, R.id.rl_bind, R.id.tv_charge, R.id.tv_cash})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_detail:
                WalletDetailActivity.startActivity(this);
                break;
            case R.id.tv_charge:
                if (mAccountIndexBean != null) {
                    RechargeActivity.startActivity(this, mAccountIndexBean.balance);
                }
                break;
            case R.id.tv_cash:
                if (mAccountIndexBean != null) {
                    WithdrawalActivity.startActivity(this, mAccountIndexBean);
                }
                break;
        }
    }

    public static void startWalletActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, WalletActivity.class));
    }

    private void getAccountIndex() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_PAY_ACCOUNT_INDEX)
                .params(params)
                .build()
                .execute(new BaseCallback<AccountIndexBean>(AccountIndexBean.class) {

                    @Override
                    public void onResponse(ObjectResult<AccountIndexBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            mAccountIndexBean = result.getData();
                            mTvBean.setText(String.valueOf(mAccountIndexBean.balance));
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtils.showToast(e.getMessage());
                    }
                });
    }

}
