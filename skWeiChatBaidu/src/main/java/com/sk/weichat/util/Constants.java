package com.sk.weichat.util;

import com.sk.weichat.AppConfig;
import com.sk.weichat.BuildConfig;

public class Constants {

    public static final String VX_APP_ID = BuildConfig.WECHAT_APP_ID;

    public static final String NOT_AUTHORIZED = AppConfig.sPackageName + "not_authorized";// XMPP登录时密码错误(密码与服务端不匹配)
    public static final String PING_FAILED = AppConfig.sPackageName + "ping_failed";// XMPP Ping服务器ping失败了
    public static final String CLOSED_ON_ERROR_NORMAL = AppConfig.sPackageName + "closed_on_error_normal";// XMPP回调了connectionClosedOnError
    public static final String CLOSED_ON_ERROR_END_DOCUMENT = AppConfig.sPackageName + "closed_on_error_end_document";// XMPP回调了connectionClosedOnError 并且Exception为Parser got END_DOCUMENT event. This could happen e.g. if the server closed the connection without sending a closing stream element
    /*
    Other
     */
    // 国家区号
    public static final String MOBILE_PREFIX = "MOBILE_PREFIX";
    // 登录冲突，否，退出app，记录，下次进入历史登录界面
    public static final String LOGIN_CONFLICT = "login_conflict";
    // 当前设备离线时间
    public static final String OFFLINE_TIME = "offline_time";
    // App启动次数
    public static final String APP_LAUNCH_COUNT = "app_launch_count";
    public static final String IS_AUDIO_CONFERENCE = "is_audio_conference";
    public static final String LOCAL_CONTACTS = "local_contacts";
    public static final String NEW_CONTACTS_NUMBER = "new_contacts_number";
    public static final String NEW_CONTACTS_IDS = "new_contacts_ids";
    // 新消息数量
    public static final String NEW_MSG_NUMBER = "new_msg_number";
    // 通知栏进入
    public final static String IS_NOTIFICATION_BAR_COMING = "is_notification_bar_coming";
    // 刷新"消息"角标
    public final static String NOTIFY_MSG_SUBSCRIPT = AppConfig.sPackageName + "notify_msg_subscript";
    public final static String AREA_CODE_KEY = "areCode";
    public final static String UPDATE_ROOM = AppConfig.sPackageName + "update_room";
    public final static String BROWSER_SHARE_MOMENTS_CONTENT = "browser_share_moments_content";
    /*
    Chat Publish
     */
    // 最近一张屏幕截图的路径
    public final static String SCREEN_SHOTS = "screen_shots";
    // 删除
    public final static String CHAT_MESSAGE_DELETE_ACTION = AppConfig.sPackageName + "chat_message_delete";
    public final static String CHAT_REMOVE_MESSAGE_POSITION = "CHAT_REMOVE_MESSAGE_POSITION";
    // 多选
    public final static String SHOW_MORE_SELECT_MENU = AppConfig.sPackageName + "show_more_select_menu";
    public final static String CHAT_SHOW_MESSAGE_POSITION = "CHAT_SHOW_MESSAGE_POSITION";
    public final static String IS_MORE_SELECTED_INSTANT = "IS_MORE_SELECTED_INSTANT";// 是否为多选转发
    public final static String IS_SINGLE_OR_MERGE = "IS_SINGLE_OR_MERGE";// 逐条还是合并转发
    // 单、群聊 清空聊天记录
    public final static String CHAT_HISTORY_EMPTY = AppConfig.sPackageName + "chat_history_empty";
    // 更新消息过期时间的通知
    public final static String CHAT_TIME_OUT_ACTION = AppConfig.sPackageName + "chat_time_out_action";
    /*
    Person Set
     */
    // 阅后即焚
    public final static String MESSAGE_READ_FIRE = "message_read_fire";
    // 聊天背景
    public final static String SET_CHAT_BACKGROUND = "chat_background";
    public final static String SET_CHAT_BACKGROUND_PATH = "chat_background_path";
    /*
    Group Set
     */
    public final static String GROUP_JOIN_NOTICE = "group_join_notice";
    // 屏蔽群组消息
    public final static String SHIELD_GROUP_MSG = "shield_group_msg";
    // 全体禁言
    public final static String GROUP_ALL_SHUP_UP = "group_all_shut_up";
    // 是否开启群已读
    public final static String IS_SHOW_READ = "is_show_read";
    //是否允许普通群成员私聊
    public final static String IS_SEND_CARD = "is_send_card";
    // 是否允许普通成员召开会议
    public final static String IS_ALLOW_NORMAL_CONFERENCE = "is_allow_normal_conference";
    // 是否允许普通成员发送讲课
    public final static String IS_ALLOW_NORMAL_SEND_COURSE = "is_allow_normal_send_course";
    // 是否需要群主确认进群
    public final static String IS_NEED_OWNER_ALLOW_NORMAL_INVITE_FRIEND = "is_need_owner_allow_normal_invite_friend";
    // 是否允许普通成员发送文件、上传群共享
    public final static String IS_ALLOW_NORMAL_SEND_UPLOAD = "is_allow_normal_send_upload";

    public final static String SPEAKER_AUTO_SWITCH = "speaker_auto_switch";
    /*
    Set
     */
    // 字体大小
    public final static String FONT_SIZE = "font_size";
    public final static String IS_PAY_PASSWORD_SET = "isPayPasswordSet";
    @SuppressWarnings("WeakerAccess")
    public static final String KEY_SKIN_NAME = "KEY_SKIN_NAME";
    /*
    收款 设置的金额与转账说明
     */
    public final static String RECEIPT_SETTING_MONEY = "receipt_setting_money";
    public final static String RECEIPT_SETTING_DESCRIPTION = "receipt_setting_description";
    // 与服务器的时间差，用于校准时间，
    public static final String KEY_TIME_DIFFERENCE = "KEY_TIME_DIFFERENCE";
    public static boolean IS_CLOSED_ON_ERROR_END_DOCUMENT;
    public static boolean OFFLINE_TIME_IS_FROM_SERVICE = false;// 离线时间是否为服务端获取的
    public static boolean IS_SENDONG_COURSE_NOW = false;// 现在是否正在发送课程
    // 群成员分页
    public static String MUC_MEMBER_PAGE_SIZE = "50";
    public static String MUC_MEMBER_LAST_JOIN_TIME = "muc_member_last_join_time";
    // 消息漫游条数
    public static int MSG_ROMING_PAGE_SIZE = 50;
    // 要求用户同意隐私政策，
    public static String PRIVACY_AGREE_STATUS = "PRIVACY_AGREE_STATUS";
    // 保存在sp的deviceId的key,
    public static String KEY_DEVICE_ID = "KEY_DEVICE_ID";
}
