package com.sk.weichat.bean.message;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.User;
import com.sk.weichat.util.TimeUtils;

import java.io.Serializable;
import java.util.UUID;

/**
 * #define CALL_CENTER_USERID @"10000" //系统消息<br/>
 * #define FRIEND_CENTER_USERID @"10001" //新朋友 <br/>
 * #define BLOG_CENTER_USERID @"10003" //商务圈 <br/>
 * #define TEST_CENTER_USERID @"10004" //面试中心<br/>
 * 朋友中心
 */
@DatabaseTable
public class NewFriendMessage extends XmppMessage implements Cloneable, Serializable {
    private static final long serialVersionUID = -4231369003725583507L;
    @DatabaseField(generatedId = true)
    private int _id;
    @DatabaseField(canBeNull = false)
    private String ownerId; // 这个消息是属于哪个用户的
    @DatabaseField(canBeNull = false)
    private String userId; // 此新朋友消息针对的是哪个用户（一定是别人，不是自己）
    @DatabaseField // 默认值
    private int state = 20;
    @DatabaseField
    private String nickName;// 此新朋友消息针对的是哪个用户（一定是别人，不是自己）
    @DatabaseField
    private String content;// (打招呼的内容)
    @DatabaseField
    private boolean isRead;
    @DatabaseField
    private int companyId;// 此新朋友消息针对的是哪个用户,他的公司Id（一定是别人，不是自己）
    @DatabaseField(defaultValue = "0")
    private int unReadNum; // NewFriend 未读消息数量
    /* 下面5个只用于xmpp通讯时，生成json消息。在接受时，会自动转为上面的有效消息，所以不应该作为其他用途，不作为判断依据，不写入数据库 */
    private String fromUserId;
    private String fromUserName;
    private String toUserId;
    private String toUserName;
    private int fromCompanyId;

    public NewFriendMessage() {
    }

    public NewFriendMessage(String jsonData) {
        parserJsonData(jsonData);
    }

    /**
     * @param fromUser    应该是当前登陆的User
     * @param type
     * @param content
     * @param toUserId
     * @param toNickName
     * @param toCompanyId 此状态主要用于更新朋友关系。 发送加关注、加好友 此状态有效<br/>
     *                    发送打招呼 、解除关注、解除好友此状态无效，填Integer.MIN_VALUE<br/>
     *                    下面几个重载方法都遵循此原则<br/>
     * @return
     */
    public static NewFriendMessage createWillSendMessage(User fromUser, int type, String content, String toUserId, String toNickName, int toCompanyId) {
        String packetId = UUID.randomUUID().toString().replace("-", "");
        NewFriendMessage message = new NewFriendMessage();
        message.setPacketId(packetId);
        // 首先是传输协议的字段，
        message.setFromUserId(fromUser.getUserId());
        message.setFromUserName(fromUser.getNickName());
        message.setToUserId(toUserId);
        message.setToUserName(toNickName);
        message.setFromCompanyId(fromUser.getCompanyId());
        message.setType(type);
        message.setContent(content);
        message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        // 本地数据库状态
        message.setOwnerId(fromUser.getUserId());
        message.setUserId(toUserId);
        message.setNickName(toNickName);
        message.setCompanyId(toCompanyId);
        message.setRead(true);
        message.setMySend(true);
        return message;
    }

    // 多点登陆，本地收到其他端发送的新朋友消息，本地也需要创建NewFriendMessage并存入本地
    public static NewFriendMessage createLocalMessage(User fromUser, int type, String content, String toUserId, String toNickName) {
        String packetId = UUID.randomUUID().toString().replace("-", "");
        NewFriendMessage message = new NewFriendMessage();
        message.setPacketId(packetId);
        // 首先是传输协议的字段，
        message.setFromUserId(fromUser.getUserId());
        message.setFromUserName(fromUser.getNickName());
        message.setToUserId(toUserId);
        message.setToUserName(toNickName);
        message.setFromCompanyId(fromUser.getCompanyId());
        message.setType(type);
        message.setContent(content);
        message.setDoubleTimeSend(TimeUtils.sk_time_current_time_double());
        // 本地数据库状态
        message.setOwnerId(fromUser.getUserId());
        message.setUserId(toUserId);
        message.setNickName(toNickName);
        message.setRead(true);
        message.setMySend(true);
        return message;
    }

    public static NewFriendMessage createWillSendMessage(User fromUser, int type, String content, User toUser) {
        return createWillSendMessage(fromUser, type, content, toUser.getUserId(), toUser.getNickName(), toUser.getCompanyId());
    }

    public static NewFriendMessage createWillSendMessage(User fromUser, int type, String content, Friend toFriend) {
        return createWillSendMessage(fromUser, type, content, toFriend.getUserId(), toFriend.getNickName(), toFriend.getCompanyId());
    }

    public static NewFriendMessage createWillSendMessage(User fromUser, int type, String content, NewFriendMessage existMessage) {
        return createWillSendMessage(fromUser, type, content, existMessage.getUserId(), existMessage.getNickName(), existMessage.getCompanyId());
    }

    public int getUnReadNum() {
        return unReadNum;
    }

    public void setUnReadNum(int unReadNum) {
        this.unReadNum = unReadNum;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickname) {
        this.nickName = nickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public int getFromCompanyId() {
        return fromCompanyId;
    }

    public void setFromCompanyId(int fromCompanyId) {
        this.fromCompanyId = fromCompanyId;
    }

    @Override
    public NewFriendMessage clone() {
        NewFriendMessage n = null;
        try {
            n = (NewFriendMessage) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return n;
    }

    private void parserJsonData(String jsonData) {
        try {
            JSONObject jObject = JSON.parseObject(jsonData);
            userId = getStringValueFromJSONObject(jObject, "fromUserId");
            nickName = getStringValueFromJSONObject(jObject, "fromUserName");
            toUserId = getStringValueFromJSONObject(jObject, "toUserId");
            toUserName = getStringValueFromJSONObject(jObject, "toUserName");
            companyId = getIntValueFromJSONObject(jObject, "fromCompanyId");
            type = getIntValueFromJSONObject(jObject, "type");
            timeSend = getIntValueFromJSONObject(jObject, "timeSend");
            content = getStringValueFromJSONObject(jObject, "content");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * fromUserId, fromUserName, fromCompanyId, timeSend, content, type,
     *
     * @return
     */
    public String toJsonString() {
        String msg;
        JSONObject object = new JSONObject();
        object.put("fromUserId", this.fromUserId);
        object.put("fromUserName", this.fromUserName);
        object.put("toUserId", this.toUserId);
        object.put("toUserName", this.toUserName);
        object.put("fromCompanyId", this.fromCompanyId);
        object.put("type", this.type);
        if (!TextUtils.isEmpty(this.content)) {
            object.put("content", this.content);
        }
        object.put("messageId", this.packetId);
        object.put("timeSend", this.timeSend);
        msg = object.toString();
        return msg;
    }
}
