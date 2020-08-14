package com.sk.weichat.ui.trill;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.danikula.videocache.HttpProxyCacheServer;
import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.User;
import com.sk.weichat.bean.circle.PublicMessage;
import com.sk.weichat.bean.event.EventPraiseUpdate;
import com.sk.weichat.fragment.TrillFragment;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.other.BasicInfoActivity;
import com.sk.weichat.ui.xrce.Xpreprogressbar;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.likeView.LikeAnimationView;
import com.sk.weichat.view.likeView.LikeRelativeLayout;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.greenrobot.event.EventBus;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoViewbyXuan;
import fm.jiecao.jcvideoplayer_lib.OnJcvdListener;
import okhttp3.Call;

public class JcvTrillVideo extends FrameLayout implements View.OnClickListener {
    TriListActivity mShareListener;
    int position;
    private Context mContext;
    private boolean isPause;
    private boolean isPraise;
    private int mCommCount;
    private int mLikeCount;
    private long mVideoSize;
    private String mToken;
    private User mUser;
    private String videoUserid;
    private String mMessageid;
    private String sVideoUrl;
    private TextView titleTextView;
    private TextView tvlikesCount;
    private TextView tvCommCount;
    private TextView tvShare;
    private TextView tvPlay;
    private TextView tvBgName;
    private TextView tvName;
    private ImageView ivAvatar;
    private ImageView ivDisc;
    private ImageView startBtn;
    private ImageView thumbImageView;
    public Runnable HideThumbTask = new Runnable() {

        @Override
        public void run() {
            thumbImageView.setVisibility(GONE);
        }
    };
    private RelativeLayout rlDisc;
    private JCVideoViewbyXuan mVideoView;
    private Xpreprogressbar progressBar;
    private LikeRelativeLayout mLikeRelativeLayout;
    private LikeAnimationView btnLikes;
    private long mLastDoubleTapClickTime;
    private GestureDetector mGestureDetector;
    private Animation rotateAnim;
    private TrillCommDialog mCommDialog;
    private ProgressBar willPro;
    private PublicMessage data;

    private OnJcvdListener mVideoListener = new OnJcvdListener() {
        @Override
        public void onPrepared() {
            isPause = false;
            // thumbImageView.setVisibility(GONE);
            thumbImageView.postDelayed(HideThumbTask, 300);

            startBtn.setVisibility(GONE);
            rlDisc.startAnimation(rotateAnim);

            if (mVideoView.mCurrState == JCVideoPlayer.CURRENT_STATE_PAUSE) {
                progressBar.play(mVideoView.getDuration());
            } else {
                progressBar.play(0, mVideoView.getDuration());

            }
            willPro.setVisibility(GONE);
            Log.e("xuan", "onPrepared: " + mVideoView.mCurrState);
        }

        @Override
        public void onCompletion() {
            progressBar.clear();
            willPro.setVisibility(GONE);
        }

        @Override
        public void onError() {
            willPro.setVisibility(GONE);
            startBtn.setVisibility(VISIBLE);
            startBtn.setImageResource(R.drawable.jc_click_error_selector);
        }

        @Override
        public void onPause() {
            isPause = true;
            startBtn.setVisibility(VISIBLE);
            willPro.setVisibility(GONE);
            startBtn.setImageResource(R.drawable.ic_play_inco);
            progressBar.cancelProgressTimer();
            rlDisc.clearAnimation();
        }

        @Override
        public void onReset() {
            thumbImageView.setVisibility(VISIBLE);
            startBtn.setVisibility(GONE);
            willPro.setVisibility(VISIBLE);
            rlDisc.clearAnimation();
            progressBar.clear();
            progressBar.cancelProgressTimer();
        }
    };

    public JcvTrillVideo(Context context) {
        this(context, null);
    }

    public JcvTrillVideo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JcvTrillVideo(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        View.inflate(context, R.layout.layout_jcv_trill, this);
        mContext = context;

        mLikeRelativeLayout = findViewById(R.id.like_relativeLayout);
        mVideoView = findViewById(R.id.xuan_video);
        thumbImageView = findViewById(R.id.thumb);
        startBtn = findViewById(R.id.start);
        willPro = findViewById(R.id.progress);

        tvName = findViewById(R.id.tv_name); // 用户名称
        titleTextView = findViewById(R.id.tv_title);
        tvBgName = findViewById(R.id.tv_bgname); // 背景音乐名称
        progressBar = findViewById(R.id.bottom_progress);

        ivAvatar = findViewById(R.id.iv_avatar);
        btnLikes = findViewById(R.id.iv_likes);
        tvlikesCount = findViewById(R.id.tv_likes);// 点赞数
        tvCommCount = findViewById(R.id.tv_comm);  // 评论数
        tvShare = findViewById(R.id.tv_share);  // 转发数
        tvPlay = findViewById(R.id.tv_play);  // 播放数
        ivDisc = findViewById(R.id.iv_disc); // 光盘图标
        rlDisc = findViewById(R.id.rl_disc); // 大光盘

        ivAvatar.setOnClickListener(this);
        btnLikes.setOnClickListener(this);// 点赞按钮
        findViewById(R.id.iv_follow).setOnClickListener(this);// 关注
        findViewById(R.id.iv_comm).setOnClickListener(this);  // 评论按钮
        findViewById(R.id.iv_share).setOnClickListener(this); // 分享按钮

        mVideoView.addOnJcvdListener(mVideoListener);
        initGestureAndAnimation(context);
    }

    private void initGestureAndAnimation(Context context) {
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (System.currentTimeMillis() - mLastDoubleTapClickTime <= 600) {
                    /*
                    之前有一个bug，即当用户在双击点赞时，如点击屏幕的次数为单数，最后都会触发onSingleTapConfirmed事件
                    现加一个时间判断，如当前时间减去最后一次双击时间小于600ms，认定当前用户正在进行双击操作 做弹爱心处理
                     */
                    mLikeRelativeLayout.start(e);
                    return true;
                }
                if (isPause) {
                    mVideoView.play("");
                } else {
                    mVideoView.pause();
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // 记录触发双击的时间
                mLastDoubleTapClickTime = System.currentTimeMillis();
                if (!isPraise) {// 非like状态，需要调用赞接口
                    praiseOrCancel(mMessageid);
                }
                mLikeRelativeLayout.start(e);
                return true;
            }
        });

        rotateAnim = AnimationUtils.loadAnimation(context, R.anim.rotate_anim_disc);
        LinearInterpolator lin = new LinearInterpolator();
        rotateAnim.setInterpolator(lin);
        rotateAnim.setFillAfter(true);
    }

    public int getCurrState() {
        return mVideoView.mCurrState;
    }

    public void startVideo() {
        // 增加缓存
        HttpProxyCacheServer proxy = MyApplication.getProxy(mContext);
        mVideoView.play(proxy.getProxyUrl(sVideoUrl));
    }

    private String formatCount(int count) {
        if (count < 10000) {
            return String.valueOf(count);
        }
        return String.format(Locale.getDefault(), "%.1fw", count / 1000 / 10.0);
    }

    public void updateDatas(PublicMessage bean, User loginUser, String commUrl, String token) {
        data = bean;

        mUser = loginUser;
        mToken = token;
        mMessageid = bean.getMessageId();

        sVideoUrl = bean.getFirstVideo();
        String iUrl = bean.getFirstImageOriginal();
        String text = bean.getBody().getText();

        ImageLoadHelper.showImage(
                mContext, iUrl, thumbImageView
        );

        videoUserid = bean.getUserId();
        AvatarHelper.getInstance().displayAvatar(text, bean.getUserId(), ivAvatar, false);
        AvatarHelper.getInstance().displayAvatar(bean.getUserId(), ivDisc, false);

        mVideoSize = bean.getFirstVideoSize();
        mCommCount = bean.getComments().size();
        mLikeCount = bean.getPraiseCount();
        tvCommCount.setText(formatCount(mCommCount));
        tvlikesCount.setText(formatCount(mLikeCount));
        tvShare.setText(formatCount(bean.getForward()));
        tvPlay.setText(formatCount(bean.getPlay()));

        mCommDialog = TrillCommDialog.getInstance();// 创建评论窗口
        mCommDialog.setOnUpdateCommListener(mContext, bean.getComments(), token, loginUser, commUrl, bean.getMessageId(), new TrillCommDialog.OnUpdateCommListener() {
            @Override
            public void updateCommCount() {
                mCommCount++;
                tvCommCount.setText(String.valueOf(mCommCount));
            }
        });

        isPraise = bean.getIsPraise() == 1;
        if (isPraise) {
            btnLikes.setLikeStatus(1);
        } else {
            btnLikes.setLikeStatus(0);
        }

        if (TextUtils.isEmpty(bean.getBody().getText())) {
            titleTextView.setVisibility(GONE);
        } else {
            titleTextView.setVisibility(VISIBLE);
            titleTextView.setText(bean.getBody().getText());
        }

        tvName.setText("@" + String.valueOf(bean.getNickName()));
        String str = "@" + bean.getNickName() + MyApplication.getContext().getString(R.string.original_music);
        tvBgName.setText(str + "             " + str + "               " + str);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_avatar:// 点击头像
                if (TrillFragment.OPEN_FRIEND) {
                    Intent intent = new Intent(mContext, BasicInfoActivity.class);
                    intent.putExtra(AppConstant.EXTRA_USER_ID, videoUserid);
                    mContext.startActivity(intent);
                } else {
                    ToastUtil.showToast(mContext, R.string.tip_trill_friend_disable);
                }
                break;
            case R.id.iv_follow:// 关注
                break;
            case R.id.iv_likes:
                // 点赞按钮
                praiseOrCancel(mMessageid);
                break;
            case R.id.iv_comm:
                // 评论按钮
                if (mContext instanceof BaseActivity) {
                    if (!mCommDialog.isAdded()) {
                        mCommDialog.show(((BaseActivity) mContext).getSupportFragmentManager(), "TilTok");
                    }
                }
                break;
            case R.id.iv_share:
                // 分享按钮
                shareMessage();
                break;
            case R.id.xuan_video:// 暂停视频
                mVideoView.pause();
                break;
        }

    }

    private void praiseOrCancel(String messageId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", mToken);
        params.put("messageId", messageId);
        String requestUrl;
        if (isPraise) {
            requestUrl = CoreManager.requireConfig(MyApplication.getInstance()).MSG_PRAISE_DELETE;
        } else {
            requestUrl = CoreManager.requireConfig(MyApplication.getInstance()).MSG_PRAISE_ADD;
        }
        HttpUtils.get().url(requestUrl)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            if (isPraise) {
                                btnLikes.cancel();
                            } else {
                                btnLikes.start();
                            }
                            isPraise = !isPraise;
                            if (isPraise) {
                                mLikeCount++;
                            } else {
                                mLikeCount--;
                            }
                            EventBus.getDefault().post(new EventPraiseUpdate(messageId, isPraise));
                            tvlikesCount.setText(String.valueOf(mLikeCount));

                            // 列表的数据也需要同步，否则返回列表在进来状态有显示不对
                            // data.setIsPraise(isPraise ? 1 : 0);
                            // data.setPraise(mLikeCount);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void shareMessage() {
        if (mShareListener != null) {
            mShareListener.onShare(mMessageid, sVideoUrl, mVideoSize, position);
        }
    }

    public void onShareListener(TriListActivity triListActivity) {
        mShareListener = triListActivity;
    }

    public void setPosiont(int position) {
        this.position = position;
    }
}
