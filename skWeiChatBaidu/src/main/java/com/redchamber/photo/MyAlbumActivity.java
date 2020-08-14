package com.redchamber.photo;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.bean.PhotoBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.photo.adapter.MyAlbumAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
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
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 相册
 */
public class MyAlbumActivity extends BaseActivity {

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.rv)
    RecyclerView mRvAlbum;

    private MyAlbumAdapter mAdapter;
    private int pageIndex = 1;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_my_album;
    }

    @Override
    protected void initView() {
        mAdapter = new MyAlbumAdapter(this, null);
        mRvAlbum.setLayoutManager(new GridLayoutManager(this, 4));
        mRvAlbum.setAdapter(mAdapter);
        initRefreshLayout();
        queryMyAlbum();
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageIndex = 1;
                queryMyAlbum();
            }
        });
        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                queryMyAlbum();
            }
        });
    }

    @OnClick(R.id.iv_back)
    void onClick() {
        finish();
    }

    public static void startActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, MyAlbumActivity.class));
    }

    private void queryMyAlbum() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);
        params.put("pageNo", String.valueOf(pageIndex));
        params.put("pageSize", "20");

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_QUERY_MY_ALBUM)
                .params(params)
                .build()
                .execute(new ListCallback<PhotoBean>(PhotoBean.class) {

                    @Override
                    public void onResponse(ArrayResult<PhotoBean> result) {
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
                        ToastUtil.showErrorNet(MyAlbumActivity.this);
                        mRefreshLayout.finishRefresh(false);
                        mRefreshLayout.finishLoadMore(false);
                    }
                });
    }

}
