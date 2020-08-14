package com.sk.weichat.ui.circle;

import android.widget.ListView;

import com.sk.weichat.bean.circle.PublicMessage;

/**
 * 评论
 */
public class MessageEventComment {
    public final String event;
    public final String id;
    public final int type;
    public final String path;
    public final PublicMessage pbmessage;
    public final ListView view;
    public final int isAlloComment;


    public MessageEventComment(String event, String id, int isAlloComment, int type, String path, PublicMessage pbmessage, ListView view) {
        this.event = event;
        this.id = id;
        this.type = type;
        this.path = path;
        this.pbmessage = pbmessage;
        this.view = view;
        this.isAlloComment = isAlloComment;
    }
}