package com.sk.weichat.ui.search;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.message.ChatActivity;
import com.sk.weichat.ui.message.MucChatActivity;
import com.sk.weichat.ui.message.search.SearchChatHistoryActivity;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.view.HeadView;

import java.util.ArrayList;
import java.util.List;

class ChatHistorySearchResultAdapter extends SearchResultAdapter<ChatHistorySearchResultAdapter.ViewHolder, ChatHistorySearchResultAdapter.Item> {
    private static final int TYPE_SINGLE = 1;
    private static final int TYPE_NORMAL = 2;
    private Context ctx;
    private String ownerId;
    private List<Friend> mFriendList;

    ChatHistorySearchResultAdapter(Context ctx, String ownerId, OnSearchResultClickListener listener) {
        this(ctx, ownerId, DEFAULT_ITEM_COUNT_LIMIT, listener);
    }

    @SuppressWarnings("WeakerAccess")
        // 单类型搜索结果页反射到用，
    ChatHistorySearchResultAdapter(Context ctx, String ownerId, int itemCountLimit, OnSearchResultClickListener listener) {
        super(itemCountLimit, listener);
        this.ctx = ctx;
        this.ownerId = ownerId;
    }

    @Override
    public int getSearchType() {
        return R.string.chat_history;
    }

    @Override
    public List<Item> realSearch(String str) throws Exception {
        List<Item> data = new ArrayList<>();
        // 聊天记录搜索保留旧代码,
        if (mFriendList == null) {
            mFriendList = FriendDao.getInstance().getNearlyFriendMsg(ownerId);
        }
        for (int i = 0; i < mFriendList.size(); i++) {
            Friend friend = mFriendList.get(i);
            List<Friend> friends = ChatMessageDao.getInstance().queryChatMessageByContent(friend, str);
            if (friends != null && friends.size() > 0) {
                Friend f = friends.get(0);
                int count = friends.size();
                Item item = new Item();
                item.friend = f;
                item.count = count;
                data.add(item);
            }
        }
        return data;
    }

    @Override
    public int getItemViewType(int position) {
        Item item = data.get(position);
        if (item.count == 1) {
            return TYPE_SINGLE;
        } else {
            return TYPE_NORMAL;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == TYPE_NORMAL) {
            View itemView = LayoutInflater.from(ctx).inflate(R.layout.item_search_result_chat_history, viewGroup, false);
            return new ViewHolderNormal(itemView);
        } else if (i == TYPE_SINGLE) {
            View itemView = LayoutInflater.from(ctx).inflate(R.layout.item_search_result_single_chat_history, viewGroup, false);
            return new ViewHolderSingle(itemView);
        } else {
            throw new IllegalStateException("unkown viewType " + i);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Item item = data.get(i);
        viewHolder.bind(item);
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bind(Item item);
    }

    class ViewHolderNormal extends ViewHolder {
        HeadView ivAvatar = itemView.findViewById(R.id.ivAvatar);
        TextView tvName = itemView.findViewById(R.id.tvName);
        TextView tvContent = itemView.findViewById(R.id.tvContent);

        ViewHolderNormal(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void bind(Item item) {
            itemView.setOnClickListener(v -> {
                Friend friend = item.friend;
                SearchChatHistoryActivity.start(ctx, friend.getUserId(), friend.getRoomFlag() != 1, searchKey);
            });
            AvatarHelper.getInstance().displayAvatar(ownerId, item.friend, ivAvatar);
            tvName.setText(item.friend.getShowName());
            tvContent.setText(ctx.getString(R.string.search_result_reason_chat_history, item.count));
        }
    }

    class ViewHolderSingle extends ViewHolder {
        HeadView ivAvatar = itemView.findViewById(R.id.avatar_img);
        TextView tvName = itemView.findViewById(R.id.nick_name_tv);
        TextView tvContent = itemView.findViewById(R.id.content_tv);
        TextView tvTime = itemView.findViewById(R.id.time_tv);

        ViewHolderSingle(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void bind(Item item) {
            itemView.setOnClickListener(v -> {
                Friend friend = item.friend;
                Intent intent;
                if (friend.getRoomFlag() != 1) {
                    intent = new Intent(ctx, ChatActivity.class);
                    intent.putExtra(ChatActivity.FRIEND, friend);
                } else {
                    intent = new Intent(ctx, MucChatActivity.class);
                    intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                    intent.putExtra(AppConstant.EXTRA_NICK_NAME, friend.getNickName());
                }
                intent.putExtra("isserch", true);
                intent.putExtra("jilu_id", friend.getChatRecordTimeOut());
                ctx.startActivity(intent);
                callOnSearchResultClickListener();
            });
            AvatarHelper.getInstance().displayAvatar(ownerId, item.friend, ivAvatar);
            tvName.setText(item.friend.getShowName());
            highlight(tvContent, item.friend.getContent());
            tvTime.setText(TimeUtils.getFriendlyTimeDesc(ctx, item.friend.getTimeSend()));
        }
    }

    class Item {
        Friend friend;
        int count;
    }
}

