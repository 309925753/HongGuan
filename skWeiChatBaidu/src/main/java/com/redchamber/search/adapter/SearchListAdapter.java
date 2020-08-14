package com.redchamber.search.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.bean.RedIndexUser;
import com.redchamber.friend.FriendHomePageActivity;
import com.redchamber.util.GlideUtils;
import com.redchamber.util.RedAvatarUtils;
import com.redchamber.util.SplitUtils;
import com.redchamber.util.UserLevelUtils;
import com.sk.weichat.R;
import com.sk.weichat.ui.base.CoreManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 搜索列表
 */
public class SearchListAdapter extends BaseQuickAdapter<RedIndexUser, SearchListAdapter.GirlViewHolder> {

    public SearchListAdapter(@Nullable List<RedIndexUser> data) {
        super(R.layout.item_rv_girl_list, data);
    }

    @Override
    protected void convert(@NonNull GirlViewHolder helper, RedIndexUser item) {
        helper.mTvNickName.setText(item.nickname);
        helper.mTvCity.setText(item.cityName);
        helper.mTvAge.setText(SplitUtils.splitAgeConstellation(item.ageConstellation));
        helper.mTvJob.setText(item.position);
        helper.mTvDistance.setText(item.distance);
        helper.mTvOnline.setText(item.onlineStatus);
        if (item.photoNum > 0) {
            helper.mTvPhotoNum.setText(String.valueOf(item.photoNum));
            helper.mTvPhotoNum.setVisibility(View.VISIBLE);
        } else {
            helper.mTvPhotoNum.setVisibility(View.GONE);
        }

        if (item.photoAlbum != null) {
            //0公开 1申请访问 2付费
            if (1 == item.photoAlbum.type) {
                helper.mTvPhotoType.setText("申请访问");
                helper.mTvPhotoType.setVisibility(View.VISIBLE);
            } else if (2 == item.photoAlbum.type) {
                helper.mTvPhotoType.setText("付费相册");
                helper.mTvPhotoType.setVisibility(View.VISIBLE);
            } else {
                helper.mTvPhotoType.setVisibility(View.GONE);
            }
        }

        if (1 == item.collectStatus) {
            GlideUtils.load(mContext, R.mipmap.ic_star_checked, helper.mIvLike);
        } else {
            GlideUtils.load(mContext, R.mipmap.ic_star, helper.mIvLike);
        }

        Glide.with(mContext).load(R.mipmap.gif_debutante).into(helper.mIvDebutante);
        Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext, item.userId)).into(helper.mIvAvatar);

        boolean[] userLevels = UserLevelUtils.getLevels(item.userLevel);
        if (CoreManager.getInstance(mContext).getConfig().enablePayModule && userLevels[1]) {//VIP
            helper.mLabelVip.setVisibility(View.VISIBLE);
        } else {
            helper.mLabelVip.setVisibility(View.GONE);
        }
        if (userLevels[2]) {//女神
            helper.mLabelGirl.setVisibility(View.VISIBLE);
            helper.mLabelTrue.setVisibility(View.VISIBLE);
        } else {
            helper.mLabelGirl.setVisibility(View.GONE);
            if (userLevels[3]) {//真人
                helper.mLabelTrue.setVisibility(View.VISIBLE);
            } else {
                helper.mLabelTrue.setVisibility(View.GONE);
            }
        }
        if (userLevels[4]) { //名媛
            helper.mIvDebutante.setVisibility(View.VISIBLE);
        } else {
            helper.mIvDebutante.setVisibility(View.GONE);
        }
        if (1 == item.collectStatus) {
            helper.mIvLike.setImageResource(R.mipmap.ic_star_checked);
        } else {
            helper.mIvLike.setImageResource(R.mipmap.ic_star);
        }

        helper.mRlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendHomePageActivity.startFriendHomePageActivity(mContext, item.userId);
            }
        });

    }

    public static class GirlViewHolder extends BaseViewHolder {

        @BindView(R.id.rl_root)
        RelativeLayout mRlRoot;
        @BindView(R.id.iv_avatar)
        RoundedImageView mIvAvatar;
        @BindView(R.id.tv_nickname)
        TextView mTvNickName;
        @BindView(R.id.tv_girl)
        TextView mLabelGirl; //女神标签
        @BindView(R.id.tv_true)
        TextView mLabelTrue; //真人标签
        @BindView(R.id.tv_vip)
        TextView mLabelVip;  //VIP标签
        @BindView(R.id.iv_debutante)
        ImageView mIvDebutante;  //女媛标签
        @BindView(R.id.tv_city)
        TextView mTvCity;
        @BindView(R.id.tv_age)
        TextView mTvAge;
        @BindView(R.id.tv_job)
        TextView mTvJob;
        @BindView(R.id.tv_distance)
        TextView mTvDistance;
        @BindView(R.id.tv_online)
        TextView mTvOnline;
        @BindView(R.id.tv_photo)
        TextView mTvPhotoType;
        @BindView(R.id.tv_photo_num)
        TextView mTvPhotoNum;
        @BindView(R.id.iv_like)
        ImageView mIvLike;

        public GirlViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
