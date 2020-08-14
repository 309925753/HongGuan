package com.redchamber.invite.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.bean.InviteCodeBean;
import com.redchamber.util.RedAvatarUtils;
import com.sk.weichat.R;
import com.sk.weichat.util.DateUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 邀请人数
 */
public class MyInviteAdapter extends BaseQuickAdapter<InviteCodeBean.InviteUser, MyInviteAdapter.InviteViewHolder> {

    public MyInviteAdapter(@Nullable List<InviteCodeBean.InviteUser> data) {
        super(R.layout.red_item_rv_my_invite, data);
    }

    @Override
    protected void convert(@NonNull InviteViewHolder helper, InviteCodeBean.InviteUser item) {
        Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext, item.userId)).into(helper.mIvAvatar);
        helper.mTvNickName.setText(item.nickname);
        helper.mTvTime.setText(DateUtils.date2TimeStamp(item.createTime * 1000));
    }

    public static class InviteViewHolder extends BaseViewHolder {

        @BindView(R.id.iv_avatar)
        RoundedImageView mIvAvatar;
        @BindView(R.id.tv_nickname)
        TextView mTvNickName;
        @BindView(R.id.tv_time)
        TextView mTvTime;

        public InviteViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
