package com.sk.weichat.ui.xrce;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;


/**
 * 抖音封面选择模块
 */
public class SelectCoverDialog extends Dialog implements View.OnClickListener {

    private Context mContext;
    private String musicListUrl;
    private Xcoverbar mCoverBar;
    private Xcoverbar.OnChangeListener mListener;

    public SelectCoverDialog(Context context, Xcoverbar.OnChangeListener listener) {
        super(context, R.style.TrillDialog);
        this.mContext = context;
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_trill_cover);
        setCanceledOnTouchOutside(true);

        initView();
    }

    private void initView() {
        findViewById(R.id.iv_close).setOnClickListener(this);
        findViewById(R.id.iv_select).setOnClickListener(this);
        mCoverBar = findViewById(R.id.xcover_bar);
        mCoverBar.addOnChangeListener(new Xcoverbar.OnChangeListener() {
            @Override
            public void change(String curr) {
                musicListUrl = curr;
                if (mListener != null) {
                    mListener.change(curr);
                }
            }

            @Override
            public void confirm(String curr) {

            }

            @Override
            public void cancel() {

            }
        });
        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        lp.width = ScreenUtil.getScreenWidth(getContext());
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.BOTTOM);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }

    @Override
    public void onClick(View v) {
        hideCover(v.getId() == R.id.iv_select);
    }

    public void show(String str) {
        super.show();
        mCoverBar.setCoverBackground(str);
    }

    public void hideCover(boolean sure) {
        super.dismiss();
        if (mListener != null) {
            if (sure) {
                mListener.confirm(musicListUrl);
            } else {
                mListener.cancel();
            }
        }
    }

    @Override
    public void dismiss() {
        hideCover(false);
    }

}
