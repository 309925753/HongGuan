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

/**
 * Created by zq on 2017/9/20 0020.
 * 保存图片
 */
public class SaveWindow extends Dialog {
    private TextView mIdentification;
    private View.OnClickListener itemsOnClick;
    private boolean isTrue;

    public SaveWindow(Context context, boolean isQRcode, View.OnClickListener itemsOnClick) {
        super(context, R.style.BottomDialog);
        this.itemsOnClick = itemsOnClick;
        this.isTrue = isQRcode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_to_galley);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        mIdentification = (TextView) findViewById(R.id.identification_qr_code);
        mIdentification.setVisibility(isTrue ? View.VISIBLE : View.GONE);
        // 设置按钮监听
        findViewById(R.id.save_image).setOnClickListener(itemsOnClick);
        findViewById(R.id.edit_image).setOnClickListener(itemsOnClick);
        findViewById(R.id.identification_qr_code).setOnClickListener(itemsOnClick);

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
}
