package com.redchamber.wallet;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.redchamber.api.GlobalConstants;
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
 * 提现
 */
public class WithdrawalActivity extends BaseActivity {

    @BindView(R.id.et_name)
    EditText mEtName;
    @BindView(R.id.et_alipay)
    EditText mEtAliPay;
    @BindView(R.id.et_amount)
    EditText mEtAmount;
    @BindView(R.id.tv_balance)
    TextView mTvBalance;
    @BindView(R.id.tv_money)
    TextView mTvMoney;
    @BindView(R.id.tv_hint)
    TextView mTvHint;

    private AccountIndexBean mAccountIndexBean;
    private String mGold;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_withdrawal;
    }

    @Override
    protected void initView() {
        if (getIntent() != null) {
            mAccountIndexBean = (AccountIndexBean) getIntent().getSerializableExtra(GlobalConstants.KEY_ACCOUNT_INDEX);
            mTvBalance.setText(String.format("%d红豆", mAccountIndexBean.balance));
            mEtAmount.setHint("最少提现" + mAccountIndexBean.minGold + "红豆");
            mTvHint.setText(String.format("剩余%d次机会", mAccountIndexBean.leftTimes));
            if (mAccountIndexBean.goldScale != 0) {
                addEditTextWatch(mAccountIndexBean.goldScale);
            } else {
                addEditTextWatch(1);
            }
        }
    }

    @OnClick({R.id.iv_back, R.id.tv_draw})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_draw:
                drawMoney();
                break;
        }
    }

    public static void startActivity(Context context, AccountIndexBean accountIndexBean) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, WithdrawalActivity.class);
        intent.putExtra(GlobalConstants.KEY_ACCOUNT_INDEX, accountIndexBean);
        context.startActivity(intent);
    }

    private void addEditTextWatch(int goldScale) {
        mEtAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mGold = mEtAmount.getText().toString().trim();
                if (TextUtils.isEmpty(mGold)) {
                    mTvMoney.setText("0元");
                } else {
                    mTvMoney.setText(Double.valueOf(mGold) / goldScale + "元");
                }
            }
        });
    }

    private void drawMoney() {
        String name = mEtName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtils.showToast("请输入真实姓名");
            return;
        }
        String account = mEtAliPay.getText().toString().trim();
        if (TextUtils.isEmpty(account)) {
            ToastUtils.showToast("请输入支付宝账号");
            return;
        }
        if (TextUtils.isEmpty(mGold)) {
            ToastUtils.showToast("请输入提现金额");
            return;
        }
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);
        params.put("drawType", "0");//支付宝
        params.put("account", account);//提现对应的账户
        params.put("realName", name);//用户真实姓名
        params.put("gold", mGold);//要提现的红豆数

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_PAY_DRAW_MONEY)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            ToastUtils.showToast("已提交");
                            finish();
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
