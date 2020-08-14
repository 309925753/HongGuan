package com.sk.weichat.db.dao;

import android.os.Handler;
import android.text.TextUtils;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sk.weichat.MyApplication;
import com.sk.weichat.bean.circle.CircleMessage;
import com.sk.weichat.db.SQLiteHelper;
import com.sk.weichat.util.ThreadManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 访问用户数据的Dao，包括访问两个实体，User和UserDetail
 */
public class CircleMessageDao {
    private static CircleMessageDao instance = null;

    public static CircleMessageDao getInstance() {
        if (instance == null) {
            synchronized (CircleMessageDao.class) {
                if (instance == null) {
                    instance = new CircleMessageDao();
                }
            }
        }
        return instance;
    }

    public Dao<CircleMessage, String> circleMsgDao;

    private CircleMessageDao() {
        try {
            OrmLiteSqliteOpenHelper helper = OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class);
            circleMsgDao = DaoManager.createDao(helper.getConnectionSource(), CircleMessage.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        OpenHelperManager.releaseHelper();
    }

    /**
     * 重新登陆时候，下载自己的商务圈数据后，用此方法添加<br/>
     * 添加数据,注意此方法中的messages 里面UserId必须不为空
     */
    public void addMessages(final Handler handler, final String ownerId, final List<CircleMessage> messages, final OnCompleteListener listener) {
        ThreadManager.getPool().execute(new Runnable() {
            @Override
            public void run() {
                // 先确保无重复，删除
                DeleteBuilder<CircleMessage, String> builder = circleMsgDao.deleteBuilder();
                try {
                    builder.where().eq("ownerId", ownerId);
                    circleMsgDao.delete(builder.prepare());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (messages != null && messages.size() > 0) {
                    for (int i = 0; i < messages.size(); i++) {
                        String messageId = messages.get(i).getMsgId();
                        // 消息完整性填充
                        messages.get(i).setOwnerId(ownerId);
                        long time = 0;
                        if (!TextUtils.isEmpty(messageId) && messageId.length() >= 8) {
                            time = Integer.parseInt(messageId.substring(0, 2), 16) << 24 | Integer.parseInt(messageId.substring(2, 4), 16) << 16
                                    | Integer.parseInt(messageId.substring(4, 6), 16) << 8 | Integer.parseInt(messageId.substring(6, 8), 16);
                        }
                        messages.get(i).setTime(time);
                        try {
                            circleMsgDao.create(messages.get(i));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (handler != null && listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onCompleted();
                        }
                    });
                }
            }
        });
    }

    /**
     * 添加某一个关注级别用户的商务圈数据
     */
    public void addFriendMessages(final String ownerId, final String friendId, final List<CircleMessage> messages) {
        ThreadManager.getPool().execute(new Runnable() {
            @Override
            public void run() {
                // 先确保无重复，删除
                DeleteBuilder<CircleMessage, String> builder = circleMsgDao.deleteBuilder();
                try {
                    builder.where().eq("ownerId", ownerId).and().eq("userId", friendId);
                    circleMsgDao.delete(builder.prepare());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (messages != null && messages.size() > 0) {
                    for (int i = 0; i < messages.size(); i++) {
                        String messageId = messages.get(i).getMsgId();
                        // 消息完整性填充
                        messages.get(i).setOwnerId(ownerId);
                        long time = 0;
                        if (!TextUtils.isEmpty(messageId) && messageId.length() >= 8) {
                            time = Integer.parseInt(messageId.substring(0, 2), 16) << 24 | Integer.parseInt(messageId.substring(2, 4), 16) << 16
                                    | Integer.parseInt(messageId.substring(4, 6), 16) << 8 | Integer.parseInt(messageId.substring(6, 8), 16);
                        }
                        messages.get(i).setTime(time);

                        try {
                            circleMsgDao.create(messages.get(i));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /**
     * 添加一条刚刚发布的消息
     *
     * @param ownerId
     * @param messageId
     */
    public void addMessage(final String ownerId, String messageId) {
        CircleMessage message = new CircleMessage();
        message.setOwnerId(ownerId);
        message.setMsgId(messageId);
        message.setUserId(ownerId);
        message.setTime(System.currentTimeMillis());
        try {
            circleMsgDao.create(message);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getCircleMessageIds(String ownerId, int pageIndex, int pageSize) {
        List<String> msgIds = new ArrayList<String>();
        try {
            PreparedQuery<CircleMessage> preparedQuery = circleMsgDao.queryBuilder().selectColumns("msgId").orderBy("time", false)
                    .limit((long) pageSize).offset((long) pageSize * pageIndex).where().eq("ownerId", ownerId).prepare();
            List<CircleMessage> circleMessages = circleMsgDao.query(preparedQuery);
            if (circleMessages != null && circleMessages.size() > 0) {
                for (int i = 0; i < circleMessages.size(); i++) {
                    msgIds.add(circleMessages.get(i).getMsgId());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return msgIds;
    }

    public int getCircleMessageCount(String ownerId, String firendId) {
        try {
            QueryBuilder<CircleMessage, String> builder = circleMsgDao.queryBuilder();
            builder.setCountOf(true);
            builder.where().eq("ownerId", ownerId).and().eq("userId", firendId);
            GenericRawResults<String[]> results = circleMsgDao.queryRaw(builder.prepareStatementString());
            if (results != null) {
                String[] first = results.getFirstResult();
                if (first != null && first.length > 0) {
                    return Integer.parseInt(first[0]);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void deleteMessage(final String ownerId, final String friendId) {
        ThreadManager.getPool().execute(new Runnable() {
            @Override
            public void run() {
                DeleteBuilder<CircleMessage, String> builder = circleMsgDao.deleteBuilder();
                try {
                    builder.where().eq("ownerId", ownerId).and().eq("userId", friendId);
                    circleMsgDao.delete(builder.prepare());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void deleteMessage(final String messageId) {
        DeleteBuilder<CircleMessage, String> builder = circleMsgDao.deleteBuilder();
        try {
            builder.where().eq("msgId", messageId);
            circleMsgDao.delete(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
