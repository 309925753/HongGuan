package com.sk.weichat.view.chatHolder;

import android.view.MotionEvent;
import android.view.View;

import com.sk.weichat.bean.message.ChatMessage;

public interface ChatHolderListener {

    void onItemClick(View v, AChatHolderInterface aChatHolderInterface, ChatMessage mdata);

    void onItemLongClick(View v, AChatHolderInterface aChatHolderInterface, ChatMessage mdata);

    void onItemLongClick(View v, MotionEvent event, AChatHolderInterface aChatHolderInterface, ChatMessage mdata);

    void onChangeInputText(String text);

    void onCompDownVoice(ChatMessage message);

    void onReplayClick(View v, AChatHolderInterface aChatHolderInterface, ChatMessage mdata);
}
