package com.sk.weichat.ui.trill;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.danikula.videocache.HttpProxyCacheServer;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.circle.PublicMessage;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseRecAdapter;
import com.sk.weichat.ui.base.BaseRecViewHolder;
import com.sk.weichat.ui.base.EasyFragment;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 推荐页短视频
 */
public class TrillHomeFragment extends EasyFragment {

    public int pagerIndex, position;
    public boolean isLoad;
    private RecyclerView mPager;
    private PagerSnapHelper snapHelper;
    private LinearLayoutManager layoutManager;
    private ListVideoAdapter videoAdapter;
    private List<PublicMessage> urlList;

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_trill_home;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        initView();
        initData();
        addListener();
    }


    private void initView() {
        mPager = findViewById(R.id.rv_pager);

        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mPager);

        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mPager.setLayoutManager(layoutManager);
        urlList = new ArrayList<>();

        videoAdapter = new ListVideoAdapter(urlList);
        mPager.setAdapter(videoAdapter);

        //        PreferenceUtils.putInt(this, "trill_index", pagerIndex);
        //        PreferenceUtils.putInt(this, "trill_position", position % 10);
    }

    private void initData() {
        pagerIndex = PreferenceUtils.getInt(getContext(), "trill_index", 0);
        position = PreferenceUtils.getInt(getContext(), "trill_position", 0);

        loadData();
    }


    private void addListener() {
        mPager.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE://停止滚动

                        if (urlList == null || urlList.size() == 0) {
                            return;
                        }

                        View view = snapHelper.findSnapView(layoutManager);
                        RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
                        position = recyclerView.getChildLayoutPosition(view);

                        if (viewHolder != null && viewHolder instanceof VideoViewHolder) {
                            JcvTrillVideo video = ((VideoViewHolder) viewHolder).mp_video;
                            //                            Log.e("xuan", position + " : 滚动到: " + video.getCurrState());
                            video.startVideo();
                        }

                        if (urlList.size() > position + 1) {
                            Log.e("xuan", "onScrollStateChanged: " + (position + 1) + ",  size " + urlList.size());
                            HttpProxyCacheServer proxy = MyApplication.getProxy(getContext());
                            proxy.getProxyUrl(urlList.get(position + 1).getFirstVideo());
                        }

                        if (!isLoad && position > urlList.size() - 4) {
                            pagerIndex++;
                            loadData();
                        }

                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING://拖动
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING://惯性滑动
                        break;
                }

            }
        });
    }

    private void loadData() {
        isLoad = true;
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageIndex", pagerIndex + "");
        params.put("pageSize", "10");// 给一个尽量大的值
        params.put("userId", coreManager.getSelf().getUserId());

        HttpUtils.get().url(coreManager.getConfig().GET_TRILL_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(ArrayResult<PublicMessage> result) {
                        isLoad = false;
                        DialogHelper.dismissProgressDialog();
                        List<PublicMessage> data = result.getData();
                        if (data != null && data.size() > 0) {
                            urlList.addAll(data);
                            Log.e("xuan", "onResponse: " + urlList.size());
                            videoAdapter.notifyDataSetChanged();
                            layoutManager.scrollToPosition(position);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getContext());
                        isLoad = false;
                    }
                });
    }


    class ListVideoAdapter extends BaseRecAdapter<PublicMessage, VideoViewHolder> {
        public ListVideoAdapter(List<PublicMessage> list) {
            super(list);
        }

        @Override
        public void onHolder(VideoViewHolder holder, PublicMessage bean, int position) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            holder.mp_video.updateDatas(bean, coreManager.getSelf(), coreManager.getConfig().MSG_COMMENT_ADD, coreManager.getSelfStatus().accessToken);
            if (position == TrillHomeFragment.this.position) {
                holder.mp_video.startVideo();
            }
        }

        @Override
        public VideoViewHolder onCreateHolder() {
            return new VideoViewHolder(getViewByRes(R.layout.item_trill));
        }
    }

    public class VideoViewHolder extends BaseRecViewHolder {
        public View rootView;
        public JcvTrillVideo mp_video;

        public VideoViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.mp_video = rootView.findViewById(R.id.mp_video);
            // this.tv_title = rootView.findViewById(R.id.tv_title);
        }
    }
}
