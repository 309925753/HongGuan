package com.redchamber.bar.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.bean.PhotoBean;
import com.redchamber.photo.BarPhotoActivity;
import com.redchamber.util.GlideUtils;
import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 图片相册
 */
public  class CommBarPhotoAdapter extends BaseQuickAdapter<PhotoBean, CommBarPhotoAdapter.PhotoViewHolder> {

    private Context mContext;
    private int mWidth;
    private int mPhotoNum = getItemCount();

    public CommBarPhotoAdapter(Context context,  List<PhotoBean> data) {
        super(R.layout.red_item_rv_friend_photo, data);
        this.mContext = context;
        this.mWidth = (ScreenUtil.getScreenWidth(mContext) - ScreenUtil.dip2px(mContext, 40)) / 4;
    }

    @Override
    public int getItemCount() {
        if (getData().size() > 8) {
            return 8;
        }
        return super.getItemCount();
    }

    @Override
    protected void convert(@NonNull CommBarPhotoAdapter.PhotoViewHolder helper, PhotoBean item) {
        ViewGroup.LayoutParams lp = helper.mRlRoot.getLayoutParams();
        lp.width = lp.height = mWidth;

        if (7 == helper.getAdapterPosition() && mPhotoNum > 8) {
            helper.mTvRest.setVisibility(View.VISIBLE);
            helper.mTvRest.setText(String.format("+%d", mPhotoNum - 8));
        } else {
            helper.mTvRest.setVisibility(View.GONE);
        }
        Glide.with(mContext).load(item.photoUrl).into(helper.mIvPhoto);
        helper.mTvStatus.setVisibility(View.GONE);

        helper.mIvPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BarPhotoActivity.startActivity(mContext, getData(), helper.getLayoutPosition());
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

        public PhotoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public void setPhotoNum(int photoNum) {
        this.mPhotoNum = photoNum;
    }

}

