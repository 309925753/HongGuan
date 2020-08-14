package com.sk.weichat.broadcast;

import com.sk.weichat.AppConfig;

/**
 * 项目老代码中大量广播都是写死action，统一移到这里，改成包名开头，
 */
public class OtherBroadcast {
    public static final String Read = AppConfig.sPackageName + "Read";
    public static final String NAME_CHANGE = AppConfig.sPackageName + "NAME_CHANGE";
    public static final String TYPE_DELALL = AppConfig.sPackageName + "TYPE_DELALL";
    public static final String SEND_MULTI_NOTIFY = AppConfig.sPackageName + "SEND_MULTI_NOTIFY";
    public static final String CollectionRefresh = AppConfig.sPackageName + "CollectionRefresh";
    public static final String CollectionRefresh_ChatFace = AppConfig.sPackageName + "CollectionRefresh_ChatFace";
    public static final String NO_EXECUTABLE_INTENT = AppConfig.sPackageName + "NO_EXECUTABLE_INTENT";
    public static final String QC_FINISH = AppConfig.sPackageName + "QC_FINISH";
    public static final String longpress = AppConfig.sPackageName + "longpress";
    public static final String IsRead = AppConfig.sPackageName + "IsRead";
    public static final String MULTI_LOGIN_READ_DELETE = AppConfig.sPackageName + "MULTI_LOGIN_READ_DELETE";
    public static final String TYPE_INPUT = AppConfig.sPackageName + "TYPE_INPUT";
    public static final String MSG_BACK = AppConfig.sPackageName + "MSG_BACK";
    public static final String REFRESH_MANAGER = AppConfig.sPackageName + "REFRESH_MANAGER";
    public static final String singledown = AppConfig.sPackageName + "singledown";
    public static final String SYNC_CLEAN_CHAT_HISTORY = AppConfig.sPackageName + "sync_clean_chat_history";
    public static final String SYNC_SELF_DATE = AppConfig.sPackageName + "sync_self_data";
    public static final String SYNC_SELF_DATE_NOTIFY = AppConfig.sPackageName + "sync_self_data_notify";
    public static final String BROADCASTTEST_ACTION = AppConfig.sPackageName + ".action.broadcasttest.startActivity";
    public static final String ACTION_SHAKE_MSG_NOTIFY = AppConfig.sPackageName + ".action.shake_msg_notify";
}
