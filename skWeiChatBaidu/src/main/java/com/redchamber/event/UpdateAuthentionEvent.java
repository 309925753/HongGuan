package com.redchamber.event;

/**
 * Created by hy on 2020/4/22 0013.
 */
public class UpdateAuthentionEvent {

    public final String MessageData;

    public UpdateAuthentionEvent(String MessageData) {
        this.MessageData = MessageData;
    }
}
