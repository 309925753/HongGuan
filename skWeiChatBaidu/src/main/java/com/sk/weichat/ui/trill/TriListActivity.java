package com.sk.weichat.ui.trill;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.danikula.videocache.HttpProxyCacheServer;
import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.circle.PublicMessage;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.fragment.TrillFragment;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.TrillStatisticsHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.base.BaseRecAdapter;
import com.sk.weichat.ui.base.BaseRecViewHolder;
import com.sk.weichat.ui.message.InstantMessageActivity;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.VideotillManager;
import okhttp3.Call;

public class TriListActivity extends BaseActivity implements View.OnClickListener {
    boolean shareBack;
    private RecyclerView mPager;
    private ListVideoAdapter videoAdapter;
    private List<PublicMessage> urlList;
    private PagerSnapHelper snapHelper;
    private LinearLayoutManager layoutManager;
    private boolean isLoad;
    private int pagerIndex;
    private int position = -1;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            videoAdapter.notifyDataSetChanged();
            layoutManager.scrollToPosition(position);
            return true;
        }
    });
    private TextView tvTitle;
    private int mLable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trill);
        getSupportActionBar().hide();

        initData();
        initView();
        addListener();
    }

    private void initData() {
        position = getIntent().getIntExtra("position", 0);
        pagerIndex = getIntent().getIntExtra("pagerIndex", 0);
        mLable = getIntent().getIntExtra("tab_lable", 0);

        urlList = TrillFragment.urlList;
    }

    private void initView() {
        tvTitle = findViewById(R.id.tv_text);
        String name = getIntent().getStringExtra("tab_name");
        tvTitle.setText(name);

        mPager = findViewById(R.id.rv_pager);
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mPager);
        videoAdapter = new ListVideoAdapter(urlList);
        layoutManager = new LinearLayoutManager(this);
        mPager.setLayoutManager(layoutManager);
        mPager.setAdapter(videoAdapter);
        layoutManager.scrollToPosition(position);

        findViewById(R.id.iv_title_add).setVisibility(View.INVISIBLE);
        findViewById(R.id.iv_title_left).setOnClickListener(this);

        mPager.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int position = -1;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int cur = layoutManager.findFirstVisibleItemPosition();
                if (cur != position) {
                    position = cur;
                    TrillStatisticsHelper.play(mContext, coreManager, urlList.get(cur));
                }
            }
        });
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
                            //  Log.e("xuan", position + " : 滚动到: " + video.getCurrState());
                            video.startVideo();
                        }

                        if (urlList.size() > position + 1) {
                            Log.e("xuan", "onScrollStateChanged: " + (position + 1) + ",  size " + urlList.size());
                            HttpProxyCacheServer proxy = MyApplication.getProxy(TriListActivity.this);
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
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageIndex", pagerIndex + "");
        params.put("pageSize", "10");// 给一个尽量大的值
        params.put("userId", coreManager.getSelf().getUserId());
        if (mLable > 0) {
            params.put("lable", Integer.toString(mLable));
        }

        HttpUtils.get().url(coreManager.getConfig().GET_TRILL_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(ArrayResult<PublicMessage> result) {
                        DialogHelper.dismissProgressDialog();
                        isLoad = false;
                        if (Result.checkSuccess(mContext, result)) {
                            List<PublicMessage> data = result.getData();
                            if (data != null && data.size() > 0) {
                                urlList.addAll(data);
                                Log.e("xuan", "onResponse: " + urlList.size());
                                videoAdapter.notifyDataSetChanged();
                                layoutManager.scrollToPosition(position);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        isLoad = false;
                        ToastUtil.showErrorNet(TriListActivity.this);
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        VideotillManager.instance().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("xuan", "onResume: " + shareBack);
        if (!shareBack) {
            VideotillManager.instance().play();
        } else {
            VideotillManager.instance().releaseVideo();
            JCMediaManager.instance().releaseMediaPlayer();
            handler.sendEmptyMessageDelayed(1, 500);

        }
        shareBack = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        JCMediaManager.addOnJcvdListener(null);
        VideotillManager.instance().releaseVideo();
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    public void onShare(String trillId, String videoUrl, long fileSize, int position) {
        String userid = coreManager.getSelf().getUserId();
        shareBack = true;
        this.position = position;
        String packetId = trillId;
        ChatMessage message = new ChatMessage(); //
        message.setContent(videoUrl);
        message.setFromUserId(AppConstant.TRILL_INSTANT_ID); // url
        message.setFromUserName(AppConstant.TRILL_INSTANT_ID);
        message.setFilePath(videoUrl);
        message.setDownload(false);
        message.setUpload(true);
        message.setFileSize((int) fileSize);
        message.setToUserId(userid);
        message.setPacketId(packetId);
        message.setType(XmppMessage.TYPE_VIDEO);
        message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        ChatMessageDao.getInstance().saveNewSingleChatMessage(userid, AppConstant.TRILL_INSTANT_ID, message);

        Intent intent = new Intent(mContext, InstantMessageActivity.class);
        intent.putExtra("fromUserId", AppConstant.TRILL_INSTANT_ID);
        intent.putExtra("messageId", packetId);
        mContext.startActivity(intent);
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
            holder.mp_video.onShareListener(TriListActivity.this);
            holder.mp_video.setPosiont(position);

            Log.e("xuan", "onHolder: " + position + " ,,  " + TriListActivity.this.position);
            if (position == TriListActivity.this.position) {
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
        }
    }
}
