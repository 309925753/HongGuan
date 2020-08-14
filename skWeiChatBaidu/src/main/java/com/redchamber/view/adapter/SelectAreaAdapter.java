package com.redchamber.view.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.redchamber.bean.ProvinceCityBean;
import com.sk.weichat.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectAreaAdapter extends BaseQuickAdapter<ProvinceCityBean, SelectAreaAdapter.ProvinceViewHolder> {

    private int mOldPosition = 0;
    private onProvinceClickListener mOnProvinceClickListener;

    public SelectAreaAdapter(@Nullable List<ProvinceCityBean> data) {
        super(R.layout.item_rv_resident_province, data);
    }

    @Override
    protected void convert(@NonNull ProvinceViewHolder helper, ProvinceCityBean item) {
        helper.mTvProvince.setText(item.name);

        if (mOldPosition == helper.getAdapterPosition()) {
            helper.mTvProvince.setTextColor(mContext.getResources().getColor(R.color.white));
            helper.mTvProvince.setBackgroundColor(mContext.getResources().getColor(R.color.color_FB719A));
        } else {
            helper.mTvProvince.setTextColor(mContext.getResources().getColor(R.color.color_333333));
            helper.mTvProvince.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }

        helper.mTvProvince.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOldPosition == helper.getAdapterPosition()) {
                    return;
                }
                item.isSelect = true;
                notifyItemChanged(mOldPosition);
                mOldPosition = helper.getAdapterPosition();
                notifyItemChanged(mOldPosition);
                if (mOnProvinceClickListener != null) {
                    mOnProvinceClickListener.onProvinceItemClick(mOldPosition);
                }
            }
        });
    }

    public static class ProvinceViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_province)
        TextView mTvProvince;

        public ProvinceViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public interface onProvinceClickListener {

        void onProvinceItemClick(int position);

    }

    public void setOnProvinceClickListener(onProvinceClickListener mOnProvinceClickListener) {
        this.mOnProvinceClickListener = mOnProvinceClickListener;
    }


}
