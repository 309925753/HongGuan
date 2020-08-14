package com.redchamber.view.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.redchamber.view.bean.DateShowBean;
import com.sk.weichat.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 交友节目dialog
 */
public class DateShowAdapter extends BaseQuickAdapter<DateShowBean, DateShowAdapter.DateShowViewHolder> {

    private onShowClickListener mOnShowClickListener;

    public DateShowAdapter(@Nullable List<DateShowBean> data) {
        super(R.layout.item_rv_dialog_date_show, data);
    }

    @Override
    protected void convert(@NonNull DateShowViewHolder helper, DateShowBean item) {
        helper.mTvShow.setText(item.name);

//        if (item.isSelect) {
//            helper.mTvShow.setTextColor(mContext.getResources().getColor(R.color.color_FB719A));
//        } else {
//            helper.mTvShow.setTextColor(mContext.getResources().getColor(R.color.color_999999));
//        }

        helper.mTvShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.isSelect = !item.isSelect;
                notifyItemChanged(helper.getAdapterPosition());
                if (mOnShowClickListener != null) {
                    mOnShowClickListener.onShowItemClick(helper.getAdapterPosition(), item.name);
                }
            }
        });
    }

    public static class DateShowViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_show)
        TextView mTvShow;

        public DateShowViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public interface onShowClickListener {

        void onShowItemClick(int position, String show);

    }

    public void setOnShowClickListener(onShowClickListener mOnShowClickListener) {
        this.mOnShowClickListener = mOnShowClickListener;
    }

}
