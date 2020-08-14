package com.redchamber.wallet.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.redchamber.bean.RechargeRedBean;
import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 充值
 */
public class RechargeAdapter extends BaseQuickAdapter<RechargeRedBean, RechargeAdapter.PackageViewHolder> {

    private int mWidth;
    private int mOldPosition = -1;
    private onPackageClickListener mOnPackageClickListener;

    public RechargeAdapter(Context context, @Nullable List<RechargeRedBean> data) {
        super(R.layout.red_item_rv_recharge_center, data);
        this.mWidth = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 45)) / 3;
    }

    @Override
    protected void convert(@NonNull PackageViewHolder helper, RechargeRedBean item) {
        ViewGroup.LayoutParams lp = helper.mRlRoot.getLayoutParams();
        lp.width = mWidth;

        helper.mTvMoney.setText(item.money + "元");
        helper.mTvNum.setText(String.valueOf(item.redBeanNum));
        helper.mRlRoot.setBackgroundResource(item.isSelect ? R.drawable.red_shape_bg_item_recharge_center_checked : R.drawable.red_shape_bg_item_recharge_center);

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
        @BindView(R.id.tv_num)
        TextView mTvNum;
        @BindView(R.id.tv_money)
        TextView mTvMoney;
        @BindView(R.id.rl_root)
        RelativeLayout mRlRoot;

        public PackageViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public interface onPackageClickListener {

        void onPackageItemClick(RechargeRedBean rechargeRedBean);

    }

    public void setOnPackageClickListener(onPackageClickListener mOnPackageClickListener) {
        this.mOnPackageClickListener = mOnPackageClickListener;
    }

}
