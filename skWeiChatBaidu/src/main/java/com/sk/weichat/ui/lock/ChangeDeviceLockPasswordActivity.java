package com.sk.weichat.ui.lock;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.Md5Util;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.PasswordInputView;

public class ChangeDeviceLockPasswordActivity extends BaseActivity {
    private boolean needOldPassword = true;
    private boolean needTwice = true;
    private String newPassword;
    private TextView tvTip;
    private String oldPassword;

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, ChangeDeviceLockPasswordActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_device_lock_password);

        initActionBar();
        initView();
        initData();
    }

    private void initData() {
        String userId = coreManager.getSelf().getUserId();
        if (TextUtils.isEmpty(userId)) {
            ToastUtil.showToast(this, R.string.tip_no_user_id);
            finish();
            return;
        }
        // 如果没有设置过密码，就不需要输入旧密码，
        oldPassword = DeviceLockHelper.getPassword();
        needOldPassword = !TextUtils.isEmpty(oldPassword);
        Log.d(TAG, "initData: oldPassword = " + oldPassword);
        TextView tvTitle = findViewById(R.id.tv_title_center);
        TextView tvAction = findViewById(R.id.tvAction);
        ((TextView) findViewById(R.id.tv_title_center)).setText(getString(R.string.change_password));
        ((TextView) findViewById(R.id.tvAction)).setText(getString(R.string.set_device_lock_password));
        if (!needOldPassword) {
            // 如果不需要旧密码，直接传空字符串，
            oldPassword = "";
            tvTip.setText(R.string.tip_change_device_lock_password_input_new);
            tvTitle.setText(R.string.set_device_lock_password);
            tvAction.setText(R.string.set_device_lock_password);
        } else {
            tvTitle.setText(R.string.change_device_lock_password);
            tvAction.setText(R.string.change_device_lock_password);
        }
    }

    private void initView() {
        tvTip = findViewById(R.id.tvTip);
        final TextView tvFinish = findViewById(R.id.tvFinish);
        tvFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceLockHelper.setPassword(newPassword);
                finish();
            }
        });
        final PasswordInputView passwordInputView = findViewById(R.id.passwordInputView);
        passwordInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvFinish.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable psw) {
                if (psw.length() == AppConstant.PASS_WORD_LENGTH) {
                    if (needOldPassword) {
                        passwordInputView.setText("");
                        if (TextUtils.equals(oldPassword, Md5Util.toMD5(psw.toString()))) {
                            needOldPassword = false;
                            tvTip.setText(R.string.tip_change_device_lock_password_input_new);
                        } else {
                            tvTip.setText(R.string.tip_device_lock_password_incorrect);
                        }
                    } else if (needTwice) {
                        needTwice = false;
                        newPassword = psw.toString();
                        passwordInputView.setText("");
                        tvTip.setText(R.string.tip_change_device_lock_password_input_twice);
                    } else if (psw.toString().equals(newPassword)) {
                        // 二次确认成功，
                        tvFinish.setVisibility(View.VISIBLE);
                    } else {
                        // 二次确认失败，重新输入新密码，
                        passwordInputView.setText("");
                        needTwice = true;
                        tvTip.setText(R.string.tip_change_device_lock_password_input_incorrect);
                        tvFinish.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(getString(R.string.change_password));
    }
}
