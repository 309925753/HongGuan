package com.redchamber.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.info.adapter.SelectCityTagAdapter;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.util.CityUtils;
import com.redchamber.view.adapter.DateShowAdapter;
import com.redchamber.view.bean.DateShowBean;
import com.sk.weichat.R;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 期望对象
 */
public class ExpectObjectDialog extends Dialog implements DateShowAdapter.onShowClickListener {

    private Unbinder mBinder;

    @BindView(R.id.rv_object)
    RecyclerView mRvObject;
    @BindView(R.id.tfl_object)
    TagFlowLayout mTFLObject;

    private DateShowAdapter mAdapter;
    private OnConfirmListener mOnConfirmListener;
    private List<DateShowBean> dateShowBeanList = new ArrayList<>();
    private ArrayList<String> mSelectShow = new ArrayList<>();
    private SelectCityTagAdapter mShowTagAdapter;

    public ExpectObjectDialog(Context context) {
        super(context, R.style.BaseDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_expect_object);
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
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_confirm:
                if (mOnConfirmListener != null) {
                    mOnConfirmListener.onConfirmClick(CityUtils.formatCity(mSelectShow));
                }
                dismiss();
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

    private void initData() {
        String[] arr = getContext().getResources().getStringArray(R.array.expect_object);

        for (String str : arr) {
            dateShowBeanList.add(new DateShowBean(str));
        }
        mAdapter = new DateShowAdapter(dateShowBeanList);
        mRvObject.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvObject.setAdapter(mAdapter);
        mAdapter.setOnShowClickListener(this);

        mShowTagAdapter = new SelectCityTagAdapter(getContext(), mSelectShow);
        mTFLObject.setAdapter(mShowTagAdapter);
        mTFLObject.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                updateShowList(mSelectShow.get(position));
                mSelectShow.remove(position);
                mShowTagAdapter.notifyDataChanged();
                return false;
            }
        });
    }

    @Override
    public void onShowItemClick(int position, String show) {
        if (mSelectShow.contains(show)) {
            mSelectShow.remove(show);
            mShowTagAdapter.notifyDataChanged();
        } else {
            if (mSelectShow.size() >= 4) {
                ToastUtils.showToast("最多选择四个对象哦");
            } else {
                mSelectShow.add(show);
                mShowTagAdapter.notifyDataChanged();
            }
        }
    }

    public interface OnConfirmListener {
        void onConfirmClick(String show);
    }

    public void setOnConfirmListener(OnConfirmListener mOnConfirmListener) {
        this.mOnConfirmListener = mOnConfirmListener;
    }

    private void updateShowList(String show) {
        for (int i = 0; i < dateShowBeanList.size(); i++) {
            if (TextUtils.equals(show, dateShowBeanList.get(i).name)) {
                dateShowBeanList.get(i).isSelect = false;
                mAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

}
