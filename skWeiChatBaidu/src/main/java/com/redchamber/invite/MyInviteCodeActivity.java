package com.redchamber.invite;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redchamber.bean.InviteCodeBean;
import com.redchamber.invite.adapter.MyInviteAdapter;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 我的邀请码
 */
public class MyInviteCodeActivity extends BaseActivity {

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.rv)
    RecyclerView mRvInvite;
    @BindView(R.id.tv_code)
    TextView mTvCode;
    @BindView(R.id.tv_invite_num)
    TextView mTvInviteNum;

    private String mInviteCode;
    private MyInviteAdapter mMyInviteAdapter;
    private int pageIndex = 1;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_my_invite_code;
    }

    @Override
    protected void initView() {
        initRefreshLayout();
        mMyInviteAdapter = new MyInviteAdapter(null);
        mRvInvite.setLayoutManager(new LinearLayoutManager(this));
        mRvInvite.setAdapter(mMyInviteAdapter);

        queryMyInviteCode();
    }

    @OnClick({R.id.iv_back, R.id.tv_copy})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_copy:
                if (TextUtils.isEmpty(mInviteCode)) {
                    return;
                }
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", mTvCode.getText().toString().trim());
                cm.setPrimaryClip(mClipData);
                ToastUtils.showToast("已复制到剪切板");
                break;
        }
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageIndex = 1;
                queryMyInviteCode();
            }
        });
        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                queryMyInviteCode();
            }
        });
    }

    public static void startMyInviteCodeActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, MyInviteCodeActivity.class));
    }

    private void queryMyInviteCode() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);
        params.put("pageIndex", String.valueOf(pageIndex));
        params.put("pageSize", "20");

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_GET_MY_INVITE_CODE)
                .params(params)
                .build()
                .execute(new BaseCallback<InviteCodeBean>(InviteCodeBean.class) {

                    @Override
                    public void onResponse(ObjectResult<InviteCodeBean> result) {
                        DialogHelper.dismissProgressDialog();
                        mRefreshLayout.finishRefresh(true);
                        mRefreshLayout.finishLoadMore(true);
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            mTvInviteNum.setText(String.valueOf(result.getData().inviteNum));
                            mInviteCode = result.getData().inviteCode;
                            mTvCode.setText(mInviteCode);
                            if (result.getData().userInvites != null &&
                                    result.getData().userInvites.size() > 0) {
                                if (pageIndex > 1) {
                                    mMyInviteAdapter.addData(result.getData().userInvites);
                                } else {
                                    mMyInviteAdapter.setNewData(result.getData().userInvites);
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
