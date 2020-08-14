package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;


/**
 * 提示框
 */
public class TipDialog extends Dialog {
    private TextView
            mTipTv,
            mConfirm;
    private String mTipString;
    private ConfirmOnClickListener mConfirmOnClickListener;

    public TipDialog(Context context) {
        super(context, R.style.BottomDialog);
    }

    public void setTip(String tip) {
        this.mTipString = tip;
        if (mTipTv != null) {
            mTipTv.setText(mTipString);
        }
    }

    public void setmConfirmOnClickListener(String tip, ConfirmOnClickListener mConfirmOnClickListener) {
        setTip(tip);
        this.mTipString = tip;
        this.mConfirmOnClickListener = mConfirmOnClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip_dialog);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView() {
        mTipTv = (TextView) findViewById(R.id.tip_tv);
        if (!TextUtils.isEmpty(mTipString)) {
            mTipTv.setText(mTipString);
        }
        mConfirm = findViewById(R.id.confirm);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int) (ScreenUtil.getScreenWidth(getContext()) * 0.9);
        initEvent();
    }

    private void initEvent() {
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mConfirmOnClickListener != null) {
                    mConfirmOnClickListener.confirm();
                }
            }
        });
    }

    public interface ConfirmOnClickListener {
        void confirm();
    }
}
