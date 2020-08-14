package com.sk.weichat.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by lixuuan
 * 屏蔽回车键
 */

public abstract class DisableEnterListener implements TextWatcher {

    private EditText mEditText;

    public DisableEnterListener(EditText editText) {
        mEditText = editText;
    }

    String str = "";

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        str = s.toString();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (count == 1 && s.charAt(s.length() - 1) == 10) {
            isEnter = true;
        } else {
            isEnter = false;
        }
    }

    boolean isEnter;

    @Override
    public void afterTextChanged(Editable s) {
        if (isEnter && str.length() > 0 && mEditText != null) {
            mEditText.setText(str);
            mEditText.setSelection(str.length() - 1);
            shield();
        }
    }

    public abstract void shield();
}
