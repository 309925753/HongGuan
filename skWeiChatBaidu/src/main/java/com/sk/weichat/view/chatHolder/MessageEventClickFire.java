package com.sk.weichat.view.chatHolder;

/**
 * Created by xuan on 2017/11/8 0008.
 * <p>
 * 1.点击了阅后即焚消息 , 单聊界面接收处理
 */
public class MessageEventClickFire {

    public final String event;
    public final String packedId;

    //    public MessageEventClickFire(String event) {
    //        this.event = event;
    //    }

    public MessageEventClickFire(String event, String packedId) {
        this.event = event;
        this.packedId = packedId;
    }
}
