package com.sk.weichat.bean.event;

/**
 * Created by Administrator on 2018/1/13 0013.
 */

public class EventXMPPJoinGroupFailed {

    public final String roomJId;

    public EventXMPPJoinGroupFailed(String roomJId) {
        this.roomJId = roomJId;
    }
}
