package com.sk.weichat.ui.account;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.UserRandomStr;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.helper.PasswordHelper;
import com.sk.weichat.sp.UserSp;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.Base64;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.StringUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.ViewPiexlUtil;
import com.sk.weichat.util.secure.LoginPassword;
import com.sk.weichat.util.secure.RSA;
import com.sk.weichat.util.secure.chat.SecureChatUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 修改密码
 */
public class ChangePasswordActivity extends BaseActivity implements View.OnClickListener {
    private Button btn_change;
    private EditText mPhoneNumberEdit;
    private EditText mOldPasswordEdit;
    private EditText mPasswordEdit, mConfigPasswordEdit;
    private TextView tv_prefix;
    private int mobilePrefix = 86;
    // SecureFlag
    private String authCode;
    private boolean isSupportSecureChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        initActionBar();
        initView();
        isSupportSecureChat = MyApplication.IS_SUPPORT_SECURE_CHAT
                && !TextUtils.isEmpty(SecureChatUtil.getDHPrivateKey(coreManager.getSelf().getUserId()));
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.change_password));
    }

    private void initView() {
        tv_prefix = (TextView) findViewById(R.id.tv_prefix);
        tv_prefix.setOnClickListener(this);
        mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        tv_prefix.setText("+" + mobilePrefix);
        btn_change = (Button) findViewById(R.id.login_btn);
        ButtonColorChange.colorChange(this, btn_change);
        btn_change.setOnClickListener(this);
        mPhoneNumberEdit = (EditText) findViewById(R.id.phone_numer_edit);
        String telephone = coreManager.getSelf().getTelephoneNoAreaCode();
        mPhoneNumberEdit.setText(telephone);

        mOldPasswordEdit = (EditText) findViewById(R.id.old_password_edit);
        PasswordHelper.bindPasswordEye(mOldPasswordEdit, findViewById(R.id.tbEyeOld));
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        PasswordHelper.bindPasswordEye(mPasswordEdit, findViewById(R.id.tbEye));
        mConfigPasswordEdit = (EditText) findViewById(R.id.confirm_password_edit);
        PasswordHelper.bindPasswordEye(mConfigPasswordEdit, findViewById(R.id.tbEyeConfirm));
        List<EditText> mEditList = new ArrayList<>();
        mEditList.add(mOldPasswordEdit);
        mEditList.add(mPasswordEdit);
        mEditList.add(mConfigPasswordEdit);
        setBound(mEditList);

        mPhoneNumberEdit.setHint(getString(R.string.hint_input_phone_number));
        mPasswordEdit.setHint(getString(R.string.please_input_new_password));
        mConfigPasswordEdit.setHint(getString(R.string.please_confirm_new_password));
        btn_change.setText(getString(R.string.change_password));
    }

    public void setBound(List<EditText> mEditList) {// 为Edit内的drawableLeft设置大小
        for (int i = 0; i < mEditList.size(); i++) {
            Drawable[] compoundDrawable = mEditList.get(i).getCompoundDrawables();
            Drawable drawable = compoundDrawable[0];
            if (drawable != null) {
                drawable.setBounds(0, 0, ViewPiexlUtil.dp2px(this, 20), ViewPiexlUtil.dp2px(this, 20));
                mEditList.get(i).setCompoundDrawables(drawable, null, null, null);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_prefix:
                // 选择国家区号
                Intent intent = new Intent(this, SelectPrefixActivity.class);
                startActivityForResult(intent, SelectPrefixActivity.REQUEST_MOBILE_PREFIX_LOGIN);
                break;
            case R.id.login_btn:
                // 确认修改
                if (configPassword()) {
                    if (isSupportSecureChat) {
                        getCheckCode();
                    } else {
                        changePassword();
                    }
                }
                break;
        }
    }

    private void getCheckCode() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().USER_GET_RANDOM_STR)
                .params(params)
                .build()
                .execute(new BaseCallback<UserRandomStr>(UserRandomStr.class) {
                    @Override
                    public void onResponse(ObjectResult<UserRandomStr> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            String key = SecureChatUtil.getRSAPrivateKey(coreManager.getSelf().getUserId());
                            authCode = new String(RSA.decryptFromBase64(result.getData().getUserRandomStr(), Base64.decode(key)));
                            changePassword();
                        } else {
                            DialogHelper.dismissProgressDialog();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    /**
     * 修改密码
     */
    private void changePassword() {
        final String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
        final String oldPassword = mOldPasswordEdit.getText().toString().trim();
        final String password = mPasswordEdit.getText().toString().trim();

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("telephone", phoneNumber);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("oldPassword", LoginPassword.encodeMd5(oldPassword));
        params.put("newPassword", LoginPassword.encodeMd5(password));
        String url;
        if (isSupportSecureChat) {
            url = coreManager.getConfig().USER_PASSWORD_UPDATE_V1;
            // SecureFlag 取出本地保存的私钥，使用新密码加密私钥，上传服务器
            String dhPrivateKey = SecureChatUtil.getDHPrivateKey(coreManager.getSelf().getUserId());
            String rsaPrivateKey = SecureChatUtil.getRSAPrivateKey(coreManager.getSelf().getUserId());
            String newDHPrivateKey = SecureChatUtil.aesEncryptDHPrivateKey(password, dhPrivateKey);
            String newRSAPrivateKey = SecureChatUtil.aesEncryptRSAPrivateKey(password, rsaPrivateKey);
            String signature = SecureChatUtil.signatureUpdateKeys(password, authCode);
            params.put("dhPrivateKey", newDHPrivateKey);
            params.put("rsaPrivateKey", newRSAPrivateKey);
            params.put("mac", signature);
        } else {
            DialogHelper.showDefaulteMessageProgressDialog(mContext);
            url = coreManager.getConfig().USER_PASSWORD_UPDATE;
        }
        HttpUtils.get().url(url)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(ChangePasswordActivity.this, result)) {
                            Toast.makeText(ChangePasswordActivity.this, getString(R.string.update_sccuess), Toast.LENGTH_SHORT).show();
                            if (coreManager.getSelf() != null
                                    && !TextUtils.isEmpty(coreManager.getSelf().getTelephone())) {
                                UserSp.getInstance(mContext).clearUserInfo();
                                MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_SIMPLE_TELPHONE;
                                coreManager.logout();
                                LoginHelper.broadcastLogout(mContext);
                                LoginHistoryActivity.start(ChangePasswordActivity.this);
                            }
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(ChangePasswordActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 确认两次输入的密码是否一致
     */
    private boolean configPassword() {
        String password = mPasswordEdit.getText().toString().trim();
        String confirmPassword = mConfigPasswordEdit.getText().toString().trim();
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            mPasswordEdit.requestFocus();
            mPasswordEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.password_empty_error));
            return false;
        }
        if (TextUtils.isEmpty(confirmPassword) || confirmPassword.length() < 6) {
            mConfigPasswordEdit.requestFocus();
            mConfigPasswordEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.confirm_password_empty_error));
            return false;
        }
        if (confirmPassword.equals(password)) {
            return true;
        } else {
            mConfigPasswordEdit.requestFocus();
            mConfigPasswordEdit.setError(StringUtils.editTextHtmlErrorTip(this, R.string.password_confirm_password_not_match));
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != SelectPrefixActivity.RESULT_MOBILE_PREFIX_SUCCESS)
            return;
        mobilePrefix = data.getIntExtra(Constants.MOBILE_PREFIX, 86);
        tv_prefix.setText("+" + mobilePrefix);
    }
}
