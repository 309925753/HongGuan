package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.sk.weichat.R;

public class ModifyFontSizeDialog extends Dialog {

    private ControlFontSize mControlFontSize;
    private WebView mWebView;
    private WebSettings mSettings;

    public ModifyFontSizeDialog(Context context, WebView webView) {
        super(context, R.style.Browser_Dialog);
        this.mWebView = webView;
        mSettings = mWebView.getSettings();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_dialog_for_font_size);
        setCanceledOnTouchOutside(true);
        initView();
        initListener();
    }

    public void initView() {
        mControlFontSize = findViewById(R.id.control_font);
        int fontSize = 1;
        if (mSettings.getTextSize() == WebSettings.TextSize.SMALLER) {
            fontSize = 0;
        } else if (mSettings.getTextSize() == WebSettings.TextSize.NORMAL) {
            fontSize = 1;
        } else if (mSettings.getTextSize() == WebSettings.TextSize.LARGER) {
            fontSize = 2;
        } else if (mSettings.getTextSize() == WebSettings.TextSize.LARGEST) {
            fontSize = 3;
        }
        mControlFontSize.setCurrentProgress(fontSize);

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        window.setGravity(Gravity.BOTTOM);
        // window.setWindowAnimations(R.style.BottomDialog_Animation);
    }

    private void initListener() {
        mControlFontSize.setOnPointResultListener(new ControlFontSize.OnPointResultListener() {
            @Override
            public void onPointResult(int position) {
                switch (position) {
                    case 0:
                        mSettings.setTextSize(WebSettings.TextSize.SMALLEST);
                        break;
                    case 1:
                        mSettings.setTextSize(WebSettings.TextSize.NORMAL);
                        break;
                    case 2:
                        mSettings.setTextSize(WebSettings.TextSize.LARGER);
                        break;
                    case 3:
                        mSettings.setTextSize(WebSettings.TextSize.LARGEST);
                        break;
                }
            }
        });
    }
}
