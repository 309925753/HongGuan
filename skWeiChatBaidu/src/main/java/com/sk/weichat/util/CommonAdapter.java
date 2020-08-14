package com.sk.weichat.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by zq on 2017/9/6 0006.
 */

public class CommonAdapter<T> extends BaseAdapter {
    protected Context mContext;
    protected List<T> data;

    public CommonAdapter(Context context, List<T> data) {
        this.mContext = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        if (data == null || data.size() == 0) {
            return 0;
        }
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        if (data == null || data.size() == 0) {
            return null;
        }
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
