package com.sk.weichat.pay;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sk.weichat.R;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.view.KeyBoad;
import com.sk.weichat.view.VerifyDialog;

/**
 * 收款码，设置收款固定金额
 */
public class ReceiptSetMoneyActivity extends BaseActivity {

    private String money, words;

    private TextView mMoneyTv;
    private TextView mTransferDescTv, mTransferDescClickTv;

    private EditText et_transfer;
    private KeyBoad keyBoad;
    private boolean isUiCreat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_money);
        initActionBar();
        initView();
        initEvent();
        initKeyBoad();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(view -> finish());
        TextView titleTv = findViewById(R.id.tv_title_center);
        titleTv.setText(getString(R.string.receipt_set_money));
    }

    private void initView() {
        mMoneyTv = findViewById(R.id.transfer_je_tv);
        mMoneyTv.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);// 允许输入数字与小数点
        mTransferDescTv = findViewById(R.id.transfer_desc_tv);
        mTransferDescClickTv = findViewById(R.id.transfer_edit_desc_tv);

        et_transfer = findViewById(R.id.et_transfer);
        keyBoad = new KeyBoad(ReceiptSetMoneyActivity.this, getWindow().getDecorView(), et_transfer);
    }

    private void initEvent() {
        mTransferDescClickTv.setOnClickListener(v -> {
            VerifyDialog verifyDialog = new VerifyDialog(mContext);
            verifyDialog.setVerifyClickListener(getString(R.string.receipt_add_desc), getString(R.string.transfer_desc_max_length_10),
                    words, 10, new VerifyDialog.VerifyClickListener() {
                        @Override
                        public void cancel() {

                        }

                        @Override
                        public void send(String str) {
                            words = str;
                            if (TextUtils.isEmpty(words)) {
                                mTransferDescTv.setText("");
                                mTransferDescTv.setVisibility(View.GONE);
                                mTransferDescClickTv.setText(getString(R.string.receipt_add_desc));
                            } else {
                                mTransferDescTv.setText(str);
                                mTransferDescTv.setVisibility(View.VISIBLE);
                                mTransferDescClickTv.setText(getString(R.string.transfer_modify));
                            }
                            keyBoad.show();
                        }
                    });
            verifyDialog.setOkButton(R.string.sure);
            keyBoad.dismiss();
            Window window = verifyDialog.getWindow();
            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); // 软键盘弹起
            }
            verifyDialog.show();
        });

//        findViewById(R.id.transfer_btn).setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());
        ButtonColorChange.colorChange(this, findViewById(R.id.transfer_btn));

        findViewById(R.id.transfer_btn).setOnClickListener(v -> {
            money = et_transfer.getText().toString().trim();
            double mMoney = 0;
            try {
                mMoney = Double.parseDouble(money);
            } catch (NumberFormatException e) {
                Toast.makeText(mContext, getString(R.string.transfer_input_money), Toast.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(money) || mMoney <= 0) {
                Toast.makeText(mContext, getString(R.string.transfer_input_money), Toast.LENGTH_SHORT).show();
                return;
            }

            if (money.endsWith(".")) {
                money = money.replace(".", "");
            }

            PreferenceUtils.putString(mContext, Constants.RECEIPT_SETTING_MONEY + coreManager.getSelf().getUserId(), money);
            if (!TextUtils.isEmpty(words)) {
                PreferenceUtils.putString(mContext, Constants.RECEIPT_SETTING_DESCRIPTION + coreManager.getSelf().getUserId(), words);
            }

            finish();
        });
    }

    private void initKeyBoad() {
        et_transfer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (keyBoad != null && isUiCreat) {
                    keyBoad.refreshKeyboardOutSideTouchable(!hasFocus);
                } else if (isUiCreat) {
                    keyBoad.show();
                }
                if (hasFocus) {
                    InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(et_transfer.getWindowToken(), 0);
                }
            }
        });

        et_transfer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.startsWith(".")) {
                    et_transfer.setText("0" + text);
                } else if (text.startsWith("0") && !text.contains(".") && text.length() > 1) {
                    et_transfer.setText(text.substring(1, text.length()));
                }

            }
        });

        et_transfer.setOnClickListener(v -> {
            if (keyBoad != null) {
                keyBoad.show();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        isUiCreat = true;
    }
}
