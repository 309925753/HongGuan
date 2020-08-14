package com.sk.weichat.ui.groupchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.message.MucRoom;
import com.sk.weichat.bean.message.MucRoomMember;
import com.sk.weichat.broadcast.MsgBroadcast;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.message.HandleActivityDestroyedDoSomeThing;
import com.sk.weichat.ui.message.MucChatActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class FaceToFaceGroup extends BaseActivity implements View.OnClickListener {

    private ImageView iv1, iv2, iv3, iv4;
    private TextView tv1, tv2, tv3, tv4;

    private TextView tvResult1, tvResult2, tvResult3, tvResult4;
    private GridView mGridView;
    private GridViewAdapter mGridViewAdapter;
    private Button a_sure_btn;
    private List<MucRoomMember> mMucRoomMemberList = new ArrayList<>();
    private int mCurrentIndex = 0;
    private String mSignal;

    private boolean isQuery;
    private String jid;
    private double longitude, latitude;
    private BroadcastReceiver UpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            if (action.equals(MsgBroadcast.ACTION_FACE_GROUP_NOTIFY) && isQuery) {
                String operating = intent.getStringExtra(MsgBroadcast.EXTRA_OPERATING);
                if (TextUtils.equals(operating, "notify_list")) {
                    // 刷新页面
                    querySignalGroup();
                } else if (TextUtils.equals(operating, "join_room")) {
                    DialogHelper.dismissProgressDialog();
                    // 加入房间
                    Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), jid);
                    if (friend != null) {
                        goRoom(friend);
                    } else {
                        Toast.makeText(context, getString(R.string.tip_join_face_to_face_group_failed), Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_to_face_group);
        latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
        if (latitude == 0 && longitude == 0) {
            DialogHelper.tipDialog(mContext, getString(R.string.sure_open_user));
        }
        initActionBar();
        initStep1();
        initStep2();
        registerReceiver(UpdateReceiver, new IntentFilter(MsgBroadcast.ACTION_FACE_GROUP_NOTIFY));
        // 当前页面不支侧滑退出
        setSwipeBackEnable(false);
    }

    @Override
    protected void onDestroy() {
        if (isQuery) {
            HandleActivityDestroyedDoSomeThing.handleFaceGroupDestroyed(jid);
        }
        unregisterReceiver(UpdateReceiver);
        super.onDestroy();
    }

    private void initActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
    }

    private void initStep1() {
        iv1 = findViewById(R.id.ni_1_iv);
        iv2 = findViewById(R.id.ni_2_iv);
        iv3 = findViewById(R.id.ni_3_iv);
        iv4 = findViewById(R.id.ni_4_iv);
        tv1 = findViewById(R.id.ni_1_tv);
        tv2 = findViewById(R.id.ni_2_tv);
        tv3 = findViewById(R.id.ni_3_tv);
        tv4 = findViewById(R.id.ni_4_tv);

        findViewById(R.id.n_0_tv).setOnClickListener(this);
        findViewById(R.id.n_1_tv).setOnClickListener(this);
        findViewById(R.id.n_2_tv).setOnClickListener(this);
        findViewById(R.id.n_3_tv).setOnClickListener(this);
        findViewById(R.id.n_4_tv).setOnClickListener(this);
        findViewById(R.id.n_5_tv).setOnClickListener(this);
        findViewById(R.id.n_6_tv).setOnClickListener(this);
        findViewById(R.id.n_7_tv).setOnClickListener(this);
        findViewById(R.id.n_8_tv).setOnClickListener(this);
        findViewById(R.id.n_9_tv).setOnClickListener(this);
        findViewById(R.id.n_back_tv).setOnClickListener(this);
    }

    private void initStep2() {
        tvResult1 = findViewById(R.id.ni_1_tv_result);
        tvResult2 = findViewById(R.id.ni_2_tv_result);
        tvResult3 = findViewById(R.id.ni_3_tv_result);
        tvResult4 = findViewById(R.id.ni_4_tv_result);
        mGridView = findViewById(R.id.join_gd);
        mGridViewAdapter = new GridViewAdapter();
        mGridView.setAdapter(mGridViewAdapter);

        a_sure_btn = findViewById(R.id.a_sure_btn);
        ButtonColorChange.colorChange(this, a_sure_btn);

        a_sure_btn.setOnClickListener(v -> {
            Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), jid);
            if (friend != null) {
                goRoom(friend);
                finish();
            } else {
                joinSignalGroup();
            }
        });
    }

    private void goRoom(Friend friend) {
        Intent intent = new Intent(mContext, MucChatActivity.class);
        intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
        intent.putExtra(AppConstant.EXTRA_NICK_NAME, friend.getNickName());
        intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
        startActivity(intent);
        if (friend.getUnReadNum() > 0) {// 如该群组未读消息数量大于1, 刷新MessageFragment
            MsgBroadcast.broadcastMsgNumReset(mContext);
            MsgBroadcast.broadcastMsgUiUpdate(mContext);
        }
    }

    @Override
    public void onClick(View v) {
        if (mCurrentIndex == 4) {
            return;
        }
        switch (v.getId()) {
            case R.id.n_0_tv:
                show(false, 0);
                break;
            case R.id.n_1_tv:
                show(false, 1);
                break;
            case R.id.n_2_tv:
                show(false, 2);
                break;
            case R.id.n_3_tv:
                show(false, 3);
                break;
            case R.id.n_4_tv:
                show(false, 4);
                break;
            case R.id.n_5_tv:
                show(false, 5);
                break;
            case R.id.n_6_tv:
                show(false, 6);
                break;
            case R.id.n_7_tv:
                show(false, 7);
                break;
            case R.id.n_8_tv:
                show(false, 8);
                break;
            case R.id.n_9_tv:
                show(false, 9);
                break;
            case R.id.n_back_tv:
                show(true, 0);
                break;
        }
    }

    public void show(boolean isBack, int inputNum) {
        if (isBack && mCurrentIndex == 0) {
            return;
        }
        mCurrentIndex = isBack ? mCurrentIndex - 1 : mCurrentIndex + 1;

        if (isBack) {
            if (mCurrentIndex == 0) {
                iv1.setVisibility(View.VISIBLE);
                tv1.setVisibility(View.INVISIBLE);
            } else if (mCurrentIndex == 1) {
                iv2.setVisibility(View.VISIBLE);
                tv2.setVisibility(View.INVISIBLE);
            } else if (mCurrentIndex == 2) {
                iv3.setVisibility(View.VISIBLE);
                tv3.setVisibility(View.INVISIBLE);
            } else if (mCurrentIndex == 3) {
                iv4.setVisibility(View.VISIBLE);
                tv4.setVisibility(View.INVISIBLE);
            }
        } else {
            if (mCurrentIndex == 1) {
                iv1.setVisibility(View.INVISIBLE);
                tv1.setText(String.valueOf(inputNum));
                tv1.setVisibility(View.VISIBLE);
            } else if (mCurrentIndex == 2) {
                iv2.setVisibility(View.INVISIBLE);
                tv2.setText(String.valueOf(inputNum));
                tv2.setVisibility(View.VISIBLE);
            } else if (mCurrentIndex == 3) {
                iv3.setVisibility(View.INVISIBLE);
                tv3.setText(String.valueOf(inputNum));
                tv3.setVisibility(View.VISIBLE);
            } else if (mCurrentIndex == 4) {
                iv4.setVisibility(View.INVISIBLE);
                tv4.setText(String.valueOf(inputNum));
                tv4.setVisibility(View.VISIBLE);
            }
        }

        if (mCurrentIndex == 4) {
            tvResult1.setText(tv1.getText());
            tvResult2.setText(tv2.getText());
            tvResult3.setText(tv3.getText());
            tvResult4.setText(tv4.getText());

            Animation animation1 = AnimationUtils.loadAnimation(mContext, R.anim.translate_dialog_out);
            Animation animation2 = AnimationUtils.loadAnimation(mContext, R.anim.translate_dialog_in);
            findViewById(R.id.ll_step1).setAnimation(animation1);
            findViewById(R.id.ll_step2).setAnimation(animation2);
            findViewById(R.id.ll_step1).setVisibility(View.GONE);
            findViewById(R.id.ll_step2).setVisibility(View.VISIBLE);

            mSignal = tvResult1.getText().toString() + tvResult2.getText().toString() + tvResult3.getText().toString() + tvResult4.getText().toString();
            querySignalGroup();
        }
    }

    private void querySignalGroup() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("longitude", String.valueOf(longitude));
        params.put("latitude", String.valueOf(latitude));
        params.put("password", mSignal);
        params.put("isQuery", isQuery ? String.valueOf(1) : String.valueOf(0));

        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_LOCATION_QUERY)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {

                    @Override
                    public void onResponse(ObjectResult<MucRoom> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            isQuery = true;
                            jid = result.getData().getJid();
                            mMucRoomMemberList = result.getData().getMembers();
                            mGridViewAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(mContext, result.getResultMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    private void joinSignalGroup() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("jid", jid);

        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_LOCATION_JOIN)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {

                    @Override
                    public void onResponse(ObjectResult<MucRoom> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            // 服务端代发907 ，在XChatManagerListener内接收处理，处理完成之后发送广播通知该界面，本地就不建群了
                        } else {
                            DialogHelper.dismissProgressDialog();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    class GridViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mMucRoomMemberList.size();
        }

        @Override
        public Object getItem(int position) {
            return mMucRoomMemberList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_room_info_view, parent, false);
                GridViewHolder gridViewHolder = new GridViewHolder(convertView);
                convertView.setTag(gridViewHolder);
            }
            GridViewHolder gridViewHolder = (GridViewHolder) convertView.getTag();
            MucRoomMember mucRoomMember = mMucRoomMemberList.get(position);
            if (mucRoomMember != null) {
                AvatarHelper.getInstance().displayAvatar(mucRoomMember.getNickName(), mucRoomMember.getUserId(), gridViewHolder.iv, true);
                gridViewHolder.tv.setText(mucRoomMember.getNickName());
            }
            return convertView;
        }
    }

    class GridViewHolder {
        ImageView iv;
        TextView tv;

        GridViewHolder(View itemView) {
            iv = itemView.findViewById(R.id.content);
            tv = itemView.findViewById(R.id.member_name);
        }
    }
}
