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

import com.redchamber.bean.PageDataBean;
import com.redchamber.bean.SysteMessageBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.message.adapter.UserMessageCommAdapter;
import com.redchamber.radio.MomentDetailActivity;
import com.redchamber.radio.ProgramDetailActivity;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sk.weichat.AppConfig;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.view.cjt2325.cameralibrary.util.LogUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
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
 * 报名
 */
public class UserCommActivity extends BaseActivity {
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
    private UserMessageCommAdapter messageAdapter;
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
        tvTitle.setText(getIntent().getStringExtra("title"));
        messageAdapter = new UserMessageCommAdapter(null);
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
        messageAdapter.setOnPackageClickListener(new UserMessageCommAdapter.onPackageClickListener() {
            @Override
            public void onPackageItemClick(SysteMessageBean item) {
                if (item.getNeType() == 0) {
                    //   HeProgramOnLineActivity.startActivity(DianzanActivity.this, String.valueOf(item.getUserId()), item.getProgramId());
                    myDetails(item.getProgramId(), 0);
                } else {
                    myDetails(item.getProgramId(), 1);
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
        DialogHelper.showDefaulteMessageProgressDialog(UserCommActivity.this);
        HttpUtils.post().url(coreManager.getConfig().RED_SYSTEM_MESSAGE_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<SysteMessageBean>(SysteMessageBean.class) {
                    @Override
                    public void onResponse(ArrayResult<SysteMessageBean> result) {
                        DialogHelper.dismissProgressDialog();
                        refreshLayout.finishRefresh(true);
                        refreshLayout.finishLoadMore(true);
                        if (Result.checkSuccess(UserCommActivity.this, result)) {
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
                                if(showData!=1){
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
                        Toast.makeText(UserCommActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void myDetails(String programId, int type) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        LogUtil.e("data************************************ =" + coreManager.getSelf().getUserId());
        params.put("programId", String.valueOf(programId));
        params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));
        HttpUtils.post().url(coreManager.getConfig().RED_MY_DETAILS)
                .params(params)
                .build()
                .execute(new BaseCallback<PageDataBean>(PageDataBean.class) {
                    @Override
                    public void onResponse(ObjectResult<PageDataBean> result) {
                        if (Result.checkSuccess(getContext(), result) && result.getData() != null && result.getData() != null) {

                            ////类型 0节目 1动态
                            if (type == 0) {
                                ProgramDetailActivity.startActivity(mContext, result.getData());
                            } else {
                                MomentDetailActivity.startActivity(mContext, result.getData());
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getContext(), R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });

    }


}


