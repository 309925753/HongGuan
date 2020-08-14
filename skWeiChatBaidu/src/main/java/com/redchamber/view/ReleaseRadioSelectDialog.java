package com.redchamber.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.sk.weichat.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 发布-选择：发节目-发动态
 */
public class ReleaseRadioSelectDialog extends Dialog {

    private Unbinder mBinder;

    private OnConfirmListener mOnConfirmListener;

    public ReleaseRadioSelectDialog(Context context) {
        super(context, R.style.BaseDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_dialog_release_select);
        mBinder = ButterKnife.bind(this);
        Window window = getWindow();
        window.setGravity(Gravity.TOP | Gravity.RIGHT);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        setCanceledOnTouchOutside(true);
    }

    @OnClick({R.id.ll_program, R.id.ll_moment})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_program:
                clickButton(0);
                break;
            case R.id.ll_moment:
                clickButton(1);
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
        void onConfirmClick(int type);
    }

    public void setOnConfirmListener(OnConfirmListener mOnConfirmListener) {
        this.mOnConfirmListener = mOnConfirmListener;
    }

    private void clickButton(int type) {
        if (mOnConfirmListener != null) {
            mOnConfirmListener.onConfirmClick(type);
        }
        dismiss();
    }

}
