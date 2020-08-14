package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;

/**
 * 选择加密模式
 */
public class SelectChatModeDialog extends Dialog implements View.OnClickListener {
    private TextView mNoGag, mOneGag, mTwoGag, mThreeGag;
    private OnBannedDialogClickListener mOnBannedDialogClickListener;
    private boolean isGoneSecret;

    public SelectChatModeDialog(Context context, OnBannedDialogClickListener onBannedDialogClickListener) {
        super(context, R.style.BottomDialog);
        this.mOnBannedDialogClickListener = onBannedDialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_chat_mode_dialog);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        mNoGag = findViewById(R.id.no_gag);
        mOneGag = findViewById(R.id.one_gag);
        mTwoGag = findViewById(R.id.two_gag);
        mThreeGag = findViewById(R.id.three_gag);
        mNoGag.setOnClickListener(this);
        mOneGag.setOnClickListener(this);
        mTwoGag.setOnClickListener(this);
        mThreeGag.setOnClickListener(this);
        if (!MyApplication.IS_SUPPORT_SECURE_CHAT
                || isGoneSecret) {
            // SecureFlag/SecureFlagGroup
            mTwoGag.setVisibility(View.GONE);
            mThreeGag.setVisibility(View.GONE);
        }

        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        // x/y坐标
        // lp.x = 100;
        // lp.y = 100;
        lp.width = ScreenUtil.getScreenWidth(getContext());
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.BOTTOM);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }

    public void setGoneSecret(boolean goneSecret) {
        isGoneSecret = goneSecret;
        if (isGoneSecret) {
            if (mTwoGag != null && mThreeGag != null) {
                mTwoGag.setVisibility(View.GONE);
                mThreeGag.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.no_gag:
                mOnBannedDialogClickListener.tv1Click();
                break;
            case R.id.one_gag:
                mOnBannedDialogClickListener.tv2Click();
                break;
            case R.id.two_gag:
                mOnBannedDialogClickListener.tv3Click();
                break;
            case R.id.three_gag:
                mOnBannedDialogClickListener.tv4Click();
                break;
        }
    }

    public interface OnBannedDialogClickListener {
        void tv1Click();

        void tv2Click();

        void tv3Click();

        void tv4Click();
    }
}
