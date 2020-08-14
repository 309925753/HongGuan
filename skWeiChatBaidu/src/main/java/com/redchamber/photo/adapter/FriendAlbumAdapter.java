package com.redchamber.photo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.bean.PhotoBean;
import com.redchamber.photo.PreviewFriendPhotosActivity;
import com.redchamber.util.GlideUtils;
import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 他人相册
 */
public class FriendAlbumAdapter extends BaseQuickAdapter<PhotoBean, FriendAlbumAdapter.PhotoViewHolder> {

    private Context mContext;
    private int mWidth;
    private String mUserId;

    public FriendAlbumAdapter(Context context, List<PhotoBean> data, String userId) {
        super(R.layout.red_item_rv_friend_photo, data);
        this.mContext = context;
        this.mWidth = (ScreenUtil.getScreenWidth(mContext) - ScreenUtil.dip2px(mContext, 40)) / 4;
        this.mUserId = userId;
    }

    @Override
    protected void convert(@NonNull PhotoViewHolder helper, PhotoBean item) {
        ViewGroup.LayoutParams lp = helper.mRlRoot.getLayoutParams();
        lp.width = lp.height = mWidth;

        if (1 == item.visitType) {
            helper.mTvStatus.setVisibility(View.VISIBLE);
            helper.mTvStatus.setText("阅后即焚");
            if (1 == item.status) {
                helper.mTvStatus.setText("已焚毁");
            }
            GlideUtils.loadBlur(mContext, item.photoUrl, helper.mIvPhoto, mWidth, mWidth);
        } else if (2 == item.visitType) {
            helper.mTvStatus.setVisibility(View.VISIBLE);
            helper.mTvStatus.setText("红包照片");
            if (1 != item.status) {
                GlideUtils.loadBlur(mContext, item.photoUrl, helper.mIvPhoto, mWidth, mWidth);
            } else {
                GlideUtils.load(mContext, item.photoUrl, helper.mIvPhoto, mWidth, mWidth);
            }
        } else {
            helper.mTvStatus.setVisibility(View.GONE);
            GlideUtils.load(mContext, item.photoUrl, helper.mIvPhoto, mWidth, mWidth);
        }

        if (1 == item.isSelf) {
            helper.mTvSelf.setVisibility(View.VISIBLE);
        } else {
            helper.mTvSelf.setVisibility(View.GONE);
        }

        helper.mIvPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreviewFriendPhotosActivity.startActivity(mContext, getData(), mUserId, helper.getLayoutPosition());
            }
        });

    }

    public static class PhotoViewHolder extends BaseViewHolder {

        @BindView(R.id.rl_root)
        RelativeLayout mRlRoot;
        @BindView(R.id.iv_photo)
        RoundedImageView mIvPhoto;
        @BindView(R.id.tv_status)
        TextView mTvStatus;
        @BindView(R.id.tv_rest)
        TextView mTvRest;
        @BindView(R.id.tv_self)
        TextView mTvSelf;

        public PhotoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
