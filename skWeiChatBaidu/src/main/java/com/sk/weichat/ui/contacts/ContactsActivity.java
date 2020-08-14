package com.sk.weichat.ui.contacts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.Area;
import com.sk.weichat.bean.Contact;
import com.sk.weichat.bean.Contacts;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.message.MucRoom;
import com.sk.weichat.broadcast.MucgroupUpdateUtil;
import com.sk.weichat.db.dao.ContactDao;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.NewFriendDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.sortlist.BaseComparator;
import com.sk.weichat.sortlist.BaseSortModel;
import com.sk.weichat.sortlist.SideBar;
import com.sk.weichat.sortlist.SortHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.dialog.TowInputDialogView;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.AnimationUtil;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.Base64;
import com.sk.weichat.util.CharUtils;
import com.sk.weichat.util.CommonAdapter;
import com.sk.weichat.util.CommonViewHolder;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.ContactsUtil;
import com.sk.weichat.util.PermissionUtil;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.ViewHolder;
import com.sk.weichat.util.secure.RSA;
import com.sk.weichat.util.secure.chat.SecureChatUtil;
import com.sk.weichat.view.PullToRefreshSlideListView;
import com.sk.weichat.view.SelectionFrame;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import okhttp3.Call;

public class ContactsActivity extends BaseActivity {
    private SideBar mSideBar;
    private TextView mTextDialog;
    private PullToRefreshSlideListView mListView;
    private ContactsAdapter mContactsAdapter;
    private List<Contact> mContactList;
    private List<BaseSortModel<Contact>> mSortContactList;
    private BaseComparator<Contact> mBaseComparator;
    private String mLoginUserId;
    private View mHeadView;
    private ListView mNewContactListView;
    private NewContactAdapter mNewContactAdapter;
    private List<Contact> mNewContactList = new ArrayList<>();
    private List<Friend> mBlackList = new ArrayList<>();
    // 批量添加 联系人
    private TextView tvTitleRight;
    private boolean isBatch;
    private Map<String, Contact> mBatchAddContacts = new HashMap<>();
    private TextView mBatchAddTv;
    private boolean isSelectAll;

    private String mRoomId;
    private boolean isSecretGroup = false;
    private String chatKey;

    private Map<String, Contacts> phoneContacts;
    private TowInputDialogView towInputDialogView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        mLoginUserId = coreManager.getSelf().getUserId();
        mContactList = new ArrayList<>();
        mSortContactList = new ArrayList<>();
        mBaseComparator = new BaseComparator<>();
        mContactsAdapter = new ContactsAdapter();

        initActionBar();
        boolean isReadContacts = PermissionUtil.checkSelfPermissions(this, new String[]{Manifest.permission.READ_CONTACTS});
        if (!isReadContacts) {
            DialogHelper.tip(this, getString(R.string.please_open_address_per));
            return;
        }

        initView();
        dataLayering();
        initNewContacts();
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
        tvTitle.setText(getString(R.string.phone_contact));

        tvTitleRight = (TextView) findViewById(R.id.tv_title_right);
        tvTitleRight.setText(getString(R.string.select_all));
    }

/*
    private void loadData() {
        mContactList.clear();
        mSortContactList.clear();

        List<Contact> allContacts = ContactDao.getInstance().getAllContacts(mLoginUserId);
        // 服务端会将该账号在所有手机上上传的联系人返回，我们只显示本地通信录的联系人
        for (int i = 0; i < allContacts.size(); i++) {
            if (phoneContacts.containsKey(allContacts.get(i).getToTelephone())) {
                mContactList.add(allContacts.get(i));
            }
        }

        // HEAD - NEW CONTACT START
        String mContactsIds = PreferenceUtils.getString(this, Constants.NEW_CONTACTS_IDS);
        if (!TextUtils.isEmpty(mContactsIds)) {
            List<String> array = JSON.parseArray(mContactsIds, String.class);
            for (int i = 0; i < array.size(); i++) {
                List<Contact> contactList = ContactDao.getInstance().getContactsByToUserId(mLoginUserId, array.get(i));
                if (contactList != null && contactList.size() > 0) {
                    mNewContactList.add(contactList.get(0));
                }
            }
        }
        List<Contact> removeContactList = new ArrayList<>();
        if (mNewContactList.size() > 0) {
            for (int i = 0; i < mContactList.size(); i++) {
                String toUserId = mContactList.get(i).getToUserId();
                for (int i1 = 0; i1 < mNewContactList.size(); i1++) {
                    if (mNewContactList.get(i1).getToUserId().equals(toUserId)) {
                        removeContactList.add(mContactList.get(i));
                    }
                }
            }
            mContactList.removeAll(removeContactList);
        }
        // HEAD - NEW CONTACT END
        for (int i = 0; i < mContactList.size(); i++) {
            BaseSortModel<Contact> mode = new BaseSortModel<>();
            mode.setBean(mContactList.get(i));
            setSortCondition(mode);
            mSortContactList.add(mode);
        }
        Collections.sort(mSortContactList, mBaseComparator);

        mContactsAdapter.setData(mSortContactList);
        mListView.onRefreshComplete();
    }
*/

    public void initView() {
        mListView = (PullToRefreshSlideListView) findViewById(R.id.pull_refresh_list);
        mListView.getRefreshableView().setAdapter(mContactsAdapter);
        mListView.setMode(PullToRefreshBase.Mode.DISABLED);

        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar.setTextView(mTextDialog);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mContactsAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.getRefreshableView().setSelection(position);
                }
            }
        });

        mBatchAddTv = (TextView) findViewById(R.id.sure_add_tv);
    }

    private void dataLayering() {
        mBlackList = FriendDao.getInstance().getAllBlacklists(mLoginUserId);

        phoneContacts = ContactsUtil.getPhoneContacts(this);

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
                mContactList.add(allContacts.get(i));
            }
        }

        // 加载 头部 data 即新增的联系人
        String mContactsIds = PreferenceUtils.getString(this, Constants.NEW_CONTACTS_IDS + mLoginUserId);
        if (!TextUtils.isEmpty(mContactsIds)) {
            List<String> array = JSON.parseArray(mContactsIds, String.class);
            for (int i = 0; i < array.size(); i++) {
                List<Contact> contactList = ContactDao.getInstance().getContactsByToUserId(mLoginUserId, array.get(i));
                if (contactList != null && contactList.size() > 0) {
                    mNewContactList.add(contactList.get(0));
                }
            }
        }
        PreferenceUtils.putString(ContactsActivity.this, Constants.NEW_CONTACTS_IDS + mLoginUserId, "");// 清空ids

        // 总 data 内 移除 头部 data
        List<Contact> removeContactList = new ArrayList<>();
        if (mNewContactList.size() > 0) {
            for (int i = 0; i < mContactList.size(); i++) {
                String toUserId = mContactList.get(i).getToUserId();
                for (int i1 = 0; i1 < mNewContactList.size(); i1++) {
                    if (mNewContactList.get(i1).getToUserId().equals(toUserId)) {
                        removeContactList.add(mContactList.get(i));
                    }
                }
            }
            mContactList.removeAll(removeContactList);
        }

        DialogHelper.showDefaulteMessageProgressDialog(this);
        try {
            AsyncUtils.doAsync(this, e -> {
                Reporter.post("加载数据失败，", e);
                AsyncUtils.runOnUiThread(this, ctx -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(ctx, R.string.data_exception);
                });
            }, c -> {
                Map<String, Integer> existMap = new HashMap<>();
                List<BaseSortModel<Contact>> sortedList = SortHelper.toSortedModelList(mContactList, existMap, contact -> {
                    String name;
                    Contacts contacts = phoneContacts.get(contact.getToTelephone());
                    if (contacts != null) {// 取出本地通讯录的名字
                        name = contacts.getName();
                    } else {
                        name = contact.getToUserName();
                    }
                    return name;
                });
                c.uiThread(r -> {
                    DialogHelper.dismissProgressDialog();
                    mSideBar.setExistMap(existMap);
                    mSortContactList = sortedList;
                    mContactsAdapter.setData(sortedList);
                });
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initNewContacts() {
        if (mNewContactList == null || mNewContactList.size() <= 0) {// 无新联系人
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        mHeadView = inflater.inflate(R.layout.head_contact, null);
        mNewContactListView = (ListView) mHeadView.findViewById(R.id.head_lv);
        mNewContactAdapter = new NewContactAdapter(this, mNewContactList);
        mNewContactListView.setAdapter(mNewContactAdapter);
        mListView.getRefreshableView().addHeaderView(mHeadView, null, false);

        boolean isExistContactGroup = false;
        List<Friend> mFriendList = FriendDao.getInstance().getAllRooms(mLoginUserId);
        for (int i = 0; i < mFriendList.size(); i++) {
            Friend friend = mFriendList.get(i);
            if (friend != null && friend.getRoomFlag() == 510) {
                isExistContactGroup = true;
                mRoomId = mFriendList.get(i).getRoomId();
                isSecretGroup = mFriendList.get(i).getIsSecretGroup() == 1;
            }
        }

        if (isExistContactGroup) {// 存在我的联系人群组 , 弹出界面提示是否邀请进群
            SelectionFrame mSelectionFrame = new SelectionFrame(this);
            mSelectionFrame.setSomething(null, getString(R.string.is_invite), new SelectionFrame.OnSelectionFrameClickListener() {
                @Override
                public void cancelClick() {

                }

                @Override
                public void confirmClick() {
                    go(mRoomId, isSecretGroup, false);
                }
            });
            mSelectionFrame.show();
        } else if (!coreManager.getLimit().cannotCreateGroup()) {// 不存在 ' 我的手机联系人' 群组 提示建群
            SelectionFrame mSelectionFrame = new SelectionFrame(this);
            mSelectionFrame.setSomething(null, getString(R.string.is_create), new SelectionFrame.OnSelectionFrameClickListener() {
                @Override
                public void cancelClick() {

                }

                @Override
                public void confirmClick() {
                    showCreateGroupChatDialog();
                }
            });
            mSelectionFrame.show();
        }
    }

    private void initEvent() {
        tvTitleRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isControlBatchStatus(true);
            }
        });

/*
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<SlideListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<SlideListView> refreshView) {
                refreshContactsFromService();
            }
        });
*/

        findViewById(R.id.invited_friend_ll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ContactsMsgInviteActivity.class));
            }
        });

        mListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position = (int) id;
                Contact contact = mSortContactList.get(position).getBean();
                if (contact != null) {
                    if (isBatch) {
                        Friend friend = FriendDao.getInstance().getFriendAndFriendStatus(mLoginUserId, contact.getToUserId());
                        if (friend != null) {
                            return;
                        }
                        if (mBatchAddContacts.containsKey(contact.getToUserId())) {
                            mBatchAddContacts.remove(contact.getToUserId());
                        } else {
                            mBatchAddContacts.put(contact.getToUserId(), contact);
                        }
                    }
                    mContactsAdapter.notifyDataSetChanged();
                }
            }
        });

        mBatchAddTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collection<Contact> values = mBatchAddContacts.values();
                List<Contact> contactList = new ArrayList<>(values);
                addFriendInContacts(contactList, true);
            }
        });
    }

    private void isControlBatchStatus(boolean isOperating) {
        if (isOperating) {
            isBatch = !isBatch;
            if (isBatch) {
                isSelectAll = true;
                if (mNewContactList.size() > 0) {
                    Toast.makeText(mContext, getString(R.string.tip_new_contact_not_support_add), Toast.LENGTH_SHORT).show();
                }
                tvTitleRight.setText(getString(R.string.cancel));
                // mBatchAddTv.setVisibility(View.VISIBLE);
                AnimationUtil.setVisible(mBatchAddTv);
            } else {
                tvTitleRight.setText(getString(R.string.select_all));
                mBatchAddContacts.clear();
                // mBatchAddTv.setVisibility(View.GONE);
                AnimationUtil.setGone(mBatchAddTv);
            }
        }

        if (mNewContactAdapter != null) {
            mNewContactAdapter.notifyDataSetChanged();
        }
        mContactsAdapter.notifyDataSetChanged();
    }

/*
    private void refreshContactsFromService() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.get().url(coreManager.getConfig().ADDRESSBOOK_GETALL)
                .params(params)
                .build()
                .execute(new ListCallback<Contact>(Contact.class) {

                    @Override
                    public void onResponse(ArrayResult<Contact> result) {
                        if (result.getResultCode() == 1) {
                            ContactDao.getInstance().refreshContact(mLoginUserId, result.getData());

                            loadData();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }
*/

    // 在联系人内加好友，需要
    private void addFriendInContacts(final List<Contact> contacts, final boolean isOperating) {
        if (contacts.size() <= 0) {
            DialogHelper.tip(this, getString(R.string.tip_select_at_lease_one_contacts));
            return;
        }

        String toUserIds = "";
        for (int i = 0; i < contacts.size(); i++) {
            if (i == contacts.size() - 1) {
                toUserIds += contacts.get(i).getToUserId();
            } else {
                toUserIds += contacts.get(i).getToUserId() + ",";
            }
        }

        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserIds", toUserIds);

        HttpUtils.get().url(coreManager.getConfig().ADDENTION_BATCH_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(ContactsActivity.this, result)) {
                            for (int i = 0; i < contacts.size(); i++) {
                                NewFriendDao.getInstance().addFriendOperating(contacts.get(i).getToUserId(), contacts.get(i).getToUserName(), "");
                            }
                        }
                        isControlBatchStatus(isOperating);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorData(ContactsActivity.this);
                        isControlBatchStatus(isOperating);
                    }
                });
    }

    /*
    创建 手机联系人 群组
     */
    private void showCreateGroupChatDialog() {
        towInputDialogView = DialogHelper.showTowInputDialogAndReturnDialog(this,
                getString(R.string.create_rooms),
                getString(R.string.jx_inputroomname),
                getString(R.string.jxalert_inputsomething),
                getString(R.string.x_phone_address_group, coreManager.getSelf().getNickName()), getString(R.string.x_phone_address_group, coreManager.getSelf().getNickName()),
                (roomNameEdit, roomDescEdit, isRead, isLook, isNeedVerify, isShowMember, isAllowSendCard, isSecretGroup) -> {
                    String roomName = roomNameEdit.getText().toString().trim();
                    if (TextUtils.isEmpty(roomName)) {
                        Toast.makeText(ContactsActivity.this, getString(R.string.room_name_empty_error), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String roomDesc = roomDescEdit.getText().toString();
                    if (TextUtils.isEmpty(roomDesc)) {
                        Toast.makeText(ContactsActivity.this, getString(R.string.room_des_empty_error), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int length = 0;
                    for (int i = 0; i < roomName.length(); i++) {
                        String substring = roomName.substring(i, i + 1);
                        if (CharUtils.isChinese(substring)) {  // 中文占两个字符
                            length += 2;
                        } else {
                            length += 1;
                        }
                    }
                    if (length > 20) {
                        Toast.makeText(ContactsActivity.this, getString(R.string.tip_group_name_too_long), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int length2 = 0;
                    for (int i = 0; i < roomDesc.length(); i++) {
                        String substring = roomDesc.substring(i, i + 1);
                        if (CharUtils.isChinese(substring)) {
                            length2 += 2;
                        } else {
                            length2 += 1;
                        }
                    }
                    if (length2 > 100) {
                        Toast.makeText(ContactsActivity.this, getString(R.string.tip_group_description_too_long), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isSecretGroup == 1) {
                        if (TextUtils.isEmpty(SecureChatUtil.getDHPrivateKey(coreManager.getSelf().getUserId()))) {
                            ToastUtil.showToast(mContext, getString(R.string.friend_are_not_eligible_for_create_secret_group));
                            return;
                        }
                    }

                    if (towInputDialogView != null) {
                        towInputDialogView.dismiss();
                    }

                    createGroupChat(roomName, roomDesc, isRead, isLook, isNeedVerify, isShowMember, isAllowSendCard, isSecretGroup);
                });
    }

    private void createGroupChat(final String roomName, final String roomDesc, int isRead, int isLook,
                                 int isNeedVerify, int isShowMember, int isAllowSendCard, int isSecretGroup) {
        final String roomJid = coreManager.createMucRoom(roomName);
        if (TextUtils.isEmpty(roomJid)) {
            ToastUtil.showToast(mContext, getString(R.string.create_room_failed));
            return;
        }
        MyApplication.mRoomKeyLastCreate = roomJid;
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("jid", roomJid);
        params.put("name", roomName);
        params.put("desc", roomDesc);
        params.put("countryId", String.valueOf(Area.getDefaultCountyId()));

        params.put("showRead", isRead + "");
        // 显示已读人数
        PreferenceUtils.putBoolean(mContext, Constants.IS_SHOW_READ + roomJid, isRead == 1);
        // 是否公开
        params.put("isLook", isLook + "");
        // 是否开启进群验证
        params.put("isNeedVerify", isNeedVerify + "");
        // 其他群管理
        params.put("showMember", isShowMember + "");
        params.put("allowSendCard", isAllowSendCard + "");

        params.put("allowInviteFriend", "1");
        params.put("allowUploadFile", "1");
        params.put("allowConference", "1");
        params.put("allowSpeakCourse", "1");

        params.put("category", "510");// 群组类型 手机联系人群组

        PreferenceUtils.putBoolean(mContext, Constants.IS_SEND_CARD + roomJid, isAllowSendCard == 1);

        Area area = Area.getDefaultProvince();
        if (area != null) {
            params.put("provinceId", String.valueOf(area.getId()));
        }
        area = Area.getDefaultCity();
        if (area != null) {
            params.put("cityId", String.valueOf(area.getId()));
            area = Area.getDefaultDistrict(area.getId());
            if (area != null) {
                params.put("areaId", String.valueOf(area.getId()));
            }
        }

        double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));
        // SecureFlagGroup
        params.put("isSecretGroup", String.valueOf(isSecretGroup));
        if (isSecretGroup == 1) {
            chatKey = UUID.randomUUID().toString().replaceAll("-", "");
            String chatKeyGroup = RSA.encryptBase64(chatKey.getBytes(),
                    Base64.decode(SecureChatUtil.getRSAPublicKey(coreManager.getSelf().getUserId())));
            Map<String, String> keys = new HashMap<>();
            keys.put(coreManager.getSelf().getUserId(), chatKeyGroup);
            String keysStr = JSON.toJSONString(keys);
            params.put("keys", keysStr);
        }

        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {

                    @Override
                    public void onResponse(ObjectResult<MucRoom> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            createRoomSuccess(result.getData());
                        } else {
                            MyApplication.mRoomKeyLastCreate = "compatible";// 还原回去
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        MyApplication.mRoomKeyLastCreate = "compatible";// 还原回去
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    // 创建成功的时候将会调用此方法，将房间也存为好友
    private void createRoomSuccess(MucRoom mucRoom) {
        Friend friend = new Friend();
        friend.setOwnerId(mLoginUserId);
        friend.setUserId(mucRoom.getJid());
        friend.setNickName(mucRoom.getName());
        friend.setDescription(mucRoom.getDesc());
        friend.setRoomId(mucRoom.getId());
        friend.setRoomCreateUserId(mLoginUserId);
        friend.setRoomFlag(510);
        friend.setStatus(Friend.STATUS_FRIEND);
        // timeSend作为取群聊离线消息的标志，所以要在这里设置一个初始值
        friend.setTimeSend(TimeUtils.sk_time_current_time());
        // SecureFlagGroup
        friend.setIsSecretGroup(mucRoom.getIsSecretGroup());
        if (friend.getIsSecretGroup() == 1) {
            friend.setChatKeyGroup(SecureChatUtil.encryptChatKey(mucRoom.getJid(), chatKey));
        }
        FriendDao.getInstance().createOrUpdateFriend(friend);
        // 更新群聊界面
        MucgroupUpdateUtil.broadcastUpdateUi(this);

        go(mucRoom.getId(), mucRoom.getIsSecretGroup() == 1, true);
    }

    private void go(String roomId, boolean isSecretGroup, boolean isLoadAll) {
        String str = "";
        if (!isLoadAll) {// 只显示新联系人 需要将新联系人传过去
            List<String> toUserIdList = new ArrayList<>();
            for (int i = 0; i < mNewContactList.size(); i++) {
                toUserIdList.add(mNewContactList.get(i).getToUserId());
            }
            str = JSON.toJSONString(toUserIdList);
        }

        Intent intent = new Intent(ContactsActivity.this, ContactInviteActivity.class);
        intent.putExtra("roomId", roomId);
        intent.putExtra("isSecretGroup", isSecretGroup);
        intent.putExtra("isLoadAll", isLoadAll);
        intent.putExtra("contactStr", str);
        startActivity(intent);
    }

    class NewContactAdapter extends CommonAdapter<Contact> {
        private Context mContext;

        public NewContactAdapter(Context context, List<Contact> data) {
            super(context, data);
            this.mContext = context;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.row_contacts, position);

            TextView categoryTitleTv = viewHolder.getView(R.id.catagory_title);
            ImageView avatarImg = viewHolder.getView(R.id.avatar_img);
            TextView contactNameTv = viewHolder.getView(R.id.contact_name_tv);
            TextView userNameTv = viewHolder.getView(R.id.user_name_tv);
            Button isFriendBtn = viewHolder.getView(R.id.is_friend_btn);
            Button isNotFriendBtn = viewHolder.getView(R.id.is_not_friend_btn);
            ButtonColorChange.colorChange(mContext, isNotFriendBtn);
            categoryTitleTv.setVisibility(View.GONE);
            final Contact contact = data.get(position);
            if (contact != null) {
                Friend friend = FriendDao.getInstance().getFriendAndFriendStatus(mLoginUserId, contact.getToUserId());
                Contacts contacts = phoneContacts.get(contact.getToTelephone());
                if (contacts != null) {
                    AvatarHelper.getInstance().displayAvatar(contacts.getName(), contact.getToUserId(), avatarImg, true);
                    contactNameTv.setText(contacts.getName());
                } else {
                    AvatarHelper.getInstance().displayAvatar(contact.getToUserId(), avatarImg, true);
                    contactNameTv.setText(contact.getToUserName());
                }

                if (friend != null) {
                    String inAppName = TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName();
                    userNameTv.setText(getString(R.string.app_name) + ": " + inAppName);
                    isFriendBtn.setText(getString(R.string.added));
                    isFriendBtn.setVisibility(View.VISIBLE);
                    isNotFriendBtn.setVisibility(View.GONE);
                } else {
                    userNameTv.setText(getString(R.string.app_name) + ": " + contact.getToUserName());
                    isFriendBtn.setVisibility(View.GONE);
                    isNotFriendBtn.setVisibility(View.VISIBLE);
                    for (int i = 0; i < mBlackList.size(); i++) {
                        if (mBlackList.get(i).getUserId().equals(contact.getToUserId())) {// 该联系人存在于我的黑名单列表
                            isFriendBtn.setText(R.string.added_black);
                            isFriendBtn.setVisibility(View.VISIBLE);
                            isNotFriendBtn.setVisibility(View.GONE);
                        }
                    }
                }

                isNotFriendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<Contact> contactList = new ArrayList<>();
                        contactList.add(contact);
                        addFriendInContacts(contactList, false);
                    }
                });
            }

            return viewHolder.getConvertView();
        }
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
            Button isFriendBtn = ViewHolder.get(convertView, R.id.is_friend_btn);
            Button isNotFriendBtn = ViewHolder.get(convertView, R.id.is_not_friend_btn);
            ButtonColorChange.colorChange(mContext, isNotFriendBtn);
            // 根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                categoryTitleTv.setVisibility(View.VISIBLE);
                categoryTitleTv.setText(mSortContactList.get(position).getFirstLetter());
            } else {
                categoryTitleTv.setVisibility(View.GONE);
            }

            final Contact contact = mSortContactList.get(position).getBean();
            if (contact != null) {
                Friend friend = FriendDao.getInstance().getFriendAndFriendStatus(mLoginUserId, contact.getToUserId());
                Contacts contacts = phoneContacts.get(contact.getToTelephone());
                if (contacts != null) {
                    AvatarHelper.getInstance().displayAvatar(contacts.getName(), contact.getToUserId(), avatarImg, true);
                    contactNameTv.setText(contacts.getName());
                } else {
                    AvatarHelper.getInstance().displayAvatar(contact.getToUserId(), avatarImg, true);
                    contactNameTv.setText(contact.getToUserName());
                }
                if (isBatch) {// 批量
                    isFriendBtn.setVisibility(View.GONE);
                    isNotFriendBtn.setVisibility(View.GONE);
                    if (friend != null) {
                        checkBox.setVisibility(View.INVISIBLE);
                    } else {
                        checkBox.setVisibility(View.VISIBLE);
                        if (isSelectAll) {// 全选填充数据
                            mBatchAddContacts.put(contact.getToUserId(), contact);
                        }
                    }
                    if (position == mSortContactList.size() - 1) {
                        isSelectAll = false;// 全选填充数据结束
                    }
                    if (mBatchAddContacts.containsKey(contact.getToUserId())) {
                        checkBox.setChecked(true);
                    } else {
                        checkBox.setChecked(false);
                    }
                } else {
                    checkBox.setVisibility(View.GONE);

                    if (friend != null) {
                        String inAppName = TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName();
                        userNameTv.setText(getString(R.string.app_name) + ": " + inAppName);
                        isFriendBtn.setVisibility(View.VISIBLE);
                        isNotFriendBtn.setVisibility(View.GONE);
                        // 已添加的昵称变灰
                        contactNameTv.setTextColor(getResources().getColor(R.color.Grey_400));
                        userNameTv.setTextColor(getResources().getColor(R.color.Grey_400));
                    } else {
                        userNameTv.setText(getString(R.string.app_name) + ": " + contact.getToUserName());
                        isFriendBtn.setVisibility(View.GONE);
                        isNotFriendBtn.setVisibility(View.VISIBLE);
                        for (int i = 0; i < mBlackList.size(); i++) {
                            if (mBlackList.get(i).getUserId().equals(contact.getToUserId())) {// 该联系人存在于我的黑名单列表
                                isFriendBtn.setText(R.string.added_black);
                                isFriendBtn.setVisibility(View.VISIBLE);
                                isNotFriendBtn.setVisibility(View.GONE);
                            }
                        }
                        // 未添加的昵称变黑
                        contactNameTv.setTextColor(getResources().getColor(R.color.black));
                        userNameTv.setTextColor(getResources().getColor(R.color.Grey_600));
                    }
                }

                isNotFriendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<Contact> contactList = new ArrayList<>();
                        contactList.add(contact);
                        addFriendInContacts(contactList, false);
                    }
                });
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
