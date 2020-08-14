package com.redchamber.photo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.github.chrisbanes.photoview.PhotoView;
import com.redchamber.bean.PhotoBean;
import com.redchamber.util.GlideUtils;
import com.sk.weichat.R;

import java.util.List;

public class PreviewPhotosAdapter extends PagerAdapter {

    private Context mContext;
    private List<PhotoBean> mImgList;

    public PreviewPhotosAdapter(Context context, List<PhotoBean> imgList) {
        this.mContext = context;
        mImgList = imgList;
    }

    @Override
    public int getCount() {
        return mImgList != null && mImgList.size() > 0 ? mImgList.size() : 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.red_item_vp_preview_photo, null);
        container.addView(view);

        bindView(view, position);

        return view;
    }

    private void bindView(View view, int position) {
        PhotoView photoView = view.findViewById(R.id.photo_view);
        photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        GlideUtils.load(mContext, mImgList.get(position).photoUrl, photoView);
    }

}
