package com.sk.weichat.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.MyApplication;
import com.sk.weichat.bean.Contacts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 手机联系人 工具
 * Create by zq
 */
public class ContactsUtil {
    /**
     * 获取手机联系人 名字 + 手机号
     */
    public static Map<String, Contacts> getPhoneContacts(Context context) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projects = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor cursor = context.getContentResolver().query(uri, projects, null, null, null);
        Map<String, Contacts> mContactsMap = new HashMap<>();
        if (cursor == null) {
            // 部分国产设备会假装给权限进入这里然后查询到的cursor为空，
            return mContactsMap;
        }
        try {
            cursor.moveToFirst();

            int mobilePrefix = PreferenceUtils.getInt(MyApplication.getContext(), Constants.AREA_CODE_KEY, 86);

            while (!cursor.isAfterLast()) {
                String name = cursor.getString(0);
                String number = cursor.getString(1);

                // 1：+区号(根据当前账号的区号来，默认86)
                // Todo 当该用户通讯录内存在国内与国外联系人且都注册时，与用户不同区号的联系人在手机联系人内显示不出来，因为上传的手机号统一加上了当前用户的区号(服务端需要改，最好能做到不添加区号，服务端依旧能查到该人)
                if (!number.startsWith(String.valueOf(mobilePrefix))) {
                    number = String.valueOf(mobilePrefix) + number;
                }
                // 2：替换所有" " "-"
                String replaceSpaceNumber = number.replaceAll(" ", "");
                String replaceSymbolNumber = replaceSpaceNumber.replaceAll("-", "");
                mContactsMap.put(replaceSymbolNumber, new Contacts(name, replaceSymbolNumber));

                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return mContactsMap;
    }

    /**
     * 与本地数据进行对比，获取新添加的联系人上传
     */
    public static List<Contacts> getNewAdditionContacts(Context context, String userId) {
        List<Contacts> mNewAdditionContacts = new ArrayList<>();
        List<String> mUpdate = new ArrayList<>();

        // 获得手机联系人
        Map<String, Contacts> phoneContacts = getPhoneContacts(context);

        // 取出本地联系人
        String string = PreferenceUtils.getString(MyApplication.getContext(),
                userId + Constants.LOCAL_CONTACTS);
        List<String> telephoneList = JSON.parseArray(string, String.class);
        if (telephoneList == null) {
            telephoneList = new ArrayList<>();
        }

        // 对比，获取新增联系人
        Collection<Contacts> values = phoneContacts.values();
        List<Contacts> mContactsList = new ArrayList<>(values);

        for (int i = 0; i < mContactsList.size(); i++) {

            mUpdate.add(mContactsList.get(i).getTelephone());

            boolean isContainsKey = false;
            for (int i1 = 0; i1 < telephoneList.size(); i1++) {
                if (telephoneList.get(i1).equals(mContactsList.get(i).getTelephone())) {
                    isContainsKey = true;
                }
            }
            if (!isContainsKey) {
                mNewAdditionContacts.add(mContactsList.get(i));
            }
        }

        // 更新本地联系人
        PreferenceUtils.putString(MyApplication.getContext(),
                userId + Constants.LOCAL_CONTACTS, JSON.toJSONString(mUpdate));

        Log.e("zq-contacts", "新增联系人数量 : " + mNewAdditionContacts.size());
        for (int i = 0; i < mNewAdditionContacts.size(); i++) {
            Log.e("zq-contacts", mNewAdditionContacts.get(i).getName() + " : " + mNewAdditionContacts.get(i).getTelephone());
        }
        return mNewAdditionContacts;
    }

    public static void cleanLocalCache(Context context, String userId) {
        PreferenceUtils.putString(MyApplication.getContext(),
                userId + Constants.LOCAL_CONTACTS, "");
    }
}
