package com.sk.weichat.bean;

import android.text.TextUtils;

import com.sk.weichat.Reporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/4/29.
 */
public class PrivacySetting {

    private int allowAtt;
    private int allowGreet;
    private double chatRecordTimeOut;
    private int closeTelephoneFind;
    private int openService;
    // 消息漫游时长
    private double chatSyncTimeLen;
    // 需要好友验证
    private int friendsVerify;
    private int phoneSearch;
    private int nameSearch;
    private String friendFromList;
    // 消息加密传输
    private int isEncrypt;
    // 消息加密传输
    private int authSwitch; //授权开关  0-需要授权   1-不需要授权
    // 消息来时振动
    private int isVibration;
    // 让对方知道我正在输入...
    private int isTyping;
    private int isKeepalive;
    // 使用谷歌地图
    private int isUseGoogleMap;
    // 支持多设备登录
    private int multipleDevices;
    // 下面五个允许相关的设置是通过反射赋值的，
    private int showLastLoginTime;
    private int showTelephone;
    private int allowMsg;
    private int allowCall;
    private int allowJoinRoom;
    private List<String> throughoutShowLastLoginTimeList;// 始终显示上线时间的用户列表
    private List<String> noShowLastLoginTimeList;// 始终不显示上线时间的用户列表
    private List<String> throughoutShowTelephoneList;// 始终显示我的手机号码的用户列表
    private List<String> noShowTelephoneList;// 始终不显示我的手机号码的用户列表
    private List<String> throughoutAllowMsgList;// 始终允许发送消息用户列表
    private List<String> noAllowMsgList;// 始终不允许发送消息用户列表
    private List<String> throughoutAllowCallList;// 始终允许发送音视频通话的用户列表
    private List<String> noAllowCallList;// 始终不允许音视频通话的用户列表
    private List<String> throughoutAllowJoinRoomList;// 始终允许加群的用户列表
    private List<String> noAllowJoinRoomList;// 始终不允许加群的用户列表
    private int isSkidRemoveHistoryMsg; // 首页侧滑删除服务器聊天记录  1：开启 0：关闭
    private int isShowMsgState; // 首页侧滑删除服务器聊天记录  1：开启 0：关闭
    private int isOpenPrivacyPosition; // 是否开启个人位置相关服务  1：开启 0：关闭

    public List<String> getThroughoutShowLastLoginTimeList() {
        return throughoutShowLastLoginTimeList;
    }

    public void setThroughoutShowLastLoginTimeList(List<String> throughoutShowLastLoginTimeList) {
        this.throughoutShowLastLoginTimeList = throughoutShowLastLoginTimeList;
    }

    public List<String> getNoShowLastLoginTimeList() {
        return noShowLastLoginTimeList;
    }

    public void setNoShowLastLoginTimeList(List<String> noShowLastLoginTimeList) {
        this.noShowLastLoginTimeList = noShowLastLoginTimeList;
    }

    public List<String> getThroughoutShowTelephoneList() {
        return throughoutShowTelephoneList;
    }

    public void setThroughoutShowTelephoneList(List<String> throughoutShowTelephoneList) {
        this.throughoutShowTelephoneList = throughoutShowTelephoneList;
    }

    public List<String> getNoShowTelephoneList() {
        return noShowTelephoneList;
    }

    public void setNoShowTelephoneList(List<String> noShowTelephoneList) {
        this.noShowTelephoneList = noShowTelephoneList;
    }

    public List<String> getThroughoutAllowMsgList() {
        return throughoutAllowMsgList;
    }

    public void setThroughoutAllowMsgList(List<String> throughoutAllowMsgList) {
        this.throughoutAllowMsgList = throughoutAllowMsgList;
    }

    public List<String> getNoAllowMsgList() {
        return noAllowMsgList;
    }

    public void setNoAllowMsgList(List<String> noAllowMsgList) {
        this.noAllowMsgList = noAllowMsgList;
    }

    public List<String> getThroughoutAllowCallList() {
        return throughoutAllowCallList;
    }

    public void setThroughoutAllowCallList(List<String> throughoutAllowCallList) {
        this.throughoutAllowCallList = throughoutAllowCallList;
    }

    public List<String> getNoAllowCallList() {
        return noAllowCallList;
    }

    public void setNoAllowCallList(List<String> noAllowCallList) {
        this.noAllowCallList = noAllowCallList;
    }

    public List<String> getThroughoutAllowJoinRoomList() {
        return throughoutAllowJoinRoomList;
    }

    public void setThroughoutAllowJoinRoomList(List<String> throughoutAllowJoinRoomList) {
        this.throughoutAllowJoinRoomList = throughoutAllowJoinRoomList;
    }

    public List<String> getNoAllowJoinRoomList() {
        return noAllowJoinRoomList;
    }

    public void setNoAllowJoinRoomList(List<String> noAllowJoinRoomList) {
        this.noAllowJoinRoomList = noAllowJoinRoomList;
    }

    public int getShowLastLoginTime() {
        return showLastLoginTime;
    }

    public void setShowLastLoginTime(int showLastLoginTime) {
        this.showLastLoginTime = showLastLoginTime;
    }

    public int getShowTelephone() {
        return showTelephone;
    }

    public void setShowTelephone(int showTelephone) {
        this.showTelephone = showTelephone;
    }

    public int getAllowMsg() {
        return allowMsg;
    }

    public void setAllowMsg(int allowMsg) {
        this.allowMsg = allowMsg;
    }

    public int getAllowCall() {
        return allowCall;
    }

    public void setAllowCall(int allowCall) {
        this.allowCall = allowCall;
    }

    public int getAllowJoinRoom() {
        return allowJoinRoom;
    }

    public void setAllowJoinRoom(int allowJoinRoom) {
        this.allowJoinRoom = allowJoinRoom;
    }

    public double getChatSyncTimeLen() {
        return chatSyncTimeLen;
    }

    public void setChatSyncTimeLen(double chatSyncTimeLen) {
        this.chatSyncTimeLen = chatSyncTimeLen;
    }

    public int getFriendsVerify() {
        return friendsVerify;
    }

    public void setFriendsVerify(int friendsVerify) {
        this.friendsVerify = friendsVerify;
    }

    public int getIsKeepalive() {
        return isKeepalive;
    }

    public void setIsKeepalive(int isKeepalive) {
        this.isKeepalive = isKeepalive;
    }

    public int getIsEncrypt() {
        return isEncrypt;
    }

    public void setIsEncrypt(int isEncrypt) {
        this.isEncrypt = isEncrypt;
    }

    public int getIsVibration() {
        return isVibration;
    }

    public void setIsVibration(int isVibration) {
        this.isVibration = isVibration;
    }

    public int getIsTyping() {
        return isTyping;
    }

    public void setIsTyping(int isTyping) {
        this.isTyping = isTyping;
    }

    public int getIsUseGoogleMap() {
        return isUseGoogleMap;
    }

    public void setIsUseGoogleMap(int isUseGoogleMap) {
        this.isUseGoogleMap = isUseGoogleMap;
    }

    public int getMultipleDevices() {
        return multipleDevices;
    }

    public void setMultipleDevices(int multipleDevices) {
        this.multipleDevices = multipleDevices;
    }

    public int getPhoneSearch() {
        return phoneSearch;
    }

    public void setPhoneSearch(int phoneSearch) {
        this.phoneSearch = phoneSearch;
    }

    public int getNameSearch() {
        return nameSearch;
    }

    public void setNameSearch(int nameSearch) {
        this.nameSearch = nameSearch;
    }

    public String getFriendFromList() {
        return friendFromList;
    }

    public void setFriendFromList(String friendFromList) {
        this.friendFromList = friendFromList;
    }

    public List<Integer> getFriendFromListArray() {
        if (TextUtils.isEmpty(friendFromList)) {
            return new ArrayList<>();
        }
        // 服务器改了数据结构，兼容之前这个参数是数组的格式，
        if (friendFromList.startsWith("[")) {
            friendFromList = friendFromList.substring(1);
        }
        if (friendFromList.endsWith("]")) {
            friendFromList = friendFromList.substring(0, friendFromList.length() - 1);
        }
        String[] split = friendFromList.split(",");
        List<Integer> ret = new ArrayList<>(split.length);
        for (String s : split) {
            try {
                ret.add(Integer.valueOf(s));
            } catch (Exception e) {
                Reporter.unreachable(e);
            }
        }
        return ret;
    }

    public void setFriendFromListArray(List<Integer> friendFromList) {
        this.friendFromList = TextUtils.join(",", friendFromList);
    }

    public int getAuthSwitch() {
        return authSwitch;
    }

    public void setAuthSwitch(int authSwitch) {
        this.authSwitch = authSwitch;
    }

    public int getIsSkidRemoveHistoryMsg() {
        return isSkidRemoveHistoryMsg;
    }

    public void setIsSkidRemoveHistoryMsg(int isSkidRemoveHistoryMsg) {
        this.isSkidRemoveHistoryMsg = isSkidRemoveHistoryMsg;
    }

    public int getIsShowMsgState() {
        return isShowMsgState;
    }

    public void setIsShowMsgState(int isShowMsgState) {
        this.isShowMsgState = isShowMsgState;
    }

    public int getIsOpenPrivacyPosition() {
        return isOpenPrivacyPosition;
    }

    public void setIsOpenPrivacyPosition(int isOpenPrivacyPosition) {
        this.isOpenPrivacyPosition = isOpenPrivacyPosition;
    }
}
