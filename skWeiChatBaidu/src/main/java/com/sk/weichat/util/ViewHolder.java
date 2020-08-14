package com.sk.weichat.util;

import android.util.SparseArray;
import android.view.View;

/**
 * TODO: 坑，view绑定的context也就是activity可能被销毁，但是这里不会监听到也不会重建，Fragment复用可能出问题，
 */
public class ViewHolder {
    private ViewHolder() {
    }

    // I added a generic return type to reduce the casting noise in client code
    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }
}
