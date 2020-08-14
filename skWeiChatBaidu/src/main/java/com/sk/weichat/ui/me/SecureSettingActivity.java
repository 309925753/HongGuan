package com.sk.weichat.ui.me;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.sk.weichat.AppConfig;
import com.sk.weichat.R;
import com.sk.weichat.bean.PrivacySetting;
import com.sk.weichat.broadcast.OtherBroadcast;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.PrivacySettingHelper;
import com.sk.weichat.map.MapHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.lock.ChangeDeviceLockPasswordActivity;
import com.sk.weichat.ui.lock.DeviceLockActivity;
import com.sk.weichat.ui.lock.DeviceLockHelper;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.SwitchButton;
import com.sk.weichat.view.TipDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

public class SecureSettingActivity extends BaseActivity {

    public static final int REQUEST_DISABLE_LOCK = 1;
    SwitchButton.OnCheckedChangeListener onCheckedChangeListener = new SwitchButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(SwitchButton view, boolean isChecked) {
            switch (view.getId()) {
                case R.id.mSbVerify:
                    submitPrivacySetting(1, isChecked);
                    break;
                case R.id.mSbEncrypt:
                    submitPrivacySetting(2, isChecked);
                    break;
                case R.id.mSbzhendong:
                    submitPrivacySetting(3, isChecked);
                    break;
                case R.id.mSbInputState:
                    submitPrivacySetting(4, isChecked);
                    break;
                case R.id.sb_google_map:
                    submitPrivacySetting(5, isChecked);
                    break;
                case R.id.mSbSupport:
                    submitPrivacySetting(6, isChecked);
                    break;
                case R.id.mSbKeepLive:
                    submitPrivacySetting(7, isChecked);
                    break;
                case R.id.sbPhoneSearch:
                    submitPrivacySetting(8, isChecked);
                    break;
                case R.id.sbNameSearch:
                    submitPrivacySetting(9, isChecked);
                    break;
                case R.id.sbAuthLogin:
                    submitPrivacySetting(10, isChecked);
                    break;
            }
        }
    };
    private SwitchButton sbDeviceLock;
    private SwitchButton sbDeviceLockFree;
    private View llDeviceLockDetail;
    private View rlChangeDeviceLockPassword;
    private SwitchButton mSbAuthLogin;    // 旧设备授权登录，

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_setting);
        initActionBar();
        initView();
        getPrivacySetting();

        sbDeviceLock.setOnCheckedChangeListener((view, isChecked) -> {
            if (!isChecked) {
                DeviceLockActivity.verify(this, REQUEST_DISABLE_LOCK);
                return;
            }
            rlChangeDeviceLockPassword.setVisibility(View.VISIBLE);
            ChangeDeviceLockPasswordActivity.start(this);
        });
        rlChangeDeviceLockPassword.setOnClickListener(v -> {
            ChangeDeviceLockPasswordActivity.start(this);
        });
        sbDeviceLockFree.setChecked(DeviceLockHelper.isAutoLock());
        sbDeviceLockFree.setOnCheckedChangeListener((view, isChecked) -> {
            DeviceLockHelper.setAutoLock(isChecked);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDeviceLockSettings();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        switch (requestCode) {
            case REQUEST_DISABLE_LOCK:
                DeviceLockHelper.clearPassword();
                updateDeviceLockSettings();
                break;
        }

    }

    private void updateDeviceLockSettings() {
        boolean enabled = DeviceLockHelper.isEnabled();
        sbDeviceLock.setChecked(enabled);
        if (enabled) {
            llDeviceLockDetail.setVisibility(View.VISIBLE);
        } else {
            llDeviceLockDetail.setVisibility(View.GONE);
        }
        boolean autoLock = DeviceLockHelper.isAutoLock();
        sbDeviceLockFree.setChecked(autoLock);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.secure_settings);
    }

    private void initView() {
        mSbAuthLogin = findViewById(R.id.sbAuthLogin);
        sbDeviceLock = findViewById(R.id.sbDeviceLock);
        sbDeviceLockFree = findViewById(R.id.sbDeviceLockFree);
        llDeviceLockDetail = findViewById(R.id.llDeviceLockDetail);
        rlChangeDeviceLockPassword = findViewById(R.id.rlChangeDeviceLockPassword);
        if (coreManager.getConfig().enableAuthLogin) {
            findViewById(R.id.rlAuthLogin).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.rlAuthLogin).setVisibility(View.GONE);
        }
    }

    // 获取用户的设置状态
    private void getPrivacySetting() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().USER_GET_PRIVACY_SETTING)
                .params(params)
                .build()
                .execute(new BaseCallback<PrivacySetting>(PrivacySetting.class) {

                    @Override
                    public void onResponse(ObjectResult<PrivacySetting> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            PrivacySetting settings = result.getData();
                            PrivacySettingHelper.setPrivacySettings(mContext, settings);
                        }

                        initStatus();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(mContext);

                        initStatus();
                    }
                });
    }

    private void initStatus() {
        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(this);

        mSbAuthLogin.setChecked(privacySetting.getAuthSwitch() == 1);

        mSbAuthLogin.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSbAuthLogin.setOnCheckedChangeListener(onCheckedChangeListener);
            }
        }, 200);
    }

    // 提交设置用户的状态
    private void submitPrivacySetting(final int index, final boolean isChecked) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        String status = isChecked ? "1" : "0";
        if (index == 1) {
            params.put("friendsVerify", status);
        } else if (index == 2) {
            params.put("isEncrypt", status);
        } else if (index == 3) {
            params.put("isVibration", status);
        } else if (index == 4) {
            params.put("isTyping", status);
        } else if (index == 5) {
            params.put("isUseGoogleMap", status);
        } else if (index == 6) {
            params.put("multipleDevices", status);
        } else if (index == 7) {
            params.put("isKeepalive", status);
        } else if (index == 8) {
            params.put("phoneSearch", status);
        } else if (index == 9) {
            params.put("nameSearch", status);
        } else if (index == 10) {
            params.put("authSwitch", status);
        }
        DialogHelper.showDefaulteMessageProgressDialog(this);
        TipDialog tipDialog = new TipDialog(this);
        HttpUtils.get().url(coreManager.getConfig().USER_SET_PRIVACY_SETTING)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            Toast.makeText(mContext, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                            PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(mContext);
                            int value = Integer.parseInt(status);
                            if (index == 2) {
                                privacySetting.setIsEncrypt(value);
                            } else if (index == 3) {
                                privacySetting.setIsVibration(value);
                            } else if (index == 4) {
                                privacySetting.setIsTyping(value);
                            } else if (index == 5) {
                                privacySetting.setIsUseGoogleMap(value);
                                if (isChecked) {
                                    MapHelper.setMapType(MapHelper.MapType.GOOGLE);
                                } else {
                                    MapHelper.setMapType(MapHelper.MapType.BAIDU);
                                }
                            } else if (index == 6) {
                                privacySetting.setMultipleDevices(value);
                                tipDialog.setmConfirmOnClickListener(getString(R.string.multi_login_need_reboot), new TipDialog.ConfirmOnClickListener() {
                                    @Override
                                    public void confirm() {
                                    }
                                });
                                tipDialog.show();
                            } else if (index == 7) {
                                privacySetting.setIsKeepalive(value);
                                tipDialog.setmConfirmOnClickListener(getString(R.string.update_success_restart), new TipDialog.ConfirmOnClickListener() {
                                    @Override
                                    public void confirm() {
                                        Intent intent = new Intent(OtherBroadcast.BROADCASTTEST_ACTION);
                                        intent.setComponent(new ComponentName(AppConfig.sPackageName, AppConfig.myBroadcastReceiverClass));
                                        sendBroadcast(intent);
                                    }
                                });
                                tipDialog.show();
                            } else if (index == 8) {
                                privacySetting.setPhoneSearch(value);
                            } else if (index == 9) {
                                privacySetting.setNameSearch(value);
                            }
                            PrivacySettingHelper.setPrivacySettings(mContext, privacySetting);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(mContext);
                    }
                });
    }
}
