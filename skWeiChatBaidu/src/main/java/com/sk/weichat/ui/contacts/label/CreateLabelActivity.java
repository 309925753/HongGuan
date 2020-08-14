package com.sk.weichat.ui.contacts.label;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.roamer.slidelistview.SlideBaseAdapter;
import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.Label;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.LabelDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.other.BasicInfoActivity;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.ViewHolder;
import com.sk.weichat.view.PullToRefreshSlideListView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class CreateLabelActivity extends BaseActivity implements View.OnClickListener {
    private EditText mLabelEdit;
    private TextView mLabelUserSizeTv;

    private PullToRefreshSlideListView mListView;
    private LabelAdapter mLabelAdapter;
    private List<Friend> mFriendList;

    private String mLoginUserId;

    private boolean isEditLabel;// 创建  || 编辑 标签
    private String labelId;// 编辑标签传入的值
    private Label mOldLabel;

    /**
     * Todo add 2018.6.20 intent from 发布说说-谁可以看
     */
    private boolean isFromSeeCircleActivity;
    private TextView mTvTitle, mTitleRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_label);
        mLoginUserId = coreManager.getSelf().getUserId();
        isEditLabel = getIntent().getBooleanExtra("isEditLabel", false);
        if (isEditLabel) {
            labelId = getIntent().getStringExtra("labelId");
            isFromSeeCircleActivity = getIntent().getBooleanExtra("IS_FROM_SEE_CIRCLE_ACTIVITY", false);
            mOldLabel = LabelDao.getInstance().getLabel(mLoginUserId, labelId);
        }
        initActionBar();
        initView();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        ImageView mTitleLeft = (ImageView) findViewById(R.id.iv_title_left);
        mTitleLeft.setVisibility(View.VISIBLE);
        mTitleLeft.setOnClickListener(this);
        mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        if (isEditLabel) {
            mTvTitle.setText(R.string.edit_tag);
        } else {
            mTvTitle.setText(R.string.create_tag);
        }
        mTitleRight = (TextView) findViewById(R.id.tv_title_right);
        mTitleRight.setTextColor(getResources().getColor(R.color.white));
        mTitleRight.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_grey_circle));
        ViewCompat.setBackgroundTintList(mTitleRight, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
        mTitleRight.setText(getString(R.string.finish));
        changeTitle(1, "");
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        mFriendList = new ArrayList<>();
        mLabelEdit = (EditText) findViewById(R.id.label_name_et);
        mLabelUserSizeTv = (TextView) findViewById(R.id.label_user_size);

        if (isEditLabel) {
            List<String> list = JSON.parseArray(mOldLabel.getUserIdList(), String.class);
            if (list != null) {
                for (String s : list) {
                    Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, s);
                    if (friend != null) {
                        mFriendList.add(friend);
                    }
                }
            }
            mLabelEdit.setTextColor(getResources().getColor(R.color.app_black));
            mLabelEdit.setText(mOldLabel.getGroupName());
            mLabelUserSizeTv.setText(getString(R.string.tag_member) + "(" + mFriendList.size() + ")");
        }

        findViewById(R.id.add_label_user).setOnClickListener(this);
        ImageView iv_add_people = findViewById(R.id.iv_add_people);
        ImageViewCompat.setImageTintList(iv_add_people, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
        TextView tv_add_people = findViewById(R.id.tv_add_people);
        tv_add_people.setTextColor(ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
        mListView = (PullToRefreshSlideListView) findViewById(R.id.pull_refresh_list);
        mLabelAdapter = new LabelAdapter(this);
        mListView.setAdapter(mLabelAdapter);
        mListView.getRefreshableView().setAdapter(mLabelAdapter);
        mListView.setMode(PullToRefreshBase.Mode.DISABLED);
        mListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position = (int) id;
                Friend friend = mFriendList.get(position);
                if (friend != null) {
                    Intent intent = new Intent(CreateLabelActivity.this, BasicInfoActivity.class);
                    intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                    startActivity(intent);
                }
            }
        });
    }

    private void initEvent() {
        mLabelEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                changeTitle(0, s.toString().trim());
                if (!TextUtils.isEmpty(s.toString().trim())) {
                    mLabelEdit.setTextColor(getResources().getColor(R.color.app_black));
                    changeTitle(2, "");
                } else {
                    mLabelEdit.setTextColor(getResources().getColor(R.color.Grey_400));
                    changeTitle(1, "");
                }
            }
        });
    }

    private void changeTitle(int i, String str) {
        mTitleRight.setTextColor(getResources().getColor(R.color.white));
        if (i == 0) {
            if (TextUtils.isEmpty(str)) {
                if (isEditLabel) {
                    mTvTitle.setText(R.string.edit_tag);
                } else {
                    mTvTitle.setText(R.string.create_tag);
                }
            } else {
                mTvTitle.setText(str);

            }
        } else if (i == 1) {
            mTitleRight.setAlpha(0.5f);
            mTitleRight.setOnClickListener(null);
        } else {
            mTitleRight.setAlpha(1.0f);
            mTitleRight.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_left:
                finish();
                break;
            case R.id.tv_title_right:
                if (isEditLabel) {
                    DialogHelper.showDefaulteMessageProgressDialog(this);
                    if (!mOldLabel.getGroupName().equals(mLabelEdit.getText().toString())) {// 标签名已改变
                        updateLabelName();
                    }
                    updateLabelUserIdList(mOldLabel);
                } else {
                    createLabel();
                }
                break;
            case R.id.add_label_user:
                Intent intent = new Intent(this, SelectLabelFriendActivity.class);
                List<String> ids = new ArrayList<>();
                for (int i = 0; i < mFriendList.size(); i++) {
                    ids.add(mFriendList.get(i).getUserId());
                }
                intent.putExtra("exist_ids", JSON.toJSONString(ids));
                startActivityForResult(intent, 0x01);
                break;
        }
    }

    // 创建标签
    private void createLabel() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("groupName", mLabelEdit.getText().toString());
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().FRIENDGROUP_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ObjectResult<Label> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            LabelDao.getInstance().createLabel(result.getData());
                            updateLabelUserIdList(result.getData());
                        } else {
                            DialogHelper.dismissProgressDialog();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    // 修改标签名称
    private void updateLabelName() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("groupId", mOldLabel.getGroupId());
        params.put("groupName", mLabelEdit.getText().toString());

        HttpUtils.get().url(coreManager.getConfig().FRIENDGROUP_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ObjectResult<Label> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            LabelDao.getInstance().updateLabelName(mLoginUserId, mOldLabel.getGroupId(), mLabelEdit.getText().toString());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    // 修改标签下的成员
    private void updateLabelUserIdList(final Label label) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("groupId", label.getGroupId());
        String userIdListStr = "";
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < mFriendList.size(); i++) {
            if (i == mFriendList.size() - 1) {
                userIdListStr += mFriendList.get(i).getUserId();
            } else {
                userIdListStr += mFriendList.get(i).getUserId() + ",";
            }
            strings.add(mFriendList.get(i).getUserId());
        }
        params.put("userIdListStr", userIdListStr);
        final String userIdList = JSON.toJSONString(strings);

        HttpUtils.get().url(coreManager.getConfig().FRIENDGROUP_UPDATEGROUPUSERLIST)
                .params(params)
                .build()
                .execute(new BaseCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ObjectResult<Label> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            LabelDao.getInstance().updateLabelUserIdList(mLoginUserId, label.getGroupId(), userIdList);
                            setResult(RESULT_OK, new Intent());// 通知界面刷新
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    @Override
    @SuppressLint("SetTextI18n")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x01 && resultCode == RESULT_OK) {
            String userIds = data.getStringExtra("inviteId");
            String userNames = data.getStringExtra("inviteName");
            List<String> userIdList = JSON.parseArray(userIds, String.class);
            List<String> userNameList = JSON.parseArray(userNames, String.class);
            for (int i = 0; i < userIdList.size(); i++) {
                Friend friend = new Friend();
                friend.setUserId(userIdList.get(i));
                friend.setNickName(userNameList.get(i));
                mFriendList.add(friend);
            }
            mLabelAdapter.notifyDataSetChanged();
            if (!TextUtils.isEmpty(mLabelEdit.getText().toString())) {
                changeTitle(2, "");
            }
            mLabelUserSizeTv.setText(getString(R.string.tag_member) + "(" + mFriendList.size() + ")");
        }
    }

    class LabelAdapter extends SlideBaseAdapter {

        public LabelAdapter(Context context) {
            super(context);
        }

        @Override
        public int getFrontViewId(int position) {
            return R.layout.row_create_label;
        }

        @Override
        public int getLeftBackViewId(int position) {
            return 0;
        }

        @Override
        public int getRightBackViewId(int position) {
            return R.layout.row_item_delete;
        }

        @Override
        public int getCount() {
            if (mFriendList != null) {
                return mFriendList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mFriendList != null) {
                return mFriendList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = createConvertView(position);
            }
            ImageView labelIv = ViewHolder.get(convertView, R.id.label_avatar);
            TextView labelUserNameTv = ViewHolder.get(convertView, R.id.label_user_name);
            TextView delete_tv = ViewHolder.get(convertView, R.id.delete_tv);
            View view = ViewHolder.get(convertView, R.id.view);

            final Friend friend = mFriendList.get(position);
            if (friend != null) {
                AvatarHelper.getInstance().displayAvatar(friend.getUserId(), labelIv);
                labelUserNameTv.setText(!TextUtils.isEmpty(friend.getRemarkName()) ? friend.getRemarkName() : friend.getNickName());
            }
            delete_tv.setOnClickListener(v -> {
                changeTitle(2, "");
                mFriendList.remove(position);
                notifyDataSetChanged();
                mLabelUserSizeTv.setText(getString(R.string.tag_member) + "(" + mFriendList.size() + ")");
            });

            view.setVisibility(position == mFriendList.size() - 1 ? View.GONE : View.VISIBLE);

            return convertView;
        }
    }
}
