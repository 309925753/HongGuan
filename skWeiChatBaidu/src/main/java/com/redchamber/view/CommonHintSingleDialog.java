package com.redchamber.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.sk.weichat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 通用单个提示、单个按钮
 */
public class CommonHintSingleDialog extends Dialog {

    private Unbinder mBinder;

    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_hint)
    TextView mTvHint;
    @BindView(R.id.btn_confirm)
    Button mBtnConfirm;

    private OnConfirmListener mOnConfirmListener;

    private String mStrTitle, mStrHint, mStrButton;

    public CommonHintSingleDialog(Context context, String strHint, String strButton) {
        super(context, R.style.BaseDialogStyle);
        this.mStrHint = strHint;
        this.mStrButton = strButton;
    }

    public CommonHintSingleDialog(Context context, String strTitle, String strHint, String strButton) {
        super(context, R.style.BaseDialogStyle);
        this.mStrTitle = strTitle;
        this.mStrHint = strHint;
        this.mStrButton = strButton;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_dialog_common_hint_single);
        mBinder = ButterKnife.bind(this);
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        setCanceledOnTouchOutside(false);

        initView();
    }

    private void initView() {
        if (TextUtils.isEmpty(mStrTitle)) {
            mTvTitle.setVisibility(View.GONE);
        } else {
            mTvTitle.setVisibility(View.VISIBLE);
            mTvTitle.setText(mStrTitle);
        }
        mTvHint.setText(mStrHint);
        mBtnConfirm.setText(mStrButton);
    }

    @OnClick({R.id.btn_confirm, R.id.iv_close})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
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

}
