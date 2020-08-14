package com.sk.weichat.db.dao;

import android.text.TextUtils;

import com.sk.weichat.bean.Label;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.sk.weichat.MyApplication;
import com.sk.weichat.db.SQLiteHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * 访问标签的Dao
 */
public class LabelDao {
    private static LabelDao instance = null;

    public static LabelDao getInstance() {
        if (instance == null) {
            synchronized (LabelDao.class) {
                if (instance == null) {
                    instance = new LabelDao();
                }
            }
        }
        return instance;
    }

    public Dao<Label, Integer> labelDao;

    private LabelDao() {
        try {
            labelDao = DaoManager.createDao(OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class).getConnectionSource(),
                    Label.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        OpenHelperManager.releaseHelper();
    }

    // 创建标签
    public void createLabel(Label label) {
        try {
            labelDao.createOrUpdate(label);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 删除标签
    public void deleteLabel(String ownerId, String groupId) {
        try {
            DeleteBuilder<Label, Integer> builder = labelDao.deleteBuilder();
            builder.where().eq("userId", ownerId).and().eq("groupId", groupId);
            labelDao.delete(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 修改标签名称
    public void updateLabelName(String ownerId, String groupId, String groupName) {
        UpdateBuilder<Label, Integer> builder = labelDao.updateBuilder();
        try {
            builder.updateColumnValue("groupName", groupName);
            builder.where().eq("userId", ownerId).and().eq("groupId", groupId);
            labelDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 修改标签下的用户数量
    public void updateLabelUserIdList(String ownerId, String groupId, String userIdList) {
        UpdateBuilder<Label, Integer> builder = labelDao.updateBuilder();
        try {
            builder.updateColumnValue("userIdList", userIdList);
            builder.where().eq("userId", ownerId).and().eq("groupId", groupId);
            labelDao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 获取单个标签
    public Label getLabel(String ownerId, String groupId) {
        try {
            PreparedQuery<Label> preparedQuery = labelDao.queryBuilder().where().eq("userId", ownerId)
                    .and().eq("groupId", groupId).prepare();
            return labelDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取某个好友的所属标签
    public List<Label> getFriendLabelList(String ownerId, String friendId) {
        List<Label> labelList = new ArrayList<>();
        List<Label> allLabels = getAllLabels(ownerId);
        for (int i = 0; i < allLabels.size(); i++) {
            Label label = allLabels.get(i);
            if (!TextUtils.isEmpty(label.getUserIdList())) {
                if (label.getUserIdList().contains(friendId)) {
                    labelList.add(label);
                }
            }
        }
        return labelList;
    }

    // 获取ownerId用户的全部标签
    public List<Label> getAllLabels(String ownerId) {
        try {
            PreparedQuery<Label> preparedQuery = labelDao.queryBuilder().where()
                    .eq("userId", ownerId)
                    .prepare();

            return labelDao.query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void refreshLabel(String ownerId, List<Label> labelListFromService) {
        // 1.
        List<Label> labelListFromLocal = LabelDao.getInstance().getAllLabels(ownerId);
        for (Label label : labelListFromLocal) {
            LabelDao.getInstance().deleteLabel(ownerId, label.getGroupId());
        }
        // 2.
        for (Label label : labelListFromService) {
            LabelDao.getInstance().createLabel(label);
        }
    }
}