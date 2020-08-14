package com.sk.weichat.ui.me.select;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.Label;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.LabelDao;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.CommonAdapter;
import com.sk.weichat.util.CommonViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/5 0005.
 * 只是对临时的数据(status)进行更改，来标记是否选中，并不需要去更新数据库 status 100 未选中 101选中
 */

public class SelectLabelActivity extends BaseActivity {
    private String mLoginUserId;

    private ListView mLabelLv;
    private LabelAdapter mLabelAdapter;
    private List<Label> mLabelList;

    private List<String> mSelectedLabel;// 已选中的标签

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_label);
        initActionBar();
        initData();
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
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.select_recipient));
        TextView tvRight = (TextView) findViewById(R.id.tv_title_right);
        tvRight.setText(getString(R.string.sure));
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> lIds = new ArrayList<>();
                List<String> lNames = new ArrayList<>();
                for (int i = 0; i < mLabelList.size(); i++) {
                    if (mLabelList.get(i).isSelected()) {
                        lIds.add(mLabelList.get(i).getGroupId());
                        lNames.add(mLabelList.get(i).getGroupName());
                    }
                }
                Intent intent = new Intent();
                intent.putExtra("SELECTED_LABEL_IDS", JSON.toJSONString(lIds));
                intent.putExtra("SELECTED_LABEL_NAMES", JSON.toJSONString(lNames));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void initData() {
        mLoginUserId = coreManager.getSelf().getUserId();
        mLabelList = LabelDao.getInstance().getAllLabels(mLoginUserId);
        mSelectedLabel = new ArrayList<>();
        String mSelectedLabelIds = getIntent().getStringExtra("SELECTED_LABEL");
        mSelectedLabel = JSON.parseArray(mSelectedLabelIds, String.class);
        if (mSelectedLabel != null && mSelectedLabel.size() > 0) {
            for (int i = 0; i < mSelectedLabel.size(); i++) {
                for (int i1 = 0; i1 < mLabelList.size(); i1++) {
                    if (mLabelList.get(i1).getGroupId().equals(mSelectedLabel.get(i))) {
                        mLabelList.get(i1).setSelected(true);
                    }
                }
            }
        }
    }

    private void initView() {
        mLabelLv = (ListView) findViewById(R.id.label_lv);
        mLabelAdapter = new LabelAdapter(this, mLabelList);
        mLabelLv.setAdapter(mLabelAdapter);
    }

    private void initEvent() {
        mLabelLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mLabelList.get(position).isSelected()) {
                    mLabelList.get(position).setSelected(false);
                } else {
                    mLabelList.get(position).setSelected(true);
                }
                mLabelAdapter.notifyDataSetChanged();
            }
        });
    }

    class LabelAdapter extends CommonAdapter<Label> {

        public LabelAdapter(Context context, List<Label> data) {
            super(context, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.row_select_label, position);
            TextView labelName = viewHolder.getView(R.id.label_name);
            TextView labelUserNames = viewHolder.getView(R.id.label_user_name);
            CheckBox cb = viewHolder.getView(R.id.select_cb);
            final Label label = mLabelList.get(position);
            if (label != null) {
                List<String> userIds = JSON.parseArray(label.getUserIdList(), String.class);
                if (userIds != null) {
                    labelName.setText(label.getGroupName() + "(" + userIds.size() + ")");
                } else {
                    labelName.setText(label.getGroupName() + "(0)");

                }
                String userNames = "";
                if (userIds != null && userIds.size() > 0) {
                    for (int i = 0; i < userIds.size(); i++) {
                        Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, userIds.get(i));
                        if (friend != null) {
                            if (i == userIds.size() - 1) {
                                userNames += TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName();
                            } else {
                                userNames += TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() + "，" : friend.getRemarkName() + "，";
                            }
                        }
                    }
                    if (TextUtils.isEmpty(userNames)) {
                        labelUserNames.setVisibility(View.GONE);
                    } else {
                        labelUserNames.setVisibility(View.VISIBLE);
                        labelUserNames.setText(userNames);
                    }
                }
                if (label.isSelected()) {
                    cb.setChecked(true);
                } else {
                    cb.setChecked(false);
                }
            }
            return viewHolder.getConvertView();
        }
    }
}
