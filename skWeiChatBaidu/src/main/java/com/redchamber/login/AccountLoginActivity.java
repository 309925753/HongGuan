package com.redchamber.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.redchamber.bean.BaiduFaceBean;
import com.redchamber.bean.BaiduFaceMatchBean;
import com.redchamber.face.FaceLivenessExpActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.util.EditTextUtils;
import com.redchamber.util.GsonUtils;
import com.redchamber.util.HttpUtil;
import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.LoginRegisterResult;
import com.sk.weichat.bean.event.MessageLogin;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.helper.LoginSecureHelper;
import com.sk.weichat.helper.PrivacySettingHelper;
import com.sk.weichat.helper.YeepayHelper;
import com.sk.weichat.ui.MainActivity;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.DeviceInfoUtil;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.secure.LoginPassword;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;

/**
 * 登陆
 */
public class AccountLoginActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.et_phone)
    EditText mEtPhone;
    @BindView(R.id.et_password)
    EditText mEtPassword;

    private int mobilePrefix = 86;

    private String mCertificateInfo, mBaiduAiToken;
    private int mUserStatus;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_login);
        PreferenceUtils.putBoolean(this, Constants.LOGIN_CONFLICT, false);// 重置登录冲突记录
        initView();

        EventBus.getDefault().register(this);
    }

    public AccountLoginActivity() {
        noLoginRequired();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mCompositeDisposable.clear();
    }

    private void initView() {
        mEtPhone = findViewById(R.id.et_phone);
        mEtPassword = findViewById(R.id.et_password);
        mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.tv_forget).setOnClickListener(this);
        findViewById(R.id.btn_login).setOnClickListener(this);
        mEtPhone.setText(com.redchamber.lib.utils.PreferenceUtils.getMobilePhone());
        EditTextUtils.setEditTextInhibitInputSpace(mEtPhone, 11);
        EditTextUtils.setEditTextInhibitInputSpace(mEtPassword, 11);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageLogin message) {
        finish();
    }

    @OnClick({R.id.iv_back, R.id.tv_forget, R.id.btn_login})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_forget:
                startActivity(new Intent(this, ResetPasswordActivity.class));
                break;
            case R.id.btn_login:
                login();
                break;
        }
    }

    private void login() {
        PreferenceUtils.putInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        String phoneNumber = mEtPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)) {
            ToastUtils.showToast("请输入手机号");
            return;
        }
        String password = mEtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            ToastUtils.showToast("请输入密码");
            return;
        }
        final String digestPwd = LoginPassword.encodeMd5(password);

        DialogHelper.showDefaulteMessageProgressDialog(this);
        HashMap<String, String> params = new HashMap<>();
        params.put("xmppVersion", "1");
        // 附加信息
        params.put("model", DeviceInfoUtil.getModel());
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        params.put("serial", DeviceInfoUtil.getDeviceId(this));
        // 地址信息
        double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));

        if (MyApplication.IS_OPEN_CLUSTER) {// 服务端集群需要
            String area = PreferenceUtils.getString(this, AppConstant.EXTRA_CLUSTER_AREA);
            if (!TextUtils.isEmpty(area)) {
                params.put("area", area);
            }
        }

        LoginSecureHelper.secureLogin(
                this, coreManager, String.valueOf(mobilePrefix), phoneNumber, password,
                params,
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(this, this.getString(R.string.tip_login_secure_place_holder, t.getMessage()));
                }, result -> {
                    DialogHelper.dismissProgressDialog();
                    if (!Result.checkSuccess(getApplicationContext(), result)) {
                        return;
                    }
                    if (!TextUtils.isEmpty(result.getData().getAuthKey())) {
                        DialogHelper.showMessageProgressDialog(mContext, getString(R.string.tip_need_auth_login));
                        CheckAuthLoginRunnable authLogin = new CheckAuthLoginRunnable(result.getData().getAuthKey(), phoneNumber, digestPwd);
                        waitAuth(authLogin);
                        return;
                    }
                    afterLogin(result, phoneNumber, digestPwd);
                });
    }

    private class CheckAuthLoginRunnable implements Runnable {
        private final String phoneNumber;
        private final String digestPwd;
        private Handler waitAuthHandler = new Handler();
        private int waitAuthTimes = 10;
        private String authKey;

        public CheckAuthLoginRunnable(String authKey, String phoneNumber, String digestPwd) {
            this.authKey = authKey;
            this.phoneNumber = phoneNumber;
            this.digestPwd = digestPwd;
        }

        @Override
        public void run() {
            HttpUtils.get().url(coreManager.getConfig().CHECK_AUTH_LOGIN)
                    .params("authKey", authKey)
                    .build(true, true)
                    .execute(new BaseCallback<LoginRegisterResult>(LoginRegisterResult.class) {
                        @Override
                        public void onResponse(ObjectResult<LoginRegisterResult> result) {
                            if (com.xuan.xuanhttplibrary.okhttp.result.Result.checkError(result, com.xuan.xuanhttplibrary.okhttp.result.Result.CODE_AUTH_LOGIN_SCUESS)) {
                                DialogHelper.dismissProgressDialog();
                                login();
                            } else if (com.xuan.xuanhttplibrary.okhttp.result.Result.checkError(result, com.xuan.xuanhttplibrary.okhttp.result.Result.CODE_AUTH_LOGIN_FAILED_1)) {
                                waitAuth(CheckAuthLoginRunnable.this);
                            } else {
                                DialogHelper.dismissProgressDialog();
                                if (!TextUtils.isEmpty(result.getResultMsg())) {
                                    ToastUtil.showToast(mContext, result.getResultMsg());
                                } else {
                                    ToastUtil.showToast(mContext, R.string.tip_server_error);
                                }
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            DialogHelper.dismissProgressDialog();
                            ToastUtil.showErrorNet(mContext);
                        }
                    });
        }
    }

    private void waitAuth(CheckAuthLoginRunnable authLogin) {
        authLogin.waitAuthHandler.postDelayed(authLogin, 3000);
    }

    private void afterLogin(ObjectResult<LoginRegisterResult> result, String phoneNumber, String digestPwd) {
        boolean success = LoginHelper.setLoginUser(mContext, coreManager, phoneNumber, digestPwd, result);
        if (success) {
            LoginRegisterResult.Settings settings = result.getData().getSettings();
            MyApplication.getInstance().initPayPassword(result.getData().getUserId(), result.getData().getPayPassword());
            YeepayHelper.saveOpened(mContext, result.getData().getWalletUserNo() == 1);
            PrivacySettingHelper.setPrivacySettings(this, settings);
            MyApplication.getInstance().initMulti();
            com.redchamber.lib.utils.PreferenceUtils.saveMobilePhone(phoneNumber);
            mUserStatus = result.getData().getUserStatus();

            if (!TextUtils.isEmpty(result.getData().getCertificateInfo()) &&
                    !TextUtils.isEmpty(result.getData().getBaiduAiToken())) {
                mCertificateInfo = result.getData().getCertificateInfo();
                mBaiduAiToken = result.getData().getBaiduAiToken();
                Intent intent = new Intent(this, FaceLivenessExpActivity.class);
                startActivityForResult(intent, 100);
            } else {
                jump();
            }
        } else {
            // 登录失败
            String message = TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.login_failed) : result.getResultMsg();
            ToastUtil.showToast(mContext, message);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 100:
                if (data != null) {
                    String image = data.getStringExtra("image");
                    faceMatch(image);
                }
                break;
        }
    }

    private void jump() {
        // 登陆成功
        EventBus.getDefault().post(new MessageLogin());
        if (1 == mUserStatus) {
            MainActivity.start(mContext);
        } else {
            //去补填邀请码，或者开通vip
            InvitationCodeActivity.startActivity(mContext, InvitationCodeActivity.TYPE_REGISTERED_NO_CODE);
        }
        finish();
    }

    private void faceMatch(String image) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        String url = "https://aip.baidubce.com/rest/2.0/face/v3/match";

        final Observable<BaiduFaceMatchBean> observable = Observable.create(new ObservableOnSubscribe<BaiduFaceMatchBean>() {

            @Override
            public void subscribe(ObservableEmitter<BaiduFaceMatchBean> e) throws Exception {
                List<BaiduFaceBean> list = new ArrayList<>();
                BaiduFaceBean baiduFaceBean1 = new BaiduFaceBean();
                baiduFaceBean1.image = image;
                baiduFaceBean1.image_type = "BASE64";
                baiduFaceBean1.face_type = "LIVE";
                baiduFaceBean1.quality_control = "LOW";
                baiduFaceBean1.liveness_control = "HIGH";

                BaiduFaceBean baiduFaceBean2 = new BaiduFaceBean();
                baiduFaceBean2.image = mCertificateInfo;
                baiduFaceBean2.image_type = "BASE64";
                baiduFaceBean2.face_type = "LIVE";
                baiduFaceBean2.quality_control = "LOW";
                baiduFaceBean2.liveness_control = "HIGH";

                list.add(baiduFaceBean1);
                list.add(baiduFaceBean2);

                String param = GsonUtils.toJson(list);
                String accessToken = mBaiduAiToken;

                String result = HttpUtil.post(url, accessToken, "application/json", param);
                BaiduFaceMatchBean entity = new Gson().fromJson(result, BaiduFaceMatchBean.class);
                e.onNext(entity);
                e.onComplete();
            }

        });
        DisposableObserver<BaiduFaceMatchBean> disposableObserver = new DisposableObserver<BaiduFaceMatchBean>() {

            @Override
            public void onNext(BaiduFaceMatchBean value) {
                if (value != null && value.result != null && value.result.score >= 80) {
                    ToastUtils.showToast("人脸识别通过");
                    jump();
                } else {
                    ToastUtils.showToast("人脸识别未通过");
                }
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showToast(e.getMessage());
                DialogHelper.dismissProgressDialog();
            }

            @Override
            public void onComplete() {
                DialogHelper.dismissProgressDialog();
            }
        };
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(disposableObserver);
        mCompositeDisposable.add(disposableObserver);
    }

}
