package com.sk.weichat.pay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qrcode.utils.CommonUtils;
import com.sk.weichat.AppConfig;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.QrKey;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.OtpHelper;
import com.sk.weichat.helper.PaySecureHelper;
import com.sk.weichat.sp.UserSp;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.Base64;
import com.sk.weichat.util.DisplayUtil;
import com.sk.weichat.util.ScreenUtil;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.secure.AES;
import com.sk.weichat.util.secure.MAC;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 付款码
 */
public class PaymentActivity extends BaseActivity {

    private ImageView mPayQrCodeIv;
    private ImageView mPayBarCodeIv;
    // 每间隔一分钟刷新一次付款码
    private CountDownTimer mCodeRefreshCountDownTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            refreshPaymentCode();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initActionBar();
        initView();
        initEvent();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCodeRefreshCountDownTimer.cancel();
        EventBus.getDefault().unregister(this);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(view -> finish());
        TextView titleTv = findViewById(R.id.tv_title_center);
        titleTv.setText(getString(R.string.receipt_payment));
    }

    private void initView() {
        mPayQrCodeIv = findViewById(R.id.pm_qr_code_iv);
        mPayBarCodeIv = findViewById(R.id.pm_bar_code_iv);
        refreshPaymentCode();
    }

    private void initEvent() {
/*
        mPayQrCodeIv.setOnClickListener(v -> {// 刷新付款码
            refreshPaymentCode();
        });
*/

        findViewById(R.id.go_receipt_ll).setOnClickListener(v -> startActivity(new Intent(mContext, ReceiptActivity.class)));
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventPaymentSuccess message) {
        DialogHelper.tip(mContext, getString(R.string.receipted, message.getReceiptName()));
        refreshPaymentCode();
    }

    private void refreshPaymentCode() {
        mCodeRefreshCountDownTimer.cancel();
        mCodeRefreshCountDownTimer.start();

        checkQrKey(this::updateQrImage, () -> {
            PaySecureHelper.inputPayPassword(mContext, getString(R.string.tip_enable_payment_qr_code), null, password -> requestQrCode(password, this::updateQrImage))
                    .setOnCancelListener(dialog -> {
                        finish();
                    });
        });
    }

    private void checkQrKey(Function<String> onSuccess, Runnable noQrCode) {
        String qrKeyBase64 = UserSp.getInstance(mContext).getQrKey();
        if (TextUtils.isEmpty(qrKeyBase64)) {
            noQrCode.run();
            return;
        }
        byte[] qrKey = Base64.decode(qrKeyBase64);
        String salt = String.valueOf(System.currentTimeMillis());
        String content = AppConfig.apiKey + coreManager.getSelf().getUserId() + UserSp.getInstance(mContext).getAccessToken() + salt;
        String mac = MAC.encodeBase64(content, qrKey);
        Map<String, String> p = new HashMap<>();
        p.put("salt", salt);
        p.put("mac", mac);
        HttpUtils.get().url(coreManager.getConfig().PAY_SECURE_VERIFY_QR_KEY)
                .params(p)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkError(result, Result.CODE_QR_KEY_INVALID)) {
                            // qrKey过期，
                            noQrCode.run();
                        } else if (Result.checkSuccess(mContext, result)) {
                            onSuccess.apply(generateQrCode(qrKey));
                        } else {
                            // 接口内部异常，不留在这个页面，
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        // 断网情况不提示，直接使用本地qrKey生成付款码，不管qrKey是否过期，
                        onSuccess.apply(generateQrCode(qrKey));
                    }
                });
    }

    private void updateQrImage(String code) {
        Bitmap bitmap1 = CommonUtils.createQRCode(code, DisplayUtil.dip2px(MyApplication.getContext(), 160),
                DisplayUtil.dip2px(MyApplication.getContext(), 160));
        Bitmap bitmap2 = CommonUtils.createBarCode(code, ScreenUtil.getScreenWidth(MyApplication.getContext()) - DisplayUtil.dip2px(MyApplication.getContext(), 40),
                DisplayUtil.dip2px(MyApplication.getContext(), 80));
        mPayQrCodeIv.setImageBitmap(bitmap1);
        mPayBarCodeIv.setImageBitmap(bitmap2);
    }

    private void requestQrCode(String password, Function<String> callback) {
        DialogHelper.showDefaulteMessageProgressDialog(mContext);
        Map<String, String> params = new HashMap<String, String>();

        PaySecureHelper.generateParam(
                mContext, password, params,
                "",
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(mContext, mContext.getString(R.string.tip_pay_secure_place_holder, t.getMessage()));
                    finish();
                }, (p, code) -> {
                    HttpUtils.get().url(coreManager.getConfig().PAY_SECURE_GET_QR_KEY)
                            .params(p)
                            .build()
                            .execute(new BaseCallback<QrKey>(QrKey.class) {

                                @Override
                                public void onResponse(ObjectResult<QrKey> result) {
                                    DialogHelper.dismissProgressDialog();
                                    if (Result.checkSuccess(mContext, result)
                                            && result.getData() != null
                                            && result.getData().getData() != null) {
                                        String qrKeyEncrypt = result.getData().getData();
                                        byte[] qrKey = AES.decryptFromBase64(qrKeyEncrypt, code);
                                        String qrCode = generateQrCode(qrKey);
                                        callback.apply(qrCode);
                                    } else {
                                        // 接口内部异常，不留在这个页面，
                                        finish();
                                    }
                                }

                                @Override
                                public void onError(Call call, Exception e) {
                                    DialogHelper.dismissProgressDialog();
                                    ToastUtil.showErrorNet(mContext);
                                    finish();
                                }
                            });
                });
    }

    public String generateQrCode(byte[] qrKey) {
        UserSp.getInstance(mContext).setQrKey(Base64.encode(qrKey));
        return OtpHelper.generate(Integer.valueOf(coreManager.getSelf().getUserId()), qrKey).getQrCodeString();
    }

    public interface Function<T> {
        void apply(T t);
    }
}
