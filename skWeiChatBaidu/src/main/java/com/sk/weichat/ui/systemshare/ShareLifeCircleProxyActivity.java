package com.sk.weichat.ui.systemshare;

import android.content.Intent;
import android.os.Bundle;

import com.sk.weichat.R;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.ui.SplashActivity;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.LogUtils;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.ToastUtil;

public class ShareLifeCircleProxyActivity extends BaseActivity {
    private boolean isNeedExecuteLogin;

    public ShareLifeCircleProxyActivity() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            startActivity(new Intent(mContext, SplashActivity.class));
            finish();
            return;
        }

        Intent intent = getIntent();
        LogUtils.log(intent);
        if (ShareUtil.isImage(intent)) {
            ShareShuoshuoActivity.start(this, intent);
        } else if (ShareUtil.isVideo(intent)) {
            ShareVideoActivity.start(this, intent);
        } else if (ShareUtil.isFile(intent)) {
            ShareFileActivity.start(this, intent);
        } else if (ShareUtil.isText(intent)) {
            ShareShuoshuoActivity.start(this, intent);
        } else {
            ToastUtil.showToast(this, R.string.tip_share_type_not_supported);
        }
        finish();
    }
}
