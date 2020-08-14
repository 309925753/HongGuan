package com.redchamber.info.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.sk.weichat.R;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;

import java.util.List;

public class SelectCityTagAdapter extends TagAdapter<String> {

    private Context mContext;

    public SelectCityTagAdapter(Context context, List data) {
        super(data);
        this.mContext = context;
    }

    @Override
    public View getView(FlowLayout parent, int position, String string) {
        TextView tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.item_rv_tfl_city,
                parent, false);
        tv.setText(string);
        return tv;
    }

}
