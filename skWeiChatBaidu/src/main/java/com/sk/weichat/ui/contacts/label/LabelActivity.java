package com.sk.weichat.ui.contacts.label;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.roamer.slidelistview.SlideBaseAdapter;
import com.roamer.slidelistview.SlideListView;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.Label;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.db.dao.LabelDao;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.ViewHolder;
import com.sk.weichat.view.PullToRefreshSlideListView;
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

/**
 * Search Location:
 * <p>
 * AddContactsActivity:        activity_select_contacts.xml | row_select_contacts
 * <p>
 * SelectContactsActivity:     activity_select_contacts.xml | row_select_contacts
 * <p>
 * SelectLabelFriendActivity: activity_select_contacts.xml | row_select_contacts_clone
 * <p>
 * SelectFriendsActivity:        activity_select_friends | row_select_friend
 * <p>
 * SelectCardPopupWindow: pop_send_card.xml | row_select_contacts
 * <p>
 * AtSeeCircleActivity:            activity_select_contacts | row_select_remind_see_circle
 * <p>
 * SelectRoomMemberPopupWindow: pop_at_room_member | a_item_set_manager
 * <p>
 * SetManagerActivity&&CancelManagerActivity: activity_set_manager.xml | a_item_set_manager
 * <p>
 * JitsiInviteActivity: activity_jitsi_invite | row_select_contacts_jitsi
 * <p>
 * SelectPrefixActivity: a_act_selectaddr | a_item_resume_fnid
 * <p>
 * OK Button Style: activity_select_contacts | activity_add_muc_file | activity_map_picker
 * <p>
 * Select Location:
 * <p>
 * type 1
 * SelectContactsActivity
 * AddContactsActivity
 * SelectLabelFriendActivity
 * SelectFriendsActivity
 * type 2
 * FriendFragment
 * RoomFragment
 * BlackActivity
 * SelectNewContactsActivity
 * SelectNewGroupInstantActivity
 * type 3
 * GroupMoreFeaturesActivity
 * GroupTransferActivity
 * <p>
 * 发送名片、@
 * 提醒谁看、会议邀请、管理员
 */
public class LabelActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mCreateLinearLayout;
    private Button mCreateButton;

    private PullToRefreshSlideListView mListView;
    private View mHeadView;
    private LabelAdapter mLabelAdapter;
    private List<Label> mLabelList;
    private String mLoginUserId;

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, LabelActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label);
        initActionBar();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(this);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.tag);
    }

    private void initView() {
        mLabelList = new ArrayList<>();
        mLoginUserId = coreManager.getSelf().getUserId();

        mCreateLinearLayout = (LinearLayout) findViewById(R.id.create_ll);
        mCreateButton = (Button) findViewById(R.id.create_btn);
        ButtonColorChange.colorChange(this, mCreateButton);
        mCreateButton.setOnClickListener(this);

        mListView = (PullToRefreshSlideListView) findViewById(R.id.pull_refresh_list);
        mHeadView = LayoutInflater.from(this).inflate(R.layout.a_head_for_create_label, null);
        mHeadView.findViewById(R.id.head_for_add_label).setOnClickListener(this);
        mListView.getRefreshableView().addHeaderView(mHeadView, null, false);
        mLabelAdapter = new LabelAdapter(this);
        mListView.getRefreshableView().setAdapter(mLabelAdapter);
        mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position = (int) id;
                Label label = mLabelList.get(position);
                if (label != null) {
                    Intent intent = new Intent(LabelActivity.this, CreateLabelActivity.class);
                    intent.putExtra("isEditLabel", true);
                    intent.putExtra("labelId", label.getGroupId());
                    startActivity(intent);
                }
            }
        });
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<SlideListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<SlideListView> refreshView) {
                refreshLabelListFromService();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_left:
                finish();
                break;
            case R.id.create_btn:
            case R.id.head_for_add_label:
                Intent intent = new Intent(this, CreateLabelActivity.class);
                intent.putExtra("isEditLabel", false);
                startActivity(intent);
                break;
        }
    }

    // 从数据库加载标签
    private void loadData() {
        mLabelList.clear();

        mLabelList = LabelDao.getInstance().getAllLabels(mLoginUserId);
        if (mLabelList.size() == 0) {
            mCreateLinearLayout.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        } else {
            mCreateLinearLayout.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }
        mListView.onRefreshComplete();
        mLabelAdapter.notifyDataSetChanged();
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
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            List<Label> labelList = result.getData();
                            LabelDao.getInstance().refreshLabel(mLoginUserId, labelList);

                            loadData();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    // 删除标签
    private void deleteLabel(final Label label) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("groupId", label.getGroupId());
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().FRIENDGROUP_DELETE)
                .params(params)
                .build()
                .execute(new BaseCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ObjectResult<Label> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            LabelDao.getInstance().deleteLabel(mLoginUserId, label.getGroupId());
                            loadData();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    class LabelAdapter extends SlideBaseAdapter {

        public LabelAdapter(Context context) {
            super(context);
        }

        @Override
        public int getFrontViewId(int position) {
            return R.layout.row_label;
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
            if (mLabelList != null) {
                return mLabelList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mLabelList != null) {
                return mLabelList.get(position);
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
            TextView labelName = ViewHolder.get(convertView, R.id.label_name);
            TextView labelUserNames = ViewHolder.get(convertView, R.id.label_user_name);
            TextView delete_tv = ViewHolder.get(convertView, R.id.delete_tv);
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
                    labelUserNames.setText(userNames);
                } else labelUserNames.setText("");
            }

            delete_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteLabel(label);
                }
            });
            return convertView;
        }
    }
}
