package com.sk.weichat.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.core.graphics.drawable.DrawableCompat;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.sortlist.BaseSortModel;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.message.multi.RoomInfoActivity;
import com.sk.weichat.ui.other.BasicInfoActivity;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.UiUtils;
import com.sk.weichat.util.ViewHolder;
import com.sk.weichat.view.HeadView;

import java.util.List;

public class FriendSortAdapter extends BaseAdapter implements SectionIndexer {

    private Context mContext;
    private List<BaseSortModel<Friend>> mSortFriends;
    private boolean isShowCheckBox;

    public FriendSortAdapter(Context context, List<BaseSortModel<Friend>> sortFriends) {
        mContext = context;
        mSortFriends = sortFriends;
    }

    public void setData(List<BaseSortModel<Friend>> sortFriends) {
        mSortFriends = sortFriends;
        notifyDataSetChanged();
    }

    public void showCheckBox() {
        isShowCheckBox = true;
    }

    @Override
    public int getCount() {
        return mSortFriends.size();
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.row_sort_friend, parent, false);
        }
        TextView catagoryTitleTv = ViewHolder.get(convertView, R.id.catagory_title);
        View v_friend_bg = ViewHolder.get(convertView, R.id.v_friend_bg);
        ImageView avatar_img_divices = ViewHolder.get(convertView, R.id.avatar_img_divices);
        ImageView avatar_img = ViewHolder.get(convertView, R.id.avatar_img);
        HeadView avatar_imgS = ViewHolder.get(convertView, R.id.avatar_imgS);
        TextView nick_name_tv = ViewHolder.get(convertView, R.id.nick_name_tv);

        // 根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            catagoryTitleTv.setVisibility(View.VISIBLE);
            v_friend_bg.setVisibility(View.GONE);
            catagoryTitleTv.setText(mSortFriends.get(position).getFirstLetter());
        } else {
            catagoryTitleTv.setVisibility(View.GONE);
            v_friend_bg.setVisibility(View.VISIBLE);
        }

        final Friend friend = mSortFriends.get(position).getBean();
        if (friend.getRoomFlag() == 0) {// 单人
            avatar_img.setVisibility(View.VISIBLE);
            avatar_imgS.setVisibility(View.GONE);
            if (friend.getUserId().equals(Friend.ID_SYSTEM_MESSAGE)) {
                avatar_img.setImageResource(R.mipmap.ic_logo);
            } else if (friend.getUserId().equals(Friend.ID_NEW_FRIEND_MESSAGE)) {
                avatar_img.setImageResource(R.drawable.im_new_friends);
            } else if (friend.getIsDevice() == 1) {
                avatar_img.setVisibility(View.GONE);
                avatar_img_divices.setVisibility(View.VISIBLE);
                if ("android".equals(friend.getUserId())) {
                    avatar_img_divices.setImageResource(R.mipmap.my_android_icon);
                } else if ("ios".equals(friend.getUserId())) {
                    avatar_img_divices.setImageResource(R.mipmap.my_iphone_icon);
                } else if ("mac".equals(friend.getUserId())) {
                    avatar_img_divices.setImageResource(R.mipmap.my_mac_computer_icon);
                } else if ("web".equals(friend.getUserId())) {
                    avatar_img_divices.setImageResource(R.mipmap.my_web_icon);
                } else if ("pc".equals(friend.getUserId())) {
                    avatar_img_divices.setImageResource(R.mipmap.my_windows_icon);
                }
            } else {
                avatar_img.setVisibility(View.VISIBLE);
                AvatarHelper.getInstance().displayAvatar(friend.getUserId(), avatar_img, true);
            }
        } else {// 群组
            avatar_img.setVisibility(View.GONE);
            avatar_imgS.setVisibility(View.VISIBLE);
            AvatarHelper.getInstance().displayAvatar(CoreManager.requireSelf(mContext).getUserId(), friend, avatar_imgS);
        }

        // 昵称
        nick_name_tv.setText(!TextUtils.isEmpty(friend.getRemarkName()) ? friend.getRemarkName() : friend.getNickName());
        // 个性签名
        // des_tv.setText(friend.getDescription());

        // 点击头像跳转详情
        avatar_img.setOnClickListener(view -> {
            if (!UiUtils.isNormalClick(view)) {
                return;
            }
            if (friend.getRoomFlag() == 0) {  // 单人
                if (!friend.getUserId().equals(Friend.ID_SYSTEM_MESSAGE)
                        && !friend.getUserId().equals(Friend.ID_NEW_FRIEND_MESSAGE)
                        && friend.getIsDevice() != 1) {
                    Intent intent = new Intent(mContext, BasicInfoActivity.class);
                    intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                    mContext.startActivity(intent);
                }
            } else {  // 群组
                Intent intent = new Intent(mContext, RoomInfoActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                mContext.startActivity(intent);
            }
        });

        // 点击头像跳转详情
        avatar_imgS.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, RoomInfoActivity.class);
            intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
            mContext.startActivity(intent);
        });

        // 转发时支持多选
        CheckBox checkBox = ViewHolder.get(convertView, R.id.cb_instant);
        checkBox.setVisibility(isShowCheckBox ? View.VISIBLE : View.GONE);
        if (friend.isCheck()) {
            checkBox.setChecked(true);
            Drawable drawable = mContext.getResources().getDrawable(R.drawable.sel_check_wx2);
            drawable = DrawableCompat.wrap(drawable);
            ColorStateList tabColor = SkinUtils.getSkin(mContext).getTabColorState();
            DrawableCompat.setTintList(drawable, tabColor);
            checkBox.setButtonDrawable(drawable);
        } else {
            checkBox.setChecked(false);
            checkBox.setButtonDrawable(mContext.getResources().getDrawable(R.drawable.sel_nor_wx2));
        }
        return convertView;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = mSortFriends.get(i).getFirstLetter();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return mSortFriends.get(position).getFirstLetter().charAt(0);
    }
}
