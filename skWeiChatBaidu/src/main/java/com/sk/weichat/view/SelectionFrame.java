package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;


/**
 * 对话框
 */
public class SelectionFrame extends Dialog {
    private TextView
            mTitle,
            mDescribe,
            mCancel,
            mConfirm;
    private String
            mTitleString,
            mDescribeString,
            mCancelString,
            mConfirmString;
    private boolean mEnableAutoDismiss = true;
    private OnSelectionFrameClickListener mOnSelectionFrameClickListener;

    public SelectionFrame(Context context) {
        super(context, R.style.BottomDialog);
    }

    public void setSomething(String title, String describe, OnSelectionFrameClickListener onSelectionFrameClickListener) {
        this.mTitleString = title;
        this.mDescribeString = describe;
        this.mOnSelectionFrameClickListener = onSelectionFrameClickListener;
    }

    public void setSomething(String title, String describe, String cancel, String confirm, OnSelectionFrameClickListener onSelectionFrameClickListener) {
        this.mTitleString = title;
        this.mDescribeString = describe;
        this.mCancelString = cancel;
        this.mConfirmString = confirm;
        this.mOnSelectionFrameClickListener = onSelectionFrameClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selection_frame);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView() {
        mTitle = (TextView) findViewById(R.id.title);
        mDescribe = (TextView) findViewById(R.id.describe);
        mCancel = (TextView) findViewById(R.id.cancel);
        mConfirm = (TextView) findViewById(R.id.confirm);

        if (!TextUtils.isEmpty(mTitleString)) {
            mTitle.setText(mTitleString);
        }
        if (!TextUtils.isEmpty(mDescribeString)) {
            mDescribe.setText(mDescribeString);
        }
        if (!TextUtils.isEmpty(mCancelString)) {
            mCancel.setText(mCancelString);
        }
        if (!TextUtils.isEmpty(mConfirmString)) {
            mConfirm.setText(mConfirmString);
        }
        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        lp.width = (int) (ScreenUtil.getScreenWidth(getContext()) * 0.9);
        lp.gravity = Gravity.CENTER;
        o.setAttributes(lp);
        initEvent();
    }

    private void initEvent() {
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mOnSelectionFrameClickListener != null) {
                    mOnSelectionFrameClickListener.cancelClick();
                }
            }
        });
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEnableAutoDismiss) {
                    dismiss();
                }
                if (mOnSelectionFrameClickListener != null) {
                    mOnSelectionFrameClickListener.confirmClick();
                }
            }
        });
    }

    public void setAutoDismiss(boolean enableAutoDismiss) {
        mEnableAutoDismiss = enableAutoDismiss;
    }

    public interface OnSelectionFrameClickListener {

        void cancelClick();

        void confirmClick();
    }
}
