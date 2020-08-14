package com.redchamber.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.sk.weichat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 拉黑、举报
 */
public class PullBlackDialog extends Dialog {

    private Unbinder mBinder;

    @BindView(R.id.tv_black)
    TextView mTvBlack;

    private OnConfirmListener mOnConfirmListener;
    private boolean mIsBlack;

    public PullBlackDialog(Context context, boolean isBlack) {
        super(context, R.style.BaseDialogStyle);
        this.mIsBlack = isBlack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_dialog_pull_black);
        mBinder = ButterKnife.bind(this);
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        setCanceledOnTouchOutside(true);

        if (mIsBlack) {
            mTvBlack.setText("已拉黑");
        } else {
            mTvBlack.setText("拉黑(屏蔽双方)");
        }

    }

    @OnClick({R.id.tv_black, R.id.tv_report})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_black:
                clickButton(0);
                break;
            case R.id.tv_report:
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
