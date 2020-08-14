package com.redchamber.like.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.bean.BlackListBean;
import com.redchamber.util.RedAvatarUtils;
import com.sk.weichat.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 黑名单
 */
public class BlackListAdapter extends BaseQuickAdapter<BlackListBean.BlackUser, BlackListAdapter.BlackViewHolder> {

    public BlackListAdapter(@Nullable List<BlackListBean.BlackUser> data) {
        super(R.layout.red_item_rv_black_list, data);
    }

    @Override
    protected void convert(@NonNull BlackViewHolder helper, BlackListBean.BlackUser item) {
        helper.mTvNickName.setText(item.nickname);
        Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext, item.friendId)).into(helper.mIvAvatar);
    }

    public static class BlackViewHolder extends BaseViewHolder {

        @BindView(R.id.iv_avatar)
        RoundedImageView mIvAvatar;
        @BindView(R.id.tv_nickname)
        TextView mTvNickName;

        public BlackViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
