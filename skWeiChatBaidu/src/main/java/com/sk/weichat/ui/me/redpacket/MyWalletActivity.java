package com.sk.weichat.ui.me.redpacket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.helper.YeepayHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.yeepay.YeepayOpenActivity;
import com.sk.weichat.ui.yeepay.YeepayWallet;

public class MyWalletActivity extends BaseActivity {
    public static void start(Context context) {
        if (YeepayHelper.ENABLE) {
            Intent starter = new Intent(context, MyWalletActivity.class);
            context.startActivity(starter);
        } else {
            context.startActivity(new Intent(context, WxPayBlance.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wallet);
        initActionBar();
        initView();
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
        mTvTitle.setText(getString(R.string.my_purse));
    }

    private void initView() {
        findViewById(R.id.my_change).setOnClickListener(v -> {
            startActivity(new Intent(this, WxPayBlance.class));
        });
        findViewById(R.id.my_cloud_wallet).setOnClickListener(v -> {
            if (YeepayHelper.isOpened(this)) {
                YeepayWallet.start(this);
            } else {
                YeepayOpenActivity.start(this);
            }
        });
        findViewById(R.id.bank_card).setOnClickListener(v -> {
            YeepayHelper.bind(mContext, coreManager);
        });
    }
}
