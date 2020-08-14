package com.redchamber.photo;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.api.GlobalConstants;
import com.redchamber.bean.PhotoBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.photo.adapter.FriendAlbumAdapter;
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
 * 他人相册页面
 */
public class FriendAlbumActivity extends BaseActivity {

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.rv)
    RecyclerView mRvAlbum;

    private FriendAlbumAdapter mAdapter;

    private String mUserId;
    private int pageIndex = 1;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_friend_album;
    }

    @Override
    protected void initView() {
        if (getIntent() != null) {
            mUserId = getIntent().getStringExtra(GlobalConstants.KEY_USER_ID);
        }
        mAdapter = new FriendAlbumAdapter(this, null, mUserId);
        mRvAlbum.setLayoutManager(new GridLayoutManager(this, 4));
        mRvAlbum.setAdapter(mAdapter);
        initRefreshLayout();
        queryFriendAlbum();
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageIndex = 1;
                queryFriendAlbum();
            }
        });
        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                queryFriendAlbum();
            }
        });
    }

    @OnClick(R.id.iv_back)
    void onClick(View view) {
        finish();
    }

    public static void startActivity(Context context, String userId) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, FriendAlbumActivity.class);
        intent.putExtra(GlobalConstants.KEY_USER_ID, userId);
        context.startActivity(intent);
    }

    private void queryFriendAlbum() {
        if (TextUtils.isEmpty(mUserId)) {
            ToastUtils.showToast("用户id为空");
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);
        params.put("otherUserId", mUserId);
        params.put("pageNo", String.valueOf(pageIndex));
        params.put("pageSize", "20");

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_QUERY_USER_PHOTO)
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
                        ToastUtil.showErrorNet(FriendAlbumActivity.this);
                        mRefreshLayout.finishRefresh(false);
                        mRefreshLayout.finishLoadMore(false);
                    }
                });
    }

}
