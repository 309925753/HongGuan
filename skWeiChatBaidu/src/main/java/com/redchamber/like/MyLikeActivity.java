package com.redchamber.like;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.bean.RedIndexUser;
import com.redchamber.home.adapter.GirlListAdapter;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 我喜欢的
 */
public class MyLikeActivity extends BaseActivity {

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView mRvLike;

    private GirlListAdapter mAdapter;
    private int pageIndex = 1;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_my_like;
    }

    @Override
    protected void initView() {
        mAdapter = new GirlListAdapter(null);
        mRvLike.setLayoutManager(new LinearLayoutManager(this));
        mRvLike.setAdapter(mAdapter);
        initRefreshLayout();
        getLikeList();
    }

    @OnClick(R.id.iv_back)
    void onClick() {
        finish();
    }

    public static void startMyLikeActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, MyLikeActivity.class));
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageIndex = 1;
                getLikeList();
            }
        });
        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getLikeList();
            }
        });
    }

    private void getLikeList() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
        params.put("latitude", String.valueOf(latitude));
        params.put("longitude", String.valueOf(longitude));
        params.put("pageIndex", String.valueOf(pageIndex));
        params.put("pageSize", "20");

        HttpUtils.post().url(coreManager.getConfig().RED_COLLECTION_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<RedIndexUser>(RedIndexUser.class) {
                    @Override
                    public void onResponse(ArrayResult<RedIndexUser> result) {
                        DialogHelper.dismissProgressDialog();
                        mRefreshLayout.finishRefresh(true);
                        mRefreshLayout.finishLoadMore(true);
                        if (result.getResultCode() == 1) {
                            if (result.getData() != null && result.getData().size() > 0) {
                                if (pageIndex > 1) {
                                    mAdapter.addData(result.getData());
                                } else {
                                    mAdapter.setNewData(result.getData());
                                }
                                pageIndex++;
                            } else {
                                ToastUtils.showToast("暂无数据");
                            }
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtils.showToast(e.getMessage());
                        mRefreshLayout.finishRefresh(false);
                        mRefreshLayout.finishLoadMore(false);
                    }
                });
    }

}
