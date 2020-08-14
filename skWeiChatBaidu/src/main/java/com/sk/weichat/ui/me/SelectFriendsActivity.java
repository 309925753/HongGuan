package com.sk.weichat.ui.me;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.SelectFriendItem;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.sortlist.BaseComparator;
import com.sk.weichat.sortlist.BaseSortModel;
import com.sk.weichat.sortlist.SideBar;
import com.sk.weichat.sortlist.SortHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.me.select.SelectAdapter;
import com.sk.weichat.ui.me.select.SelectColleagueAdapter;
import com.sk.weichat.ui.me.select.SelectLabelAdapter;
import com.sk.weichat.ui.me.select.SelectRoomAdapter;
import com.sk.weichat.ui.me.sendgroupmessage.ChatActivityForSendGroup;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.ViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2017/12/5 0005.
 * 只是对临时的数据(status)进行更改，来标记是否选中，并不需要去更新数据库 status 100 未选中 101选中
 */

public class SelectFriendsActivity extends BaseActivity {
    public static String tempData;
    private ViewGroup llSelectExtension;
    private EditText mSearchEdit;
    private boolean isSearch;

    private SideBar mSideBar;
    private TextView mTextDialog;
    private ListView mListView;
    private SelectFriendAdapter mSelectAdapter;
    private List<Friend> mFriendList;
    private List<BaseSortModel<Friend>> mSortFriends;
    private List<BaseSortModel<Friend>> mSearchSortFriends;
    private BaseComparator<Friend> mBaseComparator;

    private TextView mNextTv;

    private TextView tvRight;
    private boolean isAllOrCancel;
    private My_BroadcastReceiver mMyBroadcastReceiver = new My_BroadcastReceiver();
    private List<SelectAdapter> adapterList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friends);
        initActionBar();
        initData();
        initView();
        initEvent();

        registerReceiver(mMyBroadcastReceiver, new IntentFilter(com.sk.weichat.broadcast.OtherBroadcast.SEND_MULTI_NOTIFY));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMyBroadcastReceiver != null) {
            unregisterReceiver(mMyBroadcastReceiver);
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
        tvTitle.setText(getString(R.string.select_recipient));
        tvRight = (TextView) findViewById(R.id.tv_title_right);
        tvRight.setText(getString(R.string.select_all));
    }

    private void initData() {
        adapterList.add(new SelectRoomAdapter());
        adapterList.add(new SelectColleagueAdapter());
        adapterList.add(new SelectLabelAdapter());
        mFriendList = new ArrayList<>();
        mSortFriends = new ArrayList<>();
        mSearchSortFriends = new ArrayList<>();
        mBaseComparator = new BaseComparator<>();
    }

    private void initView() {
        llSelectExtension = findViewById(R.id.llSelectExtension);
        for (SelectAdapter adapter : adapterList) {
            View block = LayoutInflater.from(this).inflate(R.layout.block_select_extension, llSelectExtension, false);
            TextView tvLabel = block.findViewById(R.id.tvLabel);
            TextView tvValue = block.findViewById(R.id.tvValue);
            block.setOnClickListener(v -> {
                adapter.startSelect(this);
            });
            tvLabel.setText(adapter.getLabel());
            adapter.bindValueWidget(tvValue);

            llSelectExtension.addView(block);
        }
        mSearchEdit = (EditText) findViewById(R.id.search_et);

        mListView = (ListView) findViewById(R.id.select_lv);
        mSelectAdapter = new SelectFriendAdapter(this);
        mListView.setAdapter(mSelectAdapter);

        mNextTv = (TextView) findViewById(R.id.next_tv);

        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mSideBar.setVisibility(View.VISIBLE);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar.setTextView(mTextDialog);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mSelectAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }
            }
        });

        loadData();
    }

    private void loadData() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        AsyncUtils.doAsync(this, e -> {
            Reporter.post("加载数据失败，", e);
            AsyncUtils.runOnUiThread(this, ctx -> {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(ctx, R.string.data_exception);
            });
        }, c -> {
            final List<Friend> friends = FriendDao.getInstance().getAllFriends(coreManager.getSelf().getUserId());
            for (Friend friend : friends) {
                // 只是对临时的数据(status)进行更改，来标记是否选中，并不需要去更新数据库 status 100 未选中 101选中
                // TODO: 不如改成存个稀疏数组表示选中，最好是另外封装个实体，包括friend和选中状态，
                friend.setStatus(100);
            }
            Map<String, Integer> existMap = new HashMap<>();
            List<BaseSortModel<Friend>> sortedList = SortHelper.toSortedModelList(friends, existMap, Friend::getShowName);
            c.uiThread(r -> {
                DialogHelper.dismissProgressDialog();
                mSideBar.setExistMap(existMap);
                mFriendList = friends;
                mSortFriends = sortedList;
                mSelectAdapter.setData(sortedList);
            });
        });
    }

    private void initEvent() {
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFriendList == null || mFriendList.size() <= 0
                        || mSortFriends == null || mSortFriends.size() <= 0)
                    return;

                isAllOrCancel = !isAllOrCancel;
                if (isAllOrCancel) {
                    for (int i = 0; i < mFriendList.size(); i++) {
                        mFriendList.get(i).setStatus(101);
                        mSortFriends.get(i).getBean().setStatus(101);
                    }
                    tvRight.setText(getString(R.string.cancel));
                    mNextTv.setText(getString(R.string.next_step) + "(" + mFriendList.size() + ")");
                } else {
                    for (int i = 0; i < mFriendList.size(); i++) {
                        mFriendList.get(i).setStatus(100);
                        mSortFriends.get(i).getBean().setStatus(100);
                    }
                    tvRight.setText(getString(R.string.select_all));
                    mNextTv.setText(getString(R.string.next_step));
                }
                mSelectAdapter.setData(mSortFriends);
            }
        });

        mSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = mSearchEdit.getText().toString();
                mSearchSortFriends.clear();
                if (TextUtils.isEmpty(str)) {
                    isSearch = false;
                    mSelectAdapter.setData(mSortFriends);
                    tvRight.setVisibility(View.VISIBLE);
                } else {
                    isSearch = true;
                    for (int i = 0; i < mSortFriends.size(); i++) {
                        Friend friend = mSortFriends.get(i).getBean();
                        String matchX = !TextUtils.isEmpty(friend.getRemarkName()) ? friend.getRemarkName() : friend.getNickName();
                        if (matchX.contains(str)) {
                            mSearchSortFriends.add(mSortFriends.get(i));
                        }
                    }
                    mSelectAdapter.setData(mSearchSortFriends);
                    tvRight.setVisibility(View.GONE);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Friend friend;
                if (!isSearch) {
                    friend = mSortFriends.get(position).getBean();
                } else {
                    friend = mSearchSortFriends.get(position).getBean();
                }
                if (friend.getStatus() == 101) {
                    friend.setStatus(100);
                } else {
                    friend.setStatus(101);
                }
                if (!isSearch) {
                    mSortFriends.get(position).getBean().setStatus(friend.getStatus());
                    mSelectAdapter.setData(mSortFriends);
                } else {
                    mSearchSortFriends.get(position).getBean().setStatus(friend.getStatus());
                    mSelectAdapter.setData(mSearchSortFriends);
                }
                // 同时需要更新总数据
                for (int i = 0; i < mFriendList.size(); i++) {
                    if (mFriendList.get(i).getUserId().equals(friend.getUserId())) {
                        mFriendList.get(i).setStatus(friend.getStatus());
                    }
                }

                int count = 0;
                // 计算被选中人数
                for (int i = 0; i < mFriendList.size(); i++) {
                    if (mFriendList.get(i).getStatus() == 101) {
                        count = count + 1;
                    }
                }

                if (count == 0) {
                    mNextTv.setText(getString(R.string.next_step));
                } else {
                    mNextTv.setText(getString(R.string.next_step) + "(" + count + ")");
                }
            }
        });

        mNextTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<SelectFriendItem> items = new HashSet<>();
                for (Friend friend : mFriendList) {
                    if (friend.getStatus() == 101) {
                        items.add(new SelectFriendItem(friend.getUserId(), friend.getShowName(), friend.getRoomFlag()));
                    }
                }
                for (SelectAdapter adapter : adapterList) {
                    List<Friend> friendList = adapter.query(SelectFriendsActivity.this);
                    for (Friend friend : friendList) {
                        items.add(new SelectFriendItem(friend.getUserId(), friend.getShowName(), friend.getRoomFlag()));
                    }
                }

                if (items.size() > 0) {
                    ChatActivityForSendGroup.start(mContext, items);
                } else {
                    ToastUtil.showToast(mContext, getString(R.string.alert_select_one));
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            for (SelectAdapter adapter : adapterList) {
                if (adapter.consume(this, requestCode, data)) {
                    return;
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class My_BroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(com.sk.weichat.broadcast.OtherBroadcast.SEND_MULTI_NOTIFY)) {
                finish();
            }
        }
    }

    class SelectFriendAdapter extends BaseAdapter implements SectionIndexer {

        List<BaseSortModel<Friend>> mSortFriends;
        private Context mContext;

        public SelectFriendAdapter(Context context) {
            this.mContext = context;
            this.mSortFriends = new ArrayList<>();
        }

        public void setData(List<BaseSortModel<Friend>> sortFriends) {
            this.mSortFriends = sortFriends;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mSortFriends.size();
        }

        @Override
        public Object getItem(int position) {
            return mSortFriends.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_select_friend, parent, false);
            }

            CheckBox cb = ViewHolder.get(convertView, R.id.select_cb);
            ImageView iv = ViewHolder.get(convertView, R.id.select_iv);
            TextView tv = ViewHolder.get(convertView, R.id.select_tv);
            View view = ViewHolder.get(convertView, R.id.view);

            Friend friend = mSortFriends.get(position).getBean();
            if (friend != null) {
                AvatarHelper.getInstance().displayAvatar(TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName(),
                        friend.getUserId(), iv, true);
                tv.setText(!TextUtils.isEmpty(friend.getRemarkName()) ? friend.getRemarkName() : friend.getNickName());
                cb.setChecked(friend.getStatus() == 101);
            }
            if (position == mSortFriends.size() - 1) {
                view.setVisibility(View.GONE);
            } else {
                view.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        @Override
        public Object[] getSections() {
            return null;
        }

        @Override
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

        @Override
        public int getSectionForPosition(int position) {
            return mSortFriends.get(position).getFirstLetter().charAt(0);
        }
    }
}
