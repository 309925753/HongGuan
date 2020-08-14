package com.redchamber.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.sk.weichat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 支付
 */
public class PaymentDialog extends Dialog {

    @BindView(R.id.tv_pay)
    TextView tvPay;
    @BindView(R.id.tv_pay_money)
    TextView tvPayMoney;
    @BindView(R.id.tv_red_nums)
    TextView tvRedNums;
    @BindView(R.id.rb_alipay)
    RadioButton rbAlipay;
    @BindView(R.id.rb_wechat)
    RadioButton rbWechat;
    @BindView(R.id.btn_buy)
    Button btnBuy;
    @BindView(R.id.iv_close)
    ImageView ivClose;
    private Unbinder mBinder;
    private String money;
    private OnConfirmListener mOnConfirmListener;

    public PaymentDialog(Context context, String _money) {
        super(context, R.style.BaseDialogStyle);
        money = _money;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_payment_layout);
        mBinder = ButterKnife.bind(this);
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        setCanceledOnTouchOutside(false);
        tvPayMoney.setText(money);

        rbAlipay.setOnCheckedChangeListener(mChangeListener);
        rbWechat.setOnCheckedChangeListener(mChangeListener);
    }

    @OnClick({R.id.btn_buy, R.id.iv_close})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_buy:
                if (mOnConfirmListener != null) {
                    mOnConfirmListener.onConfirmClick();
                }
                dismiss();
                break;
            case R.id.iv_close:
                dismiss();
                break;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mBinder != null) {
            mBinder.unbind();
        }
    }

    public interface OnConfirmListener {
        void onConfirmClick();
    }

    public void setOnConfirmListener(OnConfirmListener mOnConfirmListener) {
        this.mOnConfirmListener = mOnConfirmListener;
    }

    final CompoundButton.OnCheckedChangeListener mChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == R.id.rb_alipay && isChecked) {
                rbWechat.setChecked(false);
            } else if (buttonView.getId() == R.id.rb_wechat && isChecked) {
                rbAlipay.setChecked(false);
            }
        }
    };
}
