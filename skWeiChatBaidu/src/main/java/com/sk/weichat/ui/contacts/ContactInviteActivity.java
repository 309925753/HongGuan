package com.sk.weichat.ui.contacts;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.Contact;
import com.sk.weichat.bean.Contacts;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.RoomMember;
import com.sk.weichat.bean.message.MucRoom;
import com.sk.weichat.db.dao.ContactDao;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.RoomMemberDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.sortlist.BaseComparator;
import com.sk.weichat.sortlist.BaseSortModel;
import com.sk.weichat.sortlist.SideBar;
import com.sk.weichat.sortlist.SortHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.Base64;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.ContactsUtil;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.ViewHolder;
import com.sk.weichat.util.secure.RSA;
import com.sk.weichat.util.secure.chat.SecureChatUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import okhttp3.Call;

/**
 * 邀请 手机联系人 加入 x的联系人群组
 */
public class ContactInviteActivity extends BaseActivity {
    private EditText mEditText;
    private boolean isSearch;

    private SideBar mSideBar;
    private TextView mTextDialog;
    private ListView mListView;
    private ContactsAdapter mAdapter;
    private List<Contact> mContactList;
    private List<BaseSortModel<Contact>> mSortContacts;
    private List<BaseSortModel<Contact>> mSearchSortContacts;
    private BaseComparator<Contact> mBaseComparator;

    private String roomId;
    private boolean isSecretGroup;
    private boolean isLoadAll;
    private String mLoginUserId;
    private Friend mFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);

        if (getIntent() != null) {
            roomId = getIntent().getStringExtra("roomId");
            isSecretGroup = getIntent().getBooleanExtra("isSecretGroup", false);
            isLoadAll = getIntent().getBooleanExtra("isLoadAll", false);
        }

        mLoginUserId = coreManager.getSelf().getUserId();
        mFriend = FriendDao.getInstance().getMucFriendByRoomId(mLoginUserId, roomId);

        mContactList = new ArrayList<>();
        mSortContacts = new ArrayList<>();
        mSearchSortContacts = new ArrayList<>();
        mBaseComparator = new BaseComparator<>();
        mAdapter = new ContactsAdapter();

        initActionBar();
        initView();
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
        tvTitle.setText(getString(R.string.select_group_members));
        TextView tvTitleRight = (TextView) findViewById(R.id.tv_title_right);
        tvTitleRight.setText(getString(R.string.sure));
        tvTitleRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> mInviteContactList = new ArrayList<>();
                for (int i = 0; i < mSortContacts.size(); i++) {
                    if (mSortContacts.get(i).getBean().getCheckStatus() == 100) {
                        mInviteContactList.add(mSortContacts.get(i).getBean().getToUserId());
                    }
                }

                if (mInviteContactList.size() <= 0) {
                    DialogHelper.tip(ContactInviteActivity.this, getString(R.string.tip_select_at_lease_one_contacts));
                    return;
                }

                String text = JSON.toJSONString(mInviteContactList);
                inviteContact(roomId, text);
            }
        });
    }

    private void initView() {
        findViewById(R.id.added_layout).setVisibility(View.GONE);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter);

        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mSideBar.setVisibility(View.VISIBLE);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar.setTextView(mTextDialog);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }
            }
        });

        /**
         * 创建群组邀请好友搜索功能
         */
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
                mSearchSortContacts.clear();
                String str = mEditText.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    isSearch = false;
                    mAdapter.setData(mSortContacts);
                    return;
                }
                for (int i = 0; i < mSortContacts.size(); i++) {
                    Contact contact = mSortContacts.get(i).getBean();
                    Friend friend = FriendDao.getInstance().getFriendAndFriendStatus(mLoginUserId, contact.getToUserId());
                    String name;
                    if (friend != null && !TextUtils.isEmpty(friend.getRemarkName())) {
                        name = friend.getRemarkName();
                    } else {
                        name = contact.getToUserName();
                    }
                    if (name.contains(str)) {// 符合搜索条件的联系人
                        mSearchSortContacts.add((mSortContacts.get(i)));
                    }
                }
                mAdapter.setData(mSearchSortContacts);
            }
        });

        mListView.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            Contact contact;
            if (isSearch) {
                contact = mSearchSortContacts.get(position).bean;
            } else {
                contact = mSortContacts.get(position).bean;
            }

            if (isSecretGroup) {
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, contact.getUserId());
                if (friend == null || TextUtils.isEmpty(friend.getPublicKeyRSARoom())) {
                    Toast.makeText(mContext, getString(R.string.friend_are_not_eligible_for_join_secret_group), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            for (int i = 0; i < mSortContacts.size(); i++) {
                if (mSortContacts.get(i).getBean().getToUserId().equals(contact.getToUserId())) {
                    if (contact.getCheckStatus() != 100) {
                        contact.setCheckStatus(100);
                        mSortContacts.get(i).getBean().setCheckStatus(100);
                    } else {
                        contact.setCheckStatus(101);
                        mSortContacts.get(i).getBean().setCheckStatus(101);
                    }

                    if (isSearch) {
                        mAdapter.setData(mSearchSortContacts);
                    } else {
                        mAdapter.setData(mSortContacts);
                    }
                }
            }
        });

        loadData();
    }

    private void loadData() {
        List<Contact> contacts = new ArrayList<>();
        if (isLoadAll) {
            Map<String, Contacts> phoneContacts = ContactsUtil.getPhoneContacts(this);
            List<Contact> allContacts = ContactDao.getInstance().getAllContacts(mLoginUserId);
            // 现在数据库内在创建联系人的时候已经去重了，按理说这里已经不需要处理了，但是一些老用户联系人表内已经生成了一些重复的数据，所以这里在去下重
            Set<Contact> set = new TreeSet<>(new Comparator<Contact>() {
                @Override
                public int compare(Contact o1, Contact o2) {
                    return o1.getToUserId().compareTo(o2.getToUserId());
                }
            });
            set.addAll(allContacts);
            allContacts = new ArrayList<>(set);

            // 服务端会将该账号在所有手机上上传的联系人返回，我们只显示本地通信录的联系人
            for (int i = 0; i < allContacts.size(); i++) {
                if (phoneContacts.containsKey(allContacts.get(i).getToTelephone())) {
                    contacts.add(allContacts.get(i));
                }
            }
        } else {
            String str = getIntent().getStringExtra("contactStr");
            List<String> array = JSON.parseArray(str, String.class);
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    List<Contact> contactList = ContactDao.getInstance().getContactsByToUserId(mLoginUserId, array.get(i));
                    if (contactList != null && contactList.size() > 0) {
                        contacts.add(contactList.get(0));
                    }
                }
            }
        }

        if (mContactList != null) {
            mContactList.clear();
            mContactList.addAll(contacts);
        }

        if (!isLoadAll) {
            loadMembers();
        } else {
            updateContactList();
        }

        if (isSecretGroup) {// 私密群组，因为部分联系人可能不支持端到端，默认不选中
            for (int i = 0; i < mContactList.size(); i++) {
                mContactList.get(i).setCheckStatus(101);
            }
        }
    }

    private void updateContactList() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        AsyncUtils.doAsync(this, e -> {
            Reporter.post("加载数据失败，", e);
            AsyncUtils.runOnUiThread(this, ctx -> {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(ctx, R.string.data_exception);
            });
        }, c -> {
            Map<String, Integer> existMap = new HashMap<>();
            List<BaseSortModel<Contact>> sortedList = SortHelper.toSortedModelList(mContactList, existMap, contact -> {
                Friend friend = FriendDao.getInstance().getFriendAndFriendStatus(mLoginUserId, contact.getToUserId());
                String name;
                if (friend != null && !TextUtils.isEmpty(friend.getRemarkName())) {
                    name = friend.getRemarkName();
                } else {
                    name = contact.getToUserName();
                }
                return name;
            });
            c.uiThread(r -> {
                DialogHelper.dismissProgressDialog();
                mSideBar.setExistMap(existMap);
                mSortContacts = sortedList;
                mAdapter.setData(sortedList);
            });
        });
    }

    private void inviteContact(String roomId, String text) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", roomId);
        params.put("text", text);
        // SecureFlagGroup
        params.put("isSecretGroup", String.valueOf(mFriend.getIsSecretGroup()));
        if (isSecretGroup) {
            List<String> strings = JSON.parseArray(text, String.class);
            Map<String, String> keys = new HashMap<>();
            String keysStr;
            String chatKey = SecureChatUtil.decryptChatKey(mFriend.getUserId(), mFriend.getChatKeyGroup());
            for (int i = 0; i < strings.size(); i++) {
                Friend inviteUser = FriendDao.getInstance().getFriend(mLoginUserId, strings.get(i));
                String chatKeyGroup = RSA.encryptBase64(chatKey.getBytes(),
                        Base64.decode(inviteUser.getPublicKeyRSARoom()));
                keys.put(strings.get(i), chatKeyGroup);
            }
            keysStr = JSON.toJSONString(keys);
            params.put("keys", keysStr);
        }

        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            ToastUtil.showToast(ContactInviteActivity.this, getString(R.string.invite_success));
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorData(ContactInviteActivity.this);
                    }
                });
    }

    private void loadMembers() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", roomId);
        params.put("pageSize", Constants.MUC_MEMBER_PAGE_SIZE);

        HttpUtils.get().url(coreManager.getConfig().ROOM_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {

                             @Override
                             public void onResponse(ObjectResult<MucRoom> result) {
                                 if (result.getResultCode() == 1 && result.getData() != null) {
                                     final MucRoom mucRoom = result.getData();
                                     MyApplication.getInstance().saveGroupPartStatus(mucRoom.getJid(), mucRoom.getShowRead(), mucRoom.getAllowSendCard(),
                                             mucRoom.getAllowConference(), mucRoom.getAllowSpeakCourse(), mucRoom.getTalkTime());
                                     PreferenceUtils.putBoolean(MyApplication.getContext(),
                                             Constants.IS_NEED_OWNER_ALLOW_NORMAL_INVITE_FRIEND + mucRoom.getJid(), mucRoom.getIsNeedVerify() == 1);
                                     PreferenceUtils.putBoolean(MyApplication.getContext(),
                                             Constants.IS_ALLOW_NORMAL_SEND_UPLOAD + mucRoom.getJid(), mucRoom.getAllowUploadFile() == 1);

                                     AsyncUtils.doAsync(this, (AsyncUtils.Function<AsyncUtils.AsyncContext<BaseCallback<MucRoom>>>) baseCallbackAsyncContext -> {
                                         for (int i = 0; i < mucRoom.getMembers().size(); i++) {// 在异步任务内存储
                                             RoomMember roomMember = new RoomMember();
                                             roomMember.setRoomId(mucRoom.getId());
                                             roomMember.setUserId(mucRoom.getMembers().get(i).getUserId());
                                             roomMember.setUserName(mucRoom.getMembers().get(i).getNickName());
                                             if (TextUtils.isEmpty(mucRoom.getMembers().get(i).getRemarkName())) {
                                                 roomMember.setCardName(mucRoom.getMembers().get(i).getNickName());
                                             } else {
                                                 roomMember.setCardName(mucRoom.getMembers().get(i).getRemarkName());
                                             }
                                             roomMember.setRole(mucRoom.getMembers().get(i).getRole());
                                             roomMember.setCreateTime(mucRoom.getMembers().get(i).getCreateTime());
                                             RoomMemberDao.getInstance().saveSingleRoomMember(mucRoom.getId(), roomMember);
                                         }
                                     });

                                     List<Contact> mExistContactList = new ArrayList<>();
                                     if (mContactList != null && mContactList.size() > 0) {
                                         for (int i = 0; i < mContactList.size(); i++) {
                                             for (int i1 = 0; i1 < mucRoom.getMembers().size(); i1++) {
                                                 if (mucRoom.getMembers().get(i1).getUserId().equals(mContactList.get(i).getToUserId())) {// 移除已在群组的联系人
                                                     mExistContactList.add(mContactList.get(i));
                                                 }
                                             }
                                         }
                                     }
                                     mContactList.removeAll(mExistContactList);

                                     updateContactList();
                                 }
                             }

                             @Override
                             public void onError(Call call, Exception e) {
                                 ToastUtil.showNetError(ContactInviteActivity.this);
                             }
                         }
                );
    }

    class ContactsAdapter extends BaseAdapter implements SectionIndexer {
        List<BaseSortModel<Contact>> mSortContactList;

        public ContactsAdapter() {
            mSortContactList = new ArrayList<>();
        }

        public void setData(List<BaseSortModel<Contact>> sortContactList) {
            mSortContactList = sortContactList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mSortContactList.size();
        }

        @Override
        public Object getItem(int position) {
            return mSortContactList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_contacts, parent, false);
            }
            TextView categoryTitleTv = ViewHolder.get(convertView, R.id.catagory_title);
            CheckBox checkBox = ViewHolder.get(convertView, R.id.check_box);
            ImageView avatarImg = ViewHolder.get(convertView, R.id.avatar_img);
            TextView contactNameTv = ViewHolder.get(convertView, R.id.contact_name_tv);
            TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);
            FrameLayout fl = ViewHolder.get(convertView, R.id.friend_fl);

            // 根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                categoryTitleTv.setVisibility(View.VISIBLE);
                categoryTitleTv.setText(mSortContactList.get(position).getFirstLetter());
            } else {
                categoryTitleTv.setVisibility(View.GONE);
            }
            checkBox.setVisibility(View.VISIBLE);
            contactNameTv.setVisibility(View.GONE);
            fl.setVisibility(View.GONE);
            final Contact contact = mSortContactList.get(position).getBean();
            if (contact != null) {
                if (contact.getCheckStatus() == 100) {
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
                }
                Friend friend = FriendDao.getInstance().getFriendAndFriendStatus(mLoginUserId, contact.getToUserId());
                if (friend != null) {
                    userNameTv.setText(TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName());
                } else {
                    userNameTv.setText(contact.getToUserName());
                }
                AvatarHelper.getInstance().displayAvatar(userNameTv.getText().toString(), contact.getToUserId(), avatarImg, true);
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
                String sortStr = mSortContactList.get(i).getFirstLetter();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            return mSortContactList.get(position).getFirstLetter().charAt(0);
        }
    }
}
