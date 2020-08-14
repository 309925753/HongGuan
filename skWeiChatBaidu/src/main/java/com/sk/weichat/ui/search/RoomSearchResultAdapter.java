package com.sk.weichat.ui.search;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.RoomMember;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.RoomMemberDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.message.MucChatActivity;
import com.sk.weichat.view.HeadView;

import java.util.ArrayList;
import java.util.List;

class RoomSearchResultAdapter extends SearchResultAdapter<RoomSearchResultAdapter.ViewHolder, RoomSearchResultAdapter.Item> {
    private Context ctx;
    private String ownerId;
    private List<Friend> mFriendList;

    RoomSearchResultAdapter(Context ctx, String ownerId, OnSearchResultClickListener listener) {
        this(ctx, ownerId, DEFAULT_ITEM_COUNT_LIMIT, listener);
    }

    @SuppressWarnings("WeakerAccess")
        // 单类型搜索结果页反射到用，
    RoomSearchResultAdapter(Context ctx, String ownerId, int itemCountLimit, OnSearchResultClickListener listener) {
        super(itemCountLimit, listener);
        this.ctx = ctx;
        this.ownerId = ownerId;
    }

    @Override
    public int getSearchType() {
        return R.string.search_result_room;
    }

    @Override
    public List<Item> realSearch(String str) throws Exception {
        List<Item> data = new ArrayList<>();
        if (mFriendList == null) {
            mFriendList = FriendDao.getInstance().getAllRooms(ownerId);
        }
        for (Friend friend : mFriendList) {
            if (friend.getNickName().contains(str)) {
                Item item = new Item();
                item.friend = friend;
                data.add(item);
                continue;
            }
            RoomMember roomMember = RoomMemberDao.getInstance().searchMemberContains(friend, str);
            if (roomMember != null) {
                Item item = new Item();
                item.friend = friend;
                item.reason = ctx.getString(R.string.search_result_reason_contains, roomMember.getUserName());
                data.add(item);
            }
        }
        return data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(ctx).inflate(R.layout.item_search_result_room, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Item item = data.get(i);
        viewHolder.bind(item);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        HeadView ivAvatar = itemView.findViewById(R.id.ivAvatar);
        TextView tvName = itemView.findViewById(R.id.tvName);
        TextView tvContent = itemView.findViewById(R.id.tvContent);

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(Item item) {
            itemView.setOnClickListener(v -> {
                Friend friend = item.friend;
                Intent intent = new Intent(ctx, MucChatActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                intent.putExtra(AppConstant.EXTRA_NICK_NAME, friend.getNickName());
                intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
                ctx.startActivity(intent);
                callOnSearchResultClickListener();
            });
            AvatarHelper.getInstance().displayAvatar(ownerId, item.friend, ivAvatar);
            highlight(tvName, item.friend.getNickName());
            if (!TextUtils.isEmpty(item.reason)) {
                tvContent.setVisibility(View.VISIBLE);
                highlight(tvContent, item.reason);
            } else {
                tvContent.setVisibility(View.GONE);
            }
        }
    }

    class Item {
        Friend friend;
        String reason;
    }
}

