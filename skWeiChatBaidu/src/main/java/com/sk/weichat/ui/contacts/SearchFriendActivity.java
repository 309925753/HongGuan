package com.sk.weichat.ui.contacts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.message.ChatActivity;
import com.sk.weichat.ui.message.MucChatActivity;
import com.sk.weichat.util.CommonAdapter;
import com.sk.weichat.util.CommonViewHolder;
import com.sk.weichat.view.MessageAvatar;

import java.util.ArrayList;
import java.util.List;

public class SearchFriendActivity extends BaseActivity {
    private EditText mSearchEdit;

    private LinearLayout mContactLinearLayout;
    private ListView mContactListView;
    private ContactAdapter mContactAdapter;
    private List<Friend> mAllContacts;
    private List<Friend> mCurrentContacts = new ArrayList<>();

    private LinearLayout mRoomLinearLayout;
    private ListView mRoomListView;
    private RoomAdapter mRoomAdapter;
    private List<Friend> mAllRooms;
    private List<Friend> mCurrentRooms = new ArrayList<>();

    private String mLoginUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);
        initActionBar();
        loadData();
        initView();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        TextView mTvTitle = findViewById(R.id.tv_title_center);
//        mTvTitle.setText(getString(R.string.search_contact_group));

    }

    private void loadData() {
        mLoginUserId = coreManager.getSelf().getUserId();
        mAllContacts = FriendDao.getInstance().getAllFriends(mLoginUserId);
        mAllRooms = FriendDao.getInstance().getAllRooms(mLoginUserId);
    }

    private void initView() {
        mSearchEdit = findViewById(R.id.search_edit);

        mContactLinearLayout = findViewById(R.id.ll1);
        mContactListView = findViewById(R.id.lv1);
        mContactAdapter = new ContactAdapter(mContext, mCurrentContacts);
        mContactListView.setAdapter(mContactAdapter);

        mRoomLinearLayout = findViewById(R.id.ll2);
        mRoomListView = findViewById(R.id.lv2);
        mRoomAdapter = new RoomAdapter(mContext, mCurrentRooms);
        mRoomListView.setAdapter(mRoomAdapter);
    }

    private void initEvent() {
        mSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String mContent = mSearchEdit.getText().toString();
                mCurrentContacts.clear();
                mCurrentRooms.clear();
                if (TextUtils.isEmpty(mContent)) {
                    mContactLinearLayout.setVisibility(View.GONE);
                    mRoomLinearLayout.setVisibility(View.GONE);
                    mContactAdapter.notifyDataSetChanged();
                    mRoomAdapter.notifyDataSetChanged();
                    return;
                }

                for (int i = 0; i < mAllContacts.size(); i++) {
                    String str = TextUtils.isEmpty(mAllContacts.get(i).getRemarkName()) ? mAllContacts.get(i).getNickName() : mAllContacts.get(i).getRemarkName();
                    if (str.toLowerCase().contains(mContent.toLowerCase())) {
                        mCurrentContacts.add(mAllContacts.get(i));
                    }
                }

                for (int i = 0; i < mAllRooms.size(); i++) {
                    if (mAllRooms.get(i).getNickName().toLowerCase().contains(mContent.toLowerCase())) {
                        mCurrentRooms.add(mAllRooms.get(i));
                    }
                }

                if (mCurrentContacts.size() > 0) {
                    mContactLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    mContactLinearLayout.setVisibility(View.GONE);
                }

                if (mCurrentRooms.size() > 0) {
                    mRoomLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    mRoomLinearLayout.setVisibility(View.GONE);
                }

                mContactAdapter.notifyDataSetChanged();
                mRoomAdapter.notifyDataSetChanged();
            }
        });

        mContactListView.setOnItemClickListener((parent, view, position, id) -> {
            Friend friend = mCurrentContacts.get(position);
            if (friend != null) {
                Intent intent = new Intent(SearchFriendActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.FRIEND, friend);
                startActivity(intent);
            }
        });

        mRoomListView.setOnItemClickListener((parent, view, position, id) -> {
            Friend friend = mCurrentRooms.get(position);
            if (friend != null) {
                Intent intent = new Intent(SearchFriendActivity.this, MucChatActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                intent.putExtra(AppConstant.EXTRA_NICK_NAME, friend.getNickName());
                intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
                startActivity(intent);
            }
        });
    }

    class ContactAdapter extends CommonAdapter<Friend> {

        ContactAdapter(Context context, List<Friend> data) {
            super(context, data);
        }

        @Override
        @SuppressLint("SetTextI18n")
        public View getView(int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.row_device, position);

            ImageView iv = viewHolder.getView(R.id.device_ava);
            TextView tv = viewHolder.getView(R.id.device_name);

            Friend friend = data.get(position);
            if (friend != null) {
                AvatarHelper.getInstance().displayAvatar(friend.getUserId(), iv);
                tv.setText(TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName());
            }

            return viewHolder.getConvertView();
        }
    }

    class RoomAdapter extends CommonAdapter<Friend> {

        RoomAdapter(Context context, List<Friend> data) {
            super(context, data);
        }

        @Override
        @SuppressLint("SetTextI18n")
        public View getView(int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.row_search_group, position);

            MessageAvatar iv = viewHolder.getView(R.id.group_avatar);
            TextView tv = viewHolder.getView(R.id.group_name);

            Friend friend = data.get(position);
            if (friend != null) {
                iv.fillData(friend);
                tv.setText(friend.getNickName());
            }

            return viewHolder.getConvertView();
        }
    }
}
