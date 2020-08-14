package com.sk.weichat.call;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.sk.weichat.R;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.ui.SplashActivity;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.WebViewActivity;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.LogUtils;
import com.sk.weichat.util.PreferenceUtils;

import java.util.Map;

public class QuickMeetingActivity extends BaseActivity {

    private boolean isNeedExecuteLogin;
    private String room;
    private boolean isVideo;

    public QuickMeetingActivity() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_meeting);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 判断本地登录状态
        int userStatus = LoginHelper.prepareUser(mContext, coreManager);
        switch (userStatus) {
            case LoginHelper.STATUS_USER_FULL:
            case LoginHelper.STATUS_USER_NO_UPDATE:
            case LoginHelper.STATUS_USER_TOKEN_OVERDUE:
                boolean isConflict = PreferenceUtils.getBoolean(this, Constants.LOGIN_CONFLICT, false);
                if (isConflict) {
                    isNeedExecuteLogin = true;
                }
                break;
            case LoginHelper.STATUS_USER_SIMPLE_TELPHONE:
                isNeedExecuteLogin = true;
                break;
            case LoginHelper.STATUS_NO_USER:
            default:
                isNeedExecuteLogin = true;
        }

        if (isNeedExecuteLogin) {// 需要先执行登录操作
            login();
            return;
        }

        parseParam();
        dial();
    }

    private void parseParam() {
        Intent intent = getIntent();
        LogUtils.log(intent);

        Uri data = intent.getData();
        if (data == null) {
            Log.e(TAG, "data异常");
            login();
            return;
        }
        Map<String, String> map = WebViewActivity.URLRequest(data.toString());
        room = map.get("room");
        if (TextUtils.isEmpty(room)) {
            login();
            return;
        }
        isVideo = TextUtils.equals(map.get("type"), "video");
    }

    private void dial() {
        Jitsi_connecting_second.start(this, room, coreManager.getSelf().getUserId(), isVideo ? 2 : 1);
        finish();
    }

    private void login() {
        startActivity(new Intent(mContext, SplashActivity.class));
        finish();
    }
}
