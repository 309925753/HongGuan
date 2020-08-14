package com.sk.weichat.bean;

import android.text.TextUtils;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sk.weichat.MyApplication;
import com.sk.weichat.ui.message.HandleSecureChatMessage;

import java.io.Serializable;

@DatabaseTable
public class Friend implements Serializable {
    public static final String ID_SYSTEM_MESSAGE = "10000";// 系统消息ID
    public static final String ID_NEW_FRIEND_MESSAGE = "10001";// 新朋友消息 ID
    public static final String ID_SK_PAY = "1100";// 支付公众号，
    public static final String ID_BLOG_MESSAGE = "10002";// 商务圈消息ID
    public static final String ID_INTERVIEW_MESSAGE = "10004";// 面试中心ID（用于职位、初试、面试的推送）
    public static final String ID_SYSTEM_NOTIFICATION = "10005";// 系统号，用于各种控制消息通知，
    // -1:黑名单；0：陌生人；1:单方关注；2:互为好友；8:显示系统号；9:非显示系统号
    public static final int STATUS_BLACKLIST = -1;// 黑名单
    public static final int STATUS_UNKNOW = 0;// 陌生人(不可能出现在好友表，只可能在新朋友消息表)
    public static final int STATUS_ATTENTION = 1;// 关注
    public static final int STATUS_FRIEND = 2;// 好友
    public static final int STATUS_SYSTEM = 8;// 显示系统号
    // todo 以下status定义的很多，不过确都是NewFriendMessage内的Status，真正用到Friend表内的status仅上面的status以及status_19、status_23
    public static final int STATUS_10 = 10; //等待XXX验证
    public static final int STATUS_11 = 11; //确认XXX验证
    public static final int STATUS_12 = 12; //通过对XXX验证
    public static final int STATUS_13 = 13; //验证被XXX通过
    public static final int STATUS_14 = 14; //XXX回话
    public static final int STATUS_15 = 15; //回话XXX
    public static final int STATUS_16 = 16; //已删除了XXX
    public static final int STATUS_17 = 17; //XXX删除了我
    public static final int STATUS_18 = 18; //已拉黑了XXX
    public static final int STATUS_19 = 19; //XXX拉黑了我
    public static final int STATUS_20 = 20; //默认值什么都不显示，仅用于新的朋友页面容错
    public static final int STATUS_21 = 21;//XXX 添加你为好友
    public static final int STATUS_22 = 22;//你添加好友 XXX
    public static final int STATUS_24 = 24;//XXX 已移除黑名单
    public static final int STATUS_23 = 23;//todo 此状态代表多个操作，有点乱 如对方将我删除，我将对方删除，取消公众号关注
    public static final int STATUS_25 = 25;//通过手机联系人添加
    public static final int STATUS_26 = 26;//被后台删除的好友，仅用于新的朋友页面显示，
    private static final long serialVersionUID = -6859528031175998594L;
    @DatabaseField(generatedId = true)
    private int _id;

    @DatabaseField(canBeNull = false)
    private String ownerId; // 属于哪个用户的id

    @DatabaseField(canBeNull = false)
    private String userId; // 用户id或者聊天室id

    @DatabaseField(canBeNull = false)
    @JSONField(name = "nickname")
    private String nickName;// 用户昵称或者聊天室名称

    @DatabaseField
    private String description;// 签名

    @DatabaseField
    private long timeCreate;// 创建好友关系的时间

    @DatabaseField(defaultValue = "0")
    private int unReadNum; // 未读消息数量

    @DatabaseField
    private String content;// 最后一条消息内容

    @DatabaseField
    private int type;// 最后一条消息类型

    @DatabaseField
    private long timeSend;// 最后一条消息发送时间

    @DatabaseField(defaultValue = "0")
    private int roomFlag;// 0朋友 1群组 510（我的联系人群组）

    @DatabaseField(defaultValue = "0")
    private int companyId; // 0表示不是公司

    @DatabaseField
    private int status;// -1:黑名单；0：陌生人；1:单方关注；2:互为好友；8:系统号；9:非显示系统号

    @DatabaseField
    private String privacy;// 隐私

    @DatabaseField
    private String remarkName;// 备注

    @DatabaseField
    private String describe;// 描述，

    @DatabaseField
    private int version;// 本地表的版本

    @DatabaseField
    private String roomId;// 仅仅当roomFlag==1，为群组的时候才有效

    @DatabaseField
    private String roomCreateUserId;// 仅仅当roomFlag==1，为群组的时候才有效

    @DatabaseField
    private String roomMyNickName;// 我在这个群组的昵称

    @DatabaseField
    private int roomTalkTime;// 在这个群组的禁言时间

    @DatabaseField(defaultValue = "0")
    private long topTime;
    // 0:正常 1:被踢出该群组 2:该群已被解散 3:该群已被后台锁定
    @DatabaseField(defaultValue = "0")
    private int groupStatus;
    // 是否为设备，如果为设备，该Friend的userId为android || ios || pc...，之后需要在各个地方判断
    @DatabaseField(defaultValue = "0")
    private int isDevice;
    // 消息免打扰 0:未设置 1:已设置
    @DatabaseField(defaultValue = "0")
    private int offlineNoPushMsg;
    @DatabaseField(defaultValue = "0")
    private double chatRecordTimeOut;//0 || -1 消息永久保存 单位：day
    @DatabaseField(defaultValue = "0")
    private long downloadTime;// 最后一次同步消息的时间
    @DatabaseField(defaultValue = "0")
    private int isAtMe;// 是否有人@我 0 正常 1 @我 2 @全体成员

    private boolean isCheck;// 局部变量，用于多选，是否选中
    @DatabaseField(defaultValue = "0")
    private int encryptType;
    @DatabaseField
    private String publicKeyDH;// 好友dh钥
    @DatabaseField
    private String publicKeyRSARoom;// 好友rsa公钥
    @DatabaseField
    private int isSecretGroup;// 是否为私密群组
    @DatabaseField
    private String chatKeyGroup;// 私密群组通信Key
    @DatabaseField
    private int isLostChatKeyGroup;// 标记我已丢失当前私密群组的chatKey

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public int getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(int encryptType) {
        if (!MyApplication.IS_SUPPORT_SECURE_CHAT
                && (encryptType == 2 || encryptType == 3)) {
            // SecureFlag 非端到端，兼容之前在端到端版本注册过的账号，encryptType==2 || encryptType==3，统一当做1处理
            encryptType = 1;
        }
        this.encryptType = encryptType;
    }

    public String getPublicKeyDH() {
        return publicKeyDH;
    }

    public void setPublicKeyDH(String publicKeyDH) {
        this.publicKeyDH = publicKeyDH;
    }

    public String getPublicKeyRSARoom() {
        return publicKeyRSARoom;
    }

    public void setPublicKeyRSARoom(String publicKeyRSARoom) {
        this.publicKeyRSARoom = publicKeyRSARoom;
    }

    public int getIsSecretGroup() {
        return isSecretGroup;
    }

    public void setIsSecretGroup(int isSecretGroup) {
        this.isSecretGroup = isSecretGroup;
    }

    public String getChatKeyGroup() {
        return chatKeyGroup;
    }

    public void setChatKeyGroup(String chatKeyGroup) {
        this.chatKeyGroup = chatKeyGroup;
    }

    public int getIsLostChatKeyGroup() {
        return isLostChatKeyGroup;
    }

    /**
     * FriendDao addRooms
     * HandleSecureChatMessage handleGroupUpdate create
     * XChatMessageListener 907
     *
     * @param isLostChatKeyGroup
     */
    public void setIsLostChatKeyGroup(String userId, int isLostChatKeyGroup) {
        this.isLostChatKeyGroup = isLostChatKeyGroup;
        HandleSecureChatMessage.sendRequestChatKeyGroupMessage(true, userId);
    }

    public long getTopTime() {
        return topTime;
    }

    public void setTopTime(long topTime) {
        this.topTime = topTime;
    }

    public int getGroupStatus() {
        return groupStatus;
    }

    public void setGroupStatus(int groupStatus) {
        this.groupStatus = groupStatus;
    }

    public int getIsDevice() {
        return isDevice;
    }

    public void setIsDevice(int isDevice) {
        this.isDevice = isDevice;
    }

    public int getOfflineNoPushMsg() {
        return offlineNoPushMsg;
    }

    public void setOfflineNoPushMsg(int offlineNoPushMsg) {
        this.offlineNoPushMsg = offlineNoPushMsg;
    }

    public double getChatRecordTimeOut() {
        return chatRecordTimeOut;
    }

    public void setChatRecordTimeOut(double chatRecordTimeOut) {
        this.chatRecordTimeOut = chatRecordTimeOut;
    }

    public long getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(long downloadTime) {
        this.downloadTime = downloadTime;
    }

    public int getIsAtMe() {
        return isAtMe;
    }

    public void setIsAtMe(int isAtMe) {
        this.isAtMe = isAtMe;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomCreateUserId() {
        return roomCreateUserId;
    }

    public void setRoomCreateUserId(String roomCreateUserId) {
        this.roomCreateUserId = roomCreateUserId;
    }

    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickname) {
        this.nickName = nickname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimeCreate() {
        return timeCreate;
    }

    public void setTimeCreate(long timeCreate) {
        this.timeCreate = timeCreate;
    }

    public int getUnReadNum() {
        return unReadNum;
    }

    public void setUnReadNum(int unReadNum) {
        this.unReadNum = unReadNum;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimeSend() {
        return timeSend;
    }

    public void setTimeSend(long timeSend) {
        this.timeSend = timeSend;
    }

    public int getRoomFlag() {
        return roomFlag;
    }

    public void setRoomFlag(int roomFlag) {
        this.roomFlag = roomFlag;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getRoomMyNickName() {
        return roomMyNickName;
    }

    public void setRoomMyNickName(String roomMyNickName) {
        this.roomMyNickName = roomMyNickName;
    }

    public int getRoomTalkTime() {
        return roomTalkTime;
    }

    public void setRoomTalkTime(int roomTalkTime) {
        this.roomTalkTime = roomTalkTime;
    }

    /* 快捷方法，获取在好友列表中显示的名称 */
    public String getShowName() {
        if (!TextUtils.isEmpty(remarkName)) {
            return remarkName.trim();
        } else if (!TextUtils.isEmpty(nickName)) {
            return nickName.trim();
        } else {
            return "";
        }
    }
}
