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

public class SaveVideoDialog extends Dialog implements View.OnClickListener {

    private TextView tv1;

    private OnSavaVideoDialogClickListener mOnSavaVideoDialogClickListener;

    public SaveVideoDialog(Context context, OnSavaVideoDialogClickListener mOnSavaVideoDialogClickListener) {
        super(context, R.style.BottomDialog);
        this.mOnSavaVideoDialogClickListener = mOnSavaVideoDialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_sava_video);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        tv1 = (TextView) findViewById(R.id.tv1);
        tv1.setOnClickListener(this);

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
        switch (v.getId()) {
            case R.id.tv1:
                mOnSavaVideoDialogClickListener.tv1Click();
                break;
        }
    }

    public interface OnSavaVideoDialogClickListener {
        void tv1Click();
    }
}
