package com.redchamber.radio.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.redchamber.bean.PageDataBean;
import com.sk.weichat.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 个人中心 动态详情-评论
 */
public class MomentDetailCommentAdapter extends BaseQuickAdapter<PageDataBean.DiscussesBean, MomentDetailCommentAdapter.CommentViewHolder> {

    public MomentDetailCommentAdapter(@Nullable List<PageDataBean.DiscussesBean> data) {
        super(R.layout.red_item_rv_comment_list, data);
    }

    @Override
    public int getItemCount() {
        if (getData().size() > 5) {
            return 5;
        }
        return super.getItemCount();
    }

    @Override
    protected void convert(@NonNull CommentViewHolder helper, PageDataBean.DiscussesBean item) {
        helper.mTvContent.setText(item.getContent());
        helper.mTvUserName.setText(item.getNickName());

    }

    public static class CommentViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_user_name)
        TextView mTvUserName;
        @BindView(R.id.tv_content)
        TextView mTvContent;

        public CommentViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
