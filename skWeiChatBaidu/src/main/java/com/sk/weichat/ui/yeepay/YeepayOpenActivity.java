package com.sk.weichat.ui.yeepay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sk.weichat.R;
import com.sk.weichat.bean.Code;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.helper.YeepayHelper;
import com.sk.weichat.ui.account.SelectPrefixActivity;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.filter.RegexInputFilter;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;

public class YeepayOpenActivity extends BaseActivity {
    private Button btn_getCode;
    private ImageView mImageCodeIv;
    private EditText mAuthCodeEdit;
    private EditText mImageCodeEdit;
    private EditText etRealName;
    private EditText etIDCard;
    private EditText etPhone;
    private TextView tv_prefix;
    private int mobilePrefix = 86;
    private int reckonTime = 60;
    private Handler mReckonHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x1) {
                btn_getCode.setText("(" + reckonTime + ")");
                reckonTime--;
                if (reckonTime < 0) {
                    mReckonHandler.sendEmptyMessage(0x2);
                } else {
                    mReckonHandler.sendEmptyMessageDelayed(0x1, 1000);
                }
            } else if (msg.what == 0x2) {
                // 60秒结束
                btn_getCode.setText(getString(R.string.send));
                btn_getCode.setEnabled(true);
                reckonTime = 60;
            }
        }
    };

    public static void start(Context context) {
        Intent starter = new Intent(context, YeepayOpenActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yeepay_open);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mAuthCodeEdit = findViewById(R.id.auth_code_edit);
        mImageCodeEdit = findViewById(R.id.image_tv);
        mImageCodeIv = findViewById(R.id.image_iv);
        etRealName = findViewById(R.id.etRealName);
        // 只能输入中文，
        etRealName.setFilters(new InputFilter[]{new RegexInputFilter("[\\u4e00-\\u9fa5]*")});
        etIDCard = findViewById(R.id.etIDCard);
        // 18位，只能输入数字或者X，
        etIDCard.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18), new RegexInputFilter("[0-9xX]*")});
        etPhone = findViewById(R.id.etPhone);
        // 11位，只能输入数字，
        etPhone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11), new RegexInputFilter("[0-9]*")});
        Button btnOpen = (Button) findViewById(R.id.btnOpen);
        ButtonColorChange.colorChange(this, btnOpen);
        btnOpen.setOnClickListener(v -> {
            if (checkInput()) {
                return;
            }
            openAccount(etRealName.getText().toString(), etIDCard.getText().toString(), etPhone.getText().toString(), mAuthCodeEdit.getText().toString());
        });
        btn_getCode = (Button) findViewById(R.id.send_again_btn);
        ButtonColorChange.colorChange(this, btn_getCode);
        btn_getCode.setOnClickListener(v -> {
            // 获取验证码
            String phoneNumber = etPhone.getText().toString().trim();
            String imagecode = mImageCodeEdit.getText().toString().trim();
            if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(imagecode)) {
                ToastUtil.showToast(mContext, getString(R.string.tip_phone_number_verification_code_empty));
                return;
            }
            verifyTelephone(phoneNumber, imagecode);
        });
        View mRefreshIv = (ImageView) findViewById(R.id.image_iv_refresh);
        mRefreshIv.setOnClickListener(v -> {
            if (TextUtils.isEmpty(etPhone.getText().toString())) {
                ToastUtil.showToast(this, getString(R.string.tip_phone_number_empty_request_verification_code));
            } else {
                requestImageCode();
            }
        });
        etPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // 手机号输入完成后自动刷新验证码，
                    // 只在移开焦点，也就是点击其他EditText时调用，
                    requestImageCode();
                }
            }
        });
        tv_prefix = (TextView) findViewById(R.id.tv_prefix);
        tv_prefix.setOnClickListener(v -> {
            // 选择国家区号
            Intent intent = new Intent(this, SelectPrefixActivity.class);
            startActivityForResult(intent, SelectPrefixActivity.REQUEST_MOBILE_PREFIX_LOGIN);
        });
        mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        tv_prefix.setText("+" + mobilePrefix);
    }

    /**
     * 请求图形验证码
     */
    private void requestImageCode() {
        Map<String, String> params = new HashMap<>();
        params.put("telephone", mobilePrefix + etPhone.getText().toString().trim());
        String url = HttpUtils.get().url(coreManager.getConfig().USER_GETCODE_IMAGE)
                .params(params)
                .buildUrl();
        ImageLoadHelper.loadBitmapWithoutCache(
                mContext,
                url,
                b -> {
                    mImageCodeIv.setImageBitmap(b);
                }, e -> {
                    Toast.makeText(mContext, R.string.tip_verification_code_load_failed, Toast.LENGTH_SHORT).show();
                }
        );
    }

    /**
     * 请求验证码
     */
    private void verifyTelephone(String phoneNumber, String imageCode) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        String language = Locale.getDefault().getLanguage();
        params.put("language", language);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("telephone", phoneNumber);
        params.put("imgCode", imageCode);

        HttpUtils.get().url(coreManager.getConfig().YOP_OPEN_SEND_AUTH_CODE)
                .params(params)
                .build()
                .execute(new BaseCallback<Code>(Code.class) {
                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            Toast.makeText(mContext, R.string.verification_code_send_success, Toast.LENGTH_SHORT).show();
                            btn_getCode.setEnabled(false);
                            // 开始计时
                            mReckonHandler.sendEmptyMessage(0x1);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(mContext, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openAccount(String realName, String idCard, String phone, String smsCode) {
        HashMap<String, String> params = new HashMap<>();
        params.put("name", realName);
        params.put("certificateNo", idCard);
        params.put("mobile", phone);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("smsCode", smsCode);

        HttpUtils.get().url(coreManager.getConfig().YOP_OPEN_ACCOUNT)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            ToastUtil.showToast(mContext, R.string.yeepay_open_success);
                            YeepayHelper.saveOpened(mContext, true);
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private boolean checkInput() {
        if (TextUtils.isEmpty(etRealName.getText())) {
            ToastUtil.showToast(mContext, R.string.input_real_name);
            return true;
        }
        if (TextUtils.isEmpty(etIDCard.getText())) {
            ToastUtil.showToast(mContext, R.string.input_id_card);
            return true;
        }
        if (TextUtils.isEmpty(etPhone.getText())) {
            ToastUtil.showToast(mContext, R.string.hint_input_phone_number);
            return true;
        }
        String authCode = mAuthCodeEdit.getText().toString().trim();
        if (TextUtils.isEmpty(authCode)) {
            Toast.makeText(this, getString(R.string.input_message_code), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != SelectPrefixActivity.RESULT_MOBILE_PREFIX_SUCCESS) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        mobilePrefix = data.getIntExtra(Constants.MOBILE_PREFIX, 86);
        tv_prefix.setText("+" + mobilePrefix);
        // 图形验证码可能因区号失效，
        // 请求图形验证码
        if (!TextUtils.isEmpty(etPhone.getText().toString())) {
            requestImageCode();
        }
    }
}
