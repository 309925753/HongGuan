package com.redchamber.home.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.api.GlobalConstants;
import com.redchamber.bean.RedIndexUser;
import com.redchamber.event.UpdateCityOnlineEvent;
import com.redchamber.home.adapter.GirlListAdapter;
import com.redchamber.lib.base.BaseFragment;
import com.redchamber.lib.utils.ToastUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

public class HomeBaseFragment extends BaseFragment {

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.rv)
    RecyclerView mRecyclerView;

    private GirlListAdapter mAdapter;

    private String type;
    private String mOnlineFirst = "0";
    private String mCityName = ""; //附近
    private int pageIndex = 1;

    @Override
    protected int setLayout() {
        return R.layout.red_fragment_home_base;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initView() {
        Bundle bundle = getArguments();
        type = bundle.getString(GlobalConstants.KEY_TYPE);

        initRefreshLayout();
        mAdapter = new GirlListAdapter(null);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);

        getIndexUser(mOnlineFirst, mCityName);
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageIndex = 1;
                getIndexUser(mOnlineFirst, mCityName);
            }
        });
        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getIndexUser(mOnlineFirst, mCityName);
            }
        });
    }

    private void getIndexUser(String onlineFirst, String cityName) {
        DialogHelper.showDefaulteMessageProgressDialog(getContext());
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(getContext()).getSelfStatus().accessToken);
        double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
        params.put("latitude", String.valueOf(latitude));
        params.put("longitude", String.valueOf(longitude));
        params.put("searchType", type);
        params.put("cityName", cityName);
        params.put("onlineFirst", onlineFirst);
        params.put("pageIndex", String.valueOf(pageIndex));
        params.put("pageSize", "20");

        HttpUtils.post().url(CoreManager.getInstance(getContext()).getConfig().RED_INDEX_USERS)
                .params(params)
                .build()
                .execute(new ListCallback<RedIndexUser>(RedIndexUser.class) {
                    @Override
                    public void onResponse(ArrayResult<RedIndexUser> result) {
                        DialogHelper.dismissProgressDialog();
                        mRefreshLayout.finishRefresh(true);
                        mRefreshLayout.finishLoadMore(true);
                        if (result.getResultCode() == 1) {
                            if (pageIndex > 1) {//加载更多
                                if (result.getData() == null || result.getData().size() <= 0) {
                                    ToastUtils.showToast("暂无更多数据");
                                } else {
                                    pageIndex++;
                                    mAdapter.addData(result.getData());
                                }
                            } else {
                                if (result.getData() == null || result.getData().size() <= 0) {
                                    setEmptyView();
                                } else {
                                    pageIndex++;
                                }
                                mAdapter.setNewData(result.getData());
                            }
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getActivity());
                        mRefreshLayout.finishRefresh(false);
                        mRefreshLayout.finishLoadMore(false);
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onMessageEvent(UpdateCityOnlineEvent event) {
        if (event != null) {
            mOnlineFirst = event.onlineFirst;
            mCityName = event.cityName;
            pageIndex = 1;
            getIndexUser(event.onlineFirst, event.cityName);
        }
    }

    private void setEmptyView() {
        if (mAdapter != null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.red_layout_empty_view, null, false);
            mAdapter.setEmptyView(view);
        }
    }

}
