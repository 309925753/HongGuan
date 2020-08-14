package com.redchamber.wallet.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.redchamber.bean.WalletDetailBean;
import com.sk.weichat.R;
import com.sk.weichat.util.DateUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 钱包-明细
 */
public class WalletDetailAdapter extends BaseQuickAdapter<WalletDetailBean, WalletDetailAdapter.PackageViewHolder> {

    public WalletDetailAdapter(@Nullable List<WalletDetailBean> data) {
        super(R.layout.red_item_rv_wallet_center, data);
    }

    @Override
    protected void convert(@NonNull PackageViewHolder helper, WalletDetailBean item) {
        helper.mTvType.setText(item.msg);
        helper.mTvTime.setText(DateUtils.date2TimeStamp2(item.time * 1000));
        if (1 == item.state) {//收入
            helper.mTvBeanNum.setText("+" + item.money);
            helper.mTvBeanNum.setTextColor(mContext.getResources().getColor(R.color.color_FB719A));
        } else if (-1 == item.state) {
            helper.mTvBeanNum.setText("-" + item.money);
            helper.mTvBeanNum.setTextColor(mContext.getResources().getColor(R.color.color_333333));
        }
        if (TextUtils.isEmpty(item.progress)) {
            helper.mTvProgress.setVisibility(View.GONE);
        } else {
            helper.mTvProgress.setVisibility(View.VISIBLE);
            helper.mTvProgress.setText(item.progress);
        }
    }

    public static class PackageViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_cost_type)
        TextView mTvType;
        @BindView(R.id.tv_exchange_time)
        TextView mTvTime;
        @BindView(R.id.tv_red_num)
        TextView mTvBeanNum;
        @BindView(R.id.tv_progress)
        TextView mTvProgress;

        public PackageViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
