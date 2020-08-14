package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;


public class NearSeachDialog extends Dialog implements View.OnClickListener {
    private TextView tv1, tv2, tv3, tv4;

    private OnNearSeachDialogClickListener mOnMsgSaveDaysDialogClickListener;

    public NearSeachDialog(Context context, OnNearSeachDialogClickListener mOnMsgSaveDaysDialogClickListener) {
        super(context, R.style.BottomDialog);
        this.mOnMsgSaveDaysDialogClickListener = mOnMsgSaveDaysDialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_near_seach);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv3 = (TextView) findViewById(R.id.tv3);
        tv4 = (TextView) findViewById(R.id.tv4);

        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
        tv3.setOnClickListener(this);
        tv4.setOnClickListener(this);


        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        // x/y坐标
        // lp.x = 100;
        // lp.y = 100;
        lp.width = ScreenUtil.getScreenWidth(getContext());
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.BOTTOM);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.tv1:
                mOnMsgSaveDaysDialogClickListener.tv1Click();
                break;
            case R.id.tv2:
                mOnMsgSaveDaysDialogClickListener.tv2Click();
                break;
            case R.id.tv3:
                mOnMsgSaveDaysDialogClickListener.tv3Click();
                break;
            case R.id.tv4:
                mOnMsgSaveDaysDialogClickListener.tv4Click();
                break;

        }
    }

    public interface OnNearSeachDialogClickListener {
        void tv1Click();

        void tv2Click();

        void tv3Click();

        void tv4Click();
    }
}
