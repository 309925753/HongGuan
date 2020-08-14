package com.redchamber.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.redchamber.event.UpdateCityOnlineEvent;
import com.redchamber.event.UpdateUserInfo;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.request.AlbumSettingRequest;
import com.sk.weichat.R;
import com.sk.weichat.util.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * 设置相册红豆金额
 */
public class SetAlbumCoinDialog extends Dialog {

    private Unbinder mBinder;

    @BindView(R.id.et_coin)
    EditText mEtCoin;

    public SetAlbumCoinDialog(Context context) {
        super(context, R.style.BaseDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_dialog_set_album_coin);
        mBinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        setCanceledOnTouchOutside(true);

    }

    @OnClick({R.id.btn_confirm, R.id.iv_close})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                albumSetting();
                break;
            case R.id.iv_close:
                dismiss();
                break;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mBinder != null) {
            mBinder.unbind();
        }
        EventBus.getDefault().unregister(this);
    }

    private void albumSetting() {
        String icon = mEtCoin.getText().toString().trim();
        if (TextUtils.isEmpty(icon)) {
            ToastUtils.showToast("请输入红豆数量");
            return;
        }
        AlbumSettingRequest.getInstance().setAlbum(getContext(), 2, icon, new AlbumSettingRequest.AlbumSetCallBack() {
            @Override
            public void onSuccess() {
                ToastUtils.showToast("设置成功");
                EventBus.getDefault().post(new UpdateUserInfo());
                dismiss();
            }

            @Override
            public void onFail(String error) {
                ToastUtils.showToast(error);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onMessageEvent(UpdateUserInfo event) {

    }

}
