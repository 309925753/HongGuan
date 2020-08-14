package com.sk.weichat.xmpp.listener;

import com.sk.weichat.bean.message.NewFriendMessage;

public interface NewFriendListener {

    // 新朋友消息发送状态的回调
    void onNewFriendSendStateChange(String toUserId, NewFriendMessage message, int messageState);

    /**
     * 新朋友消息来临时的回调
     * public static final int TYPE_SAYHELLO = 500; // 打招呼
     * public static final int TYPE_PASS = 501;     // 同意加好友
     * public static final int TYPE_FEEDBACK = 502; // 回话
     * public static final int TYPE_NEWSEE = 503;   // 新关注
     * public static final int TYPE_DELSEE = 504;   // 删除关注
     * public static final int TYPE_DELALL = 505;   // 彻底删除
     * public static final int TYPE_RECOMMEND = 506;// 新推荐好友
     * public static final int TYPE_BLACK = 507;    // 黑名单
     * public static final int TYPE_FRIEND = 508;   // 直接成为好友
     * public static final int TYPE_REFUSED = 509;  // 取消黑名单
     */
    boolean onNewFriend(NewFriendMessage message);
}
