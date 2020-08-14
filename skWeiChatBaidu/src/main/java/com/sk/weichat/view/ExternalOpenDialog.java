package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.sk.weichat.R;
import com.sk.weichat.util.AppUtils;
import com.sk.weichat.util.ScreenUtil;

public class ExternalOpenDialog extends Dialog implements OnClickListener {
    private Context mContent;
    private String mUrl;

    public ExternalOpenDialog(Context context, String url) {
        super(context, R.style.BottomDialog);
        this.mContent = context;
        this.mUrl = url;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_dialog_for_external_open);
        setCanceledOnTouchOutside(true);

        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        // x/y坐标
        // lp.x = 100;
        // lp.y = 100;
        lp.width = ScreenUtil.getScreenWidth(getContext());
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.BOTTOM);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        initEvent();
    }

    private void initEvent() {
        findViewById(R.id.qq).setOnClickListener(this);
        findViewById(R.id.uc).setOnClickListener(this);
        findViewById(R.id.op).setOnClickListener(this);
        findViewById(R.id.local).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.qq:
                if ((AppUtils.isAppInstalled(mContent, "com.tencent.mtt"))) {
                    // QQ浏览器打开
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(mUrl);
                    intent.setData(content_url);
                    intent.setClassName("com.tencent.mtt", "com.tencent.mtt.MainActivity");
                    mContent.startActivity(intent);
                } else {
                    Toast.makeText(mContent, R.string.tip_no_qq_browser, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.uc:
                if ((AppUtils.isAppInstalled(mContent, "com.uc.browser"))) {
                    // QQ浏览器打开
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(mUrl);
                    intent.setData(content_url);
                    intent.setClassName("com.uc.browser", "com.uc.browser.ActivityUpdate");
                } else {
                    Toast.makeText(mContent, R.string.tip_no_uc_browser, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.op:
                if ((AppUtils.isAppInstalled(mContent, "com.opera.mini.android"))) {
                    // Opera浏览器打开
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(mUrl);
                    intent.setData(content_url);
                    intent.setClassName("com.opera.mini.android", "com.opera.mini.android.Browser");
                    mContent.startActivity(intent);
                } else {
                    Toast.makeText(mContent, R.string.tip_no_opera_browser, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.local:
                // 本地浏览器打开
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(mUrl);
                intent.setData(content_url);
                try {
                    mContent.startActivity(intent);
                } catch (Exception ignore) {
                    // 可能url有问题，比如是file协议的，
                    // 也可能根本没有浏览器，
                    Toast.makeText(mContent, R.string.tip_no_local_browser, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
