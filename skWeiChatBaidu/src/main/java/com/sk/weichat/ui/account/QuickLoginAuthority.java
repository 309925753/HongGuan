package com.sk.weichat.ui.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.bean.WebCallback;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.share.ShareConstant;
import com.sk.weichat.ui.share.ShareLoginActivity;
import com.sk.weichat.ui.tool.WebViewActivity;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.PreferenceUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import okhttp3.Call;
import okhttp3.HttpUrl;

/**
 * 外部浏览器调起当前界面 授权
 */
public class QuickLoginAuthority extends BaseActivity {
    private boolean isNeedExecuteLogin;
    private String mShareContent;

    public QuickLoginAuthority() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_result);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // 已进入授权界面
        ShareConstant.IS_SHARE_QL_COME = true;

        Uri data = getIntent().getData();
        if (data != null) {
            mShareContent = data.toString();
        }
        if (TextUtils.isEmpty(mShareContent)) {// 外部跳转进入
            mShareContent = ShareConstant.ShareContent;
        } else {// 数据下载页面进入
            ShareConstant.ShareContent = mShareContent;
        }

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
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        TextView mTvTitleLeft = findViewById(R.id.tv_title_left);
        mTvTitleLeft.setText(getString(R.string.close));
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.centent_bar, getString(R.string.app_name)));
        mTvTitleLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        ImageView mAppIconIv = findViewById(R.id.app_icon_iv);
        TextView mAppNameTv = findViewById(R.id.app_name_tv);
        String webAppName = WebViewActivity.URLRequest(mShareContent).get("webAppName");
        String webAppsmallImg = WebViewActivity.URLRequest(mShareContent).get("webAppsmallImg");
        if (!TextUtils.isEmpty(webAppName)) {
            mAppNameTv.setText(webAppName);
        }
        if (!TextUtils.isEmpty(webAppsmallImg)) {
            AvatarHelper.getInstance().displayUrl(webAppsmallImg, mAppIconIv);
        }

        // 授权登录按钮
        findViewById(R.id.login_btn).setOnClickListener(v -> onAuthLogin(mShareContent));
    }

    /**
     * 授权登录
     */
    private void onAuthLogin(String url) {
        Log.e("onResponse", "onAuthLogin: " + url);

        String appId = WebViewActivity.URLRequest(url).get("appId");
        String redirectURL = WebViewActivity.URLRequest(url).get("callbackUrl");

        HttpUtils.get().url(coreManager.getConfig().AUTHOR_CHECK)
                .params("appId", appId)
                .params("state", coreManager.getSelfStatus().accessToken)
                .params("callbackUrl", redirectURL)
                .build().execute(new BaseCallback<WebCallback>(WebCallback.class) {

            @Override
            public void onResponse(ObjectResult<WebCallback> result) {
                if (Result.checkSuccess(mContext, result) && result.getData() != null) {
                    String html = HttpUrl.parse(result.getData().getCallbackUrl()).newBuilder()
                            .addQueryParameter("code", result.getData().getCode())
                            .build()
                            .toString();
                    Uri uri = Uri.parse(html);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }

            @Override
            public void onError(Call call, Exception e) {

            }
        });
    }
}

