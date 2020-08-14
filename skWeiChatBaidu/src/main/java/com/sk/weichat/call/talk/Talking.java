package com.sk.weichat.call.talk;

import androidx.annotation.NonNull;

public class Talking {
    public String name;
    @NonNull
    public String userId;
    public double requestTime;
    public double talkLength;

    public Talking(String name, @NonNull String userId, double requestTime) {
        this.name = name;
        this.userId = userId;
        this.requestTime = requestTime;
    }

    @Override
    public String toString() {
        return "Talking{" +
                "name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                ", requestTime=" + requestTime +
                ", talkLength=" + talkLength +
                '}';
    }
}
