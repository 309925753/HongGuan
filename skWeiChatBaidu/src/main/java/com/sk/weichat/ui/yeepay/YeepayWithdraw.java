package com.sk.weichat.ui.yeepay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.YeepayHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.EventBusHelper;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.ToastUtil;

import java.text.DecimalFormat;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class YeepayWithdraw extends BaseActivity {
    private RadioGroup rgArrival;
    private EditText mMentionMoneyEdit;
    private TextView mBalanceTv;
    private TextView mAllMentionTv;
    private TextView mSureMentionTv;
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public static void start(Context context) {
        Intent starter = new Intent(context, YeepayWithdraw.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yeepay_withdraw);

        initActionbar();
        initView();
        intEvent();
        EventBusHelper.register(this);
    }

    private void initActionbar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(getString(R.string.withdraw));
    }

    private void initView() {
        mMentionMoneyEdit = (EditText) findViewById(R.id.tixianmoney);
        mBalanceTv = (TextView) findViewById(R.id.blance_weixin);
        mBalanceTv.setText(decimalFormat.format(coreManager.getSelf().getBalance()));
        mAllMentionTv = (TextView) findViewById(R.id.tixianall);
        mSureMentionTv = (TextView) findViewById(R.id.tixian);
        ButtonColorChange.rechargeChange(this, mSureMentionTv, R.drawable.recharge_icon);
        mSureMentionTv.setTextColor(SkinUtils.getSkin(this).getAccentColor());
        rgArrival = findViewById(R.id.rgArrival);
        for (int i = 0; i < rgArrival.getChildCount(); i++) {
            RadioButton rb = (RadioButton) rgArrival.getChildAt(i);
            rb.setButtonTintList(SkinUtils.getSkin(mContext).getTabColorState());
        }
    }

    private void intEvent() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mMentionMoneyEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 删除开头的0，
                int end = 0;
                for (int i = 0; i < editable.length(); i++) {
                    char ch = editable.charAt(i);
                    if (ch == '0') {
                        end = i + 1;
                    } else {
                        break;
                    }
                }
                if (end > 0) {
                    editable.delete(0, end);
                    mMentionMoneyEdit.setText(editable);
                }
            }
        });

        mAllMentionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double money = coreManager.getSelf().getBalance();
                if (money < 1) {
                    DialogHelper.tip(YeepayWithdraw.this, getString(R.string.tip_withdraw_too_little));
                } else {
                    mMentionMoneyEdit.setText(String.valueOf((int) money));
                }
            }
        });

        mSureMentionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String moneyStr = mMentionMoneyEdit.getText().toString();
                if (checkMoney(moneyStr)) {
                    YeepayHelper.withdraw(mContext, coreManager, moneyStr, getType());
                }
            }
        });
    }

    private String getType() {
        switch (rgArrival.getCheckedRadioButtonId()) {
            case R.id.arrival_real_time:
                return "0";
            case R.id.arrival_two_hour:
                return "1";
            case R.id.arrival_tomorrow:
                return "2";
            default:
                throw new IllegalStateException("unknown id " + rgArrival.getCheckedRadioButtonId());
        }
    }

    private boolean checkMoney(String moneyStr) {
        if (TextUtils.isEmpty(moneyStr)) {
            DialogHelper.tip(YeepayWithdraw.this, getString(R.string.tip_withdraw_empty));
        } else {
            if (Double.valueOf(moneyStr) < 1) {
                DialogHelper.tip(YeepayWithdraw.this, getString(R.string.tip_withdraw_too_little));
            } else {// 获取用户code
                return true;
            }
        }
        return false;
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventYeepayWebSuccess message) {
        YeepayHelper.queryWithdraw(mContext, coreManager, message.data, () -> {
            ToastUtil.showToast(mContext, R.string.tip_withdraw_success);
            finish();
        });
    }
}
