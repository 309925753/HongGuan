package com.sk.weichat.ui.share;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sk.weichat.AppConfig;
import com.sk.weichat.R;
import com.sk.weichat.bean.SKLoginBean;
import com.sk.weichat.bean.SKLoginResultBean;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.DeviceInfoUtil;
import com.sk.weichat.util.Md5Util;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 分享 最近联系人
 */
public class AuthorizationActivity extends BaseActivity {
    private String mShareContent;
    private SKLoginBean mSKLoginBean;

    private boolean isNeedExecuteLogin;

    public AuthorizationActivity() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_result);

        // 已进入授权界面
        ShareConstant.IS_SHARE_L_COME = true;

        mShareContent = getIntent().getStringExtra(ShareConstant.EXTRA_SHARE_CONTENT);
        if (TextUtils.isEmpty(mShareContent)) {// 外部跳转进入
            mShareContent = ShareConstant.ShareContent;
        } else {// 数据下载页面进入
            ShareConstant.ShareContent = mShareContent;
        }
        mSKLoginBean = JSON.parseObject(mShareContent, SKLoginBean.class);

        // 判断本地登录状态
        int userStatus = LoginHelper.prepareUser(mContext, coreManager);
        switch (userStatus) {
            case LoginHelper.STATUS_USER_FULL:
            case LoginHelper.STATUS_USER_NO_UPDATE:
            case LoginHelper.STATUS_USER_TOKEN_OVERDUE:
                boolean isConflict = PreferenceUtils.getBoolean(this, Constants.LOGIN_CONFLICT, false);
                if (isConflict) {
                    isNeedExecuteLogin = true;
                }
                break;
            case LoginHelper.STATUS_USER_SIMPLE_TELPHONE:
                isNeedExecuteLogin = true;
                break;
            case LoginHelper.STATUS_NO_USER:
            default:
                isNeedExecuteLogin = true;
        }

        if (isNeedExecuteLogin) {// 需要先执行登录操作
            startActivity(new Intent(mContext, ShareLoginActivity.class));
            finish();
            return;
        }

        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        TextView mTvTitleLeft = findViewById(R.id.tv_title_left);
        mTvTitleLeft.setText(getString(R.string.close));
        mTvTitleLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        ImageView mAppIconIv = findViewById(R.id.app_icon_iv);
        AvatarHelper.getInstance().displayUrl(mSKLoginBean.getAppIcon(), mAppIconIv);
        TextView mAppNameTv = findViewById(R.id.app_name_tv);
        mAppNameTv.setText(mSKLoginBean.getAppName());

        Button login_btn = findViewById(R.id.login_btn);
        login_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                verificationLogin();
            }
        });
        ViewCompat.setBackgroundTintList(login_btn, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
    }

    // 判断是否用户 '登录' 权限
    private void verificationLogin() {
        Map<String, String> params = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis() / 1000);
        String secret = Md5Util.toMD5(AppConfig.apiKey + mSKLoginBean.getAppId() + coreManager.getSelf().getUserId() +
                Md5Util.toMD5(coreManager.getSelfStatus().accessToken + time) + Md5Util.toMD5(mSKLoginBean.getAppSecret()));
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("type", String.valueOf(1));// 1.授权 2.分享 3.支付
        params.put("appId", mSKLoginBean.getAppId());
        params.put("appSecret", mSKLoginBean.getAppSecret());
        params.put("time", time);
        params.put("secret", secret);

        HttpUtils.get().url(coreManager.getConfig().SDK_OPEN_AUTH_INTERFACE)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            JSONObject jsonObject = JSONObject.parseObject(result.getData());
                            String userId = jsonObject.getString("userId");
                            loginResult(userId);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        // 网络异常
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    private void loginResult(String userId) {
        SKLoginResultBean mSKLoginResultBean = new SKLoginResultBean();
        mSKLoginResultBean.setUserId(userId);
        mSKLoginResultBean.setAvatarUrl(AvatarHelper.getAvatarUrl(coreManager.getSelf().getUserId(), true));
        mSKLoginResultBean.setNickName(coreManager.getSelf().getNickName());
        mSKLoginResultBean.setSex(coreManager.getSelf().getSex());
        mSKLoginResultBean.setBirthday(coreManager.getSelf().getBirthday());
        String skLoginResult = JSON.toJSONString(mSKLoginResultBean);

        // 这个action要和分享sdk接收的广播action相同，不能直接改，
        Intent intent = new Intent("android.intent.action.SK_Authorization");
        intent.putExtra(ShareConstant.EXTRA_AUTHORIZATION_RESULT, skLoginResult);
        sendBroadcast(intent);
        if (DeviceInfoUtil.isOppoRom()) {
            // 调试发现OPPO手机被调起后当前界面不会自动回到后台，手动调一下
            moveTaskToBack(true);
        }
        finish();
    }
}
