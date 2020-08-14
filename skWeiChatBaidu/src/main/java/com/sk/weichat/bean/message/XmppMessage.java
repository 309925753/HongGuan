package com.sk.weichat.bean.message;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.field.DatabaseField;

public abstract class XmppMessage {
    ////////////////////////////以下为在聊天界面显示的类型/////////////////////////////////
    public static final int TYPE_TEXT = 1; // 文字
    public static final int TYPE_IMAGE = 2;// 图片
    public static final int TYPE_VOICE = 3;// 语音
    public static final int TYPE_LOCATION = 4; // 位置
    public static final int TYPE_GIF = 5;  // gif
    public static final int TYPE_VIDEO = 6;// 视频
    public static final int TYPE_SIP_AUDIO = 7;// 音频
    public static final int TYPE_CARD = 8;// 名片
    public static final int TYPE_FILE = 9;// 文件
    public static final int TYPE_TIP = 10;// 自己添加的消息类型,代表系统的提示
    public static final int TYPE_REPLAY = 94;// 回复，

    public static final int TYPE_READ = 26;    // 是否已读的回执类型

    public static final int TYPE_RED = 28;// 红包消息
    public static final int TYPE_TRANSFER = 29;// 转账消息
    public static final int TYPE_SCAN_RECHARGE = 78;// 扫码充值返回
    public static final int TYPE_SCAN_WITHDRAW = 79; // 扫码提现返回
    public static final int TYPE_IMAGE_TEXT = 80;     // 单条图文消息
    public static final int TYPE_IMAGE_TEXT_MANY = 81;// 多条图文消息
    public static final int TYPE_LINK = 82; // 链接
    public static final int TYPE_SHARE_LINK = 87; // 分享进来的链接
    public static final int TYPE_83 = 83;// 某个成员领取了红包
    public static final int TYPE_SHAKE = 84;// 戳一戳
    public static final int TYPE_CHAT_HISTORY = 85;// 聊天记录
    public static final int TYPE_RED_BACK = 86;// 红包退回通知
    public static final int TYPE_TRANSFER_RECEIVE = 88;// 转账已被领取

    public static final int TYPE_TRANSFER_BACK = 89;// 转账已退回
    public static final int TYPE_PAYMENT_OUT = 90;// 付款码-已付款通知
    public static final int TYPE_RECEIPT_OUT = 92;// 收款码-已付款通知
    public static final int TYPE_PAYMENT_GET = 91;// 付款码-已到账通知
    public static final int TYPE_RECEIPT_GET = 93;// 收款码-已到账通知
    public static final int TYPE_PAY_CERTIFICATE = 97;// 支付凭证
    public static final int TYPE_YEEPAY_SEND_RED_SUCCESS = 98;// 易宝发红包支付成功回执，
    public static final int TYPE_YEEPAY_TRANSFER_SUCCESS = 99;// 易宝发转账支付成功回执，

    public static final int TYPE_SCREENSHOT = 95;// 对方ios截图，阅后即焚的时候，
    public static final int TYPE_SYNC_CLEAN_CHAT_HISTORY = 96;// 双向清除聊天记录

    public static final int TYPE_SEND_ONLINE_STATUS = 200;// 在线情况
    public static final int TYPE_INPUT = 201;// 正在输入消息
    public static final int TYPE_BACK = 202; // 撤回消息

    ////////////////////////////音视频通话/////////////////////////////////
    public static final int TYPE_IS_CONNECT_VOICE = 100;// 发起语音通话
    public static final int TYPE_CONNECT_VOICE = 102;// 接听语音通话
    public static final int TYPE_NO_CONNECT_VOICE = 103;// 拒绝语音通话 || 对来电不响应(30s)内
    public static final int TYPE_END_CONNECT_VOICE = 104;// 结束语音通话

    public static final int TYPE_IS_CONNECT_VIDEO = 110;// 发起视频通话
    public static final int TYPE_CONNECT_VIDEO = 112;// 接听视频通话
    public static final int TYPE_NO_CONNECT_VIDEO = 113;// 拒绝视频通话 || 对来电不响应(30s内)
    public static final int TYPE_END_CONNECT_VIDEO = 114;// 结束视频通话

    public static final int TYPE_IS_CONNECT_SCREEN = 140;// 发起屏幕共享
    public static final int TYPE_CONNECT_SCREEN = 142;// 接听屏幕共享
    public static final int TYPE_NO_CONNECT_SCREEN = 143;// 拒绝屏幕共享 || 对来电不响应(30s内)
    public static final int TYPE_END_CONNECT_SCREEN = 144;// 结束屏幕共享

    public static final int TYPE_IS_MU_CONNECT_TALK = 130;// 发起对讲机
    public static final int TYPE_TALK_JOIN = 131;// 对讲机加入，
    public static final int TYPE_TALK_LEFT = 132;// 对讲机离开，
    public static final int TYPE_TALK_REQUEST = 133;// 对讲机请求说话，
    public static final int TYPE_TALK_RELEASE = 134;// 对讲机结束说话，
    public static final int TYPE_TALK_ONLINE = 135;// 对讲机在线，
    // 暂未用到
    public static final int TYPE_TALK_KICK = 136;// 对讲机踢出，

    public static final int TYPE_IS_MU_CONNECT_VIDEO = 115;// 视频会议邀请
    public static final int TYPE_IS_MU_CONNECT_VOICE = 120;// 音频会议邀请
    public static final int TYPE_IS_MU_CONNECT_SCREEN = 145;// 屏幕共享会议邀请

    public static final int TYPE_IN_CALLING = 123;// 通话中...
    public static final int TYPE_IS_BUSY = 124;// 忙线中...
    public static final int TYPE_CHANGE_VIDEO_ENABLE = 125;// 切换单聊语音/视频模式，

    // 暂未用到
    public static final int TYPE_VIDEO_IN = 116;// 视频会议进入
    public static final int TYPE_VIDEO_OUT = 117;// 视频会议退出
    public static final int TYPE_OK_MU_CONNECT_VOICE = 121;// 音频会议进入了
    public static final int TYPE_EXIT_VOICE = 122;// 音频会议退出了

    ////////////////////////////朋友圈消息/////////////////////////////////
    public static final int DIANZAN = 301; // 朋友圈点赞
    public static final int PINGLUN = 302; // 朋友圈评论
    public static final int R_DIANZAN = 303; // 朋友圈取消点赞
    public static final int ATMESEE = 304; // 提醒我看
    public static final int FRIEND_PUBLISH = 305; // 朋友发布生活圈

    ////////////////////////////新朋友消息/////////////////////////////////
    public static final int TYPE_SAYHELLO = 500;// 打招呼
    public static final int TYPE_PASS = 501;    // 同意加好友
    public static final int TYPE_FEEDBACK = 502;// 回话
    public static final int TYPE_FRIEND = 508;// 直接成为好友
    public static final int TYPE_BLACK = 507; // 黑名单
    public static final int TYPE_REFUSED = 509;// 取消黑名单
    public static final int TYPE_DELALL = 505; // 彻底删除
    public static final int TYPE_CONTACT_BE_FRIEND = 510;// 对方通过 手机联系人 添加我 直接成为好友
    public static final int TYPE_NEW_CONTACT_REGISTER = 511;// 我之前上传给服务端的联系人表内有人注册了，更新 手机联系人
    public static final int TYPE_REMOVE_ACCOUNT = 512;// 用户被后台删除，用于客户端更新本地数据 ，from是系统管理员 to是被删除人的userId，
    public static final int TYPE_BACK_BLACK = 513;// 好友用户被后台拉黑，用于客户端更新本地数据 ，from是系统管理员 to是被删除人的userId，
    public static final int TYPE_BACK_REFUSED = 514;// 好友用户被后台取消拉黑，用于客户端更新本地数据 ，from是系统管理员 to是自己的userId，objectId中有拉黑双方Id,
    public static final int TYPE_BACK_DELETE = 515;// 好友用户被后台删除，是删除好友关系 ，from是系统管理员 to是自己的userId，objectId中有删除双方Id,

    // 未用到
    public static final int TYPE_NEWSEE = 503;// 新关注
    public static final int TYPE_DELSEE = 504;// 删除关注
    public static final int TYPE_RECOMMEND = 506;// 新推荐好友

    public static final int TYPE_SYNC_OTHER = 800;
    public static final int TYPE_SYNC_FRIEND = 801;
    public static final int TYPE_SYNC_GROUP = 802;
    public static final int TYPE_SECURE_REFRESH_KEY = 803;// 好友修改、忘记密码，更新了密钥对
    public static final int TYPE_SECURE_LOST_KEY = 804;// 修改密码，请求群组chatKey
    public static final int TYPE_SECURE_SEND_KEY = 805;// 给群成员发送chatKey
    public static final int TYPE_SECURE_NOTIFY_REFRESH_KEY = 806;// 群主重置了chatKey

    public static final int TYPE_AUTH_LOGIN = 810;
    public static final int TYPE_SEAL = 811;// 封人通知

    //约吧
    public static final int TYPE_RECEIVE_GIFT=400;//约吧收礼物消息
    ////////////////////////////群组协议/////////////////////////////////
    public static final int TYPE_MUCFILE_ADD = 401; // 群文件上传
    public static final int TYPE_MUCFILE_DEL = 402; // 群文件删除
    public static final int TYPE_MUCFILE_DOWN = 403;// 群文件下载

    public static final int TYPE_CHANGE_NICK_NAME = 901; // 修改昵称
    public static final int TYPE_CHANGE_ROOM_NAME = 902; // 修改房间名
    public static final int TYPE_DELETE_ROOM = 903;// 删除房间
    public static final int TYPE_DELETE_MEMBER = 904;// 退出、被踢出群组
    public static final int TYPE_NEW_NOTICE = 905; // 新公告
    public static final int TYPE_GAG = 906;// 禁言/取消禁言
    public static final int NEW_MEMBER = 907; // 增加新成员
    public static final int TYPE_SEND_MANAGER = 913;// 设置/取消管理员

    public static final int TYPE_CHANGE_SHOW_READ = 915; // 设置群已读消息
    public static final int TYPE_GROUP_VERIFY = 916; // 群组验证消息
    public static final int TYPE_GROUP_LOOK = 917; // 群组是否公开
    public static final int TYPE_GROUP_SHOW_MEMBER = 918; // 群组是否显示群成员列表
    public static final int TYPE_GROUP_SEND_CARD = 919; // 群组是否允许发送名片
    public static final int TYPE_GROUP_ALL_SHAT_UP = 920; // 全体禁言
    public static final int TYPE_GROUP_ALLOW_NORMAL_INVITE = 921; // 允许普通成员邀请人入群
    public static final int TYPE_GROUP_ALLOW_NORMAL_UPLOAD = 922; // 允许普通成员上传群共享
    public static final int TYPE_GROUP_ALLOW_NORMAL_CONFERENCE = 923; // 允许普通成员发起会议
    public static final int TYPE_GROUP_ALLOW_NORMAL_SEND_COURSE = 924;// 允许普通成员发送讲课
    public static final int TYPE_GROUP_TRANSFER = 925; // 转让群组

    ////////////////////////////直播协议/////////////////////////////////
    public static final int TYPE_SEND_DANMU = 910;// 弹幕
    public static final int TYPE_SEND_GIFT = 911; // 礼物
    public static final int TYPE_SEND_HEART = 912;// 点赞
    public static final int TYPE_SEND_ENTER_LIVE_ROOM = 914;// 加入直播间
    // 以前直播间和群组共用了部分协议，现独立出来
    public static final int TYPE_LIVE_LOCKING = 926; // 锁定直播间(后台可锁定用户直播间)
    public static final int TYPE_LIVE_EXIT_ROOM = 927;// 退出、被踢出直播间
    public static final int TYPE_LIVE_SHAT_UP = 928;// 禁言/取消禁言
    public static final int TYPE_LIVE_SET_MANAGER = 929;// 设置/取消管理员

    public static final int TYPE_UPDATE_ROLE = 930;// 设置/取消隐身人，监控人，
    public static final int TYPE_DISABLE_GROUP = 931;// 群组被后台锁定/解锁
    public static final int TYPE_GROUP_UPDATE_MSG_AUTO_DESTROY_TIME = 932;// 消息过期自动销毁
    public static final int TYPE_FACE_GROUP_NOTIFY = 933;// 面对面建群有人加入、退出
    public static final int TYPE_EDIT_GROUP_NOTICE = 934;// 编辑群公告

    /* 网络传输字段 */
    @DatabaseField(canBeNull = false)
    protected int type;// 消息的类型
    @DatabaseField
    protected boolean isMySend = true;// 是否是由我自己发送，代替toUserId，toUserId废弃不用,默认值true，代表是我发送的
    @DatabaseField(canBeNull = false)
    protected String packetId;// 消息包的Id
    @DatabaseField(canBeNull = false)
    protected double timeSend; // 发送时间，秒级别的，为点击发送按钮，开始发送的时间

    public static boolean filter(ChatMessage chatMessage) {
        if (chatMessage.getType() == XmppMessage.TYPE_READ
                || chatMessage.getType() == XmppMessage.TYPE_BACK
                || chatMessage.getType() == XmppMessage.TYPE_SECURE_LOST_KEY
                || chatMessage.getType() == XmppMessage.TYPE_SECURE_SEND_KEY) {
            return true;
        }
        return false;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isMySend() {
        return isMySend;
    }

    public void setMySend(boolean isMySend) {
        this.isMySend = isMySend;
    }

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    // Todo 2018-11-28 by zq
    // Todo 服务器在压测时，一秒钟会发送很多条消息，且timeSend为double类型，但本地之前为long类型，在存入数据库的时候会将double类型的timeSend会强转为long类型
    // Todo 这时，在查询数据库的时候如果为同一秒发送的消息顺序排列会不准确
    // Todo 在将TimeSend改为double类型之后，因为getTimeSend()这个方法有太多地方调用了，所以我们return 的时候强转下，而不去动其他类
    // Todo 改为double只是用于数据库查询 ex:' builder.orderBy("timeSend", false);' 其它地方不会有影响
    public long getTimeSend() {
        return (long) timeSend;
    }

    public void setTimeSend(long timeSend) {
        this.timeSend = timeSend;
    }

    public double getDoubleTimeSend() {
        return timeSend;
    }

    public void setDoubleTimeSend(double timeSend) {
        this.timeSend = timeSend;
    }

    protected String getStringValueFromJSONObject(JSONObject jObject, String key) {
        if (jObject == null || TextUtils.isEmpty(key)) {
            return "";
        }

        String value = "";
        try {
            value = jObject.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (value == null) {
                value = "";
            }
            return value;
        }
    }

    protected int getIntValueFromJSONObject(JSONObject jObject, String key) {
        if (jObject == null || TextUtils.isEmpty(key)) {
            return 0;
        }
        int value = 0;
        try {
            value = jObject.getIntValue(key);
        } catch (Exception e) {
            e.printStackTrace();
            String v = jObject.getString(key);
            if ("true".equals(v)) {
                value = 1;
            } else {
                value = 0;
            }
        } finally {
            return value;
        }
    }

    protected long getLongValueFromJSONObject(JSONObject jObject, String key) {
        long value = 0;
        try {
            if (jObject != null) {
                value = jObject.getLongValue(key);
            } else {
                value = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            value = 0;
        }
        return value;
    }

    protected double getDoubleFromJSONObject(JSONObject jObject, String key) {
        double value = 0;
        try {
            if (jObject != null) {
                value = jObject.getDoubleValue(key);
            } else {
                value = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            value = 0;
        }
        return value;
    }
}
