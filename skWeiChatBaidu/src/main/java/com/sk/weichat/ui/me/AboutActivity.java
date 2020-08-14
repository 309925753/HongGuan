package com.sk.weichat.ui.me;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.weichat.AppConfig;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.other.PrivacyAgreeActivity;
import com.sk.weichat.util.DeviceInfoUtil;
import com.sk.weichat.util.UiUtils;
import com.sk.weichat.view.ShareDialog;

public class AboutActivity extends BaseActivity {
    private ShareDialog shareDialog;
    ShareDialog.OnShareDialogClickListener onShareDialogClickListener = new ShareDialog.OnShareDialogClickListener() {
        @Override
        public void tv1Click() {
        }

        @Override
        public void tv2Click() {
        }

        @Override
        public void tv3Click() {
            shareDialog.cancel();
        }
    };

    public void PrivacyAgree(View view) {
        if (UiUtils.isNormalClick(view) && !TextUtils.isEmpty(coreManager.getConfig().privacyPolicyPrefix)) {
            PrivacyAgreeActivity.startIntent(AboutActivity.this);
        }
    }

    public void Privacy(View view) {
        if (UiUtils.isNormalClick(view) && !TextUtils.isEmpty(coreManager.getConfig().privacyPolicyPrefix)) {
            PrivacyAgreeActivity.startPrivacy(AboutActivity.this, coreManager.getConfig().privacyPolicyPrefix + "privacy.html");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.about_us));
        ImageView ivRight = findViewById(R.id.iv_title_right);
        ivRight.setImageResource(R.mipmap.share_icon);
        ivRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareDialog = new ShareDialog(AboutActivity.this, onShareDialogClickListener);
                shareDialog.show();
            }
        });

        TextView versionTv = findViewById(R.id.version_tv);
        versionTv.setText(getString(R.string.app_name) + DeviceInfoUtil.getVersionName(mContext));

        TextView tvCompany = findViewById(R.id.company_tv);
        TextView tvCopyright = findViewById(R.id.copy_right_tv);

        tvCompany.setText(coreManager.getConfig().companyName);
        tvCopyright.setText(coreManager.getConfig().copyright);

        if (!AppConfig.isShiku()) {
            tvCompany.setVisibility(View.GONE);
            tvCopyright.setVisibility(View.GONE);
            ivRight.setVisibility(View.GONE);
        }
    }
}
