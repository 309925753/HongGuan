package com.redchamber.home.fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.redchamber.bean.RedIndexUser;
import com.redchamber.home.adapter.GirlListAdapter;
import com.redchamber.lib.base.BaseFragment;
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
import okhttp3.Call;

/**
 * 附近
 */
public class NearFragment extends BaseFragment {

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeLayout;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private GirlListAdapter mAdapter;

    @Override
    protected int setLayout() {
        return R.layout.fragment_near;
    }

    @Override
    protected void initView() {
        initRefreshLayout();
        mAdapter = new GirlListAdapter(null);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);

        getNearIndexUser("1", "上海市");
    }

    private void initRefreshLayout() {
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeLayout.setRefreshing(false);
            }
        });
//        mSwipeLayout.setRefreshing(false);
    }

    public void getNearIndexUser(String onlineFirst, String cityName) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(getContext()).getSelfStatus().accessToken);
        double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
        params.put("latitude", String.valueOf(latitude));
        params.put("longitude", String.valueOf(longitude));
        params.put("searchType", "0");
//        params.put("cityName", "0000");
        params.put("cityName", cityName);
        params.put("onlineFirst", onlineFirst);
        params.put("pageIndex", "1");
        params.put("pageSize", "20");

        HttpUtils.post().url(CoreManager.getInstance(getContext()).getConfig().RED_INDEX_USERS)
                .params(params)
                .build()
                .execute(new ListCallback<RedIndexUser>(RedIndexUser.class) {
                    @Override
                    public void onResponse(ArrayResult<RedIndexUser> result) {
                        DialogHelper.dismissProgressDialog();

                        if (result.getResultCode() == 1) {
                            mAdapter.setNewData(result.getData());
                        } else {

                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }

}
