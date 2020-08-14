package com.sk.weichat.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.sk.weichat.R;


/**
 * Created by Administrator on 2017/6/19.
 */

public class LoadingDialog {
    /*
     *
     * ----------------------兼容性更好的方案-------------------------------
     *
     * */
    AlertDialog alertDialog;
    Context context;

    public LoadingDialog(Context context) {
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.dialog_loading, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(layout);
        alertDialog = builder.create();
    }

    /**
     * dip-->px
     */
    public int dip2Px(int dip) {
        // px/dip = density;
        float density = context.getResources().getDisplayMetrics().density;
        int px = (int) (dip * density + .5f);
        return px;
    }

    public void show() {
        alertDialog.show();
        WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
        lp.width = dip2Px(120);//定义宽度
        lp.height = dip2Px(120);
        alertDialog.getWindow().setAttributes(lp);
        alertDialog.getWindow().setDimAmount(0);
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(false);
    }

    public void dismiss() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }
    /*
    * ----------------------普通方案-------------------------------

    private int height;

    public LoadingDialog(Context context) {
        this(context, R.style.alert_dialog);
    }
    public LoadingDialog(Context context, int themeResId) {
        super(context, themeResId);
        setCancelable(true);
        setCanceledOnTouchOutside(false);

        float density = context.getResources().getDisplayMetrics().density;

        width = (int) (140 * density + .5f);
        height = (int) (105 * density + .5f);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);

    }



    @Override
    public void show() {
        super.show();

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = width;
        lp.height = height;
        window.setAttributes(lp);

        // 去掉周围的颜色
        getWindow().setDimAmount(0);

    } */

}
