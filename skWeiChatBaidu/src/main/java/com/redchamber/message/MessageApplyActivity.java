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
import com.redchamber.friend.HeProgramOnLineActivity;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.message.adapter.MessageAdapter;
import com.redchamber.message.adapter.MessageApplyAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
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
public class MessageApplyActivity extends BaseActivity {
    @BindView(R.id.rv_apply_amount)
    RecyclerView rvApplyAmount;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.rl_title)
    RelativeLayout rlTitle;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    private MessageApplyAdapter messageAdapter;
    private List<SysteMessageBean> list = new ArrayList<>();
    private String functionType;
    @BindView(R.id.ll_empt)
    LinearLayout llEmpt;


    @Override
    protected int setLayout() {
        return R.layout.activity_message_apply;
    }

    @Override
    protected void initView() {
        getSupportActionBar().hide();
        functionType = getIntent().getStringExtra("type");
        tvTitle.setText(getIntent().getStringExtra("title").toString().trim());

        messageAdapter = new MessageApplyAdapter(null);
        rvApplyAmount.setLayoutManager(new LinearLayoutManager(getContext()));
        rvApplyAmount.setAdapter(messageAdapter);

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
                pageIndex++;
                getMessageList();
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

    private void initOnClick() {
        messageAdapter.setOnPackageClickListener(new MessageAdapter.onPackageClickListener() {
            @Override
            public void onPackageItemClick(SysteMessageBean item) {
                // type  0:电台  1：红馆 2：点赞 3:评论 4:报名 5:最新查看 6:最新评价
                //类型 0节目 1动态
                if (item.getType().equals("0")) {
                    HeProgramOnLineActivity.startActivity(MessageApplyActivity.this, String.valueOf(item.getOauthUserId()), item.getOauthId());
                } else {

                }

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
        DialogHelper.showDefaulteMessageProgressDialog(MessageApplyActivity.this);
        HttpUtils.post().url(coreManager.getConfig().RED_SYSTEM_MESSAGE_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<SysteMessageBean>(SysteMessageBean.class) {
                    @Override
                    public void onResponse(ArrayResult<SysteMessageBean> result) {
                        DialogHelper.dismissProgressDialog();
                        refreshLayout.finishRefresh(true);
                        refreshLayout.finishLoadMore(true);
                        if (Result.checkSuccess(MessageApplyActivity.this, result)) {
                            list = result.getData();
                            if (list != null && list.size() > 0) {
                                showData=1;
                                if (pageIndex > 1) {
                                    messageAdapter.addData(result.getData());
                                } else {
                                    messageAdapter.setNewData(result.getData());
                                }
                                rvApplyAmount.setVisibility(View.VISIBLE);
                                llEmpt.setVisibility(View.GONE);
                               // messageAdapter.setNewData(result.getData());
                            } else {
                                if (showData != 1) {
                                    rvApplyAmount.setVisibility(View.GONE);
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
                        Toast.makeText(MessageApplyActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });

    }


}
