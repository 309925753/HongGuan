package com.sk.weichat.ui.me.redpacket.scan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.bean.redpacket.ScanRecharge;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 添加提现账号
 */
public class ScanWithdrawAddActivity extends BaseActivity {
    private int mAddType;
    private EditText mAlipayNameEdit, mAlipayAccount;
    private EditText mBandCardOwnerNameEdit, mBandCardAccountEdit, mBandCardNameEdit, mBandCardSonNameEdit, mRemarkEdit;

    public static void start(Context context, int type) {
        Intent intent = new Intent(context, ScanWithdrawAddActivity.class);
        intent.putExtra("add_type", type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_withdraw_add);
        mAddType = getIntent().getIntExtra("add_type", 1);// 1 支付宝 2 银行卡
        initActionbar();
        initView();
        intEvent();
    }

    private void initActionbar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView mTvTitle = findViewById(R.id.tv_title_center);
        mTvTitle.setText(mAddType == 1 ? getString(R.string.select_withdraw_add_alipay_account) : getString(R.string.select_withdraw_add_band_card_account));
    }

    private void initView() {
        findViewById(mAddType == 1 ? R.id.ll1 : R.id.ll2).setVisibility(View.VISIBLE);
        ButtonColorChange.colorChange(mContext, findViewById(R.id.sure_band_btn));
        mAlipayNameEdit = findViewById(R.id.alipay_name_et);
        mAlipayAccount = findViewById(R.id.alipay_account_et);
        mBandCardOwnerNameEdit = findViewById(R.id.band_card_owner_name_et);
        mBandCardAccountEdit = findViewById(R.id.band_card_account_et);
        mBandCardNameEdit = findViewById(R.id.band_name_et);
        mBandCardSonNameEdit = findViewById(R.id.band_son_name_et);
        mRemarkEdit = findViewById(R.id.band_card_remark_et);
    }

    private void intEvent() {
        findViewById(R.id.sure_band_btn).setOnClickListener(v -> {
            band();
        });
    }

    private void band() {
        Map<String, String> params = new HashMap<>();
        if (mAddType == 1) {
            String alipayName = mAlipayNameEdit.getText().toString().trim();
            String alipayAccount = mAlipayAccount.getText().toString().trim();
            if (TextUtils.isEmpty(alipayName) || TextUtils.isEmpty(alipayAccount)) {
                ToastUtil.showToast(mContext, getString(R.string.must_edit_info_cannot_null));
                return;
            }
            params.put("type", String.valueOf(1));
            params.put("aliPayName", alipayName);
            params.put("aliPayAccount", alipayAccount);
        } else {
            String bandCardOwnerName = mBandCardOwnerNameEdit.getText().toString().trim();
            String bandCardAccount = mBandCardAccountEdit.getText().toString().trim();
            String bandCardName = mBandCardNameEdit.getText().toString().trim();
            String bandCardSonName = mBandCardSonNameEdit.getText().toString().trim();
            String remark = mRemarkEdit.getText().toString().trim();
            if (TextUtils.isEmpty(bandCardOwnerName) || TextUtils.isEmpty(bandCardAccount) || TextUtils.isEmpty(bandCardName)) {
                ToastUtil.showToast(mContext, getString(R.string.must_edit_info_cannot_null));
                return;
            }
            params.put("type", String.valueOf(2));
            params.put("cardName", bandCardOwnerName);
            params.put("bankCardNo", bandCardAccount);
            params.put("bankName", bandCardName);
            params.put("bankBranchName", bandCardSonName);
            params.put("desc", remark);
        }

        DialogHelper.showDefaulteMessageProgressDialog(mContext);
        HttpUtils.get().url(coreManager.getConfig().MANUAL_PAY_ADD_WITHDRAW_ACCOUNT)
                .params(params)
                .build()
                .execute(new BaseCallback<ScanRecharge>(ScanRecharge.class) {

                    @Override
                    public void onResponse(ObjectResult<ScanRecharge> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            ToastUtil.showToast(mContext, getString(R.string.addsuccess));
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });

    }
}
