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
public class CommonHintDoubleDialog extends Dialog {

    private Unbinder mBinder;

    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_hint)
    TextView mTvHint;
    @BindView(R.id.btn_first)
    Button mBtnFirst;
    @BindView(R.id.btn_second)
    Button mBtnSecond;

    private OnConfirmListener mOnConfirmListener;

    private String mStrTitle, mStrHint, mStrBtnFirst, mStrBtnSecond;

    public CommonHintDoubleDialog(Context context, String strHint, String strBtnFirst, String strBtnSecond) {
        super(context, R.style.BaseDialogStyle);
        this.mStrHint = strHint;
        this.mStrBtnFirst = strBtnFirst;
        this.mStrBtnSecond = strBtnSecond;
    }

    public CommonHintDoubleDialog(Context context, String strTitle, String strHint, String strBtnFirst, String strBtnSecond) {
        super(context, R.style.BaseDialogStyle);
        this.mStrTitle = strTitle;
        this.mStrHint = strHint;
        this.mStrBtnFirst = strBtnFirst;
        this.mStrBtnSecond = strBtnSecond;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_dialog_common_hint_double);
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
        mBtnFirst.setText(mStrBtnFirst);
        mBtnSecond.setText(mStrBtnSecond);
    }

    @OnClick({R.id.btn_first, R.id.btn_second, R.id.iv_close})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_first:
                if (mOnConfirmListener != null) {
                    mOnConfirmListener.onFirstButtonClick();
                }
                dismiss();
                break;
            case R.id.btn_second:
                if (mOnConfirmListener != null) {
                    mOnConfirmListener.onSecondButtonClick();
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
        void onFirstButtonClick();

        void onSecondButtonClick();
    }

    public void setOnConfirmListener(OnConfirmListener mOnConfirmListener) {
        this.mOnConfirmListener = mOnConfirmListener;
    }

}
