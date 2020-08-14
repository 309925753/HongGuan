package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.util.PermissionUtil;
import com.sk.weichat.util.ScreenUtil;


/**
 * 专门用于动态权限申请说明的提示框
 */
public class PermissionExplainDialog extends Dialog {
    private TextView
            mTipTv,
            mConfirm;
    private String mTipString;
    private OnConfirmListener mOnConfirmListener;
    private String[] permissions;

    public PermissionExplainDialog(Context context) {
        super(context, R.style.BottomDialog);
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
        mTipString = PermissionUtil.getPermissionExplainText(getContext(), permissions);
        updateUI();
    }

    public void setOnConfirmListener(OnConfirmListener mOnConfirmListener) {
        this.mOnConfirmListener = mOnConfirmListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip_dialog_permission_explain);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void updateUI() {
        if (mTipTv != null) {
            mTipTv.setText(mTipString);
        }
    }

    private void initView() {
        mTipTv = (TextView) findViewById(R.id.tip_tv);
        mConfirm = findViewById(R.id.confirm);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int) (ScreenUtil.getScreenWidth(getContext()) * 0.9);
        updateUI();
        initEvent();
    }

    private void initEvent() {
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mOnConfirmListener != null) {
                    mOnConfirmListener.confirm();
                }
            }
        });
    }

    public interface OnConfirmListener {
        void confirm();
    }
}
