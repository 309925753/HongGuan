package com.sk.weichat.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.LinkedList;

/**
 * Created by 魏正旺 on 2016/11/11.
 * 继承系统的pagerAdapter,实现复用ItemView的pagerAdapter
 */
public abstract class MarkerPagerAdapter extends PagerAdapter {

    // 自己写一个回收机制(pagerView 的复用机制) 基本与listview的复用原理一致
    protected LinkedList<View> markerCacheViews = new LinkedList<>();

    @Override
    public int getCount() {
        return myGetCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        View convertView = (View) object;
        ((ViewPager) container).removeView(convertView);
            /*自己回收View*/
        markerCacheViews.add(convertView);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View convertView = null, contantView;
        if (markerCacheViews != null && markerCacheViews.size() > 0) {
            convertView = markerCacheViews.removeFirst();
        }
        contantView = getView(convertView, position);
        container.addView(contantView);
        return contantView;
    }


    /*重载下面两个方法就是为了防止显示混乱问题出现*/
    @Override
    public int getItemPosition(Object object) {
        if (mChildCount > 0) {
            mChildCount--;
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }

    private int mChildCount;

    @Override
    public void notifyDataSetChanged() {
        mChildCount = getCount();
        super.notifyDataSetChanged();
    }

    public abstract View getView(View convertView, int position);

    public abstract int myGetCount();
}
