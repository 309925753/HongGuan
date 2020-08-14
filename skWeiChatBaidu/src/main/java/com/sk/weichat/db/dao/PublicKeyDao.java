package com.sk.weichat.db.dao;


import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sk.weichat.MyApplication;
import com.sk.weichat.bean.PublicKey;
import com.sk.weichat.db.SQLiteHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * 好友dh public list Dao
 */
public class PublicKeyDao {
    private static PublicKeyDao instance = null;
    public Dao<PublicKey, Integer> publicKeyDao;

    private PublicKeyDao() {
        try {
            publicKeyDao = DaoManager.createDao(OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class).getConnectionSource(),
                    PublicKey.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static PublicKeyDao getInstance() {
        if (instance == null) {
            synchronized (PublicKeyDao.class) {
                if (instance == null) {
                    instance = new PublicKeyDao();
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

    // 创建PublicKey
    public void createPublicKey(PublicKey publicKey) {
        try {
            publicKeyDao.create(publicKey);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 判断是否需要创建PublicKey
    public void updatePublicKey(String ownerId, String userId, PublicKey publicKey) {
        QueryBuilder<PublicKey, Integer> builder = publicKeyDao.queryBuilder();
        try {
            builder.where().eq("ownerId", ownerId).
                    and().eq("userId", userId).
                    and().eq("publicKey", publicKey.getPublicKey());
            List<PublicKey> query = publicKeyDao.query(builder.prepare());
            if (query == null || query.size() == 0) {
                createPublicKey(publicKey);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 删除ownerId下的userId的全部dh公钥
    public boolean deleteAllPublicKeys(String ownerId, String userId) {
        DeleteBuilder<PublicKey, Integer> builder = publicKeyDao.deleteBuilder();
        try {
            builder.where().eq("ownerId", ownerId).and().eq("userId", userId);
            publicKeyDao.delete(builder.prepare());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取ownerId下的userId的全部dh公钥
    public List<PublicKey> getAllPublicKeys(String ownerId, String userId) {
        QueryBuilder<PublicKey, Integer> builder = publicKeyDao.queryBuilder();
        try {
            builder.where().eq("ownerId", ownerId).and().eq("userId", userId);
            return publicKeyDao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    // 刷新ownerId下的userId的全部dh公钥
    public boolean refreshPublicKeys(String ownerId, String userId, List<PublicKey> publicKeysFromService) {
        boolean success = deleteAllPublicKeys(ownerId, userId);
        if (success) {
            for (int i = 0; i < publicKeysFromService.size(); i++) {
                createPublicKey(publicKeysFromService.get(i));
            }
            return true;
        }
        return false;
    }
}