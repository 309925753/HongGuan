package com.sk.weichat.ui.trill;


import android.text.Editable;
import android.text.TextWatcher;

public abstract class CommInputListener implements TextWatcher {

    public int mLength = 0;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() < mLength) {
            clearReply();
        }
    }

    public void setLength(int length) {
        mLength = length;
    }

    public abstract void clearReply();
}
