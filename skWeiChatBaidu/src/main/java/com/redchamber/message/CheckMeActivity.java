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
import com.redchamber.friend.FriendHomePageActivity;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.message.adapter.CheckMeApplyAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.util.ToastUtil;
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

public class CheckMeActivity extends BaseActivity {

    @BindView(R.id.rv_apply_amount)
    RecyclerView rvApplyAmount;
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
    private List<SysteMessageBean> list = new ArrayList<>();
    private CheckMeApplyAdapter checkMeApplyAdapter;

    @Override
    protected int setLayout() {
        return R.layout.activity_message_apply;
    }

    @Override
    protected void initView() {
        getSupportActionBar().hide();
        tvTitle.setText(getIntent().getStringExtra("title").toString().trim());
        if (MyApplication.mMyHomepageBean != null) {
            char[] arr = MyApplication.mMyHomepageBean.userLevel.toCharArray();
            checkMeApplyAdapter = new CheckMeApplyAdapter(null,arr[0]);
            rvApplyAmount.setLayoutManager(new LinearLayoutManager(getContext()));
            rvApplyAmount.setAdapter(checkMeApplyAdapter);
        }


        getMessageList();
        initOnClick();
        initRefreshLayout();
    }
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
        checkMeApplyAdapter.setOnPackageClickListener(new CheckMeApplyAdapter.onPackageClickListener() {
            @Override
            public void onPackageItemClick(SysteMessageBean item) {
                if (MyApplication.mMyHomepageBean != null) {
                    char[] arr = MyApplication.mMyHomepageBean.userLevel.toCharArray();
                    if(arr[0]=='0'){
                        FriendHomePageActivity.startFriendHomePageActivity(mContext, String.valueOf(item.getApplyUserId()));
                    }else {
                        FriendHomePageActivity.startFriendHomePageActivity(mContext, String.valueOf(item.getUserId()));
                    }

                }


            }
        });
        checkMeApplyAdapter.setOnPackageClickListener(new CheckMeApplyAdapter.onCheckMeClickListener() {
            @Override
            public void OnCheckMeClickListener(SysteMessageBean systeMessageBean, int type) {
                //1 为允许   2为拒绝
                if (type == 1) {
                    auditAllow(systeMessageBean);
                } else {
                    auditRefuse(systeMessageBean);
                }
            }
        });
    }

    private int pageIndex=1;
    private void auditAllow(SysteMessageBean systeMessageBean) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("applyId", systeMessageBean.getId());
        params.put("flag", String.valueOf(1));
        HttpUtils.post().url(coreManager.getConfig().RED_SYSTEM_PHOTO_AUDIT)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {

                        if (result.getResultCode() == 1) {
                            ToastUtil.showLongToast(CheckMeActivity.this,"已允许");
                            pageIndex=1;
                            getMessageList();

                        } else {
                            Toast.makeText(CheckMeActivity.this, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(CheckMeActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void auditRefuse(SysteMessageBean systeMessageBean) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("applyId", systeMessageBean.getId());
        params.put("flag", String.valueOf(-1));
        HttpUtils.post().url(coreManager.getConfig().RED_SYSTEM_PHOTO_AUDIT)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {

                        if (result.getResultCode() == 1) {
                            ToastUtil.showLongToast(CheckMeActivity.this,"已拒绝");
                            pageIndex=1;
                            getMessageList();
                        } else {
                            Toast.makeText(CheckMeActivity.this, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(CheckMeActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int showData=0;
    private void getMessageList() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", String.valueOf(5));
        params.put("pageIndex", String.valueOf(pageIndex));
        params.put("pageSize", String.valueOf(10));
        DialogHelper.showDefaulteMessageProgressDialog(CheckMeActivity.this);
        HttpUtils.post().url(coreManager.getConfig().RED_SYSTEM_MESSAGE_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<SysteMessageBean>(SysteMessageBean.class) {
                    @Override
                    public void onResponse(ArrayResult<SysteMessageBean> result) {
                        DialogHelper.dismissProgressDialog();
                        refreshLayout.finishRefresh(true);
                        refreshLayout.finishLoadMore(true);
                        if (Result.checkSuccess(CheckMeActivity.this, result)) {
                            list = result.getData();
                            if (list != null && list.size() > 0) {
                                showData=1;
                                if (pageIndex > 1) {
                                    checkMeApplyAdapter.addData(result.getData());
                                } else {
                                    checkMeApplyAdapter.setNewData(result.getData());
                                }
                                rvApplyAmount.setVisibility(View.VISIBLE);
                                llEmpt.setVisibility(View.GONE);
                                //  messageAdapter.setNewData(result.getData());

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
                        Toast.makeText(CheckMeActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });

    }


}
