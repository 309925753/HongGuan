package com.sk.weichat.ui.circle.range;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.Label;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.LabelDao;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.contacts.label.CreateLabelActivity;
import com.sk.weichat.ui.contacts.label.SelectLabelFriendActivity;
import com.sk.weichat.util.CommonAdapter;
import com.sk.weichat.util.CommonViewHolder;
import com.sk.weichat.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 谁可以看
 */
public class SeeCircleActivity extends BaseActivity implements View.OnClickListener {
    public static final int SELECT_LABEL_FRIEND_REQUEST_CODE = 10000;
    public static final int CREATE_LABEL_REQUEST_CODE = 10001;
    private ImageView iv_sel1, iv_sel2, iv_sel3, iv_sel4;
    private List<ImageView> imageViews;
    private ListView lv1, lv2;
    private SeeCircleAdapter mSeeCircleAdapter;
    private List<Label> mLabelList;
    // 记录被选中的标签id
    private List<String> mSelectPositions;
    // 从‘通讯录选择’ 选中的userIdList与userNameList
    private List<String> mUserIdList;
    private List<String> mUserNameList;
    private String mLoginUserId;

    // 记录当前选中的发布说说方式，默认为公开
    private int currentSelected;
    // 用于展开/收起 lv1,lv2列表
    private boolean flag1, flag2;
    private String str1;
    private String str2;
    private String str3;
    private int mCurrentLabelPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.see_circle_activity);

        currentSelected = getIntent().getIntExtra("THIS_CIRCLE_TYPE", 0);
        str1 = getIntent().getStringExtra("THIS_CIRCLE_PERSON_RECOVER1");
        str2 = getIntent().getStringExtra("THIS_CIRCLE_PERSON_RECOVER2");
        str3 = getIntent().getStringExtra("THIS_CIRCLE_PERSON_RECOVER3");

        initAction();
        loadData();
        initView();
        initEvent();
    }

    private void initAction() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(this);
        TextView title = (TextView) findViewById(R.id.tv_title_center);
        title.setText(getString(R.string.who_can_see));
        TextView title_right = (TextView) findViewById(R.id.tv_title_right);
        title_right.setText(getString(R.string.finish));
        findViewById(R.id.tv_title_right).setOnClickListener(this);
    }

    private void loadData() {
        imageViews = new ArrayList<>();
        mLabelList = new ArrayList<>();
        mSelectPositions = new ArrayList<>();
        mUserIdList = new ArrayList<>();
        mUserNameList = new ArrayList<>();

        mLoginUserId = coreManager.getSelf().getUserId();
        mLabelList = LabelDao.getInstance().getAllLabels(mLoginUserId);
        Label label = new Label();
        label.setGroupId("0x01");
        mLabelList.add(label);
    }

    private void initView() {
        iv_sel1 = (ImageView) findViewById(R.id.iv_sel1);
        iv_sel2 = (ImageView) findViewById(R.id.iv_sel2);
        iv_sel3 = (ImageView) findViewById(R.id.iv_sel3);
        iv_sel4 = (ImageView) findViewById(R.id.iv_sel4);
        imageViews.add(iv_sel1);
        imageViews.add(iv_sel2);
        imageViews.add(iv_sel3);
        imageViews.add(iv_sel4);

        lv1 = (ListView) findViewById(R.id.lv1);
        lv2 = (ListView) findViewById(R.id.lv2);
        mSeeCircleAdapter = new SeeCircleAdapter(this, mLabelList);
        lv1.setAdapter(mSeeCircleAdapter);
        lv2.setAdapter(mSeeCircleAdapter);

        setSelected(currentSelected);
        if (currentSelected == 2 || currentSelected == 3) {
            List<String> string1 = JSON.parseArray(str1, String.class);
            List<String> string2 = JSON.parseArray(str2, String.class);
            List<String> string3 = JSON.parseArray(str3, String.class);
            mSelectPositions.addAll(string1);
            mUserIdList.addAll(string2);
            mUserNameList.addAll(string3);
            mSeeCircleAdapter.notifyDataSetChanged();
        }
    }

    private void initEvent() {
        findViewById(R.id.rl_public).setOnClickListener(this);
        findViewById(R.id.rl_private).setOnClickListener(this);
        findViewById(R.id.rl_not_all).setOnClickListener(this);
        findViewById(R.id.rl_not_see).setOnClickListener(this);
        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                String groupId = mLabelList.get(position).getGroupId();
                if (groupId.equals("0x01")) {
                    Intent intent1 = new Intent(SeeCircleActivity.this, SelectLabelFriendActivity.class);
                    intent1.putExtra("IS_FROM_SEE_CIRCLE_ACTIVITY", true);
                    intent1.putExtra("exist_ids", JSON.toJSONString(mUserIdList));
                    startActivityForResult(intent1, SELECT_LABEL_FRIEND_REQUEST_CODE);
                } else {
                    if (hasSelected(groupId)) {
                        removeSelect(groupId);
                    } else {
                        addSelect(groupId);
                    }
                }
            }
        });

        lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                String groupId = mLabelList.get(position).getGroupId();
                if (groupId.equals("0x01")) {
                    Intent intent1 = new Intent(SeeCircleActivity.this, SelectLabelFriendActivity.class);
                    intent1.putExtra("IS_FROM_SEE_CIRCLE_ACTIVITY", true);
                    intent1.putExtra("exist_ids", JSON.toJSONString(mUserIdList));
                    startActivityForResult(intent1, SELECT_LABEL_FRIEND_REQUEST_CODE);
                } else {
                    if (hasSelected(groupId)) {
                        removeSelect(groupId);
                    } else {
                        addSelect(groupId);
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_left:
                finish();
                break;
            case R.id.tv_title_right:
                // 完成
                Intent intent = new Intent();
                if (currentSelected == 2 || currentSelected == 3) {
                    // 部分可见 || 不给谁看
                    if (mSelectPositions.size() > 0 || mUserIdList.size() > 0) {
                        intent.putExtra("THIS_CIRCLE_TYPE", currentSelected + 1);
                        intent.putExtra("THIS_CIRCLE_PERSON", getSelected());
                        intent.putExtra("THIS_CIRCLE_PERSON_NAME", getSelectedName());
                        // Todo 部分可见 ||  不给谁看,需要将以下参数也返回，下次进入恢复选中状态
                        intent.putExtra("THIS_CIRCLE_PERSON_RECOVER1", JSON.toJSONString(mSelectPositions));
                        intent.putExtra("THIS_CIRCLE_PERSON_RECOVER2", JSON.toJSONString(mUserIdList));
                        intent.putExtra("THIS_CIRCLE_PERSON_RECOVER3", JSON.toJSONString(mUserNameList));
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        ToastUtil.showToast(SeeCircleActivity.this, R.string.tip_select_at_least_one);
                    }
                } else {
                    // 公开 || 私密
                    intent.putExtra("THIS_CIRCLE_TYPE", currentSelected + 1);
                    Log.e("zq", "currentSelected:" + currentSelected);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
            case R.id.rl_public:
                setSelected(0);
                break;
            case R.id.rl_private:
                setSelected(1);
                break;
            case R.id.rl_not_all:
                setSelected(2);
                break;
            case R.id.rl_not_see:
                setSelected(3);
                break;
        }
    }

    // 得到'部分可见' || '不给谁看'的人的id,','号拼接
    private String getSelected() {
        String userIdList = "";
        List<String> list = new ArrayList<>();
        // 1.先添加选中的label内的userId
        for (int i = 0; i < mSelectPositions.size(); i++) {
            for (int j = 0; j < mLabelList.size(); j++) {
                if (mLabelList.get(j).getGroupId().equals(mSelectPositions.get(i))) {
                    List<String> strings = JSON.parseArray(mLabelList.get(j).getUserIdList(), String.class);
                    list.addAll(strings);
                }
            }
        }
        // 2.在添加未建立标签的选中的userId
        list.addAll(mUserIdList);
        // 3.去重
        List<String> newList = new ArrayList<>(new HashSet<>(list));
        // List<String> newList = new ArrayList<>(new TreeSet<>(list));// 可去重并且排序
        // 4.在拼接为服务器需要的格式
        for (int i = 0; i < newList.size(); i++) {
            if (i == newList.size() - 1) {
                userIdList += newList.get(i);
            } else {
                userIdList += newList.get(i) + ",";
            }
        }
        return userIdList;
    }

    // 记录(标记)当前选中的发布说说方式
    private void setSelected(int position) {
        mSelectPositions.clear();
        mUserIdList.clear();
        mUserNameList.clear();

        for (int i = 0; i < imageViews.size(); i++) {
            if (i == position) {
                currentSelected = position;
                imageViews.get(i).setVisibility(View.VISIBLE);
            } else {
                imageViews.get(i).setVisibility(View.INVISIBLE);
            }
        }
        if (position == 0 || position == 1) {
            lv1.setVisibility(View.GONE);
            lv2.setVisibility(View.GONE);
            flag1 = false;
            flag2 = false;
        } else if (position == 2) {
            flag1 = !flag1;
            if (flag1) {
                lv1.setVisibility(View.VISIBLE);
                lv2.setVisibility(View.GONE);
                flag2 = false;
            } else {
                lv1.setVisibility(View.GONE);
                lv2.setVisibility(View.GONE);
            }
            mSeeCircleAdapter.notifyDataSetChanged();
        } else if (position == 3) {
            flag2 = !flag2;
            if (flag2) {
                lv1.setVisibility(View.GONE);
                lv2.setVisibility(View.VISIBLE);
                flag1 = false;
            } else {
                lv1.setVisibility(View.GONE);
                lv2.setVisibility(View.GONE);
            }
            mSeeCircleAdapter.notifyDataSetChanged();
        }
    }

    // 得到'部分可见' || '不给谁看'的人的名字,'、'号拼接
    private String getSelectedName() {
        String userNameList = "";
        List<String> list = new ArrayList<>();
        // 1.先添加选中的labelName
        for (int i = 0; i < mSelectPositions.size(); i++) {
            for (int j = 0; j < mLabelList.size(); j++) {
                if (mLabelList.get(j).getGroupId().equals(mSelectPositions.get(i))) {
                    list.add(mLabelList.get(j).getGroupName());
                }
            }
        }
        // 2.在添加未建立标签的选中的userName
        list.addAll(mUserNameList);
        for (int i = 0; i < list.size(); i++) {
            if (i == list.size() - 1) {
                userNameList += list.get(i);
            } else {
                userNameList += list.get(i) + ",";
            }
        }
        return userNameList;
    }

    // ITEM OPERATING
    private boolean hasSelected(String groupId) {
        for (int i = 0; i < mSelectPositions.size(); i++) {
            if (mSelectPositions.get(i).equals(groupId)) {
                return true;
            }
        }
        return false;
    }

    private void addSelect(String groupId) {
        mSelectPositions.add(groupId);
        mSeeCircleAdapter.notifyDataSetChanged();
    }

    private void removeSelect(String groupId) {
        mSelectPositions.remove(groupId);
        mSeeCircleAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_LABEL_FRIEND_REQUEST_CODE && resultCode == RESULT_OK) {
            String labelId = data.getStringExtra("NEW_LABEL_ID");
            if (!TextUtils.isEmpty(labelId)) {
                Label label = LabelDao.getInstance().getLabel(mLoginUserId, labelId);
                mLabelList.add(0, label);
                mSelectPositions.add(labelId);// 选中
                mSeeCircleAdapter.notifyDataSetChanged();
            } else {// 未存标签
                String userIds = data.getStringExtra("inviteId");
                String userNames = data.getStringExtra("inviteName");
                mUserIdList = JSON.parseArray(userIds, String.class);
                mUserNameList = JSON.parseArray(userNames, String.class);
                mSeeCircleAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == CREATE_LABEL_REQUEST_CODE && resultCode == RESULT_OK) {
            Label label = LabelDao.getInstance().getLabel(mLoginUserId, mLabelList.get(mCurrentLabelPosition).getGroupId());
            mLabelList.remove(mCurrentLabelPosition);
            mLabelList.add(mCurrentLabelPosition, label);
            mSeeCircleAdapter.notifyDataSetChanged();
        }
    }

    class SeeCircleAdapter extends CommonAdapter<Label> {

        SeeCircleAdapter(Context context, List<Label> data) {
            super(context, data);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.row_select_see_circle, position);
            CheckBox mGroupSelectCb = viewHolder.getView(R.id.see_check_box);
            mGroupSelectCb.setChecked(false);
            TextView mGroupNameTv = viewHolder.getView(R.id.label_name);
            TextView mGroupUserNameTv = viewHolder.getView(R.id.label_user_name);
            ImageView mEditLabelIv = viewHolder.getView(R.id.edit_label_iv);
            final Label label = data.get(position);
            if (label != null) {
                if (label.getGroupId().equals("0x01")) {
                    mGroupSelectCb.setVisibility(View.INVISIBLE);
                    mGroupNameTv.setTextColor(getResources().getColor(R.color.link_nick_name_color));
                    mGroupUserNameTv.setTextColor(getResources().getColor(R.color.main_color));
                    mEditLabelIv.setVisibility(View.GONE);
                    mGroupNameTv.setText(R.string.select_from_contacts);
                    String userNames = "";
                    if (mUserNameList.size() > 0) {
                        for (int i = 0; i < mUserNameList.size(); i++) {
                            if (i == mUserNameList.size() - 1) {
                                userNames += mUserNameList.get(i);
                            } else {
                                userNames += mUserNameList.get(i) + "，";
                            }
                        }
                        mGroupUserNameTv.setVisibility(View.VISIBLE);
                        mGroupUserNameTv.setText(userNames);
                    } else {
                        mGroupUserNameTv.setVisibility(View.GONE);
                    }
                } else {
                    mGroupSelectCb.setVisibility(View.VISIBLE);
                    mGroupNameTv.setTextColor(getResources().getColor(R.color.app_black));
                    mGroupUserNameTv.setTextColor(getResources().getColor(R.color.Grey_400));
                    mGroupUserNameTv.setVisibility(View.VISIBLE);
                    mEditLabelIv.setVisibility(View.VISIBLE);
                    for (int i = mSelectPositions.size() - 1; i >= 0; i--) {
                        if (mSelectPositions.get(i).equals(label.getGroupId())) {
                            mGroupSelectCb.setChecked(true);
                        }
                    }
                    mGroupNameTv.setText(label.getGroupName());
                    List<String> userIds = JSON.parseArray(label.getUserIdList(), String.class);
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
                        mGroupUserNameTv.setText(userNames);
                    }
                }
            }

            mEditLabelIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentLabelPosition = position;

                    Intent intent = new Intent(SeeCircleActivity.this, CreateLabelActivity.class);
                    intent.putExtra("isEditLabel", true);
                    intent.putExtra("labelId", label.getGroupId());
                    intent.putExtra("IS_FROM_SEE_CIRCLE_ACTIVITY", true);
                    startActivityForResult(intent, CREATE_LABEL_REQUEST_CODE);
                }
            });
            return viewHolder.getConvertView();
        }
    }
}
