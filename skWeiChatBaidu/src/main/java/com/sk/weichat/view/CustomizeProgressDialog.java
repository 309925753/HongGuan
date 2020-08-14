package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;

public class CustomizeProgressDialog extends Dialog {

    private String message;

    private TextView mCpTv;

    public CustomizeProgressDialog(Context context) {
        super(context, R.style.Browser_Dialog);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_dialog_custimize_progress_dialog);
        mCpTv = findViewById(R.id.cp_tv);
        if (!TextUtils.isEmpty(message)) {
            mCpTv.setText(message);
        }

        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        lp.width = ScreenUtil.getScreenWidth(getContext()) / 3 * 2;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.CENTER);
    }

}
