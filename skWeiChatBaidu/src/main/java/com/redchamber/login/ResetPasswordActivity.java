package com.redchamber.login;

import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.PreferenceUtils;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.util.EditTextUtils;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.Code;
import com.sk.weichat.bean.UserRandomStr;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.util.DeviceInfoUtil;
import com.sk.weichat.util.secure.DH;
import com.sk.weichat.util.secure.LoginPassword;
import com.sk.weichat.util.secure.RSA;
import com.sk.weichat.util.secure.chat.SecureChatUtil;
import com.sk.weichat.view.TipDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 重置密码
 */
public class ResetPasswordActivity extends BaseActivity {

    @BindView(R.id.et_phone)
    EditText mEtPhone;
    @BindView(R.id.et_code)
    EditText mEtCode;
    @BindView(R.id.et_password)
    EditText mEtPassword;
    @BindView(R.id.tv_send)
    TextView mTvSend;
    @BindView(R.id.tv_hint)
    TextView mTvHint;

    private TimeCount mTimeCount;

    @Override
    protected int setLayout() {
        return R.layout.activity_reset_password;
    }

    @Override
    protected void initView() {
        mTimeCount = new TimeCount(60000, 1000);
        mEtPhone.setText(PreferenceUtils.getMobilePhone());
        EditTextUtils.setEditTextInhibitInputSpace(mEtPhone, 11);
        EditTextUtils.setEditTextInhibitInputSpace(mEtPassword, 11);
    }

    @OnClick({R.id.iv_back, R.id.tv_send, R.id.btn_reset})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_send:
                if (checkPhone()) {
                    sendSmsCode();
                }
                break;
            case R.id.btn_reset:
                if (nextStep()) {
                    if (MyApplication.IS_SUPPORT_SECURE_CHAT) {
                        checkSupportSecureChat();
                    } else {
                        resetPassword(false);
                    }
                }
                break;
        }
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
            mTvHint.setVisibility(View.VISIBLE);
            mTvHint.setText("请输入正确的手机号码");
        }
        return false;
    }

    private boolean nextStep() {
        final String phoneNumber = mEtPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, getString(R.string.hint_input_phone_number), Toast.LENGTH_SHORT).show();
            return false;
        }

        String authCode = mEtCode.getText().toString().trim();
        if (TextUtils.isEmpty(authCode)) {
            Toast.makeText(this, getString(R.string.input_message_code), Toast.LENGTH_SHORT).show();
            return false;
        }

        String password = mEtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password) || password.length() < 6 || password.length() > 11) {
            mTvHint.setVisibility(View.VISIBLE);
            mTvHint.setText("请输入6-11位的密码");
            return false;
        }
        return true;
    }

    private void checkSupportSecureChat() {
        final String phoneNumber = mEtPhone.getText().toString().trim();
        Map<String, String> params = new HashMap<>();
        params.put("areaCode", "86");
        params.put("telephone", phoneNumber);

        HttpUtils.get().url(coreManager.getConfig().AUTHKEYS_IS_SUPPORT_SECURE_CHAT)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<UserRandomStr>(UserRandomStr.class) {

                    @Override
                    public void onResponse(ObjectResult<UserRandomStr> result) {
                        if (Result.checkSuccess(mContext, result)) {// 新用户才需要，老用户不支持端到端加密，不需要
                            if (result.getData().getIsSupportSecureChat() == 1) {
                                TipDialog dialog = new TipDialog(mContext);
                                dialog.setmConfirmOnClickListener(getString(R.string.tip_forget_password), () -> {
                                    // 如果验证码正确，则可以重置密码
                                    resetPassword(true);
                                });
                                dialog.show();
                            } else {
                                resetPassword(false);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    private void resetPassword(boolean isSupportSecureChat) {
        final String phoneNumber = mEtPhone.getText().toString().trim();
        final String password = mEtPassword.getText().toString().trim();
        String authCode = mEtCode.getText().toString().trim();
        Map<String, String> params = new HashMap<>();

        String url;
        if (isSupportSecureChat) {
            url = coreManager.getConfig().USER_PASSWORD_RESET_V1;
            // SecureFlag 将密钥对上传服务器
            DH.DHKeyPair dhKeyPair = DH.genKeyPair();
            String dhPublicKey = dhKeyPair.getPublicKeyBase64();
            String dhPrivateKey = dhKeyPair.getPrivateKeyBase64();
            String aesEncryptDHPrivateKeyResult = SecureChatUtil.aesEncryptDHPrivateKey(password, dhPrivateKey);
            RSA.RsaKeyPair rsaKeyPair = RSA.genKeyPair();
            String rsaPublicKey = rsaKeyPair.getPublicKeyBase64();
            String rsaPrivateKey = rsaKeyPair.getPrivateKeyBase64();
            String aesEncryptRSAPrivateKeyResult = SecureChatUtil.aesEncryptRSAPrivateKey(password, rsaPrivateKey);
            String signature = SecureChatUtil.signatureUploadKeys(password, phoneNumber);
            params.put("dhPublicKey", dhPublicKey);
            params.put("dhPrivateKey", aesEncryptDHPrivateKeyResult);
            params.put("rsaPublicKey", rsaPublicKey);
            params.put("rsaPrivateKey", aesEncryptRSAPrivateKeyResult);
            params.put("mac", signature);
        } else {
            url = coreManager.getConfig().USER_PASSWORD_RESET;
        }
        params.put("telephone", phoneNumber);
        params.put("randcode", authCode);
        params.put("areaCode", "86");
        params.put("newPassword", LoginPassword.encodeMd5(password));
        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));

        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(url)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(ResetPasswordActivity.this, result)) {
                            if (isSupportSecureChat) {
                                SecureChatUtil.setFindPasswordStatus(phoneNumber, true);
                            }
                            ToastUtils.showToast("修改成功");
                            finish();
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
        params.put("isRegister", "0");
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


}
