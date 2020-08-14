package com.sk.weichat.db.dao;

import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.sk.weichat.MyApplication;
import com.sk.weichat.bean.MsgRoamTask;
import com.sk.weichat.db.SQLiteHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * 漫游任务Dao
 */
public class MsgRoamTaskDao {
    private static final String TAG = "batchJoinRoom";
    private static MsgRoamTaskDao instance = null;

    public static MsgRoamTaskDao getInstance() {
        if (instance == null) {
            synchronized (MsgRoamTaskDao.class) {
                if (instance == null) {
                    instance = new MsgRoamTaskDao();
                }
            }
        }
        return instance;
    }

    public Dao<MsgRoamTask, Integer> MsgRoamTaskDao;

    private MsgRoamTaskDao() {
        try {
            MsgRoamTaskDao = DaoManager.createDao(OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class).getConnectionSource(),
                    MsgRoamTask.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        OpenHelperManager.releaseHelper();
    }

    // 创建任务
    public void createMsgRoamTask(MsgRoamTask MsgRoamTask) {
        Log.i(TAG, "createMsgRoamTask() called with: MsgRoamTask = [" + MsgRoamTask + "]");
        try {
            MsgRoamTaskDao.create(MsgRoamTask);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 删除某个任务
    public void deleteMsgRoamTask(String ownerId, String userId, long taskId) {
        Log.i(TAG, "deleteMsgRoamTask() called with: ownerId = [" + ownerId + "], userId = [" + userId + "], taskId = [" + taskId + "]");
        try {
            DeleteBuilder<MsgRoamTask, Integer> builder = MsgRoamTaskDao.deleteBuilder();
            builder.where().eq("ownerId", ownerId).and().eq("userId", userId).and().eq("taskId", taskId);
            MsgRoamTaskDao.delete(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 修改某个任务的endTime
    public boolean updateMsgRoamTaskEndTime(String ownerId, String userId, long taskId, long endTime) {
        Log.i(TAG, "updateMsgRoamTaskEndTime() called with: ownerId = [" + ownerId + "], userId = [" + userId + "], taskId = [" + taskId + "], endTime = [" + endTime + "]");
        UpdateBuilder<MsgRoamTask, Integer> builder = MsgRoamTaskDao.updateBuilder();
        try {
            builder.updateColumnValue("endTime", endTime);
            builder.where().eq("ownerId", ownerId).and().eq("userId", userId).and().eq("taskId", taskId);
            MsgRoamTaskDao.update(builder.prepare());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取ownerId用户的全部任务
    public List<MsgRoamTask> getAllMsgRoamTasks(String ownerId) {
        Log.i(TAG, "getAllMsgRoamTasks() called with: ownerId = [" + ownerId + "]");
        List<MsgRoamTask> query = new ArrayList<>();
        try {
            PreparedQuery<MsgRoamTask> preparedQuery = MsgRoamTaskDao.queryBuilder().where()
                    .eq("ownerId", ownerId)
                    .prepare();

            query = MsgRoamTaskDao.query(preparedQuery);
            return query;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return query;
    }

    // 获取单个群组的任务
    public List<MsgRoamTask> getFriendMsgRoamTaskList(String ownerId, String userId) {
        Log.i(TAG, "getFriendMsgRoamTaskList() called with: ownerId = [" + ownerId + "], userId = [" + userId + "]");
        List<MsgRoamTask> MsgRoamTaskList = new ArrayList<>();
        List<MsgRoamTask> allMsgRoamTasks = getAllMsgRoamTasks(ownerId);
        for (int i = 0; i < allMsgRoamTasks.size(); i++) {
            MsgRoamTask mMsgRoamTask = allMsgRoamTasks.get(i);
            if (mMsgRoamTask.getUserId().equals(userId)) {
                MsgRoamTaskList.add(mMsgRoamTask);
            }
        }
        return MsgRoamTaskList;
    }

    // 获取单个群组的最后一条任务
    public MsgRoamTask getFriendLastMsgRoamTask(String ownerId, String userId) {
        Log.i(TAG, "getFriendLastMsgRoamTask() called with: ownerId = [" + ownerId + "], userId = [" + userId + "]");
        List<MsgRoamTask> tasks = getFriendMsgRoamTaskList(ownerId, userId);
        for (int i = 0; i < tasks.size(); i++) {
            if (i == tasks.size() - 1) {
                return tasks.get(i);
            }
        }
        return null;
    }
}