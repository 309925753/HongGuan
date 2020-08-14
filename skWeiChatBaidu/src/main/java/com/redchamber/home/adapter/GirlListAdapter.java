package com.redchamber.home.adapter;

import android.text.TextUtils;
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
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.util.GlideUtils;
import com.redchamber.util.SplitUtils;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;

/**
 * 红馆首页附近、新注册、女神、女媛公用一个adapter
 */
public class GirlListAdapter extends BaseQuickAdapter<RedIndexUser, GirlListAdapter.GirlViewHolder> {

    public GirlListAdapter(@Nullable List<RedIndexUser> data) {
        super(R.layout.item_rv_girl_list, data);
    }

    @Override
    protected void convert(@NonNull GirlViewHolder helper, RedIndexUser item) {
        helper.mTvNickName.setText(item.nickname);
        helper.mTvCity.setText(item.cityName);
        helper.mTvAge.setText(SplitUtils.splitAgeConstellation(item.ageConstellation));
        if (TextUtils.isEmpty(item.position)) {
            helper.mTvJob.setVisibility(View.GONE);
        } else {
            helper.mTvJob.setVisibility(View.VISIBLE);
            helper.mTvJob.setText(item.position);
        }
        helper.mTvDistance.setText(item.distance);
        helper.mTvOnline.setText(item.onlineStatus);
        if (item.photoNum > 0) {
            helper.mTvPhotoNum.setText(String.valueOf(item.photoNum));
            helper.mTvPhotoNum.setVisibility(View.VISIBLE);
        } else {
            helper.mTvPhotoNum.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(item.userLevel) && item.userLevel.length() == 5) {
            char[] arr = item.userLevel.toCharArray();
            if ('0' == arr[0]) {
                //性别
            }
            if (CoreManager.getInstance(mContext).getConfig().enablePayModule && '1' == arr[1]) {//VIP
                helper.mLabelVip.setVisibility(View.VISIBLE);
            } else {
                helper.mLabelVip.setVisibility(View.GONE);
            }
            if ('1' == arr[2]) {//女神
                helper.mLabelGirl.setVisibility(View.VISIBLE);
                helper.mLabelTrue.setVisibility(View.GONE);
            } else {
                helper.mLabelGirl.setVisibility(View.GONE);
                if ('1' == arr[3]) {//真人
                    helper.mLabelTrue.setVisibility(View.VISIBLE);
                } else {
                    helper.mLabelTrue.setVisibility(View.GONE);
                }
            }
            if ('1' == arr[4] && item.showBadge == 1) {//徽章
                helper.mIvDebutante.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(R.mipmap.gif_debutante).into(helper.mIvDebutante);
            } else {
                helper.mIvDebutante.setVisibility(View.GONE);
            }
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

        GlideUtils.loadAvatar(mContext, item.userId, helper.mIvAvatar);

        helper.mRlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendHomePageActivity.startFriendHomePageActivity(mContext, item);
            }
        });

        helper.mIvLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (0 == item.collectStatus) {
                    addCollection(helper.getAdapterPosition(), item.userId, "0", helper.mIvLike);
                } else {
                    addCollection(helper.getAdapterPosition(), item.userId, "1", helper.mIvLike);
                }
            }
        });

    }

    static class GirlViewHolder extends BaseViewHolder {

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

    private void addCollection(int position, String friendId, String state, ImageView imageView) {
        DialogHelper.showDefaulteMessageProgressDialog(mContext);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(mContext).getSelfStatus().accessToken);
        params.put("friendId", friendId);
        params.put("state", state);// 0：添加 1：取消

        HttpUtils.post().url(CoreManager.getInstance(mContext).getConfig().RED_ADD_COLLECTION)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();

                        if (result.getResultCode() == 1) {
                            getData().get(position).collectStatus = "0".equals(state) ? 1 : 0;
                            notifyItemChanged(position);
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

}
