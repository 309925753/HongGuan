package com.sk.weichat.ui.circle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.sk.weichat.AppConfig;
import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.adapter.PublicMessageRecyclerAdapter;
import com.sk.weichat.audio_x.VoicePlayer;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.User;
import com.sk.weichat.bean.circle.Comment;
import com.sk.weichat.bean.circle.PublicMessage;
import com.sk.weichat.db.dao.CircleMessageDao;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.downloader.Downloader;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.circle.range.NewZanActivity;
import com.sk.weichat.ui.circle.range.SendAudioActivity;
import com.sk.weichat.ui.circle.range.SendFileActivity;
import com.sk.weichat.ui.circle.range.SendShuoshuoActivity;
import com.sk.weichat.ui.circle.range.SendVideoActivity;
import com.sk.weichat.ui.circle.util.RefreshListImp;
import com.sk.weichat.ui.other.BasicInfoActivity;
import com.sk.weichat.util.StringUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.MergerStatus;
import com.sk.weichat.view.PMsgBottomView;
import com.sk.weichat.view.TrillCommentInputDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JVCideoPlayerStandardSecond;
import okhttp3.Call;

/**
 * 我的商务圈
 */
public class BusinessCircleActivity extends BaseActivity implements showCEView, RefreshListImp {
    private static final int REQUEST_CODE_SEND_MSG = 1;
    // 自定义的弹出框类
    SelectPicPopupWindow menuWindow;
    /**
     * 接口,调用外部类的方法,让应用不可见时停止播放声音
     */
    ListenerAudio listener;
    CommentReplyCache mCommentReplyCache = null;
    private int mType;
    /* mPageIndex仅用于商务圈情况下 */
    private int mPageIndex = 0;
    /* 封面视图 */
    private View mMyCoverView;   // 封面root view
    private ImageView mCoverImg; // 封面图片ImageView
    private ImageView mAvatarImg;// 用户头像
    private TextView tv_user_name;
    private PMsgBottomView mPMsgBottomView;
    private List<PublicMessage> mMessages = new ArrayList<>();
    private SmartRefreshLayout mRefreshLayout;
    private SwipeRecyclerView mPullToRefreshListView;
    private PublicMessageRecyclerAdapter mAdapter;
    private String mLoginUserId;       // 当前登陆用户的UserId
    private String mLoginNickName;// 当前登陆用户的昵称
    private boolean isdongtai;
    private String cricleid;
    private String pinglun;
    private String dianzan;
    /* 当前选择的是哪个用户的个人空间,仅用于查看个人空间的情况下 */
    private String mUserId;
    private String mNickName;
    private ImageView mIvTitleLeft;
    private TextView mTvTitle;
    private ImageView mIvTitleRight;
    private boolean showTitle = true;

    // 为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            menuWindow.dismiss();
            Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.btn_send_picture:// 发表图文，
                    intent.setClass(getApplicationContext(), SendShuoshuoActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.btn_send_voice:  // 发表语音
                    intent.setClass(getApplicationContext(), SendAudioActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.btn_send_video:  // 发表视频
                    intent.setClass(getApplicationContext(), SendVideoActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.btn_send_file:   // 发表文件
                    intent.setClass(getApplicationContext(), SendFileActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.new_comment:     // 最新评论
                    Intent intent2 = new Intent(getApplicationContext(), NewZanActivity.class);
                    intent2.putExtra("OpenALL", true);
                    startActivity(intent2);
                    break;
                default:
                    break;
            }
        }
    };
    private boolean more;
    private RelativeLayout rl_title;
    private MergerStatus mergerStatus;
    private View actionBar;
    private Friend mFriend;
    private User mUser;
    private String bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_circle);
        mLoginUserId = coreManager.getSelf().getUserId();
        mLoginNickName = coreManager.getSelf().getNickName();

        if (getIntent() != null) {
            mType = getIntent().getIntExtra(AppConstant.EXTRA_CIRCLE_TYPE, AppConstant.CIRCLE_TYPE_MY_BUSINESS);// 默认的为查看我的商务圈
            mUserId = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
            mNickName = getIntent().getStringExtra(AppConstant.EXTRA_NICK_NAME);
            mFriend = FriendDao.getInstance().getFriend(mLoginUserId, mUserId);

            pinglun = getIntent().getStringExtra("pinglun");
            dianzan = getIntent().getStringExtra("dianzan");
            isdongtai = getIntent().getBooleanExtra("isdongtai", false);
            cricleid = getIntent().getStringExtra("messageid");
        }

        if (!isMyBusiness()) {//如果查看的是个人空间的话，那么mUserId必须要有意义
            if (TextUtils.isEmpty(mUserId)) {// 没有带userId参数，那么默认看的就是自己的空间
                mUserId = mLoginUserId;
                mNickName = mLoginNickName;
            }
        }

       /* if (mUserId != null && mUserId.equals(mLoginUserId)) {
            String mLastMessage = PreferenceUtils.getString(this, "BUSINESS_CIRCLE_DATA");
            if (!TextUtils.isEmpty(mLastMessage)) {
                mMessages = JSON.parseArray(mLastMessage, PublicMessage.class);
            }
        }*/

        initActionBar();
        Downloader.getInstance().init(MyApplication.getInstance().mAppDir + File.separator + coreManager.getSelf().getUserId()
                + File.separator + Environment.DIRECTORY_MOVIES);// 初始化视频下载目录
        initView();
    }

    private boolean isMyBusiness() {
        return mType == AppConstant.CIRCLE_TYPE_MY_BUSINESS;
    }

    private boolean isMySpace() {
        return mLoginUserId.equals(mUserId);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(mNickName);
        mIvTitleRight = (ImageView) findViewById(R.id.iv_title_right);
        if (mUserId.equals(mLoginUserId)) {// 查看自己的空间才有发布按钮
            mIvTitleRight.setImageResource(R.mipmap.more_icon);
            mIvTitleRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuWindow = new SelectPicPopupWindow(BusinessCircleActivity.this, itemsOnClick);
                    // 在获取宽高之前需要先测量，否则得不到宽高
                    menuWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    // +x右,-x左,+y下,-y上
                    // pop向左偏移显示
                    menuWindow.showAsDropDown(v,
                            -(menuWindow.getContentView().getMeasuredWidth() - v.getWidth() / 2 - 40),
                            0);
                }
            });
        } else {
            findViewById(R.id.iv_title_add).setVisibility(View.GONE);
        }
    }

    private void initView() {
        mergerStatus = findViewById(R.id.mergerStatus);
        rl_title = findViewById(R.id.rl_title);
        mPullToRefreshListView = findViewById(R.id.recyclerView);
        mPullToRefreshListView.setLayoutManager(new LinearLayoutManager(this));
        initCoverView();
        mRefreshLayout = findViewById(R.id.refreshLayout);
        mPMsgBottomView = (PMsgBottomView) findViewById(R.id.bottom_view);
       /* mResizeLayout.setOnResizeListener(new ResizeLayout.OnResizeListener() {
            @Override
            public void OnResize(int w, int h, int oldw, int oldh) {
                if (oldh < h) {// 键盘被隐藏
                    mCommentReplyCache = null;
                    mPMsgBottomView.setHintText("");
                    mPMsgBottomView.reset();
                }
            }
        });*/

        mPMsgBottomView.setPMsgBottomListener(new PMsgBottomView.PMsgBottomListener() {
            @Override
            public void sendText(String text) {
                if (mCommentReplyCache != null) {
                    mCommentReplyCache.text = text;
                    addComment(mCommentReplyCache);
                    mPMsgBottomView.hide();
                }
            }
        });

        if (isdongtai) {
            // 如果是动态，不添加HeadView
            mPullToRefreshListView.addHeaderView(actionBar);
        } else {
            mPullToRefreshListView.addHeaderView(mMyCoverView);
        }

        mAdapter = new PublicMessageRecyclerAdapter(this, coreManager, mMessages);
        setListenerAudio(mAdapter);
        mPullToRefreshListView.setAdapter(mAdapter);

        if (isdongtai) {
            mRefreshLayout.setEnableRefresh(false);
            mRefreshLayout.setEnableLoadMore(false);
        }
        mRefreshLayout.setOnRefreshListener(refreshLayout -> {
            requestData(true);
        });
        mRefreshLayout.setOnLoadMoreListener(refreshLayout -> {
            requestData(false);
        });

        mPullToRefreshListView.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    int totalScroll;

                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int scrollState) {
                        if (mPMsgBottomView.getVisibility() != View.GONE) {
                            mPMsgBottomView.hide();
                        }
                    }

                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        totalScroll += dy;
                        if (totalScroll < 0) {
                            totalScroll = 0;
                        }
                        rl_title.setAlpha(totalScroll > 500 ? 0 : 1f - (Float.valueOf(totalScroll) / 500.0f));
                        mergerStatus.setAlpha(totalScroll > 500 ? 1f : Float.valueOf(totalScroll) / 500.0f);
                        if (dy > 2) {
                            startTranslateAnim(false);
                        }

                        //&& mMyCoverView.getTop() == 0
                        if (dy < -4 && mMyCoverView.getTop() == 0) {
                            startTranslateAnim(true);
                        }
                    }
                });

        requestData(true);
    }

    private void getUserInfo() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mUserId);

        HttpUtils.get().url(coreManager.getConfig().USER_GET_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<User>(User.class) {

                    @Override
                    public void onResponse(ObjectResult<User> result) {
                        if (Result.checkSuccess(BusinessCircleActivity.this, result)) {
                            mUser = result.getData();
                            bg = mUser.getMsgBackGroundUrl();
                            if (!TextUtils.isEmpty(bg)) {
                                ImageLoadHelper.loadImageDontAnimateWithPlaceholder(
                                        mContext,
                                        bg,
                                        R.drawable.avatar_normal,
                                        d -> {
                                            mCoverImg.setImageDrawable(d);
                                        }, e -> {
                                            AvatarHelper.getInstance().displayRoundAvatar(mLoginNickName, mUserId, mCoverImg, false);
                                        }
                                );
                            } else {
                                AvatarHelper.getInstance().displayRoundAvatar(mNickName, mUserId, mCoverImg, false);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    private void initCoverView() {
        actionBar = LayoutInflater.from(this).inflate(R.layout.a_view_actionbar, mPullToRefreshListView, false);
        mMyCoverView = LayoutInflater.from(this).inflate(R.layout.space_cover_view, mPullToRefreshListView, false);
        mMyCoverView.findViewById(R.id.ll_btn_send).setVisibility(View.GONE);
        mCoverImg = (ImageView) mMyCoverView.findViewById(R.id.cover_img);
        mAvatarImg = (ImageView) mMyCoverView.findViewById(R.id.avatar_img);
        tv_user_name = (TextView) mMyCoverView.findViewById(R.id.tv_user_name);

        // 头像
        if (isMyBusiness() || isMySpace()) {
            AvatarHelper.getInstance().displayAvatar(mLoginNickName, mLoginUserId, mAvatarImg, true);
            // 优先加载user信息中的背景图片，失败就加载头像，
            String bg = coreManager.getSelf().getMsgBackGroundUrl();

            tv_user_name.setText(mLoginNickName);
            if (!TextUtils.isEmpty(bg)) {
                ImageLoadHelper.loadImageDontAnimateWithPlaceholder(
                        this,
                        bg,
                        R.drawable.avatar_normal,
                        d -> {
                            mCoverImg.setImageDrawable(d);
                        }, e -> {
                            AvatarHelper.getInstance().displayRoundAvatar(mLoginNickName, mLoginUserId, mCoverImg, false);
                        }
                );
            } else {
                AvatarHelper.getInstance().displayRoundAvatar(mLoginNickName, mLoginUserId, mCoverImg, false);
            }
        } else {
            if (mFriend != null && !TextUtils.isEmpty(mFriend.getRemarkName())) {
                tv_user_name.setText(mFriend.getRemarkName());
            } else {
                tv_user_name.setText(mNickName);
            }
            AvatarHelper.getInstance().displayAvatar(mNickName, mUserId, mAvatarImg, true);
            getUserInfo(); //获取User，得到bg
        }
        mAvatarImg.setOnClickListener(v -> {// 进入个人资料页
            if (isMyBusiness() || isMySpace()) {
                BasicInfoActivity.start(mContext, mLoginUserId);
            } else {
                BasicInfoActivity.start(mContext, mUserId);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (mPMsgBottomView != null && mPMsgBottomView.getVisibility() == View.VISIBLE) {
            mPMsgBottomView.hide();
        } else {
            // 点返回键退出全屏视频，
            // 如果PublicMessageAdapter用在其他activity, 也要加上，
            if (JVCideoPlayerStandardSecond.backPress()) {
                JCMediaManager.instance().recoverMediaPlayer();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            listener.ideChange();
        }
        listener = null;
    }

    @Override
    public void finish() {
        VoicePlayer.instance().stop();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       /* if (mUserId.equals(mLoginUserId)) {
            if (mMessages != null && mMessages.size() > 0) {
                PreferenceUtils.putString(this, "BUSINESS_CIRCLE_DATA", JSON.toJSONString(mMessages));
            }
        }*/
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
    }

    public void setListenerAudio(ListenerAudio listener) {
        this.listener = listener;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SEND_MSG) {
            if (resultCode == Activity.RESULT_OK) {// 发说说成功
                String messageId = data.getStringExtra(AppConstant.EXTRA_MSG_ID);
                CircleMessageDao.getInstance().addMessage(mLoginUserId, messageId);
                requestData(true);
                removeNullTV();
            }
        }
    }

    /********** 公共消息的数据请求部分 *********/

    /**
     * 请求公共消息
     *
     * @param isPullDwonToRefersh 是下拉刷新，还是上拉加载
     */
    private void requestData(boolean isPullDwonToRefersh) {
        if (isMyBusiness()) {
            requestMyBusiness(isPullDwonToRefersh);
        } else {
            if (isdongtai) {
                if (isPullDwonToRefersh) {
                    more = true;
                }
                if (!more) {
                    // ToastUtil.showToast(getContext(), getString(R.string.tip_last_item));
                    mRefreshLayout.setNoMoreData(true);
                    refreshComplete();
                } else {
                    requestSpacedongtai(isPullDwonToRefersh);
                }
            } else {
                requestSpace(isPullDwonToRefersh);
            }
        }
    }

    /**
     * 停止刷新动画
     */
    private void refreshComplete() {
        mPullToRefreshListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.finishRefresh();
                mRefreshLayout.finishLoadMore();
            }
        }, 200);
    }

    private void requestMyBusiness(final boolean isPullDwonToRefersh) {
        if (isPullDwonToRefersh) {
            mPageIndex = 0;
        }
        List<String> msgIds = CircleMessageDao.getInstance().getCircleMessageIds(mLoginUserId, mPageIndex, AppConfig.PAGE_SIZE);

        if (msgIds == null || msgIds.size() <= 0) {
            refreshComplete();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("ids", JSON.toJSONString(msgIds));

        HttpUtils.get().url(coreManager.getConfig().MSG_GETS)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(com.xuan.xuanhttplibrary.okhttp.result.ArrayResult<PublicMessage> result) {
                        List<PublicMessage> data = result.getData();
                        if (isPullDwonToRefersh) {
                            mMessages.clear();
                        }
                        if (data != null && data.size() > 0) {// 没有更多数据
                            mPageIndex++;
                            mMessages.addAll(data);
                        }
                        mAdapter.notifyDataSetChanged();

                        refreshComplete();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getApplicationContext());
                        refreshComplete();
                    }
                });
    }

    private void requestSpace(final boolean isPullDwonToRefersh) {
        String messageId = null;
        if (!isPullDwonToRefersh && mMessages.size() > 0) {
            messageId = mMessages.get(mMessages.size() - 1).getMessageId();
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mUserId);
        params.put("flag", PublicMessage.FLAG_NORMAL + "");

        if (!TextUtils.isEmpty(messageId)) {
            if (isdongtai) {
                params.put("messageId", cricleid);
            } else {
                params.put("messageId", messageId);
            }
        }
        params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));

        HttpUtils.get().url(coreManager.getConfig().MSG_USER_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(ArrayResult<PublicMessage> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            List<PublicMessage> data = result.getData();
                            if (isPullDwonToRefersh) {
                                mMessages.clear();
                            }
                            if (data != null && data.size() > 0) {
                                mMessages.addAll(data);
                            }
                            more = !(data == null || data.size() < AppConfig.PAGE_SIZE);
                            mAdapter.notifyDataSetChanged();

                            if (more) {
                                mRefreshLayout.resetNoMoreData();
                            } else {
                                mRefreshLayout.setNoMoreData(true);
                            }
                            refreshComplete();
                            if (mAdapter.getItemCount() == 0)
                                addNullTV2LV();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getApplicationContext());
                        refreshComplete();
                    }
                });
    }

    // 最近评论&赞进入
    private void requestSpacedongtai(final boolean isPullDwonToRefersh) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", cricleid);

        HttpUtils.get().url(coreManager.getConfig().MSG_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(com.xuan.xuanhttplibrary.okhttp.result.ObjectResult<PublicMessage> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            PublicMessage datas = result.getData();
                            if (datas == null) {
                                // 查看某条特定消息的进来的情况，如果消息不存在，直接返回，
                                ToastUtil.showToast(mContext, R.string.message_not_found);
                                finish();
                                return;
                            }
                            List<PublicMessage> datass = new ArrayList<>();
                            datass.add(datas);
                            if (isPullDwonToRefersh) {
                                mMessages.clear();
                            }
                            mMessages.addAll(datass);
                            mAdapter.notifyDataSetChanged();

                            refreshComplete();
                            if (mAdapter.getItemCount() == 0)
                                addNullTV2LV();
                        } else if (result.getResultCode() == 101002) {
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getApplicationContext());
                        refreshComplete();
                    }
                });
    }

    public void showCommentEnterView(int messagePosition, String toUserId, String toNickname, String toShowName) {
        mCommentReplyCache = new CommentReplyCache();
        mCommentReplyCache.messagePosition = messagePosition;
        mCommentReplyCache.toUserId = toUserId;
        mCommentReplyCache.toNickname = toNickname;
        String hint;
        if (TextUtils.isEmpty(toUserId) || TextUtils.isEmpty(toNickname) || TextUtils.isEmpty(toShowName)) {
            // mPMsgBottomView.setHintText("");
            hint = getString(R.string.enter_pinlunt);
        } else {
            // mPMsgBottomView.setHintText(getString(R.string.replay_text, toShowName));
            hint = getString(R.string.replay_text, toShowName);
        }
        // mPMsgBottomView.show();
        TrillCommentInputDialog trillCommentInputDialog = new TrillCommentInputDialog(this, hint, str -> {
            if (mCommentReplyCache != null) {
                mCommentReplyCache.text = str;
                addComment(mCommentReplyCache);
            }
        });
        Window window = trillCommentInputDialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);// 软键盘弹起
            trillCommentInputDialog.show();
        }
    }

    private void addComment(CommentReplyCache cache) {
        Comment comment = new Comment();
        comment.setUserId(mLoginUserId);
        comment.setNickName(mLoginNickName);
        comment.setToUserId(cache.toUserId);
        comment.setToNickname(cache.toNickname);
        comment.setBody(cache.text);
        addComment(cache.messagePosition, comment);
    }

    private void addComment(final int position, final Comment comment) {
        final PublicMessage message = mMessages.get(position);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", message.getMessageId());
        if (!TextUtils.isEmpty(comment.getToUserId())) {
            params.put("toUserId", comment.getToUserId());
        }
        if (!TextUtils.isEmpty(comment.getToNickname())) {
            params.put("toNickname", comment.getToNickname());
        }
        params.put("body", comment.getBody());

        HttpUtils.post().url(coreManager.getConfig().MSG_COMMENT_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(com.xuan.xuanhttplibrary.okhttp.result.ObjectResult<String> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            List<Comment> comments = message.getComments();
                            if (comments == null) {
                                comments = new ArrayList<>();
                                message.setComments(comments);
                            }
                            comment.setCommentId(result.getData());
                            comments.add(0, comment);
                            message.getCount().setComment(message.getCount().getComment() + 1);
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getApplicationContext());
                    }
                });
    }

    @Override
    public void showView(int messagePosition, String toUserId, String toNickname, String toShowName) {
        showCommentEnterView(messagePosition, toUserId, toNickname, toShowName);
    }

    @Override
    public void refreshAfterOperation(PublicMessage message) {
        int size = mMessages.size();
        for (int i = 0; i < size; i++) {
            if (StringUtils.strEquals(mMessages.get(i).getMessageId(), message.getMessageId())) {
                mMessages.set(i, message);
                mAdapter.setData(mMessages);
            }
        }
    }

    public void addNullTV2LV() {
        TextView nullTextView = new TextView(this);
        nullTextView.setTag("NullTV");
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int paddingSize = getResources().getDimensionPixelSize(R.dimen.NormalPadding);
        nullTextView.setPadding(0, paddingSize, 0, paddingSize);
        nullTextView.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        nullTextView.setGravity(Gravity.CENTER);

        nullTextView.setLayoutParams(lp);
        nullTextView.setText(getString(R.string.no_data_now));
        mPullToRefreshListView.addFooterView(nullTextView);
        mRefreshLayout.setEnableRefresh(false);
    }

    public void removeNullTV() {
        mPullToRefreshListView.removeFooterView(mPullToRefreshListView.findViewWithTag("NullTV"));
        mRefreshLayout.setEnableRefresh(true);
    }

    public interface ListenerAudio {
        void ideChange();
    }

    class CommentReplyCache {
        int messagePosition;// 消息的Position
        String toUserId;
        String toNickname;
        String text;
    }
}
