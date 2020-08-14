package com.sk.weichat.ui.mucfile;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author liuxuan
 * @time 2017-7-7 14:39:11
 * @des 创建数据库 下载表
 */
public class FileDownDBHelper extends SQLiteOpenHelper {
    /**
     * 数据库名
     **/
    private static final String DB_NAME = "dowload.db";
    /**
     * 数据库版本
     **/
    private static final int VERSION = 1;
    /**
     * 创建数据表
     **/
    private static final String SQL_CREATE = "create table tb_down(_id INTEGER primary key autoincrement," +
            "url text," +
            "name text," +
            "start INTEGER," +
            "end INTEGER," +
            "state INTEGER)";
    /**
     * 删除数据表
     **/
    private static final String SQL_DROP = "drop table if exists thread_info";
    private static FileDownDBHelper mDbHelper = null;

    private FileDownDBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public static FileDownDBHelper instance(Context context) {
        if (null == mDbHelper) {
            synchronized (FileDownDBHelper.class) {
                if (null == mDbHelper) {
                    mDbHelper = new FileDownDBHelper(context);
                }
            }
        }
        return mDbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE); //创建表
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //先删除，再创建
        db.execSQL(SQL_DROP);
        db.execSQL(SQL_CREATE);
    }
}
