package com.sk.weichat.bean.event;

/**
 * Created by Administrator on 2017/10/31 0031.
 */

public class MessageVideoFile {
    public final int timelen;
    public final long length;
    public final String path;

    public MessageVideoFile(int timelen, long length, String path) {
        this.timelen = timelen;
        this.length = length;
        this.path = path;
    }
}
