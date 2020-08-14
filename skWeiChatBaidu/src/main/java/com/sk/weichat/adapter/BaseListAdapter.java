package com.sk.weichat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseListAdapter<T> extends BaseAdapter {
    protected Context ctx;
    protected LayoutInflater inflater;
    public List<T> datas = new ArrayList<T>();

    public BaseListAdapter(Context ctx) {
        initWithContext(ctx);
    }

    public void initWithContext(Context ctx) {
        this.ctx = ctx;
        inflater = LayoutInflater.from(ctx);
    }

    public BaseListAdapter(Context ctx, List<T> datas) {
        initWithContext(ctx);
        this.datas = datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }


    public List<T> getDatas() {
        return datas;
    }

    public void add(T object) {
        datas.add(object);
        notifyDataSetChanged();
    }

    public void addAll(List<T> subDatas) {
        datas.addAll(subDatas);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        datas.remove(position);
        notifyDataSetChanged();
    }

    public void removeAll() {
        datas.removeAll(datas);
        notifyDataSetChanged();
    }

    public void clear() {
        datas.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
