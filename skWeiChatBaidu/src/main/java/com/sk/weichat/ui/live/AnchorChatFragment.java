package com.sk.weichat.ui.live;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.seu.magicfilter.utils.MagicFilterType;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.bean.redpacket.Balance;
import com.sk.weichat.broadcast.MucgroupUpdateUtil;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.EasyFragment;
import com.sk.weichat.ui.live.adapter.MemberAdapter;
import com.sk.weichat.ui.live.bean.Detail;
import com.sk.weichat.ui.live.bean.Gift;
import com.sk.weichat.ui.live.bean.GiftItem;
import com.sk.weichat.ui.live.bean.Member;
import com.sk.weichat.ui.live.view.FragmentDialog;
import com.sk.weichat.ui.live.view.FragmentEditDialog;
import com.sk.weichat.ui.live.view.FragmentPayDialog;
import com.sk.weichat.ui.live.view.GiftItemView;
import com.sk.weichat.ui.live.view.HorizontialListView;
import com.sk.weichat.ui.me.redpacket.WxPayBlance;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.BannedDialog;
import com.sk.weichat.view.ChatContentView;
import com.sk.weichat.view.ManagerPopupWindow;
import com.sk.weichat.view.PullDownListView;
import com.sk.weichat.xmpp.ListenerManager;
import com.sk.weichat.xmpp.ReceiptManager;
import com.sk.weichat.xmpp.listener.ChatMessageListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import net.ossrs.yasea.SrsPublisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;
import okhttp3.Call;
import tyrantgit.widget.HeartLayout;

import static android.content.Context.INPUT_METHOD_SERVICE;


/**
 * 主播Fragment
 * 关于弹幕:
 * DanmakuFlameMaster是Android上开源弹幕解析绘制引擎项目，也是Android上最好的开源弹幕引擎-烈焰弹幕。其架构清晰，简单易用，支持多种高效率绘制方式选择，支持多种自定义功能设置上。
 * Features使用多种方式(View/SurfaceView/TextureView)实现高效绘制
 * xml弹幕格式解析基础
 * 弹幕精确还原绘制
 * 支持mode7特殊弹幕
 * 多核机型优化，高效的预缓存机制支持多种显示效果选项实时切换
 * 实时弹幕显示支持
 * 换行弹幕支持/运动弹幕支持
 * 支持自定义字体
 * 支持多种弹幕参数设置
 * 支持多种方式的弹幕屏蔽
 * ToDo: Follow-up function
 * 继续精确/稳定绘帧周期
 * 增加OpenGL ES绘制方式
 * 改进缓存策略和效率
 */
@SuppressLint("ValidFragment")
public class AnchorChatFragment extends EasyFragment implements View.OnClickListener,
        View.OnLayoutChangeListener, ChatMessageListener, ChatContentView.MessageEventListener {
    /********************
     * ToDo 直播间接口操作
     ********************/
    final Context mContext = MyApplication.getContext();
    // 推流对象
    SrsPublisher mSrsPublisher;
    // 管理
    ManagerPopupWindow mManagerPopupWindow;
    private HorizontialListView listview;
    private MemberAdapter mAdapter;
    private GiftItemView giftView;
    // 成员/礼物
    private ArrayList<Member> members;
    private ArrayList<Gift> gift;
    private ArrayList<String> gifts;
    // 上
    private ImageView mAvatar;
    private TextView mNickName;
    private TextView mPeopleNum;
    private TextView mPayAttention;
    // 下
    private TextView mDanMuS;
    private EditText sendEditText;
    private TextView sendTextView;
    // 烈焰弹幕
    private DanmakuView danmakuView;
    private DanmakuContext danmakuContext;
    // 爱心
    private HeartLayout heartLayout;
    // 随机生成rgb
    private Random mRandom;
    // Operation layout
    private View topView, bottomView, mSkin, sendView;
    // 用来监听界面的变化，判断是否弹出软键盘
    private View rootView;
    private InputMethodManager inputManager;
    // 屏幕高度
    private int screenHeight = 0;
    // 软件盘弹起后所占高度阀值
    private int keyHeight = 0;
    // 是否发送弹幕
    private boolean flag = false;
    private String mLoginUserId;
    private String mLoginNickName;
    private String mAccessToken;
    /* chat room */
    private ChatContentView mChatContentView;
    // 存储聊天消息
    private List<ChatMessage> mChatMessages;
    // 直播间id&聊天室jid&名称
    private String mRoomid;
    private String mRoomJid;
    private String mRoomName;
    private String mRoomNotice;
    private String mRoomUser;
    // 是否是直播&群聊
    private boolean isGroupChat = true;
    private KeyBoardShowListener mKeyBoardShowListener;
    private int keyBoradCount = 0;
    private Handler mHandler = new Handler();
    // 解析器,用来解析弹幕内容
    private BaseDanmakuParser parser = new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };
    private int mMinId = 0;
    private int mPageSize = 20;
    private boolean mHasMoreData = true;
    private Member self;
    /**
     * 用于通知更新已读状态的广播
     */
    private BroadcastReceiver changeSomeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(LiveConstants.LIVE_MEMBER_ADD)) {
                // 成员加入
                Log.e("zq", "接收到成员加入广播");
                getLiveRoomMember();
            } else if (action.equals(LiveConstants.LIVE_MEMBER_DELETE)) {
                // 在该直播间内没有人可以对我进行管理,所以就不在这里做过多的操作
                // 成员退出
                Log.e("zq", "接收到成员退出广播");
                getLiveRoomMember();
            } else if (action.equals(LiveConstants.LIVE_DANMU_DRAWABLE)) {
                // 绘制弹幕
                Log.e("zq", "接收绘制弹幕的广播");
                Bundle bundle = intent.getExtras();
                String content = bundle.getString("danmu");
                String fromUserId = bundle.getString("fromUserId");
                String fromUserName = bundle.getString("fromUserName");
                if (!TextUtils.isEmpty(fromUserId)
                        && !fromUserId.equals(mLoginUserId)) {
                    addDanmaku(content, false, false);
                }
            } else if (action.equals(LiveConstants.LIVE_SEND_GIFT)) {
                // 发送礼物
                Bundle bundle = intent.getExtras();
                String content = bundle.getString("gift");
                String fromUserId = bundle.getString("fromUserId");
                String fromUserName = bundle.getString("fromUserName");
                GiftItem giftItem = new GiftItem();
                giftItem.setGiftUi(fromUserId);
                giftItem.setGiftUn(fromUserName);
                // 得到礼物图片与礼物名称
                String giftName = PreferenceUtils.getString(getActivity(), content);
                String giftImage = PreferenceUtils.getString(getActivity(), giftName);
                giftItem.setName(giftName);
                giftItem.setPhoto(giftImage);
                if (!gifts.contains(giftName + fromUserId)) {
                    gifts.clear();
                    gifts.add(giftName + fromUserId);
                    giftView.setGift(giftItem);
                }
                giftView.addNum(1);
            } else if (action.equals(LiveConstants.LIVE_SEND_LOVE_HEART)) {
                // 发送爱心
                heartLayout.addHeart(randomColor());
            } else if (action.equals(LiveConstants.LIVE_SEND_MANAGER)) {
                // 设置管理
                // 刷新成员列表,改变此人的状态
                getLiveRoomMember();
            } else if (action.equals(LiveConstants.LIVE_SEND_SHUT_UP)) {
                // 禁言
                // 刷新成员列表,改变此人的状态
                getLiveRoomMember();
            } else if (action.equals(LiveConstants.LIVE_SEND_LOCKED)) {
                getActivity().finish();
            }
        }
    };

    public AnchorChatFragment() {
    }

    /**
     * todo: 这个fragment没有无参构造方法，直播中后台activity被销毁回来会导致崩溃，java.lang.RuntimeException: Unable to start activity ComponentInfo{com.btchat.weichat/com.sk.weichat.ui.live.PushFlowActivity}: android.support.v4.app.Fragment$InstantiationException: Unable to instantiate fragment com.sk.weichat.ui.live.AnchorChatFragment: could not find Fragment constructor
     *
     * @param SrsPublisher
     * @param roomId
     * @param roomJid
     * @param roomName
     * @param roomUser
     */
    public AnchorChatFragment(SrsPublisher SrsPublisher, String roomId, String roomJid, String roomName, String roomUser) {
        this.mSrsPublisher = SrsPublisher;
        this.mRoomid = roomId;
        this.mRoomJid = roomJid;
        this.mRoomName = roomName;
        this.mRoomUser = roomUser;
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_chat_for_auchor;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            mLoginUserId = coreManager.getSelf().getUserId();
            mLoginNickName = coreManager.getSelf().getNickName();
            mAccessToken = coreManager.getSelfStatus().accessToken;
            this.mRoomNotice = requireActivity().getIntent().getStringExtra(LiveConstants.LIVE_ROOM_NOTICE);
            // 添加消息监听
            ListenerManager.getInstance().addChatMessageListener(this);
            // 初始化控件
            initView();
            // 初始化数据
            initData();
            // 初始化弹幕
            initDanmaku();
            // 注册成员列表/弹幕/送礼物广播
            IntentFilter filter = new IntentFilter();
            filter.addAction(LiveConstants.LIVE_MEMBER_ADD);
            filter.addAction(LiveConstants.LIVE_MEMBER_DELETE);
            filter.addAction(LiveConstants.LIVE_DANMU_DRAWABLE);
            filter.addAction(LiveConstants.LIVE_SEND_GIFT);
            filter.addAction(LiveConstants.LIVE_SEND_LOVE_HEART);
            filter.addAction(LiveConstants.LIVE_SEND_MANAGER);
            filter.addAction(LiveConstants.LIVE_SEND_SHUT_UP);
            filter.addAction(LiveConstants.LIVE_SEND_LOCKED);
            getActivity().registerReceiver(changeSomeBroadcastReceiver, filter);
        }
    }

    private void initView() {
        mRandom = new Random();
        inputManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        /* 成员 */
        listview = (HorizontialListView) findViewById(R.id.list);
        mAdapter = new MemberAdapter(getActivity());
        listview.setAdapter(mAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showMember(mAdapter.datas.get(i));
            }
        });
        topView = findViewById(R.id.layout_top);
        bottomView = findViewById(R.id.layout_bottom_menu);
        mSkin = findViewById(R.id.select_skin);
        /* 礼物/爱心 */
        giftView = (GiftItemView) findViewById(R.id.gift_item_first);
        heartLayout = (HeartLayout) findViewById(R.id.heart_layout);
        /* 上 */
        mAvatar = (ImageView) findViewById(R.id.avatar_iv);
        mAvatar.setOnClickListener(this);
        mNickName = (TextView) findViewById(R.id.nick_name);
        mPeopleNum = (TextView) findViewById(R.id.people_num);
        mPayAttention = (TextView) findViewById(R.id.attention_live);
        /* 下 */
        sendView = findViewById(R.id.layout_send_message);
        mDanMuS = (TextView) findViewById(R.id.danmu);
        sendEditText = (EditText) findViewById(R.id.send_edit);
        sendTextView = (TextView) findViewById(R.id.send_tv);
        // 隐藏关注按钮
        mPayAttention.setVisibility(View.GONE);
        mDanMuS.setText(getString(R.string.danmu));
        sendEditText.setHint(getString(R.string.live_vc_chatplaceholder));
        sendTextView.setText(getString(R.string.send));
        mDanMuS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = !flag;
                if (flag) {
                    sendEditText.setHint(getString(R.string.live_vc_barrage));
                    mDanMuS.setBackground(getResources().getDrawable(R.drawable.bg_danmu));
                } else {
                    sendEditText.setHint(getString(R.string.live_vc_chatplaceholder));
                    mDanMuS.setBackground(null);
                }
            }
        });
        sendTextView.setOnClickListener(this);
        // Send  message
        mChatContentView = (ChatContentView) findViewById(R.id.chat_content_view);
        // 获取屏幕高度
        screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        // 阀值设置为屏幕高度的1/3(软件盘高度)
        keyHeight = screenHeight / 3;
        rootView = findViewById(R.id.activity_main);
        rootView.setOnClickListener(this);
        rootView.addOnLayoutChangeListener(this);
        // 发消息
        findViewById(R.id.send_message).setOnClickListener(this);
        // 切换摄像头
        findViewById(R.id.swCam).setOnClickListener(this);
        // 美颜
        findViewById(R.id.skinWhite).setOnClickListener(this);
        // 关闭直播间
        findViewById(R.id.close).setOnClickListener(this);
        findViewById(R.id.close_bottom).setOnClickListener(this);
        // Skin whitening
        findViewById(R.id.original_filter).setOnClickListener(this);
        findViewById(R.id.romance_filter).setOnClickListener(this);
        findViewById(R.id.warm_filter).setOnClickListener(this);
        findViewById(R.id.tender_filter).setOnClickListener(this);
        findViewById(R.id.sunrise_filter).setOnClickListener(this);
        findViewById(R.id.sunset_filter).setOnClickListener(this);
        findViewById(R.id.cool_filter).setOnClickListener(this);
        findViewById(R.id.evergreen_filter).setOnClickListener(this);

        /**
         * 因为PushFlow的windowSoftInputMode并没有设置adjustResize属性，如果设置adjustResize软键盘弹起会挤压Activity,
         * 这样rootView的onLayoutChange才监测的到屏幕宽/高变化，如果设置其他属性根本监测不到
         * so,换一种方式监测
         */
        mKeyBoardShowListener = new KeyBoardShowListener(getActivity(), sendView);
        mKeyBoardShowListener.setKeyboardListener(new KeyBoardShowListener.OnKeyboardVisibilityListener() {
            @Override
            public void onVisibilityChanged(boolean visible) {
                if (visible) {
                    sendView.setVisibility(View.VISIBLE);
                    topView.setVisibility(View.GONE);
                    bottomView.setVisibility(View.GONE);
                } else {
                    sendView.setVisibility(View.GONE);
                    topView.setVisibility(View.VISIBLE);
                    bottomView.setVisibility(View.VISIBLE);
                }
            }
        }, getActivity());
    }

    /**
     * 初始化数据
     */
    private void initData() {
        getIdentity();

        // 获取直播间详情
        getLiveRoomDetail();
        // 获取直播间成员列表
        members = new ArrayList<>();
        getLiveRoomMember();
        // 消息数据
        mChatMessages = new ArrayList<>();
        mChatContentView.setChatListType(ChatContentView.ChatListType.LIVE);
        mChatContentView.setCurGroup(isGroupChat, mLoginNickName);
        mChatContentView.setToUserId(mRoomJid);
        mChatContentView.setData(mChatMessages);
        mChatContentView.setMessageEventListener(this);

        mChatContentView.setRefreshListener(new PullDownListView.RefreshingListener() {
            @Override
            public void onHeaderRefreshing() {
                loadDatas(false);
            }
        });
        gift = new ArrayList<>();
        gifts = new ArrayList<>();
        // 初始化时就获取礼物列表
        GetGiftS();
    }

    private void initDanmaku() {
        danmakuView = (DanmakuView) findViewById(R.id.danmaku_view);
        danmakuContext = DanmakuContext.create();
        // 设置弹幕的最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        // 滚动弹幕最大显示3行
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 3);
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_LR, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_BOTTOM, true);

        danmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3) //设置描边样式
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(1.2f) //是否启用合并重复弹幕
                .setScaleTextSize(1.2f) //设置弹幕滚动速度系数,只对滚动弹幕有效
                // .setCacheStuffer(new SpannedCacheStuffer(), mCacheStufferAdapter) // 图文混排使用SpannedCacheStuffer  设置缓存绘制填充器，默认使用{@link SimpleTextCacheStuffer}只支持纯文字显示, 如果需要图文混排请设置{@link SpannedCacheStuffer}如果需要定制其他样式请扩展{@link SimpleTextCacheStuffer}|{@link SpannedCacheStuffer}
                .setMaximumLines(maxLinesPair) //设置最大显示行数
                .preventOverlapping(overlappingEnablePair); //设置防弹幕重叠，null为允许重叠

        danmakuView.setCallback(new DrawHandler.Callback() {

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }

            @Override
            public void prepared() {
                // 是否显示FPS
                danmakuView.showFPS(false);
                // 节省绘制时间,在模拟器上可能会报错
                danmakuView.enableDanmakuDrawingCache(true);
                danmakuView.start();
            }
        });
        danmakuView.prepare(parser, danmakuContext);
    }

    /**
     * 修改直播间资料
     */
    public void modifyLiveRoom(String roomName, String roomNotice) {
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        Map<String, String> params = new HashMap<>();
        params.put("access_token", mAccessToken);
        params.put("roomId", mRoomid);
        params.put("name", roomName);
        params.put("notice", roomNotice);
        HttpUtils.get().url(coreManager.getConfig().LIVE_ROOM_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            mRoomName = roomName;
                            mRoomNotice = roomNotice;
                            MucgroupUpdateUtil.broadcastUpdateUi(requireContext());
                        }
                    }

                    @Override
                    public void onError(okhttp3.Call call, Exception e) {

                    }

                });
    }

    // TODO Auto-generated method stub
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.avatar_iv) {
            if (self == null) {
                return;
            }
            FragmentEditDialog.newInstance(self, self, mRoomName, mRoomNotice, new FragmentEditDialog.OnEditListener() {
                @Override
                public void onNameEdit(String name) {
                    modifyLiveRoom(name, mRoomNotice);
                }

                @Override
                public void onNoticeEdit(String notice) {
                    modifyLiveRoom(mRoomName, notice);
                }
            }).show(getChildFragmentManager(), "dialog");
        }
        if (id == R.id.send_message) {
            keyBoradCount++;
            Log.e("zq", String.valueOf(keyBoradCount));
            mKeyBoardShowListener.setKeyBoardCount(keyBoradCount);
             /*
             发消息/弹幕
             */
            sendEditText.requestFocus();
            // 软键盘弹起
            inputManager.showSoftInput(sendEditText, 0);
        }
        if (id == R.id.send_tv) {
            String content = sendEditText.getText().toString().trim();
            if (isAuthenticated()) {
                return;
            }
            if (TextUtils.isEmpty(content)) {
                return;
            }
            if (flag) {
                // 在查余额之前需要先更新余额
                int balance = (int) coreManager.getSelf().getBalance();
                if (balance >= 1) {
                    // 发送弹幕
                    SendDanmu(content);
                } else {
                    // 余额不足，去充值
                    // 充值
                    FragmentPayDialog.newInstance(new FragmentPayDialog.OnClickBottomListener() {
                        @Override
                        public void onPositiveClick() {
                            // 充值
                            startActivity(new Intent(getActivity(), WxPayBlance.class));
                        }

                        @Override
                        public void onNegativeClick() {
                            // 取消
                        }
                    }).show(getChildFragmentManager(), "dialog");
                }
            } else {
                // 聊天
                sendOnlyWord(content);
            }
            // 清空EditText
            sendEditText.getText().clear();
            // 软键盘消失
            inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
        if (id == R.id.swCam) {
            /*
            切换摄像头
             */
            mSrsPublisher.switchCameraFace((mSrsPublisher.getCamraId() + 1) % Camera.getNumberOfCameras());
        }
        if (id == R.id.close || id == R.id.close_bottom) {
            // 退出群聊房间
            getActivity().finish();
        }
        if (id == R.id.skinWhite) {
            /*
            切换美颜效果
             */
            mSkin.setVisibility(View.VISIBLE);
            topView.setVisibility(View.GONE);
            bottomView.setVisibility(View.GONE);
        }
        // Skin whitening
        if (id == R.id.original_filter) {
            mSrsPublisher.switchCameraFilter(MagicFilterType.NONE);
            sureSkin();
        }
        if (id == R.id.romance_filter) {
            mSrsPublisher.switchCameraFilter(MagicFilterType.ROMANCE);
            sureSkin();
        }
        if (id == R.id.warm_filter) {
            mSrsPublisher.switchCameraFilter(MagicFilterType.WARM);
            sureSkin();
        }
        if (id == R.id.tender_filter) {
            mSrsPublisher.switchCameraFilter(MagicFilterType.TENDER);
            sureSkin();
        }
        if (id == R.id.sunrise_filter) {
            mSrsPublisher.switchCameraFilter(MagicFilterType.SUNRISE);
            sureSkin();
        }
        if (id == R.id.sunset_filter) {
            mSrsPublisher.switchCameraFilter(MagicFilterType.SUNSET);
            sureSkin();
        }
        if (id == R.id.cool_filter) {
            mSrsPublisher.switchCameraFilter(MagicFilterType.COOL);
            sureSkin();
        }
        if (id == R.id.evergreen_filter) {
            mSrsPublisher.switchCameraFilter(MagicFilterType.EVERGREEN);
            sureSkin();
        }
    }

    // 展示成员
    private void showMember(final Member member) {
        if (self == null) {
            return;
        }
        FragmentDialog.newInstance(self, member, (View v) -> {
            if (isAuthenticated()) {
                return;
            }
            switch (v.getId()) {
                case R.id.set_manager:
                    // 设置管理员
                    if (member.getType() == Member.TYPE_MANAGER) {
                        cancleManager(member);
                    } else {
                        setManager(member);
                    }
                    break;
                case R.id.shut_up:
                    if (member.getTalkTime() < TimeUtils.sk_time_current_time()) {
                        showBannedDialog(member);
                    } else {
                        cancelShutUpMember(member);
                    }
                    break;
                case R.id.kick_room:
                    // 踢人
                    kickMember(member);
                    break;
                case R.id.cancel:
                    break;
            }
        }).show(getChildFragmentManager(), "dialog");
    }

    private void showBannedDialog(Member member) {
        final int daySeconds = 24 * 60 * 60;
        BannedDialog bannedDialog = new BannedDialog(getActivity(), new BannedDialog.OnBannedDialogClickListener() {

            @Override
            public void tv1Click() {
                shutUpMember(member, 0);
            }

            @Override
            public void tv2Click() {
                shutUpMember(member, TimeUtils.sk_time_current_time() + daySeconds / 48);
            }

            @Override
            public void tv3Click() {
                shutUpMember(member, TimeUtils.sk_time_current_time() + daySeconds / 24);
            }

            @Override
            public void tv4Click() {
                shutUpMember(member, TimeUtils.sk_time_current_time() + daySeconds);
            }

            @Override
            public void tv5Click() {
                shutUpMember(member, TimeUtils.sk_time_current_time() + daySeconds * 3);
            }

            @Override
            public void tv6Click() {
                shutUpMember(member, TimeUtils.sk_time_current_time() + daySeconds * 7);
            }

            @Override
            public void tv7Click() {
                shutUpMember(member, TimeUtils.sk_time_current_time() + daySeconds * 15);
            }
        });
        bannedDialog.setNoGagGone();
        bannedDialog.show();
    }

    // 美颜
    private void sureSkin() {
        mSkin.setVisibility(View.GONE);
        topView.setVisibility(View.VISIBLE);
        bottomView.setVisibility(View.VISIBLE);
    }

    private void loadDatas(final boolean scrollToBottom) {
        if (mChatMessages.size() > 0) {
            mMinId = mChatMessages.get(0).get_id();
        } else {
            mMinId = 0;
        }
        List<ChatMessage> chatLists = ChatMessageDao.getInstance().getSingleChatMessages(mLoginUserId, mRoomJid, mMinId,
                mPageSize);
        if (chatLists == null || chatLists.size() <= 0) {
            mHasMoreData = false;
        } else {
            long currentTime = System.currentTimeMillis() / 1000;

            for (int i = 0; i < chatLists.size(); i++) {
                ChatMessage message = chatLists.get(i);
                if (message.isMySend() && message.getMessageState() == ChatMessageListener.MESSAGE_SEND_ING) {
                    // 如果是我发的消息，有时候在消息发送中，直接退出了程序，此时消息发送状态可能使用是发送中，
                    if (currentTime - message.getTimeSend() > ReceiptManager.MESSAGE_DELAY / 1000) {
                        Log.e("xuan", "anchor: 退出了程序，改变消息发送状态");
                        ChatMessageDao.getInstance().updateMessageSendState(mLoginUserId, mRoomJid, message.get_id(),
                                ChatMessageListener.MESSAGE_SEND_FAILED);
                        message.setMessageState(ChatMessageListener.MESSAGE_SEND_FAILED);
                    }
                }
                mChatMessages.add(0, message);
            }
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mChatContentView.notifyDataSetInvalidated(scrollToBottom);
                mChatContentView.headerRefreshingCompleted();
                if (!mHasMoreData) {
                    mChatContentView.setNeedRefresh(false);
                }
            }
        }, 0);
    }

    /***********************
     * 直播聊天，只可以发送文字
     ***********************/
    public void sendOnlyWord(String content) {
        if (isAuthenticated()) {
            return;
        }

        if (TextUtils.isEmpty(content)) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_TEXT);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(content);
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());

        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);

        ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mRoomJid, message);
        coreManager.sendMucChatMessage(mRoomJid, message);
    }

    /**
     * 添加文本弹幕
     *
     * @param content    弹幕的具体内容
     * @param withBorder 弹幕是否有边框
     */
    private void addDanmaku(String content, boolean withBorder, boolean whoSend) {
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = content;
        danmaku.padding = 5;
        // 0 表示可能会被各种过滤器过滤并隐藏显示,1表示一定会显示,一般用于本机发送的弹幕
        danmaku.priority = 1;
        // 直播弹幕
        danmaku.isLive = true;
        danmaku.textSize = sp2px(14);
        if (whoSend) {
            // is my send,change text color
            danmaku.textColor = getResources().getColor(R.color.chat_background);
        } else {
            danmaku.textColor = Color.WHITE;
        }
        // 阴影/描边颜色
        danmaku.textShadowColor = Color.WHITE;
        danmaku.setTime(danmakuView.getCurrentTime());
        if (withBorder) {
            // 边框颜色，0表示无边框
            danmaku.borderColor = Color.GREEN;
        }
        danmakuView.addDanmaku(danmaku);
    }

    // 更新余额
    public void updateMoney() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", mAccessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        HttpUtils.get().url(coreManager.getConfig().RECHARGE_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<Balance>(Balance.class) {
                    @Override
                    public void onResponse(ObjectResult<Balance> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            Balance balance = result.getData();
                            coreManager.getSelf().setBalance(balance.getBalance());
                        } else {
                            ToastUtil.showErrorData(requireContext());
                        }
                    }

                    @Override
                    public void onError(okhttp3.Call call, Exception e) {
                        ToastUtil.showErrorData(requireContext());
                    }
                });
    }

    // 获取直播间详情
    public void getLiveRoomDetail() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", mAccessToken);
        params.put("roomId", mRoomid);
        HttpUtils.get().url(coreManager.getConfig().LIVE_ROOM_DETAIL)
                .params(params)
                .build()
                .execute(new BaseCallback<Detail>(Detail.class) {
                    @Override
                    public void onResponse(ObjectResult<Detail> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            Detail detail = result.getData();
                            AvatarHelper.getInstance().displayAvatar(String.valueOf(detail.getUserId()), mAvatar, true);
                            mNickName.setText(detail.getNickName());
                        }
                    }

                    @Override
                    public void onError(okhttp3.Call call, Exception e) {

                    }

                });
    }

    // 获取直播间成员列表
    public void getLiveRoomMember() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", mAccessToken);
        params.put("roomId", mRoomid);
        HttpUtils.get().url(coreManager.getConfig().LIVE_ROOM_MEMBER_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<Member>(Member.class) {

                    @Override
                    public void onResponse(ArrayResult<Member> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            members.clear();
                            for (int i = 0; i < result.getData().size(); i++) {
                                if (String.valueOf(result.getData().get(i).getUserId()).equals(mRoomUser)) {// 主播，不显示在成员列表中
                                    self = result.getData().get(i);
                                } else {
                                    members.add(result.getData().get(i));
                                }
                            }
                            mPeopleNum.setText(String.valueOf(members.size()));
                            mAdapter.setDatas(members);
                        }
                    }

                    @Override
                    public void onError(okhttp3.Call call, Exception e) {

                    }

                });
    }

    // 获取礼物列表
    public void GetGiftS() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", mAccessToken);
        params.put("pageIndex", String.valueOf(0));
        params.put("pageSize", String.valueOf(50));
        HttpUtils.get().url(coreManager.getConfig().GET_LIVE_GIFT_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<Gift>(Gift.class) {

                    @Override
                    public void onResponse(ArrayResult<Gift> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            gift = (ArrayList<Gift>) result.getData();
                            // 将数据保存至共享参数内
                            for (int i = 0; i < gift.size(); i++) {
                                PreferenceUtils.putString(getActivity(), gift.get(i).getGiftId(), gift.get(i).getName());
                            }
                            for (int i = 0; i < gift.size(); i++) {
                                PreferenceUtils.putString(getActivity(), gift.get(i).getName(), gift.get(i).getPhoto());
                            }
                        }
                    }

                    @Override
                    public void onError(okhttp3.Call call, Exception e) {

                    }

                });
    }

    /**
     * 获取自己的身份信息(现在服务端可能都不返回主播的信息了，自己获取一下)
     */
    public void getIdentity() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", mAccessToken);
        params.put("roomId", mRoomid);
        params.put("userId", mLoginUserId);
        HttpUtils.get().url(coreManager.getConfig().LIVE_ROOM_GET_IDENTITY)
                .params(params)
                .build()
                .execute(new BaseCallback<Member>(Member.class) {
                    @Override
                    public void onResponse(ObjectResult<Member> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            self = result.getData();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    // 获取他人的身份信息,用于直播昵称点击
    public void getOtherIdentity(String userId) {
        Log.e("zq", "身份认证标识:" + userId);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", mAccessToken);
        params.put("roomId", mRoomid);
        params.put("userId", userId);
        HttpUtils.get().url(coreManager.getConfig().LIVE_ROOM_GET_IDENTITY)
                .params(params)
                .build()
                .execute(new BaseCallback<Member>(Member.class) {
                    @Override
                    public void onResponse(ObjectResult<Member> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            Member member = result.getData();
                            showMember(member);
                        }
                    }

                    @Override
                    public void onError(okhttp3.Call call, Exception e) {

                    }

                });
    }

    // 发送弹幕
    public void SendDanmu(final String context) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", mAccessToken);
        params.put("roomId", mRoomid);
        params.put("userId", mLoginUserId);
        params.put("text", context);
        HttpUtils.get().url(coreManager.getConfig().LIVE_ROOM_DANMU)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            // 更新余额
                            updateMoney();
                            addDanmaku(context, false, true);
                        }
                    }

                    @Override
                    public void onError(okhttp3.Call call, Exception e) {

                    }
                });
    }

    // 设置管理员
    public void setManager(final Member member) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", mAccessToken);
        params.put("roomId", mRoomid);
        params.put("userId", String.valueOf(member.getUserId()));
        params.put("type", String.valueOf(Member.TYPE_MANAGER));
        HttpUtils.get().url(coreManager.getConfig().LIVE_ROOM_SET_MANAGER)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            member.setType(Member.TYPE_MANAGER);
                            Toast.makeText(getActivity(), getString(R.string.room_member_vc_set_administrator_success), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(okhttp3.Call call, Exception e) {

                    }
                });
    }

    // 取消管理员
    public void cancleManager(final Member member) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", mAccessToken);
        params.put("roomId", mRoomid);
        params.put("userId", String.valueOf(member.getUserId()));
        params.put("type", String.valueOf(Member.TYPE_MEMBER));
        HttpUtils.get().url(coreManager.getConfig().LIVE_ROOM_SET_MANAGER)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            member.setType(Member.TYPE_MEMBER);
                            if (getContext() != null) {
                                ToastUtil.showToast(requireContext(), getString(R.string.room_member_vc_cancel_administrator_success));
                            }
                        }
                    }

                    @Override
                    public void onError(okhttp3.Call call, Exception e) {

                    }

                });
    }

    // 禁言
    public void shutUpMember(Member member, long talkTime) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", mAccessToken);
        params.put("roomId", mRoomid);
        params.put("userId", String.valueOf(member.getUserId()));
        params.put("state", String.valueOf(1));
        params.put("talkTime", String.valueOf(talkTime));
        HttpUtils.get().url(coreManager.getConfig().LIVE_ROOM_SHUT_UP)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            Toast.makeText(getActivity(),
                                    getString(R.string.live_vc_setgag) + " " + getString(R.string.success),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(okhttp3.Call call, Exception e) {

                    }

                });
    }

    // 取消禁言
    public void cancelShutUpMember(Member member) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", mAccessToken);
        params.put("roomId", mRoomid);
        params.put("userId", String.valueOf(member.getUserId()));
        params.put("state", String.valueOf(0));
        params.put("talkTime", String.valueOf(0));
        HttpUtils.get().url(coreManager.getConfig().LIVE_ROOM_SHUT_UP)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            Toast.makeText(getActivity(),
                                    getString(R.string.live_gag_cancel) + " " + getString(R.string.success),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(okhttp3.Call call, Exception e) {

                    }

                });
    }

    // 踢人
    public void kickMember(final Member member) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", mAccessToken);
        params.put("roomId", mRoomid);
        params.put("userId", String.valueOf(member.getUserId()));
        HttpUtils.get().url(coreManager.getConfig().LIVE_ROOM_KICK)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            Toast.makeText(getActivity(),
                                    getString(R.string.live_kick_success), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(okhttp3.Call call, Exception e) {

                    }
                });
    }

    // 退出直播间
    public void exitRoom() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", mAccessToken);
        params.put("roomId", mRoomid);
        params.put("userId", mLoginUserId);
        HttpUtils.get().url(coreManager.getConfig().EXIT_LIVE_ROOM)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        coreManager.exitMucChat(mRoomJid);
                    }

                    @Override
                    public void onError(okhttp3.Call call, Exception e) {

                    }

                });
    }

    // 软键盘弹出，屏幕被挤压
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        // 现在认为只要控件将Activity向上推的高度超过了1/3屏幕高，就认为软键盘弹起
        if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
            sendView.setVisibility(View.VISIBLE);
            topView.setVisibility(View.GONE);
            bottomView.setVisibility(View.GONE);
        } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {
            sendView.setVisibility(View.GONE);
            topView.setVisibility(View.VISIBLE);
            bottomView.setVisibility(View.VISIBLE);
        }
    }

    /**********************
     * MUC Message Listener
     ********************/
    @Override
    public void onMessageSendStateChange(int messageState, String msgId) {

    }

    @Override
    public boolean onNewMessage(String fromUserId, ChatMessage message, boolean isGroupMsg) {
        if (isGroupMsg != isGroupChat) {
            return false;
        }
        if (mRoomJid.compareToIgnoreCase(fromUserId) == 0) {
            mChatMessages.add(message);
            mChatContentView.notifyDataSetInvalidated(true);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        try {
            exitRoom();
            getActivity().unregisterReceiver(changeSomeBroadcastReceiver);
        } catch (Exception e) {
            // activity有可能被系统销毁，到时候系统重建是先创建fragment，在创建activity，
            // 另在PushFlowActivity的onCreate方法内会调用removeFragments方法，将系统创建的空fragment移除掉，自己重新创建fragment，此时空fragment会回调onDestroy方法
        }
        super.onDestroy();
    }

    @Override
    public void onNickNameClick(String friendUserId) {
        getOtherIdentity(friendUserId);
    }

    @Override
    public void onEmptyTouch() {

    }

    @Override
    public void onTipMessageClick(ChatMessage message) {

    }

    @Override
    public void onMyAvatarClick() {

    }

    @Override
    public void onFriendAvatarClick(String friendUserId) {

    }

    @Override
    public void LongAvatarClick(ChatMessage chatMessage) {

    }

    @Override
    public void onMessageClick(ChatMessage chatMessage) {

    }

    @Override
    public void onMessageLongClick(ChatMessage chatMessage) {

    }

    @Override
    public void onMessageBack(ChatMessage chatMessage, int position) {

    }

    @Override
    public void onSendAgain(ChatMessage chatMessage) {

    }

    @Override
    public void onCallListener(int type) {

    }

    /**
     * 随机rgb
     */
    private int randomColor() {
        return Color.rgb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255));
    }

    /**
     * sp转px的方法
     */
    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /*******************************************
     * 是否离线&&重连
     ******************************************/
    public boolean isAuthenticated() {
        boolean isLogin = coreManager.isLogin();
        if (!isLogin) {
            coreManager.autoReconnect(getActivity());
        }
        return !isLogin;
    }
}
