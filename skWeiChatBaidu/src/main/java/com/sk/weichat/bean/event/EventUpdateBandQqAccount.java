package com.sk.weichat.bean.event;

public class EventUpdateBandQqAccount {
    public String result;
    public String msg;

    public EventUpdateBandQqAccount(String result, String ok) {
        this.result = result;
        this.msg = ok;
    }
}
