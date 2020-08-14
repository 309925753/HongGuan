package com.sk.weichat.xmpp.listener;

public interface MucListener {
    // 群组被删除
    void onDeleteMucRoom(String toUserId);

    // 我被踢出群组
    void onMyBeDelete(String toUserId);

    // 群组内昵称发生改变
    void onNickNameChange(String toUserId, String changedUserId, String changedName);

    // 我被禁言了
    void onMyVoiceBanned(String toUserId, int time);
}
