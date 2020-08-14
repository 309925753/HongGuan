package com.sk.weichat.db.dao;

import android.text.TextUtils;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.Area;
import com.sk.weichat.db.SQLiteHelper;

import java.sql.SQLException;
import java.util.List;

public class AreasDao {
    private static AreasDao instance = null;

    public static AreasDao getInstance() {
        if (instance == null) {
            synchronized (AreasDao.class) {
                if (instance == null) {
                    instance = new AreasDao();
                }
            }
        }
        return instance;
    }

    public Dao<Area, Integer> dao;

    private AreasDao() {
        try {
            dao = DaoManager
                    .createDao(OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class).getConnectionSource(), Area.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        OpenHelperManager.releaseHelper();
    }

    public Area getArea(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mDefaultAreas;
    }

    public Area mDefaultAreas;

    {
        mDefaultAreas = new Area();
        mDefaultAreas.setId(0);
        mDefaultAreas.setName(MyApplication.getInstance().getString(R.string.unknown));
    }

    /**
     * 根据Type查询
     *
     * @param type
     * @return
     */
    public List<Area> getAreasByTypeAndParentId(int type, int id) {

        QueryBuilder<Area, Integer> builder = dao.queryBuilder();
        try {
            if (id <= 0) {
                builder.where().eq("type", type);
            } else {
                builder.where().eq("type", type).and().eq("parent_id", id);
            }
            return dao.query(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据Type查询
     *
     * @param id
     * @return
     */
    public boolean hasSubAreas(int id) {
        try {
            QueryBuilder<Area, Integer> builder = dao.queryBuilder();
            builder.setCountOf(true);
            builder.where().eq("parent_id", id);
            GenericRawResults<String[]> results = dao.queryRaw(builder.prepareStatementString());
            if (results != null) {
                String[] first = results.getFirstResult();
                if (first != null && first.length > 0) {
                    return Integer.parseInt(first[0]) > 0 ? true : false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Area searchByName(String likeName) {
        try {
            QueryBuilder<Area, Integer> builder = dao.queryBuilder();
            builder.where().like("name", likeName);
            return dao.queryForFirst(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Area searchByNameAndParentId(int parentId, String likeName) {
        try {
            QueryBuilder<Area, Integer> builder = dao.queryBuilder();
            if (TextUtils.isEmpty(likeName)) {
                builder.where().eq("parent_id", parentId);
            } else {
                builder.where().like("name", likeName).and().eq("parent_id", parentId);
            }
            Area area = dao.queryForFirst(builder.prepare());
            if (area == null && !TextUtils.isEmpty(likeName)) {
                QueryBuilder<Area, Integer> builder2 = dao.queryBuilder();
                builder2.where().eq("parent_id", parentId);
                area = dao.queryForFirst(builder.prepare());
            }
            return area;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
