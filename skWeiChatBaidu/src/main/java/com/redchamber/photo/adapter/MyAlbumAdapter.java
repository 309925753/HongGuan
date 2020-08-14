package com.redchamber.photo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.bean.PhotoBean;
import com.redchamber.photo.PreviewPhotosActivity;
import com.redchamber.util.GlideUtils;
import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 我的相册
 */
public class MyAlbumAdapter extends BaseQuickAdapter<PhotoBean, MyAlbumAdapter.PhotoViewHolder> {

    private int mWidth;

    public MyAlbumAdapter(Context context, @Nullable List<PhotoBean> data) {
        super(R.layout.red_item_rv_my_album, data);
        this.mWidth = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 45)) / 4;
    }

    @Override
    protected void convert(@NonNull MyAlbumAdapter.PhotoViewHolder helper, PhotoBean item) {
        ViewGroup.LayoutParams lp = helper.mRlRoot.getLayoutParams();
        lp.width = lp.height = mWidth;

        GlideUtils.load(mContext, item.photoUrl, helper.mIvPhoto);

        if (1 == item.visitType) {
            helper.mTvStatus.setVisibility(View.VISIBLE);
            helper.mTvStatus.setText("阅后即焚");
        } else if (2 == item.visitType) {
            helper.mTvStatus.setVisibility(View.VISIBLE);
            helper.mTvStatus.setText("红包照片");
        } else {
            helper.mTvStatus.setVisibility(View.GONE);
        }

        if (1 == item.isSelf) {
            helper.mTvSelf.setVisibility(View.VISIBLE);
        } else {
            helper.mTvSelf.setVisibility(View.GONE);
        }

        helper.mIvPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreviewPhotosActivity.startActivity(mContext, getData(), helper.getLayoutPosition());
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
        @BindView(R.id.tv_self)
        TextView mTvSelf;

        public PhotoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
