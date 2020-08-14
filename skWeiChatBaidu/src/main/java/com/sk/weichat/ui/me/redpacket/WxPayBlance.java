package com.sk.weichat.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.bean.event.EventNotifyByTag;
import com.sk.weichat.bean.redpacket.Balance;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.me.redpacket.scan.ScanRechargeActivity;
import com.sk.weichat.ui.me.redpacket.scan.ScanWithdrawActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.EventBusHelper;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.text.DecimalFormat;
import java.util.HashMap;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

public class WxPayBlance extends BaseActivity {

    public static final String RSA_PRIVATE = "";
    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_AUTH_FLAG = 2;
    private TextView mBalanceTv;
    private TextView mRechargeTv;
    private TextView mWithdrawTv;
    private TextView mScanRechargeTv;
    private TextView mScanWithdrawTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wx_pay_blance);
        initActionBar();
        initView();
        EventBusHelper.register(this);
    }

    @Override
    protected void onResume() {
        // todo 提现之后回到该界面，服务端待微信响应之后才会更新余额，此时调用刷新余额的方法获取到的可能还是之前的余额，另加一个EventBus来刷新吧
        super.onResume();
        initData();
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
        ImageView mImageView = findViewById(R.id.iv_title_right);
        mImageView.setImageDrawable(getResources().getDrawable(R.mipmap.navigation));
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 访问接口 获取记录
                Intent intent = new Intent(WxPayBlance.this, PaymentCenterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        mBalanceTv = (TextView) findViewById(R.id.myblance);
        mRechargeTv = (TextView) findViewById(R.id.chongzhi);
        mWithdrawTv = (TextView) findViewById(R.id.quxian);
        mScanRechargeTv = (TextView) findViewById(R.id.scan_recharge);
        mScanWithdrawTv = (TextView) findViewById(R.id.scan_withdraw);
        ButtonColorChange.rechargeChange(this, mRechargeTv, R.drawable.chongzhi_icon);
        ButtonColorChange.rechargeChange(this, mWithdrawTv, R.drawable.recharge_icon);
        ButtonColorChange.rechargeChange(this, mScanRechargeTv, R.drawable.chongzhi_icon);
        ButtonColorChange.rechargeChange(this, mScanWithdrawTv, R.drawable.recharge_icon);
        mWithdrawTv.setTextColor(SkinUtils.getSkin(this).getAccentColor());
        mScanWithdrawTv.setTextColor(SkinUtils.getSkin(this).getAccentColor());

        // 判断微信和支付宝是否都是关着的
        boolean isClose = (!coreManager.getConfig().enableWxPay && !coreManager.getConfig().enableAliPay);
        //如果判断为true，则说明都是关着的，提现和充值按钮都隐藏
        mRechargeTv.setVisibility(isClose ? View.GONE : View.VISIBLE);
        mWithdrawTv.setVisibility(isClose ? View.GONE : View.VISIBLE);
        if (coreManager.getConfig().isOpenManualPay) {
            mScanRechargeTv.setVisibility(View.VISIBLE);
            // 审核提现移至到提现内，如提现按钮被隐藏，审核提现则在外面显示
            mScanWithdrawTv.setVisibility(isClose ? View.VISIBLE : View.GONE);
        }

        /**
         * 充值、提现
         */
        mRechargeTv.setOnClickListener(view -> {
            Intent intent = new Intent(WxPayBlance.this, WxPayAdd.class);
            startActivity(intent);
        });

        mWithdrawTv.setOnClickListener(view -> {
            Intent intent = new Intent(WxPayBlance.this, QuXianActivity.class);
            startActivity(intent);
        });

        /**
         * 扫码充值、审核提现
         */
        mScanRechargeTv.setOnClickListener(view -> {
            Intent intent = new Intent(WxPayBlance.this, ScanRechargeActivity.class);
            startActivity(intent);
        });

        mScanWithdrawTv.setOnClickListener(view -> {
            Intent intent = new Intent(WxPayBlance.this, ScanWithdrawActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.tvPayPassword).setOnClickListener(v -> {
            Intent intent = new Intent(WxPayBlance.this, ChangePayPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void initData() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        HttpUtils.get().url(coreManager.getConfig().RECHARGE_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<Balance>(Balance.class) {

                    @Override
                    public void onResponse(ObjectResult<Balance> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            DecimalFormat decimalFormat = new DecimalFormat("0.00");
                            Balance balance = result.getData();
                            coreManager.getSelf().setBalance(balance.getBalance());
                            mBalanceTv.setText("￥" + decimalFormat.format(((balance.getBalance()))));
                        } else {
                            ToastUtil.showErrorData(WxPayBlance.this);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(WxPayBlance.this);
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventNotifyByTag message) {
        if (TextUtils.equals(message.tag, EventNotifyByTag.Withdraw)) {
            initData();
        }
    }
}
