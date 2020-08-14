package com.sk.weichat.db.dao;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sk.weichat.MyApplication;
import com.sk.weichat.bean.VideoFile;
import com.sk.weichat.db.SQLiteHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * 访问VideoFile的Dao，实际上是一个工具类
 */
public class VideoFileDao {
    private static VideoFileDao instance = null;

    public static VideoFileDao getInstance() {
        if (instance == null) {
            synchronized (VideoFileDao.class) {
                if (instance == null) {
                    instance = new VideoFileDao();
                }
            }
        }
        return instance;
    }

    public Dao<VideoFile, Integer> dao;

    private VideoFileDao() {
        try {
            dao = DaoManager
                    .createDao(OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class).getConnectionSource(), VideoFile.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        OpenHelperManager.releaseHelper();
    }

    public boolean addVideoFile(VideoFile videoFile) {
        try {
            dao.create(videoFile);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteVideoFile(VideoFile videoFile) {
        try {
            dao.delete(videoFile);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<VideoFile> getVideoFiles(String ownerId) {
        try {
            QueryBuilder<VideoFile, Integer> builder = dao.queryBuilder();
            builder.where().eq("ownerId", ownerId);
            builder.orderBy("_id", false);
            return dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }
}
