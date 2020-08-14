package com.redchamber.radio.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.bean.DiscussesBean;
import com.redchamber.util.RedAvatarUtils;
import com.sk.weichat.R;
import com.sk.weichat.util.TimeUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 个人中心 点赞列表
 */
public class ThumbDetailListAdapter extends BaseQuickAdapter<DiscussesBean, ThumbDetailListAdapter.ThumbViewHolder> {


    public ThumbDetailListAdapter(@Nullable List<DiscussesBean> data) {
        super(R.layout.red_item_rv_thumb_detail_list, data);
    }

    @Override
    protected void convert(@NonNull ThumbViewHolder helper, DiscussesBean item) {
        helper.tvNickname.setText(item.getNickName());
        Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext,String.valueOf(item.getUserId()))).into(helper.ivAvatar);
        helper.tvTime.setText(TimeUtils.getFriendlyTimeDesc(mContext, item.getJoinTime()));

    }

    public static class ThumbViewHolder extends BaseViewHolder {
        @BindView(R.id.iv_avatar)
        RoundedImageView ivAvatar;
        @BindView(R.id.tv_nickname)
        TextView tvNickname;
        @BindView(R.id.tv_girl)
        TextView tvGirl;
        @BindView(R.id.tv_vip)
        TextView tvVip;
        @BindView(R.id.iv_debutante)
        ImageView ivDebutante;
        @BindView(R.id.ll_user)
        LinearLayout llUser;
        @BindView(R.id.tv_time)
        TextView tvTime;


        public ThumbViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
