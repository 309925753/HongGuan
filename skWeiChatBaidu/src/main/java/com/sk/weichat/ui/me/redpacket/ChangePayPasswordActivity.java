package com.sk.weichat.ui.me.redpacket;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.secure.PayPassword;
import com.sk.weichat.view.PasswordInputView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import okhttp3.Call;

public class ChangePayPasswordActivity extends BaseActivity {

    private boolean needOldPassword = true;
    private boolean needTwice = true;

    private String oldPayPassword;
    private String newPayPassword;

    private TextView tvTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pay_password);

        initActionBar();
        initView();
        initData();
    }

    private void initData() {
        String userId = coreManager.getSelf().getUserId();
        if (TextUtils.isEmpty(userId)) {
            ToastUtil.showToast(this, R.string.tip_no_user_id);
            finish();
            return;
        }
        // 如果没有设置过支付密码，就不需要输入旧密码，
        needOldPassword = PreferenceUtils.getBoolean(this, Constants.IS_PAY_PASSWORD_SET + userId, true);
        Log.d(TAG, "initData: needOldPassword = " + needOldPassword);
        TextView tvTitle = findViewById(R.id.tv_title_center);
        TextView tvAction = findViewById(R.id.tvAction);
        ((TextView) findViewById(R.id.tv_title_center)).setText(getString(R.string.change_password));
        ((TextView) findViewById(R.id.tvAction)).setText(getString(R.string.btn_set_pay_password));
        if (!needOldPassword) {
            // 如果不需要旧密码，直接传空字符串，
            oldPayPassword = "";
            tvTip.setText(R.string.tip_change_pay_password_input_new);
            tvTitle.setText(R.string.btn_set_pay_password);
            tvAction.setText(R.string.btn_set_pay_password);
        } else {
            tvTitle.setText(R.string.btn_change_pay_password);
            tvAction.setText(R.string.btn_change_pay_password);
        }
    }

    private void initView() {
        tvTip = findViewById(R.id.tvTip);
        final TextView tvFinish = findViewById(R.id.tvFinish);
        ButtonColorChange.colorChange(ChangePayPasswordActivity.this, tvFinish);
        tvFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.showDefaulteMessageProgressDialog(ChangePayPasswordActivity.this);
                HttpUtils.get().url(coreManager.getConfig().UPDATE_PAY_PASSWORD)
                        .params("access_token", coreManager.getSelfStatus().accessToken)
                        .params("oldPayPassword", PayPassword.encodeMd5(coreManager.getSelf().getUserId(), oldPayPassword))
                        .params("payPassword", PayPassword.encodeMd5(coreManager.getSelf().getUserId(), newPayPassword))
                        .build()
                        .execute(new BaseCallback<Void>(Void.class) {
                            @Override
                            public void onResponse(ObjectResult<Void> result) {
                                DialogHelper.dismissProgressDialog();
                                if (Result.checkSuccess(ChangePayPasswordActivity.this, result)) {
                                    // 成功，
                                    ToastUtil.showToast(ChangePayPasswordActivity.this, R.string.tip_change_pay_password_success);
                                    // 记录下支付密码已经设置，
                                    MyApplication.getInstance().initPayPassword(coreManager.getSelf().getUserId(), 1);
                                }
                                finish();
                            }

                            @Override
                            public void onError(Call call, Exception e) {
                                Reporter.post("修改支付密码接口调用失败，", e);
                                DialogHelper.dismissProgressDialog();
                                String reason = e.getMessage();
                                if (TextUtils.isEmpty(reason)) {
                                    // 提示网络异常，
                                    reason = getString(R.string.net_exception);
                                }
                                ToastUtil.showToast(ChangePayPasswordActivity.this, reason);
                                finish();
                            }
                        });
            }
        });
        final PasswordInputView passwordInputView = findViewById(R.id.passwordInputView);
        passwordInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvFinish.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable psw) {
                if (psw.length() == AppConstant.PASS_WORD_LENGTH) {
                    if (needOldPassword) {
                        Log.e("zx", "afterTextChanged: " + psw.toString());
                        oldPayPassword = psw.toString();
                        DialogHelper.showDefaulteMessageProgressDialog(ChangePayPasswordActivity.this);
                        HttpUtils.get().url(coreManager.getConfig().CHECK_PAY_PASSWORD)
                                .params("access_token", coreManager.getSelfStatus().accessToken)
                                .params("payPassword", PayPassword.encodeMd5(coreManager.getSelf().getUserId(), oldPayPassword))
                                .build()
                                .execute(new BaseCallback<Void>(Void.class) {
                                    @Override
                                    public void onResponse(ObjectResult<Void> result) {
                                        DialogHelper.dismissProgressDialog();
                                        passwordInputView.setText("");
                                        if (Result.checkSuccess(ChangePayPasswordActivity.this, result)) {
                                            needOldPassword = false;
                                            tvTip.setText(R.string.tip_change_pay_password_input_new);
                                        }
                                    }

                                    @Override
                                    public void onError(Call call, Exception e) {
                                        Reporter.post("修改支付密码接口调用失败，", e);
                                        DialogHelper.dismissProgressDialog();
                                        String reason = e.getMessage();
                                        if (TextUtils.isEmpty(reason)) {
                                            // 提示网络异常，
                                            reason = getString(R.string.net_exception);
                                        }
                                        ToastUtil.showToast(ChangePayPasswordActivity.this, reason);
                                        finish();
                                    }
                                });
                    } else if (needTwice) {
                        needTwice = false;
                        newPayPassword = psw.toString();
                        Log.e("zx", "afterTextChanged: " + psw.toString());
                        passwordInputView.setText("");
                        tvTip.setText(R.string.tip_change_pay_password_input_twice);
                    } else if (psw.toString().equals(newPayPassword)) {
                        // 二次确认成功，
                        tvFinish.setVisibility(View.VISIBLE);
                    } else {
                        // 二次确认失败，重新输入新密码，
                        passwordInputView.setText("");
                        needTwice = true;
                        tvTip.setText(R.string.tip_change_pay_password_input_incorrect);
                        tvFinish.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(getString(R.string.change_password));
    }
}
