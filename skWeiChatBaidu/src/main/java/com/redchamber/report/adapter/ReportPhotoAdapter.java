package com.redchamber.report.adapter;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.bean.PhotoBean;
import com.sk.weichat.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 举报上传的图片
 */
public class ReportPhotoAdapter extends BaseQuickAdapter<PhotoBean, ReportPhotoAdapter.PhotoViewHolder> {

    private addPhotoListener mAddPhotoListener;

    public ReportPhotoAdapter(@Nullable List<PhotoBean> data) {
        super(R.layout.red_item_rv_report_photo, data);
    }

    @Override
    protected void convert(@NonNull PhotoViewHolder helper, PhotoBean item) {

        Glide.with(mContext).load(item.photoUrl).into(helper.mIvPhoto);

        int position = helper.getLayoutPosition();

        if (getData().size() <= 6) {
            if (position == getItemCount() - 1) {
                helper.mIvDelete.setVisibility(View.GONE);
                Glide.with(mContext).load(R.mipmap.red_ic_add_photo).into(helper.mIvPhoto);
                helper.mIvPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mAddPhotoListener != null) {
                            mAddPhotoListener.addPhoto();
                        }
                    }
                });
            }
        }

        helper.mIvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData().remove(helper.getAdapterPosition());
                notifyItemRemoved(helper.getAdapterPosition());
            }
        });

    }

    public static class PhotoViewHolder extends BaseViewHolder {

        @BindView(R.id.iv_photo)
        RoundedImageView mIvPhoto;
        @BindView(R.id.iv_delete)
        ImageView mIvDelete;

        public PhotoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public interface addPhotoListener {
        void addPhoto();
    }

    public void setAddPhotoListener(addPhotoListener mAddPhotoListener) {
        this.mAddPhotoListener = mAddPhotoListener;
    }
}
