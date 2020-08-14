package com.sk.weichat.db.dao;

import android.text.TextUtils;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.AttentionUser;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.User;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.NewFriendMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.broadcast.CardcastUiUpdateUtil;
import com.sk.weichat.db.SQLiteHelper;
import com.sk.weichat.helper.FriendHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.xmpp.ListenerManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 访问NewFriend数据的Dao
 */
public class NewFriendDao {
    private static NewFriendDao instance = null;
    public Dao<NewFriendMessage, Integer> newFriendDao;

    private NewFriendDao() {
        try {
            newFriendDao = DaoManager.createDao(OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class).getConnectionSource(),
                    NewFriendMessage.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static NewFriendDao getInstance() {
        if (instance == null) {
            synchronized (NewFriendDao.class) {
                if (instance == null) {
                    instance = new NewFriendDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        OpenHelperManager.releaseHelper();
    }

    /**
     * 更新阅读状态
     *
     * @param ownerId
     */
    public void markNewFriendRead(String ownerId) {
        UpdateBuilder<NewFriendMessage, Integer> builder = newFriendDao.updateBuilder();
        try {
            builder.updateColumnValue("isRead", true);
            builder.where().eq("ownerId", ownerId).and().eq("isRead", true);
            newFriendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**********************************************************************************
     * Todo NewFriend Add UnRead Start
     **********************************************************************************/
    // 更新单个NewFriend的消息未读数量
    public void markNewFriendUnRead(String ownerId, String friendId) {
        Log.e("markNewMessageUnRead", "+1条未读消息");
        NewFriendMessage mNewFriendMessage = getNewFriendById(ownerId, friendId);
        if (mNewFriendMessage != null) {
            int unReadCount = mNewFriendMessage.getUnReadNum();
            mNewFriendMessage.setUnReadNum(++unReadCount);
            try {
                newFriendDao.update(mNewFriendMessage);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 获取单个单个NewFriend的消息未读数量
    public int getNewFriendUnRead(String ownerId, String friendId) {
        Log.e("markNewMessageUnRead", "+1条未读消息");
        NewFriendMessage mNewFriendMessage = getNewFriendById(ownerId, friendId);
        if (mNewFriendMessage != null) {
            return mNewFriendMessage.getUnReadNum();
        }
        return 0;
    }

    // 重置所有NewFriend的未读数量
    public void resetAllNewFriendUnRead(String owenId) {
        List<NewFriendMessage> mAllNewFriend = getAllNewFriendMsg(owenId);
        if (mAllNewFriend != null && mAllNewFriend.size() > 0) {
            for (int i = 0; i < mAllNewFriend.size(); i++) {
                NewFriendMessage mNewFriendMessage = mAllNewFriend.get(i);
                mNewFriendMessage.setUnReadNum(0);
                try {
                    newFriendDao.update(mNewFriendMessage);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**********************************************************************************
     * Todo NewFriend Add UnRead End
     **********************************************************************************/

    /**
     * 更新好友状态
     *
     * @param friendId
     */
    public void changeNewFriendState(String friendId, int state) {
        UpdateBuilder<NewFriendMessage, Integer> builder = newFriendDao.updateBuilder();
        String myId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
        try {
            builder.updateColumnValue("state", state);
            builder.where().eq("ownerId", myId).and().eq("userId", friendId);
            newFriendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateNewFriendContent(String friendId, String content) {
        UpdateBuilder<NewFriendMessage, Integer> builder = newFriendDao.updateBuilder();
        String myId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
        try {
            builder.updateColumnValue("content", content);
            builder.where().eq("ownerId", myId).and().eq("userId", friendId);
            newFriendDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param ownerId  当前登陆用户的userId
     * @param friendId
     * @return
     */
    public NewFriendMessage getNewFriendById(String ownerId, String friendId) {
        try {
            PreparedQuery<NewFriendMessage> preparedQuery = newFriendDao.queryBuilder()
                    .where().eq("ownerId", ownerId)
                    .and().eq("userId", friendId)
                    .prepare();
            List<NewFriendMessage> friends = newFriendDao.query(preparedQuery);
            if (friends != null && friends.size() > 0) {
                return friends.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createOrUpdateNewFriend(NewFriendMessage newFriend) {
        try {
            PreparedQuery<NewFriendMessage> preparedQuery = newFriendDao.queryBuilder()
                    .where().eq("ownerId", newFriend.getOwnerId())
                    .and().eq("userId", newFriend.getUserId())
                    .prepare();
            // Update会更新所有字段，不建议频繁使用
/*
            if (existFriend != null) {
                newFriend.set_id(existFriend.get_id());
            }
            CreateOrUpdateStatus status = newFriendDao.createOrUpdate(newFriend);
            return status.isCreated() || status.isUpdated();
*/
            NewFriendMessage existFriend = newFriendDao.queryForFirst(preparedQuery);
            if (existFriend != null) {
                // 如果content不为空，仅更新content字段吧
                if (!TextUtils.isEmpty(newFriend.getContent())) {
                    updateNewFriendContent(newFriend.getUserId(), newFriend.getContent());
                }
                return true;
            } else {
                newFriendDao.create(newFriend);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 分页查询最近聊天的好友
     */
    @Deprecated
    public List<NewFriendMessage> getNearlyNewFriendMsg(String ownerId, int pageIndex, int pageSize) {
        List<NewFriendMessage> friends = null;
        try {
            PreparedQuery<NewFriendMessage> preparedQuery = newFriendDao.queryBuilder()
                    .orderBy("timeSend", false)
                    .limit((long) pageSize)
                    .offset((long) pageSize * pageIndex)
                    .where().eq("ownerId", ownerId)
                    .prepare();
            friends = newFriendDao.query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    public List<NewFriendMessage> getAllNewFriendMsg(String ownerId) {
        List<NewFriendMessage> friends = null;
        try {
            PreparedQuery<NewFriendMessage> preparedQuery = newFriendDao.queryBuilder()
                    .orderBy("timeSend", false)
                    .where().eq("ownerId", ownerId)
                    .prepare();
            friends = newFriendDao.query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    /**
     * 本地生成好友关系
     *
     * @param newFriendMessage
     * @param status
     */
    public void ascensionNewFriend(NewFriendMessage newFriendMessage, int status) {
        createOrUpdateNewFriend(newFriendMessage);
        createFriendAndSetStatus(newFriendMessage, status);
    }

    private void createFriendAndSetStatus(final NewFriendMessage newFriend, final int status) {
        // 先将好友添加至本地 在调用接口获取好友信息，判断是否为公众号 如为公众号，更新好友Status
        FriendDao.getInstance().createOrUpdateFriendByNewFriend(newFriend, status);
        CardcastUiUpdateUtil.broadcastUpdateUi(MyApplication.getContext());

        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(MyApplication.getInstance()).accessToken);
        params.put("userId", newFriend.getUserId());

        HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getInstance()).USER_GET_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<User>(User.class) {
                    @Override
                    public void onResponse(ObjectResult<User> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            User user = result.getData();
                            if (user.getUserType() == 2) {// 公众号
                                FriendDao.getInstance().updateFriendStatus(CoreManager.requireSelf(MyApplication.getContext()).getUserId(),
                                        newFriend.getUserId(), Friend.STATUS_SYSTEM);
                                CardcastUiUpdateUtil.broadcastUpdateUi(MyApplication.getContext());
                            } else if (user.getFriends() != null) {
                                AttentionUser attentionUser = user.getFriends();
                                // 服务器的状态 与本地状态对比
                                // 获取可能存在的陌生人时设置的备注，
                                if (FriendHelper.updateFriendRelationship(attentionUser.getUserId(), user)) {
                                    CardcastUiUpdateUtil.broadcastUpdateUi(MyApplication.getContext());
                                }
                                FriendDao.getInstance().updateFriendPartStatus(newFriend.getUserId(), user);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    /**
     * 通讯录 自动成为好友 ||  手动加好友(无视对方验证)
     */
    public void addFriendOperating(String userId, String nickName, String remarkName) {
        String mLoginUserId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();
        NewFriendMessage message = NewFriendMessage.createLocalMessage(
                CoreManager.requireSelf(MyApplication.getInstance()), XmppMessage.TYPE_FRIEND, null, userId, nickName);

        // 手机联系人 直接成为好友 不调用接口判断是否为公众号
        createOrUpdateNewFriend(message);
        FriendDao.getInstance().createOrUpdateFriendByNewFriend(message, Friend.STATUS_FRIEND);

        ChatMessage addChatMessage = new ChatMessage();
        addChatMessage.setContent(MyApplication.getInstance().getString(R.string.add_friend) + ":" + nickName);
        addChatMessage.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        FriendDao.getInstance().updateLastChatMessage(mLoginUserId, Friend.ID_NEW_FRIEND_MESSAGE, addChatMessage);

        NewFriendDao.getInstance().changeNewFriendState(userId, Friend.STATUS_25);
        if (!TextUtils.isEmpty(remarkName)) {
            FriendDao.getInstance().updateRemarkName(mLoginUserId, userId, remarkName);
        }
        FriendDao.getInstance().updateFriendContent(mLoginUserId, userId,
                MyApplication.getInstance().getString(R.string.be_friendand_chat), XmppMessage.TYPE_TEXT, TimeUtils.sk_time_current_time());
        ListenerManager.getInstance().notifyNewFriend(mLoginUserId, message, true);
        CardcastUiUpdateUtil.broadcastUpdateUi(MyApplication.getContext());
    }
}