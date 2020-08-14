package com.sk.weichat.ui.mucfile;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sk.weichat.MyApplication;
import com.sk.weichat.ui.mucfile.bean.DownBean;

import java.util.concurrent.atomic.AtomicInteger;

import static com.sk.weichat.ui.mucfile.DownManager.STATE_UNDOWNLOAD;

/**
 * @author liuxuan
 * @time 2017-7-7 14:39:11
 * @des 数据库操作类，下载dao
 */

public class DownDao {

    private AtomicInteger mOpenCounter = new AtomicInteger();
    private SQLiteDatabase mDatabase;

    private DownDao() {
        mHelper = FileDownDBHelper.instance(MyApplication.getContext());
    }

    private volatile static DownDao instance;
    private FileDownDBHelper mHelper;

    public static DownDao instance() {
        if (instance == null) {
            synchronized (DownManager.class) {
                if (instance == null) {
                    instance = new DownDao();
                }
            }
        }
        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = mHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            mDatabase.close();
        }
    }

    public boolean insert(DownBean info) {
        SQLiteDatabase db = openDatabase();
        try {
            db.execSQL("insert into tb_down(url,name,start,end,state) values(?,?,?,?,?)",
                    new Object[]{info.url, info.name, 0, info.max, STATE_UNDOWNLOAD});
        } catch (Exception e) {
            return false;
        } finally {
            closeDatabase();
        }
        return true;
    }


    public DownBean query(String url) {
        SQLiteDatabase db = openDatabase();
        DownBean result = null;
        Cursor cursor = db.rawQuery("select * from tb_down where url = ?", new String[]{url});
        if (null != cursor) {
            if (cursor.moveToNext()) {
                result = new DownBean();
                result.url = (cursor.getString(cursor.getColumnIndex("url")));
                result.cur = (cursor.getLong(cursor.getColumnIndex("start")));
                result.max = (cursor.getLong(cursor.getColumnIndex("end")));
                result.state = (cursor.getInt(cursor.getColumnIndex("state")));
                result.name = (cursor.getString(cursor.getColumnIndex("name")));
            }
            cursor.close();
        }
        closeDatabase();
        return result;
    }

    public boolean update(DownBean bean) {
        SQLiteDatabase db = openDatabase();
        try {
            db.execSQL("update tb_down set start = ?,end = ?,state = ? where url = ?",
                    new Object[]{bean.cur, bean.max, bean.state, bean.url});
        } catch (Exception e) {
            return false;
        } finally {
            closeDatabase();
        }
        return true;
    }

    public boolean delete(String fileUrl) {
        if (isExists(fileUrl)) {
            SQLiteDatabase db = openDatabase();
            try {
                db.execSQL("delete from tb_down where url = ?",
                        new Object[]{fileUrl});
            } catch (Exception e) {
                return false;
            } finally {
                closeDatabase();
            }
        }
        return true;
    }

    public boolean isExists(String fileUrl) {
        SQLiteDatabase db = openDatabase();
        Cursor cursor = db.rawQuery("select * from tb_down where url = ?",
                new String[]{fileUrl});
        boolean exists = false;
        if (null != cursor) {
            exists = cursor.moveToNext();
        }
        cursor.close();

        closeDatabase();
        return exists;
    }
}
