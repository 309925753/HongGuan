package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;

public class ShareDialog extends Dialog implements View.OnClickListener {
    private LinearLayout tv1, tv2, tv3, tv4;

    private OnShareDialogClickListener onShareDialogClickListener;

    public ShareDialog(Context context, OnShareDialogClickListener mOnMsgSaveDaysDialogClickListener) {
        super(context, R.style.BottomDialog);
        this.onShareDialogClickListener = mOnMsgSaveDaysDialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_share);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        tv1 = (LinearLayout) findViewById(R.id.tv1);
        tv2 = (LinearLayout) findViewById(R.id.tv2);
        tv3 = (LinearLayout) findViewById(R.id.tv3);

        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
        tv3.setOnClickListener(this);


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
                onShareDialogClickListener.tv1Click();
                break;
            case R.id.tv2:
                onShareDialogClickListener.tv2Click();
                break;
            case R.id.tv3:
                onShareDialogClickListener.tv3Click();
                break;

        }
    }

    public interface OnShareDialogClickListener {
        void tv1Click();

        void tv2Click();

        void tv3Click();

    }
}
