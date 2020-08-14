package com.sk.weichat.ui.yeepay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckedTextView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;

import com.sk.weichat.R;
import com.sk.weichat.bean.event.EventPaySuccess;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.YeepayHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.EventBusHelper;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.ToastUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * 易宝充值
 */
public class YeepayRecharge extends BaseActivity {
    private List<BigDecimal> mRechargeList = new ArrayList<>();
    private List<CheckedTextView> mRechargeMoneyViewList = new ArrayList<>();
    private TextView mSelectMoneyTv;
    private int mSelectedPosition = 0;

    public static void start(Context context) {
        Intent starter = new Intent(context, YeepayRecharge.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yeepay_recharge);

        initActionBar();
        initData();
        initView();

        EventBusHelper.register(this);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
            mSelectedPosition = -1;
            for (int i = 0, mRechargeMoneyViewListSize = mRechargeMoneyViewList.size(); i < mRechargeMoneyViewListSize; i++) {
                CheckedTextView textView = mRechargeMoneyViewList.get(i);
                if (textView == v) {
                    mSelectedPosition = i;
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
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    BigDecimal money = new BigDecimal(s.toString());
                    if (money.scale() > 2) {
                        mSelectMoneyTv.setText(money.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
                        return;
                    }
                } catch (Exception ignored) {
                    // 就算TextUtils.isEmpty判断了s不空，还是可能NumberFormatException: For input string: ""
                    // 看着像是存在异步修改，多线程冲突，但是没找到，
                }
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

        findViewById(R.id.recharge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String money = getCurrentMoney();
                if (TextUtils.equals(money, "0")) {
                    Toast.makeText(mContext, getString(R.string.transfer_input_money), Toast.LENGTH_SHORT).show();
                    return;
                }
                YeepayHelper.recharge(mContext, coreManager, getCurrentMoney());
            }
        });
    }

    private String getCurrentMoney() {
        if (TextUtils.isEmpty(mSelectMoneyTv.getText())) {
            return "0";
        }
        return new BigDecimal(mSelectMoneyTv.getText().toString()).stripTrailingZeros().toPlainString();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventYeepayWebSuccess message) {
        YeepayHelper.queryRecharge(mContext, coreManager, message.data, () -> {
            ToastUtil.showToast(mContext, R.string.recharge_success);
            finish();
        });
    }
}
