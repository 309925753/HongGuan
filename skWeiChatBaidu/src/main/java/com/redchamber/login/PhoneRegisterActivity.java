package com.redchamber.login;

import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.redchamber.api.GlobalConstants;
import com.redchamber.bean.RegisterInfoBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.PreferenceUtils;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.util.EditTextUtils;
import com.redchamber.web.WebViewActivity;
import com.sk.weichat.R;
import com.sk.weichat.bean.Code;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.util.secure.LoginPassword;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 手机号码注册
 */
public class PhoneRegisterActivity extends BaseActivity {

    @BindView(R.id.et_phone)
    EditText mEtPhone;
    @BindView(R.id.et_code)
    EditText mEtCode;
    @BindView(R.id.tv_send)
    TextView mTvSend;
    @BindView(R.id.et_password)
    EditText mEtPassword;
    @BindView(R.id.tv_hint)
    TextView mTvHint;

    private TimeCount mTimeCount;
    private String mSmsCode;

    @Override
    protected int setLayout() {
        return R.layout.activity_phone_register;
    }

    @Override
    protected void initView() {
        mTimeCount = new TimeCount(60000, 1000);
        mEtPhone.setText(PreferenceUtils.getMobilePhone());
        EditTextUtils.setEditTextInhibitInputSpace(mEtPhone, 11);
        EditTextUtils.setEditTextInhibitInputSpace(mEtPassword, 11);
    }

    public PhoneRegisterActivity() {
        noLoginRequired();
    }

    @OnClick({R.id.iv_back, R.id.tv_send, R.id.btn_next, R.id.tv_agreement})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_send:
                if (checkPhone()) {
                    verifyPhoneNumber();
                }
                break;
            case R.id.btn_next:
                if (checkPhone() && checkCodeAndPsd()) {
                    RegisterInfoBean registerInfoBean = new RegisterInfoBean();
                    registerInfoBean.telephone = mEtPhone.getText().toString().trim();
                    registerInfoBean.smsCode = mEtCode.getText().toString().trim();
                    registerInfoBean.realPassword = mEtPassword.getText().toString().trim();
                    registerInfoBean.password = LoginPassword.encodeMd5(registerInfoBean.realPassword);
                    SexSelectActivity.startSexSelectActivity(this, registerInfoBean);
                }
                break;
            case R.id.tv_agreement:
                WebViewActivity.startWebActivity(this, GlobalConstants.URL_AGREEMENT_USER, "用户协议");
                break;
        }
    }

    private boolean checkPhone() {
        String phone = mEtPhone.getText().toString().trim();
        if (!TextUtils.isEmpty(phone) && phone.length() == 11) {
            return true;
        } else {
            mTvHint.setVisibility(View.VISIBLE);
            mTvHint.setText("请输入正确的手机号码");
        }
        return false;
    }

    private boolean checkCodeAndPsd() {
        String code = mEtCode.getText().toString().trim();
        if (TextUtils.isEmpty(code) || code.length() != 6) {
            mTvHint.setVisibility(View.VISIBLE);
            mTvHint.setText("请输入6位数的验证码");
            return false;
        }
        if (!TextUtils.equals(mSmsCode, code)) {
            mTvHint.setVisibility(View.VISIBLE);
            mTvHint.setText("验证码错误");
            return false;
        }
        String password = mEtPassword.getText().toString().trim();
        if (!TextUtils.isEmpty(password) && password.length() >= 6 && password.length() <= 11) {
            return true;
        } else {
            mTvHint.setVisibility(View.VISIBLE);
            mTvHint.setText("请输入6-11位的密码");
        }
        return false;
    }

    class TimeCount extends CountDownTimer {

        TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mTvSend.setTextColor(getResources().getColor(R.color.color_333333));
            mTvSend.setText(millisUntilFinished / 1000 + "s后重发");
            mTvSend.setClickable(false);
        }

        @Override
        public void onFinish() {
            mTvSend.setTextColor(getResources().getColor(R.color.color_FB719A));
            mTvSend.setText("发送验证码");
            mTvSend.setClickable(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimeCount != null) {
            mTimeCount.cancel();
        }
    }

    private void sendSmsCode() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        String language = Locale.getDefault().getLanguage();
        params.put("language", language);
        params.put("areaCode", "86");
        params.put("telephone", mEtPhone.getText().toString().trim());
        params.put("imgCode", "");
        params.put("isRegister", "1");
        params.put("version", "1");
        HttpUtils.get().url(coreManager.getConfig().SEND_AUTH_CODE)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Code>(Code.class) {

                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            mSmsCode = result.getData().getCode();
                            ToastUtils.showToast("短信发送成功");
                            mTimeCount.start();
                        } else {
                            if (!TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtils.showToast(result.getResultMsg());
                            } else {
                                ToastUtils.showToast("内部服务器错误");
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtils.showToast(e.getMessage());
                    }
                });
    }

    private void verifyPhoneNumber() {
        DialogHelper.showDefaulteMessageProgressDialog(PhoneRegisterActivity.this);
        Map<String, String> params = new HashMap<>();
        params.put("telephone", mEtPhone.getText().toString().trim());
        params.put("areaCode", "86");
        params.put("verifyType", "1");
        HttpUtils.get().url(coreManager.getConfig().VERIFY_TELEPHONE)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {//手机号已经被注册
                            if (!TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtils.showToast(result.getResultMsg());
                            } else {
                                ToastUtils.showToast("手机号已经被注册");
                            }
                        } else {
                            sendSmsCode();
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
