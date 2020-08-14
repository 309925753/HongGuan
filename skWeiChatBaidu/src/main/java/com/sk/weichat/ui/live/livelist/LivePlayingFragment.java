package com.sk.weichat.ui.live.livelist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.AppConfig;
import com.sk.weichat.R;
import com.sk.weichat.broadcast.MucgroupUpdateUtil;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseGridFragment;
import com.sk.weichat.ui.live.LiveConstants;
import com.sk.weichat.ui.live.LivePlayingActivity;
import com.sk.weichat.ui.live.PushFlowActivity;
import com.sk.weichat.ui.live.bean.LiveRoom;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.UiUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 正在直播的列表
 */
public class LivePlayingFragment extends BaseGridFragment<LivePlayingFragment.LiveViewHolder> {
    private List<LiveRoom> mMucRoomS;
    private int mPageIndex = 0;
    private boolean isPullDwonToRefersh;
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MucgroupUpdateUtil.ACTION_UPDATE)) {
                requestData(true);
            }
        }
    };

    public LivePlayingFragment() {
        mMucRoomS = new ArrayList<>();
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        super.onActivityCreated(savedInstanceState, createView);
        if (createView) {
            initView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mUpdateReceiver);
    }

    public void initView() {
        getActivity().registerReceiver(mUpdateReceiver, MucgroupUpdateUtil.getUpdateActionFilter());
    }

    @Override
    public void initDatas(int pager) {
        if (pager == 0) {
            isPullDwonToRefersh = true;
        } else {
            isPullDwonToRefersh = false;
        }
        requestData(isPullDwonToRefersh);
    }

    // 获取正在直播的直播间列表
    private void requestData(final boolean isPullDwonToRefersh) {
        if (isPullDwonToRefersh) {
            mPageIndex = 0;
        }
        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", String.valueOf(mPageIndex));
        params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));
        params.put("status", "1");
        HttpUtils.get().url(coreManager.getConfig().GET_LIVE_ROOM_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<LiveRoom>(LiveRoom.class) {
                    @Override
                    public void onResponse(ArrayResult<LiveRoom> result) {
                        if (getActivity() == null) {
                            // 页面已经被关闭了就不操作了，
                            return;
                        }
                        mPageIndex++;
                        if (isPullDwonToRefersh) {
                            mMucRoomS.clear();
                        }
                        List<LiveRoom> data = result.getData();
                        if (data != null && data.size() > 0) {
                            mMucRoomS.addAll(data);
                        }
                        update(mMucRoomS);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }

    @Override
    public LiveViewHolder initHolder(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.row_live_room, parent, false);
        return new LiveViewHolder(v);
    }

    @Override
    public void fillData(LiveViewHolder holder, int position) {
        LiveRoom room = mMucRoomS.get(position);
        holder.tv1.setText(room.getName());
        holder.tv2.setText(room.getNickName());
        holder.tv3.setText(String.valueOf(room.getNumbers()));
        AvatarHelper.getInstance().displayAvatar(String.valueOf(room.getUserId()), holder.live_default, false);
        AvatarHelper.getInstance().displayAvatar(room.getNickName(), String.valueOf(room.getUserId()), holder.avatar_img, false);
    }

    private void onItemClick(View view, int position) {
        LiveRoom room = mMucRoomS.get(position);
        if (UiUtils.isNormalClick(view)) {// 防止过快点击
            if (String.valueOf(room.getUserId()).equals(coreManager.getSelf().getUserId())) {// 开启直播
                if (room.getStatus() != 0) {
                    DialogHelper.tip(getActivity(), getString(R.string.tip_live_room_online));
                } else {
                    gotoLiveRoom(room, true);
                }
            } else { // 进入直播间
                gotoLiveRoom(room, false);
            }
        }
    }

    private void gotoLiveRoom(final LiveRoom room, final boolean liver) {
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", room.getRoomId());
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("status", "1");

        HttpUtils.get().url(coreManager.getConfig().JOIN_LIVE_ROOM)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            if (liver) {
                                Intent intent = new Intent(getActivity(), PushFlowActivity.class);
                                intent.putExtra(LiveConstants.LIVE_PUSH_FLOW_URL, room.getUrl());
                                intent.putExtra(LiveConstants.LIVE_ROOM_ID, room.getRoomId());
                                intent.putExtra(LiveConstants.LIVE_CHAT_ROOM_ID, room.getJid());
                                intent.putExtra(LiveConstants.LIVE_ROOM_NAME, room.getName());
                                intent.putExtra(LiveConstants.LIVE_ROOM_PERSON_ID, String.valueOf(room.getUserId()));
                                intent.putExtra(LiveConstants.LIVE_ROOM_NOTICE, room.getNotice());
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(getActivity(), LivePlayingActivity.class);
                                intent.putExtra(LiveConstants.LIVE_GET_FLOW_URL, room.getUrl());
                                intent.putExtra(LiveConstants.LIVE_ROOM_ID, room.getRoomId());
                                intent.putExtra(LiveConstants.LIVE_CHAT_ROOM_ID, room.getJid());
                                intent.putExtra(LiveConstants.LIVE_ROOM_NAME, room.getName());
                                intent.putExtra(LiveConstants.LIVE_ROOM_PERSON_ID, String.valueOf(room.getUserId()));
                                intent.putExtra(LiveConstants.LIVE_STATUS, room.getStatus());
                                intent.putExtra(LiveConstants.LIVE_ROOM_NOTICE, room.getNotice());
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.kicked_not_in), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }

    class LiveViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout ll_live_child;
        private final ImageView avatar_img;
        private final ImageView live_default;
        private TextView tv1;
        private TextView tv2;
        private TextView tv3;

        public LiveViewHolder(@NonNull View itemView) {
            super(itemView);
            ll_live_child = itemView.findViewById(R.id.ll_live_child);
            live_default = itemView.findViewById(R.id.live_default);
            avatar_img = itemView.findViewById(R.id.live_avatar_img);

            tv1 = itemView.findViewById(R.id.live_title);
            tv2 = itemView.findViewById(R.id.live_nick_name);
            tv3 = itemView.findViewById(R.id.tv_online_people);
            ll_live_child.setOnClickListener(v -> onItemClick(v, getLayoutPosition()));
        }
    }
}
