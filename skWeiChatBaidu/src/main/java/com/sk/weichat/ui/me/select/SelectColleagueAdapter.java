package com.sk.weichat.ui.me.select;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.SelectFriendItem;
import com.sk.weichat.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class SelectColleagueAdapter extends SelectAdapter {
    private List<SelectFriendItem> mItemList;

    public void startSelect(Activity ctx) {
        SelectColleagueActivity.start(ctx, mRequestCode, mItemList);
    }

    @Override
    public int getLabel() {
        return R.string.hint_selec_colleague;
    }

    @NonNull
    @Override
    public List<Friend> query(BaseActivity ctx) {
        List<Friend> ret = new ArrayList<>();
        if (mItemList == null) {
            return ret;
        }
        for (int i = 0; i < mItemList.size(); i++) {
            SelectFriendItem item = mItemList.get(i);
            Friend friend = new Friend();
            friend.setOwnerId(ctx.coreManager.getSelf().getUserId());
            friend.setUserId(item.getUserId());
            friend.setNickName(item.getName());
            ret.add(friend);
        }
        return ret;
    }

    @Override
    public boolean consume(Context ctx, int requestCode, Intent data) {
        if (requestCode != mRequestCode) {
            return false;
        }
        String selectedUser = data.getStringExtra("SELECTED_ITEMS");
        if (TextUtils.isEmpty(selectedUser)) {
            return false;
        }
        mItemList = JSON.parseArray(selectedUser, SelectFriendItem.class);

        if (mItemList.size() > 0) {
            final StringBuilder sb = new StringBuilder();
            sb.append(mItemList.get(0).getName());
            for (int i = 1; i < mItemList.size(); i++) {
                sb.append(",");
                sb.append(mItemList.get(i).getName());
            }
            tvValue.setText(sb);
            tvValue.setVisibility(View.VISIBLE);
        } else {
            tvValue.setText("");
            tvValue.setVisibility(View.GONE);
        }
        return true;
    }
}
