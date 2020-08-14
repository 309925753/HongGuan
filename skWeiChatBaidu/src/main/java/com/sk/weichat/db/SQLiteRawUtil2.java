package com.sk.weichat.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 对原生SQL语句的一些支持
 *
 * @author zq
 */
public class SQLiteRawUtil2 {
    public static final String MEMBER_TABLE_PREFIX = "member_";

    // 一个实体类创建多张表格,不能使用TableUtils创建表格,需使用原生sql语句
    public static String getCreateRoomMemberTableSql(String tableName) {
        String sql = "CREATE TABLE IF NOT EXISTS "
                + tableName
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,roomId VARCHAR,userId VARCHAR,userName VARCHAR,cardName VARCHAR,role INTEGER,createTime INTEGER)";
        return sql;
    }

    // 创建表格
    public static void createTableIfNotExist(SQLiteDatabase db, String tableName, String createTableSql) {
        if (isTableExist(db, tableName)) {
            return;
        }
        db.execSQL(createTableSql);
    }

    // 表格是否存在
    public static boolean isTableExist(SQLiteDatabase db, String tableName) {
        boolean result = false;
        if (TextUtils.isEmpty(tableName.trim())) {
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='" + tableName.trim() + "' ";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }


    // 清除指定的表格
    public static void dropTable(SQLiteDatabase db, String tableName) {
        String sql = "drop table " + tableName;
        db.execSQL(sql);
    }

    /**
     * 获取当前用户的群组表
     *
     * @param db
     * @param roomId
     * @return
     */
    public static List<String> getUserRoomMemberTables(SQLiteDatabase db, String roomId) {
        String tablePrefix = MEMBER_TABLE_PREFIX + roomId;
        Cursor cursor = null;
        try {
            String sql = "select name from Sqlite_master where type ='table' and name like '" + tablePrefix + "%'";
            cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                List<String> tables = new ArrayList<String>();
                while (cursor.moveToNext()) {
                    String name = cursor.getString(0);
                    if (!TextUtils.isEmpty(name)) {
                        tables.add(name);
                    }
                }
                return tables;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
