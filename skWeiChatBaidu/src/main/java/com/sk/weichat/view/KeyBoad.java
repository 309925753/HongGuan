package com.sk.weichat.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.sk.weichat.R;

import java.lang.reflect.Method;

/**
 * 收付款、转账输入键盘
 */
public class KeyBoad extends PopupWindow {
    private Context context;
    private View decoview;
    private View view;
    private EditText editText;

    private int[] buttonsNum = {
            R.id.button00,
            R.id.button01,
            R.id.button02,
            R.id.button03,
            R.id.button04,
            R.id.button05,
            R.id.button06,
            R.id.button07,
            R.id.button08,
            R.id.button09,
    };

    public KeyBoad(Context context, View decoview, EditText editText) {
        super(context);
        this.context = context;
        this.decoview = decoview;
        this.editText = editText;
        if (context == null || decoview == null) {
            return;
        }
        initConfig();
        initView();
    }

    private void initView() {
        view = LayoutInflater.from(context).inflate(R.layout.kyebord, null);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        initKeyView(view);
        setContentView(view);
    }

    private void initKeyView(View view) {
        // 数字键设置点击监听
        for (int i = 0; i < buttonsNum.length; i++) {
            final Button button = view.findViewById(buttonsNum[i]);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int curSelection = editText.getSelectionStart();
                    int length = editText.getText().toString().length();
                    if (curSelection < length) {
                        String content = editText.getText().toString();
                        if (!content.substring(0, curSelection).endsWith(".")) {
                            editText.setText(content.substring(0, curSelection) + button.getText() + content.subSequence(curSelection, length));
                            editText.setSelection(curSelection + 1);
                        }
                    } else {
                        String content = editText.getText().toString();
                        int position = content.lastIndexOf(".");

                        if (position == -1 || content.length() - position < 3) {
                            editText.setText(content + button.getText());
                            editText.setSelection(editText.getText().toString().length());
                        }
                    }
                }

            });
        }
        view.findViewById(R.id.dropdownLl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (decoview != null) {
                    dismiss();
                }
            }
        });

        //小数点按键设置点击监听
        view.findViewById(R.id.button_dot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().trim().contains(".")) {
                    int curSelection = editText.getSelectionStart();
                    int length = editText.getText().toString().length();
                    if (curSelection < length) {
                        String content = editText.getText().toString();
                        if (content.lastIndexOf(".") >= 2) {
                            editText.setText(content.substring(0, curSelection) + "." + content.subSequence(curSelection, curSelection + 2));
                            editText.setSelection(curSelection + 1);
                        } else {
                            editText.setText(content.substring(0, curSelection) + "." + content.subSequence(curSelection, curSelection + 1) + "0");
                            editText.setSelection(curSelection + 1);
                        }

                    } else {
                        editText.setText(editText.getText().toString() + ".");
                        editText.setSelection(editText.getText().toString().length());
                    }
                }
            }
        });

        //删除键设置点击监听
        view.findViewById(R.id.button_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int length = editText.getText().toString().length();
                int curSelection = editText.getSelectionStart();
                if (length > 0 && curSelection > 0 && curSelection <= length) {
                    String content = editText.getText().toString();
                    editText.setText(content.substring(0, curSelection - 1) + content.subSequence(curSelection, length));
                    editText.setSelection(curSelection - 1);
                }
            }
        });
    }

    private void initConfig() {
        setOutsideTouchable(false);
        setFocusable(false);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        try {
            stopKeybord();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshKeyboardOutSideTouchable(boolean isTouchable) {
        setOutsideTouchable(isTouchable);
        if (!isTouchable) {
            show();
        } else {
            dismiss();
        }
    }

    public void stopKeybord() throws Exception {
        if (editText == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 10) {
            Class<EditText> c = EditText.class;
            Method method = c.getMethod("setShowSoftInputOnFocus", boolean.class);
            method.setAccessible(true);
            method.invoke(editText, false);
        }
    }

    public void show() {
        if (!isShowing() && decoview != null) {
            this.showAtLocation(decoview, Gravity.BOTTOM, 0, 0);
        }
    }

    public void resourceGl() {
        context = null;
        decoview = null;
    }
}
