package com.sk.weichat.view.photopicker;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.sk.weichat.R;
import com.sk.weichat.helper.ImageLoadHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by donglua on 15/6/21.
 */
public class PhotoPagerAdapter extends PagerAdapter {

    public PhotoViewClickListener listener;
    private List<Item> paths = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public PhotoPagerAdapter(Context mContext, List<Item> paths) {
        this.mContext = mContext;
        this.paths = paths;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void setPhotoViewClickListener(PhotoViewClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View itemView = mLayoutInflater.inflate(R.layout.item_preview, container, false);

        ImageView imageView = itemView.findViewById(R.id.iv_pager);

        Item item = paths.get(position);
        String path = item.path;
        if (item.changed) {
            path = item.resultPath;
        }
        final Uri uri;
        if (path.startsWith("http")) {
            uri = Uri.parse(path);
        } else {
            uri = Uri.fromFile(new File(path));
        }
        String mImageUri = uri.toString();
        ImageView mImageView = imageView;
        // copy from com.sk.weichat.ui.tool.SingleImagePreviewActivity.initView
        if (mImageUri.endsWith(".gif")) {
            ImageLoadHelper.showGifWithError(
                    mContext,
                    mImageUri,
                    R.drawable.image_download_fail_icon,
                    mImageView
            );
        } else {
            ImageLoadHelper.loadBitmapCenterCropDontAnimateWithError(
                    mContext,
                    mImageUri,
                    R.drawable.image_download_fail_icon,
                    b -> {
                        mImageView.setImageBitmap(b);
                    }, e -> {
                        mImageView.setImageResource(R.drawable.image_download_fail_icon);
                    });
        }

/*
        imageView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float v, float v1) {
                if (listener != null) {
                    listener.OnPhotoTapListener(view, v, v1);
                }
            }
        });
*/

        container.addView(itemView);

        return itemView;
    }

    @Override
    public int getCount() {
        return paths.size();
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
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public interface PhotoViewClickListener {
        void OnPhotoTapListener(View view, float v, float v1);
    }

    public static class Item {
        public String path;
        // 图片是否经过编辑，
        public boolean changed = false;
        // 图片编辑后的路径，
        public String resultPath = null;
    }

}
