package com.redchamber.login;

import android.content.Intent;
import android.view.View;

import com.redchamber.lib.base.BaseActivity;
import com.sk.weichat.R;

import butterknife.OnClick;

/**
 * 登录注册
 */
public class LoginRegisterActivity extends BaseActivity {

    @Override
    protected int setLayout() {
        return R.layout.activity_login_register;
    }

    @Override
    protected void initView() {
    }

    @OnClick({R.id.btn_login, R.id.btn_register})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                startActivity(new Intent(this, AccountLoginActivity.class));
                break;
            case R.id.btn_register:
                startActivity(new Intent(this, PhoneRegisterActivity.class));
                break;
        }
    }

}
