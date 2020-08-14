package com.redchamber.vip.adapter;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.redchamber.bean.VipRechargeBean;
import com.sk.weichat.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 会员中心
 */
public class VipCenterAdapter extends BaseQuickAdapter<VipRechargeBean, VipCenterAdapter.PackageViewHolder> {

    private int mOldPosition = -1;
    private onPackageClickListener mOnPackageClickListener;

    public VipCenterAdapter(@Nullable List<VipRechargeBean> data) {
        super(R.layout.red_item_rv_vip_center, data);
    }

    @Override
    protected void convert(@NonNull PackageViewHolder helper, VipRechargeBean item) {

        helper.mTvPriceDiscount.setText(String.format("%s元", item.price));
        helper.mTvTime.setText(item.ofTime);

        helper.mRlRoot.setBackgroundResource(item.isSelect ? R.drawable.red_shape_bg_item_vip_center_checked : R.drawable.red_shape_bg_item_vip_center);
        helper.mTvName.setTextColor(mContext.getResources().getColor(item.isSelect ? R.color.color_FB719A : R.color.color_333333));
        helper.mTvTime.setTextColor(mContext.getResources().getColor(item.isSelect ? R.color.color_FB719A : R.color.color_333333));
        helper.mLine.setBackgroundColor(mContext.getResources().getColor(item.isSelect ? R.color.color_FB719A : R.color.color_333333));
        helper.mTvPriceDiscount.setTextColor(mContext.getResources().getColor(item.isSelect ? R.color.color_FB719A : R.color.color_333333));

        helper.mRlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.isSelect) {
                    return;
                }
                if (mOldPosition >= 0) {
                    getData().get(mOldPosition).isSelect = false;
                }
                notifyItemChanged(mOldPosition);
                mOldPosition = helper.getAdapterPosition();
                getData().get(mOldPosition).isSelect = true;
                notifyItemChanged(mOldPosition);
                if (mOnPackageClickListener != null) {
                    mOnPackageClickListener.onPackageItemClick(item);
                }
            }
        });
    }

    public static class PackageViewHolder extends BaseViewHolder {

        @BindView(R.id.rl_root)
        RelativeLayout mRlRoot;
        @BindView(R.id.tv_name)
        TextView mTvName;
        @BindView(R.id.tv_recommend)
        TextView mTvRecommend;
        @BindView(R.id.tv_time)
        TextView mTvTime;
        @BindView(R.id.line)
        TextView mLine;
        @BindView(R.id.tv_price_discount)
        TextView mTvPriceDiscount;

        public PackageViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public interface onPackageClickListener {

        void onPackageItemClick(VipRechargeBean vipPackageBean);

    }

    public void setOnPackageClickListener(onPackageClickListener mOnPackageClickListener) {
        this.mOnPackageClickListener = mOnPackageClickListener;
    }

}
