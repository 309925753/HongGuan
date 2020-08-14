package com.redchamber.wallet;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.bean.WalletDetailBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.wallet.adapter.WalletDetailAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 钱包-明细
 */
public class WalletDetailActivity extends BaseActivity {

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.rv)
    RecyclerView mRvList;

    private WalletDetailAdapter mAdapter;
    private int pageIndex = 1;

    @Override
    protected int setLayout() {
        return R.layout.activity_wallet_detail;
    }

    @Override
    protected void initView() {
        initRefreshLayout();
        mAdapter = new WalletDetailAdapter(null);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRvList.setLayoutManager(linearLayoutManager);
        mRvList.setAdapter(mAdapter);
        getDetailList();
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageIndex = 1;
                getDetailList();
            }
        });
        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getDetailList();
            }
        });
    }

    private void getDetailList() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);
        params.put("pageIndex", String.valueOf(pageIndex));
        params.put("pageSize", "20");

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_WALLET_DETAIL_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<WalletDetailBean>(WalletDetailBean.class) {

                    @Override
                    public void onResponse(ArrayResult<WalletDetailBean> result) {
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

    public static void startActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, WalletDetailActivity.class));
    }

    @OnClick(R.id.iv_back)
    void onClick() {
        finish();
    }

}
