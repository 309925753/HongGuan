package com.sk.weichat.db.dao;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.sk.weichat.MyApplication;
import com.sk.weichat.bean.UploadingFile;
import com.sk.weichat.db.SQLiteHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * 正在上传的文件的Dao
 */
public class UploadingFileDao {
    private static UploadingFileDao instance = null;

    public static UploadingFileDao getInstance() {
        if (instance == null) {
            synchronized (UploadingFileDao.class) {
                if (instance == null) {
                    instance = new UploadingFileDao();
                }
            }
        }
        return instance;
    }

    public Dao<UploadingFile, Integer> UploadingFileDao;

    private UploadingFileDao() {
        try {
            UploadingFileDao = DaoManager.createDao(OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class).getConnectionSource(),
                    UploadingFile.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        OpenHelperManager.releaseHelper();
    }

    // 创建单条
    public void createUploadingFile(UploadingFile UploadingFile) {
        try {
            UploadingFileDao.create(UploadingFile);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 删除单条
    public void deleteUploadingFile(String userId, String msgId) {
        try {
            DeleteBuilder<UploadingFile, Integer> builder = UploadingFileDao.deleteBuilder();
            builder.where().eq("userId", userId).and().eq("msgId", msgId);
            UploadingFileDao.delete(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 删除全部
    public void deleteAllUploadingFiles(String userId) {
        try {
            DeleteBuilder<UploadingFile, Integer> builder = UploadingFileDao.deleteBuilder();
            builder.where().eq("userId", userId);
            UploadingFileDao.delete(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 获取全部
    public List<UploadingFile> getAllUploadingFiles(String userId) {
        List<UploadingFile> query = new ArrayList<>();
        try {
            PreparedQuery<UploadingFile> preparedQuery = UploadingFileDao.queryBuilder().where()
                    .eq("userId", userId)
                    .prepare();

            query = UploadingFileDao.query(preparedQuery);
            return query;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return query;
    }
}