package com.sk.weichat.ui.live.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.live.bean.Gift;

import java.util.ArrayList;

/**
 * 礼物Adapter
 */
public class GiftGridViewAdapter extends BaseAdapter {
    private ArrayList<Gift> gifts;
    private Context context;
    private int page;
    private int count;

    public void setOnGridViewClickListener(OnGridViewClickListener onGridViewClickListener) {
        this.onGridViewClickListener = onGridViewClickListener;
    }

    public GiftGridViewAdapter(Context context, int page, int count) {
        this.context = context;
        this.page = page;
        this.count = count;
    }

    public void setGifts(ArrayList<Gift> gifts) {
        this.gifts = gifts;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 8;
    }

    @Override
    public Gift getItem(int position) {
        // TODO Auto-generated method stub
        return gifts.get(page * count + position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder viewHolder;
        final Gift gift = gifts.get(page * count + position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_gift, null);
            viewHolder.grid_fragment_home_item_img =
                    (ImageView) convertView.findViewById(R.id.grid_fragment_home_item_img);
            viewHolder.grid_fragment_home_item_txt =
                    (TextView) convertView.findViewById(R.id.grid_fragment_home_item_txt);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        AvatarHelper.getInstance().displayUrl(gift.getPhoto(), viewHolder.grid_fragment_home_item_img);
        viewHolder.grid_fragment_home_item_txt.setText(String.valueOf(gift.getPrice()));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onGridViewClickListener != null) {
                    onGridViewClickListener.click(gift);
                }
            }
        });

        return convertView;
    }

    public class ViewHolder {
        ImageView grid_fragment_home_item_img;
        TextView grid_fragment_home_item_txt;
    }

    private OnGridViewClickListener onGridViewClickListener;

    public interface OnGridViewClickListener {
        void click(Gift gift);
    }
}
