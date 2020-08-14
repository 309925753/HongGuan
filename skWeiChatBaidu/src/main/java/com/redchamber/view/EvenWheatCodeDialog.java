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
public class EvenWheatCodeDialog extends Dialog {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.btn_confirm)
    Button btnConfirm;
    @BindView(R.id.iv_close)
    ImageView ivClose;
    @BindView(R.id.bt_center)
    Button btCenter;
    @BindView(R.id.tv_certification)
    TextView tvCertification;
    private Unbinder mBinder;

    private OnConfirmListener mOnConfirmListener;
    private OnCenterListener mOnCenterListener;

    private String title;
    private String confirm;
    private String center;

    public EvenWheatCodeDialog(Context context, String _title, String _confirm, String bcenter) {
        super(context, R.style.BaseDialogStyle);
        title = _title;
        confirm = _confirm;
        center = bcenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_dialog_non_bar_even_wheat);
        mBinder = ButterKnife.bind(this);
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        tvTitle.setText(title == null ? "" : title);
        btnConfirm.setText(confirm == null ? "" : confirm);
        btCenter.setText((center == null ? "" : center));
        btCenter.setVisibility(center == null ? View.GONE : View.VISIBLE);
        btnConfirm.setVisibility(confirm == null ? View.GONE : View.VISIBLE);
        if((!TextUtils.isEmpty(confirm))&&confirm.equals("马上认证")){
             tvCertification.setVisibility(View.VISIBLE);
        }else {
            tvCertification.setVisibility(View.GONE);
        }

        ivClose.setVisibility(View.VISIBLE);
        setCanceledOnTouchOutside(false);
    }

    @OnClick({R.id.btn_confirm, R.id.iv_close, R.id.bt_center})
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
            case R.id.bt_center:
                if (mOnCenterListener != null) {
                    mOnCenterListener.onCenterClick();
                }
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

    public interface OnCenterListener {
        void onCenterClick();
    }

    public void setOnCenterListener(OnCenterListener mOnCenterListener) {
        this.mOnCenterListener = mOnCenterListener;
    }

}
