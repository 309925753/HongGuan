package com.redchamber.view.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sk.weichat.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectHeightAdapter extends BaseQuickAdapter<String, SelectHeightAdapter.HeightViewHolder> {

    private onHeightClickListener mOnHeightClickListener;

    public SelectHeightAdapter(@Nullable List<String> data) {
        super(R.layout.item_rv_dialog_select_height, data);
    }

    @Override
    protected void convert(@NonNull HeightViewHolder helper, String item) {
        helper.mTvHeight.setText(item);

        helper.mTvHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnHeightClickListener != null) {
                    mOnHeightClickListener.onHeightClick(item);
                }
            }
        });
    }

    public static class HeightViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_height)
        TextView mTvHeight;

        public HeightViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public interface onHeightClickListener {

        void onHeightClick(String height);

    }

    public void setOnHeightClickListener(onHeightClickListener mOnHeightClickListener) {
        this.mOnHeightClickListener = mOnHeightClickListener;
    }
}
