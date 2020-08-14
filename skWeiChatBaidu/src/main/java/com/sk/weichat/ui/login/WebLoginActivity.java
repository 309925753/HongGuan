package com.sk.weichat.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.sk.weichat.R;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.LogUtils;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.HeadView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;

import okhttp3.Call;
import okhttp3.HttpUrl;

/**
 * 用于其他平台拉起app登录，
 */
public class WebLoginActivity extends BaseActivity {
    private static final String QR_CODE_ACTION_WEB_LOGIN = "webLogin";
    private String qrCodeKey;

    public static void start(Context ctx, String qrCodeResult) {
        Intent intent = new Intent(ctx, WebLoginActivity.class);
        intent.putExtra("qrCodeResult", qrCodeResult);
        ctx.startActivity(intent);
    }

    /**
     * 检查这个二维码是不是用于其他平台登录的，
     */
    public static boolean checkQrCode(String qrCodeResult) {
        return qrCodeResult.contains("action=" + QR_CODE_ACTION_WEB_LOGIN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h5_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        findViewById(R.id.iv_title_left).setOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        LogUtils.log(TAG, intent);

        String qrCodeResult = intent.getStringExtra("qrCodeResult");
        HttpUrl httpUrl = HttpUrl.parse(qrCodeResult);
        qrCodeKey = httpUrl.queryParameter("qrCodeKey");
        scan(qrCodeKey);
        Button login_btn = findViewById(R.id.login_btn);
        login_btn.setOnClickListener(v -> {
            login(qrCodeKey);
        });
        ViewCompat.setBackgroundTintList(login_btn, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
        findViewById(R.id.tv_cancel_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
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

    private void scan(String qrCodeKey) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("qrCodeKey", qrCodeKey);
        params.put("type", String.valueOf(1));

        HttpUtils.get().url(coreManager.getConfig().QR_CODE_LOGIN)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (!Result.checkSuccess(mContext, result)) {
                            // 二维码有问题直接关闭这个页面，
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    private void login(String qrCodeKey) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("qrCodeKey", qrCodeKey);
        params.put("type", String.valueOf(2));

        HttpUtils.get().url(coreManager.getConfig().QR_CODE_LOGIN)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            ToastUtil.showToast(mContext, R.string.tip_web_login_success);
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
