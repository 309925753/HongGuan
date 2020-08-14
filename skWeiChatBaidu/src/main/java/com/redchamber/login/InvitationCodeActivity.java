package com.redchamber.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.redchamber.api.GlobalConstants;
import com.redchamber.bean.RegisterInfoBean;
import com.redchamber.friend.FriendHomePageActivity;
import com.redchamber.lib.utils.PreferenceUtils;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.request.UserLevelRequest;
import com.redchamber.util.UserLevelUtils;
import com.redchamber.vip.VipCenterActivity;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.helper.LoginSecureHelper;
import com.sk.weichat.helper.PrivacySettingHelper;
import com.sk.weichat.helper.YeepayHelper;
import com.sk.weichat.ui.MainActivity;
import com.sk.weichat.ui.account.RegisterActivity;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.DeviceInfoUtil;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.secure.DH;
import com.sk.weichat.util.secure.RSA;
import com.sk.weichat.util.secure.chat.SecureChatUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 邀请码
 */
public class InvitationCodeActivity extends BaseActivity implements View.OnClickListener {

    EditText mEtCode;
    TextView mTvHint;

    private RegisterInfoBean mRegisterInfoBean;
    private String dhPrivateKey, rsaPublicKey, rsaPrivateKey;
    public static int isRegisteredSyncCount = 0;

    public final static int TYPE_REGISTERED_NO_CODE = 1; //用户已注册但是没有邀请码
    private int mPageType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_code);
        initView();
    }

    public InvitationCodeActivity() {
        noLoginRequired();
    }

    protected void initView() {
        mEtCode = findViewById(R.id.et_code);
        mTvHint = findViewById(R.id.tv_hint);
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.iv_vip).setOnClickListener(this);
        mRegisterInfoBean = new RegisterInfoBean();
        if (getIntent() != null) {
            mRegisterInfoBean = (RegisterInfoBean) getIntent().getSerializableExtra(GlobalConstants.KEY_REGISTER_INFO);
            mPageType = getIntent().getIntExtra(GlobalConstants.KEY_TYPE, 0);
        }
        setSomething();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVip();
    }

    private void isVip() {
        UserLevelRequest.getInstance().queryUserLevel(InvitationCodeActivity.this, new UserLevelRequest.UserLevelCallBack() {
            @Override
            public void onSuccess(String userLevel) {
                if (UserLevelUtils.getLevels(userLevel)[1]) { //VIP
                    MainActivity.start(mContext);
                }
            }

            @Override
            public void onFail(String error) {
//                ToastUtils.showToast(error);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_next:
                String inviteCode = mEtCode.getText().toString().trim();
                if (TextUtils.isEmpty(inviteCode)) {
                    mTvHint.setVisibility(View.VISIBLE);
                    mTvHint.setText("请填写邀请码");
                    return;
                }
                if (1 == mPageType) {
                    //补填邀请码
                    putInviteCode(inviteCode);
                } else {
                    //注册
                    register(0);
                }
                break;
            case R.id.iv_vip:
                if (1 == mPageType) {
                    VipCenterActivity.startVipCenterActivity(InvitationCodeActivity.this);
                } else {
                    //注册
                    register(1);
                }
                break;
        }
    }

    public static void startActivity(Context context, RegisterInfoBean registerInfoBean) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, InvitationCodeActivity.class);
        intent.putExtra(GlobalConstants.KEY_REGISTER_INFO, (Serializable) registerInfoBean);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int type) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, InvitationCodeActivity.class);
        intent.putExtra(GlobalConstants.KEY_TYPE, type);
        context.startActivity(intent);
    }

    /**
     * 注册
     *
     * @param type 1->购买VIP注册； 0->填写邀请码注册
     */
    private void register(int type) {
        if (0 == type) {
            mRegisterInfoBean.inviteCode = mEtCode.getText().toString().trim();
            mRegisterInfoBean.userType = "0";
            mRegisterInfoBean.isSmsRegister = "1";
        }
        Map<String, String> params = new HashMap<>();
        if (MyApplication.IS_SUPPORT_SECURE_CHAT) {
            // SecureFlag 将密钥对上传服务器
            DH.DHKeyPair dhKeyPair = DH.genKeyPair();
            String dhPublicKey = dhKeyPair.getPublicKeyBase64();
            dhPrivateKey = dhKeyPair.getPrivateKeyBase64();
            String aesEncryptDHPrivateKeyResult = SecureChatUtil.aesEncryptDHPrivateKey(mRegisterInfoBean.realPassword, dhPrivateKey);
            RSA.RsaKeyPair rsaKeyPair = RSA.genKeyPair();
            rsaPublicKey = rsaKeyPair.getPublicKeyBase64();
            rsaPrivateKey = rsaKeyPair.getPrivateKeyBase64();
            String aesEncryptRSAPrivateKeyResult = SecureChatUtil.aesEncryptRSAPrivateKey(mRegisterInfoBean.realPassword, rsaPrivateKey);
            params.put("dhPublicKey", dhPublicKey);
            params.put("dhPrivateKey", aesEncryptDHPrivateKeyResult);
            params.put("rsaPublicKey", rsaPublicKey);
            params.put("rsaPrivateKey", aesEncryptRSAPrivateKeyResult);
        }

        // 前面页面传递的信息
        params.put("userType", "0");
        params.put("telephone", mRegisterInfoBean.telephone);
        params.put("password", mRegisterInfoBean.password);
        params.put("smsCode", mRegisterInfoBean.smsCode);
        if (!TextUtils.isEmpty(mRegisterInfoBean.inviteCode)) {
            params.put("inviteCode", mRegisterInfoBean.inviteCode);
        }
        params.put("areaCode", "86");// AreaCode 区号暂时不带
        // 本页面信息
        params.put("nickname", mRegisterInfoBean.nickname);
        params.put("sex", String.valueOf(mRegisterInfoBean.sex));
        params.put("birthday", mRegisterInfoBean.birthDay);
        params.put("residentCity", mRegisterInfoBean.residentCity);
        params.put("position", mRegisterInfoBean.position);
        params.put("program", mRegisterInfoBean.program);
        params.put("expectFriend", mRegisterInfoBean.expectFriend);
        params.put("height", mRegisterInfoBean.height);
        params.put("weight", mRegisterInfoBean.weight);
        params.put("description", mRegisterInfoBean.selfDesc);
        params.put("xmppVersion", "1");
//        params.put("countryId", String.valueOf(mTempData.getCountryId()));
//        params.put("provinceId", String.valueOf(mTempData.getProvinceId()));
//        params.put("cityId", String.valueOf(mTempData.getCityId()));
//        params.put("areaId", String.valueOf(mTempData.getAreaId()));

        params.put("isSmsRegister", String.valueOf(RegisterActivity.isSmsRegister));

        // 附加信息
        params.put("apiVersion", DeviceInfoUtil.getVersionCode(mContext) + "");
        params.put("model", DeviceInfoUtil.getModel());
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));
        // 地址信息
        double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
        String location = MyApplication.getInstance().getBdLocationHelper().getAddress();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));
        if (!TextUtils.isEmpty(location))
            params.put("location", location);
        params.put("areaName", MyApplication.getInstance().getBdLocationHelper().getDistrictName());
        DialogHelper.showDefaulteMessageProgressDialog(this);

        LoginSecureHelper.secureRegister(
                this, coreManager, mRegisterInfoBean.thirdToken, mRegisterInfoBean.thirdTokenType,
                params,
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(this, this.getString(R.string.tip_login_secure_place_holder, t.getMessage()));
                }, result -> {
                    DialogHelper.dismissProgressDialog();
                    if (!com.xuan.xuanhttplibrary.okhttp.result.Result.checkSuccess(getApplicationContext(), result)) {
                        if (result == null) {
                            Reporter.post("注册失败，result为空");
                        } else {
                            Reporter.post("注册失败，" + result.toString());
                        }
                        return;
                    }
                    // 注册成功
                    boolean success = LoginHelper.setLoginUser(InvitationCodeActivity.this, coreManager, mRegisterInfoBean.telephone,
                            mRegisterInfoBean.password, result);
                    if (success) {
                        PreferenceUtils.saveMobilePhone(mRegisterInfoBean.telephone);
                        isRegisteredSyncCount = 3;

                        if (MyApplication.IS_SUPPORT_SECURE_CHAT) {
                            SecureChatUtil.setDHPrivateKey(result.getData().getUserId(), dhPrivateKey);
                            SecureChatUtil.setRSAPublicKey(result.getData().getUserId(), rsaPublicKey);
                            SecureChatUtil.setRSAPrivateKey(result.getData().getUserId(), rsaPrivateKey);
                        }

                        // 新注册的账号没有支付密码，
                        MyApplication.getInstance().initPayPassword(result.getData().getUserId(), 0);
                        YeepayHelper.saveOpened(mContext, false);
                        PrivacySettingHelper.setPrivacySettings(InvitationCodeActivity.this, result.getData().getSettings());
                        MyApplication.getInstance().initMulti();
                        if (mRegisterInfoBean.headImage != null) {
                            //选择了头像，那么先上传头像
                            uploadAvatar(new File(mRegisterInfoBean.headImage.getPath()), type);
                            return;
                        } else {
                            if (1 == type) {
                                VipCenterActivity.startVipCenterActivity(InvitationCodeActivity.this, mRegisterInfoBean.sex);
                            } else {
                                MainActivity.start(mContext);
                                finish();
                            }

                        }
                        ToastUtil.showToast(InvitationCodeActivity.this, R.string.register_success);
                    } else {
                        // 失败
                        if (TextUtils.isEmpty(result.getResultMsg())) {
                            ToastUtil.showToast(InvitationCodeActivity.this, R.string.register_error);
                        } else {
                            ToastUtil.showToast(InvitationCodeActivity.this, result.getResultMsg());
                        }
                    }
                });
    }

    private void uploadAvatar(File file, int type) {
        if (!file.exists()) {
            // 文件不存在
            return;
        }
        // 显示正在上传的ProgressDialog
        DialogHelper.showMessageProgressDialog(this, getString(R.string.upload_avataring));
        RequestParams params = new RequestParams();
        String loginUserId = coreManager.getSelf().getUserId();
        params.put("userId", loginUserId);
        try {
            params.put("file1", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(coreManager.getConfig().AVATAR_UPLOAD_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                boolean success = false;
                if (arg0 == 200) {
                    Result result = null;
                    try {
                        result = JSON.parseObject(new String(arg2), Result.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (result != null && result.getResultCode() == Result.CODE_SUCCESS) {
                        success = true;
                    }
                }

                DialogHelper.dismissProgressDialog();
                if (success) {
                    ToastUtil.showToast(InvitationCodeActivity.this, R.string.upload_avatar_success);
                } else {
                    ToastUtil.showToast(InvitationCodeActivity.this, R.string.upload_avatar_failed);
                }
                if (1 == type) {
                    VipCenterActivity.startVipCenterActivity(InvitationCodeActivity.this, mRegisterInfoBean.sex);
                } else {
                    MainActivity.start(mContext);
                    finish();
                }

            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(InvitationCodeActivity.this, R.string.upload_avatar_failed);
            }
        });
    }

    //补填邀请码
    private void putInviteCode(String inviteCode) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);
        params.put("inviteCode", inviteCode);
        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_PUT_INVITE_CODE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            MainActivity.start(InvitationCodeActivity.this);
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

    private void setSomething() {
        if (!CoreManager.getInstance(this).getConfig().enablePayModule) {
            findViewById(R.id.iv_vip).setVisibility(View.GONE);
            findViewById(R.id.tv_vip_hint).setVisibility(View.GONE);
        }
    }

}
