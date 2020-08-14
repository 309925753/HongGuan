package com.redchamber.radio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.api.GlobalConstants;
import com.redchamber.bean.DiscussesBean;
import com.redchamber.bean.PageDataBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.message.DianzanActivity;
import com.redchamber.radio.adapter.ThumbDetailListAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sk.weichat.AppConfig;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.view.cjt2325.cameralibrary.util.LogUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 个人中心 点赞列表
 */
public class ThumbDetailListActivity extends BaseActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRvComment;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;

    private ThumbDetailListAdapter mAdapter;
    private PageDataBean pageDataBean = new PageDataBean();

    @Override
    protected int setLayout() {
        return R.layout.red_activity_thumb_detail_list;
    }

    @Override
    protected void initView() {

        if (getIntent() != null) {
            pageDataBean = (PageDataBean) getIntent().getSerializableExtra(GlobalConstants.KEY_THEME);
        }

        mAdapter = new ThumbDetailListAdapter(null);
        mRvComment.setLayoutManager(new LinearLayoutManager(this));
        mRvComment.setAdapter(mAdapter);
        getMessageList();
        initRefreshLayout();
    }
    private int pageIndex=1;
    private void initRefreshLayout() {
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageIndex = 1;
                getMessageList();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                pageIndex++;
                getMessageList();
            }
        });
    }

    @OnClick(R.id.iv_back)
    void onClick(View view) {
        finish();
    }

  /*  private void fakeData() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            data.add("");
        }
        mAdapter.setNewData(data);
    }*/

    public static void startActivity(Context context, PageDataBean pageDataBean) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, ThumbDetailListActivity.class));
        Intent intent = new Intent(context, ThumbDetailListActivity.class);
        intent.putExtra(GlobalConstants.KEY_THEME, pageDataBean);
        context.startActivity(intent);

    }

    private int showData=0;
    private void getMessageList() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        LogUtil.e("data************************************ =" + coreManager.getSelf().getUserId());
        params.put("joinType", String.valueOf(0));
        params.put("programId", pageDataBean.getProgramId());
        params.put("pageIndex", String.valueOf(pageIndex));
        params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));
        DialogHelper.showDefaulteMessageProgressDialog(ThumbDetailListActivity.this);
        HttpUtils.post().url(coreManager.getConfig().RED_MY_JOIN_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<DiscussesBean>(DiscussesBean.class) {
                    @Override
                    public void onResponse(ArrayResult<DiscussesBean> result) {
                        DialogHelper.dismissProgressDialog();
                        refreshLayout.finishRefresh(true);
                        refreshLayout.finishLoadMore(true);
                        if (Result.checkSuccess(ThumbDetailListActivity.this, result) && result.getData() != null) {
                            List<DiscussesBean> data = result.getData();
                            if(data!=null &&data.size()>0){
                                if (pageIndex > 1) {
                                    mAdapter.addData(data);
                                } else {
                                    mAdapter.setNewData(data);
                                }
                            }
                       //     mAdapter.setNewData(data);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        refreshLayout.finishRefresh(false);
                        refreshLayout.finishLoadMore(false);
                        Toast.makeText(ThumbDetailListActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
