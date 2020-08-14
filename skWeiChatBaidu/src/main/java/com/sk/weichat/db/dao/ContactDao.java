package com.sk.weichat.db.dao;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.sk.weichat.MyApplication;
import com.sk.weichat.bean.Contact;
import com.sk.weichat.db.SQLiteHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * 访问手机联系人的Dao
 */
public class ContactDao {
    private static ContactDao instance = null;

    public static ContactDao getInstance() {
        if (instance == null) {
            synchronized (ContactDao.class) {
                if (instance == null) {
                    instance = new ContactDao();
                }
            }
        }
        return instance;
    }

    public Dao<Contact, Integer> contactDao;

    private ContactDao() {
        try {
            contactDao = DaoManager.createDao(OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class).getConnectionSource(),
                    Contact.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        OpenHelperManager.releaseHelper();
    }

    // 创建联系人
    public boolean createContact(Contact contact) {
        try {
            List<Contact> mContactsByToUserId = getContactsByToUserId(contact.getUserId(), contact.getToUserId());
            if (mContactsByToUserId != null && mContactsByToUserId.size() > 0) {
                return false;
            }
            contactDao.create(contact);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 删除联系人
    public void deleteContact(String ownerId, String toUserId) {
        try {
            DeleteBuilder<Contact, Integer> builder = contactDao.deleteBuilder();
            builder.where().eq("userId", ownerId).and().eq("toUserId", toUserId);
            contactDao.delete(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 获取ownerId用户的所有联系人
    public List<Contact> getAllContacts(String ownerId) {
        List<Contact> contacts = new ArrayList<>();
        try {
            PreparedQuery<Contact> preparedQuery = contactDao.queryBuilder().where()
                    .eq("userId", ownerId)
                    .prepare();

            contacts = contactDao.query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contacts;
    }

    // 获取ownerId下指定id的联系人
    public List<Contact> getContactsByToUserId(String ownerId, String toUserId) {
        try {
            PreparedQuery<Contact> preparedQuery = contactDao.queryBuilder().where()
                    .eq("userId", ownerId).and()
                    .eq("toUserId", toUserId)
                    .prepare();

            return contactDao.query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void refreshContact(String ownerId, List<Contact> contacts) {
        // 1.清空本地
        List<Contact> contactListFromLocal = getAllContacts(ownerId);
        for (Contact contact : contactListFromLocal) {
            deleteContact(ownerId, contact.getToUserId());
        }
        // 2.将服务端数据存入本地
        for (Contact contact : contacts) {
            createContact(contact);
        }
    }
}