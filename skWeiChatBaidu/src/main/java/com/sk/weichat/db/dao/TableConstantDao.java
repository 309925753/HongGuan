package com.sk.weichat.db.dao;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.sk.weichat.MyApplication;
import com.sk.weichat.bean.TableConstant;
import com.sk.weichat.db.SQLiteHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * 访问VideoFile的Dao，实际上是一个工具类
 */
public class TableConstantDao {
    private static TableConstantDao instance = null;

    public static TableConstantDao getInstance() {
        if (instance == null) {
            synchronized (TableConstantDao.class) {
                if (instance == null) {
                    instance = new TableConstantDao();
                }
            }
        }
        return instance;
    }

    public Dao<TableConstant, Integer> dao;

    private TableConstantDao() {
        try {
            dao = DaoManager.createDao(OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class).getConnectionSource(),
                    TableConstant.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        OpenHelperManager.releaseHelper();
    }

    public List<TableConstant> getSubConstants(int parentId) {
        try {
            return dao.queryForEq("parent_id", parentId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public TableConstant getConstant(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getConstantName(int id) {
        try {
            TableConstant constant = dao.queryForId(id);
            if (constant != null) {
                return constant.getName();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
