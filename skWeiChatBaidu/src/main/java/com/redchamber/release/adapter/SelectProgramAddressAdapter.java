package com.redchamber.release.adapter;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sk.weichat.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 选择节目地点
 */
public class SelectProgramAddressAdapter extends BaseQuickAdapter<String, SelectProgramAddressAdapter.AddressViewHolder> {

    private onAddressClickListener mOnAddressClickListener;

    public SelectProgramAddressAdapter(@Nullable List<String> data) {
        super(R.layout.red_item_rv_select_program_address, data);
    }

    @Override
    protected void convert(@NonNull AddressViewHolder helper, String item) {
        helper.mTvName.setText(item);
//        helper.mTvAddress.setText(item.name);

        helper.mLlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnAddressClickListener != null) {
                    mOnAddressClickListener.onAddressItemClick(item);
                }
            }
        });
    }

    public static class AddressViewHolder extends BaseViewHolder {

        @BindView(R.id.ll_root)
        LinearLayout mLlRoot;
        @BindView(R.id.tv_name)
        TextView mTvName;
        @BindView(R.id.tv_address)
        TextView mTvAddress;

        public AddressViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public interface onAddressClickListener {

        void onAddressItemClick(String address);

    }

    public void setOnAddressClickListener(onAddressClickListener mOnAddressClickListener) {
        this.mOnAddressClickListener = mOnAddressClickListener;
    }

}
