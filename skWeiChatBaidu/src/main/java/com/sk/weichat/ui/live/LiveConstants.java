package com.sk.weichat.ui.live;

import com.sk.weichat.AppConfig;

/**
 * 直播常量
 */

public class LiveConstants {
    /*
     * Do not change these values without updating their counterparts in native
     * ijk playing ListenInfo
     */
    public static int MEDIA_INFO_UNKNOWN = 1;//未知信息
    public static int MEDIA_INFO_STARTED_AS_NEXT = 2;//播放下一条
    public static int MEDIA_INFO_VIDEO_RENDERING_START = 3;//视频开始整备中
    public static int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;//视频日志跟踪
    public static int MEDIA_INFO_BUFFERING_START = 701;//开始缓冲中
    public static int MEDIA_INFO_BUFFERING_END = 702;//缓冲结束
    public static int MEDIA_INFO_NETWORK_BANDWIDTH = 703;//网络带宽，网速方面
    public static int MEDIA_INFO_BAD_INTERLEAVING = 800;//
    public static int MEDIA_INFO_NOT_SEEKABLE = 801;//不可设置播放位置，直播方面
    public static int MEDIA_INFO_METADATA_UPDATE = 802;//
    public static int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    public static int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;//不支持字幕
    public static int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;//字幕超时

    public static int MEDIA_INFO_VIDEO_INTERRUPT = -10000;//数据连接中断
    public static int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;//视频方向改变
    public static int MEDIA_INFO_AUDIO_RENDERING_START = 10002;//音频开始整备中

    public static int MEDIA_ERROR_UNKNOWN = 1;//未知错误
    public static int MEDIA_ERROR_SERVER_DIED = 100;//服务挂掉
    public static int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;//数据错误没有有效的回收
    public static int MEDIA_ERROR_IO = -1004;//IO 错误
    public static int MEDIA_ERROR_MALFORMED = -1007;
    public static int MEDIA_ERROR_UNSUPPORTED = -1010;//数据不支持
    public static int MEDIA_ERROR_TIMED_OUT = -110;//数据超时

    /**
     * Constants for activity intent
     */
    public static String LIVE_PUSH_FLOW_URL = "live_push_flow";// 推流地址
    public static String LIVE_GET_FLOW_URL = "live_get_flow";  // 取流地址
    public static String LIVE_ROOM_ID = "live_room_id";        // 直播间id
    public static String LIVE_ROOM_PERSON_ID = "live_room_person_id";// 主播id
    public static String LIVE_CHAT_ROOM_ID = "live_room_j_id";       // 聊天室id(群组)
    public static String LIVE_ROOM_NAME = "live_room_name";          // 直播间/聊天室名字
    public static String LIVE_ROOM_NOTICE = "live_room_notice";          // 直播间公告

    public static String LIVE_STATUS = "live_status"; // 直播间状态(正在直播...)
    /**
     * Constants for broadcast intent
     */
    public static String LIVE_MEMBER_ADD = AppConfig.sPackageName + "live_member_add";          // 直播间列表刷新
    public static String LIVE_MEMBER_DELETE = AppConfig.sPackageName + "live_member_delete";    // 直播间列表刷新
    public static String LIVE_DANMU_DRAWABLE = AppConfig.sPackageName + "live_danmu_drawable";  // 绘制弹幕
    public static String LIVE_SEND_GIFT = AppConfig.sPackageName + "live_send_gift";            // 发送礼物
    public static String LIVE_SEND_LOVE_HEART = AppConfig.sPackageName + "live_send_love_heart";// 发送爱心
    public static String LIVE_SEND_MANAGER = AppConfig.sPackageName + "live_send_manager";      // 设为管理员
    public static String LIVE_SEND_SHUT_UP = AppConfig.sPackageName + "live_send_shut_up";      // 禁言
    public static String LIVE_SEND_LOCKED = AppConfig.sPackageName + "live_is_locked";          // 锁定直播间
    public static String LIVE_SEND_REFRESH_MONEY = AppConfig.sPackageName + "live_send_refresh_money";// 刷新余额
}
