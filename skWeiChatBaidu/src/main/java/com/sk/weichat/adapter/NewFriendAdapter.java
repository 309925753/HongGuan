package com.sk.weichat.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.message.NewFriendMessage;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.other.BasicInfoActivity;
import com.sk.weichat.util.ViewHolder;

import java.util.List;

public class NewFriendAdapter extends BaseAdapter {
    Button action_btn_1;
    Button action_btn_2;
    private String mLoginUserId;
    private Context mContext;
    private List<NewFriendMessage> mNewFriends;
    private NewFriendActionListener mListener;
    private String huihua = null;
    private String username = null;

    public NewFriendAdapter(Context context, String loginUserId, List<NewFriendMessage> newFriends, NewFriendActionListener listener) {
        mContext = context;
        mNewFriends = newFriends;
        mListener = listener;
        mLoginUserId = loginUserId;
    }

    @Override
    public int getCount() {
        return mNewFriends.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.row_new_friend, parent, false);
        }
        ImageView avatar_img = ViewHolder.get(convertView, R.id.avatar_img);
        TextView nick_name_tv = ViewHolder.get(convertView, R.id.nick_name_tv);
        TextView des_tv = ViewHolder.get(convertView, R.id.des_tv);
        action_btn_1 = ViewHolder.get(convertView, R.id.action_btn_1);
        // ButtonColorChange.colorChange(mContext, action_btn_1);
        action_btn_2 = ViewHolder.get(convertView, R.id.action_btn_2);
        final NewFriendMessage newFriend = mNewFriends.get(position);
        // 设置头像
        AvatarHelper.getInstance().displayAvatar(newFriend.getNickName(), newFriend.getUserId(), avatar_img, true);
        // 昵称
        nick_name_tv.setText(newFriend.getNickName());
        // 重置状态
        action_btn_1.setVisibility(View.GONE);
        action_btn_2.setVisibility(View.GONE);
        action_btn_1.setOnClickListener(new AgreeListener(position));
        action_btn_2.setOnClickListener(new FeedbackListener(position));
        action_btn_1.setText(R.string.pass);
        action_btn_2.setText(R.string.answer);
        avatar_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 头像的点击事件
                Intent intent = new Intent(mContext, BasicInfoActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, newFriend.getUserId());
                mContext.startActivity(intent);
            }
        });
        // Friend friend = FriendDao.getInstance().getFriend(newFriend.getOwnerId(), newFriend.getUserId());
        int status = newFriend.getState();
        Log.e("state", "-----" + status);
        username = newFriend.getNickName();
        huihua = newFriend.getContent();
        if (status == Friend.STATUS_11) { // 别人发过来的验证消息
            action_btn_1.setVisibility(View.VISIBLE);
            action_btn_2.setVisibility(View.VISIBLE);
        }
        // 显示朋友的状态
        fillFriendState(status, newFriend.getUserId(), des_tv);
        return convertView;
    }

    private void fillFriendState(int status, String userId, TextView tvDesc) {
        switch (status) {
            case Friend.STATUS_20:
                tvDesc.setText("");
                break;
            case Friend.STATUS_10:
                tvDesc.setText(mContext.getString(R.string.wait_pass));
                break;
            case Friend.STATUS_11:
                // tvDesc.setText(mContext.getString("JXUserInfoVC_Hello"));
                tvDesc.setText(huihua);
                break;
            case Friend.STATUS_12:
                tvDesc.setText(mContext.getString(R.string.friend_object_passed));
                break;
            case Friend.STATUS_13:
                tvDesc.setText(mContext.getString(R.string.friend_object_passgo));
                break;
            case Friend.STATUS_14:
                tvDesc.setText(huihua);
                action_btn_2.setVisibility(View.VISIBLE);
                break;
            case Friend.STATUS_15:
                tvDesc.setText(huihua);
                action_btn_1.setVisibility(View.VISIBLE);
                action_btn_2.setVisibility(View.VISIBLE);
                break;
            case Friend.STATUS_16:
                // 不只是好友，还有公众号，直接在外面更新content吧
                // tvDesc.setText(mContext.getString(R.string.delete_firend) + username);
                tvDesc.setText(huihua);
                break;
            case Friend.STATUS_17:
                tvDesc.setText(username + mContext.getString(R.string.delete_me));
                break;
            case Friend.STATUS_18:
                tvDesc.setText(mContext.getString(R.string.added_black_list) + username);
                break;
            case Friend.STATUS_19:
                tvDesc.setText(username + mContext.getString(R.string.friend_object_pulled_black));
                break;
            case Friend.STATUS_21:
                tvDesc.setText(username + mContext.getString(R.string.add_me_as_friend));
                break;
            case Friend.STATUS_22:
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, userId);
                if (friend != null && friend.getStatus() == Friend.STATUS_SYSTEM) {
                    tvDesc.setText(mContext.getString(R.string.added_notice_friend) + username);
                } else {
                    tvDesc.setText(mContext.getString(R.string.added_friend) + username);
                }
                break;
            case Friend.STATUS_24:
                tvDesc.setText(username + mContext.getString(R.string.cancel_black_me));
                break;
            case Friend.STATUS_25:
                tvDesc.setText(MyApplication.getContext().getString(R.string.add_by_address));
                break;
            case Friend.STATUS_26:
                tvDesc.setText(huihua);
                break;
        }
    }

    public interface NewFriendActionListener {
        void addAttention(int position);// 加关注

        void removeBalckList(int position);// 移除黑名单

        void agree(int position);// 同意加好友

        void feedback(int position);
    }

    // 加关注
    private class AddAttentionListener implements View.OnClickListener {
        private int position;

        public AddAttentionListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.addAttention(position);
            }
        }
    }

    // 移除黑名单
    private class RemoveBlackListListener implements View.OnClickListener {
        private int position;

        public RemoveBlackListListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.removeBalckList(position);
            }
        }
    }

    // 同意加好友
    private class AgreeListener implements View.OnClickListener {
        private int position;

        public AgreeListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.agree(position);
            }
        }
    }

    // 回话
    private class FeedbackListener implements View.OnClickListener {
        private int position;

        public FeedbackListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.feedback(position);
            }
        }
    }
}
