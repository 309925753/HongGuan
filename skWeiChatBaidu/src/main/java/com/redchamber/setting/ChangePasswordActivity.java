package com.redchamber.setting;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.PreferenceUtils;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.util.EditTextUtils;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.util.secure.LoginPassword;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 修改密码
 */
public class ChangePasswordActivity extends BaseActivity {

    @BindView(R.id.et_password_old)
    EditText mEtPasswordOld;
    @BindView(R.id.et_password_new)
    EditText mEtPasswordNew;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_change_password;
    }

    @Override
    protected void initView() {
        EditTextUtils.setEditTextInhibitInputSpace(mEtPasswordOld, 11);
        EditTextUtils.setEditTextInhibitInputSpace(mEtPasswordNew, 11);
    }

    public ChangePasswordActivity() {
        noLoginRequired();
    }

    @OnClick({R.id.iv_back, R.id.tv_save})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_save:
                if (configPassword()) {
                    changePassword();
                }
                break;
        }
    }

    public static void startActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, ChangePasswordActivity.class));
    }

    private boolean configPassword() {
        String passwordOld = mEtPasswordOld.getText().toString().trim();
        if (TextUtils.isEmpty(passwordOld)) {
            ToastUtils.showToast("请输入原密码");
            return false;
        }
        String passwordNew = mEtPasswordNew.getText().toString().trim();
        if (TextUtils.isEmpty(passwordNew) || passwordNew.length() < 6) {
            ToastUtils.showToast("新密码最少6位数");
            return false;
        }
        return true;
    }

    private void changePassword() {
        final String phoneNumber = PreferenceUtils.getMobilePhone();
        final String oldPassword = mEtPasswordOld.getText().toString().trim();
        final String password = mEtPasswordNew.getText().toString().trim();

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("telephone", phoneNumber);
        params.put("areaCode", "86");
        params.put("oldPassword", LoginPassword.encodeMd5(oldPassword));
        params.put("newPassword", LoginPassword.encodeMd5(password));
        String url;
//        if (isSupportSecureChat) {
//            url = coreManager.getConfig().USER_PASSWORD_UPDATE_V1;
//            // SecureFlag 取出本地保存的私钥，使用新密码加密私钥，上传服务器
//            String dhPrivateKey = SecureChatUtil.getDHPrivateKey(coreManager.getSelf().getUserId());
//            String rsaPrivateKey = SecureChatUtil.getRSAPrivateKey(coreManager.getSelf().getUserId());
//            String newDHPrivateKey = SecureChatUtil.aesEncryptDHPrivateKey(password, dhPrivateKey);
//            String newRSAPrivateKey = SecureChatUtil.aesEncryptRSAPrivateKey(password, rsaPrivateKey);
//            String signature = SecureChatUtil.signatureUpdateKeys(password, authCode);
//            params.put("dhPrivateKey", newDHPrivateKey);
//            params.put("rsaPrivateKey", newRSAPrivateKey);
//            params.put("mac", signature);
//        } else {
        DialogHelper.showDefaulteMessageProgressDialog(mContext);
        url = coreManager.getConfig().USER_PASSWORD_UPDATE;
//        }
        HttpUtils.get().url(url)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (1 == result.getResultCode()) {
                            ToastUtils.showToast("修改成功");
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
