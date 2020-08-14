package com.sk.weichat.ui.search;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.pay.sk.SKPayActivity;
import com.sk.weichat.ui.contacts.NewFriendActivity;
import com.sk.weichat.ui.message.ChatActivity;

import java.util.ArrayList;
import java.util.List;

class ContactsSearchResultAdapter extends SearchResultAdapter<ContactsSearchResultAdapter.ViewHolder, ContactsSearchResultAdapter.Item> {
    private Context ctx;
    private String ownerId;

    ContactsSearchResultAdapter(Context ctx, String ownerId, OnSearchResultClickListener listener) {
        this(ctx, ownerId, DEFAULT_ITEM_COUNT_LIMIT, listener);
    }

    @SuppressWarnings("WeakerAccess")
        // 单类型搜索结果页反射到用，
    ContactsSearchResultAdapter(Context ctx, String ownerId, int itemCountLimit, OnSearchResultClickListener listener) {
        super(itemCountLimit, listener);
        this.ctx = ctx;
        this.ownerId = ownerId;
    }

    @Override
    public int getSearchType() {
        return R.string.search_result_contacts;
    }

    @Override
    public List<Item> realSearch(String str) throws Exception {
        List<Item> data = new ArrayList<>();
        List<Friend> friendList = FriendDao.getInstance().searchFriend(ownerId, str);
        for (Friend friend : friendList) {
            if (!friend.getNickName().contains(str) && friend.getRemarkName().contains(str)) {
                Item item = new Item();
                item.friend = friend;
                item.reason = ctx.getString(R.string.search_result_reason_remark, friend.getRemarkName());
                data.add(item);
            } else {
                Item item = new Item();
                item.friend = friend;
                data.add(item);
            }
        }
        return data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(ctx).inflate(R.layout.item_search_result_contacts, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Item item = data.get(i);
        viewHolder.bind(item);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar = itemView.findViewById(R.id.ivAvatar);
        TextView tvName = itemView.findViewById(R.id.tvName);
        TextView tvContent = itemView.findViewById(R.id.tvContent);

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(Item item) {
            itemView.setOnClickListener(v -> {
                if (TextUtils.equals(item.friend.getUserId(), Friend.ID_NEW_FRIEND_MESSAGE)) {
                    ctx.startActivity(new Intent(ctx, NewFriendActivity.class));
                } else if (TextUtils.equals(item.friend.getUserId(), Friend.ID_SK_PAY)) {
                    ctx.startActivity(new Intent(ctx, SKPayActivity.class));
                } else {
                    Intent intent = new Intent(ctx, ChatActivity.class);
                    intent.putExtra(ChatActivity.FRIEND, item.friend);
                    ctx.startActivity(intent);
                }
                callOnSearchResultClickListener();
            });
            AvatarHelper.getInstance().displayAvatar(item.friend.getNickName(), item.friend.getUserId(), ivAvatar, true);
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

