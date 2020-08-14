package com.sk.weichat.ui.other;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.User;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.SwitchButton;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

public class LifeCircleSettingsActivity extends BaseActivity {
    private Friend mFriend;
    private SwitchButton sbBanToFriend, sbBanFromFriend;
    private TextView tvBanToFriend;
    private TextView tvBanFromFriend;

    public static void start(Context ctx, Friend friend) {
        Intent intent = new Intent(ctx, LifeCircleSettingsActivity.class);
        intent.putExtra("userId", friend.getUserId());
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_life_circle_settings);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.live_circle_privacy_settings));

        String userId = getIntent().getStringExtra("userId");
        mFriend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), userId);

        sbBanToFriend = findViewById(R.id.sbBanToFriend);
        sbBanFromFriend = findViewById(R.id.sbBanFromFriend);

        tvBanToFriend = findViewById(R.id.tvBanToFriend);
        tvBanFromFriend = findViewById(R.id.tvBanFromFriend);

        requestData();
    }

    private void requestData() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mFriend.getUserId());

        HttpUtils.get().url(coreManager.getConfig().USER_GET_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<User>(User.class) {

                    @Override
                    public void onResponse(ObjectResult<User> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            if (result.getData().getSex() == 0) {
                                tvBanToFriend.setText(R.string.ban_to_female);
                                tvBanFromFriend.setText(R.string.ban_from_female);
                            }
                            sbBanFromFriend.setChecked(result.getData().getNotSeeHim() == 1);
                            sbBanToFriend.setChecked(result.getData().getNotLetSeeHim() == 1);
                            sbBanToFriend.postDelayed(() -> {
                                // 这控件有点恶心，切换动画后会回调，还没办法判断是不是手动切换，
                                sbBanFromFriend.setOnCheckedChangeListener((buttonView, isChecked) -> updateFromFriend(isChecked));
                                sbBanToFriend.setOnCheckedChangeListener((buttonView, isChecked) -> updateToFriend(isChecked));
                            }, 200);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(mContext);
                    }
                });

    }

    private void updateFromFriend(boolean ban) {
        updateSettings(ban ? 1 : -1, -1);
    }

    private void updateToFriend(boolean ban) {
        updateSettings(ban ? 1 : -1, 1);
    }

    private void updateSettings(int type, int shieldType) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", String.valueOf(type));
        params.put("shieldType", String.valueOf(shieldType));
        params.put("toUserId", mFriend.getUserId());

        HttpUtils.get().url(coreManager.getConfig().FILTER_USER_CIRCLE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            ToastUtil.showToast(mContext, R.string.update_success);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }
}
