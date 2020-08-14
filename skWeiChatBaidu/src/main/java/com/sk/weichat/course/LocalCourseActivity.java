package com.sk.weichat.course;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.roamer.slidelistview.SlideBaseAdapter;
import com.roamer.slidelistview.SlideListView;
import com.sk.weichat.AppConfig;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.event.MessageSendChat;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.CourseBean;
import com.sk.weichat.bean.message.CourseChatBean;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.PrivacySettingHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.AppUtils;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.DES;
import com.sk.weichat.util.Md5Util;
import com.sk.weichat.util.PermissionUtil;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.ViewHolder;
import com.sk.weichat.view.PullToRefreshSlideListView;
import com.sk.weichat.view.SelectionFrame;
import com.sk.weichat.xmpp.listener.ChatMessageListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 我的课件
 *
 * @author xuan
 * @version 1.0
 */
public class LocalCourseActivity extends BaseActivity {
    private static TextView tvRight;
    boolean isRun;
    int mPos;
    private PullToRefreshSlideListView mPullToRefreshListView;
    Runnable RefreComplete = new Runnable() {
        @Override
        public void run() {
            mPullToRefreshListView.onRefreshComplete();
        }
    };
    private List<CourseBean> mVideoFiles;
    private Map<Integer, Integer> mStates;
    private LocalCourseAdapter mAdapter;
    private String mLoginUserId;
    private Button mSure;
    private boolean state; // 是否编辑模式
    private int currt;
    private int currtDateils;
    private ArrayList<ChatMessage> mChatMessages;
    Runnable sendMessageTask = new Runnable() {
        @Override
        public void run() {
            while (isRun) {
                mHandlerChat.sendEmptyMessage(mPos);
                mPos++;
                if (mPos == mChatMessages.size()) {
                    // 最后一条已发送完成
                    isRun = false;
                    SystemClock.sleep(400);
                    mHandlerChat.sendEmptyMessage(-1);
                } else {
                    long sleepTime = 1000;
                    SystemClock.sleep(sleepTime);
                }
            }
        }
    };
    private boolean isGroup;
    private String toUserId;
    private SuspenionWondow windowManager;
    private TextView mTvSuspen;
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);

            if (msg.what > mStates.size()) {
                sendDateils();
                return;
            }

            mTvSuspen.setText(getString(R.string.downloading_cource_index_place_holder, msg.what));
            SystemClock.sleep(200);

            for (int i : mStates.keySet()) {
                int value = mStates.get(i);
                if (value == msg.what) {
                    currtDateils++;
                    CourseBean courseBean = mVideoFiles.get(i);
                    new Thread(new LoadDateilsTask(courseBean.getCourseId())).start();
                    Log.e("xuan", "sendListChat: " + courseBean.getCourseName());
                    return;
                }
            }
        }
    };
    private boolean isEncrypt;
    @SuppressLint("HandlerLeak")
    Handler mHandlerChat = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == -1) {
                Constants.IS_SENDONG_COURSE_NOW = false;// 课程发送完成，修改发送状态
                hideSuspensionWondow();
                ToastUtil.showToast(LocalCourseActivity.this, getString(R.string.course_sent_success));
                return;
            }
            if (mChatMessages.size() == 0) {
                if (!CourseDateilsActivity.isCourseDetailClassSendHandler) {// todo 因为CourseDateilsActivity也有一个mHandler，需要坐下兼容处理
                    Constants.IS_SENDONG_COURSE_NOW = false;// 讲课数量为0
                    hideSuspensionWondow();
                }
                return;
            }
            mTvSuspen.setText(getString(R.string.send_now) + getString(R.string.number) + " " + (msg.what + 1)
                    + getString(R.string.mainviewcontroller_message) + "," + getString(R.string.weibodata_perzan2) + mChatMessages.size() +
                    getString(R.string.article));

            ChatMessage chatMessage = mChatMessages.get(msg.what);
            // 数据解密
            if (chatMessage.getIsEncrypt() == 1) { // == 1
                try {
                    String decryptKey = Md5Util.toMD5(AppConfig.apiKey + chatMessage.getTimeSend() + chatMessage.getPacketId());
                    String decryptContent = DES.decryptDES(chatMessage.getContent(), decryptKey);
                    // 为chatMessage重新设值
                    chatMessage.setContent(decryptContent);
                    chatMessage.setIsEncrypt(0);
                } catch (Exception e) {
                    Reporter.unreachable(e);
                }
            }
            // 重新加密，
            if (isEncrypt) {
                chatMessage.setIsEncrypt(1);
            } else {
                chatMessage.setIsEncrypt(0);
            }
            chatMessage.setFromUserId(mLoginUserId);
            chatMessage.setFromUserName(coreManager.getSelf().getNickName());
            chatMessage.setToUserId(toUserId);
            chatMessage.setIsReadDel(0);
            chatMessage.setMySend(true);
            chatMessage.setGroup(isGroup);
            chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
            chatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());

            ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, toUserId, chatMessage);
            MessageSendChat chat = new MessageSendChat(isGroup, toUserId, chatMessage);
            EventBus.getDefault().post(chat);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_pullrefresh_list_slide);
        mLoginUserId = coreManager.getSelf().getUserId();

        mSure = (Button) findViewById(R.id.sure_btn);
        ButtonColorChange.colorChange(this, mSure);

        mVideoFiles = new ArrayList<>();
        mStates = new HashMap<>();
        mChatMessages = new ArrayList<>();
        currt = 0;
        mAdapter = new LocalCourseAdapter(this);
        initActivityBar();
        initView();

        isEncrypt = PrivacySettingHelper.getPrivacySettings(this).getIsEncrypt() == 1;

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initActivityBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.my_lecture));

        tvRight = (TextView) findViewById(R.id.tv_title_right);
        tvRight.setVisibility(mAdapter.getCount() == 0 ? View.GONE : View.VISIBLE);
        tvRight.setText(getString(R.string.multiselect));
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state) {
                    mStates.clear();
                    currt = 0;
                    mSure.setVisibility(View.GONE);
                    tvRight.setText(getString(R.string.multiselect));
                } else {
                    mSure.setVisibility(View.VISIBLE);
                    tvRight.setText(getString(R.string.cancel));
                }
                state = !state;
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initView() {
        mPullToRefreshListView = (PullToRefreshSlideListView) findViewById(R.id.pull_refresh_list);
        View emptyView = LayoutInflater.from(mContext).inflate(R.layout.layout_list_empty_view, null);
        mPullToRefreshListView.setEmptyView(emptyView);
        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullToRefreshListView.setShowIndicator(false);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<SlideListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<SlideListView> refreshView) {
                loadData();
            }
        });

        mPullToRefreshListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position = (int) id;
                if (state) {
                    if (mStates.containsKey(position)) {
                        nextPosition(position); // 前移position
                    } else {
                        currt++;
                        mStates.put(position, currt);
                    }
                    mAdapter.notifyDataSetChanged();
                } else {
                    CourseBean courseBean = mVideoFiles.get(position);
                    Intent intent = new Intent(LocalCourseActivity.this, CourseDateilsActivity.class);
                    intent.putExtra("data", courseBean.getCourseId());
                    intent.putExtra("title", courseBean.getCourseName());
                    startActivity(intent);
                }
            }
        });

        mSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendListChat();
            }
        });

        DialogHelper.showDefaulteMessageProgressDialog(this);
        loadData();
    }

    private void nextPosition(int position) {
        int oldValue = 100;
        if (mStates.containsKey(position)) {
            oldValue = mStates.get(position);
        }
        for (int i : mStates.keySet()) {
            int value = mStates.get(i);
            if (value > oldValue) {
                mStates.put(i, value - 1);
            }
        }
        mStates.remove(position);
        currt--;
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventSendCourse message) {
        if (SelectFriendsActivity.isIntentLocalCourseActivity) {// 当前页面回调出来的Eventbus，才生效
            toUserId = message.getToUserId();
            isGroup = message.isGroup();
            showSuspensionWondow();
            mChatMessages.clear();
            currtDateils = 1;
            mHandler.sendEmptyMessage(currtDateils);
        }
    }

    private void loadData() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mLoginUserId);

        HttpUtils.get().url(coreManager.getConfig().USER_QUERY_COURSE)
                .params(params)
                .build()
                .execute(new ListCallback<CourseBean>(CourseBean.class) {
                    @Override
                    public void onResponse(ArrayResult<CourseBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            mVideoFiles = result.getData();
                            if (mVideoFiles == null) {
                                // 至少不能为null，容易崩溃，
                                mVideoFiles = new ArrayList<>();
                            }
                            mAdapter.notifyDataSetChanged();
                            mPullToRefreshListView.postDelayed(RefreComplete, 200);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(LocalCourseActivity.this);
                        mPullToRefreshListView.postDelayed(RefreComplete, 200);
                    }
                });
    }

    private void fromatDatas(List<CourseChatBean> result) {
        for (int i = 0; i < result.size(); i++) {
            try {
                CourseChatBean data = result.get(i);
                String messageBody = data.getMessage();
                org.json.JSONObject json = new org.json.JSONObject(messageBody);
                String body = json.getString("body");
                body = body.replaceAll("&quot;", "\"");
                ChatMessage chatMessage = new ChatMessage(body);
                ChatMessageDao.getInstance().decrypt(chatMessage.isGroup(), chatMessage);
                if (chatMessage.getType() == XmppMessage.TYPE_IMAGE
                        || chatMessage.getType() == XmppMessage.TYPE_VIDEO
                        || chatMessage.getType() == XmppMessage.TYPE_FILE) {
                    chatMessage.setUploadSchedule(100);
                    chatMessage.setUpload(true);
                }
                chatMessage.setMySend(true);
                chatMessage.setMessageState(ChatMessageListener.MESSAGE_SEND_SUCCESS);
                mChatMessages.add(chatMessage);
            } catch (JSONException e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(currtDateils);
                return;
            }
        }
        mHandler.sendEmptyMessage(currtDateils);
    }

    /**
     * 显示悬浮窗
     */
    private void showSuspensionWondow() {
        mTvSuspen = new TextView(this);
        mTvSuspen.setGravity(Gravity.CENTER);
        mTvSuspen.setBackgroundResource(R.drawable.course_connors);
        mTvSuspen.setTextAppearance(this, R.style.TextStyle);
        mTvSuspen.setText(R.string.sending_course);
        windowManager = new SuspenionWondow(LocalCourseActivity.this);
        windowManager.show(mTvSuspen);
    }

    /**
     * 隐藏悬浮窗
     */
    private void hideSuspensionWondow() {
        windowManager.hide();
    }

    /**
     * 修改课件名称
     *
     * @param position
     */
    private void updateName(int position) {
        final CourseBean data = mVideoFiles.get(position);
        DialogHelper.showLimitSingleInputDialog(this, getString(R.string.modify_name), data.getCourseName(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String input = ((EditText) v).getText().toString().trim();
                if (input.equals(data.getCourseName()) || TextUtils.isEmpty(input)) {// 备注名没变
                    return;
                }

                DialogHelper.showDefaulteMessageProgressDialog(LocalCourseActivity.this);
                Map<String, String> params = new HashMap<>();
                params.put("access_token", coreManager.getSelfStatus().accessToken);
                params.put("courseId", data.getCourseId());
                params.put("courseName", input);
                params.put("updateTime", TimeUtils.sk_time_current_time() + "");

                HttpUtils.get().url(coreManager.getConfig().USER_EDIT_COURSE)
                        .params(params)
                        .build()
                        .execute(new BaseCallback<Void>(Void.class) {
                            @Override
                            public void onResponse(ObjectResult<Void> result) {
                                DialogHelper.dismissProgressDialog();
                                if (Result.checkSuccess(mContext, result)) {
                                    ToastUtil.showToast(LocalCourseActivity.this, getString(R.string.update_sccuess));
                                    data.setCourseName(input);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onError(Call call, Exception e) {
                                DialogHelper.dismissProgressDialog();
                                ToastUtil.showErrorNet(LocalCourseActivity.this);
                            }
                        });
            }
        });
    }

    private boolean delete(final int position) {
        final CourseBean data = mVideoFiles.get(position);
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("courseId", data.getCourseId());

        HttpUtils.get().url(coreManager.getConfig().USER_DEL_COURSE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            ToastUtil.showToast(LocalCourseActivity.this, R.string.delete_all_succ);
                            mVideoFiles.remove(position);
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(LocalCourseActivity.this, R.string.delete_failed);
                    }
                });

        return true;
    }

    private void sendDateils() {
        ToastUtil.showToast(LocalCourseActivity.this, getString(R.string.all_course_complete));
        Log.e("xuan", "sendDateils: " + mChatMessages.size());
        mPos = 0;
        isRun = true;
        new Thread(sendMessageTask).start();
    }

    private void sendListChat() {
        if (mStates.size() == 0) {
            ToastUtil.showToast(this, getString(R.string.need_a_course));
            return;
        }

        if (AppUtils.checkAlertWindowsPermission(this)) { // 已开启悬浮窗权限
            startActivity(new Intent(mContext, SelectFriendsActivity.class));
            SelectFriendsActivity.isIntentLocalCourseActivity = true;
        } else {
            SelectionFrame selectionFrame = new SelectionFrame(this);
            selectionFrame.setSomething(null, getString(R.string.av_no_float), new SelectionFrame.OnSelectionFrameClickListener() {
                @Override
                public void cancelClick() {

                }

                @Override
                public void confirmClick() {
                    PermissionUtil.startApplicationDetailsSettings(LocalCourseActivity.this, 0x01);
                }
            });
            selectionFrame.show();
        }
    }

    private class LocalCourseAdapter extends SlideBaseAdapter {

        public LocalCourseAdapter(Context context) {
            super(context);
        }

        @Override
        public int getCount() {
            return mVideoFiles == null ? 0 : mVideoFiles.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = createConvertView(position);
            }
            TextView tvNema = ViewHolder.get(convertView, R.id.item_name);
            TextView tvTime = ViewHolder.get(convertView, R.id.item_time);
            TextView tvCheckBox = ViewHolder.get(convertView, R.id.item_checkbox);
            ImageView tvNext = ViewHolder.get(convertView, R.id.item_next_iv);
            TextView delete_tv = ViewHolder.get(convertView, R.id.delete_tv);
            TextView edit_tv = ViewHolder.get(convertView, R.id.top_tv);
            if (getCount() == 0) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        tvRight.setVisibility(View.GONE);
                    }
                });
            } else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        tvRight.setVisibility(View.VISIBLE);
                    }
                });
            }
            edit_tv.setText(getString(R.string.modify_name));
            if (state) {
                tvNext.setVisibility(View.GONE);
                tvCheckBox.setVisibility(View.VISIBLE);
                if (mStates.containsKey(position)) {
                    int value = mStates.get(position);
                    tvCheckBox.setText(String.valueOf(value));
                    tvCheckBox.setBackgroundResource(R.drawable.bg_radio_blu);
                } else {
                    tvCheckBox.setText("");
                    tvCheckBox.setBackgroundResource(R.drawable.bg_radio_no);
                }

            } else {
                tvNext.setVisibility(View.VISIBLE);
                tvCheckBox.setVisibility(View.GONE);
            }

            final CourseBean courseBean = mVideoFiles.get(position);
            long time = courseBean.getCreateTime();
            tvNema.setText(getString(R.string.coursename) + ": " + courseBean.getCourseName());
            tvTime.setText(getString(R.string.create_chat_time) + ": " + TimeUtils.long_to_yMdHm_str(time * 1000));
            delete_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!state) {
                        delete(position);
                    } else {
                        ToastUtil.showToast(LocalCourseActivity.this, getString(R.string.exit_edit));
                    }
                }
            });

            edit_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!state) {
                        updateName(position);
                    } else {
                        ToastUtil.showToast(LocalCourseActivity.this, getString(R.string.exit_edit));
                    }
                }
            });
            return convertView;
        }

        @Override
        public int getFrontViewId(int position) {
            return R.layout.item_course_list;
        }

        @Override
        public int getLeftBackViewId(int position) {
            return 0;
        }

        @Override
        public int getRightBackViewId(int position) {
            return R.layout.row_item_delete;
        }
    }

    class LoadDateilsTask implements Runnable {

        String courseId;

        public LoadDateilsTask(String id) {
            courseId = id;
        }

        @Override
        public void run() {
            Map<String, String> params = new HashMap<>();
            params.put("access_token", coreManager.getSelfStatus().accessToken);
            params.put("courseId", courseId);

            HttpUtils.get().url(coreManager.getConfig().USER_COURSE_DATAILS)
                    .params(params)
                    .build()
                    .execute(new ListCallback<CourseChatBean>(CourseChatBean.class) {
                        @Override
                        public void onResponse(ArrayResult<CourseChatBean> result) {
                            if (Result.checkSuccess(mContext, result)) {
                                fromatDatas(result.getData());
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            mHandler.sendEmptyMessage(currtDateils);
                        }
                    });
        }
    }
}
