package com.redchamber.info.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.redchamber.bean.IndustryJobBean;
import com.sk.weichat.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 职业-选择行业
 */
public class SelectIndustryAdapter extends BaseQuickAdapter<IndustryJobBean, SelectIndustryAdapter.IndustryViewHolder> {

    private int mOldPosition = 0;
    private onIndustryClickListener mOnIndustryClickListener;

    public SelectIndustryAdapter(@Nullable List<IndustryJobBean> data) {
        super(R.layout.item_rv_select_industry, data);
    }

    @Override
    protected void convert(@NonNull IndustryViewHolder helper, IndustryJobBean item) {
        helper.mTvIndustry.setText(item.profession);

        if (mOldPosition == helper.getAdapterPosition()) {
            helper.mTvIndustry.setTextColor(mContext.getResources().getColor(R.color.white));
            helper.mTvIndustry.setBackgroundColor(mContext.getResources().getColor(R.color.color_FB719A));
        } else {
            helper.mTvIndustry.setTextColor(mContext.getResources().getColor(R.color.color_333333));
            helper.mTvIndustry.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }

        helper.mTvIndustry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.isSelect = true;
                notifyItemChanged(mOldPosition);
                mOldPosition = helper.getAdapterPosition();
                notifyItemChanged(mOldPosition);
                if (mOnIndustryClickListener != null) {
                    mOnIndustryClickListener.onIndustryItemClick(mOldPosition);
                }
            }
        });
    }

    public static class IndustryViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_industry)
        TextView mTvIndustry;

        public IndustryViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public interface onIndustryClickListener {

        void onIndustryItemClick(int position);

    }

    public void setOnIndustryClickListener(onIndustryClickListener mOnIndustryClickListener) {
        this.mOnIndustryClickListener = mOnIndustryClickListener;
    }
}
