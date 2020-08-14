package com.redchamber.auth;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.redchamber.bean.BadgeInfoBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 购买徽章
 */
public class BuyBadgeActivity extends BaseActivity {

    @BindView(R.id.tv_tip)
    TextView mTvTip;
    @BindView(R.id.btn_buy)
    Button mButton;

    private int status = -1;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_buy_badge;
    }

    @Override
    protected void initView() {
        mButton.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBadgeInfo();
    }

    @OnClick({R.id.iv_back, R.id.btn_buy})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_buy:
                if (0 == status) {
                    buyBadge();
                } else {
                    changeBadge();
                }
                break;
        }
    }

    public static void startActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, BuyBadgeActivity.class));
    }

    private void getBadgeInfo() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_GET_BADGE_INFO)
                .params(params)
                .build()
                .execute(new BaseCallback<BadgeInfoBean>(BadgeInfoBean.class) {

                    @Override
                    public void onResponse(ObjectResult<BadgeInfoBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            updateBadgeInfo(result.getData());
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtils.showToast(e.getMessage());
                    }
                });
    }

    private void updateBadgeInfo(BadgeInfoBean badgeInfoBean) {
        if (badgeInfoBean != null) {
            status = badgeInfoBean.status;//0无徽章 1有徽章(显示) 2徽章已隐藏
            mTvTip.setText(String.format("做一个不一样的小姐姐，精美徽章只需要%d红豆", badgeInfoBean.gold));
            mButton.setEnabled(true);
            if (status == 0) {
                mButton.setText("购买");
            } else if (1 == status) {
                mButton.setText("隐藏");
            } else if (2 == status) {
                mButton.setText("显示");
            }
        }
    }

    private void buyBadge() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_PAY_BUY_BADGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            ToastUtils.showToast("购买成功 ");
                            getBadgeInfo();
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtils.showToast(e.getMessage());
                    }
                });
    }

    private void changeBadge() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);
        if (1 == status) {
            params.put("type", "0");
        } else if (2 == status) {
            params.put("type", "1");
        }

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_PAY_CHANGE_BADGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            ToastUtils.showToast("设置成功 ");
                            getBadgeInfo();
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtils.showToast(e.getMessage());
                    }
                });
    }

}
