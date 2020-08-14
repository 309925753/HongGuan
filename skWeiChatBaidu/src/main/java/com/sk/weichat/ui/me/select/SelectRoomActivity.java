package com.sk.weichat.ui.me.select;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.RoomMember;
import com.sk.weichat.bean.SelectFriendItem;
import com.sk.weichat.bean.event.EventSentChatHistory;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.RoomMemberDao;
import com.sk.weichat.ui.backup.ChatAdapter;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.EventBusHelper;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class SelectRoomActivity extends BaseActivity implements ChatAdapter.OnItemSelectedChangeListener {

    private RecyclerView rvChatList;
    private ChatAdapter chatAdapter;
    private TextView tvSelectedCount;
    private Set<SelectFriendItem> selectedUserIdList = new HashSet<>();
    private View llSelectedCount;
    private TextView btnSelectAll;
    private View btnSelectFinish;

    public static void start(Activity ctx, int requestCode, List<SelectFriendItem> mItemList) {
        Intent intent = new Intent(ctx, SelectRoomActivity.class);
        if (mItemList != null && mItemList.size() > 0) {
            intent.putExtra("SELECTED_ITEMS", JSON.toJSONString(mItemList));
        }
        ctx.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_chat);

        initActionBar();

        String sSelectedList = getIntent().getStringExtra("SELECTED_ITEMS");
        if (!TextUtils.isEmpty(sSelectedList)) {
            selectedUserIdList.addAll(JSON.parseArray(sSelectedList, SelectFriendItem.class));
        }

        initView();

        initData();
        EventBusHelper.register(this);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventSentChatHistory message) {
        finish();
    }

    private void initData() {
        AsyncUtils.doAsync(this, r -> {
            Reporter.post("查询群聊失败", r);
        }, c -> {
            List<Friend> chatFriendList = FriendDao.getInstance().getAllRooms(coreManager.getSelf().getUserId());
            List<Friend> removeChatFriendList = new ArrayList<>();
            for (Friend friend : chatFriendList) {
                if (!isAllow(friend)) {
                    removeChatFriendList.add(friend);
                }
            }
            if (removeChatFriendList.size() > 0) {
                chatFriendList.removeAll(removeChatFriendList);
                c.uiThread(selectRoomActivity -> ToastUtil.showToast(mContext, getString(R.string.remove_some_group)));
            }
            List<ChatAdapter.Item> data = new ArrayList<>(chatFriendList.size());
            for (Friend friend : chatFriendList) {
                ChatAdapter.Item item = ChatAdapter.Item.fromFriend(friend);
                if (selectedUserIdList.contains(new SelectFriendItem(item.friend.getUserId(), item.friend.getShowName(), item.friend.getRoomFlag()))) {
                    item.selected = true;
                }
                data.add(item);
            }
            c.uiThread(r -> {
                chatAdapter.setData(data);
                btnSelectAll.setEnabled(true);
                updateSelectedCount();
            });
        });
    }

    private void initView() {
        btnSelectFinish = findViewById(R.id.btnSelectFinish);
        ButtonColorChange.colorChange(this, btnSelectFinish);
        btnSelectFinish.setOnClickListener((v) -> {
            Intent intent = new Intent();
            intent.putExtra("SELECTED_ITEMS", JSON.toJSONString(selectedUserIdList));
            setResult(RESULT_OK, intent);
            finish();
        });

        btnSelectAll = findViewById(R.id.tv_title_right);
        btnSelectAll.setText(R.string.select_all);
        btnSelectAll.setOnClickListener((v) -> {
            if (selectedUserIdList.size() == chatAdapter.getItemCount()) {
                chatAdapter.cancelAll();
            } else {
                chatAdapter.selectAll();
            }
        });
        ButtonColorChange.textChange(this, btnSelectAll);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        tvSelectedCount.setText(getString(R.string.room_count_place_holder, selectedUserIdList.size()));
        ButtonColorChange.textChange(this, tvSelectedCount);
        rvChatList = findViewById(R.id.rvChatList);
        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(this, coreManager.getSelf().getUserId());
        rvChatList.setAdapter(chatAdapter);
    }

    private void initActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        findViewById(R.id.iv_title_left).setOnClickListener((v) -> {
            onBackPressed();
        });
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.select_room));
    }

    @Override
    public void onItemSelectedChange(ChatAdapter.Item item, boolean isSelected) {
        Log.i(TAG, "checked change " + isSelected + ", " + item);
        if (item.selected) {
            selectedUserIdList.add(new SelectFriendItem(item.friend.getUserId(), item.friend.getShowName(), 1));
        } else {
            selectedUserIdList.remove(new SelectFriendItem(item.friend.getUserId(), item.friend.getShowName(), 1));
        }
        updateSelectedCount();
    }

    private void updateSelectedCount() {
        if (selectedUserIdList.isEmpty()) {
            btnSelectFinish.setEnabled(false);

            tvSelectedCount.setText(getString(R.string.room_count_place_holder, selectedUserIdList.size()));
        } else {
            btnSelectFinish.setEnabled(true);
            tvSelectedCount.setText(getString(R.string.room_count_place_holder, selectedUserIdList.size()));
        }
        if (selectedUserIdList.size() == chatAdapter.getItemCount()) {
            btnSelectAll.setText(R.string.cancel);
        } else {
            btnSelectAll.setText(R.string.select_all);
        }
    }

    private boolean isAllow(Friend friend) {
        if (friend.getIsLostChatKeyGroup() == 1) {
            return false;
        }
        // 用户可能不在群组里，
        int status = friend.getGroupStatus();
        if (1 == status) {
            // ToastUtil.showToast(mContext, R.string.tip_been_kick);
            return false;
        } else if (2 == status) {
            // ToastUtil.showToast(mContext, R.string.tip_disbanded);
            return false;
        } else if (3 == status) {
            // ToastUtil.showToast(mContext, R.string.tip_group_disable_by_service);
            return false;
        }
        RoomMember mRoomMember = RoomMemberDao.getInstance().getSingleRoomMember(friend.getRoomId(), coreManager.getSelf().getUserId());
        // 判断禁言状态，
        if (mRoomMember != null && mRoomMember.getRole() == 3) {// 普通成员需要判断是否被禁言
            if (friend.getRoomTalkTime() > (System.currentTimeMillis() / 1000)) {
                // ToastUtil.showToast(mContext, getString(R.string.has_been_banned));
                return false;
            }
        } else if (mRoomMember == null) {// 也需要判断是否被禁言
            if (friend.getRoomTalkTime() > (System.currentTimeMillis() / 1000)) {
                // ToastUtil.showToast(mContext, getString(R.string.has_been_banned));
                return false;
            }
        }
        if (PreferenceUtils.getBoolean(mContext,
                Constants.GROUP_ALL_SHUP_UP + friend.getUserId(), false)) {// 全体禁言
            // ToastUtil.showToast(mContext, R.string.has_been_banned);
            return false;
        }
        if (mRoomMember != null && mRoomMember.getRole() == 4) {
            // ToastUtil.showToast(mContext, R.string.hint_invisible);
            return false;
        }
        return true;
    }
}
