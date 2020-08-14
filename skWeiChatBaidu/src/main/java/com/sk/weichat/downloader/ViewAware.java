package com.sk.weichat.downloader;

import android.view.View;

import com.sk.weichat.R;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wrapper for Android {@link View View}. Keeps weak reference of
 * View to prevent memory leaks.
 */
public class ViewAware {
    public static final AtomicInteger sIdSeq = new AtomicInteger(0);
    public ViewAware(View view) {
        if (view == null) {
            emptyView = true;
        } else {
            this.viewRef = new WeakReference<View>(view);
            // 应对视图复用，每次包装同一个视图就修改这个tag, tag变了就是视图复用开了新的下载，此时忽略旧的成功回调，
            view.setTag(R.id.key_downloader_view_aware, nextId());
        }
    }

    protected Reference<View> viewRef;
    private boolean emptyView;

    public static final int nextId() {
        return sIdSeq.addAndGet(1);
    }

    public View getWrappedView() {
        if (emptyView) {
            return null;
        }
        return viewRef.get();
    }

    public boolean isCollected() {
        if (emptyView) {
            return false;
        }
        return viewRef.get() == null;
    }

    public int getId() {
        if (emptyView) {
            return super.hashCode();
        }
        View view = viewRef.get();
        if (view == null) {
            return super.hashCode();
        }
        // 应对视图复用，每次包装同一个视图就修改这个tag, tag变了就是视图复用开了新的下载，此时忽略旧的成功回调，
        Integer seq = (Integer) view.getTag(R.id.key_downloader_view_aware);
        if (seq == null) {
            return view.hashCode();
        }
        return seq;
    }

}
