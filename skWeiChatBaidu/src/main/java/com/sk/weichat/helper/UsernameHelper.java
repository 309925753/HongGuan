package com.sk.weichat.helper;

import android.app.Activity;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sk.weichat.AppConfig;
import com.sk.weichat.R;

public class UsernameHelper {
    private UsernameHelper() {
    }

    public static void initSearchLabel(TextView view, AppConfig config) {
        if (config.cannotSearchByNickName) {
            if (config.registerUsername) {
                view.setText(R.string.username);
            } else {
                view.setText(R.string.phone_number);
            }
        } else {
            if (config.registerUsername) {
                view.setText(R.string.nickname_or_username);
            } else {
                view.setText(R.string.nickname_or_phone_number);
            }
        }
    }

    public static void initSearchEdit(EditText view, AppConfig config) {
        if (config.cannotSearchByNickName) {
            if (config.registerUsername) {
                view.setHint(R.string.hint_input_username);
            } else {
                view.setHint(R.string.hint_input_phone_number);
            }
        } else {
            if (config.registerUsername) {
                view.setHint(R.string.hint_input_nickname_or_username);
            } else {
                view.setHint(R.string.hint_input_nickname_or_phone_number);
            }
        }
    }

    public static void initEditText(EditText view, boolean registerUsername) {
        if (registerUsername) {
            view.setHint(R.string.hint_input_username);
            view.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            view.setKeyListener(new NumberKeyListener() {
                @NonNull
                @Override
                protected char[] getAcceptedChars() {
                    return view.getContext().getString(R.string.alphabet_and_number).toCharArray();
                }

                @Override
                public int getInputType() {
                    return InputType.TYPE_CLASS_TEXT;
                }
            });
        } else {
            view.setHint(R.string.hint_input_phone_number);
            view.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }

    public static void initTextView(TextView view, boolean registerUsername) {
        if (registerUsername) {
            view.setText(R.string.username);
        } else {
            view.setText(R.string.phone_number);
        }
    }

    /**
     * 检测用户名或者手机号是否合法，
     * 同时处理提示，
     *
     * @return 返回true表示text合法，
     */
    public static boolean verify(Activity ctx, String text, boolean registerUsername) {
        boolean ret = true;
        String tip = null;
        if (TextUtils.isEmpty(text)) {
            ret = false;
            if (registerUsername) {
                tip = ctx.getString(R.string.tip_username_empty);
            } else {
                tip = ctx.getString(R.string.tip_phone_number_empty);
            }
        }
        if (!registerUsername) {
            // 手机号不检测，
        } else {
            if (text.length() > 10) {
                ret = false;
                tip = ctx.getString(R.string.tip_username_too_long);
            }
            // 没检测是否字母数字，因为EditText就限定死了，
        }
        if (!ret) {
            DialogHelper.tip(ctx, tip);
        }
        return ret;
    }
}
