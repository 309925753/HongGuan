package com.redchamber.message;

import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.redchamber.bean.SysteMessageBean;
import com.redchamber.lib.base.BaseFragment;
import com.redchamber.message.adapter.MessageAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sk.weichat.R;
import com.sk.weichat.view.cjt2325.cameralibrary.util.LogUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import okhttp3.Call;

/**
 * 显示评论
 */
public class SystemNewFragment extends BaseFragment {
    private MessageAdapter messageAdapter;
    private List<SysteMessageBean> list = new ArrayList<>();

    @BindView(R.id.swipeRefreshLayout)
    SmartRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_message_amount)
    RecyclerView mRvMessageAmount;


    private void initRefreshLayout() {

        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getMessageList();
            }
        });
        swipeRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getMessageList();
            }
        });

//        mSwipeLayout.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getMessageList();
    }

    private void initOnClick() {
        messageAdapter.setOnPackageClickListener(new MessageAdapter.onPackageClickListener() {
            @Override
            public void onPackageItemClick(SysteMessageBean item) {
               // type  0:电台  1：红馆 2：点赞 3:评论 4:报名 5:最新查看 6:最新评价
                switch (item.getType()){
                    case "0":
                        Intent intent3 = new Intent(getActivity(), MessageApplyActivity.class);
                        intent3.putExtra("type", item.getType());
                        intent3.putExtra("title", "电台");
                        startActivity(intent3);
                        break;
                    case "1":
                        Intent  intnetDian = new Intent(getActivity(), CommentsNotifyActivity.class);
                        intnetDian.putExtra("type", item.getType());
                        intnetDian.putExtra("title", "红馆");
                        startActivity(intnetDian);

                        break;
                    case "2":
                        Intent intent1 = new Intent(getActivity(), DianzanActivity.class);
                        intent1.putExtra("type", item.getType());
                        intent1.putExtra("title", "点赞");
                        startActivity(intent1);
                        break;
                    case "3":
                        Intent  intentComments = new Intent(getActivity(), UserCommActivity.class);
                        intentComments.putExtra("type", item.getType());
                        intentComments.putExtra("title", "评论");
                        startActivity(intentComments);

                        break;
                    case "4":
                        Intent intent2 = new Intent(getActivity(), UserEnrollActivity.class);
                        intent2.putExtra("type", item.getType());
                        intent2.putExtra("title", "报名");
                        startActivity(intent2);
                        break;
                    case "5":
                        Intent checkMeIntent = new Intent(getActivity(), CheckMeActivity.class);
                        checkMeIntent.putExtra("title", "查看申请");
                        startActivity(checkMeIntent);

                        break;
                    case "6":
                        Intent  intnetDian6 = new Intent(getActivity(), CommentsNotifyActivity.class);
                        intnetDian6.putExtra("type", item.getType());
                        intnetDian6.putExtra("title", "评价通知");
                        startActivity(intnetDian6);
                        break;
                    case "7":
                        Intent intent7 = new Intent(getActivity(), CommentsNotifyActivity.class);
                        intent7.putExtra("type", item.getType());
                        intent7.putExtra("title", "钱包提醒");
                        startActivity(intent7);

                        break;

                }
            }
        });
    }

    @Override
    protected int setLayout() {
         return R.layout.fragment_system_new;
    }

    @Override
    protected void initView() {
        messageAdapter=new MessageAdapter(null);
        mRvMessageAmount.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvMessageAmount.setAdapter(messageAdapter);
      //  getMessageList();
        initOnClick();
        initRefreshLayout();

    }


    private void getMessageList(){
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token",coreManager.getSelfStatus().accessToken);
        HttpUtils.post().url(coreManager.getConfig().RED_MESSAGE_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<SysteMessageBean>(SysteMessageBean.class) {
                    @Override
                    public void onResponse(ArrayResult<SysteMessageBean> result) {
                        swipeRefreshLayout.finishRefresh(true);
                        swipeRefreshLayout.finishLoadMore(true);
                        if (Result.checkSuccess(getActivity(), result)) {
                            list = result.getData();
                            if (list != null && list.size() > 0) {
                                messageAdapter.setNewData(result.getData());
                            }
                        }
                    }
                    @Override
                    public void onError(Call call, Exception e) {
                        swipeRefreshLayout.finishRefresh(false);
                        swipeRefreshLayout.finishLoadMore(false);
                        Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });

    }

}
