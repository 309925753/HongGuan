package com.redchamber.event;

/**
 * Created by hy on 2020/4/22 0013.
 */
public class UpdateGiftEvent {

    public final String MessageData;

    public UpdateGiftEvent(String MessageData) {
        this.MessageData = MessageData;
    }
}
