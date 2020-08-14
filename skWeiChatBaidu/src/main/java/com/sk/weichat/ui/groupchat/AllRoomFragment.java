package com.sk.weichat.ui.groupchat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sk.weichat.AppConfig;
import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.RoomMember;
import com.sk.weichat.bean.event.EventCreateGroupFriend;
import com.sk.weichat.bean.event.EventSendVerifyMsg;
import com.sk.weichat.bean.message.MucRoom;
import com.sk.weichat.broadcast.MucgroupUpdateUtil;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.RoomMemberDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.EasyFragment;
import com.sk.weichat.ui.message.MucChatActivity;
import com.sk.weichat.ui.message.multi.RoomInfoActivity;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.ViewHolder;
import com.sk.weichat.view.VerifyDialog;
import com.sk.weichat.view.circularImageView.CircularImageVIew;
import com.sk.weichat.xmpp.XmppConnectionManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

public class AllRoomFragment extends EasyFragment {
    private PullToRefreshListView mPullToRefreshListView;
    private List<MucRoom> mMucRooms;
    private MucRoomAdapter mAdapter;
    private int mPageIndex = 0;
    private String roomName = null;
    private boolean mNeedUpdate = true;
    private List<RoomMember> memberS;
    private List<String> urlS;
    private String mLoginUserId;
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MucgroupUpdateUtil.ACTION_UPDATE)) {
                if (isResumed()) {
                    requestData(true);
                } else {
                    mNeedUpdate = true;
                }
            }
        }
    };

    public AllRoomFragment() {
        mMucRooms = new ArrayList<>();
        mAdapter = new MucRoomAdapter();
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.layout_pullrefresh_list_os;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            roomName = getActivity().getIntent().getStringExtra("roomName");
            initView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 发送广播获取数据
        MucgroupUpdateUtil.broadcastUpdateUi(getActivity());
        if (mNeedUpdate) {
            mNeedUpdate = false;
            mPullToRefreshListView.post(new Runnable() {
                @Override
                public void run() {
                    mPullToRefreshListView.setPullDownRefreshing(200);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mUpdateReceiver);
        super.onDestroy();
    }

    @SuppressLint("InflateParams")
    private void initView() {
        mLoginUserId = coreManager.getSelf().getUserId();

        memberS = new ArrayList<>();
        urlS = new ArrayList<>();

        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        View emptyView = LayoutInflater.from(getActivity()).inflate(
                R.layout.layout_list_empty_view, null);
        mPullToRefreshListView.setAdapter(mAdapter);
        mPullToRefreshListView.setEmptyView(emptyView);
        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                requestData(true);
            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                requestData(false);
            }
        });

        mPullToRefreshListView.getRefreshableView().setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        if (XmppConnectionManager.mXMPPCurrentState == 0 || XmppConnectionManager.mXMPPCurrentState == 1) {
                            Toast.makeText(getActivity(), R.string.tip_xmpp_connecting, Toast.LENGTH_SHORT).show();
                        } else if (XmppConnectionManager.mXMPPCurrentState == 2) {
                            final MucRoom mucRoom = mMucRooms.get((int) id);
                            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, mucRoom.getJid());
                            if (friend != null) {
                                if (friend.getGroupStatus() == 0) {
                                    interMucChat(mucRoom.getJid(), mucRoom.getName());
                                    return;
                                } else {// 已被踢出该群组 || 群组已被解散
                                    FriendDao.getInstance().deleteFriend(mLoginUserId, friend.getUserId());
                                    ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, friend.getUserId());
                                }
                            }

                            if (mucRoom.getIsNeedVerify() == 1) {
                                VerifyDialog verifyDialog = new VerifyDialog(getActivity());
                                verifyDialog.setVerifyClickListener(MyApplication.getInstance().getString(R.string.tip_reason_invite_friends), new VerifyDialog.VerifyClickListener() {
                                    @Override
                                    public void cancel() {

                                    }

                                    @Override
                                    public void send(String str) {
                                        EventBus.getDefault().post(new EventSendVerifyMsg(mucRoom.getUserId(), mucRoom.getJid(), str));
                                    }
                                });
                                verifyDialog.show();
                                return;
                            }
                            joinRoom(mucRoom, mLoginUserId);
                        } else {
                            Toast.makeText(getActivity(), R.string.tip_xmpp_offline, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        getActivity().registerReceiver(mUpdateReceiver, MucgroupUpdateUtil.getUpdateActionFilter());
    }

    private void requestData(final boolean isPullDwonToRefersh) {
        if (isPullDwonToRefersh) {
            mPageIndex = 0;
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageIndex", String.valueOf(mPageIndex));
        params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));
        if (!TextUtils.isEmpty(roomName)) {
            params.put("roomName", roomName);
        }

        HttpUtils.get().url(coreManager.getConfig().ROOM_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<MucRoom>(MucRoom.class) {
                    @Override
                    public void onResponse(ArrayResult<MucRoom> result) {
                        mPageIndex++;
                        if (isPullDwonToRefersh) {
                            mMucRooms.clear();
                        }
                        List<MucRoom> data = result.getData();
                        if (data != null && data.size() > 0) {
                            mMucRooms.addAll(data);
                        }
                        mAdapter.notifyDataSetChanged();
                        mPullToRefreshListView.onRefreshComplete();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getActivity());
                        mPullToRefreshListView.onRefreshComplete();
                    }
                });
    }

    private void joinRoom(final MucRoom room, final String loginUserId) {
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", room.getId());
        if (room.getUserId().equals(loginUserId))
            params.put("type", "1");
        else
            params.put("type", "2");

        MyApplication.mRoomKeyLastCreate = room.getJid();

        HttpUtils.get().url(coreManager.getConfig().ROOM_JOIN)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            EventBus.getDefault().post(new EventCreateGroupFriend(room));
                            mPullToRefreshListView.postDelayed(new Runnable() {
                                @Override
                                public void run() {// 给500ms的时间缓存，防止群组还未创建好就进入群聊天界面
                                    interMucChat(room.getJid(), room.getName());
                                }
                            }, 500);
                        } else {
                            MyApplication.mRoomKeyLastCreate = "compatible";
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(getActivity());
                        MyApplication.mRoomKeyLastCreate = "compatible";
                    }
                });
    }

    private void interMucChat(String roomJid, String roomName) {
        Intent intent = new Intent(getActivity(), MucChatActivity.class);
        intent.putExtra(AppConstant.EXTRA_USER_ID, roomJid);
        intent.putExtra(AppConstant.EXTRA_NICK_NAME, roomName);
        intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
        startActivity(intent);

        // 更新群组页面
        MucgroupUpdateUtil.broadcastUpdateUi(getActivity());
    }

    public boolean canSeeInfo(String userId) {
        boolean canSee = false;
        List<Friend> friends = FriendDao.getInstance().getAllRooms(mLoginUserId);
        for (int i = 0; i < friends.size(); i++) {
            Friend friend = friends.get(i);
            if (friend.getUserId().equals(userId)) {
                canSee = true;
            }
        }
        return canSee;
    }

    class MucRoomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mMucRooms.size();
        }

        @Override
        public Object getItem(int position) {
            return mMucRooms.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        @SuppressLint("SetTextI18n")
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.row_muc_room, parent, false);
            }
            CircularImageVIew avatar_img = ViewHolder.get(convertView, R.id.avatar_img);
            TextView nick_name_tv = ViewHolder.get(convertView, R.id.nick_name_tv);
            TextView content_tv = ViewHolder.get(convertView, R.id.content_tv);
            TextView time_tv = ViewHolder.get(convertView, R.id.time_tv);
            final MucRoom room = mMucRooms.get(position);

            memberS.clear();
            urlS.clear();
            memberS = RoomMemberDao.getInstance().getRoomMember(room.getId());
            if (memberS.size() > 0) {
                if (memberS.size() > 5) {
                    // 群组头像最多为5人组合
                    for (int i = 0; i < 5; i++) {
                        String avatarUrl = AvatarHelper.getAvatarUrl(memberS.get(i).getUserId(), true);
                        urlS.add(avatarUrl);
                    }
                    avatar_img.addUrl(urlS);
                } else {
                    for (int i = 0; i < memberS.size(); i++) {
                        String avatarUrl = AvatarHelper.getAvatarUrl(memberS.get(i).getUserId(), true);
                        urlS.add(avatarUrl);
                    }
                    avatar_img.addUrl(urlS);
                }
            } else {// 使用群组默认头像
                avatar_img.setImageResource(R.drawable.groupdefault);
            }

            nick_name_tv.setText(room.getName() + "(" + room.getUserSize() + "" + getString(R.string.people) + ")");
            content_tv.setText(room.getDesc());
            time_tv.setText(TimeUtils.getFriendlyTimeDesc(getActivity(),
                    (int) room.getCreateTime()));

            avatar_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (canSeeInfo(room.getJid())) {
                        Intent intent = new Intent(getActivity(), RoomInfoActivity.class);
                        intent.putExtra(AppConstant.EXTRA_USER_ID, room.getJid());
                        getActivity().startActivity(intent);
                    } else {
                        ToastUtil.showToast(getActivity(), getString(R.string.tip_not_member));
                    }

                }
            });
            return convertView;
        }
    }
}
