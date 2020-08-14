package com.sk.weichat.ui.dialog.money;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
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
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.FileUtil;
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
 * 支付宝、微信二维码弹窗
 */
public class ScanRechargeWxAlipayDialog extends Dialog {
    private ImageView mCloseIv;
    private TextView mTipTv;
    private TextView mConfirmTv, mCancelTv;
    private ImageView mCodeIv;
    private TextView mSaveTv;

    private Context mContext;
    private int type;
    private String money, url;
    private Bitmap bitmap;

    public ScanRechargeWxAlipayDialog(Context context, int type, String money, String url) {
        super(context, R.style.MyDialog);
        this.mContext = context;
        this.type = type;
        this.money = money;
        this.url = url;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_scan_rechrage_wx_alipay);
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
        mCodeIv = findViewById(R.id.code_iv);
        mSaveTv = findViewById(R.id.save_tv);
    }

    private void initData() {
        mTipTv.setText(mContext.getString(R.string.scan_recharge_wx_alipay_tip1
                , type == 1 ? mContext.getString(R.string.wechat) : mContext.getString(R.string.alipay)
                , money
                , CoreManager.requireSelf(MyApplication.getContext()).getAccount()));
        ImageLoadHelper.loadBitmapDontAnimateWithPlaceHolder(
                mContext,
                url,
                R.drawable.avatar_normal,
                R.drawable.avatar_normal,
                b -> {
                    bitmap = b;
                    mCodeIv.setImageBitmap(bitmap);
                }, e -> {
                    // 图片加载失败
                }
        );
    }

    private void initEvent() {
        mCloseIv.setOnClickListener(v -> dismiss());
        mSaveTv.setOnClickListener(v -> FileUtil.saveImageToGallery2(mContext, bitmap, true));
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
                        params.put("type", String.valueOf(type));// 支付方式 1.微信 2.支付宝 3.银行卡

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
                                            ScanRechargeWxAlipayDialog.this.dismiss();
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
