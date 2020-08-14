package com.sk.weichat.ui.contacts;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.Base64;
import com.sk.weichat.util.CommonAdapter;
import com.sk.weichat.util.CommonViewHolder;
import com.sk.weichat.util.HtmlUtils;
import com.sk.weichat.util.StringUtils;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.secure.AES;
import com.sk.weichat.util.secure.chat.SecureChatUtil;

import java.util.List;


/**
 * Created by Administrator on 2018/4/24 0024.
 * 回话记录
 */

public class TalkHistoryActivity extends BaseActivity {
    private PullToRefreshListView mPullToRefreshListView;
    private TalkHistoryAdapter mTalkHistoryAdapter;
    private List<ChatMessage> mChatMessages;

    private String mLoginUserId;
    private String mFriendId, mFriendName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_pullrefresh_list);
        if (getIntent() != null) {
            mFriendId = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
            mFriendName = getIntent().getStringExtra(AppConstant.EXTRA_NICK_NAME);
        }
        mLoginUserId = coreManager.getSelf().getUserId();
        initActionBar();
        initView();
        loadData();
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
        tvTitle.setText(mFriendName);
    }

    private void initView() {
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
    }

    private void loadData() {
        mChatMessages = ChatMessageDao.getInstance().getSingleChatMessages(mLoginUserId, mFriendId, TimeUtils.sk_time_current_time(), 500);
        if (mChatMessages == null || mChatMessages.size() <= 0) {
            return;
        }

        // 数据库取出来时，content需要解密
        for (int i = 0; i < mChatMessages.size(); i++) {
            ChatMessage message = mChatMessages.get(i);
            if (!TextUtils.isEmpty(message.getContent())) {
                String key = SecureChatUtil.getSymmetricKey(message.getPacketId());
                try {
                    String s = AES.decryptStringFromBase64(message.getContent(), Base64.decode(key));
                    message.setContent(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        mTalkHistoryAdapter = new TalkHistoryAdapter(this, mChatMessages);
        mPullToRefreshListView.getRefreshableView().setAdapter(mTalkHistoryAdapter);
    }

    public String getStr(int type) {
        String content = "";
        switch (type) {
            case XmppMessage.TYPE_TEXT:
                content = getString(R.string.msg_word);
                break;
            case XmppMessage.TYPE_VOICE:
                content = getString(R.string.msg_voice);
                break;
            case XmppMessage.TYPE_GIF:
                content = getString(R.string.msg_animation);
                break;
            case XmppMessage.TYPE_IMAGE:
                content = getString(R.string.msg_picture);
                break;
            case XmppMessage.TYPE_VIDEO:
                content = getString(R.string.msg_video);
                break;
            case XmppMessage.TYPE_RED:
                content = getString(R.string.msg_red_packet);
                break;
            case XmppMessage.TYPE_LOCATION:
                content = getString(R.string.msg_location);
                break;
            case XmppMessage.TYPE_CARD:
                content = getString(R.string.msg_card);
                break;
            case XmppMessage.TYPE_FILE:
                content = getString(R.string.msg_file);
                break;
            case XmppMessage.TYPE_TIP:
                content = getString(R.string.msg_system);
                break;
            case XmppMessage.TYPE_IMAGE_TEXT:
            case XmppMessage.TYPE_IMAGE_TEXT_MANY:
                content = getString(R.string.msg_image_text);
                break;
            case XmppMessage.TYPE_LINK:
            case XmppMessage.TYPE_SHARE_LINK:
                content = getString(R.string.msg_link);
                break;
            case XmppMessage.TYPE_SHAKE:
                content = getString(R.string.msg_shake);
                break;
            case XmppMessage.TYPE_CHAT_HISTORY:
                content = getString(R.string.msg_chat_history);
                break;
        }

        if (type >= 100 && type <= 120) {
            content = getString(R.string.msg_video_voice);
        }
        return content;
    }

    class TalkHistoryAdapter extends CommonAdapter<ChatMessage> {

        TalkHistoryAdapter(Context context, List<ChatMessage> data) {
            super(context, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.item_talk_history, position);
            RelativeLayout rl1 = viewHolder.getView(R.id.rl1);
            ImageView avatar1 = viewHolder.getView(R.id.ava1);
            TextView tv1 = viewHolder.getView(R.id.tv1);
            ImageView iv1 = viewHolder.getView(R.id.iv1);
            RelativeLayout rl2 = viewHolder.getView(R.id.rl2);
            ImageView avatar2 = viewHolder.getView(R.id.ava2);
            TextView tv2 = viewHolder.getView(R.id.tv2);
            ImageView iv2 = viewHolder.getView(R.id.iv2);
            if (data.size() > 0) {
                ChatMessage chatMessage = data.get(data.size() - (position + 1));// 倒过来拿数据
                if (chatMessage.getFromUserId().equals(mLoginUserId)) {
                    rl1.setVisibility(View.VISIBLE);
                    rl2.setVisibility(View.GONE);

                    AvatarHelper.getInstance().displayAvatar(mLoginUserId, avatar1);

                    if (chatMessage.getType() == XmppMessage.TYPE_TEXT
                            && !chatMessage.getIsReadDel()) {// 文本 非阅后即焚消息
                        String s = StringUtils.replaceSpecialChar(chatMessage.getContent());
                        CharSequence content = HtmlUtils.transform200SpanString(s, true);
                        tv1.setText(content);
                    } else {
                        tv1.setText(getStr(chatMessage.getType()));
                    }

                    if (chatMessage.getIsReadDel()) {// 阅后即焚 添加图标
                        iv1.setVisibility(View.VISIBLE);
                    } else {
                        iv1.setVisibility(View.GONE);
                    }
                } else {
                    rl1.setVisibility(View.GONE);
                    rl2.setVisibility(View.VISIBLE);

                    AvatarHelper.getInstance().displayAvatar(chatMessage.getFromUserName(), chatMessage.getFromUserId(), avatar2, false);

                    if (chatMessage.getType() == XmppMessage.TYPE_TEXT
                            && !chatMessage.getIsReadDel()) {// 文本 非阅后即焚消息
                        String s = StringUtils.replaceSpecialChar(chatMessage.getContent());
                        CharSequence content = HtmlUtils.transform200SpanString(s, true);
                        tv2.setText(content);
                    } else {
                        tv2.setText(getStr(chatMessage.getType()));
                    }

                    if (chatMessage.getIsReadDel()) {
                        iv2.setVisibility(View.VISIBLE);
                    } else {
                        iv2.setVisibility(View.GONE);
                    }
                }
            }
            return viewHolder.getConvertView();
        }
    }
}
