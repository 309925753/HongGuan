package com.sk.weichat.ui.circle;

import android.widget.ListView;

import com.sk.weichat.bean.circle.Comment;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
public class MessageEventReply {
    public final String event;
    public final Comment comment;
    public final int id;
    public final String name;
    public ListView view;

    public MessageEventReply(String event, Comment comment, int id, String name, ListView view) {
        this.event = event;
        this.comment = comment;
        this.id = id;
        this.name = name;
        this.view = view;
    }
}