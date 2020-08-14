package com.redchamber.view.adapter;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.redchamber.bean.YourCommentBean;
import com.sk.weichat.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendCommentAdapter extends BaseQuickAdapter<YourCommentBean, FriendCommentAdapter.CommentViewHolder> {

    public FriendCommentAdapter(List<YourCommentBean> data) {
        super(R.layout.item_rv_dialog_your_comment, data);
    }

    @Override
    protected void convert(@NonNull CommentViewHolder helper, YourCommentBean item) {
        helper.mTvNum.setText(String.valueOf(item.num));
        helper.mTvType.setText(item.type);

        if (item.isChecked) {
            helper.mTvNum.setBackground(mContext.getResources().getDrawable(R.drawable.red_shape_fb719a_3));
        } else {
            helper.mTvNum.setBackground(mContext.getResources().getDrawable(R.drawable.red_shape_efefef_3));
        }

        helper.mLlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item.isChecked = !item.isChecked;
                notifyItemChanged(helper.getAdapterPosition());
            }
        });

    }

    public static class CommentViewHolder extends BaseViewHolder {

        @BindView(R.id.ll_root)
        LinearLayout mLlRoot;
        @BindView(R.id.tv_num)
        TextView mTvNum;
        @BindView(R.id.tv_type)
        TextView mTvType;

        public CommentViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }


}
