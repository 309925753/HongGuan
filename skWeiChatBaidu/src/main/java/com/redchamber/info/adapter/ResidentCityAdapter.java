package com.redchamber.info.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.redchamber.bean.ProvinceCityBean;
import com.sk.weichat.R;
import com.sk.weichat.bean.Area;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 常驻城市-城市
 */
public class ResidentCityAdapter extends BaseQuickAdapter<Area, ResidentCityAdapter.CityViewHolder> {

    private int mOldPosition = 0;
    private onCityClickListener mOnCityClickListener;

    public ResidentCityAdapter(@Nullable List<Area> data) {
        super(R.layout.item_rv_resident_city, data);
    }

    @Override
    protected void convert(@NonNull CityViewHolder helper, Area item) {
        helper.mTvCity.setText(item.getName());

//        if (mOldPosition == helper.getAdapterPosition()) {
//            helper.mTvCity.setTextColor(mContext.getResources().getColor(R.color.color_FB719A));
//        } else {
//            helper.mTvCity.setTextColor(mContext.getResources().getColor(R.color.color_333333));
//        }

        helper.mTvCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.isSelect = true;
//                notifyItemChanged(mOldPosition);
                mOldPosition = helper.getAdapterPosition();
//                notifyItemChanged(mOldPosition);
                if (mOnCityClickListener != null) {
                    mOnCityClickListener.onCityItemClick(item.getName());
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

        void onCityItemClick(String city);

    }

    public void setOnCityClickListener(onCityClickListener mOnCityClickListener) {
        this.mOnCityClickListener = mOnCityClickListener;
    }
}
