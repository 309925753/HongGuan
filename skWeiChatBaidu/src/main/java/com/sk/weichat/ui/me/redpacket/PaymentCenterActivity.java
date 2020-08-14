package com.sk.weichat.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.sk.weichat.R;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.view.SkinImageView;
import com.sk.weichat.view.SkinTextView;

public class PaymentCenterActivity extends BaseActivity implements View.OnClickListener {

    private SkinImageView iv_title_left;
    private SkinTextView tv_title_center;
    private RelativeLayout bill;
    private RelativeLayout setting_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_center);
        initView();
    }

    private void initView() {
        getSupportActionBar().hide();
        iv_title_left = (SkinImageView) findViewById(R.id.iv_title_left);
        tv_title_center = (SkinTextView) findViewById(R.id.tv_title_center);
        tv_title_center.setText(getResources().getString(R.string.payment_center));
        tv_title_center.setTextColor(getResources().getColor(R.color.black));
        bill = (RelativeLayout) findViewById(R.id.bill);
        setting_password = (RelativeLayout) findViewById(R.id.setting_password);

        iv_title_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        bill.setOnClickListener(this::onClick);
        setting_password.setOnClickListener(this::onClick);

        findViewById(R.id.reset_password).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.bill:
                intent = new Intent(PaymentCenterActivity.this, MyConsumeRecordNew.class);
                break;
            case R.id.setting_password:
                intent = new Intent(PaymentCenterActivity.this, ChangePayPasswordActivity.class);
                break;
            case R.id.reset_password:
                intent = new Intent(PaymentCenterActivity.this, ResetPayPasswordActivity.class);
                break;
        }
        startActivity(intent);

    }
}
