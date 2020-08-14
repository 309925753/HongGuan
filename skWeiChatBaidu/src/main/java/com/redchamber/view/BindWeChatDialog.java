package com.redchamber.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.sk.weichat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 绑定微信
 */
public class BindWeChatDialog extends Dialog {

    private Unbinder mBinder;

    @BindView(R.id.et_phone)
    EditText mEtPhone;
    @BindView(R.id.et_name)
    EditText mEtName;
    @BindView(R.id.btn_confirm)
    Button mBtnConfirm;

    private OnConfirmListener mOnConfirmListener;

    public BindWeChatDialog(Context context) {
        super(context, R.style.BaseDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_dialog_bind_wechat);
        mBinder = ButterKnife.bind(this);
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        setCanceledOnTouchOutside(false);
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
