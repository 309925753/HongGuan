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
import com.sk.weichat.bean.Label;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.LabelDao;
import com.sk.weichat.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class SelectLabelAdapter extends SelectAdapter {
    private List<String> mLabelIds;

    public void startSelect(Activity ctx) {
        Intent intent = new Intent(ctx, SelectLabelActivity.class);
        intent.putExtra("SELECTED_LABEL", JSON.toJSONString(mLabelIds));
        ctx.startActivityForResult(intent, mRequestCode);
    }

    @Override
    public int getLabel() {
        return R.string.hint_selec_tag;
    }

    @NonNull
    @Override
    public List<Friend> query(BaseActivity ctx) {
        List<Friend> ret = new ArrayList<>();
        if (mLabelIds == null) {
            return ret;
        }
        for (int i = 0; i < mLabelIds.size(); i++) {
            Label label = LabelDao.getInstance().getLabel(ctx.coreManager.getSelf().getUserId(), mLabelIds.get(i));
            if (label != null) {
                String idList = label.getUserIdList();
                List<String> list = JSON.parseArray(idList, String.class);
                if (list != null && list.size() > 0) {
                    for (int i1 = 0; i1 < list.size(); i1++) {
                        String friendId = list.get(i1);
                        Friend friend = FriendDao.getInstance().getFriend(ctx.coreManager.getSelf().getUserId(), friendId);
                        if (friend != null) {
                            ret.add(friend);
                        }
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public boolean consume(Context ctx, int requestCode, Intent data) {
        if (requestCode != mRequestCode) {
            return false;
        }
        String mSelectedLabelIds = data.getStringExtra("SELECTED_LABEL_IDS");
        if (TextUtils.isEmpty(mSelectedLabelIds)) {
            return false;
        }
        String mSelectedLabelNames = data.getStringExtra("SELECTED_LABEL_NAMES");
        if (TextUtils.isEmpty(mSelectedLabelNames)) {
            return false;
        }
        mLabelIds = JSON.parseArray(mSelectedLabelIds, String.class);

        if (mLabelIds.size() > 0) {
            tvValue.setText(TextUtils.join(",", JSON.parseArray(mSelectedLabelNames, String.class)));
            tvValue.setVisibility(View.VISIBLE);
        } else {
            tvValue.setText("");
            tvValue.setVisibility(View.GONE);
        }
        return true;
    }
}
