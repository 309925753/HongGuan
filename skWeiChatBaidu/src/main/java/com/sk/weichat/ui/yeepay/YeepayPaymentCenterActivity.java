package com.sk.weichat.ui.yeepay;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.sk.weichat.R;
import com.sk.weichat.helper.YeepayHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.view.SkinImageView;
import com.sk.weichat.view.SkinTextView;

public class YeepayPaymentCenterActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yeepay_payment_center);
        initView();
    }

    private void initView() {
        getSupportActionBar().hide();
        SkinImageView iv_title_left = (SkinImageView) findViewById(R.id.iv_title_left);
        SkinTextView tv_title_center = (SkinTextView) findViewById(R.id.tv_title_center);
        tv_title_center.setText(getResources().getString(R.string.payment_center));
        tv_title_center.setTextColor(getResources().getColor(R.color.black));
        RelativeLayout yeepay_bind = (RelativeLayout) findViewById(R.id.yeepay_bind);
        RelativeLayout yeepay_secure = (RelativeLayout) findViewById(R.id.yeepay_secure);
        RelativeLayout bill = (RelativeLayout) findViewById(R.id.bill);

        iv_title_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        yeepay_bind.setOnClickListener(this);
        yeepay_secure.setOnClickListener(this);
        bill.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bill:
                YeepayRecordActivity.start(mContext);
                break;
            case R.id.yeepay_bind:
                YeepayHelper.bind(mContext, coreManager);
                break;
            case R.id.yeepay_secure:
                YeepayHelper.secure(mContext, coreManager);
                break;
        }

    }
}
