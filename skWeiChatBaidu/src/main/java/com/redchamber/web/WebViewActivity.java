package com.redchamber.web;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.redchamber.api.GlobalConstants;
import com.redchamber.lib.base.BaseActivity;
import com.sk.weichat.R;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * webView
 */
public class WebViewActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.mWebView)
    WebView mWebView;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_webview;
    }

    @Override
    protected void initView() {
        initWebView();
        if (getIntent() != null) {
            String title = getIntent().getStringExtra(GlobalConstants.KEY_TITLE);
            if (!TextUtils.isEmpty(title)) {
                mTvTitle.setText(title);
            }
            String url = getIntent().getStringExtra(GlobalConstants.KEY_URL);
            mWebView.loadUrl(url);
        }
    }

    @OnClick({R.id.iv_back})
    void onClick(View view) {
        finish();
    }

    private void initWebView() {
        /* 设置支持Js */
        mWebView.getSettings().setJavaScriptEnabled(true);
        /* 设置为true表示支持使用js打开新的窗口 */
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        /* 设置缓存模式 */
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.getSettings().setDomStorageEnabled(true);
        /* 设置为使用webview推荐的窗口 */
        mWebView.getSettings().setUseWideViewPort(true);
        /* 设置为使用屏幕自适配 */
        mWebView.getSettings().setLoadWithOverviewMode(true);
        /* 设置是否允许webview使用缩放的功能,我这里设为false,不允许 */
        mWebView.getSettings().setBuiltInZoomControls(false);
        /* 提高网页渲染的优先级 */
        mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        /* HTML5的地理位置服务,设置为true,启用地理定位 */
        mWebView.getSettings().setGeolocationEnabled(true);
        /* 设置可以访问文件 */
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.setWebViewClient(new WebViewClient());
    }

    public static void startWebActivity(Context context, String url, String title) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(GlobalConstants.KEY_URL, url);
        intent.putExtra(GlobalConstants.KEY_TITLE, title);
        context.startActivity(intent);
    }

}
