package com.sk.weichat.broadcast;

import android.content.Context;
import android.content.Intent;

import com.sk.weichat.AppConfig;

/**
 * 用于聊天消息的广播，更新MainActivity Tab栏显示的未读数量 和 消息界面数据的更新
 */
public class MsgBroadcast {
    public static final String ACTION_MSG_UI_UPDATE = AppConfig.sPackageName + ".action.msg_ui_update";// 消息界面的更新
    public static final String ACTION_MSG_UI_UPDATE_SINGLE = AppConfig.sPackageName + ".action.msg_ui_update_single";// 消息界面的更新
    public static final String ACTION_MSG_NUM_UPDATE = AppConfig.sPackageName + ".intent.action.msg_num_update";// 未读数量的更新
    public static final String ACTION_MSG_NUM_UPDATE_NEW_FRIEND = AppConfig.sPackageName + ".intent.action.msg_num_update_new_friend";// 新的朋友 未读数量的更新
    public static final String ACTION_MSG_NUM_RESET = AppConfig.sPackageName + ".action.msg_num_reset";// 未读数量需要重置，即从数据库重新查
    public static final String EXTRA_NUM_COUNT = "count";
    public static final String EXTRA_NUM_OPERATION = "operation";
    public static final int NUM_ADD = 0; // 消息加
    public static final int NUM_REDUCE = 1; // 消息减

    public static final String ACTION_MSG_STATE_UPDATE = AppConfig.sPackageName + ".action.CHANGE_MESSAGE_STATE"; // 通知某条消息改变

    public static final String ACTION_DISABLE_GROUP_BY_SERVICE = AppConfig.sPackageName + ".action.disable_group_by_service"; // 群组被锁定/解锁
    public static final String ACTION_FACE_GROUP_NOTIFY = AppConfig.sPackageName + ".action.face_group_notify"; // 面对面建群界面广播
    public static final String EXTRA_OPERATING = "EXTRA_OPERATING";

    public static final String ACTION_MSG_UPDATE_ROOM = AppConfig.sPackageName + ".action.msg_room_update"; // 聊天群组界面的更新
    public static final String ACTION_MSG_ROLE_CHANGED = AppConfig.sPackageName + ".action.ROLE_CHANGED";
    public static final String ACTION_MSG_UPDATE_ROOM_GET_ROOM_STATUS = AppConfig.sPackageName + ".action.msg_room_update_get_room_status"; // 聊天群组界面的更新并调用群属性接口
    public static final String ACTION_MSG_CLOSE_TRILL = AppConfig.sPackageName + ".action.colse_trill"; // 关闭抖音模块
    public static final String ACTION_MSG_UPDATE_ROOM_INVITE = AppConfig.sPackageName + ".action.msg_room_update_invite"; // 群组信息界面更新允许群成员邀请好友，
    public static final String EXTRA_ENABLED = "EXTRA_ENABLED";

    /**
     * 更新消息Fragment的广播
     */
    public static void broadcastMsgUiUpdate(Context context) {
        context.sendBroadcast(new Intent(ACTION_MSG_UI_UPDATE));
    }

    /**
     * 更新消息Fragment的广播
     */
    public static void broadcastMsgUiUpdateSingle(Context context, String fromUserId) {
        Intent intent = new Intent(ACTION_MSG_UI_UPDATE_SINGLE);
        intent.putExtra("fromUserId", fromUserId);
        context.sendBroadcast(intent);
    }

    public static void broadcastMsgNumUpdate(Context context, boolean add, int count) {
        Intent intent = new Intent(ACTION_MSG_NUM_UPDATE);
        intent.putExtra(EXTRA_NUM_COUNT, count);
        if (add) {
            intent.putExtra(EXTRA_NUM_OPERATION, NUM_ADD);
        } else {
            intent.putExtra(EXTRA_NUM_OPERATION, NUM_REDUCE);
        }
        context.sendBroadcast(intent);
    }

    public static void broadcastMsgNumUpdateNewFriend(Context context) {
        Intent intent = new Intent(ACTION_MSG_NUM_UPDATE_NEW_FRIEND);
        context.sendBroadcast(intent);
    }

    public static void broadcastMsgNumReset(Context context) {
        context.sendBroadcast(new Intent(ACTION_MSG_NUM_RESET));
    }

    public static void broadcastMsgReadUpdate(Context context, String packetId) {
        Intent intent = new Intent(ACTION_MSG_STATE_UPDATE);
        intent.putExtra("packetId", packetId);
        context.sendBroadcast(intent);
    }

    /**
     * 群组有关广播
     *
     * @param context
     */
    public static void broadcastMsgRoomUpdate(Context context) {
        context.sendBroadcast(new Intent(ACTION_MSG_UPDATE_ROOM));
    }

    public static void broadcastMsgRoleChanged(Context context) {
        context.sendBroadcast(new Intent(ACTION_MSG_ROLE_CHANGED));
    }

    public static void broadcastMsgRoomUpdateGetRoomStatus(Context context) {
        context.sendBroadcast(new Intent(ACTION_MSG_UPDATE_ROOM_GET_ROOM_STATUS));
    }

    public static void broadcastMsgRoomUpdateInvite(Context context, int enabled) {
        Intent intent = new Intent(ACTION_MSG_UPDATE_ROOM_INVITE);
        intent.putExtra(EXTRA_ENABLED, enabled);
        context.sendBroadcast(intent);
    }

    public static void broadcastFaceGroupNotify(Context context, String operating) {
        Intent intent = new Intent(ACTION_FACE_GROUP_NOTIFY);
        intent.putExtra(EXTRA_OPERATING, operating);
        context.sendBroadcast(intent);
    }

    public static void broadcastMsgColseTrill(Context context) {
        context.sendBroadcast(new Intent(ACTION_MSG_CLOSE_TRILL));
    }
}

