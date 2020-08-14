package com.sk.weichat.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sk.weichat.R;
import com.sk.weichat.view.NoLastDividerItemDecoration;

import java.util.List;

public abstract class BaseListActivity<VH extends RecyclerView.ViewHolder> extends BaseActivity {
    public static final int PAGE_SIZE = 20;
    public SwipeRefreshLayout mSSRlayout;
    public RecyclerView mRecyclerView;
    public PreviewAdapter mAdapter;
    public LayoutInflater mInflater;
    public boolean more = false;
    FrameLayout mFlNoDatas;
    private int pager;
    private boolean loading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_list);
        mSSRlayout = (SwipeRefreshLayout) findViewById(R.id.fragment_list_swip);
        mRecyclerView = (RecyclerView) findViewById(R.id.fragment_list_recyview);
        mFlNoDatas = (FrameLayout) findViewById(R.id.fl_empty);
        mInflater = LayoutInflater.from(this);

        mSSRlayout.setRefreshing(true);
        initView();
        initFristDatas();
        initBaseView();
    }

    public void initView() {

    }

    public void initFristDatas() {

    }

    /**
     * 如果需要最后一项不显示的分割线，就继承这个方法，返回分割线drawable,
     */
    @Nullable
    @DrawableRes
    protected Integer getMiddleDivider() {
        return null;
    }

    protected void initBaseView() {
        mSSRlayout.setColorSchemeResources(R.color.orange, R.color.purple,
                R.color.btn_live_2);
        mSSRlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initDatas(0);
                pager = 0;
                loading = false;
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        Integer dividerRes = getMiddleDivider();
        if (dividerRes != null) {
            NoLastDividerItemDecoration divider = new NoLastDividerItemDecoration(this, DividerItemDecoration.VERTICAL);
            divider.setDrawable(getResources().getDrawable(dividerRes));
            mRecyclerView.addItemDecoration(divider);
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new PreviewAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager));
        more = true;
        initDatas(0);
        pager = 0;
    }

    /**
     * 数据层面
     */
    public abstract void initDatas(int pager);

    /* 视图层面 */
    public abstract VH initHolder(ViewGroup parent);

    public abstract void fillData(VH holder, int position);

    /**
     * 通知更新
     */
    public void update(List<?> data) {
        if (data != null && data.size() > 0) {
            if (mSSRlayout.isRefreshing()) {
                mSSRlayout.setRefreshing(false);
            }
            mFlNoDatas.setVisibility(View.GONE);
        } else {
            if (mSSRlayout.isRefreshing()) {
                mSSRlayout.setRefreshing(false);
            }
            mFlNoDatas.setVisibility(View.VISIBLE);
            more = false;
        }
        if (data != null) {
            mAdapter.setData(data);
        }
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    public void notifyItemData(int position) {
        mAdapter.notifyDataSetChanged();
    }

    class PreviewAdapter extends RecyclerView.Adapter<VH> {
        private List<?> data;

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return initHolder(parent);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            fillData(holder, position);
        }

        @Override
        public int getItemCount() {
            if (data != null) {
                return data.size();
            }
            return 0;
        }

        public void setData(List<?> data) {
            if (data != null) {
                this.data = data;
                notifyDataSetChanged();
            }
        }
    }

    public class EndlessRecyclerOnScrollListener extends
            RecyclerView.OnScrollListener {

        int firstVisibleItem, visibleItemCount, totalItemCount;
        private int previousTotal = 0;
        private LinearLayoutManager mLinearLayoutManager;

        public EndlessRecyclerOnScrollListener(
                LinearLayoutManager linearLayoutManager) {
            this.mLinearLayoutManager = linearLayoutManager;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (!more) {
                // 外界不让加载数据了
                return;
            }
            visibleItemCount = recyclerView.getChildCount();
            totalItemCount = mLinearLayoutManager.getItemCount();
            firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= firstVisibleItem) {
                pager++;
                initDatas(pager);
                loading = true;
            }
        }
    }
}
