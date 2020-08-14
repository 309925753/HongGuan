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
 * 职业-选择职业
 */
public class SelectJobAdapter extends BaseQuickAdapter<IndustryJobBean.Job, SelectJobAdapter.JobViewHolder> {

    private onJobClickListener mOnJobClickListener;

    public SelectJobAdapter(@Nullable List<IndustryJobBean.Job> data) {
        super(R.layout.item_rv_select_job, data);
    }

    @Override
    protected void convert(@NonNull JobViewHolder helper, IndustryJobBean.Job item) {
        helper.mTvJob.setText(item.name);

        helper.mTvJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnJobClickListener != null) {
                    mOnJobClickListener.onJobItemClick(item.name);
                }
            }
        });
    }

    public static class JobViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_job)
        TextView mTvJob;

        public JobViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public interface onJobClickListener {

        void onJobItemClick(String job);

    }

    public void setOnJobClickListener(onJobClickListener mOnJobClickListener) {
        this.mOnJobClickListener = mOnJobClickListener;
    }
}
