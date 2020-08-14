package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import com.sk.weichat.R;

public class MatchKeyWordEditDialog extends Dialog {

    private EditText mKeyWordEdit;
    private TextView mMatchNumTv;

    private WebView mWebView;

    public MatchKeyWordEditDialog(Context context, WebView webView) {
        super(context, R.style.Browser_Dialog);
        this.mWebView = webView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_dialog_for_match_keyword_browser);
        setCanceledOnTouchOutside(false);
        initView();
        initListener();
        initEvent();
    }

    public void initView() {
        mKeyWordEdit = findViewById(R.id.keyword_et);
        mMatchNumTv = findViewById(R.id.count_tv);

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        window.setGravity(Gravity.BOTTOM);
        // window.setWindowAnimations(R.style.BottomDialog_Animation);
    }

    private void initListener() {
        mKeyWordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mWebView.findAllAsync(mKeyWordEdit.getText().toString());
            }
        });

        mWebView.setFindListener((activeMatchOrdinal, numberOfMatches, isDoneCounting) -> {
            if (isDoneCounting) {
                if (numberOfMatches != 0) {
                    mMatchNumTv.setText(String.format("%d/%d", (activeMatchOrdinal + 1), numberOfMatches));
                } else {
                    mMatchNumTv.setText("0/0");
                }
            }
        });
    }

    private void initEvent() {
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.clearMatches();
                dismiss();
            }
        });

        findViewById(R.id.up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.findNext(false);
            }
        });

        findViewById(R.id.down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.findNext(true);
            }
        });
    }
}
