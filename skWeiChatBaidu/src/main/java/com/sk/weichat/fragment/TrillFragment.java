package com.sk.weichat.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.makeramen.roundedimageview.RoundedImageView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.sk.weichat.R;
import com.sk.weichat.bean.circle.Praise;
import com.sk.weichat.bean.circle.PublicMessage;
import com.sk.weichat.bean.event.EventPraiseUpdate;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.ui.MainActivity;
import com.sk.weichat.ui.base.BaseRecAdapter;
import com.sk.weichat.ui.base.BaseRecViewHolder;
import com.sk.weichat.ui.base.EasyFragment;
import com.sk.weichat.ui.trill.TriListActivity;
import com.sk.weichat.ui.xrce.RecordxActivity;
import com.sk.weichat.util.CalculationUtil;
import com.sk.weichat.util.EventBusHelper;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import fm.jiecao.jcvideoplayer_lib.VideotillManager;
import okhttp3.Call;

public class TrillFragment extends EasyFragment {
    public static final boolean OPEN_COMM = true;   // 不可点击发布评论按钮
    public static final boolean OPEN_FRIEND = true; // 是否开启短视频加好友功能，使头像不可点击
    public static List<PublicMessage> urlList;
    private SmartRefreshLayout mRefreshLayout;
    private SwipeRecyclerView mPager;
    private ListVideoAdapter videoAdapter;
    //    private PagerSnapHelper snapHelper;
    private StaggeredGridLayoutManager layoutManager;
    private int[] lastPositions;
    private boolean isLoad;
    private int pagerIndex;
    private int position = -1;
    private RelativeLayout rlTitle;
    private TextView tvTitle;
    private int mLable = 0;
    public View.OnClickListener tabListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int curr = -1;
            switch (v.getId()) {
                case R.id.ll_tab1:
                    curr = 1;
                    tvTitle.setText(R.string.video_food);
                    break;
                case R.id.ll_tab2:
                    curr = 2;
                    tvTitle.setText(R.string.video_scenery);
                    break;
                case R.id.ll_tab3:
                    curr = 3;
                    tvTitle.setText(R.string.video_culture);
                    break;
                case R.id.ll_tab4:
                    curr = 4;
                    tvTitle.setText(R.string.video_recreation);
                    break;
                case R.id.ll_tab5:
                    curr = 5;
                    tvTitle.setText(R.string.video_hotel);
                    break;
                case R.id.ll_tab6:
                    curr = 6;
                    tvTitle.setText(R.string.video_shopping);
                    break;
                case R.id.ll_tab7:
                    curr = 7;
                    tvTitle.setText(R.string.video_sport);
                    break;
                default:
                    curr = 0;
                    tvTitle.setText(R.string.video_all);
                    break;
            }

            if (mLable == curr) {
                return;
            }

            mLable = curr;
            loadData(true);
        }
    };
    private boolean showTitle = true;

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_trill;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        initData();
        initView();
        addListener();
        EventBusHelper.register(this);
    }

    private void initData() {
        TrillFragment.urlList = new ArrayList<>();
        pagerIndex = PreferenceUtils.getInt(requireContext(), "trill_index", 0);
        position = PreferenceUtils.getInt(requireContext(), "trill_position", 0);
        loadData(false);
    }

    private void initView() {
        if (requireActivity() instanceof MainActivity) {
            findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        } else {
            findViewById(R.id.iv_title_left).setOnClickListener(this);
        }
        findViewById(R.id.iv_title_add).setOnClickListener(this);

        mRefreshLayout = findViewById(R.id.refreshLayout);
        mRefreshLayout.setOnRefreshListener(refreshLayout -> {
            loadData(true);
        });
        mPager = findViewById(R.id.rv_pager);

        tvTitle = findViewById(R.id.tv_text);
        rlTitle = findViewById(R.id.rl_title);
        videoAdapter = new ListVideoAdapter(TrillFragment.urlList);
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mPager.setLayoutManager(layoutManager);

        mPager.setAdapter(videoAdapter);

        View headView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_trill_tag, mPager, false);
        mPager.addHeaderView(headView);

        headView.findViewById(R.id.ll_tab1).setOnClickListener(tabListener);
        headView.findViewById(R.id.ll_tab2).setOnClickListener(tabListener);
        headView.findViewById(R.id.ll_tab3).setOnClickListener(tabListener);
        headView.findViewById(R.id.ll_tab4).setOnClickListener(tabListener);
        headView.findViewById(R.id.ll_tab5).setOnClickListener(tabListener);
        headView.findViewById(R.id.ll_tab6).setOnClickListener(tabListener);
        headView.findViewById(R.id.ll_tab7).setOnClickListener(tabListener);
        headView.findViewById(R.id.ll_tab8).setOnClickListener(tabListener);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventPraiseUpdate message) {
        for (int i = 0; i < urlList.size(); i++) {
            PublicMessage publicMessage = urlList.get(i);
            if (TextUtils.equals(publicMessage.getMessageId(), message.messageId)) {
                publicMessage.setIsPraise(message.isParise ? 1 : 0);
                publicMessage.setPraise(publicMessage.getPraise() + (message.isParise ? 1 : -1));
                if (message.isParise) {
                    // 这个praise对象最好要让服务器返回，但目前并没有实际使用，就这些new一个让点赞数正常就好，
                    Praise praise = new Praise();
                    praise.setNickName(coreManager.getSelf().getNickName());
                    praise.setUserId(coreManager.getSelf().getUserId());
                    publicMessage.getPraises().add(praise);
                } else {
                    Iterator<Praise> iterator = publicMessage.getPraises().iterator();
                    while (iterator.hasNext()) {
                        Praise next = iterator.next();
                        if (TextUtils.equals(next.getUserId(), coreManager.getSelf().getUserId())) {
                            iterator.remove();
                        }
                    }
                }
                videoAdapter.notifyItemChanged(i);
            }
        }
    }

    private void addListener() {
        mPager.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                //当前RecyclerView显示出来的最后一个的item的position
                int lastPosition = -1;

                //当前状态为停止滑动状态SCROLL_STATE_IDLE时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    if (layoutManager instanceof GridLayoutManager) {
                        //通过LayoutManager找到当前显示的最后的item的position
                        lastPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                    } else if (layoutManager instanceof LinearLayoutManager) {
                        lastPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                        StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                        if (lastPositions == null) {
                            lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                        }
                        staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                        lastPosition = CalculationUtil.findMax(lastPositions);
                    }
                    // 时判断界面显示的最后item的position是否等于itemCount总数-1也就是最后一个item的position
                    // 如果相等则说明已经滑动到最后了
                    if (lastPosition == recyclerView.getLayoutManager().getItemCount() - 1 && !isLoad) {
                        pagerIndex++;
                        position = lastPosition;
                        loadData(false);
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                float distance = getScollYDistance();
                if (dy > 2 && distance > 35) {
                    startTranslateAnim(false);
                }

                if (dy < -4) {
                    startTranslateAnim(true);
                }
            }
        });
    }

    private void loadData(final boolean clear) {
        isLoad = true;
        if (clear) {
            pagerIndex = 0;
            TrillFragment.urlList.clear();
            videoAdapter.notifyDataSetChanged();
        }

        DialogHelper.showDefaulteMessageProgressDialog(requireContext());
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageIndex", Integer.toString(pagerIndex));
        params.put("pageSize", "20");// 给一个尽量大的值
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
                        isLoad = false;
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(getActivity(), result)) {
                            List<PublicMessage> data = result.getData();
                            if (data != null && data.size() > 0) {
                                TrillFragment.urlList.addAll(data);
                                videoAdapter.notifyDataSetChanged();
                            }
                        }
                        mRefreshLayout.finishRefresh();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        isLoad = false;
                        DialogHelper.dismissProgressDialog();
                        mRefreshLayout.finishRefresh();
                        ToastUtil.showErrorNet(requireContext());
                    }
                });
    }

    public int getScollYDistance() {
        StaggeredGridLayoutManager mlayoutManager = layoutManager;
        int[] position = mlayoutManager.findFirstVisibleItemPositions(null);
        View firstVisiableChildView = mlayoutManager.findViewByPosition(position[0]);
        int itemHeight = firstVisiableChildView.getHeight();
        return (position[0]) * itemHeight - firstVisiableChildView.getTop();
    }

    public void startTranslateAnim(boolean show) {
        if (showTitle == show) {
            return;
        }
        showTitle = show;
        float fromy = -300;
        float toy = 0;

        if (!show) {
            fromy = 0;
            toy = -300;
        }

        TranslateAnimation animation = new TranslateAnimation(0, 0, fromy, toy);
        animation.setDuration(500);
        animation.setFillAfter(true);
        rlTitle.startAnimation(animation);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        boolean foreground = isVisibleToUser;
        if (!foreground) {
            release();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        boolean foreground = !hidden;
        if (!foreground) {
            release();
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        release();
        super.onDestroy();
    }

    private void release() {
        PreferenceUtils.putInt(requireContext(), "trill_index", pagerIndex);
        PreferenceUtils.putInt(requireContext(), "trill_position", position % 10);

        VideotillManager.instance().releaseVideo();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_left:
                requireActivity().finish();
                break;
            case R.id.iv_title_add:
                Intent intent = new Intent(requireContext(), RecordxActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 跳转到视频预览界面
     */
    private void intentPreview(int position) {
        Intent intent = new Intent(requireContext(), TriListActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("pagerIndex", pagerIndex);
        intent.putExtra("tab_name", tvTitle.getText().toString().trim());
        intent.putExtra("tab_lable", mLable);
        startActivity(intent);

        Log.e("xuan", "onClick: " + position);
    }

    class ListVideoAdapter extends BaseRecAdapter<PublicMessage, VideoViewHolder> {
        public ListVideoAdapter(List<PublicMessage> list) {
            super(list);
        }

        @Override
        public void onHolder(VideoViewHolder holder, PublicMessage bean, int position) {
            String imageUrl = bean.getFirstImageOriginal();
            if (TextUtils.isEmpty(imageUrl)) {
                AvatarHelper.getInstance().asyncDisplayOnlineVideoThumb(bean.getFirstVideo(), holder.mIvImage);
            } else {
                ImageLoadHelper.showImageWithoutAnimate(requireContext(),
                        imageUrl,
                        R.drawable.default_gray,
                        R.drawable.default_gray,
                        holder.mIvImage
                );
            }
            AvatarHelper.getInstance().displayAvatar(bean.getNickName(), bean.getUserId(), holder.mIvInco, false);
            holder.mTvName.setText(bean.getNickName());

            String title = TextUtils.isEmpty(bean.getBody().getText()) ? "" : bean.getBody().getText();
            if (TextUtils.isEmpty(title)) {
                holder.mTvContent.setVisibility(View.GONE);
            } else {
                holder.mTvContent.setVisibility(View.VISIBLE);
                holder.mTvContent.setText(title);
            }

            holder.mTvCount.setText(String.valueOf(bean.getPraise()));
        }

        @Override
        public VideoViewHolder onCreateHolder() {
            return new VideoViewHolder(getViewByRes(R.layout.item_trill_tag));
        }
    }

    public class VideoViewHolder extends BaseRecViewHolder implements View.OnClickListener {
        public RoundedImageView mIvImage;
        public ImageView mIvInco;
        public TextView mTvCount;
        public TextView mTvName;
        public TextView mTvContent;

        public VideoViewHolder(View rootView) {
            super(rootView);

            mTvContent = rootView.findViewById(R.id.tv_content);
            mTvName = rootView.findViewById(R.id.tv_name);
            mTvCount = rootView.findViewById(R.id.tv_count);
            mIvInco = rootView.findViewById(R.id.iv_avatar);
            mIvImage = rootView.findViewById(R.id.iv_image);

            rootView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition() - 1; // 减1一个头布局
            intentPreview(position);
        }
    }

}
