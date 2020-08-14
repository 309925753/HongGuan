package com.sk.weichat.bean;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.SparseIntArray;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sk.weichat.db.dao.TableConstantDao;
import com.sk.weichat.ui.tool.SelectConstantActivity;
import com.sk.weichat.ui.tool.SelectConstantSectionActivity;

@DatabaseTable(tableName = "tb_constants")
public class TableConstant {
    /* 某些关系节点的 ID */
    public static final int NOTE_EDUCATION = 1;      // 学历（1）
    public static final int NOTE_WORK_EXPERIENCE = 2;// 工作经验（1）
    public static final int NOTE_SALARY = 3;         // 薪水（1）
    public static final int NOTE_COMPANY_NATURE = 32;// 公司性质（1）
    public static final int NOTE_COMPANY_SCALE = 44; // 公司规模（1）
    public static final int NOTE_GANG_TIME = 52;// 到岗时间（1）
    public static final int NOTE_JOB_TYPE = 59; // 工作类型（1）
    public static final int NOTE_INDUSTRY = 63;// 行业（2）
    public static final int NOTE_FUNCTION = 64;// 职能（3）
    public static final int NOTE_MAJOR = 1005; // 专业（3）

    /**
     * 存放节点深度的值。
     * <p>
     * 1、代表该节点有1级的子节点<br/>
     * 2、代表该节点有2级子节点<br/>
     * 3、代表该节点有3级子节点<br/>
     * 当节点深度>=2，那么就进入SelectConstantSectionActivity，否则进入SelectConstantActivity
     */
    public static SparseIntArray sTopNotes;

    static {
        sTopNotes = new SparseIntArray();
        sTopNotes.put(NOTE_EDUCATION, 1);
        sTopNotes.put(NOTE_WORK_EXPERIENCE, 1);
        sTopNotes.put(NOTE_SALARY, 1);
        sTopNotes.put(NOTE_COMPANY_NATURE, 1);
        sTopNotes.put(NOTE_COMPANY_SCALE, 1);
        sTopNotes.put(NOTE_GANG_TIME, 1);
        sTopNotes.put(NOTE_JOB_TYPE, 1);
        sTopNotes.put(NOTE_INDUSTRY, 2);
        sTopNotes.put(NOTE_FUNCTION, 3);
        sTopNotes.put(NOTE_MAJOR, 3);
    }

    public static void select(Context context, int noteId, String title, int requestCode) {
        TableConstant constant = TableConstantDao.getInstance().getConstant(noteId);
        if (constant == null || constant.more == 0) {// 为null或者没有下级节点，那么return
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(SelectConstantActivity.EXTRA_CONSTANT_ID, noteId);
        if (!TextUtils.isEmpty(title)) {
            intent.putExtra(SelectConstantActivity.EXTRA_CONSTANT_TITLE, title);
        }
        int deep = sTopNotes.get(noteId);
        if (deep < 2) {// 深度小于2
            intent.setClass(context, SelectConstantActivity.class);
        } else {
            intent.setClass(context, SelectConstantSectionActivity.class);
        }
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, requestCode);
        }
    }

    @DatabaseField(id = true)
    private int id;

    @DatabaseField(columnName = "parent_id")
    private int parentId;

    @DatabaseField
    private String name;

    @DatabaseField
    private int more;// 0 表示没有，1表示有

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMore() {
        return more;
    }

    public void setMore(int more) {
        this.more = more;
    }
}
