package com.sk.weichat.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.db.dao.UserDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 设置app_name号
 */
public class SetAccountActivity extends BaseActivity {
    private String mUserId, mUserName;
    private ImageView mAccountAvatarIv;
    private TextView mAccountNameTv;
    private EditText mAccountInputEt;
    private Button mAccountSureBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_account);
        mUserId = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
        mUserName = getIntent().getStringExtra(AppConstant.EXTRA_NICK_NAME);
        initActionBar();
        initView();
        initData();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.sk_account_set, getString(R.string.sk_account_code)));
    }

    private void initView() {
        TextView singleDesc = findViewById(R.id.sk_account_single_tv);
        singleDesc.setText(getString(R.string.sk_account_single, getString(R.string.sk_account_code)));
        mAccountAvatarIv = findViewById(R.id.a_avatar_iv);
        mAccountNameTv = findViewById(R.id.a_name_tv);
        mAccountInputEt = findViewById(R.id.a_input_et);
        mAccountSureBtn = findViewById(R.id.a_sure_btn);
//        mAccountSureBtn.setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());
        ButtonColorChange.colorChange(this, mAccountSureBtn);

    }

    private void initData() {
        AvatarHelper.getInstance().displayAvatar(mUserId, mAccountAvatarIv, true);
        mAccountNameTv.setText(mUserName);
        mAccountSureBtn.setOnClickListener(v -> {
            String account = mAccountInputEt.getText().toString();
            if (TextUtils.isEmpty(account)) {
                Toast.makeText(mContext, getString(R.string.name_connot_null), Toast.LENGTH_SHORT).show();
                return;
            }
            updateAccount(account);
        });
    }

    private void updateAccount(String account) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getSelfStatus(mContext).accessToken);
        params.put("account", account);

        HttpUtils.get().url(coreManager.getConfig().USER_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            ToastUtil.showToast(mContext, getString(R.string.update_success));
                            coreManager.getSelf().setAccount(account);
                            coreManager.getSelf().setSetAccountCount(1);
                            // 更新数据库
                            UserDao.getInstance().updateAccount(mUserId, account);

                            Intent intent = new Intent();
                            intent.putExtra(AppConstant.EXTRA_USER_ACCOUNT, account);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(SetAccountActivity.this);
                    }
                });
    }
}
