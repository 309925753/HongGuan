package com.sk.weichat.call;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.RoomMember;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.RoomMemberDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class JitsiInviteActivity extends BaseActivity {
    private String mLoginUserId;
    private String mLoginNickName;
    private int type;
    private String voicejid; // 群发消息jid
    private String roomId; // 查询群成员
    private EditText mEditText;
    private boolean isSearch = false;
    private ListView inviteList;
    private ListViewAdapter mAdapter;
    private List<SelectMuMembers> mMucMembers;
    private List<SelectMuMembers> mCurrentMucMembers;

    public static void start(Context ctx, int type, String mUseId, String roomId) {
        Intent intent = new Intent(ctx, JitsiInviteActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("voicejid", mUseId);
        intent.putExtra("roomid", roomId);
        ctx.startActivity(intent);
    }

    public static void start(Context ctx, int type, String mUseId) {
        Intent intent = new Intent(ctx, JitsiInviteActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("voicejid", mUseId);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jitsi_invite);
        mLoginUserId = coreManager.getSelf().getUserId();
        mLoginNickName = coreManager.getSelf().getNickName();

        type = getIntent().getIntExtra("type", CallConstants.Video_Meet);
        voicejid = getIntent().getStringExtra("voicejid");
        roomId = getIntent().getStringExtra("roomid");

        initAction();
        initView();
        initData();
    }

    private void initAction() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.select_contacts));
        TextView tvRight = (TextView) findViewById(R.id.tv_title_right);
        tvRight.setText(getString(R.string.finish));
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> data = new ArrayList<>();
                for (int i = 0; i < mMucMembers.size(); i++) {
                    if (mMucMembers.get(i).isChecked) {
                        data.add(mMucMembers.get(i).getMember().getUserId());
                    }
                }
                EventBus.getDefault().post(new MessageEventMeetingInvite(roomId, "", mLoginUserId, mLoginUserId, mLoginNickName, voicejid, data, type));

                intent();
            }
        });
    }

    private void initView() {
        mEditText = findViewById(R.id.search_edit);
        mEditText.setHint(getString(R.string.search));
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mCurrentMucMembers.clear();
                String str = s.toString().trim();
                if (TextUtils.isEmpty(str)) {
                    isSearch = false;
                    mCurrentMucMembers.addAll(mMucMembers);
                } else {
                    isSearch = true;
                    for (int i = 0; i < mMucMembers.size(); i++) {
                        if (mMucMembers.get(i).getMember().getUserName().contains(str)) {// 符合条件
                            mCurrentMucMembers.add((mMucMembers.get(i)));
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });
        inviteList = findViewById(R.id.invitelist);
        mAdapter = new ListViewAdapter();

        inviteList.setOnItemClickListener((adapterView, view, position, l) -> {
            if (isSearch) {
                mCurrentMucMembers.get(position).setChecked(mCurrentMucMembers.get(position).isChecked ? false : true);
                for (int i = 0; i < mMucMembers.size(); i++) {
                    if (mMucMembers.get(i).getMember().getUserId().
                            equals(mCurrentMucMembers.get(position).getMember().getUserId())) {
                        mMucMembers.get(i).setChecked(mCurrentMucMembers.get(position).isChecked ? true : false);
                    }
                }
                mAdapter.notifyDataSetChanged();
            } else {
                mCurrentMucMembers.get(position).setChecked(mCurrentMucMembers.get(position).isChecked ? false : true);
                mMucMembers.get(position).setChecked(mCurrentMucMembers.get(position).isChecked ? true : false);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initData() {
        mMucMembers = new ArrayList<>();
        mCurrentMucMembers = new ArrayList<>();
        if (TextUtils.isEmpty(roomId)) {// 通话界面跳转过来的，没有传roomId过来，先获取一下
            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, voicejid);
            List<RoomMember> roomMember = RoomMemberDao.getInstance().getRoomMember(friend.getRoomId());
            setAdapter(roomMember);
        } else {
            List<RoomMember> roomMember = RoomMemberDao.getInstance().getRoomMember(roomId);
            setAdapter(roomMember);
        }
    }

    private void setAdapter(List<RoomMember> roomMember) {
        for (int i = 0; i < roomMember.size(); i++) {
            if (!roomMember.get(i).getUserId().equals(mLoginUserId)) {// 移除自己
                SelectMuMembers selectMuMembers = new SelectMuMembers();
                selectMuMembers.setMember(roomMember.get(i));
                selectMuMembers.setChecked(false);
                mMucMembers.add(selectMuMembers);
                mCurrentMucMembers.add(selectMuMembers);
            }
        }
        inviteList.setAdapter(mAdapter);
    }

    private void intent() {
        if (TextUtils.isEmpty(roomId)) {
            finish();
        } else {
            Jitsi_connecting_second.start(this, voicejid, mLoginUserId, type);
            finish();
        }
    }

    class ListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCurrentMucMembers.size();
        }

        @Override
        public Object getItem(int position) {
            return mCurrentMucMembers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_select_contacts_jitsi, parent, false);
            }
            CheckBox invite_ck = ViewHolder.get(convertView, R.id.invite_ck);
            ImageView invite_avatar = ViewHolder.get(convertView, R.id.invite_avatar);
            TextView invite_name = ViewHolder.get(convertView, R.id.invite_name);

            invite_ck.setChecked(mCurrentMucMembers.get(position).isChecked);
            ImageLoadHelper.showImageWithError(
                    mContext,
                    AvatarHelper.getAvatarUrl(mCurrentMucMembers.get(position).member.getUserId(), true),
                    R.drawable.avatar_normal,
                    invite_avatar
            );
            invite_name.setText(mCurrentMucMembers.get(position).getMember().getUserName());
            return convertView;
        }
    }

    class SelectMuMembers {
        private RoomMember member;
        private boolean isChecked;

        public RoomMember getMember() {
            return member;
        }

        public void setMember(RoomMember member) {
            this.member = member;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }
    }
}
