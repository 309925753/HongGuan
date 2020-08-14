package com.redchamber.photo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.bean.PhotoBean;
import com.redchamber.util.GlideUtils;
import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 红包相册
 */
public class RedAlbumAdapter extends BaseQuickAdapter<PhotoBean, RedAlbumAdapter.PhotoViewHolder> {

    private int mWidth;

    public RedAlbumAdapter(Context context, @Nullable List<PhotoBean> data) {
        super(R.layout.red_item_rv_red_album, data);
        this.mWidth = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 45)) / 4;
    }

    @Override
    protected void convert(@NonNull PhotoViewHolder helper, PhotoBean item) {
        ViewGroup.LayoutParams lp = helper.mRlRoot.getLayoutParams();
        lp.width = lp.height = mWidth;

        GlideUtils.load(mContext, item.photoUrl, helper.mIvPhoto);

        if (item.isSelect) {
            helper.mIvCheck.setImageResource(R.mipmap.red_ic_checkbox_checked);
        } else {
            helper.mIvCheck.setImageResource(R.mipmap.red_ic_checkbox_unchecked);
        }

        if (1 == item.isSelf) {
            helper.mTvSelf.setVisibility(View.VISIBLE);
        } else {
            helper.mTvSelf.setVisibility(View.GONE);
        }

        helper.mIvPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item.isSelect = !item.isSelect;
                notifyItemChanged(helper.getAdapterPosition());
            }
        });

    }

    public static class PhotoViewHolder extends BaseViewHolder {

        @BindView(R.id.rl_root)
        RelativeLayout mRlRoot;
        @BindView(R.id.iv_photo)
        RoundedImageView mIvPhoto;
        @BindView(R.id.iv_check)
        ImageView mIvCheck;
        @BindView(R.id.tv_self)
        TextView mTvSelf;

        public PhotoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
