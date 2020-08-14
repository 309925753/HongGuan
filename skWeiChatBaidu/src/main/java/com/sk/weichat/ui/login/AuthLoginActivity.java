package com.sk.weichat.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.LogUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.HeadView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;

import okhttp3.Call;

/**
 * 用于新设备登录时老设备验证登录，
 */
public class AuthLoginActivity extends BaseActivity {
    public static void start(Context ctx, String authKey) {
        Intent intent = new Intent(ctx, AuthLoginActivity.class);
        intent.putExtra("authKey", authKey);
        // ctx可能不是activity,
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        findViewById(R.id.iv_title_left).setOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        LogUtils.log(TAG, intent);

        String authKey = intent.getStringExtra("authKey");
        findViewById(R.id.login_btn).setOnClickListener(v -> {
            authLogin(authKey);
        });
        findViewById(R.id.tv_cancel_login).setOnClickListener(v -> {
            finish();
        });

        String name = coreManager.getSelf().getNickName();
        String phone = coreManager.getSelf().getTelephoneNoAreaCode();
        String userId = coreManager.getSelf().getUserId();
        TextView tvName = findViewById(R.id.tvName);
        TextView tvPhone = findViewById(R.id.tvPhone);
        HeadView hvHead = findViewById(R.id.hvHead);

        tvName.setText(name);
        tvPhone.setText(phone);
        AvatarHelper.getInstance().displayAvatar(name, userId, hvHead.getHeadImage(), true);
    }

    private void authLogin(String authKey) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("authKey", authKey);

        HttpUtils.get().url(coreManager.getConfig().ACCESS_AUTH_LOGIN)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(mContext);
                    }
                });
    }
}
