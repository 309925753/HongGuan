package com.redchamber.setting;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.PreferenceUtils;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.util.EditTextUtils;
import com.redchamber.view.CommonHintSingleDialog;
import com.sk.weichat.R;
import com.sk.weichat.bean.Code;
import com.sk.weichat.helper.DialogHelper;
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
 * 更换手机号
 */
public class BindPhoneActivity extends BaseActivity {

    @BindView(R.id.et_phone)
    EditText mEtPhone;
    @BindView(R.id.et_code)
    EditText mEtCode;
//    @BindView(R.id.et_password)
//    EditText mEtPassword;
    @BindView(R.id.tv_send)
    TextView mTvSend;

    private TimeCount mTimeCount;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_bind_phone;
    }

    @Override
    protected void initView() {
        mTimeCount = new TimeCount(60000, 1000);
        EditTextUtils.setEditTextInhibitInputSpace(mEtPhone, 11);
//        EditTextUtils.setEditTextInhibitInputSpace(mEtPassword, 11);
    }

    @OnClick({R.id.iv_back, R.id.tv_send, R.id.btn_confirm})
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
            case R.id.btn_confirm:
                CommonHintSingleDialog commonHintSingleDialog = new CommonHintSingleDialog(this,
                        "你确定要修改绑定的手机号码？", "确定");
                commonHintSingleDialog.show();
                commonHintSingleDialog.setOnConfirmListener(new CommonHintSingleDialog.OnConfirmListener() {
                    @Override
                    public void onConfirmClick() {
                        if (checkPhone() && checkCodeAndPsd()) {
                            changePhoneNo();
                        }
                    }
                });
                break;
        }
    }

    public static void startActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, BindPhoneActivity.class));
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

    private boolean checkPhone() {
        String phone = mEtPhone.getText().toString().trim();
        if (!TextUtils.isEmpty(phone) && phone.length() == 11) {
            return true;
        } else {
            ToastUtils.showToast("请输入正确的手机号码");
        }
        return false;
    }

    private boolean checkCodeAndPsd() {
        String code = mEtCode.getText().toString().trim();
        if (TextUtils.isEmpty(code) || code.length() != 6) {
            ToastUtils.showToast("请输入6位数的验证码");
            return false;
        }
//        String password = mEtPassword.getText().toString().trim();
//        if (!TextUtils.isEmpty(password) && password.length() >= 6 && password.length() <= 14) {
//            return true;
//        } else {
//            ToastUtils.showToast("请输入6-14位的密码");
//        }
        return true;
    }

    private void verifyPhoneNumber() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
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
                        if (result.getResultCode() == 1) {
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

    private void changePhoneNo() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("oldPhoneNo", PreferenceUtils.getMobilePhone());
        params.put("phoneNo", mEtPhone.getText().toString().trim());
        params.put("smsCode", mEtCode.getText().toString().trim());
        HttpUtils.post().url(coreManager.getConfig().RED_CHANGE_PHONE_NO)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            ToastUtils.showToast("更换成功");
                            PreferenceUtils.saveMobilePhone(mEtPhone.getText().toString().trim());
                            finish();
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

}
