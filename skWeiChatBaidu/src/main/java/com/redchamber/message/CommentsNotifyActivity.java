package com.redchamber.message;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.bean.SysteMessageBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.message.adapter.MessageAdapter;
import com.redchamber.message.adapter.MessageCommentsAdapter;
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
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

import static com.sk.weichat.MyApplication.getContext;

/**
 * 评论通知
 */
public class CommentsNotifyActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.rl_title)
    RelativeLayout rlTitle;
    @BindView(R.id.ll_empt)
    LinearLayout llEmpt;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    private MessageCommentsAdapter commentsAdapter;
    private List<SysteMessageBean> list = new ArrayList<>();
    private String functionType;


    @BindView(R.id.rv_comments_amount)
    RecyclerView rvCommentsAmount;

    @Override
    protected int setLayout() {
        return R.layout.activity_comments_notify;
    }

    @Override
    protected void initView() {
        getSupportActionBar().hide();
        functionType = getIntent().getStringExtra("type");
        tvTitle.setText(getIntent().getStringExtra("title"));
        if (MyApplication.mMyHomepageBean != null) {
            char[] arr = MyApplication.mMyHomepageBean.userLevel.toCharArray();
            commentsAdapter = new MessageCommentsAdapter(null, functionType,arr[0]);
            rvCommentsAmount.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCommentsAmount.setAdapter(commentsAdapter);

        }

        getMessageList();
        initOnClick();
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
                getMessageList();
            }
        });
    }

    private void initOnClick() {
        commentsAdapter.setOnPackageClickListener(new MessageAdapter.onPackageClickListener() {
            @Override
            public void onPackageItemClick(SysteMessageBean item) {

            }
        });
    }

    private int showData=0;
    private void getMessageList() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", functionType);
        params.put("pageIndex", String.valueOf(pageIndex));
        params.put("pageSize", String.valueOf(10));
        DialogHelper.showDefaulteMessageProgressDialog(CommentsNotifyActivity.this);

        HttpUtils.post().url(coreManager.getConfig().RED_SYSTEM_MESSAGE_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<SysteMessageBean>(SysteMessageBean.class) {
                    @Override
                    public void onResponse(ArrayResult<SysteMessageBean> result) {
                        DialogHelper.dismissProgressDialog();
                        refreshLayout.finishRefresh(true);
                        refreshLayout.finishLoadMore(true);
                        if (Result.checkSuccess(CommentsNotifyActivity.this, result)) {
                            list = result.getData();
                            if (list != null && list.size() > 0) {
                                showData=1;
                                if (pageIndex > 1) {
                                    commentsAdapter.addData(result.getData());
                                } else {
                                    commentsAdapter.setNewData(result.getData());
                                }
                                rvCommentsAmount.setVisibility(View.VISIBLE);
                                llEmpt.setVisibility(View.GONE);
                               // commentsAdapter.setNewData(result.getData());
                            } else {
                                if(showData!=1){
                                    rvCommentsAmount.setVisibility(View.GONE);
                                    llEmpt.setVisibility(View.VISIBLE);
                                }

                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        refreshLayout.finishRefresh(false);
                        refreshLayout.finishLoadMore(false);
                        Toast.makeText(CommentsNotifyActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @OnClick({R.id.iv_back})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;

        }
    }


}
