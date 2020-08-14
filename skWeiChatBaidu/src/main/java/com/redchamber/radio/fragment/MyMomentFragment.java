package com.redchamber.radio.fragment;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.redchamber.bean.BarHomeBean;
import com.redchamber.bean.PageDataBean;
import com.redchamber.lib.base.BaseFragment;
import com.redchamber.radio.adapter.MyMomentAdapter;
import com.redchamber.view.ReportPopupWindow;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sk.weichat.AppConfig;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.cjt2325.cameralibrary.util.LogUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;

import butterknife.BindView;
import okhttp3.Call;

/**
 * 个人中心 我的动态
 */
public class MyMomentFragment extends BaseFragment {

    @BindView(R.id.rv_moment)
    RecyclerView rvMoment;
    @BindView(R.id.swipeRefreshLayout)
    SmartRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rl_main)
    LinearLayout rlMain;
    @BindView(R.id.ll_empt)
    LinearLayout llEmpt;

    private MyMomentAdapter mAdapter;
    private int pageIndex = 1;

    @Override
    protected int setLayout() {
        return R.layout.red_fragment_my_moment;
    }

    @Override
    protected void initView() {
        mAdapter = new MyMomentAdapter(null);
        rvMoment.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMoment.setAdapter(mAdapter);
        initRefreshLayout();
        initOnClick();
        getMessageList();

    }

    private void initOnClick() {
        mAdapter.setBtnOnClice(new MyMomentAdapter.BtnOnCommClick() {
            @Override
            public void btnOnCommClick(PageDataBean pageDataBean, int fuctionType, int Position, View view) {

                ReportPopupWindow reportPopupWindow = new ReportPopupWindow(getActivity(), pageDataBean.getDiscussFlag() == 0 ? "开启评论" : "禁止评论", "删除动态", view);
                reportPopupWindow.setBtnOnClice(new ReportPopupWindow.BtnReportOnClick() {
                    @Override
                    public void btnReportOnClick(int type) {
                        if (type == 1) {
                            anonymousReporting(pageDataBean, Position);
                        } else if (type == 2) {
                            Delblock(pageDataBean, Position);
                        }
                    }
                });

            }
        });

    }

    /**
     * 禁止评论
     *
     * @param pageDataBean
     * @param position
     */
    public void anonymousReporting(PageDataBean pageDataBean, int position) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("programId", pageDataBean.getProgramId());
        params.put("flag", pageDataBean.getDiscussFlag() == 0 ? String.valueOf(1) : String.valueOf(0));
        HttpUtils.post().url(coreManager.getConfig().RED_MY_UPDATE_DISCUSS_FLAG)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (result.getResultCode() == 1) {
                            pageDataBean.setDiscussFlag(pageDataBean.getDiscussFlag() == 0 ? 1 : 0);
                            mAdapter.notifyItemChanged(position, pageDataBean);
                            mAdapter.notifyDataSetChanged();
                            ToastUtil.showLongToast(getActivity(), pageDataBean.getDiscussFlag() == 0 ? "禁止评论成功" : "开启评论成功");

                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });


    }

    /**
     * 删除节目
     *
     * @param pageDataBean
     * @param position
     */
    public void Delblock(PageDataBean pageDataBean, int position) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("programId", pageDataBean.getProgramId());
        HttpUtils.post().url(coreManager.getConfig().RED_MY_UPDATE_DELETE)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (result.getResultCode() == 1) {
                            mAdapter.remove(position);
                            mAdapter.notifyDataSetChanged();
                            ToastUtil.showLongToast(getActivity(), "删除动态成功");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
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
        params.put("type", String.valueOf(1));
        //params.put("cityId","0000");
        params.put("pageIndex", String.valueOf(pageIndex));
        params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));
        DialogHelper.showDefaulteMessageProgressDialog(getContext());
        HttpUtils.post().url(coreManager.getConfig().RED_MY_PROGRAMS)
                .params(params)
                .build()
                .execute(new BaseCallback<BarHomeBean>(BarHomeBean.class) {
                    @Override
                    public void onResponse(ObjectResult<BarHomeBean> result) {
                        DialogHelper.dismissProgressDialog();
                        swipeRefreshLayout.finishRefresh(true);
                        swipeRefreshLayout.finishLoadMore(true);
                        if (Result.checkSuccess(getContext(), result) && result.getData() != null && result.getData().pageData != null
                                && result.getData().pageData.size() > 0) {
                            mAdapter.setNewData(result.getData().pageData);
                            rvMoment.setVisibility(View.VISIBLE);
                            llEmpt.setVisibility(View.GONE);
                            showData=1;
                            if (pageIndex > 1) {
                                mAdapter.addData(result.getData().pageData);
                            } else {
                                mAdapter.setNewData(result.getData().pageData);
                            }
                           /* if (isRefresh) {
                                isRefresh = false;
                                mAdapter.setNewData(result.getData().pageData);
                            }*/
                        }else {
                            if(showData!=1) {
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
                        Toast.makeText(getContext(), R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });

    }


}
