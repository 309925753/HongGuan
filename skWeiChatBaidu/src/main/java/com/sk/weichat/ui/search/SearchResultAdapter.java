package com.sk.weichat.ui.search;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.MyApplication;
import com.sk.weichat.Reporter;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.SkinUtils;

import java.util.ArrayList;
import java.util.List;

abstract class SearchResultAdapter<T extends RecyclerView.ViewHolder, E> extends RecyclerView.Adapter<T> {
    protected static final int DEFAULT_ITEM_COUNT_LIMIT = 3;
    protected List<E> data = new ArrayList<>();
    protected String searchKey;
    protected OnSearchResultClickListener listener;
    private int itemCountLimit;
    private View block;
    private View more;

    /**
     * @param itemCountLimit 搜索结果个数限制，为0表示不限制，
     * @param listener       搜索结果被点击时通知外面记录搜索历史,
     */
    SearchResultAdapter(int itemCountLimit, OnSearchResultClickListener listener) {
        this.listener = listener;
        if (itemCountLimit <= 0) {
            this.itemCountLimit = Integer.MAX_VALUE;
        } else {
            this.itemCountLimit = itemCountLimit;
        }
    }

    protected void callOnSearchResultClickListener() {
        if (listener != null) {
            listener.onClick();
        }
    }

    @StringRes
    public abstract int getSearchType();

    /**
     * @return 搜索结果数量，
     */
    public abstract List<E> realSearch(String str) throws Exception;

    public void search(String str) {
        searchKey = str;
        if (TextUtils.isEmpty(str)) {
            data.clear();
            notifyDataSetChanged();
            updateView(0);
            return;
        }
        AsyncUtils.doAsync(this, t -> {
            Reporter.post("搜索<" + MyApplication.getContext().getString(getSearchType()) + ">失败", t);
        }, c -> {
            List<E> result = realSearch(str);
            int count = result.size();
            c.uiThread(r -> {
                data = result;
                notifyDataSetChanged();
                updateView(count);
            });
        });
    }

    private void updateView(int count) {
        if (block != null) {
            if (count == 0) {
                block.setVisibility(View.GONE);
            } else {
                block.setVisibility(View.VISIBLE);
            }
        }
        if (more != null) {
            if (count > itemCountLimit) {
                more.setVisibility(View.VISIBLE);
            } else {
                more.setVisibility(View.GONE);
            }
        }
    }

    protected void highlight(TextView tv, String content) {
        int index = content.indexOf(searchKey);
        if (index < 0) {
            tv.setText(content);
            return;
        }
        SpannableStringBuilder sb = new SpannableStringBuilder(content);
        sb.setSpan(new ForegroundColorSpan(SkinUtils.getSkin(tv.getContext()).getAccentColor()), index, index + searchKey.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(sb);
    }

    @Override
    public int getItemCount() {
        if (data.size() > itemCountLimit) {
            return itemCountLimit;
        }
        return data.size();
    }

    /**
     * @param block 绑定这个adapter到block布局里，主要用于搜索结果为空时隐藏这个block,
     */
    void attach(View block, View more) {
        this.block = block;
        this.more = more;
    }

    interface OnSearchResultClickListener {
        void onClick();
    }
}
