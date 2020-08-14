package com.sk.weichat.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sk.weichat.R;

import java.util.List;


/**
 * Created by Administrator on 2017/6/21.
 */

public abstract class BaseListFragment<VH extends RecyclerView.ViewHolder> extends EasyFragment {
    public LayoutInflater mInflater;
    public PreviewAdapter mAdapter;
    public boolean more;
    SwipeRefreshLayout mSSRlayout;
    RecyclerView mRecyclerView;
    private int pager;
    private boolean loading = true;

    @Override
    protected int inflateLayoutId() {
        return R.layout.activity_base_list;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        mSSRlayout = (SwipeRefreshLayout) findViewById(R.id.fragment_list_swip);
        mRecyclerView = (RecyclerView) findViewById(R.id.fragment_list_recyview);
        mInflater = LayoutInflater.from(getActivity());
        more = false;
        initView();
    }

    protected void initView() {
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

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new PreviewAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager));
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
        if (mSSRlayout.isRefreshing()) {
            mSSRlayout.setRefreshing(false);
        }
        mAdapter.setData(data);
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
            visibleItemCount = recyclerView.getChildCount();
            totalItemCount = mLinearLayoutManager.getItemCount();
            firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (more && !loading && (totalItemCount - visibleItemCount) <= firstVisibleItem) {
                pager++;
                initDatas(pager);
                loading = true;
            }
        }
    }
}
