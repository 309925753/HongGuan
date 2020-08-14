package com.redchamber.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.weichat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 公共弹窗
 */
public class CommCodeDialog extends Dialog {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.btn_confirm)
    Button btnConfirm;
    @BindView(R.id.iv_close)
    ImageView ivClose;
    @BindView(R.id.bt_two)
    Button btTwo;
    private Unbinder mBinder;

    private OnConfirmListener mOnConfirmListener;
    private String title;
    private String confirm;

    public CommCodeDialog(Context context, String _title, String _confirm) {
        super(context, R.style.BaseDialogStyle);
        title = _title;
        confirm = _confirm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_dialog_non_bar_com_code);
        mBinder = ButterKnife.bind(this);
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        tvTitle.setText(title == null ? "" : title);
        btnConfirm.setText(confirm == null ? "" : confirm);
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
