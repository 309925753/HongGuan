package com.sk.weichat.db.dao;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sk.weichat.MyApplication;
import com.sk.weichat.bean.MyZan;
import com.sk.weichat.db.SQLiteHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * 发现
 */
public class MyZanDao {
    private static MyZanDao instance = null;
    public Dao<MyZan, String> dao;

    private MyZanDao() {
        try {
            dao = DaoManager.createDao(OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class).getConnectionSource(),
                    MyZan.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static MyZanDao getInstance() {
        if (instance == null) {
            synchronized (MyZanDao.class) {
                if (instance == null) {
                    instance = new MyZanDao();
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
     * 判断表内是否有同样的消息
     */
    public boolean hasSameZan(String packetId) {
        boolean exist;

        QueryBuilder<MyZan, String> builder = dao.queryBuilder();
        List<MyZan> myZans = null;
        try {
            builder.where().eq("systemid", packetId);
            myZans = dao.query(builder.prepare());
            if (myZans != null && myZans.size() > 0) {
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

    public boolean addZan(MyZan zan) {
        try {
            if (zan.getHuifu().equals("101")) {// 点赞需要判断
                if (!isLikedThisDynamic(zan.getLoginUserId(), zan)) {
                    dao.create(zan);
                    return true;
                } else {
                    return false;
                }
            } else {// 评论直接创建
                dao.create(zan);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void deleteZan(MyZan zan) {
        try {
            dao.delete(zan);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteZan(String loginUserId, String fromUserId, String cricleuserid) {
        try {
            DeleteBuilder<MyZan, String> builder = dao.deleteBuilder();
            builder.where().eq("loginUserId", loginUserId)
                    .and().eq("fromUserId", fromUserId)
                    .and().eq("cricleuserid", cricleuserid);
            builder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteZanOlnyPraise(String loginUserId, String fromUserId, String cricleuserid) {
        try {
            DeleteBuilder<MyZan, String> builder = dao.deleteBuilder();
            builder.where().eq("loginUserId", loginUserId)
                    .and().eq("fromUserId", fromUserId)
                    .and().eq("huifu", 101)
                    .and().eq("cricleuserid", cricleuserid);
            builder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void UpdataZan(MyZan zan) {
        try {
            dao.update(zan);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<MyZan> queryZan(String loginUserId) {
        List<MyZan> list = null;
        try {
            QueryBuilder<MyZan, String> builder = dao.queryBuilder();
            builder.where().eq("loginUserId", "" + loginUserId);
            list = dao.query(builder.prepare());
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getZanSize(String loginUserId) {
        try {
            return (int) dao.queryBuilder().where()
                    .eq("zanbooleanyidu", 0)
                    .and().eq("loginUserId", loginUserId)
                    .countOf();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isLikedThisDynamic(String loginUserId, MyZan zan) {
        boolean isLikedThisDynamic = false;// 是否赞过该条动态
        QueryBuilder<MyZan, String> builder = dao.queryBuilder();
        try {
            builder.where().eq("loginUserId", loginUserId)
                    .and().eq("fromUserId", zan.getFromUserId())
                    .and().eq("cricleuserid", zan.getCricleuserid())
                    .and().eq("huifu", 101);
            builder.orderBy("systemid", false);
            MyZan myZan = builder.queryForFirst();
            if (myZan != null) {// 赞过该条说说
                isLikedThisDynamic = true;
            } else {
                isLikedThisDynamic = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isLikedThisDynamic;

    }
}
