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

import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.request.PhotoSettingRequest;
import com.sk.weichat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 设置红包照片
 */
public class SetPhotoCoinDialog extends Dialog {

    private Unbinder mBinder;

    @BindView(R.id.et_coin)
    EditText mEtCoin;

    private String mUrlList;

    private setPhotoListener mSetPhotoListener;

    public SetPhotoCoinDialog(Context context, String urlList) {
        super(context, R.style.BaseDialogStyle);
        this.mUrlList = urlList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_dialog_set_album_coin);
        mBinder = ButterKnife.bind(this);
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
    }

    private void albumSetting() {
        String icon = mEtCoin.getText().toString().trim();
        if (TextUtils.isEmpty(icon)) {
            ToastUtils.showToast("请输入红豆数量");
            return;
        }
        PhotoSettingRequest.getInstance().setPhoto(getContext(), mUrlList, "2", icon, new PhotoSettingRequest.SettingCallBack() {
            @Override
            public void onSuccess() {
                if (mSetPhotoListener != null) {
                    mSetPhotoListener.onSuccess();
                }
                ToastUtils.showToast("设置成功");
                dismiss();
            }

            @Override
            public void onFail(String error) {
                ToastUtils.showToast(error);
            }
        });
    }

    public interface setPhotoListener {

        void onSuccess();

    }

    public void setSetPhotoListener(setPhotoListener mSetPhotoListener) {
        this.mSetPhotoListener = mSetPhotoListener;
    }

}
