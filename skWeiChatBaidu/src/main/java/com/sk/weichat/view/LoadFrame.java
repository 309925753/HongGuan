package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;


/**
 * 对话框
 */
public class LoadFrame extends Dialog {
    private TextView
            mTitle,
            mCancel,
            mConfirm;
    private ProgressBar mSendingPb;
    private ImageView mOkIv;

    private String mCancelString;
    private String mConfirmString;

    private OnLoadFrameClickListener mOnLoadFrameClickListener;

    public LoadFrame(Context context) {
        super(context, R.style.BottomDialog);
    }

    public void setSomething(String cancel, OnLoadFrameClickListener onLoadFrameClickListener) {
        this.mCancelString = cancel;
        this.mOnLoadFrameClickListener = onLoadFrameClickListener;
    }

    public void setSomething(String cancel, String confirm, OnLoadFrameClickListener onLoadFrameClickListener) {
        this.mCancelString = cancel;
        this.mConfirmString = confirm;
        this.mOnLoadFrameClickListener = onLoadFrameClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_frame);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView() {
        mTitle = (TextView) findViewById(R.id.title);
        mSendingPb = findViewById(R.id.ing_pb);
        mOkIv = findViewById(R.id.ok_iv);
        mCancel = (TextView) findViewById(R.id.cancel);
        mConfirm = (TextView) findViewById(R.id.confirm);

        if (!TextUtils.isEmpty(mCancelString)) {
            mCancel.setText(mCancelString);
        }

        if (!TextUtils.isEmpty(mConfirmString)) {
            mConfirm.setText(mConfirmString);
        }

        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        lp.width = (int) (ScreenUtil.getScreenWidth(getContext()) * 0.85);
        lp.gravity = Gravity.CENTER;
        o.setAttributes(lp);
        initEvent();
    }

    private void initEvent() {
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mOnLoadFrameClickListener != null) {
                    mOnLoadFrameClickListener.cancelClick();
                }
            }
        });
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mOnLoadFrameClickListener != null) {
                    mOnLoadFrameClickListener.confirmClick();
                }
            }
        });
    }

    public void change() {
        mTitle.setText(MyApplication.getContext().getString(R.string.is_send_ok));
        mSendingPb.setVisibility(View.GONE);
        mOkIv.setVisibility(View.VISIBLE);
    }

    public void failed() {
        // TODO: 优化一下失败的界面，
        mTitle.setText(MyApplication.getContext().getString(R.string.send_failed));
        mSendingPb.setVisibility(View.GONE);
        mOkIv.setVisibility(View.GONE);
    }

    public interface OnLoadFrameClickListener {

        void cancelClick();

        void confirmClick();
    }
}
