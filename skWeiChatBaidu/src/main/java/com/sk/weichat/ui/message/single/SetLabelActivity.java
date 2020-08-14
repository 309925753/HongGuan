package com.sk.weichat.ui.message.single;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.Label;
import com.sk.weichat.db.dao.LabelDao;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.CommonAdapter;
import com.sk.weichat.util.CommonViewHolder;
import com.sk.weichat.util.SkinUtils;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class SetLabelActivity extends BaseActivity implements View.OnClickListener {
    int size = 0;
    private GridView mBelongLabelGrid;
    private BelongLabelAdapter mBelongLabelAdapter;
    private List<Label> mBelongLabelList;
    private EditText mLabelEdit;
    private ListView mSearchLv;
    private SearchAdapter mSearchAdapter;
    private List<Label> mSearchLabelList;
    private GridView mAllLabelGrid;
    private AllLabelAdapter mAllLabelAdapter;
    private List<Label> mAllLabelList;
    private List<Label> mNewAndOldLabelList;// 所有标签(已有的、新增的)
    private List<String> mNewGroupId;// 在服务端新创建的标签id
    private CreateLabelListener mCreateLabelListener;
    private String mLoginUserId;
    private String mFriendId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_label);

        mLoginUserId = coreManager.getSelf().getUserId();
        mFriendId = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);

        initActionBar();
        initData();
        initView();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        ImageView imageView = findViewById(R.id.iv_title_left);
        imageView.setOnClickListener(this);
        TextView mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(R.string.edit_tag);
        TextView mTvTitleRight = (TextView) findViewById(R.id.tv_title_right);
        mTvTitleRight.setTextColor(getResources().getColor(R.color.white));
        mTvTitleRight.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_grey_circle));
        ViewCompat.setBackgroundTintList(mTvTitleRight, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
        mTvTitleRight.setText(getString(R.string.finish));
        mTvTitleRight.setOnClickListener(this);
    }

    private void initData() {
        mBelongLabelList = new ArrayList<>();
        mSearchLabelList = new ArrayList<>();
        mAllLabelList = new ArrayList<>();
        mNewAndOldLabelList = new ArrayList<>();
        mNewGroupId = new ArrayList<>();

        // 获得该用户所属的标签
        List<Label> friendLabelList = LabelDao.getInstance().getFriendLabelList(mLoginUserId, mFriendId);
        mBelongLabelList.addAll(friendLabelList);
        // 获得所有标签
        List<Label> allLabels = LabelDao.getInstance().getAllLabels(mLoginUserId);
        mAllLabelList.addAll(allLabels);
        for (int i = 0; i < mBelongLabelList.size(); i++) {
            for (int i1 = 0; i1 < mAllLabelList.size(); i1++) {
                if (mAllLabelList.get(i1).getGroupId().equals(mBelongLabelList.get(i).getGroupId())) {
                    mAllLabelList.get(i1).setSelected(true);// 为选中状态
                }
            }
        }

        mNewAndOldLabelList.addAll(allLabels);
    }

    private void initView() {
        ((TextView) findViewById(R.id.tvAllLabel)).setTextColor(SkinUtils.getSkin(this).getAccentColor());
        mBelongLabelGrid = (GridView) findViewById(R.id.belong_label_grid);
        mBelongLabelAdapter = new BelongLabelAdapter(this, mBelongLabelList);
        mBelongLabelGrid.setAdapter(mBelongLabelAdapter);
        if (mBelongLabelList.size() > 0) {
            mBelongLabelGrid.setVisibility(View.VISIBLE);
        }

        mLabelEdit = (EditText) findViewById(R.id.edit_label);

        mSearchLv = (ListView) findViewById(R.id.search_lv);
        mSearchAdapter = new SearchAdapter(this, mSearchLabelList);
        mSearchLv.setAdapter(mSearchAdapter);

        mAllLabelGrid = (GridView) findViewById(R.id.all_label_grid);
        mAllLabelAdapter = new AllLabelAdapter(this, mAllLabelList);
        mAllLabelGrid.setAdapter(mAllLabelAdapter);

        mCreateLabelListener = new CreateLabelListener() {
            @Override
            public void createSuccessful() {
                size = size - 1;
                if (size == 0) {// 最后一个也创建完成
                    List<String> mGroupIdList = new ArrayList<>();
                    for (int i = 0; i < mBelongLabelList.size(); i++) {
                        if (!TextUtils.isEmpty(mBelongLabelList.get(i).getGroupId())) {
                            mGroupIdList.add(mBelongLabelList.get(i).getGroupId());
                        }
                    }
                    // 新创建的标签id也要传过去，因为当前用户存在于该标签下，如若不传，过程为 标签创建成功-人员添加成功-服务端将该好友从标签内移除
                    mGroupIdList.addAll(mNewGroupId);
                    updateLabelUserIdList(mGroupIdList);
                }
            }

            @Override
            public void createFailed(String groupName) {
                Toast.makeText(SetLabelActivity.this, getString(R.string.tip_create_tag_failed_place_holder, groupName), Toast.LENGTH_SHORT).show();
            }
        };
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
/*
                // 在输入过程中，显示搜索ListView，隐藏全部GridVIew
                mSearchLabelList.clear();
                if (TextUtils.isEmpty(s.toString())) {
                    mSearchLv.setVisibility(View.GONE);
                    findViewById(R.id.all_label_rl).setVisibility(View.VISIBLE);
                } else {
                    mSearchLv.setVisibility(View.VISIBLE);
                    findViewById(R.id.all_label_rl).setVisibility(View.GONE);
                    for (Label label : mAllLabelList) {
                        if (!label.isSelected() && label.getGroupName().contains(s.toString())) {// 未被选中且关键字匹配
                            mSearchLabelList.add(label);
                        }
                    }
                }
                mSearchAdapter.notifyDataSetChanged();
*/
            }
        });

        mLabelEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() != KeyEvent.ACTION_UP) {// 会回调两次, 抬起不响应 只回调一次
                    if (TextUtils.isEmpty(mLabelEdit.getText().toString()) && mBelongLabelList.size() > 0) {// 输入框为空且选中数据不为空
                        Label label = mBelongLabelList.get(mBelongLabelList.size() - 1);
                        if (label.isSelectedInBelong()) {// 已选中，直接删除
                            mBelongLabelList.remove(label);
                            if (!TextUtils.isEmpty(label.getGroupId())) {// 老标签
                                for (int i = 0; i < mAllLabelList.size(); i++) {
                                    if (mAllLabelList.get(i).getGroupId().equals(label.getGroupId())) {
                                        mAllLabelList.get(i).setSelected(false);
                                    }
                                }
                                mAllLabelAdapter.notifyDataSetChanged();
                            } else {// 新标签
                                for (int i = 0; i < mNewAndOldLabelList.size(); i++) {
                                    if (mNewAndOldLabelList.get(i).getGroupName().equals(label.getGroupName())) {
                                        mNewAndOldLabelList.remove(i);
                                    }
                                }
                            }
                            mBelongLabelAdapter.notifyDataSetChanged();
                            if (mBelongLabelList.size() == 0) {
                                mBelongLabelGrid.setVisibility(View.GONE);
                            }
                        } else {
                            // mBelongLabelList.get(mBelongLabelList.size() - 1).setSelectedInBelong(true);
                            label.setSelectedInBelong(true);
                            mBelongLabelAdapter.notifyDataSetChanged();
                        }
                    }
                }
                return false;
            }
        });

        TextView sure_label = findViewById(R.id.sure_label);
        sure_label.setOnClickListener(this);
        sure_label.setTextColor(SkinUtils.getSkin(this).getAccentColor());
        mBelongLabelGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mBelongLabelList.get(position).isSelectedInBelong()) {
                    mBelongLabelList.get(position).setSelectedInBelong(false);
                } else {
                    mBelongLabelList.get(position).setSelectedInBelong(true);
                }
                mBelongLabelAdapter.notifyDataSetChanged();
            }
        });

        mSearchLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Label label = mSearchLabelList.get(position);
                label.setSelected(true);
                label.setSelectedInBelong(false);
                mBelongLabelList.add(label);
                operatingSearchLv(label);

                mLabelEdit.setText("");// 清空输入框
            }
        });

        mAllLabelGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAllLabelList.get(position).isSelected()) {
                    mAllLabelList.get(position).setSelected(false);
                    operatingAllGrid(false, mAllLabelList.get(position));
                } else {
                    mAllLabelList.get(position).setSelected(true);
                    operatingAllGrid(true, mAllLabelList.get(position));
                }
            }
        });
    }

    private void deleteLabelInBelong(Label label) {
        if (TextUtils.isEmpty(label.getGroupId())) {
            for (int i = 0; i < mNewAndOldLabelList.size(); i++) {
                if (mNewAndOldLabelList.get(i).getGroupName().equals(label.getGroupName())) {
                    mNewAndOldLabelList.remove(i);
                }
            }
        } else {
            for (int i = 0; i < mAllLabelList.size(); i++) {
                if (mAllLabelList.get(i).getGroupId().equals(label.getGroupId())) {
                    mAllLabelList.get(i).setSelectedInBelong(false);
                    mAllLabelList.get(i).setSelected(false);
                }
            }
            mAllLabelAdapter.notifyDataSetChanged();
        }
        mBelongLabelAdapter.notifyDataSetChanged();
        if (mBelongLabelList.size() > 1) {
            mBelongLabelGrid.setVisibility(View.VISIBLE);
        } else {
            mBelongLabelGrid.setVisibility(View.GONE);
        }
    }

    private void operatingSearchLv(Label label) {
        for (int i = 0; i < mAllLabelList.size(); i++) {
            if (mAllLabelList.get(i).getGroupId().equals(label.getGroupId())) {
                mAllLabelList.get(i).setSelected(true);
            }
        }
        mBelongLabelAdapter.notifyDataSetChanged();
        mAllLabelAdapter.notifyDataSetChanged();
        if (mBelongLabelList.size() > 0) {
            mBelongLabelGrid.setVisibility(View.VISIBLE);
        } else {
            mBelongLabelGrid.setVisibility(View.GONE);
        }
    }

    private void operatingAllGrid(boolean isAdd, Label label) {
        label.setSelectedInBelong(false);
        if (isAdd) {
            mBelongLabelList.add(label);
        } else {
            for (int i = 0; i < mBelongLabelList.size(); i++) {
                if (!TextUtils.isEmpty(mBelongLabelList.get(i).getGroupId())) {
                    if (mBelongLabelList.get(i).getGroupId().equals(label.getGroupId())) {
                        mBelongLabelList.remove(i);
                    }
                }
            }
        }
        mBelongLabelAdapter.notifyDataSetChanged();
        mAllLabelAdapter.notifyDataSetChanged();
        if (mBelongLabelList.size() > 0) {
            mBelongLabelGrid.setVisibility(View.VISIBLE);
        } else {
            mBelongLabelGrid.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_title_left:
                finish();
                break;
            case R.id.sure_label:
                String mEditLabelName = mLabelEdit.getText().toString().trim();
                if (!TextUtils.isEmpty(mEditLabelName)) {
                    boolean isExist = false;
                    for (int i = 0; i < mNewAndOldLabelList.size(); i++) {
                        if (mNewAndOldLabelList.get(i).getGroupName().equals(mEditLabelName)) {
                            isExist = true;
                        }
                    }
                    if (!isExist) {
                        Label label = new Label();
                        label.setUserId(mLoginUserId);
                        label.setGroupName(mEditLabelName);
                        label.setSelectedInBelong(false);
                        mBelongLabelList.add(label);
                        mNewAndOldLabelList.add(label);
                        mBelongLabelAdapter.notifyDataSetChanged();
                        if (mBelongLabelList.size() > 0) {
                            mBelongLabelGrid.setVisibility(View.VISIBLE);
                        }
                    } else {   // 虽然搜索结果显示出来了，但有些用户可能不会去点击ListView'item，而是继续点击确认按钮，所以需要坐下兼容
                        for (Label label : mSearchLabelList) {
                            if (label.getGroupName().equals(mEditLabelName)) {
                                label.setSelected(true);
                                label.setSelectedInBelong(false);
                                mBelongLabelList.add(label);
                                operatingSearchLv(label);
                            }
                        }
                    }
                    mLabelEdit.setText("");// 清空EditText
                }
                break;
            case R.id.tv_title_right:
                List<String> mNewGroupList = new ArrayList<>();
                List<String> mGroupIdList = new ArrayList<>();
                for (int i = 0; i < mBelongLabelList.size(); i++) {
                    if (TextUtils.isEmpty(mBelongLabelList.get(i).getGroupId())) {// 新添加的标签
                        mNewGroupList.add(mBelongLabelList.get(i).getGroupName());
                    } else {// 所有标签内加入的标签
                        mGroupIdList.add(mBelongLabelList.get(i).getGroupId());
                    }
                }
                if (mNewGroupList.size() > 0) {
                    size = mNewGroupList.size();
                    for (String s : mNewGroupList) {
                        createLabel(s);
                    }
                } else {
                    updateLabelUserIdList(mGroupIdList);
                }
                break;
        }
    }

    // 创建标签
    private void createLabel(final String groupName) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("groupName", groupName);

        HttpUtils.get().url(coreManager.getConfig().FRIENDGROUP_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ObjectResult<Label> result) {
                        if (result.getResultCode() == 1) {
                            createLabelUserIdList(result.getData());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (mCreateLabelListener != null) {
                            mCreateLabelListener.createFailed(groupName);
                        }
                    }
                });
    }

    private void createLabelUserIdList(final Label label) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("groupId", label.getGroupId());
        params.put("userIdListStr", mFriendId);

        HttpUtils.get().url(coreManager.getConfig().FRIENDGROUP_UPDATEGROUPUSERLIST)
                .params(params)
                .build()
                .execute(new BaseCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ObjectResult<Label> result) {
                        if (result.getResultCode() == 1) {
                            mNewGroupId.add(label.getGroupId());// 记录新创建的id，到时候调用updateLabelUserIdList方法需要传入

                            if (mCreateLabelListener != null) {
                                mCreateLabelListener.createSuccessful();
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    // 更新选中的标签lUserIdList
    private void updateLabelUserIdList(final List<String> groupIdList) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserId", mFriendId);
        String groupIdStr = "";
        for (int i = 0; i < groupIdList.size(); i++) {
            if (i == groupIdList.size() - 1) {
                groupIdStr += groupIdList.get(i);
            } else {
                groupIdStr += groupIdList.get(i) + ",";
            }
        }
        params.put("groupIdStr", groupIdStr);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().FRIENDGROUP_UPDATEFRIEND)
                .params(params)
                .build()
                .execute(new BaseCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ObjectResult<Label> result) {
                        if (result.getResultCode() == 1) {
                            refreshLabelListFromService();
                            /*List<String> alwaysList = new ArrayList<>();
                            List<Label> friendLabelList = LabelDao.getInstance().getFriendLabelList(mLoginUserId, mFriendId);
                            for (String s : groupIdList) {
                                for (Label label : friendLabelList) {
                                    if (label.getGroupId().equals(s)) {
                                        alwaysList.add(s);
                                    }
                                }
                            }
                            for (String always : alwaysList) {
                                for (Label label : friendLabelList) {
                                    if (label.getGroupId().equals(always)) {
                                        friendLabelList.removeItemMessage(label);
                                    }
                                }
                            }
                            // 剩下的Label需要移除该Friend
                            for (Label label : friendLabelList) {
                                Label label1 = LabelDao.getInstance().getLabel(mLoginUserId, label.getGroupId());
                                if (label1 != null) {
                                    if (label1.getUserIdList().contains(mFriendId)) {
                                        List<String> userIdList = JSON.parseArray(label1.getUserIdList(), String.class);
                                        userIdList.removeItemMessage(mFriendId);
                                        LabelDao.getInstance().updateLabelUserIdList(mLoginUserId, label1.getGroupId(), JSON.toJSONString(userIdList));
                                    }
                                }
                            }

                            for (String s : groupIdList) {// 服务端修改成功，本地也需要添加
                                Label label = LabelDao.getInstance().getLabel(mLoginUserId, s);
                                if (!label.getUserIdList().contains(mFriendId)) {
                                    List<String> userIdList = JSON.parseArray(label.getUserIdList(), String.class);
                                    userIdList.add(mFriendId);
                                    LabelDao.getInstance().updateLabelUserIdList(mLoginUserId, s, JSON.toJSONString(userIdList));
                                }
                            }*/
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

    // 从服务端下载标签
    private void refreshLabelListFromService() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.get().url(coreManager.getConfig().FRIENDGROUP_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ArrayResult<Label> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            List<Label> labelList = result.getData();
                            LabelDao.getInstance().refreshLabel(mLoginUserId, labelList);
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    interface CreateLabelListener {
        void createSuccessful();

        void createFailed(String groupName);
    }

    class BelongLabelAdapter extends CommonAdapter<Label> {
        public BelongLabelAdapter(Context context, List<Label> data) {
            super(context, data);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.row_set_label, position);
            RelativeLayout ll = viewHolder.getView(R.id.ll);
            TextView labelNameTv = viewHolder.getView(R.id.set_label_tv);
            SkinUtils.Skin skin = SkinUtils.getSkin(mContext);
            ImageView delete = viewHolder.getView(R.id.delete);
            final Label label = data.get(position);
            if (label != null) {
                if (label.isSelectedInBelong()) {
                    labelNameTv.setTextColor(getResources().getColor(R.color.black));
                    delete.setVisibility(View.VISIBLE);
                } else {
                    ll.setBackground(null);
                    labelNameTv.setBackgroundResource(R.drawable.bg_label_empty);
                    GradientDrawable bg = (GradientDrawable) labelNameTv.getBackground();
                    bg.setStroke(2, skin.getAccentColor());
                    labelNameTv.setTextColor(Color.BLACK);
                    delete.setVisibility(View.GONE);
                }
                labelNameTv.setText(label.getGroupName());
            }

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (label != null) {
                        deleteLabelInBelong(label);
                        mBelongLabelList.remove(position);
                    }
                }
            });
            return viewHolder.getConvertView();
        }
    }

    class AllLabelAdapter extends CommonAdapter<Label> {
        public AllLabelAdapter(Context context, List<Label> data) {
            super(context, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.row_set_label, position);
            TextView labelNameTv = viewHolder.getView(R.id.set_label_tv);
            SkinUtils.Skin skin = SkinUtils.getSkin(mContext);
            Label label = data.get(position);
            if (label != null) {
                if (label.isSelected()) {
                    labelNameTv.setBackgroundResource(R.drawable.bg_label_empty);
                    GradientDrawable bg = (GradientDrawable) labelNameTv.getBackground();
                    bg.setStroke(2, skin.getAccentColor());
                    labelNameTv.setTextColor(getResources().getColor(R.color.black));
                } else {
                    labelNameTv.setBackgroundResource(R.drawable.a_bg_set_label3);
                    labelNameTv.setTextColor(getResources().getColor(R.color.black));
                }
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                labelNameTv.setText(label.getGroupName());
            }

            return viewHolder.getConvertView();
        }
    }

    class SearchAdapter extends CommonAdapter<Label> {
        public SearchAdapter(Context context, List<Label> data) {
            super(context, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.row_set_label_search, position);
            TextView labelNameTv = viewHolder.getView(R.id.set_label_tv);
            Label label = data.get(position);
            if (label != null) {
                labelNameTv.setText(label.getGroupName());
            }
            return viewHolder.getConvertView();
        }
    }
}
