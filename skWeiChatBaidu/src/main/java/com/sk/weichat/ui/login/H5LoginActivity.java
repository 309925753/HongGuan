package com.sk.weichat.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sk.weichat.AppConfig;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.DES;
import com.sk.weichat.util.LogUtils;
import com.sk.weichat.util.Md5Util;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.HeadView;

import java.net.URL;

import okhttp3.HttpUrl;

/**
 * 用于h5拉起app登录，
 */
public class H5LoginActivity extends BaseActivity {
    public static void start(Context ctx, String callback) {
        Intent intent = new Intent(ctx, H5LoginActivity.class);
        intent.putExtra("callback", callback);
        ctx.startActivity(intent);
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

        String callback = intent.getStringExtra("callback");
        Button login_btn = findViewById(R.id.login_btn);
        login_btn.setOnClickListener(v -> {
            browser(callback);
            finish();
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

    private void browser(String callback) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("accessToken", coreManager.getSelfStatus().accessToken);
            jsonObject.put("telephone", coreManager.getSelf().getTelephone());
            jsonObject.put("password", coreManager.getSelf().getPassword());
            String json = jsonObject.toJSONString();
            String key = Md5Util.toMD5(AppConfig.apiKey);
            String encrypted = DES.encryptDES(json, key);
            Log.i(TAG, String.format("callback (json: %s), (key, %s), (encrypted: %s)", json, key, encrypted));
            String callbackUrl = JSON.parseObject(callback)
                    .getString("callbackUrl");
            HttpUrl url = HttpUrl.get(new URL(callbackUrl))
                    .newBuilder()
                    .addQueryParameter("data", encrypted)
                    .build();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
            startActivity(intent);
        } catch (Exception e) {
            // 无论如何不能在这里崩溃，
            Reporter.post("js登录回调失败", e);
            ToastUtil.showToast(this, e.getMessage());
        }
    }
}
