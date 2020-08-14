package com.sk.weichat.ui.me;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.sk.weichat.R;
import com.sk.weichat.ui.base.BaseActivity;
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

public class DescriptionActivity extends BaseActivity {
    private String description;
    private TextView tv_title_right;
    private EditText et_desc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.personalized_signature);
        tv_title_right = (TextView) findViewById(R.id.tv_title_right);
        tv_title_right.setAlpha(0.6f);
        tv_title_right.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_grey_circle));
        ViewCompat.setBackgroundTintList(tv_title_right, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
        tv_title_right.setTextColor(getResources().getColor(R.color.white));
        tv_title_right.setText(getResources().getString(R.string.save));
        tv_title_right.setOnClickListener(v -> {
            description = et_desc.getText().toString().trim();
            if (TextUtils.isEmpty(description)) {
                ToastUtil.showToast(mContext, getString(R.string.name_connot_null));
                return;
            }
            if (!description.equals(coreManager.getSelf().getDescription())) {
                loadDescription(description);
            } else {
                finish();
            }
        });

    }

    private void initView() {
        et_desc = findViewById(R.id.et_desc);
        String defaultDesc = PreferenceUtils.getString(DescriptionActivity.this, "description", coreManager.getSelf().getDescription());
        et_desc.setHint(defaultDesc);
        et_desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("zx", "onTextChanged: " + start + " before:  " + before + " count: " + count);
                if (count == 0 && start == 0 && et_desc.getText().toString().trim().equals(coreManager.getSelf().getDescription())) {
                    tv_title_right.setAlpha(0.6f);
                } else {
                    tv_title_right.setAlpha(1f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadDescription(String description) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("description", description);

        HttpUtils.get().url(coreManager.getConfig().USER_DESCRIPTION)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            PreferenceUtils.putString(DescriptionActivity.this, "description", description);
                            ToastUtil.showToast(DescriptionActivity.this, getString(R.string.save_success));
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(DescriptionActivity.this);
                    }
                });
    }
}
