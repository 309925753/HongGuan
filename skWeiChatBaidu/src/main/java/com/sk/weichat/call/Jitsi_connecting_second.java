package com.sk.weichat.call;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.facebook.react.modules.core.PermissionListener;
import com.redchamber.bean.AccountIndexBean;
import com.redchamber.bean.BannerImageBean;
import com.redchamber.bean.GiftListBean;
import com.redchamber.bean.ReceiveGiftBean;
import com.redchamber.event.UpdateGiftEvent;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.util.GlideUtils;
import com.redchamber.view.CommCodeDialog;
import com.redchamber.wallet.WalletActivity;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.VideoFile;
import com.sk.weichat.bean.event.EventNotifyByTag;
import com.sk.weichat.bean.event.MessageEventBG;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.VideoFileDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.CutoutHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.AppUtils;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.HttpUtil;
import com.sk.weichat.util.PermissionUtil;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.SelectionFrame;
import com.sk.weichat.view.TipDialog;
import com.sk.weichat.view.cjt2325.cameralibrary.util.LogUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.jitsi.meet.sdk.JitsiMeetActivityDelegate;
import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewListener;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import io.jsonwebtoken.Jwts;
import okhttp3.Call;

/**
 * 2018-2-27 录屏，保存至本地视频
 */
public class Jitsi_connecting_second extends BaseActivity implements JitsiMeetActivityInterface {
    private static final String TAG = "Jitsi_connecting_second";
    // 屏幕录制
    private static final int RECORD_REQUEST_CODE = 0x01;
    private static final int SCREEN_RECORD_REQUEST_CODE = 123;
    // 计时，给悬浮窗调用
    public static String time = null;
    boolean showing;
    private String mLocalHostJitsi = "https://meet.jit.si/";// 官网地址
    private String mLocalHost/* = "https://meet.youjob.co/"*/;  // 本地地址,现改为变量
    // 通话类型(单人语音、单人视频、群组语音、群组视频)
    private int mCallType=1;
    // 房间名，单聊发起人userId，群聊群组jid,
    private String fromUserId;
    private String  sendGiftUserId;
    // 收消息的对象，单聊是对方userId, 群聊是群组jid,
    private String toUserId;
    // 对方昵称，或者房间名，用于后台通知显示，
    private String showName;
    private boolean answer;
    private long startTime = System.currentTimeMillis();// 通话开始时间
    private long stopTime; // 通话结束时间
    private FrameLayout mFrameLayout;
    private JitsiMeetView mJitsiMeetView;
    private ImageView ivChange;
    private ImageView ivShowGit;
    private ImageView ivShowRciveGift;
    // 悬浮窗按钮
    private ImageView mFloatingView;
    // 录屏
    private LinearLayout mRecordLL;
    private ImageView mRecordIv;
    private TextView mRecordTv;
    // 标记当前手机版本是否为android 5.0,且为对方挂断
    private boolean isApi21HangUp;
    // private MediaProjection mediaProjection;
    private RecordService recordService;
    private boolean isOppositeHangUp;// 对面挂断
    private List<GiftListBean> giftListBeans=new ArrayList<>();
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            recordService = binder.getRecordService();
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("mm:ss");
    CountDownTimer mCountDownTimer = new CountDownTimer(18000000, 1000) {// 开始计时，用于显示在悬浮窗上，且每隔一秒发送一个广播更新悬浮窗
        @Override
        public void onTick(long millisUntilFinished) {
            time = formatTime();
            Jitsi_connecting_second.this.sendBroadcast(new Intent(CallConstants.REFRESH_FLOATING));
        }

        @Override
        public void onFinish() {// 12小时进入Finish

        }
    };
    private boolean isOldVersion = true;// 是否为老版本，如果一次 "通话中" 消息都没有收到，就判断对方使用的为老版本，自己也停止ping且不做检测
    private boolean isEndCallOpposite;// 对方是否结束了通话
    private int mPingReceiveFailCount;// 未收到对方发送 "通话中" 消息的次数
    // 每隔3秒给对方发送一条 "通话中" 消息
    CountDownTimer mCallingCountDownTimer = new CountDownTimer(3000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {// 计时结束
            if (isFinishing()) {
                // 避免activity结束后还在回调这个ping,
                return;
            }
            if (!HttpUtil.isGprsOrWifiConnected(Jitsi_connecting_second.this)) {
                TipDialog tipDialog = new TipDialog(Jitsi_connecting_second.this);
                tipDialog.setmConfirmOnClickListener(getString(R.string.check_network), () -> {
                    leaveJitsi();
                });

                tipDialog.show();
                return;
            }
            if (CallConstants.isSingleChat(mCallType)) {// 单人音视频通话
                if (isEndCallOpposite) {// 未收到对方发送的 "通话中" 消息
                    // 考虑到弱网情况，当Count等于3时才真正认为对方已经结束了通话，否则继续发送 "通话中" 消息且count+1
                    int maxCount = 10;
                    if (mPingReceiveFailCount == maxCount) {
                        if (isOldVersion) {
                            return;
                        }
                        Log.e(TAG, "true-->" + TimeUtils.sk_time_current_time());
                        if (!isDestroyed()) {
                            stopTime = System.currentTimeMillis();
                            overCall((int) (stopTime - startTime) / 1000);
                            Toast.makeText(Jitsi_connecting_second.this, getString(R.string.tip_opposite_offline_auto__end_call), Toast.LENGTH_SHORT).show();
                            leaveJitsi();
/*
                            TipDialog tipDialog = new TipDialog(Jitsi_connecting_second.this);
                            tipDialog.setmConfirmOnClickListener(getString(R.string.tip_opposite_offline_end_call), () -> {
                                stopTime = System.currentTimeMillis();
                                overCall((int) (stopTime - startTime) / 1000);
                                leaveJitsi();
                            });
                            tipDialog.show();
*/
                        }
                    } else {
                        mPingReceiveFailCount++;
                        Log.e(TAG, "true-->" + mPingReceiveFailCount + "，" + TimeUtils.sk_time_current_time());
                        sendCallingMessage();
                    }
                } else {
                    Log.e(TAG, "false-->" + TimeUtils.sk_time_current_time());
                    sendCallingMessage();
                }
            }
        }
    };

    public static void start(Context ctx, String fromuserid, String touserid, int type) {
        start(ctx, fromuserid, touserid, type, null);
    }

    public static void start(Context ctx, String fromuserid, String touserid, int type, @Nullable String meetUrl) {
        start(ctx, fromuserid, touserid, type, meetUrl, false);
    }

    public static void start(Context ctx, String fromuserid, String touserid, int type, @Nullable String meetUrl, boolean answer) {
        if (type == CallConstants.Talk_Meet) {
            Intent intent = new Intent(ctx, JitsiTalk.class);
            intent.putExtra("type", type);
            intent.putExtra("fromuserid", fromuserid);
            intent.putExtra("touserid", touserid);
            if (!TextUtils.isEmpty(meetUrl)) {
                intent.putExtra("meetUrl", meetUrl);
            }
            ctx.startActivity(intent);
            return;
        }
        Intent intent = new Intent(ctx, Jitsi_connecting_second.class);
        intent.putExtra("answer", answer);
        intent.putExtra("type", type);
        intent.putExtra("fromuserid", fromuserid);
        intent.putExtra("touserid", touserid);
        if (!TextUtils.isEmpty(meetUrl)) {
            intent.putExtra("meetUrl", meetUrl);
        }
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CutoutHelper.setWindowOut(getWindow());
        super.onCreate(savedInstanceState);
        // 自动解锁屏幕 | 锁屏也可显示 | Activity启动时点亮屏幕 | 保持屏幕常亮
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.jitsiconnecting);
        initData();
        if (isFinishing()) {
            // 初始化数据出问题，不再页面已经结束就不再继续初始化，否则可能崩溃，
            return;
        }
        initView();
        initEvent();
        EventBus.getDefault().register(this);
        setSwipeBackEnable(false);
    }

    private void requestScreenPermission() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent intent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, SCREEN_RECORD_REQUEST_CODE);
    }

    @Override
    public void onCoreReady() {
        super.onCoreReady();
        sendCallingMessage();// 对方可能一进入就已经挂掉了，我们就会误判对方未老版本，所以一进入就发送一条 "通话中" 消息给对方
    }

    private void initData() {
      //  mCallType = getIntent().getIntExtra("type", 0);
        fromUserId = getIntent().getStringExtra("fromuserid");
        toUserId = getIntent().getStringExtra("touserid");
        sendGiftUserId=toUserId;
        answer = getIntent().getBooleanExtra("answer", false);
        try {
            // 测试表明，单聊时toUserId是对方userId, 群聊fromUserId是群组userId,
            showName = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(),
                    CallConstants.isSingleChat(mCallType) ?
                            toUserId : fromUserId).getShowName();
        } catch (Exception e) {
            // showName不必须，
            e.printStackTrace();
        }

        // 屏幕共享只有发起方共享屏幕，接收方还是视频，
        if (CallConstants.isScreenMode(mCallType)) {
            ScreenModeHelper.startScreenMode(this::requestScreenPermission);
        } else {
            ScreenModeHelper.stopScreenMode();
        }
        JitsistateMachine.isInCalling = true;
        if (CallConstants.isSingleChat(mCallType)) {
            JitsistateMachine.callingOpposite = toUserId;
        } else {
            // 会议的话，改成记住群id,以免发起会议的人发起单聊时被挤下，
            JitsistateMachine.callingOpposite = fromUserId;
        }

        if (CallConstants.isSingleChat(mCallType)) {// 集群
            mLocalHost = getIntent().getStringExtra("meetUrl");
            if (TextUtils.isEmpty(mLocalHost)) {
                mLocalHost = coreManager.getConfig().JitsiServer;
            }
        } else {
            mLocalHost = coreManager.getConfig().JitsiServer;
        }

        if (TextUtils.isEmpty(mLocalHost)) {
            DialogHelper.tip(mContext, getString(R.string.tip_meet_server_empty));
            finish();
        }

        // mCallingCountDownTimer.start();
    }

    private void leaveJitsi() {
        Log.e(TAG, "leaveJitsi() called ");
        finish();
    }

    /**
     * startWithAudioMuted:是否禁用语音
     * startWithVideoMuted:是否禁用录像
     */
    private void initView() {
        getgiftlist();
        getAccountIndex();
        CutoutHelper.initCutoutHolderTop(getWindow(), findViewById(R.id.vCutoutHolder));
         ivShowRciveGift=findViewById(R.id.iv_show_recive_gitf);
        if (mCallType == CallConstants.Audio || mCallType == CallConstants.Video) {
            ivChange = findViewById(R.id.ivChange);
            ivShowGit=findViewById(R.id.iv_show_git);
            if (CallConstants.isAudio(mCallType)) {
                ivChange.setImageResource(R.mipmap.call_change_to_video);
                ivShowGit.setImageResource(R.mipmap.chat_gift);
            }
         //   ivChange.setVisibility(View.VISIBLE);
            ivChange.setOnClickListener(v -> {
                toggleCallType();
                // 通知对方切换语音视频，
                sendToggleCallType();
            });
        }
        mFrameLayout = (FrameLayout) findViewById(R.id.jitsi_view);
        mJitsiMeetView = new JitsiMeetView(this);
        mFrameLayout.addView(mJitsiMeetView);

        mFloatingView = (ImageView) findViewById(R.id.open_floating);

        mRecordLL = (LinearLayout) findViewById(R.id.record_ll);
        mRecordIv = (ImageView) findViewById(R.id.record_iv);
        mRecordTv = (TextView) findViewById(R.id.record_tv);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 5.0以下录屏需要root，不考虑
            Intent intent = new Intent(this, RecordService.class);
            bindService(intent, connection, BIND_AUTO_CREATE);
            mRecordLL.setVisibility(View.VISIBLE);
        }
        // TODO 暂时关闭录屏功能
        mRecordLL.setVisibility(View.GONE);

        // 配置房间参数
        JitsiMeetConferenceOptions.Builder options = new JitsiMeetConferenceOptions.Builder()
                .setWelcomePageEnabled(false)
                .setFeatureFlag("pip.enabled", false);
        if (CallConstants.isAudio(mCallType)) {
            options.setVideoMuted(true);
        } else if (CallConstants.isScreenMode(mCallType) && answer) {
            options.setVideoMuted(true);
        }

        try {
            options.setServerURL(new URL(mLocalHost));
        } catch (MalformedURLException e) {
            throw new IllegalStateException("jitsi地址异常: " + mLocalHost);
        }
        if (mCallType == CallConstants.Audio_Meet) {// 群组语音添加标识，防止和群组视频进入同一房间地址
            options.setRoom("audio" + fromUserId);
        } else {
            options.setRoom(fromUserId);
        }
        loadJwt(options);
        // 开始加载

        mJitsiMeetView.join(options.build());
        JitsiMeetActivityDelegate.onHostResume(this);
        ivShowGit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGif(v);
            }
        });


    }
    private AccountIndexBean mAccountIndexBean;
    private void getAccountIndex() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getInstance(this).getSelfStatus().accessToken);

        HttpUtils.post().url(coreManager.getInstance(this).getConfig().RED_PAY_ACCOUNT_INDEX)
                .params(params)
                .build()
                .execute(new BaseCallback<AccountIndexBean>(AccountIndexBean.class) {

                    @Override
                    public void onResponse(ObjectResult<AccountIndexBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            mAccountIndexBean = result.getData();
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtils.showToast(e.getMessage());
                    }
                });
    }



    private GiftListBean  mGift=new GiftListBean();
    /**
     * 弹出礼物
     * @param v
     */
    private void openGif(View v) {
       View contentView =this.getLayoutInflater().inflate(
                R.layout.dialog_chat_gift_comm, null);
        PopupWindow   popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView tvBalance=(TextView)contentView.findViewById(R.id.tv_balance);
        RecyclerView    rvGift=(RecyclerView)contentView.findViewById(R.id.rv_gift);
        Button btQuery=(Button) contentView.findViewById(R.id.btn_query);
        Button btGiving=(Button)contentView.findViewById(R.id.bt_giving);
        tvBalance.setText(String.valueOf(mAccountIndexBean.balance));

        btGiving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(mGift.getName())){
                    sendGift();
                }else {
                    ToastUtil.showToast(Jitsi_connecting_second.this,"请选择礼物");
                }


            }
        });
        btQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WalletActivity.startWalletActivity(Jitsi_connecting_second.this);
            }
        });

        GridLayoutManager linearLayoutManager = new GridLayoutManager(this, 4);
        linearLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        rvGift.setLayoutManager(linearLayoutManager);
        CheckLikesMeAdapter checkLikesMeAdapter = new CheckLikesMeAdapter(this);
        List<GiftListBean> giftList=new ArrayList<>();

        if(giftListBeans!=null && giftListBeans.size()>0){
            for(int i=0;i<giftListBeans.size();i++){
                GiftListBean giftListBean=new GiftListBean();
                giftListBean=giftListBeans.get(i);
                giftListBean.setSelect(false);
                giftList.add(giftListBean);
            }

            checkLikesMeAdapter.likeMeBeanList = giftList;
        }

        rvGift.setAdapter(checkLikesMeAdapter);
        checkLikesMeAdapter.notifyDataSetChanged();

        checkLikesMeAdapter.setBtnOnClice(new CheckLikesMeAdapter.BtnOnClick() {
            @Override
            public void btnOnClick(GiftListBean likeMeBean) {
                LogUtil.e("likeMeBean = " +likeMeBean.getGiftId());
                mGift=likeMeBean;
            }
        });


        popupWindow.setFocusable(true);// 取得焦点
        //注意  要是点击外部空白处弹框消息  那么必须给弹框设置一个背景色  不然是不起作用的
        //设置SelectPicPopupWindow弹出窗体的背景
        popupWindow.setBackgroundDrawable(this.getDrawable(R.drawable.dialog_style_bg));
        //点击外部消失
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
              /*  WindowManager.LayoutParams lp =getParent().getWindow().getAttributes();
                lp.alpha = 1f;
                getParent().getWindow().setAttributes(lp);*/
            }
        });

        //从底部显示
        popupWindow.setTouchable(true);
        popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        //RED_MY_USER_GIFT_LIST
    }

    private void sendGift(){
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("access_token", coreManager.getSelfStatus().accessToken);
           /* if(MyApplication.mMyHomepageBean.userId.equals(toUserId)){
                params.put("toUserId", fromUserId);
            }else {
                params.put("toUserId", toUserId);
            }*/
            params.put("toUserId", toUserId);
            params.put("giftId", mGift.getGiftId());
            params.put("count", "1");
            HttpUtils.post().url(coreManager.getConfig().RED_MY_SEND_GIFT)
                    .params(params)
                    .build()
                    .execute(new BaseCallback<String>(String.class) {

                        @Override
                        public void onResponse(ObjectResult<String> result) {

                            if (result.getResultCode() == 1) {
                                ToastUtil.showLongToast(Jitsi_connecting_second.this,"礼物成功送达");
                            } else {
                                ToastUtil.showLongToast(Jitsi_connecting_second.this,result.getResultMsg());
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            Toast.makeText(Jitsi_connecting_second.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                        }
                    });
    }
    /**
     * 得到礼物列表
     */
    private void getgiftlist() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageIndex",   "0");
        params.put("pageSize", 10 + "");
        HttpUtils.post().url(coreManager.getConfig().RED_MY_USER_GIFT_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<GiftListBean>(GiftListBean.class) {
                    @Override
                    public void onResponse(ArrayResult<GiftListBean> result) {
                        if (Result.checkSuccess(getApplicationContext(), result)) {
                            if(result.getData()!=null && result.getData().size()>0){
                                giftListBeans=result.getData();
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }


    public static class CheckLikesMeAdapter extends RecyclerView.Adapter<CheckLikesMeAdapter.ViewHolder> implements View.OnClickListener {
        private LayoutInflater mInflater;
        private Context context;
        private List<GiftListBean> likeMeBeanList = new ArrayList<GiftListBean>();
        private BtnOnClick btnOnClick;
        private int mOldPosition = -1;

        public CheckLikesMeAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            this.context = context;
        }

        @Override
        public void onClick(View v) {

        }

        public interface BtnOnClick {
            void btnOnClick(GiftListBean likeMeBean);
        }

        public void setBtnOnClice(BtnOnClick btnOnClick) {
            this.btnOnClick = btnOnClick;

        }


        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivlike;
            TextView tvLikeName;
            TextView tvLikePrice;
            LinearLayout llGift;

            public ViewHolder(View view) {
                super(view);
                ivlike = (ImageView) view.findViewById(R.id.iv_photo);
                tvLikeName = (TextView) view.findViewById(R.id.tv_name);
                tvLikePrice = (TextView) view.findViewById(R.id.tv_price);
                llGift=(LinearLayout)view.findViewById(R.id.ll_gift);

            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.dialog_chat_gift_item, null);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (likeMeBeanList != null && likeMeBeanList.size() > 0) {
                final GiftListBean likeMeBean = likeMeBeanList.get(position);
               // GlideUtils.loadBlurGift(context, likeMeBean.getPhoto(), holder.ivlike, 75, 75);
                Glide.with(context).load(likeMeBean.getPhoto()).into(holder.ivlike);
                holder.tvLikeName.setText(likeMeBean.getName()==null?"":likeMeBean.getName());
                holder.tvLikePrice.setText(likeMeBean.getPrice()==0?"":String.valueOf(likeMeBean.getPrice())+"红豆");
                holder.llGift.setBackgroundResource(likeMeBean.isSelect() ? R.drawable.red_shape_bg_item_recharge_center_checked : R.drawable.red_shape_bg_item_recharge_center);


                holder.llGift.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (likeMeBean.isSelect()) {
                            return;
                        }
                        if (mOldPosition >= 0) {
                            likeMeBeanList.get(mOldPosition).setSelect(false);
                        }
                        notifyItemChanged(mOldPosition);
                        mOldPosition = holder.getAdapterPosition();
                        likeMeBeanList.get(mOldPosition).setSelect(true);
                        notifyItemChanged(mOldPosition);

                        btnOnClick.btnOnClick(likeMeBean);
                    }
                });
            }

        }

        @Override
        public int getItemCount() {
            return likeMeBeanList.size();
        }
    }


    private void sendToggleCallType() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(XmppMessage.TYPE_CHANGE_VIDEO_ENABLE);
        // mCallType是切换后的类型，
        // content为1表示改成视频通话，为0表示改成语音通话，
        if (mCallType == CallConstants.Audio) {
            chatMessage.setContent(String.valueOf(0));
        } else if (mCallType == CallConstants.Video) {
            chatMessage.setContent(String.valueOf(1));
        } else {
            Reporter.unreachable();
            return;
        }

        chatMessage.setFromUserId(coreManager.getSelf().getUserId());
        chatMessage.setFromUserName(coreManager.getSelf().getNickName());
        chatMessage.setToUserId(toUserId);
        chatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        coreManager.sendChatMessage(toUserId, chatMessage);
    }

    private void toggleCallType() {
        toggleCallType(mCallType == CallConstants.Audio);
    }

    /**
     * @param videoEnable 改通话类型之后的视频启动情况，为true表示改成视频通话，为false表示改成语音通话，
     */
    private void toggleCallType(boolean videoEnable) {
        if (videoEnable) {
            mCallType = CallConstants.Video;
            mJitsiMeetView.setVideoEnable();
            ivChange.setImageResource(R.mipmap.call_change_to_voice);
            ivShowGit.setImageResource(R.mipmap.chat_gift);
        } else {
            mCallType = CallConstants.Audio;
            mJitsiMeetView.setVideoMuted();
            ivChange.setImageResource(R.mipmap.call_change_to_video);
            ivShowGit.setImageResource(R.mipmap.chat_gift);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadJwt(JitsiMeetConferenceOptions.Builder options) {
        try {
            Map<String, String> user = new HashMap<>();
            user.put("avatar", AvatarHelper.getAvatarUrl(coreManager.getSelf().getUserId(), false));
            user.put("name", coreManager.getSelf().getNickName());
            Map<String, Object> context = new HashMap<>();
            context.put("user", user);
            Map<String, Object> payload = new HashMap<>();
            payload.put("context", context);
            String jwt = Jwts.builder().addClaims(payload)
                    .compact();
            options.setToken(jwt);
        } catch (Exception e) {
            Log.e(TAG, "loadJwt: 加载用户信息失败", e);
        }
    }

    private void initEvent() {
        ImageView iv = findViewById(R.id.ysq_iv);
        Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), fromUserId);
        if (friend != null && friend.getRoomFlag() != 0) {
            iv.setVisibility(View.VISIBLE);
            // 群组会议，可邀请其他群成员
            iv.setOnClickListener(v -> {
                JitsiInviteActivity.start(this, mCallType, fromUserId);
            });
        }

        mJitsiMeetView.setListener(new JitsiMeetViewListener() {

            @Override
            public void onConferenceWillJoin(Map<String, Object> map) {
                Log.e("jitsi", "即将加入会议");
            }

            @Override
            public void onConferenceJoined(Map<String, Object> map) {
                Log.e(TAG, "已加入会议，显示悬浮窗按钮，开始计时");
                // 如果将runOnUiThread放在onConferenceWillJoin内，底部会闪现一条白边，偶尔白边还不会消失
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFloatingView.setVisibility(View.VISIBLE);
                    }
                });
                // 会议开始，记录开始时间
                startTime = System.currentTimeMillis();
                // 开始计时
                mCountDownTimer.start();
            }

            @Override
            public void onConferenceTerminated(Map<String, Object> map) {
                Log.e(TAG, "5");
                // 即将离开会议
                if (!isApi21HangUp) {
                    stopTime = System.currentTimeMillis();
                    overCall((int) (stopTime - startTime) / 1000);
                }

                Log.e(TAG, "6");
                Jitsi_connecting_second.this.sendBroadcast(new Intent(CallConstants.CLOSE_FLOATING));
                finish();
            }
        });

        mFloatingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppUtils.checkAlertWindowsPermission(Jitsi_connecting_second.this)) { // 已开启悬浮窗权限
                    // nonRoot = false→ 仅当activity为task根（即首个activity例如启动activity之类的）时才生效
                    // nonRoot = true → 忽略上面的限制
                    // 这个方法不会改变task中的activity中的顺序，效果基本等同于home键
                    moveTaskToBack(true);
                    // 启动悬浮窗移到了onStop，
                } else { // 未开启悬浮窗权限
                    SelectionFrame selectionFrame = new SelectionFrame(Jitsi_connecting_second.this);
                    selectionFrame.setSomething(null, getString(R.string.av_no_float), new SelectionFrame.OnSelectionFrameClickListener() {
                        @Override
                        public void cancelClick() {
                            hideBottomUIMenu();
                        }

                        @Override
                        public void confirmClick() {
                            PermissionUtil.startApplicationDetailsSettings(Jitsi_connecting_second.this, 0x01);
                            hideBottomUIMenu();
                        }
                    });
                    selectionFrame.show();
                }
            }
        });

        mRecordLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (recordService.isRunning()) {
                        if (recordService.stopRecord()) {
                            mRecordIv.setImageResource(R.drawable.recording);
                            mRecordTv.setText(getString(R.string.screen_record));
                            saveScreenRecordFile();// 将录制的视频保存至本地
                        }
                    } else {
                        // 申请屏幕录制
                        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
                        if (projectionManager != null) {
                            Intent captureIntent = projectionManager.createScreenCaptureIntent();
                            startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
                        }
                    }
                }
*/
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
/*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
                if (projectionManager != null) {
                    mediaProjection = projectionManager.getMediaProjection(resultCode, data);
                    recordService.setMediaProject(mediaProjection);
                    // 开始录制
                    recordService.startRecord();

                    mRecordIv.setImageResource(R.drawable.stoped);
                    mRecordTv.setText(getString(R.string.stop));
                }
            }
*/
        } else if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ScreenModeHelper.startScreenMode(data);
            } else {
                Toast.makeText(this, "拒绝录屏", Toast.LENGTH_SHORT).show();
            }
            return;
        } else {
            JitsiMeetActivityDelegate.onActivityResult(
                    this, requestCode, resultCode, data);
        }
    }

    public void sendCallingMessage() {
        isEndCallOpposite = true;

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(XmppMessage.TYPE_IN_CALLING);

        chatMessage.setFromUserId(coreManager.getSelf().getUserId());
        chatMessage.setFromUserName(coreManager.getSelf().getNickName());
        chatMessage.setToUserId(toUserId);
        chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        chatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        coreManager.sendChatMessage(toUserId, chatMessage);

        mCallingCountDownTimer.start();// 重新开始计时
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventNotifyByTag message) {
        if (message.tag.equals(EventNotifyByTag.Interrupt)) {
            sendBroadcast(new Intent(CallConstants.CLOSE_FLOATING));
            leaveJitsi();
        }
    }


    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageCallTypeChange message) {
        if (message.chatMessage.getType() == XmppMessage.TYPE_CHANGE_VIDEO_ENABLE) {
            if (message.chatMessage.getFromUserId().equals(toUserId)) {
                toggleCallType(TextUtils.equals(message.chatMessage.getContent(), "1"));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageCallingEvent message) {
        if (message.chatMessage.getType() == XmppMessage.TYPE_IN_CALLING) {
            if (message.chatMessage.getFromUserId().equals(toUserId)) {
                isOldVersion = false;
                // 收到 "通话中" 的消息，且该消息为当前通话对象发送过来的
                Log.e(TAG, "MessageCallingEvent-->" + TimeUtils.sk_time_current_time());
                mPingReceiveFailCount = 0;// 将count置为0
                isEndCallOpposite = false;
            }
        }
    }

    // 对方挂断
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageHangUpPhone message) {
        if (message.chatMessage.getFromUserId().equals(fromUserId)
                || message.chatMessage.getFromUserId().equals(toUserId)) {// 挂断方为当前通话对象 否则不处理
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                isApi21HangUp = true;
                TipDialog tip = new TipDialog(Jitsi_connecting_second.this);
                tip.setmConfirmOnClickListener(getString(R.string.av_hand_hang), new TipDialog.ConfirmOnClickListener() {
                    @Override
                    public void confirm() {
                        hideBottomUIMenu();
                    }
                });
                tip.show();
                return;
            }

            isOppositeHangUp = true;

            // 关闭悬浮窗
            sendBroadcast(new Intent(CallConstants.CLOSE_FLOATING));
            leaveJitsi();
        }
    }

    /*******************************************
     * Method
     ******************************************/
    // 发送挂断的XMPP消息
    private void overCall(int time) {
        if (isOppositeHangUp) {
            return;
        }
        if (mCallType == CallConstants.Audio) {
            EventBus.getDefault().post(new MessageEventCancelOrHangUp(XmppMessage.TYPE_END_CONNECT_VOICE, toUserId,
                    getString(R.string.sip_canceled) + getString(R.string.voice_call),
                    time));
        } else if (mCallType == CallConstants.Video) {
            EventBus.getDefault().post(new MessageEventCancelOrHangUp(XmppMessage.TYPE_END_CONNECT_VIDEO, toUserId,
                    getString(R.string.sip_canceled) + getString(R.string.video_call),
                    time));
        } else if (mCallType == CallConstants.Screen) {
            EventBus.getDefault().post(new MessageEventCancelOrHangUp(XmppMessage.TYPE_END_CONNECT_SCREEN, toUserId,
                    getString(R.string.sip_canceled) + getString(R.string.screen_call),
                    time));
        }
    }

    private String formatTime() {
        Date date = new Date(new Date().getTime() - startTime);
        return mSimpleDateFormat.format(date);
    }

    // 隐藏虚拟按键
    private void hideBottomUIMenu() {
        View v = this.getWindow().getDecorView();
        v.setSystemUiVisibility(View.GONE);
    }

    /*******************************************
     * 录屏，保存至本地视频
     ******************************************/
    public void saveScreenRecordFile() {
        // 录屏文件路径
        String imNewestScreenRecord = PreferenceUtils.getString(getApplicationContext(), "IMScreenRecord");
        File file = new File(imNewestScreenRecord);
        if (file.exists() && file.getName().trim().toLowerCase().endsWith(".mp4")) {
            VideoFile videoFile = new VideoFile();
            videoFile.setCreateTime(TimeUtils.f_long_2_str(getScreenRecordFileCreateTime(file.getName())));
            videoFile.setFileLength(getScreenRecordFileTimeLen(file.getPath()));
            videoFile.setFileSize(file.length());
            videoFile.setFilePath(file.getPath());
            videoFile.setOwnerId(coreManager.getSelf().getUserId());
            VideoFileDao.getInstance().addVideoFile(videoFile);
        }
    }

    private long getScreenRecordFileCreateTime(String srf) {
        int dot = srf.lastIndexOf('.');
        return Long.parseLong(srf.substring(0, dot));
    }

    private long getScreenRecordFileTimeLen(String srf) {
        long duration;
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(srf);
            player.prepare();
            duration = player.getDuration() / 1000;
        } catch (Exception e) {
            duration = 10;
            e.printStackTrace();
        }
        player.release();
        return duration;
    }

    /*******************************************
     * 生命周期
     ******************************************/
    @Override
    public void onBackPressed() {
        // 不允许按返回键离开房间，也不能finish，
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        JitsiMeetActivityDelegate.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (JitsistateMachine.isFloating) {
            sendBroadcast(new Intent(CallConstants.CLOSE_FLOATING));
        }
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(888);
        showing = true;
    }

    @Override
    protected void onStop() {
        if (!isFinishing()) {
            if (AppUtils.checkAlertWindowsPermission(Jitsi_connecting_second.this)) {
                // 开启悬浮窗
                Intent intent = new Intent(getApplicationContext(), JitsiFloatService.class);
                startService(intent);
            }
            notifyBackground();
        }
        showing = false;
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageEventBG mMessageEventBG) {
        if (mMessageEventBG.flag) {// 切换到前台
            AsyncUtils.postDelayed(this, c -> {
                if (!showing) {
                    // 避免被MainActivity清通知给清掉，
                    notifyBackground();
                }
            }, 200);
        }
    }
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(UpdateGiftEvent updateGiftEvent) {
        ReceiveGiftBean friend = JSON.parseObject(updateGiftEvent.MessageData, ReceiveGiftBean.class);
        LogUtil.e("****************************收到礼物消息**********************************************");
        LogUtil.e("****************************"+friend.getFromUserName()+"**********************************************");
        if(friend.getGift()!=null){
            ivShowRciveGift.setVisibility(View.VISIBLE);
            Glide.with(Jitsi_connecting_second.this).load(friend.getGift().getPhoto()).into(ivShowRciveGift);

         //   GlideUtils.loadBlurGift(Jitsi_connecting_second.this, friend.getGift().getPhoto(), ivShowRciveGift, 75, 75);
           // ToastUtil.showLongToast(Jitsi_connecting_second.this,"收到"+friend.getGift().getName());
            //ivShowRciveGift  photo
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    ivShowRciveGift.setVisibility(View.INVISIBLE);
                }
            }, 2000);// 设定指定的时间time,此处为2000毫秒
        }

    }
    private void notifyBackground() {
        Intent intent;
        intent = new Intent(mContext, Jitsi_connecting_second.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "meeting",
                    getString(R.string.meeting),
                    NotificationManager.IMPORTANCE_DEFAULT);
            // 关闭通知铃声，
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this, channel.getId());
        } else {
            //noinspection deprecation
            builder = new NotificationCompat.Builder(this);
        }
        builder.setContentTitle(showName) // 通知标题
                .setContentIntent(pendingIntent)
                .setContentText(getString(R.string.tip_meet_background_place_holder,
                        CallConstants.isAudio(mCallType) ?
                                getString(R.string.chat_audio) :
                                getString(R.string.chat_with_video)))  // 通知内容
                .setWhen(System.currentTimeMillis()) // 通知时间
                .setPriority(Notification.PRIORITY_HIGH) // 通知优先级
                .setAutoCancel(false)// 当用户单击面板就可以让通知自动取消
                .setOngoing(true)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setSmallIcon(R.mipmap.ic_logo); // 通知icon
        /**发起通知**/
        notificationManager.notify(888, builder.build());
    }

    @Override
    protected void onDestroy() {
        if (JitsistateMachine.isFloating) {
            sendBroadcast(new Intent(CallConstants.CLOSE_FLOATING));
        }
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(888);
        // 释放摄像头，
        JitsiMeetActivityDelegate.onHostPause(this);
        JitsistateMachine.reset();

        mCallingCountDownTimer.cancel();

        JitsiMeetActivityDelegate.onBackPressed();
        if (mJitsiMeetView != null) {
            mJitsiMeetView.dispose();
        }
        JitsiMeetActivityDelegate.onHostDestroy(this);

        EventBus.getDefault().unregister(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (connection != null) {
                // 1.用户开启录屏之后未结束录屏就直接结束通话了，此时需要释放部分资源，否则下次录屏会引发崩溃
                // 2.对方结束通话
                if (recordService != null && recordService.isRunning()) {
                    recordService.stopRecord();
                    saveScreenRecordFile();
                }
                try {
                    unbindService(connection);
                } catch (IllegalArgumentException e) {
                    // 可能没绑定，页面就结束了，
                }
            }
        }

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        Log.e(TAG, "onDestory");
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode,
            final String[] permissions,
            final int[] grantResults) {
        JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void requestPermissions(String[] permissions, int requestCode, PermissionListener listener) {
        JitsiMeetActivityDelegate.requestPermissions(this, permissions, requestCode, listener);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        Log.d(TAG, "onPointerCaptureChanged() called with: hasCapture = [" + hasCapture + "]");
    }
}
