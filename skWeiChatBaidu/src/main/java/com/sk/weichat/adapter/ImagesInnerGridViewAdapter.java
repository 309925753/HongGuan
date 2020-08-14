package com.sk.weichat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.sk.weichat.R;
import com.sk.weichat.bean.circle.PublicMessage.Resource;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.view.SquareCenterImageView;

import java.util.List;

public class ImagesInnerGridViewAdapter extends BaseAdapter {

    private Context mContext;
    private List<Resource> mDatas;

    ImagesInnerGridViewAdapter(Context context, List<Resource> data) {
        mContext = context;
        mDatas = data;
    }

    @Override
    public int getCount() {
        if (mDatas.size() >= 9) {
            return 9;
        }
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.row_mu_normal, parent, false);
            holder.imageView = (SquareCenterImageView) convertView.findViewById(R.id.muc_normal);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mDatas.get(position).getOriginalUrl().endsWith(".gif")) {
            ImageLoadHelper.showGifWithPlaceHolder(
                    mContext,
                    mDatas.get(position).getOriginalUrl(),
                    R.drawable.default_gray,
                    R.drawable.image_download_fail_icon,
                    holder.imageView
            );
        } else {
            ImageLoadHelper.showImageWithSizePlaceHolder(
                    mContext,
                    mDatas.get(position).getOriginalUrl(),
                    R.drawable.default_gray,
                    R.drawable.image_download_fail_icon,
                    150, 150,
                    holder.imageView
            );
        }

        return convertView;
    }

    static class ViewHolder {
        SquareCenterImageView imageView;
    }
}