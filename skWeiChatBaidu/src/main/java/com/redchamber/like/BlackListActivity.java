package com.redchamber.like;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.redchamber.bean.BlackListBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.like.adapter.BlackListAdapter;
import com.redchamber.request.BlackRequest;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.DisplayUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.yanzhenjie.recyclerview.OnItemMenuClickListener;
import com.yanzhenjie.recyclerview.SwipeMenu;
import com.yanzhenjie.recyclerview.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 黑名单
 */
public class BlackListActivity extends BaseActivity {

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.recyclerView)
    SwipeRecyclerView mRvBlack;

    private BlackListAdapter mBlackListAdapter;
    private List<BlackListBean.BlackUser> blackUsers = new ArrayList<>();
    private int pageIndex = 1;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_black_list;
    }

    @Override
    protected void initView() {
        mBlackListAdapter = new BlackListAdapter(blackUsers);
        mRvBlack.setSwipeMenuCreator(swipeMenuCreator);
        mRvBlack.setOnItemMenuClickListener(mMenuItemClickListener);
        mRvBlack.setLayoutManager(new LinearLayoutManager(this));
        mRvBlack.setAdapter(mBlackListAdapter);

        initRefreshLayout();

        getBlackList();
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageIndex = 1;
                getBlackList();
            }
        });
        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getBlackList();
            }
        });
    }

    @OnClick(R.id.iv_back)
    void onClick() {
        finish();
    }

    public static void startBlackListActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, BlackListActivity.class));
    }

    /**
     * 菜单创建器，在Item要创建菜单的时候调用。
     */
    private SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int position) {
            int width = DisplayUtil.dip2px(BlackListActivity.this, 80f);
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            // 添加右侧的，如果不添加，则右侧不会出现菜单。
            SwipeMenuItem top = new SwipeMenuItem(BlackListActivity.this).setBackgroundColorResource(R.color.color_FB2828)
                    .setText("移除黑名单")
                    .setTextColor(Color.WHITE)
                    .setTextSize(13)
                    .setWidth(width)
                    .setHeight(height);
            swipeRightMenu.addMenuItem(top);// 添加菜单到右侧。
        }
    };

    private OnItemMenuClickListener mMenuItemClickListener = new OnItemMenuClickListener() {
        @Override
        public void onItemClick(SwipeMenuBridge menuBridge, int position) {
            menuBridge.closeMenu();
            int direction = menuBridge.getDirection(); // 左侧还是右侧菜单。
            int menuPosition = menuBridge.getPosition(); // 菜单在RecyclerView的Item中的Position。
            if (direction == SwipeRecyclerView.RIGHT_DIRECTION) {
                if (menuPosition == 0) {
                    if (blackUsers == null || blackUsers.size() <= 0 || blackUsers.get(position) == null) {
                        return;
                    }
                    BlackRequest.getInstance().addBlackList(BlackListActivity.this, blackUsers.get(position).friendId,
                            "1", new BlackRequest.AddBlackListCallBack() {
                                @Override
                                public void onSuccess() {
                                    blackUsers.remove(position);
                                    mBlackListAdapter.notifyItemRemoved(position);
                                }

                                @Override
                                public void onFail(String error) {
                                    ToastUtils.showToast(error);
                                }
                            });
                }
            }
        }
    };

    private void getBlackList() {
        DialogHelper.showDefaulteMessageProgressDialog(this);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);
        params.put("pageIndex", String.valueOf(pageIndex));
        params.put("pageSize", "20");

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_GET_BLACK_LIST)
                .params(params)
                .build()
                .execute(new BaseCallback<BlackListBean>(BlackListBean.class) {

                    @Override
                    public void onResponse(ObjectResult<BlackListBean> result) {
                        DialogHelper.dismissProgressDialog();
                        mRefreshLayout.finishRefresh(true);
                        mRefreshLayout.finishLoadMore(true);
                        if (result.getResultCode() == 1) {
                            if (result.getData() != null && result.getData().pageData != null && result.getData().pageData.size() > 0) {
                                if (pageIndex > 1) {
                                    blackUsers.addAll(result.getData().pageData);
                                    mBlackListAdapter.addData(result.getData().pageData);
                                } else {
                                    blackUsers = result.getData().pageData;
                                    mBlackListAdapter.setNewData(result.getData().pageData);
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
