package com.sk.im.audio;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
public class MessageEventVoice {
    public final String event;
    public final long timelen;

    public MessageEventVoice(String event, long timelen) {
        this.event = event;
        this.timelen = timelen;
    }
}