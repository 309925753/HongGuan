package com.redchamber.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

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
 * 相册隐私
 */
public class PhotoPrivacyDialog extends Dialog {

    private Unbinder mBinder;

    @BindView(R.id.tv_open)
    TextView mTvOpen;
    @BindView(R.id.tv_lock)
    TextView mTvLock;
    @BindView(R.id.tv_verify)
    TextView mTvVerify;

    private int mType;

    public PhotoPrivacyDialog(Context context, int type) {
        super(context, R.style.BaseDialogStyle);
        this.mType = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_dialog_photo_privacy);
        mBinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        setCanceledOnTouchOutside(true);

        initView();
    }

    @OnClick({R.id.tv_open, R.id.tv_lock, R.id.tv_verify, R.id.tv_cancel})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_open:
                albumSetting(0);
                break;
            case R.id.tv_lock:
                albumSetting(2);
                break;
            case R.id.tv_verify:
                albumSetting(1);
                break;
            case R.id.tv_cancel:
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

    private void initView() {
        if (1 == mType) {
            mTvOpen.setTextColor(getContext().getResources().getColor(R.color.color_333333));
            mTvLock.setTextColor(getContext().getResources().getColor(R.color.color_333333));
            mTvVerify.setTextColor(getContext().getResources().getColor(R.color.color_FB719A));
        } else if (2 == mType) {
            mTvOpen.setTextColor(getContext().getResources().getColor(R.color.color_333333));
            mTvLock.setTextColor(getContext().getResources().getColor(R.color.color_FB719A));
            mTvVerify.setTextColor(getContext().getResources().getColor(R.color.color_333333));
        } else {
            mTvOpen.setTextColor(getContext().getResources().getColor(R.color.color_FB719A));
            mTvLock.setTextColor(getContext().getResources().getColor(R.color.color_333333));
            mTvVerify.setTextColor(getContext().getResources().getColor(R.color.color_333333));
        }
    }

    private void albumSetting(int type) {
        if (mType == type) {
            dismiss();
            return;
        }
        if (2 == type) {
            SetAlbumCoinDialog setAlbumCoinDialog = new SetAlbumCoinDialog(getContext());
            setAlbumCoinDialog.show();
            dismiss();
            return;
        }

        AlbumSettingRequest.getInstance().setAlbum(getContext(), type, "", new AlbumSettingRequest.AlbumSetCallBack() {
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
