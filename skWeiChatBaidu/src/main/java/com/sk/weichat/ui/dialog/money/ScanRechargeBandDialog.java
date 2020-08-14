package com.sk.weichat.ui.dialog.money;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.redpacket.ScanRecharge;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.ScreenUtil;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.SelectionFrame;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 银行卡充值弹窗
 */
public class ScanRechargeBandDialog extends Dialog {
    private ImageView mCloseIv;
    private TextView mTipTv;
    private TextView mConfirmTv, mCancelTv;
    private TextView mTv1, mTv2, mTv3;
    private ImageView mIv1, mIv2, mIv3;

    private Context mContext;
    private String name, card, bandName;
    private String money;

    public ScanRechargeBandDialog(Context context, String name, String card, String bandName, String money) {
        super(context, R.style.MyDialog);
        this.mContext = context;
        this.name = name;
        this.card = card;
        this.bandName = bandName;
        this.money = money;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_scan_rechrage_band);
        initView();
        initData();
        initEvent();

        // 屏蔽back键
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = (int) (ScreenUtil.getScreenWidth(getContext()) * 0.9);
            window.setAttributes(lp);
            window.setGravity(Gravity.CENTER);
        }
    }

    private void initView() {
        mCloseIv = findViewById(R.id.close_iv);
        mTipTv = findViewById(R.id.tip_tv);
        mConfirmTv = findViewById(R.id.confirm);
        mCancelTv = findViewById(R.id.cancel);
        mTv1 = findViewById(R.id.tv1);
        mTv2 = findViewById(R.id.tv2);
        mTv3 = findViewById(R.id.tv3);
        mIv1 = findViewById(R.id.iv1);
        mIv2 = findViewById(R.id.iv2);
        mIv3 = findViewById(R.id.iv3);
    }

    private void initData() {
        mTipTv.setText(mContext.getString(R.string.scan_recharge_band_tip1
                , CoreManager.requireSelf(MyApplication.getContext()).getAccount()));
        mTv1.setText(mContext.getString(R.string.scan_recharge_band_body1, name));
        mTv2.setText(mContext.getString(R.string.scan_recharge_band_body2, card));
        mTv3.setText(mContext.getString(R.string.scan_recharge_band_body3, bandName));
    }

    private void initEvent() {
        mCloseIv.setOnClickListener(v -> dismiss());
        mIv1.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                ClipData clipData = ClipData.newPlainText("Label", name);
                clipboardManager.setPrimaryClip(clipData);
                ToastUtil.showToast(mContext, mContext.getString(R.string.tip_copied_to_clipboard));
            }
        });
        mIv2.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                ClipData clipData = ClipData.newPlainText("Label", card);
                clipboardManager.setPrimaryClip(clipData);
                ToastUtil.showToast(mContext, mContext.getString(R.string.tip_copied_to_clipboard));
            }
        });
        mIv3.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                ClipData clipData = ClipData.newPlainText("Label", bandName);
                clipboardManager.setPrimaryClip(clipData);
                ToastUtil.showToast(mContext, mContext.getString(R.string.tip_copied_to_clipboard));
            }
        });
        mTipTv.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                ClipData clipData = ClipData.newPlainText("Label", CoreManager.requireSelf(MyApplication.getContext()).getAccount());
                clipboardManager.setPrimaryClip(clipData);
                ToastUtil.showToast(mContext, mContext.getString(R.string.label_communication) + mContext.getString(R.string.tip_copied_to_clipboard));
            }
        });
        mConfirmTv.setOnClickListener(v -> recharge());
        mCancelTv.setOnClickListener(v -> dismiss());
    }

    private void recharge() {
        SelectionFrame selectionFrame = new SelectionFrame(mContext);
        selectionFrame.setSomething(null, mContext.getString(R.string.already_pay_ask),
                null, mContext.getString(R.string.already_pay), new SelectionFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                        DialogHelper.showDefaulteMessageProgressDialog(mContext);
                        Map<String, String> params = new HashMap<>();
                        params.put("money", money);
                        params.put("type", String.valueOf(3));// 支付方式 1.微信 2.支付宝 3.银行卡

                        HttpUtils.get().url(CoreManager.requireConfig(mContext).MANUAL_PAY_RECHARGE)
                                .params(params)
                                .build()
                                .execute(new BaseCallback<ScanRecharge>(ScanRecharge.class) {

                                    @Override
                                    public void onResponse(ObjectResult<ScanRecharge> result) {
                                        DialogHelper.dismissProgressDialog();
                                        if (Result.checkSuccess(mContext, result)) {
                                            ToastUtil.showToast(mContext, mContext.getString(R.string.wait_server_notify));
                                            selectionFrame.dismiss();
                                            ScanRechargeBandDialog.this.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onError(Call call, Exception e) {
                                        DialogHelper.dismissProgressDialog();
                                        ToastUtil.showErrorNet(mContext);
                                    }
                                });
                    }
                });
        selectionFrame.setCancelable(false);
        selectionFrame.setAutoDismiss(false);
        selectionFrame.show();
    }
}
