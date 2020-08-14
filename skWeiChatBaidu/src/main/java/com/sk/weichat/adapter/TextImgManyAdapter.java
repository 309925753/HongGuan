package com.sk.weichat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.bean.TextImgBean;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.tool.WebViewActivity;

import java.util.List;

import static com.sk.weichat.ui.tool.WebViewActivity.EXTRA_URL;

/**
 * Created by Administrator on 2017/7/20.
 */

public class TextImgManyAdapter extends BaseAdapter {
    Context mContent;
    List<TextImgBean> mData;
    LayoutInflater mInflater;

    public TextImgManyAdapter(Context context, List<TextImgBean> datas) {
        mContent = context;
        mInflater = LayoutInflater.from(context);
        mData = datas;
    }

    @Override
    public int getCount() {
        if (mData != null && mData.size() > 0) {
            return mData.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mData != null && mData.size() > 0) {
            return mData.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolde holde = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_text_img_many, null);
            holde = new ViewHolde();
            holde.tvTitle = (TextView) convertView.findViewById(R.id.textimg_title_tv);
            holde.ivImg = (ImageView) convertView.findViewById(R.id.textimg_img_iv);
            holde.llRootView = (LinearLayout) convertView.findViewById(R.id.textimg_rootview);

            convertView.setTag(holde);
        } else {
            holde = (ViewHolde) convertView.getTag();
        }

        final TextImgBean data = mData.get(position);
        holde.tvTitle.setText(data.title);
        AvatarHelper.getInstance().displayUrl(data.img, holde.ivImg);
        holde.llRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContent, WebViewActivity.class);
                intent.putExtra(EXTRA_URL, data.url);
                mContent.startActivity(intent);

            }
        });
        return convertView;
    }

    class ViewHolde {
        TextView tvTitle;
        ImageView ivImg;
        LinearLayout llRootView;
    }
}