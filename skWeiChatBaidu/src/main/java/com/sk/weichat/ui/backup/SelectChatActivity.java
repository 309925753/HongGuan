package com.sk.weichat.ui.backup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.event.EventSentChatHistory;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.EventBusHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class SelectChatActivity extends BaseActivity implements ChatAdapter.OnItemSelectedChangeListener {

    private RecyclerView rvChatList;
    private ChatAdapter chatAdapter;
    private TextView tvSelectedCount;
    private Set<String> selectedUserIdList = new HashSet<>();
    private View llSelectedCount;
    private TextView btnSelectAll;
    private View btnSelectFinish;

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, SelectChatActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_chat);

        initActionBar();
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
            Reporter.post("查询存在聊天记录的好友失败", r);
        }, c -> {
            List<Friend> chatFriendList = FriendDao.getInstance().getChatFriendList(coreManager.getSelf().getUserId());
            List<ChatAdapter.Item> data = new ArrayList<>(chatFriendList.size());
            for (Friend friend : chatFriendList) {
                data.add(ChatAdapter.Item.fromFriend(friend));
            }
            c.uiThread(r -> {
                chatAdapter.setData(data);
                btnSelectAll.setEnabled(true);
            });
        });
    }

    private void initView() {
        btnSelectFinish = findViewById(R.id.btnSelectFinish);
        ButtonColorChange.colorChange(this, btnSelectFinish);
        btnSelectFinish.setOnClickListener((v) -> {
            SendChatHistoryActivity.start(this, selectedUserIdList);
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
        tvSelectedCount.setText(getString(R.string.migrate_chat_count_place_holder, selectedUserIdList.size()));
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
        tvTitle.setText(getString(R.string.select_chat_history));
    }

    @Override
    public void onItemSelectedChange(ChatAdapter.Item item, boolean isSelected) {
        Log.i(TAG, "checked change " + isSelected + ", " + item);
        if (item.selected) {
            selectedUserIdList.add(item.getUserId());
        } else {
            selectedUserIdList.remove(item.getUserId());
        }
        updateSelectedCount();
    }

    private void updateSelectedCount() {
        if (selectedUserIdList.isEmpty()) {
            btnSelectFinish.setEnabled(false);
            tvSelectedCount.setText(getString(R.string.migrate_chat_count_place_holder, selectedUserIdList.size()));
        } else {
            btnSelectFinish.setEnabled(true);
            tvSelectedCount.setText(getString(R.string.migrate_chat_count_place_holder, selectedUserIdList.size()));
        }
        if (selectedUserIdList.size() == chatAdapter.getItemCount()) {
            btnSelectAll.setText(R.string.cancel);
        } else {
            btnSelectAll.setText(R.string.select_all);
        }
    }
}
