package com.sk.weichat.ui.message.multi;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.RoomMember;
import com.sk.weichat.bean.message.MucRoomMember;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.RoomMemberDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.sortlist.BaseComparator;
import com.sk.weichat.sortlist.BaseSortModel;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.other.BasicInfoActivity;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.ViewHolder;
import com.sk.weichat.view.BannedDialog;
import com.sk.weichat.view.SelectionFrame;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * Features: 展示群成员
 * Features  禁言 && 删除群成员
 * Features: 群主对群内成员备注
 * <p>
 * 因为管理员也可以进入该界面进行前三种操作，且管理员需要显示userName 群主显示cardName 所以需要区分下
 * // Todo 当群组人数过多时，排序需要很久，先干掉排序功能，考虑替换排序规则
 */
public class GroupMoreFeaturesActivity extends BaseActivity {
    private EditText mEditText;
    private boolean isSearch;

    private PullToRefreshListView mListView;
    private GroupMoreFeaturesAdapter mAdapter;
    // private List<BaseSortModel<RoomMember>> mSortRoomMember;
    // private List<BaseSortModel<RoomMember>> mSearchSortRoomMember;
    private List<RoomMember> mSortRoomMember;
    private List<RoomMember> mSearchSortRoomMember;
    private BaseComparator<RoomMember> mBaseComparator;

    private TextView mTextDialog;

    private String mRoomId;
    private boolean isLoadByService;
    private boolean isBanned;
    private boolean isDelete;
    private boolean isSetRemark;

    private RoomMember mRoomMember;
    private Map<String, String> mRemarksMap = new HashMap<>();
    private List<MucRoomMember> roomMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_all_member);
        mRoomId = getIntent().getStringExtra("roomId");
        isLoadByService = getIntent().getBooleanExtra("isLoadByService", false);
        isBanned = getIntent().getBooleanExtra("isBanned", false);
        isDelete = getIntent().getBooleanExtra("isDelete", false);
        isSetRemark = getIntent().getBooleanExtra("isSetRemark", false);

        initActionBar();
        initData();
        initView();
        if (isLoadByService) {
            loadDataByService(false);
        }
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
        tvTitle.setText(R.string.group_member);

    }

    private void initData() {
        AsyncUtils.doAsync(this, c -> {
            List<Friend> mFriendList = FriendDao.getInstance().getAllFriends(coreManager.getSelf().getUserId());
            for (int i = 0; i < mFriendList.size(); i++) {
                if (!TextUtils.isEmpty(mFriendList.get(i).getRemarkName())) {// 针对该好友进行了备注
                    mRemarksMap.put(mFriendList.get(i).getUserId(), mFriendList.get(i).getRemarkName());
                }
            }
            c.uiThread(r -> {
                mAdapter.notifyDataSetChanged();// 刷新页面
            });
        });

        mSortRoomMember = new ArrayList<>();
        mSearchSortRoomMember = new ArrayList<>();
        mBaseComparator = new BaseComparator<>();

        List<RoomMember> data = RoomMemberDao.getInstance().getRoomMember(mRoomId);
        mRoomMember = RoomMemberDao.getInstance().getSingleRoomMember(mRoomId, coreManager.getSelf().getUserId());

        mSortRoomMember.addAll(data);
    }

    private void initView() {
        mListView = findViewById(R.id.pull_refresh_list);
        if (!isLoadByService) {// 不支持刷新
            mListView.setMode(PullToRefreshBase.Mode.DISABLED);
        }
        mAdapter = new GroupMoreFeaturesAdapter(mSortRoomMember);
        mListView.getRefreshableView().setAdapter(mAdapter);

        mEditText = (EditText) findViewById(R.id.search_et);
        mEditText.setHint(getString(R.string.search));
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                isSearch = true;
                mListView.setMode(PullToRefreshBase.Mode.DISABLED);
                mSearchSortRoomMember.clear();
                String str = mEditText.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    isSearch = false;
                    mListView.setMode(PullToRefreshBase.Mode.BOTH);
                    mAdapter.setData(mSortRoomMember);
                    return;
                }
//                for (int i = 0; i < mSortRoomMember.size(); i++) {
///*
//                    if (getName(mSortRoomMember.get(i).getBean()).contains(str)) { // 符合搜索条件的好友
//                        mSearchSortRoomMember.add((mSortRoomMember.get(i)));
//                    }
//*/
//
//                    if (getName(mSortRoomMember.get(i)).contains(str)) { // 符合搜索条件的好友
//                        mSearchSortRoomMember.add((mSortRoomMember.get(i)));
//                    }
//                }
//                mAdapter.setData(mSearchSortRoomMember);
                searchMember(mRoomId, str);// 调接口搜索
            }
        });

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadDataByService(true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadDataByService(false);
            }
        });

        mListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BaseSortModel<RoomMember> baseSortModel;
                final RoomMember roomMember;
                if (isSearch) {
                    // baseSortModel = mSearchSortRoomMember..get((int) id);
                    roomMember = mSearchSortRoomMember.get((int) id);
                } else {
                    // baseSortModel = mSortRoomMember..get((int) id);
                    roomMember = mSortRoomMember.get((int) id);
                }

                if (isDelete) {// 踢人
                    if (roomMember.getUserId().equals(coreManager.getSelf().getUserId())) {
                        ToastUtil.showToast(mContext, R.string.can_not_remove_self);
                        return;
                    }
                    if (roomMember.getRole() == 1) {
                        ToastUtil.showToast(mContext, getString(R.string.tip_cannot_remove_owner));
                        return;
                    }

                    if (roomMember.getRole() == 2 && mRoomMember != null && mRoomMember.getRole() != 1) {
                        ToastUtil.showToast(mContext, getString(R.string.tip_cannot_remove_manager));
                        return;
                    }

                    SelectionFrame mSF = new SelectionFrame(GroupMoreFeaturesActivity.this);
                    mSF.setSomething(null, getString(R.string.sure_remove_member_for_group, getName(roomMember)),
                            new SelectionFrame.OnSelectionFrameClickListener() {
                                @Override
                                public void cancelClick() {

                                }

                                @Override
                                public void confirmClick() {
                                    deleteMember(roomMember, roomMember.getUserId());
                                }
                            });
                    mSF.show();
                } else if (isBanned) {// 禁言
                    if (roomMember.getUserId().equals(coreManager.getSelf().getUserId())) {
                        ToastUtil.showToast(mContext, R.string.can_not_banned_self);
                        return;
                    }

                    if (roomMember.getRole() == 1) {
                        ToastUtil.showToast(mContext, getString(R.string.tip_cannot_ban_owner));
                        return;
                    }

                    if (roomMember.getRole() == 2) {
                        ToastUtil.showToast(mContext, getString(R.string.tip_cannot_ban_manager));
                        return;
                    }

                    showBannedDialog(roomMember.getUserId());
                } else if (isSetRemark) {// 备注
                    if (roomMember.getUserId().equals(coreManager.getSelf().getUserId())) {
                        ToastUtil.showToast(mContext, R.string.can_not_remark_self);
                        return;
                    }
                    setRemarkName(roomMember.getUserId(), getName(roomMember));
                } else {
                    BasicInfoActivity.start(mContext, roomMember.getUserId(), BasicInfoActivity.FROM_ADD_TYPE_GROUP);
                }
            }
        });
    }

    private void searchMember(String roomId, String keyword) {
        roomMembers = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put("roomId", roomId);
        params.put("keyword", keyword);

        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_SEARCH)
                .params(params)
                .build()
                .execute(new ListCallback<MucRoomMember>(MucRoomMember.class) {

                    @Override
                    public void onResponse(ArrayResult<MucRoomMember> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            roomMembers = result.getData();
                            if (roomMembers.size() > 0) {
                                for (int i = 0; i < roomMembers.size(); i++) {
                                    RoomMember roomMember = new RoomMember();
                                    roomMember.setRoomId(mRoomId);
                                    roomMember.setUserId(roomMembers.get(i).getUserId());
                                    roomMember.setUserName(roomMembers.get(i).getNickName());
                                    if (TextUtils.isEmpty(roomMembers.get(i).getRemarkName())) {
                                        roomMember.setCardName(roomMembers.get(i).getNickName());
                                    } else {
                                        roomMember.setCardName(roomMembers.get(i).getRemarkName());
                                    }
                                    roomMember.setRole(roomMembers.get(i).getRole());
                                    roomMember.setCreateTime(roomMembers.get(i).getCreateTime());
                                    mSearchSortRoomMember.add(roomMember);
                                }
                            }
                            mAdapter.setData(mSearchSortRoomMember);
                        } else {
                            mAdapter.setData(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        mAdapter.setData(new ArrayList<>());
                    }
                });
    }

    private void loadDataByService(boolean reset) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        if (reset) {
            params.put("joinTime", String.valueOf(0));
        } else {
            long lastRoamingTime = PreferenceUtils.getLong(MyApplication.getContext(), Constants.MUC_MEMBER_LAST_JOIN_TIME + coreManager.getSelf().getUserId() + mRoomId, 0);
            params.put("joinTime", String.valueOf(lastRoamingTime));
        }
        params.put("pageSize", Constants.MUC_MEMBER_PAGE_SIZE);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<MucRoomMember>(MucRoomMember.class) {
                    @Override
                    public void onResponse(ArrayResult<MucRoomMember> result) {
                        if (reset) {
                            mListView.onPullDownRefreshComplete();
                        } else {
                            mListView.onPullUpRefreshComplete();
                        }

                        HashMap<String, String> toRepeatHashMap = new HashMap<>();
                        for (RoomMember member : mSortRoomMember) {
                            toRepeatHashMap.put(member.getUserId(), member.getUserId());
                        }

                        if (Result.checkSuccess(mContext, result)) {
                            List<MucRoomMember> mucRoomMemberList = result.getData();
                            if (mucRoomMemberList.size() == Integer.valueOf(Constants.MUC_MEMBER_PAGE_SIZE)) {
                                mListView.setMode(PullToRefreshBase.Mode.BOTH);
                            } else {
                                mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                            }
                            if (mucRoomMemberList.size() > 0) {
                                List<RoomMember> roomMemberList = new ArrayList<>();
                                for (int i = 0; i < mucRoomMemberList.size(); i++) {
                                    if (!reset &&
                                            toRepeatHashMap.containsKey(mucRoomMemberList.get(i).getUserId())) {
                                        continue;
                                    }
                                    RoomMember roomMember = new RoomMember();
                                    roomMember.setRoomId(mRoomId);
                                    roomMember.setUserId(mucRoomMemberList.get(i).getUserId());
                                    roomMember.setUserName(mucRoomMemberList.get(i).getNickName());
                                    if (TextUtils.isEmpty(mucRoomMemberList.get(i).getRemarkName())) {
                                        roomMember.setCardName(mucRoomMemberList.get(i).getNickName());
                                    } else {
                                        roomMember.setCardName(mucRoomMemberList.get(i).getRemarkName());
                                    }
                                    roomMember.setRole(mucRoomMemberList.get(i).getRole());
                                    roomMember.setCreateTime(mucRoomMemberList.get(i).getCreateTime());
                                    roomMemberList.add(roomMember);
                                }

                                if (reset) {
                                    RoomMemberDao.getInstance().deleteRoomMemberTable(mRoomId);
                                }
                                AsyncUtils.doAsync(this, mucChatActivityAsyncContext -> {
                                    for (int i = 0; i < roomMemberList.size(); i++) {// 在异步任务内存储
                                        RoomMemberDao.getInstance().saveSingleRoomMember(mRoomId, roomMemberList.get(i));
                                    }
                                });

                                RoomInfoActivity.saveMucLastRoamingTime(coreManager.getSelf().getUserId(), mRoomId, mucRoomMemberList.get(mucRoomMemberList.size() - 1).getCreateTime(), reset);

                                // 刷新本地数据
                                if (reset) {
                                    mSortRoomMember.clear();
                                    mSortRoomMember.addAll(roomMemberList);
                                    mAdapter.notifyDataSetInvalidated();
                                } else {
                                    mSortRoomMember.addAll(roomMemberList);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (reset) {
                            mListView.onPullDownRefreshComplete();
                        } else {
                            mListView.onPullUpRefreshComplete();
                        }
                        ToastUtil.showErrorNet(getApplicationContext());
                    }
                });
    }

    private String getName(RoomMember member) {
        if (mRoomMember != null && mRoomMember.getRole() == 1) {
            if (!TextUtils.equals(member.getUserName(), member.getCardName())) {// 当userName与cardName不一致时，我们认为群主有设置群内备注
                return member.getCardName();
            } else {
                if (mRemarksMap.containsKey(member.getUserId())) {
                    return mRemarksMap.get(member.getUserId());
                } else {
                    return member.getUserName();
                }
            }
        } else {
            if (mRemarksMap.containsKey(member.getUserId())) {
                return mRemarksMap.get(member.getUserId());
            } else {
                return member.getUserName();
            }
        }
    }


    private void showBannedDialog(final String userId) {
        final int daySeconds = 24 * 60 * 60;
        BannedDialog bannedDialog = new BannedDialog(mContext, new BannedDialog.OnBannedDialogClickListener() {

            @Override
            public void tv1Click() {
                bannedVoice(userId, 0);
            }

            @Override
            public void tv2Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() + daySeconds / 48);
            }

            @Override
            public void tv3Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() + daySeconds / 24);
            }

            @Override
            public void tv4Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() + daySeconds);
            }

            @Override
            public void tv5Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() + daySeconds * 3);
            }

            @Override
            public void tv6Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() + daySeconds * 7);
            }

            @Override
            public void tv7Click() {
                bannedVoice(userId, TimeUtils.sk_time_current_time() + daySeconds * 15);
            }
        });
        bannedDialog.show();
    }

    /**
     * 删除群成员
     */
    private void deleteMember(final RoomMember baseSortModel, final String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        params.put("userId", userId);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_DELETE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            Toast.makeText(mContext, R.string.remove_success, Toast.LENGTH_SHORT).show();
                            mSortRoomMember.remove(baseSortModel);
                            mEditText.setText("");

                            RoomMemberDao.getInstance().deleteRoomMember(mRoomId, userId);
                            EventBus.getDefault().post(new EventGroupStatus(10001, Integer.valueOf(userId)));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    /**
     * 禁言
     */
    private void bannedVoice(String userId, final long time) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        params.put("userId", userId);
        params.put("talkTime", String.valueOf(time));
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            if (time == 0) {
                                ToastUtil.showToast(mContext, R.string.canle_banned_succ);
                            } else {
                                ToastUtil.showToast(mContext, R.string.banned_succ);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    /**
     * 对群内成员备注
     */
    private void setRemarkName(final String userId, final String name) {
        DialogHelper.showLimitSingleInputDialog(this, getString(R.string.change_remark), name, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newName = ((EditText) v).getText().toString().trim();
                if (TextUtils.isEmpty(newName) || newName.equals(name)) {
                    return;
                }

                Map<String, String> params = new HashMap<>();
                params.put("access_token", coreManager.getSelfStatus().accessToken);
                params.put("roomId", mRoomId);
                params.put("userId", userId);
                params.put("remarkName", newName);
                DialogHelper.showDefaulteMessageProgressDialog(GroupMoreFeaturesActivity.this);

                HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_UPDATE)
                        .params(params)
                        .build()
                        .execute(new BaseCallback<Void>(Void.class) {

                            @Override
                            public void onResponse(ObjectResult<Void> result) {
                                DialogHelper.dismissProgressDialog();
                                if (Result.checkSuccess(mContext, result)) {
                                    ToastUtil.showToast(mContext, R.string.modify_succ);
                                    RoomMemberDao.getInstance().updateRoomMemberCardName(mRoomId, userId, newName);

                                    for (int i = 0; i < mSortRoomMember.size(); i++) {
/*
                                                if (mSortRoomMember.get(i).getBean().getUserId().equals(userId)) {
                                                    mSortRoomMember.get(i).getBean().setCardName(newName);
                                                }
*/
                                        if (mSortRoomMember.get(i).getUserId().equals(userId)) {
                                            mSortRoomMember.get(i).setCardName(newName);
                                        }
                                    }
                                    if (!TextUtils.isEmpty(mEditText.getText().toString())) {// 清空mEditText
                                        mEditText.setText("");
                                    } else {
                                        mAdapter.setData(mSortRoomMember);
                                    }
                                    // 更新群组信息页面
                                    EventBus.getDefault().post(new EventGroupStatus(10003, 0));
                                }
                            }

                            @Override
                            public void onError(Call call, Exception e) {
                                DialogHelper.dismissProgressDialog();
                                ToastUtil.showErrorNet(mContext);
                            }
                        });
            }
        });
    }

    class GroupMoreFeaturesAdapter extends BaseAdapter {
        List<RoomMember> mSortRoomMember;

        GroupMoreFeaturesAdapter(List<RoomMember> sortRoomMember) {
            this.mSortRoomMember = new ArrayList<>();
            this.mSortRoomMember = sortRoomMember;
        }

        public void setData(List<RoomMember> sortRoomMember) {
            this.mSortRoomMember = sortRoomMember;
            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            if (mSortRoomMember == null) return 0;
            return mSortRoomMember.size();
        }

        @Override
        public Object getItem(int position) {
            return mSortRoomMember.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_room_all_member, parent, false);
            }
            TextView catagoryTitleTv = ViewHolder.get(convertView, R.id.catagory_title);
            ImageView avatarImg = ViewHolder.get(convertView, R.id.avatar_img);
            TextView roleS = ViewHolder.get(convertView, R.id.roles);
            TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);

/*
            // 根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                catagoryTitleTv.setVisibility(View.VISIBLE);
                catagoryTitleTv.setText(mSortRoomMember.get(position).getFirstLetter());
            } else {
                catagoryTitleTv.setVisibility(View.GONE);
            }
*/
            catagoryTitleTv.setVisibility(View.GONE);

            RoomMember member = mSortRoomMember.get(position);
            if (member != null) {
                AvatarHelper.getInstance().displayAvatar(getName(member), member.getUserId(), avatarImg, true);
                switch (member.getRole()) {
                    case RoomMember.ROLE_OWNER:
                        roleS.setText(getString(R.string.group_owner));
                        ViewCompat.setBackgroundTintList(roleS, ColorStateList.valueOf(getResources().getColor(R.color.color_role1)));
                        break;
                    case RoomMember.ROLE_MANAGER:
                        roleS.setText(getString(R.string.group_manager));
                        ViewCompat.setBackgroundTintList(roleS, ColorStateList.valueOf(getResources().getColor(R.color.color_role2)));
                        break;
                    case RoomMember.ROLE_MEMBER:
                        roleS.setText(getString(R.string.group_role_normal));
                        ViewCompat.setBackgroundTintList(roleS, ColorStateList.valueOf(getResources().getColor(R.color.color_role3)));
                        break;
                    case RoomMember.ROLE_INVISIBLE:
                        roleS.setText(R.string.role_invisible);
                        ViewCompat.setBackgroundTintList(roleS, ColorStateList.valueOf(getResources().getColor(R.color.color_role4)));
                        break;
                    case RoomMember.ROLE_GUARDIAN:
                        roleS.setText(R.string.role_guardian);
                        ViewCompat.setBackgroundTintList(roleS, ColorStateList.valueOf(getResources().getColor(R.color.color_role5)));
                        break;
                    default:
                        Reporter.unreachable();
                        roleS.setVisibility(View.GONE);
                        break;
                }

                userNameTv.setText(getName(member));
            }
            return convertView;
        }

/*
        @Override
        public Object[] getSections() {
            return null;
        }

        @Override
        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = mSortRoomMember.get(i).getFirstLetter();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            return mSortRoomMember.get(position).getFirstLetter().charAt(0);
        }
*/
    }
}
