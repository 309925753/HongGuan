package com.redchamber.home.adapter;

import android.content.Context;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.redchamber.bean.HomeHeaderBean;
import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeHeaderAdapter extends BaseQuickAdapter<HomeHeaderBean, HomeHeaderAdapter.HeaderViewHolder> {

    private int mWidth;
    private int mOldPosition = 0;
    private onHeaderClickListener mOnHeaderClickListener;

    public HomeHeaderAdapter(Context context, @Nullable List<HomeHeaderBean> data) {
        super(R.layout.red_item_rv_home_header, data);
        this.mWidth = (int) (ScreenUtil.getScreenWidth(context) * 1.0f / data.size());
    }

    @Override
    protected void convert(@NonNull HeaderViewHolder helper, HomeHeaderBean item) {
        ViewGroup.LayoutParams lp = helper.mTvHead.getLayoutParams();
        lp.width = mWidth;
        helper.mTvHead.setText(item.type);

        if (item.isChecked) {
            helper.mTvHead.setTextColor(mContext.getResources().getColor(R.color.color_FB719A));
            TextPaint tp = helper.mTvHead.getPaint();
            tp.setFakeBoldText(true);
        } else {
            helper.mTvHead.setTextColor(mContext.getResources().getColor(R.color.color_222222));
            TextPaint tp = helper.mTvHead.getPaint();
            tp.setFakeBoldText(false);
        }

        helper.mTvHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOldPosition == helper.getAdapterPosition()) {
                    return;
                }
                item.isChecked = true;
                notifyItemChanged(mOldPosition);
                mOldPosition = helper.getAdapterPosition();
                notifyItemChanged(mOldPosition);
                if (mOnHeaderClickListener != null) {
                    mOnHeaderClickListener.onHeaderClick(mOldPosition);
                }
            }
        });

    }

    public static class HeaderViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_head)
        TextView mTvHead;

        public HeaderViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface onHeaderClickListener {
        void onHeaderClick(int position);
    }

    public void setOnHeaderClickListener(onHeaderClickListener mOnHeaderClickListener) {
        this.mOnHeaderClickListener = mOnHeaderClickListener;
    }

}
