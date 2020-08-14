package com.sk.weichat.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.redchamber.bean.PacketBean;
import com.redchamber.lib.base.BaseActivity;
import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.ui.message.ChatActivity;
import com.sk.weichat.ui.yeepay.EventYeepaySendRedSuccess;
import com.sk.weichat.ui.yeepay.EventYeepayWebSuccess;
import com.sk.weichat.util.Base64;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.cjt2325.cameralibrary.util.LogUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.jivesoftware.smack.util.StringUtils;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

import static com.sk.weichat.AppConfig.apiKey;

public class SendRedPeasActivity extends BaseActivity {


    @BindView(R.id.JinETv)
    TextView JinETv;
    @BindView(R.id.edit_money)
    EditText editMoney;
    @BindView(R.id.yuanTv)
    TextView yuanTv;
    @BindView(R.id.redly2)
    LinearLayout redly2;
    @BindView(R.id.monry_ly)
    LinearLayout monryLy;
    @BindView(R.id.edit_blessing)
    EditText editBlessing;
    @BindView(R.id.edit_blessing_ly)
    LinearLayout editBlessingLy;
    @BindView(R.id.tv_amount_of_money)
    TextView tvAmountOfMoney;
    @BindView(R.id.ll_scan)
    LinearLayout llScan;
    @BindView(R.id.btn_sendRed)
    Button btnSendRed;
    String toUserId = null;
    @BindView(R.id.iv_back)
    ImageView ivBack;

    @Override
    protected int setLayout() {
        return R.layout.activity_send_red_peas;
    }

    @Override
    protected void initView() {
        initData();
        toUserId = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
    }

    private void initData() {
        getSupportActionBar().hide();

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnSendRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
            }
        });
        editMoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                tvAmountOfMoney.setText(s.toString().trim());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvAmountOfMoney.setText(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
                String t = s.toString().trim();
                tvAmountOfMoney.setText(s.toString().trim());
                if (s.length() != t.length()) {
                    s.replace(0, s.length(), t);
                }
            }
        });
    }

    private String blessing = "小小意思，拿去浪吧";

    private void getData() {
        String money = editMoney.getText().toString().trim();
        blessing = editBlessing.getText().toString().trim();
        if (StringUtils.isNullOrEmpty(money)) {
            ToastUtil.showToast(mContext, "请输入红豆个数");
            return;
        }
        if (money.equals("0")) {
            ToastUtil.showToast(mContext, "红豆个数不能为0");
            return;
        }
        if (StringUtils.isNullOrEmpty(blessing)) {
            blessing = "小小意思，拿去浪吧";
        }
        String toEncryptStr = "type:1;moneyStr:" + money + ";greetings:" + blessing + ";toUserId:" + toUserId + "";
        String value = Base64.encode((toEncryptStr + apiKey).getBytes());

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("data", value);
        HttpUtils.post().url(coreManager.getConfig().RED_SEND_PACKET)
                .params(params)
                .build()
                .execute(new BaseCallback<PacketBean>(PacketBean.class) {
                    @Override
                    public void onResponse(ObjectResult<PacketBean> result) {
                        if (result.getResultCode() == 1) {
                            PacketBean packetBean = result.getData();
                            result(packetBean.getId());
                        } else {
                            ToastUtil.showLongToast(SendRedPeasActivity.this, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(SendRedPeasActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean success = false;


    private void result(String objectId) {
       /* if (success) {
            // 以免重复处理，
            return;
        }
        success = true;*/
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_RED);
        message.setFromUserId(coreManager.getSelf().getUserId());
        message.setFromUserName(coreManager.getSelf().getNickName());
        message.setContent(blessing); // 祝福语
        message.setFilePath(String.valueOf(1)); // 用FilePath来储存红包类型
        message.setFileSize(1); //用filesize来储存红包状态
        message.setObjectId(objectId); // 红包id
        Intent intent = new Intent();
        intent.putExtra(AppConstant.EXTRA_CHAT_MESSAGE, message.toJsonString());
        setResult(ChatActivity.REQUEST_CODE_SEND_RED_PT, intent);
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventYeepaySendRedSuccess message) {
        result(message.id);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventYeepayWebSuccess message) {
      /*  YeepayHelper.queryRed(mContext, coreManager, message.data, () -> {
            DialogHelper.dismissProgressDialog();
            result(message.data);
        });*/
        LogUtil.e("EventYeepayWebSuccess = " + message.data);
    }

}



