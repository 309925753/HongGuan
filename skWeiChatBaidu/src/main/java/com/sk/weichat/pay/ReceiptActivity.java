package com.sk.weichat.pay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.qrcode.utils.CommonUtils;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.Receipt;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.DisplayUtil;
import com.sk.weichat.util.FileUtil;
import com.sk.weichat.util.PreferenceUtils;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * 收款码
 */
public class ReceiptActivity extends BaseActivity {

    private int resumeCount;
    private String mLoginUserId;
    private ImageView mReceiptQrCodeIv;
    private ImageView mReceiptQrCodeAvatarIv;

    private String money, description;
    private TextView mMoneyTv, mDescTv;
    private TextView mSetMoneyTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);
        mLoginUserId = coreManager.getSelf().getUserId();

        money = PreferenceUtils.getString(mContext, Constants.RECEIPT_SETTING_MONEY + mLoginUserId);
        description = PreferenceUtils.getString(mContext, Constants.RECEIPT_SETTING_DESCRIPTION + mLoginUserId);

        initActionBar();
        initView();
        initEvent();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeCount++;
        if (resumeCount > 1) {
            money = PreferenceUtils.getString(mContext, Constants.RECEIPT_SETTING_MONEY + mLoginUserId);
            description = PreferenceUtils.getString(mContext, Constants.RECEIPT_SETTING_DESCRIPTION + mLoginUserId);
            refreshView();
            refreshReceiptQRCode();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(view -> finish());
        TextView titleTv = findViewById(R.id.tv_title_center);
        titleTv.setText(getString(R.string.rp_receipt));
    }

    private void initView() {
        mReceiptQrCodeIv = findViewById(R.id.rp_qr_code_iv);
        mReceiptQrCodeAvatarIv = findViewById(R.id.rp_qr_code_avatar_iv);
        refreshReceiptQRCode();
        AvatarHelper.getInstance().displayAvatar(mLoginUserId, mReceiptQrCodeAvatarIv);

        mMoneyTv = findViewById(R.id.rp_money_tv);
        mDescTv = findViewById(R.id.rp_desc_tv);
        mSetMoneyTv = findViewById(R.id.rp_set_money_tv);
        refreshView();
    }

    private void initEvent() {
        findViewById(R.id.rp_set_money_tv).setOnClickListener(v -> {
            if (!TextUtils.isEmpty(money)) { // 清除金额
                money = "";
                description = "";
                PreferenceUtils.putString(mContext, Constants.RECEIPT_SETTING_MONEY + mLoginUserId, money);
                PreferenceUtils.putString(mContext, Constants.RECEIPT_SETTING_DESCRIPTION + mLoginUserId, description);
                mSetMoneyTv.setText(getString(R.string.rp_receipt_tip2));
                refreshView();
                refreshReceiptQRCode();
            } else { // 设置金额
                startActivity(new Intent(mContext, ReceiptSetMoneyActivity.class));
            }
        });

        findViewById(R.id.rp_save_receipt_code_tv).setOnClickListener(v -> {
            FileUtil.saveImageToGallery2(mContext, getBitmap(ReceiptActivity.this.getWindow().getDecorView()), true);
        });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventReceiptSuccess message) {
        DialogHelper.tip(mContext, getString(R.string.payment, message.getPaymentName()));
    }

    private void refreshView() {
        mMoneyTv.setText("￥" + money);
        mDescTv.setText(description);

        if (!TextUtils.isEmpty(money)) {
            mSetMoneyTv.setText(getString(R.string.rp_receipt_tip3));
            mMoneyTv.setVisibility(View.VISIBLE);
        } else {
            mSetMoneyTv.setText(getString(R.string.rp_receipt_tip2));
            mMoneyTv.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(description)) {
            mDescTv.setVisibility(View.VISIBLE);
        } else {
            mDescTv.setVisibility(View.GONE);
        }
    }

    private void refreshReceiptQRCode() {
        Receipt receipt = new Receipt();
        receipt.setUserId(mLoginUserId);
        receipt.setUserName(coreManager.getSelf().getNickName());
        receipt.setMoney(money);
        receipt.setDescription(description);

        String content = JSON.toJSONString(receipt);
        Bitmap mQRCodeBitmap = CommonUtils.createQRCode(content, DisplayUtil.dip2px(MyApplication.getContext(), 160),
                DisplayUtil.dip2px(MyApplication.getContext(), 160));
        mReceiptQrCodeIv.setImageBitmap(mQRCodeBitmap);
    }

    /**
     * 获取这个view的缓存bitmap,
     */
    private Bitmap getBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap result = Bitmap.createBitmap(view.getDrawingCache());
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);
        return result;
    }
}
