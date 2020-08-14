package com.sk.weichat.ui.me.redpacket.scan;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.sk.weichat.R;
import com.sk.weichat.bean.redpacket.ScanRecharge;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.dialog.money.ScanRechargeBandDialog;
import com.sk.weichat.ui.dialog.money.ScanRechargeWxAlipayDialog;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 扫码充值
 */
public class ScanRechargeActivity extends BaseActivity implements View.OnClickListener {
    private List<BigDecimal> mRechargeList = new ArrayList<>();
    private List<CheckedTextView> mRechargeMoneyViewList = new ArrayList<>();

    private EditText mSelectMoneyTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_recharge);

        initActionBar();
        initData();
        initView();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.recharge));
    }

    private void initData() {
        mRechargeList.add(new BigDecimal("10"));
        mRechargeList.add(new BigDecimal("20"));
        mRechargeList.add(new BigDecimal("50"));
        mRechargeList.add(new BigDecimal("100"));
        mRechargeList.add(new BigDecimal("200"));
        mRechargeList.add(new BigDecimal("500"));
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        TableLayout tableLayout = findViewById(R.id.tableLayoutRechargeMoney);
        SkinUtils.Skin skin = SkinUtils.getSkin(this);
        ColorStateList highlightColorState = skin.getHighlightColorState();
        View.OnClickListener onMoneyClickListener = v -> {
            for (int i = 0, mRechargeMoneyViewListSize = mRechargeMoneyViewList.size(); i < mRechargeMoneyViewListSize; i++) {
                CheckedTextView textView = mRechargeMoneyViewList.get(i);
                if (textView == v) {
                    mSelectMoneyTv.setText(mRechargeList.get(i).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
                    textView.setChecked(true);
                } else {
                    textView.setChecked(false);
                }
            }
        };
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            TableRow tableRow = (TableRow) tableLayout.getChildAt(i);
            for (int k = 0; k < tableRow.getChildCount(); k++) {
                CheckedTextView tvMoney = tableRow.getChildAt(k).findViewById(R.id.tvRechargeMoney);
                tvMoney.setOnClickListener(onMoneyClickListener);
                tvMoney.setTextColor(highlightColorState);
                ViewCompat.setBackgroundTintList(tvMoney, ColorStateList.valueOf(skin.getAccentColor()));
                int index = i * tableRow.getChildCount() + k;
                tvMoney.setText(mRechargeList.get(index).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + getString(R.string.yuan));
                mRechargeMoneyViewList.add(tvMoney);
            }
        }

        mSelectMoneyTv = findViewById(R.id.select_money_tv);
        mSelectMoneyTv.setTextColor(skin.getAccentColor());
        mSelectMoneyTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + 3);
                        mSelectMoneyTv.setText(s);
                        mSelectMoneyTv.setSelection(s.length());
                    }
                }

                if (!TextUtils.isEmpty(s) && s.toString().trim().substring(0, 1).equals(".")) {
                    s = "0" + s;
                    mSelectMoneyTv.setText(s);
                    mSelectMoneyTv.setSelection(1);
                }

                if (s.toString().startsWith("0")
                        && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        mSelectMoneyTv.setText(s.subSequence(0, 1));
                        mSelectMoneyTv.setSelection(1);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    mSelectMoneyTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
                    mSelectMoneyTv.setHint(null);
                } else {
                    // invisible占着高度，
                    mSelectMoneyTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    mSelectMoneyTv.setHint(R.string.need_input_money);
                }
            }
        });
    }

    private void initEvent() {
        findViewById(R.id.recharge_wechat).setOnClickListener(this);
        findViewById(R.id.recharge_alipay).setOnClickListener(this);
        findViewById(R.id.recharge_band).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (TextUtils.equals(getCurrentMoney(), "0")) {
            ToastUtil.showToast(mContext, getString(R.string.transfer_input_money));
            return;
        }
        switch (v.getId()) {
            case R.id.recharge_wechat:
                show(1);
                break;
            case R.id.recharge_alipay:
                show(2);
                break;
            case R.id.recharge_band:
                show(3);
                break;
        }
    }

    private String getCurrentMoney() {
        if (TextUtils.isEmpty(mSelectMoneyTv.getText())) {
            return "0";
        }
        return new BigDecimal(mSelectMoneyTv.getText().toString()).stripTrailingZeros().toPlainString();
    }

    private void show(int type) {
        DialogHelper.showDefaulteMessageProgressDialog(this);

        Map<String, String> params = new HashMap<>();
        params.put("type", String.valueOf(type));// 支付方式 1.微信 2.支付宝 3.银行卡

        HttpUtils.get().url(coreManager.getConfig().MANUAL_PAY_GET_RECEIVER_ACCOUNT)
                .params(params)
                .build()
                .execute(new BaseCallback<ScanRecharge>(ScanRecharge.class) {

                    @Override
                    public void onResponse(ObjectResult<ScanRecharge> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            ScanRecharge scanRecharge = result.getData();
                            if (type == 3) {
                                ScanRechargeBandDialog scanRechargeBandDialog = new ScanRechargeBandDialog(mContext
                                        , scanRecharge.getName()
                                        , scanRecharge.getBankCard()
                                        , scanRecharge.getBankName()
                                        , getCurrentMoney());
                                scanRechargeBandDialog.show();
                            } else {
                                ScanRechargeWxAlipayDialog scanRechargeWxAlipayDialog = new ScanRechargeWxAlipayDialog(mContext
                                        , type,
                                        getCurrentMoney(),
                                        scanRecharge.getUrl());
                                scanRechargeWxAlipayDialog.show();
                            }
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
