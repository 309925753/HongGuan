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

public class SelectCityAdapter  extends BaseQuickAdapter<ProvinceCityBean.CityBean, SelectCityAdapter.CityViewHolder> {

    private int mOldPosition = -1;
    private onCityClickListener mOnCityClickListener;

    public SelectCityAdapter(@Nullable List<ProvinceCityBean.CityBean> data) {
        super(R.layout.item_rv_resident_city, data);
    }

    @Override
    protected void convert(@NonNull CityViewHolder helper, ProvinceCityBean.CityBean item) {
        helper.mTvCity.setText(item.name);

        if (mOldPosition == helper.getAdapterPosition()) {
            helper.mTvCity.setTextColor(mContext.getResources().getColor(R.color.color_FB719A));
        } else {
            helper.mTvCity.setTextColor(mContext.getResources().getColor(R.color.color_333333));
        }

        helper.mTvCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.isSelect = true;
                notifyItemChanged(mOldPosition);
                mOldPosition = helper.getAdapterPosition();
                notifyItemChanged(mOldPosition);
                if (mOnCityClickListener != null) {
                    mOnCityClickListener.onCityItemClick(mOldPosition);
                }
            }
        });
    }

    public static class CityViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_city)
        TextView mTvCity;

        public CityViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public interface onCityClickListener {

        void onCityItemClick(int position);

    }

    public void setOnCityClickListener(onCityClickListener mOnCityClickListener) {
        this.mOnCityClickListener = mOnCityClickListener;
    }

}
