package com.redchamber.bar.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.redchamber.bean.BannerImageBean;
import com.redchamber.bean.PageDataBean;
import com.redchamber.radio.adapter.ProgramDetailSignAdapter;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

/**
 * 轮播图
 */
public class BannerImageAdapter extends BannerAdapter<BannerImageBean, BannerImageAdapter.BannerViewHolder> {

    private Context mContext;
    private BtnOnClick btnOnClick;
    public interface BtnOnClick {
        void btnOnClick(BannerImageBean bannerImageBean);
    }

    public void setBtnOnClice(BtnOnClick btnOnClick) {
        this.btnOnClick = btnOnClick;

    }

    public BannerImageAdapter(Context context, List<BannerImageBean> data) {
        super(data);
        this.mContext = context;
    }

    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        //注意，必须设置为match_parent，这个是viewpager2强制要求的
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new BannerViewHolder(imageView);
    }

    @Override
    public void onBindView(BannerViewHolder holder, BannerImageBean data, int position, int size) {
//        Glide.with(mContext).load(data.imageUrl).into(holder.imageView);
        Glide.with(mContext).load(data.getPicture()).into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOnClick.btnOnClick(data);
            }
        });
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        BannerViewHolder(@NonNull ImageView view) {
            super(view);
            this.imageView = view;
        }
    }
}
