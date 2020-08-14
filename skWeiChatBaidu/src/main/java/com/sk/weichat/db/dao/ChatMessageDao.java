package com.sk.weichat.db.dao;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.j256.ormlite.android.DatabaseTableConfigUtil;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.MsgRoamTask;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.db.SQLiteHelper;
import com.sk.weichat.db.SQLiteRawUtil;
import com.sk.weichat.db.UnlimitDaoManager;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.message.HandleSecureChatMessage;
import com.sk.weichat.ui.mucfile.XfileUtils;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.Base64;
import com.sk.weichat.util.DES;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.secure.AES;
import com.sk.weichat.util.secure.DH;
import com.sk.weichat.util.secure.chat.SecureChatUtil;
import com.sk.weichat.xmpp.listener.ChatMessageListener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ChatMessageDao {
    private static final String TAG = "ChatMessageDao";
    private static ChatMessageDao instance = null;
    private SQLiteHelper mHelper;
    private Map<String, Dao<ChatMessage, Integer>> mDaoMap;

    private ChatMessageDao() {
        mHelper = OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class);
        mDaoMap = new HashMap<String, Dao<ChatMessage, Integer>>();
    }

    public static ChatMessageDao getInstance() {
        if (instance == null) {
            synchronized (ChatMessageDao.class) {
                if (instance == null) {
                    instance = new ChatMessageDao();
                }
            }
        }
        return instance;
    }

    /**
     * 根据不同的消息类型，返回相应的重发次数
     */
    public static int fillReCount(int type) {
        int recount = 0;
        if (type < 100) {// 重发recount次
            recount = 5;
        }
        return recount;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        OpenHelperManager.releaseHelper();
    }

    private Dao<ChatMessage, Integer> getDao(String ownerId, String friendId) {
        if (TextUtils.isEmpty(ownerId) || TextUtils.isEmpty(friendId)) {
            return null;
        }
        String tableName = SQLiteRawUtil.CHAT_MESSAGE_TABLE_PREFIX + ownerId + friendId;
        if (mDaoMap.containsKey(tableName)) {
            return mDaoMap.get(tableName);
        }
        Dao<ChatMessage, Integer> dao = null;
        try {
            DatabaseTableConfig<ChatMessage> config = DatabaseTableConfigUtil.fromClass(mHelper.getConnectionSource(), ChatMessage.class);
            config.setTableName(tableName);
            SQLiteRawUtil.createTableIfNotExist(mHelper.getWritableDatabase(), tableName, SQLiteRawUtil.getCreateChatMessageTableSql(tableName));
            dao = UnlimitDaoManager.createDao(mHelper.getConnectionSource(), config);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (dao != null)
            mDaoMap.put(tableName, dao);
        return dao;
    }

    /**
     * 保存一条新的聊天记录
     */
    public boolean saveNewSingleChatMessage(String ownerId, String friendId, ChatMessage chatMessage) {
        // clone chatMessage的所有参数
        ChatMessage message = chatMessage.cloneAll();

        Log.e(TAG, "开始存消息");
        if (XfileUtils.isNotChatVisibility(message.getType())) {
            Log.e(TAG, "isNotChatVisibility");
            return false;
        }
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            Log.e(TAG, "dao == nul");
            return false;
        }
        try {
            // 去除重复消息
            List<ChatMessage> chatMessages = dao.queryForEq("packetId", message.getPacketId());
            Log.e(TAG, message.getPacketId());
            if (chatMessages != null && chatMessages.size() > 0) {
                Log.e(TAG, "重复消息");
                return false;
            }

            String content = message.getContent();// 下面要用到content明文，先取出来
            if (MyApplication.IS_SUPPORT_SECURE_CHAT) {
                // SecureFlag 非端到端，数据库以明文存储
                if (!TextUtils.isEmpty(content) && !message.isVerifySignatureFailed()
                        && message.getType() != XmppMessage.TYPE_READ
                        && !TextUtils.isEmpty(message.getSignature())) {
                    // content不为空且验签成功且非已读消息，本地数据库以密文的方式存储
                    // 新增条件：signature字段不为空，仅加密以端到端方式传输的消息
                    String key = SecureChatUtil.getSymmetricKey(message.getPacketId());
                    message.setContent(AES.encryptBase64(message.getContent(), Base64.decode(key)));
                }
            }

            // 为群组消息且发送方为我的好友且备注了，修改fromUserName
            // 提示消息只需要更新Content字段
            if (message.getType() != XmppMessage.TYPE_READ
                    && message.getType() != XmppMessage.TYPE_TIP
                    && message.isGroup()) {
                String groupNameForGroupOwner = RoomMemberDao.getInstance().getRoomRemarkName(friendId, message.getFromUserId());
                if (!TextUtils.isEmpty(groupNameForGroupOwner)) {
                    message.setFromUserName(groupNameForGroupOwner);
                } else {
                    Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), message.getFromUserId());
                    if (friend != null && !TextUtils.isEmpty(friend.getRemarkName())) {
                        message.setFromUserName(friend.getRemarkName());
                    }
                }
            }

            // 保存该条消息
            dao.create(message);

            Log.e(TAG, "存表成功，更新朋友表最后一次消息事件");
            if (message.getType() != XmppMessage.TYPE_READ) {// 已读消息不更新
                if (message.isGroup()) {// 群组
                    if (message.getType() == XmppMessage.TYPE_TIP || TextUtils.isEmpty(message.getFromUserName())) {// 群组控制消息 || FromUserName为空
                        FriendDao.getInstance().updateFriendContent(ownerId, friendId, content, message.getType(), message.getTimeSend());
                    } else {
                        FriendDao.getInstance().updateFriendContent(ownerId, friendId, message.getFromUserName() + " : " + content, message.getType(), message.getTimeSend());
                    }
                } else {
                    String str;
                    if (message.getIsReadDel()
                            && (message.getType() == XmppMessage.TYPE_TEXT
                            || message.getType() == XmppMessage.TYPE_REPLAY)) {
                        str = MyApplication.getContext().getString(R.string.tip_click_to_read);
                    } else {
                        str = content;
                    }
                    FriendDao.getInstance().updateFriendContent(ownerId, friendId, str, message.getType(), message.getTimeSend());
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            // Todo 问题描述：在群组内发消息其他人能收到，自己却收不到其他人的消息，且再次进入该群组聊天界面之前发的消息也消失了
            // Todo 问题分析：断点发现抛出了SQLException 基本都是本地不存在该消息表，但是对应的dao却还存在，导致了这个问题
            // Todo 问题解决：之前想从源头解决该问题，但该问题极难复现且不好分析，现抛出该异常时检查本地是否存在该消息表，如不存在重建该张消息表
            Log.e(TAG, e.getCause().getMessage());
            String tableName = SQLiteRawUtil.CHAT_MESSAGE_TABLE_PREFIX + ownerId + friendId;
            if (!SQLiteRawUtil.isTableExist(mHelper.getWritableDatabase(), tableName)) {
                Log.e(TAG, tableName + "不存在，重新创建");
                SQLiteRawUtil.createTableIfNotExist(mHelper.getWritableDatabase(), tableName, SQLiteRawUtil.getCreateChatMessageTableSql(tableName));
                saveNewSingleChatMessage(ownerId, friendId, message);// 将之前存失败的消息在存一遍
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 保存漫游下来的聊天记录，不更新Friend表
     */
    public boolean saveRoamingChatMessage(String ownerId, String friendId, ChatMessage message, boolean isGroup) {
        decrypt(isGroup, message);// 对消息进行解密
        handlerRoamingSpecialMessage(message);

        Log.e(TAG, "开始存消息");
        if (XfileUtils.isNotChatVisibility(message.getType())) {
            Log.e(TAG, "isNotChatVisibility");
            return false;
        }
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            Log.e(TAG, "dao == nul");
            return false;
        }
        try {
            // 重复消息去除
            List<ChatMessage> chatMessages = dao.queryForEq("packetId", message.getPacketId());
            Log.e(TAG, message.getPacketId());
            if (chatMessages != null && chatMessages.size() > 0) {
                Log.e(TAG, "重复消息");
                return false;
            }

            if (MyApplication.IS_SUPPORT_SECURE_CHAT) {
                // SecureFlag 非端到端，数据库以明文存储
                if (!TextUtils.isEmpty(message.getContent()) && !message.isVerifySignatureFailed()
                        && message.getType() != XmppMessage.TYPE_READ
                        && !TextUtils.isEmpty(message.getSignature())) {
                    // content不为空且验签成功且非已读消息，本地数据库以密文的方式存储
                    //  新增条件：signature字段不为空，仅加密以端到端方式传输的消息
                    String key = SecureChatUtil.getSymmetricKey(message.getPacketId());
                    message.setContent(AES.encryptBase64(message.getContent(), Base64.decode(key)));
                }
            }

            // 保存这次的消息
            dao.create(message);
            Log.e(TAG, "存表成功");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 群发消息存本地，不更新Friend表
     */
    public boolean saveChatMessageWithOutRefreshFriend(String ownerId, String friendId, ChatMessage chatMessage) {
        // clone chatMessage的所有参数
        ChatMessage message = chatMessage.cloneAll();

        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            Log.e(TAG, "dao == nul");
            return false;
        }
        try {
            if (MyApplication.IS_SUPPORT_SECURE_CHAT) {
                // SecureFlag 非端到端，数据库以明文存储
                if (!TextUtils.isEmpty(message.getContent()) && !message.isVerifySignatureFailed()
                        && message.getType() != XmppMessage.TYPE_READ
                        && !TextUtils.isEmpty(message.getSignature())) {
                    // content不为空且验签成功且非已读消息，本地数据库以密文的方式存储
                    //  新增条件：signature字段不为空，仅加密以端到端方式传输的消息
                    String key = SecureChatUtil.getSymmetricKey(message.getPacketId());
                    message.setContent(AES.encryptBase64(message.getContent(), Base64.decode(key)));
                }
            }

            // 保存这次的消息
            dao.create(message);
            Log.e(TAG, "存表成功");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 保存 新的朋友 回话
     */
    public boolean saveNewSingleAnswerMessage(String ownerId, String friendId, ChatMessage message) {
        message.setSendRead(true);// 新的朋友消息默认为已读
        if (XfileUtils.isNotChatVisibility(message.getType())) {
            return false;
        }

        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return false;
        }
        try {
            List<ChatMessage> chatMessages = dao.queryForEq("packetId", message.getPacketId());
            if (chatMessages != null && chatMessages.size() > 0) {
                return false;
            }
            FriendDao.getInstance().updateFriendContent(ownerId, friendId,
                    message.getContent(), message.getType(), message.getTimeSend());
            // 保存这次的消息
            dao.create(message);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除某条消息
     */
    public boolean deleteSingleChatMessage(String ownerId, String friendId, String packet) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return false;
        }
        try {
            List<ChatMessage> chatMessages = dao.queryForEq("packetId", packet);
            if (chatMessages != null && chatMessages.size() > 0) {
                dao.delete(chatMessages);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除过期消息-单
     */
    public boolean deleteOutTimeChatMessage(String ownerId, String friendId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return false;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        List<ChatMessage> messages;
        try {
            builder.where().ne("deleteTime", -1)
                    .and().ne("deleteTime", 0)
                    .and().lt("deleteTime", TimeUtils.sk_time_current_time());// deleteTime不等于 -1 || 0（-1、0为永久保存）并且deleteTime小于当前时间 需要删除
            messages = dao.query(builder.prepare());
            Log.e("deleteTime", TimeUtils.sk_time_current_time() + "");
            if (messages != null && messages.size() > 0) {
                Log.e("deleteTime", messages.size() + "");
                dao.delete(messages);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新isExpired -群
     *
     * @param ownerId
     * @param friendId
     */
    public void updateExpiredStatus(String ownerId, String friendId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        List<ChatMessage> messages;
        try {
            builder.where().ne("isExpired", 1)
                    .and().ne("deleteTime", -1)
                    .and().ne("deleteTime", 0)
                    .and().lt("deleteTime", TimeUtils.sk_time_current_time());// deleteTime不等于 -1 || 0（-1、0为永久保存）并且deleteTime小于当前时间 需要删除
            messages = dao.query(builder.prepare());
            Log.e("deleteTime", TimeUtils.sk_time_current_time() + "");
            if (messages != null && messages.size() > 0) {
                Log.e("deleteTime", messages.size() + "");
                Object[] msgIds = new Object[messages.size()];
                for (int i = 0; i < messages.size(); i++) {
                    msgIds[i] = messages.get(i).getPacketId();
                }
                UpdateBuilder<ChatMessage, Integer> builder2 = dao.updateBuilder();
                builder2.updateColumnValue("isExpired", 1);
                builder2.where().in("packetId", msgIds);
                dao.update(builder2.prepare());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新isExpired -群
     * 群组因为可能重复拉取离线消息问题，消息不能删除，标记为过期吧
     *
     * @param ownerId
     * @param friendId
     */
    public boolean updateExpiredStatus(String ownerId, String friendId, String packetId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return false;
        }
        try {
            UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
            builder.updateColumnValue("isExpired", 1);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除与某人的聊天消息表
     */
    public void deleteMessageTable(String ownerId, String friendId) {
        String tableName = SQLiteRawUtil.CHAT_MESSAGE_TABLE_PREFIX + ownerId + friendId;
        if (mDaoMap.containsKey(tableName)) {
            mDaoMap.remove(tableName);
        }
        if (SQLiteRawUtil.isTableExist(mHelper.getWritableDatabase(), tableName)) {
            SQLiteRawUtil.dropTable(mHelper.getWritableDatabase(), tableName);
        }
    }

    /**
     * 通过_id 更新消息发送状态
     */
    public void updateMessageSendState(String ownerId, String friendId, int msg_id, int messageState) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            Log.e(TAG, "updateMessageSendState Failed");
            return;
        }

        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("messageState", messageState);
            builder.where().idEq(msg_id);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            Log.e(TAG, "updateMessageSendState SQLException");
            e.printStackTrace();
        }
    }

    /**
     * 通过packet 更新消息发送状态
     */
    public void updateMessageState(String ownerId, String friendId, String packetId, int messageState) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("messageState", messageState);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
            Log.e(TAG, "消息发送状态更新成功-->packetId：" + packetId + "，messageState" + messageState);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "消息发送状态更新失败-->packetId：" + packetId + "，messageState" + messageState);
        }
    }


    /**
     * 更新消息上传进度
     */
    public void updateMessageUploadSchedule(String ownerId, String friendId, String packetId, int uploadSchedule) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        Log.e(TAG, "updateMessageUploadSchedule: " + uploadSchedule);
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("uploadSchedule", uploadSchedule);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新消息上传状态
     */
    public void updateMessageUploadState(String ownerId, String friendId, String packetId, boolean isUpload, String url) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("isUpload", isUpload);
            builder.updateColumnValue("content", url);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新消息下载状态
     */
    public void updateMessageDownloadState(String ownerId, String friendId, int msg_id, boolean isDownload, String filePath) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("isDownload", isDownload);
            builder.updateColumnValue("filePath", filePath);
            builder.where().idEq(msg_id);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新已读 单
     */
    public void updateMessageRead(String ownerId, String friendId, String packetId, boolean state) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            Log.e(TAG, "更新已读失败:" + friendId);
            return;
        }

        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("sendRead", state);
            if (state) { // 改变这条已读的是时候 加一个消息容错
                builder.updateColumnValue("messageState", ChatMessageListener.MESSAGE_SEND_SUCCESS);
            }
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            Log.e(TAG, "更新已读失败:" + packetId);
            e.printStackTrace();
        }
    }

    /**
     * 更新已读 群
     */
    public void updateMessageRead(String ownerId, String friendId, ChatMessage chatMessage) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            Log.e(TAG, "更新已读失败:" + friendId);
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            // builder.updateColumnValue("sendRead", true);
            builder.updateColumnValue("readPersons", chatMessage.getReadPersons());
            builder.updateColumnValue("readTime", chatMessage.getReadTime());
            builder.where().eq("packetId", chatMessage.getPacketId());

            dao.update(builder.prepare());
        } catch (SQLException e) {
            Log.e(TAG, "更新已读失败:" + chatMessage.getPacketId());
            e.printStackTrace();
        }
    }

    /**
     * 更新消息content
     */
    public void updateMessageContent(String ownerId, String friendId, String packetId, String content) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("content", content);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新消息内fromUserName
     */
    public void updateNickName(String ownerId, String friendId, String fromUserId, String newNickName) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.where().eq("fromUserId", fromUserId);
            builder.updateColumnValue("fromUserName", newNickName);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户撤回了某条消息
     */
    public boolean updateMessageBack(String ownerId, String friendId, String packetId, String name) {
        // 更新message数据库
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            ChatMessage message = findMsgById(ownerId, friendId, packetId);
            if (message != null) {
                builder.updateColumnValue("content", name + " " + MyApplication.getInstance().getString(R.string.other_withdraw));
                builder.updateColumnValue("type", XmppMessage.TYPE_TIP);
                builder.where().eq("packetId", packetId);
                dao.update(builder.prepare());
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 群里有人撤回消息的情况，
     * 有可能是管理员撤回别人的，要提示管理撤回，
     */
    public void updateMessageBack(String ownerId, String friendId, String packetId, String name, String fromUserId) {
        // 更新message数据库
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            ChatMessage message = findMsgById(ownerId, friendId, packetId);
            if (message != null) {
                if (!TextUtils.equals(message.getFromUserId(), fromUserId)) {
                    builder.updateColumnValue("content", MyApplication.getInstance().getString(R.string.tip_withdraw_message_by_manager));
                } else {
                    builder.updateColumnValue("content", name + " " + MyApplication.getInstance().getString(R.string.other_withdraw));
                }
                builder.updateColumnValue("type", XmppMessage.TYPE_TIP);
                builder.where().eq("packetId", packetId);
                dao.update(builder.prepare());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户查看了阅后即焚消息
     */
    public boolean updateReadMessage(String ownerId, String friendId, String packetId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return false;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            ChatMessage message = findMsgById(ownerId, friendId, packetId);
            if (message != null && message.getIsReadDel()) {
                builder.updateColumnValue("content", MyApplication.getInstance().getString(R.string.tip_burn_message));
                builder.updateColumnValue("type", XmppMessage.TYPE_TIP);
                builder.where().eq("packetId", packetId);
                dao.update(builder.prepare());
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 更新文本消息的阅后即焚剩余查看时间
     */
    public void updateMessageReadTime(String ownerId, String friendId, String packetId, long time) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }

        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("readTime", time);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新戳一戳状态
     */
    public void updateMessageShakeState(String ownerId, String friendId, String packetId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("isDownload", true);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新红包、转账的领取状态
     */
    public boolean updateChatMessageReceiptStatus(String ownerId, String friendId, String packetId) {
        try {
            Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
            UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
            ChatMessage message = findMsgById(ownerId, friendId, packetId);
            if (message != null) {
                builder.updateColumnValue("fileSize", 2);
                builder.where().eq("packetId", packetId);
                dao.update(builder.prepare());
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新图片宽高
     */
    public void updateMessageLocationXY(String ownerId, String friendId, String packetId, String location_x, String location_y) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("location_x", location_x);
            builder.updateColumnValue("location_y", location_y);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateMessageLocationXY(ChatMessage newmsg, String userid) {
        // toUserId等于当前用户id的情况，表示消息来自单聊对方，fromUserId就是对方id,
        // 其他情况toUserId是单聊对方id或者群组jid,
        if (!TextUtils.equals(userid, newmsg.getToUserId())) {
            updateMessageLocationXY(userid, newmsg.getToUserId(), newmsg.getPacketId(), newmsg.getLocation_x(), newmsg.getLocation_y());
        } else {
            updateMessageLocationXY(userid, newmsg.getFromUserId(), newmsg.getPacketId(), newmsg.getLocation_x(), newmsg.getLocation_y());
        }
    }

    /**
     * 更新 群聊确认消息 的状态(不添加新字段了，以isDownload字段来标志，true 群组已确认 false 未确认)
     */
    public void updateGroupVerifyMessageStatus(String ownerId, String friendId, String packetId, boolean isDownload) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }
        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("isDownload", isDownload);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询与friendId的聊天记录
     *
     * @param time     小于此time
     * @param pageSize 查询几条数据
     * @return
     */
    public List<ChatMessage> getSingleChatMessages(String ownerId, String friendId, long time, int pageSize) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return null;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        List<ChatMessage> messages = new ArrayList<>();
        try {
            // builder.where().gt("_id", mMinId);
            builder.where().ne("type", XmppMessage.TYPE_READ).and().lt("timeSend", time);
            builder.orderBy("timeSend", false);
            builder.orderBy("_id", false);
            builder.limit((long) pageSize);
            builder.offset(0L);
            messages = dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * 查询与friendId的聊天记录
     *
     * @param ownerId
     * @param friendId
     * @param time     Search >= timeSend 's Messages
     * @return
     */
    public List<ChatMessage> searchMessagesByTime(String ownerId, String friendId, double time) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return null;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        List<ChatMessage> messages = new ArrayList<>();
        try {
            // builder.where().gt("_id", mMinId);
            builder.where().ne("type", XmppMessage.TYPE_READ).and()
                    .ge("timeSend", time);
            builder.orderBy("timeSend", false);
            messages = dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * 查询与friendId的聊天记录(群组专业，漫游任务参与sql)
     */
    public List<ChatMessage> getOneGroupChatMessages(String ownerId, String friendId, double time, int pageSize) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return null;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();

        // 取出该群组最后一条漫游任务
        MsgRoamTask mLastMsgRoamTask = MsgRoamTaskDao.getInstance().getFriendLastMsgRoamTask(ownerId, friendId);

        List<ChatMessage> messages = new ArrayList<>();
        try {
            if (mLastMsgRoamTask == null) {
                builder.where().ne("type", XmppMessage.TYPE_READ)
                        .and().ne("isExpired", 1)
                        .and().lt("timeSend", time);
                builder.orderBy("timeSend", false);
                builder.orderBy("_id", false);
                builder.limit((long) pageSize);
                builder.offset(0L);
            } else {
                builder.where().ne("type", XmppMessage.TYPE_READ)
                        .and().ne("isExpired", 1)
                        .and().ge("timeSend", mLastMsgRoamTask.getEndTime())
                        .and().lt("timeSend", time);
                builder.orderBy("timeSend", false);
                builder.orderBy("_id", false);
                builder.limit((long) pageSize);
                builder.offset(0L);
            }
            messages = dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * 取出课程 录制的消息 大于等于 startTime(开始录制的那条消息) 小于等于 endTime
     */
    public List<ChatMessage> getCourseChatMessage(String ownerId, String friendId, double startTime, double endTime, int pageSize) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return null;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        List<ChatMessage> messages = new ArrayList<>();
        try {
            // builder.where().gt("_id", mMinId);
            builder.where().ne("type", XmppMessage.TYPE_READ)
                    .and().ge("timeSend", startTime)
                    .and().le("timeSend", endTime);
            builder.orderBy("timeSend", false);
            builder.orderBy("_id", false);
            builder.limit((long) pageSize);
            builder.offset(0L);
            messages = dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * 获取该群组内userId发送的群组控制消息
     */
    public List<ChatMessage> getAllVerifyMessage(String ownerId, String friendId, String userId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return null;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        List<ChatMessage> messages = null;
        try {
            builder.where().eq("type", XmppMessage.TYPE_TIP)
                    .and().eq("fromUserId", userId);
            messages = dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * 获取objectId一致的消息
     */
    public List<ChatMessage> getAllSameObjectIdMessages(String ownerId, String friendId, String objectId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return null;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        List<ChatMessage> messages = new ArrayList<>();
        try {
            builder.where().eq("objectId", objectId);
            messages = dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * 获取最后一条聊天记录
     */
    public ChatMessage getLastChatMessage(String ownerId, String friendId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        ChatMessage chatMessage;
        if (dao == null) {
            return null;
        }

        try {
            QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
            builder.orderBy("timeSend", false);
            chatMessage = dao.queryForFirst(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return chatMessage;
    }

    /**
     * 查询与friendId的聊天记录
     *
     * @param time 小于此time
     * @return
     */
    public ChatMessage getLastChatMessage(String ownerId, String friendId, long time) {
        // todo 因为自动加群机制，长连接一连接上之后，服务端就帮当前用户加入了所有群组，
        //  此时如果这个群组还在产生聊天消息，客户端就会立刻收到这些在线消息并存到本地数据库，同时，
        //  客户端在获取群组离线消息是在getLast接口调用之后获取的，而getLast接口又是在长连接连上之后才调用的。
        //  所以，当getLast接口回调成功之后，在去批量获取群组离线消息，此时本地群组最后一条消息的timeSend就及有可能为上线之后收到的在线群组消息的timeSend，
        //  导致在该在timeSend之前产生的群组离线消息全部丢失
        // todo 在发送批量获取群组离线消息协议之前，在拼接timeSend时，如群组在本地有消息记录，
        //  则需要取出该群组消息表内小于等于本地离线时间的最后一条消息，如取不出或本地没有消息记录，则直接拼接本地离线时间去获取
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        ChatMessage chatMessage;
        if (dao == null) {
            return null;
        }
        try {
            QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
            builder.where().le("timeSend", time);
            builder.orderBy("timeSend", false);
            chatMessage = dao.queryForFirst(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return chatMessage;
    }


    /**
     * 读取一条数据
     * 用于确认消息已读的时候获取数据库中的某条消息，根据packetId来查找
     */
    public ChatMessage findMsgById(String ownerId, String friendId, String packetId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return null;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        ChatMessage messages = null;
        try {
            if (!TextUtils.isEmpty(packetId)) {
                builder.where().eq("packetId", packetId);
            }
            messages = dao.queryForFirst(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (MyApplication.IS_SUPPORT_SECURE_CHAT) {
            return decryptSqLiteMessage(messages);
        } else {
            return messages;
        }
    }

    /**
     * 查询消息是否重复
     */
    public boolean hasSameMessage(String ownerId, String friendId, String packetId) {
        boolean exist;
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return false;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        List<ChatMessage> messages = null;
        try {
            builder.where().eq("packetId", packetId);
            messages = dao.query(builder.prepare());
            if (messages != null && messages.size() > 0) {
                exist = true;
            } else {
                exist = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            exist = false;
        }
        return exist;
    }

    /**
     * 查询已读消息是否重复
     */
    public boolean checkRepeatRead(String ownerId, String friendId, String userId, String content) {
        boolean b = false;
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();

        List<ChatMessage> messages;
        try {
            builder.where().eq("type", XmppMessage.TYPE_READ)
                    .and().eq("content", content)
                    .and().eq("fromUserId", userId);
            messages = builder.query();
            if (messages != null && messages.size() > 0) {
                b = true;
            } else {
                b = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * 查询某条消息的已读成员列表
     */
    public List<ChatMessage> queryFriendsByReadList(String loginUserId, String roomId, String packetId, int pager) {
        Dao<ChatMessage, Integer> dao = getDao(loginUserId, roomId);
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();

        try {
            builder.where().eq("type", XmppMessage.TYPE_READ).and().eq("content", packetId);
            builder.orderBy("timeSend", false);
            long k = (pager + 1) * 10;
            builder.limit(k);
            List<ChatMessage> list = builder.query();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询某个朋友的某一条聊天记录
     * 用于消息界面的查询历史聊天记录
     */
    public List<Friend> queryChatMessageByContent(Friend friend, String content) {
        String loginUserId = CoreManager.requireSelf(MyApplication.getInstance()).getUserId();

        Dao<ChatMessage, Integer> dao = getDao(loginUserId, friend.getUserId());
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();

        try {
            builder.where().eq("type", "1").and().like("content", "%" + content + "%");
            builder.orderBy("timeSend", true);

            List<ChatMessage> query = builder.query();
            if (query == null) {
                return null;
            }

            List<Friend> friends = new ArrayList<>();
            for (int i = 0; i < query.size(); i++) {
                ChatMessage chatMessage = query.get(i);
                if (!TextUtils.isEmpty(chatMessage.getSignature())) {
                    // 因为端到端消息在数据库内为密文存储，此处过滤掉端到端消息
                    continue;
                }
                Friend temp = new Friend();
                temp.setUserId(friend.getUserId());
                // 用于显示群组头像，
                temp.setRoomId(friend.getRoomId());
                temp.setNickName(friend.getNickName());
                temp.setRoomFlag(friend.getRoomFlag());
                temp.setContent(chatMessage.getContent());
                temp.setTimeSend(chatMessage.getTimeSend());
                // Todo 2019.2.18  现改为double查询，更加精确，不添加新字段了，就放在ChatRecordTimeOut字段内
                temp.setChatRecordTimeOut(chatMessage.getDoubleTimeSend());
                friends.add(temp);
            }
            return friends;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定Type类型的消息 过滤阅后即焚消息
     */
    public List<ChatMessage> queryChatMessageByType(String ownerId, String friendId, int type) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        List<ChatMessage> messages = new ArrayList<>();
        if (dao == null) {
            return messages;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        try {
            builder.where().eq("type", type).and().ne("isReadDel", 1);
            messages = dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * 根据关键字查询消息
     */
    public List<ChatMessage> queryChatMessageByContent(String ownerId, String friendId, String content) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        try {
            builder.where().eq("type", "1").and().like("content", "%" + content + "%");
            builder.orderBy("timeSend", true);
            chatMessages = builder.query();
            if (chatMessages != null && chatMessages.size() > 0) {
                List<ChatMessage> removeMessages = new ArrayList<>();
                for (int i = 0; i < chatMessages.size(); i++) {
                    if (!TextUtils.isEmpty(chatMessages.get(i).getSignature())) {
                        // 因为端到端消息在数据库内为密文存储，此处过滤掉端到端消息
                        removeMessages.add(chatMessages.get(i));
                    }
                }
                chatMessages.removeAll(removeMessages);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chatMessages;
    }

    /**
     * 查询某条消息之后的消息
     * 该消息不存在就返回null
     */
    @Nullable
    public List<ChatMessage> searchFromMessage(Context ctx, String ownerId, String friendId, ChatMessage fromMessage) throws Exception {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        Objects.requireNonNull(dao);
        ChatMessage localFromMessage = dao.queryBuilder().where().ne("type", XmppMessage.TYPE_READ)
                .and().ne("isExpired", 1)
                .and().eq("packetId", fromMessage.getPacketId())
                .queryForFirst();
        if (localFromMessage == null) {
            return null;
        }
        return dao.queryBuilder()
                .orderBy("timeSend", true)
                .orderBy("_id", true)
                .where().ne("type", XmppMessage.TYPE_READ)
                .and().ne("isExpired", 1)
                // 时间大于等于查询出来的localFromMessage,
                .and().ge("timeSend", localFromMessage.getTimeSend())
                .query();
    }

    /**
     * 处理处理tigase/getLastChatList 获取到的特殊消息
     */
    public String handlerGetLastSpecialMessage(int isRoom, int type, String loginUserId, String from, String fromUserName, String toUserName) {
        String text = "";
        if (type == XmppMessage.TYPE_BACK) {
            if (TextUtils.equals(from, loginUserId)) {
                text = MyApplication.getContext().getString(R.string.you) + " " + MyApplication.getInstance().getString(R.string.other_with_draw);
            } else {
                text = fromUserName + " " + MyApplication.getInstance().getString(R.string.other_with_draw);
            }
        } else if (type == XmppMessage.TYPE_83) {
            // 单聊群聊一样的处理，
            if (TextUtils.equals(from, loginUserId)) {
                // 我领取了别人的红包 正常聊天该条消息是不会显示的，但是获取漫游的时候能将该条消息拉下来
                text = MyApplication.getContext().getString(R.string.red_received_self, toUserName);
            } else {
                // 别人领取了我的红包
                text = MyApplication.getContext().getString(R.string.tip_receive_red_packet_place_holder, fromUserName, MyApplication.getContext().getString(R.string.you));
            }
        } else if (type == XmppMessage.TYPE_RED_BACK) {
            text = MyApplication.getContext().getString(R.string.tip_red_back);
        } else if (type == XmppMessage.TYPE_TRANSFER_RECEIVE) {
            if (TextUtils.equals(from, loginUserId)) {
                // 我领取了对方的转账 正常聊天该条消息是不会显示的，但是获取漫游的时候能将该条消息拉下来
                text = MyApplication.getContext().getString(R.string.transfer_received_self);
            } else {
                // 对方领取了我的转账
                text = MyApplication.getContext().getString(R.string.transfer_received);
            }
        }
        return text;
    }

    /**
     * 处理tigase/shiku_msgs 获取到的特殊消息
     *
     * @param chatMessage
     * @return
     */
    public void handlerRoamingSpecialMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() == XmppMessage.TYPE_83) {
            // 红包领取 已过滤掉了
        } else if (chatMessage.getType() == XmppMessage.TYPE_RED_BACK) {
            chatMessage.setType(XmppMessage.TYPE_TIP);
            chatMessage.setContent(MyApplication.getContext().getString(R.string.tip_red_back));
        } else if (chatMessage.getType() == XmppMessage.TYPE_TRANSFER_RECEIVE) {
            chatMessage.setType(XmppMessage.TYPE_TIP);
            if (TextUtils.equals(chatMessage.getFromUserId(), CoreManager.requireSelf(MyApplication.getInstance()).getUserId())) {
                // 我领取了对方的转账 正常聊天该条消息是不会显示的，但是获取漫游的时候能将该条消息拉下来
                chatMessage.setContent(MyApplication.getContext().getString(R.string.transfer_received_self));
            } else {
                // 对方领取了我的转账
                chatMessage.setContent(MyApplication.getContext().getString(R.string.transfer_received));
            }
        } else if (chatMessage.getType() == XmppMessage.TYPE_SYNC_CLEAN_CHAT_HISTORY) {
            // 双向清楚聊天记录 已过滤掉了
        } else if (chatMessage.getType() == XmppMessage.NEW_MEMBER) {
            // 907消息还是处里一下吧
            chatMessage.setType(XmppMessage.TYPE_TIP);
            if (TextUtils.equals(chatMessage.getFromUserId(), chatMessage.getToUserId())) {
                chatMessage.setContent(chatMessage.getFromUserName() + " " + MyApplication.getContext().getString(R.string.Message_Object_Group_Chat));
            } else {
                chatMessage.setContent(chatMessage.getFromUserName() + " " + MyApplication.getContext().getString(R.string.message_object_inter_friend) + chatMessage.getToUserName());
            }
        }
    }

    /**
     * @return 返回true表示这条漫游消息需要保存处理，false就无视该消息，
     */
    public boolean roamingMessageFilter(int type) {
        if (type == XmppMessage.NEW_MEMBER) {
            // 907消息还是处里一下吧
            return true;
        }
        return type < 100
                // 拉漫游的红包领取消息不处理，
                && type != XmppMessage.TYPE_83
                // 拉漫游的清空双向聊天记录也不处理
                && type != XmppMessage.TYPE_SYNC_CLEAN_CHAT_HISTORY;
    }

    /**
     * 用于导出聊天记录，
     * 为了避免读取数据过多导致占用过大内存，使用iterable而不是list,
     */
    public void exportChatHistory(
            String ownerId, String friendId,
            AsyncUtils.Function<Iterator<ChatMessage>> callback
    ) throws Exception {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        Objects.requireNonNull(dao);
        CloseableIterator<ChatMessage> results = dao.iterator(dao.queryBuilder()
                .where().ne("type", XmppMessage.TYPE_READ)
                .and().ne("isExpired", 1)
                .and().le("deleteTime", 0).or().gt("deleteTime", TimeUtils.sk_time_current_time())
                .prepare());
        callback.apply(results);
        results.close();
    }

    /**
     * 1.对接收到的单、群聊消息进行解密
     * 2.对漫游消息进行解密
     *
     * @param isGroup
     * @param chatMessage
     */
    public void decrypt(boolean isGroup, ChatMessage chatMessage) {
        String mLoginUserId = CoreManager.requireSelf(MyApplication.getContext()).getUserId();
        int isEncrypt = chatMessage.getIsEncrypt();
        if (!MyApplication.IS_SUPPORT_SECURE_CHAT
                && (isEncrypt == 2 || isEncrypt == 3)) {
            // SecureFlag/SecureFlagGroup 非端到端，兼容之前在端到端版本注册过的账号，收到aes以及端到端消息时不解密，直接提示
            chatMessage.setType(XmppMessage.TYPE_TIP);
            chatMessage.setIsEncrypt(0);
            if (isEncrypt == 2) {
                chatMessage.setContent(MyApplication.getContext().getString(R.string.not_show_aes_msg));
            } else {
                chatMessage.setContent(MyApplication.getContext().getString(R.string.not_show_dh_msg));
            }
            return;
        }
        if (isEncrypt == 1) {// 3des
            String key = SecureChatUtil.getSymmetricKey(chatMessage.getTimeSend(), chatMessage.getPacketId());
            try {
                chatMessage.setContent(DES.decryptDES(chatMessage.getContent(), key));// 为chatMessage重新设值
                chatMessage.setIsEncrypt(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isEncrypt == 2) {// aes
            String key = SecureChatUtil.getSymmetricKey(chatMessage.getPacketId());
            try {
                chatMessage.setContent(AES.decryptStringFromBase64(chatMessage.getContent(), Base64.decode(key)));
                chatMessage.setIsEncrypt(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isEncrypt == 3) {// aes & dh
            if (isGroup) {
                Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), chatMessage.getToUserId());
                if (friend != null && friend.getIsLostChatKeyGroup() == 1) {
                    chatMessage.setVerifySignatureFailed(true);
                    Log.e("msg", "msg dao decrypt isEncrypt==3 isLostChatKeyGroup is true, waiting...");
                    return;
                }
                if (friend != null) {
                    String key = SecureChatUtil.decryptChatKey(chatMessage.getToUserId(), friend.getChatKeyGroup());
                    String realKey = SecureChatUtil.getSingleSymmetricKey(chatMessage.getPacketId(), key);
                    // 本地根据签名规则生成签名
                    String signature = SecureChatUtil.getSignatureMulti(chatMessage.getFromUserId(), chatMessage.getToUserId(),
                            chatMessage.getIsEncrypt(), chatMessage.getPacketId(), realKey,
                            chatMessage.getContent());
                    // 对比消息签名
                    if (TextUtils.isEmpty(chatMessage.getSignature())) {
                        // todo 签名为空，理论上不太可能，先不处理，算是一个漏斗
                        Log.e("msg", "msg dao decrypt isEncrypt==3 signature == null [group]");
                    } else {
                        if (TextUtils.equals(chatMessage.getSignature(), signature)) {
                            // 验签成功，消息解密存入数据库
                            Log.e("msg", "msg dao decrypt isEncrypt==3  验签成功，消息解密存入数据库  [group]");
                            chatMessage.setContent(AES.decryptStringFromBase64(chatMessage.getContent(), Base64.decode(realKey)));
                            chatMessage.setIsEncrypt(0);
                        } else {
                            // 验签失败，将消息标记为验签失败，放入验签失败的消息队列
                            Log.e("msg", "msg dao decrypt isEncrypt==3  验签失败，将消息标记为验签失败 [group]");
                            chatMessage.setVerifySignatureFailed(true);
                            HandleSecureChatMessage.handleVerifySignatureFailedMsgGroup(chatMessage);
                        }
                    }
                } else {
                    Log.e("msg", "msg dao decrypt isEncrypt==3  friend == null ，将消息标记为验签失败 [group]");
                    chatMessage.setVerifySignatureFailed(true);
                }
            } else {
                Friend friend;
                if (TextUtils.equals(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), chatMessage.getFromUserId())) {
                    friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), chatMessage.getToUserId());
                } else {
                    friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), chatMessage.getFromUserId());
                }
                if (friend != null) {
                    try {
                        String key = DH.getCommonSecretKeyBase64(SecureChatUtil.getDHPrivateKey(mLoginUserId), friend.getPublicKeyDH());
                        String realKey = SecureChatUtil.getSingleSymmetricKey(chatMessage.getPacketId(), key);
                        // 本地根据签名规则生成签名
                        String signature = SecureChatUtil.getSignatureSingle(chatMessage.getFromUserId(), chatMessage.getToUserId(),
                                chatMessage.getIsEncrypt(), chatMessage.getPacketId(), realKey,
                                chatMessage.getContent());
                        // 对比消息签名
                        if (TextUtils.isEmpty(chatMessage.getSignature())) {
                            Log.e("msg", "msg dao decrypt isEncrypt==3 signature == null");
                        } else {
                            if (TextUtils.equals(chatMessage.getSignature(), signature)) {
                                // 验签成功，消息解密存入数据库
                                Log.e("msg", "msg dao decrypt isEncrypt==3  验签成功，消息解密存入数据库");
                                chatMessage.setContent(AES.decryptStringFromBase64(chatMessage.getContent(), Base64.decode(realKey)));
                                chatMessage.setIsEncrypt(0);
                            } else {
                                // 验签失败，将消息标记为验签失败，放入验签失败的消息队列
                                Log.e("msg", "msg dao decrypt isEncrypt==3  验签失败，将消息标记为验签失败，放入缓冲队列");
                                chatMessage.setVerifySignatureFailed(true);
                                HandleSecureChatMessage.handleVerifySignatureFailedMsg(chatMessage);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // todo dh失败，先不处理
                        Log.e("msg", "msg dao decrypt isEncrypt==3 dh exception，将消息标记为验签失败");
                        chatMessage.setVerifySignatureFailed(true);
                    }
                } else {
                    Log.e("msg", "msg dao decrypt isEncrypt==3  friend == null ，将消息标记为验签失败");
                    chatMessage.setVerifySignatureFailed(true);
                }
            }
        }
    }

    /**
     * 对getLast接口获取到的最后一条消息进行解密
     */
    public String decrypt(boolean isGroup, String userId, String content, int isEncrypt, String messageId, long timeSend) {
        String mLoginUserId = CoreManager.requireSelf(MyApplication.getContext()).getUserId();
        if (isEncrypt == 1) {// 3des
            String key = SecureChatUtil.getSymmetricKey(timeSend, messageId);
            try {
                content = DES.decryptDES(content, key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isEncrypt == 2) {
            String key = SecureChatUtil.getSymmetricKey(messageId);
            content = AES.decryptStringFromBase64(content, Base64.decode(key));
        } else if (isEncrypt == 3) {
            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, userId);
            if (friend != null) {
                if (isGroup) {
                    try {
                        String key = SecureChatUtil.decryptChatKey(userId, friend.getChatKeyGroup());
                        String realKey = SecureChatUtil.getSingleSymmetricKey(messageId, key);
                        content = AES.decryptStringFromBase64(content, Base64.decode(realKey));
                    } catch (Exception e) {
                        Log.e("msg", "msg dao decrypt isEncrypt==3 exception [group]");
                    }
                } else {
                    try {
                        String key = DH.getCommonSecretKeyBase64(SecureChatUtil.getDHPrivateKey(mLoginUserId), friend.getPublicKeyDH());
                        String realKey = SecureChatUtil.getSingleSymmetricKey(messageId, key);
                        content = AES.decryptStringFromBase64(content, Base64.decode(realKey));
                    } catch (Exception e) {
                        Log.e("msg", "msg dao decrypt isEncrypt==3 exception");
                    }
                }
            } else {
                Log.e("msg", "friend == null  don't handler");
            }
        }
        return content;
    }

    /**
     * 供updateIsLostChatKeyGroup方法调用
     */
    public void decrypt(String friendId, ChatMessage chatMessage) {
        Friend friend = FriendDao.getInstance().getFriend(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), friendId);
        if (friend != null) {
            String key = SecureChatUtil.decryptChatKey(chatMessage.getToUserId(), friend.getChatKeyGroup());
            String realKey = SecureChatUtil.getSingleSymmetricKey(chatMessage.getPacketId(), key);
            // 本地根据签名规则生成签名
            String signature = SecureChatUtil.getSignatureMulti(chatMessage.getFromUserId(), chatMessage.getToUserId(),
                    chatMessage.getIsEncrypt(), chatMessage.getPacketId(), realKey,
                    chatMessage.getContent());
            if (TextUtils.equals(chatMessage.getSignature(), signature)) {
                chatMessage.setContent(AES.decryptStringFromBase64(chatMessage.getContent(), Base64.decode(realKey)));
                chatMessage.setIsEncrypt(0);
                String saveContent = AES.encryptBase64(chatMessage.getContent(), Base64.decode(SecureChatUtil.getSymmetricKey(chatMessage.getPacketId())));
                ChatMessageDao.getInstance().updateVerifySignatureFailedMsg(CoreManager.requireSelf(MyApplication.getContext()).getUserId(), friendId,
                        chatMessage.getPacketId(), saveContent);
            }
        }
    }

    /**
     * 更新验签失败的消息
     */
    public void updateVerifySignatureFailedMsg(String ownerId, String friendId, String packetId, String content) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }

        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("content", content);
            builder.updateColumnValue("isEncrypt", 0);
            builder.updateColumnValue("isVerifySignatureFailed", false);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            Log.e(TAG, "updateVerifySignatureFailedMsg exception");
            e.printStackTrace();
        }
    }

    /**
     * 获取与friendId下验签失败的消息
     */
    public List<ChatMessage> queryVerifySignatureFailedMsg(String ownerId, String friendId) {
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        List<ChatMessage> messages = new ArrayList<>();
        if (dao == null) {
            return messages;
        }
        QueryBuilder<ChatMessage, Integer> builder = dao.queryBuilder();
        try {
            builder.where().eq("isVerifySignatureFailed", true);
            messages = dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * 因为数据库内的消息保存的都是密文，所以取出的时候需要先解密
     * todo 因为content都是密文，如出现sql直接eq content等情况，基本上都会有问题的，目前发现content参与sql eq 或其它有影响操作如下
     * todo checkRepeatRead | queryFriendsByReadList (群已读相关，处理方法为已读消息存入本地不加密)
     * todo queryChatMessageByContent(查询聊天记录，目前端到端加密传输消息处理不了)
     */
    public ChatMessage decryptSqLiteMessage(ChatMessage chatMessage) {
        if (chatMessage == null || TextUtils.isEmpty(chatMessage.getContent())) {
            return chatMessage;
        }
        String key = SecureChatUtil.getSymmetricKey(chatMessage.getPacketId());
        try {
            chatMessage.setContent(AES.decryptStringFromBase64(chatMessage.getContent(), Base64.decode(key)));
        } catch (Exception e) {
            // 非端到端加密传输的消息均为明文存储，兼容
            Log.e(TAG, "decryptSqLiteMessage Exception--->" + chatMessage.getContent());
        }
        return chatMessage;
    }

    /**
     * 仅供设置signature成功之后调用
     * 因为本地数据库需要对端到端加密传输的消息content进行加密，而针对自己端在调用saveNewSingleChatMessage方法的时候
     * 因为该方法内调用了cloneAll方法并且当前消息还未设置signature，导致自己发送的端到端消息存到本地数据库时还未加密，
     * 这里封装一个方法，在设置signature成功之后更新content与signature
     */
    public void encrypt(String ownerId, String friendId, String packetId, String signature) {
        ChatMessage message = findMsgById(ownerId, friendId, packetId);
        Dao<ChatMessage, Integer> dao = getDao(ownerId, friendId);
        if (dao == null) {
            return;
        }

        String key = SecureChatUtil.getSymmetricKey(message.getPacketId());
        message.setContent(AES.encryptBase64(message.getContent(), Base64.decode(key)));

        UpdateBuilder<ChatMessage, Integer> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue("content", message.getContent());
            builder.updateColumnValue("signature", signature);
            builder.where().eq("packetId", packetId);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            Log.e(TAG, "encrypt exception");
            e.printStackTrace();
        }
    }
}
