package com.redchamber.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.view.adapter.SelectHeightAdapter;
import com.sk.weichat.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 身高
 */
public class SelectHeightDialog extends Dialog {

    private Unbinder mBinder;

    @BindView(R.id.rv_height)
    RecyclerView mRvHeight;

    private SelectHeightAdapter mAdapter;
    private OnConfirmListener mOnConfirmListener;

    public SelectHeightDialog(Context context) {
        super(context, R.style.BaseDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_select_height);
        mBinder = ButterKnife.bind(this);
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        setCanceledOnTouchOutside(false);

        initData();
    }

    @OnClick({R.id.tv_cancel, R.id.tv_confirm})
    void onClick(View view) {
        dismiss();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mBinder != null) {
            mBinder.unbind();
        }
    }

    private void initData() {
        List<String> heightList = new ArrayList<>();
//        heightList.add("不显示");
        for (int i = 140; i <= 230; i++) {
            heightList.add(String.valueOf(i));
        }
        mAdapter = new SelectHeightAdapter(heightList);
        mRvHeight.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvHeight.setAdapter(mAdapter);
        mAdapter.setOnHeightClickListener(new SelectHeightAdapter.onHeightClickListener() {
            @Override
            public void onHeightClick(String height) {
                if (mOnConfirmListener != null) {
                    mOnConfirmListener.onConfirmClick(height);
                }
                dismiss();
            }
        });

    }

    public interface OnConfirmListener {
        void onConfirmClick(String height);
    }

    public void setOnConfirmListener(OnConfirmListener mOnConfirmListener) {
        this.mOnConfirmListener = mOnConfirmListener;
    }
}
