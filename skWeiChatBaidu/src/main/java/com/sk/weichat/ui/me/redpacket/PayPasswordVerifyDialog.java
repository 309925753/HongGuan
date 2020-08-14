package com.sk.weichat.ui.me.redpacket;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;
import com.sk.weichat.view.PasswordInputView;

public class PayPasswordVerifyDialog extends Dialog {
    private TextView tvAction;
    private View llMoney;
    private TextView tvMoney;
    private PasswordInputView passwordInputView;

    private String action;
    private String money;

    private OnInputFinishListener onInputFinishListener;

    public PayPasswordVerifyDialog(@NonNull Context context) {
        super(context, R.style.MyDialog);
    }

    public PayPasswordVerifyDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected PayPasswordVerifyDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_password_verify_dialog);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView() {
        findViewById(R.id.ivClose).setOnClickListener(v -> {
            cancel();
        });
        tvAction = findViewById(R.id.tvAction);
        if (action != null) {
            tvAction.setText(action);
        }
        llMoney = findViewById(R.id.llMoney);
        tvMoney = findViewById(R.id.tvMoney);
        if (!TextUtils.isEmpty(money)) {
            tvMoney.setText(money);
            llMoney.setVisibility(View.VISIBLE);
        } else {
            llMoney.setVisibility(View.GONE);
        }
        passwordInputView = findViewById(R.id.passwordInputView);
        passwordInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == AppConstant.PASS_WORD_LENGTH) {
                    dismiss();
                    if (onInputFinishListener != null) {
                        onInputFinishListener.onInputFinish(s.toString());
                    }
                }
            }
        });
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int) (ScreenUtil.getScreenWidth(getContext()) * 0.7);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void setAction(String action) {
        this.action = action;
        if (tvAction != null) {
            tvAction.setText(action);
        }
    }

    public void setMoney(String money) {
        this.money = money;
        if (tvMoney != null) {
            tvMoney.setText(money);
        }
        if (llMoney != null) {
            if (!TextUtils.isEmpty(money)) {
                llMoney.setVisibility(View.VISIBLE);
            } else {
                llMoney.setVisibility(View.GONE);
            }
        }
    }

    public void setOnInputFinishListener(OnInputFinishListener onInputFinishListener) {
        this.onInputFinishListener = onInputFinishListener;
    }

    public interface OnInputFinishListener {
        void onInputFinish(String password);
    }
}
