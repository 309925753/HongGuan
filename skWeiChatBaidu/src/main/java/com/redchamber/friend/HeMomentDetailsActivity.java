package com.redchamber.friend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.redchamber.api.GlobalConstants;
import com.redchamber.auth.AuthenticationCenterActivity;
import com.redchamber.bean.BannerImageBean;
import com.redchamber.bean.BarHomeBean;
import com.redchamber.bean.PageDataBean;
import com.redchamber.bean.QueryFreeAuthBean;
import com.redchamber.friend.adapter.HeMomentAdapter;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.message.CheckMeActivity;
import com.redchamber.release.ReleaseMomentActivity;
import com.redchamber.report.AnonymousReportActivity;
import com.redchamber.request.BlackRequest;
import com.redchamber.view.CommCodeDialog;
import com.redchamber.view.ReleaseCodeDialog;
import com.redchamber.view.ReleaseRadioSelectDialog;
import com.redchamber.view.ReportPopupWindow;
import com.redchamber.view.SelectProgramThemeDialog;
import com.redchamber.vip.VipCenterActivity;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sk.weichat.AppConfig;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.TrillCommentInputDialog;
import com.sk.weichat.view.cjt2325.cameralibrary.util.LogUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 用户动态列表
 */
public class HeMomentDetailsActivity extends BaseActivity {

    @BindView(R.id.rv_moment)
    RecyclerView rvMoment;
    @BindView(R.id.swipeRefreshLayout)
    SmartRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rl_main)
    LinearLayout rlMain;
    @BindView(R.id.tv_nickname)
    TextView tvNickname;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.rl_title)
    RelativeLayout rlTitle;
    @BindView(R.id.ll_empt)
    LinearLayout llEmpt;

    private HeMomentAdapter mAdapter;
    private int pageIndex = 1;
    private String mUserId = null;

    @Override
    protected int setLayout() {
        return R.layout.activity_he_moment_details;
    }

    @Override
    protected void initView() {
        getSupportActionBar().hide();
        if (getIntent() != null) {
            mUserId = getIntent().getStringExtra(GlobalConstants.KEY_THEME);
        }
        mAdapter = new HeMomentAdapter(null);
        rvMoment.setLayoutManager(new LinearLayoutManager(HeMomentDetailsActivity.this));
        rvMoment.setAdapter(mAdapter);
        getMessageList();
        initRefreshLayout();
        initData();

    }
    @OnClick({R.id.iv_back})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;

        }
    }

    private void initData() {
        mAdapter.setBtnOnClice(new HeMomentAdapter.BtnOnClick() {
            @Override
            public void btnOnClick(PageDataBean pageDataBean, int fuctionType, int Position) {

                if (MyApplication.mMyHomepageBean != null) {
                    char[] arr = MyApplication.mMyHomepageBean.userLevel.toCharArray();
                    if (arr[0] == '1' && arr[1] == '0') {
                        CommCodeDialog commCodeDialog = new CommCodeDialog(HeMomentDetailsActivity.this, "当前需要开通VIP", "办理会员");
                        commCodeDialog.show();
                        commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
                            @Override
                            public void onConfirmClick() {
                                VipCenterActivity.startVipCenterActivity(HeMomentDetailsActivity.this);
                            }
                        });
                        return;
                    } else if ((arr[0] == '0')) {
                        if( arr[1] == '0' || arr[3] == '0') {
                            ReleaseCodeDialog commCodeDialog = new ReleaseCodeDialog(HeMomentDetailsActivity.this, "需要真人认证或VIP", "真人认证", "办理会员");
                            commCodeDialog.show();
                            commCodeDialog.setOnConfirmListener(new ReleaseCodeDialog.OnConfirmListener() {
                                @Override
                                public void onConfirmClick(int type) {
                                    if (type == 1) {
                                        AuthenticationCenterActivity.startAuthenticationCenterActivity(HeMomentDetailsActivity.this);
                                    } else if (type == 2) {
                                        VipCenterActivity.startVipCenterActivity(HeMomentDetailsActivity.this);
                                    }
                                }
                            });
                            return;
                        }
                    }
                    switch (fuctionType) {
                        case 1:
                            puJoinProgram(pageDataBean, 0, "", Position);
                            break;
                        case 2:
                            if (pageDataBean.getDiscussFlag() == 0) {
                                ToastUtil.showLongToast(HeMomentDetailsActivity.this, "用户评论已经关闭");
                                return;
                            }
                            if (arr[3] == '1') {
                                TrillCommentInputDialog trillCommentInputDialog = new TrillCommentInputDialog(HeMomentDetailsActivity.this, getString(R.string.enter_pinlunt),
                                        str -> {
                                            LogUtil.e("***********" + str);
                                            puJoinProgram(pageDataBean, 1, str, Position);
                                        });
                                Window window = trillCommentInputDialog.getWindow();
                                if (window != null) {
                                    //OnSendCommentListener
                                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);// 软键盘弹起
                                    trillCommentInputDialog.show();
                                }
                            } else {
                                CommCodeDialog commCodeDialog = new CommCodeDialog(HeMomentDetailsActivity.this, "真人认证后才可以评论", "去真人认证");
                                commCodeDialog.show();
                                commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
                                    @Override
                                    public void onConfirmClick() {
                                        AuthenticationCenterActivity.startAuthenticationCenterActivity(HeMomentDetailsActivity.this);
                                    }
                                });
                            }

                            break;
                    }
                }
            }
        });


        mAdapter.setBtnOnClice(new HeMomentAdapter.BtnOnCommClick() {
            @Override
            public void btnOnCommClick(PageDataBean pageDataBean, int fuctionType, int Position, View view) {
                ReportPopupWindow reportPopupWindow = new ReportPopupWindow(HeMomentDetailsActivity.this, "匿名举报", "拉黑", view);
                reportPopupWindow.setBtnOnClice(new ReportPopupWindow.BtnReportOnClick() {
                    @Override
                    public void btnReportOnClick(int type) {
                        if (type == 1) {
                            // anonymousReporting(pageDataBean,Position);
                            AnonymousReportActivity.startActivity(HeMomentDetailsActivity.this, String.valueOf(pageDataBean.getUserId()));
                        } else if (type == 2) {
                            block(pageDataBean, Position);
                        }
                    }
                });

            }
        });
    }

    /**
     * 拉黑
     *
     * @param pageDataBean
     * @param position
     */
    public void block(PageDataBean pageDataBean, int position) {
        {
            BlackRequest.getInstance().addBlackList(HeMomentDetailsActivity.this, String.valueOf(pageDataBean.getUserId()),
                    "0", new BlackRequest.AddBlackListCallBack() {
                        @Override
                        public void onSuccess() {
                           /* mAdapter.remove(position);
                            mAdapter.notifyDataSetChanged();
                            if (showData != 1) {
                                rvMoment.setVisibility(View.GONE);
                                llEmpt.setVisibility(View.VISIBLE);
                            }*/
                        }

                        @Override
                        public void onFail(String error) {
                            ToastUtils.showToast(error);
                        }
                    });
        }

    }


    /**
     * 参与节目/动态(点赞、评论、报名等)
     */
    private void puJoinProgram(PageDataBean pageDataBean, int type, String contnet, int position) {
        List<BannerImageBean> list = new ArrayList<>();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("programId", pageDataBean.getProgramId());
        params.put("type", String.valueOf(type));
        params.put("content", contnet);
        HttpUtils.post().url(coreManager.getConfig().RED_JOIN_PROGRAM)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {

                        if (result.getResultCode() == 1) {
                            switch (type) {
                                case 0:
                                    ToastUtil.showLongToast(HeMomentDetailsActivity.this, "点赞成功");
                                    pageDataBean.setLikeNum(pageDataBean.getLikeNum() + 1);
                                    mAdapter.notifyItemChanged(position, pageDataBean);
                                    mAdapter.notifyDataSetChanged();
                                    break;
                                case 1:
                                    ToastUtil.showLongToast(HeMomentDetailsActivity.this, "评论成功");
                                    pageDataBean.setDiscussNum(pageDataBean.getDiscussNum() + 1);
                                    mAdapter.notifyItemChanged(position, pageDataBean);
                                    mAdapter.notifyDataSetChanged();
                                    break;
                                case 2:
                                    pageDataBean.setSignUpNums(pageDataBean.getSignUpNums() + 1);
                                    mAdapter.notifyItemChanged(position, pageDataBean);
                                    //报名成功，
                                    CommCodeDialog commCodeDialog = new CommCodeDialog(HeMomentDetailsActivity.this, "报名成功，如果对方觉得合适将会联系你", "知道了");
                                    commCodeDialog.show();
                                    commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
                                        @Override
                                        public void onConfirmClick() {

                                        }
                                    });
                                    break;
                            }

                        } else {
                            showBarCom(result.getResultMsg(), result.getResultCode());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(HeMomentDetailsActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int REAL_PEOPLE_VERIFY_FIRST = 100439;
    private int HAVENOLEGAL_POWER = 100440;
    private int TIMES_HAVE_USED = 10441;

    private void showBarCom(String title, int resultCode) {
        String confirm = null;

        if (resultCode == REAL_PEOPLE_VERIFY_FIRST) {
            confirm = "真人认证后才可以发布";

        } else if (resultCode == HAVENOLEGAL_POWER) {
            confirm = "会员才可以评论哦";
            title = "成为会员，免费评论";

        } else if (resultCode == TIMES_HAVE_USED) {
            confirm = "次数已用完";
        } else if (resultCode == 100445) {
            //没有配置次数，提示升级到VIP或则余额购买
            confirm = "确认";
        } else {
            //没有配置次数，提示升级到VIP或则余额购买
            confirm = "确认";
        }

        if (resultCode == 100445) {
            // 没有配置次数，提示升级到VIP或则余额购买 100445
            queryFreeAuthTimes(resultCode);
            return;
        }
        CommCodeDialog commCodeDialog = new CommCodeDialog(HeMomentDetailsActivity.this, title, confirm);
        commCodeDialog.show();
        commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick() {
                if (resultCode == REAL_PEOPLE_VERIFY_FIRST) {
                    AuthenticationCenterActivity.startAuthenticationCenterActivity(HeMomentDetailsActivity.this);
                } else if (resultCode == HAVENOLEGAL_POWER) {
                    VipCenterActivity.startVipCenterActivity(HeMomentDetailsActivity.this);
                } else if (resultCode == TIMES_HAVE_USED) {
                    queryFreeAuthTimes(resultCode);
                } else if (resultCode == 100445) {
                    //    VipCenterActivity.startVipCenterActivity(getContext());
                    //   queryFreeAuthTimes(resultCode);
                }

            }
        });

    }

    /**
     * 获取免费权限的次数
     */
    private void queryFreeAuthTimes(int resultCode) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", "001");
        HttpUtils.post().url(coreManager.getConfig().RED_MY_QUERY_FREEAUTH_TIMES)
                .params(params)
                .build()
                .execute(new BaseCallback<QueryFreeAuthBean>(QueryFreeAuthBean.class) {

                    @Override
                    public void onResponse(ObjectResult<QueryFreeAuthBean> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            if (resultCode == TIMES_HAVE_USED) {
                                payRed(result.getData(), TIMES_HAVE_USED);
                            } else if (resultCode == 100445) {
                                // 没有配置次数，提示升级到VIP或则余额购买 100445
                                VipOrPay(result.getData(), resultCode);
                            }

                        } else {
                            LogUtil.e(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(HeMomentDetailsActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void VipOrPay(QueryFreeAuthBean queryFreeAuthBean, int resultCode) {

        ReleaseCodeDialog commCodeDialog = new ReleaseCodeDialog(HeMomentDetailsActivity.this, "发布动态或节目", "支付" + queryFreeAuthBean.getPrice() + "红豆", "会员免费发布6次");
        commCodeDialog.show();
        commCodeDialog.setOnConfirmListener(new ReleaseCodeDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick(int type) {
                if (type == 1) {
                    payPublish(queryFreeAuthBean.getPrice());
                } else if (type == 2) {
                    VipCenterActivity.startVipCenterActivity(HeMomentDetailsActivity.this);
                }
            }
        });
    }


    /**
     * 红豆支付
     */
    private void payRed(QueryFreeAuthBean queryFreeAuthBean, int resultCode) {
        CommCodeDialog commCodeDialog = new CommCodeDialog(HeMomentDetailsActivity.this, "次数已用完", "支付" + queryFreeAuthBean.getPrice() + "红豆");
        commCodeDialog.show();
        commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick() {
                payPublish(queryFreeAuthBean.getPrice());
            }
        });
    }

    /**
     * 付费发布
     */
    private void payPublish(int payPublish) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("gold", String.valueOf(payPublish));
        HttpUtils.post().url(coreManager.getConfig().RED_MY_PAY_FORPUB)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (result.getResultCode() == 1) {
                            showRelease();
                        } else {
                            ToastUtil.showLongToast(HeMomentDetailsActivity.this, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(HeMomentDetailsActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRelease() {
        ReleaseRadioSelectDialog radioSelectDialog = new ReleaseRadioSelectDialog(HeMomentDetailsActivity.this);
        radioSelectDialog.show();
        radioSelectDialog.setOnConfirmListener(new ReleaseRadioSelectDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick(int type) {
                if (0 == type) {
                    SelectProgramThemeDialog selectProgramThemeDialog = new SelectProgramThemeDialog(HeMomentDetailsActivity.this);
                    selectProgramThemeDialog.show();
                } else {
                    ReleaseMomentActivity.startReleaseMomentActivity(HeMomentDetailsActivity.this);
                }
            }
        });
    }


    private boolean isRefresh = false;

    private void initRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageIndex = 1;
                getMessageList();
            }
        });
        swipeRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                pageIndex++;
                getMessageList();
            }
        });
    }

    private int showData=0;
    private void getMessageList() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        LogUtil.e("data************************************ =" + coreManager.getSelf().getUserId());
        params.put("pageIndex", String.valueOf(pageIndex));
        params.put("userId", mUserId);
        params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));
        DialogHelper.showDefaulteMessageProgressDialog(HeMomentDetailsActivity.this);
        HttpUtils.post().url(coreManager.getConfig().RED_MY_USER_NEWS)
                .params(params)
                .build()
                .execute(new BaseCallback<BarHomeBean>(BarHomeBean.class) {
                    @Override
                    public void onResponse(ObjectResult<BarHomeBean> result) {
                        DialogHelper.dismissProgressDialog();
                        swipeRefreshLayout.finishRefresh(true);
                        swipeRefreshLayout.finishLoadMore(true);
                        if (Result.checkSuccess(HeMomentDetailsActivity.this, result) && result.getData() != null && result.getData().pageData != null
                                && result.getData().pageData.size() > 0) {
                            showData=1;
                           /* mAdapter.setNewData(result.getData().pageData);
                            if (isRefresh) {
                                isRefresh = false;
                                mAdapter.setNewData(result.getData().pageData);
                            }*/
                            if (pageIndex > 1) {
                                mAdapter.addData(result.getData().pageData);
                            } else {
                                mAdapter.setNewData(result.getData().pageData);
                            }
                            rvMoment.setVisibility(View.VISIBLE);
                            llEmpt.setVisibility(View.GONE);
                        }else {
                            if (showData != 1) {
                                rvMoment.setVisibility(View.GONE);
                                llEmpt.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        swipeRefreshLayout.finishRefresh(false);
                        swipeRefreshLayout.finishLoadMore(false);
                        Toast.makeText(HeMomentDetailsActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public static void startActivity(Context context, String mUserId) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, HeMomentDetailsActivity.class);
        intent.putExtra(GlobalConstants.KEY_THEME, mUserId);
        context.startActivity(intent);
    }



}
