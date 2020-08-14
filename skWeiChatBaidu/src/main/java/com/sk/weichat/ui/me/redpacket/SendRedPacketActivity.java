package com.sk.weichat.ui.me.redpacket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.bean.redpacket.RedPacket;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.PaySecureHelper;
import com.sk.weichat.helper.PayTypeHelper;
import com.sk.weichat.helper.YeepayHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.message.ChatActivity;
import com.sk.weichat.ui.smarttab.SmartTabLayout;
import com.sk.weichat.ui.yeepay.EventYeepaySendRedSuccess;
import com.sk.weichat.ui.yeepay.EventYeepayWebSuccess;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.EventBusHelper;
import com.sk.weichat.util.InputChangeListener;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.secure.Money;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.jivesoftware.smack.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * Created by 魏正旺 on 2016/9/9.
 */
public class SendRedPacketActivity extends BaseActivity implements View.OnClickListener {
    LayoutInflater inflater;
    private SmartTabLayout smartTabLayout;
    private ViewPager viewPager;
    private List<View> views;
    private List<String> mTitleList;
    private EditText editTextPt;  // 普通红包的金额输入框
    private EditText editTextKl;  // 口令红包的金额输入框
    private EditText editTextPwd; // 口令输入框
    private EditText editTextGre; // 祝福语输入框

    private String toUserId;
    private int mCurrentItem;
    private String type;
    private String words;
    private boolean success = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redpacket);
        toUserId = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
        inflater = LayoutInflater.from(this);
        initActionBar();
        initView();

        checkHasPayPassword();
        EventBusHelper.register(this);
    }

    private void checkHasPayPassword() {
        boolean hasPayPassword = PreferenceUtils.getBoolean(this, Constants.IS_PAY_PASSWORD_SET + coreManager.getSelf().getUserId(), true);
        if (!hasPayPassword) {
            ToastUtil.showToast(this, R.string.tip_no_pay_password);
            Intent intent = new Intent(this, ChangePayPasswordActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        findViewById(R.id.tv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.chat_redpacket));
    }

    /**
     * 初始化布局
     */
    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.viewpagert_redpacket);
        smartTabLayout = (SmartTabLayout) findViewById(R.id.smarttablayout_redpacket);
        views = new ArrayList<View>();
        mTitleList = new ArrayList<String>();
        mTitleList.add(getString(R.string.Usual_Gift));
        mTitleList.add(getString(R.string.chat_kl_red));
        View v1, v2;
        v1 = inflater.inflate(R.layout.redpacket_pager_pt, null);
        v2 = inflater.inflate(R.layout.redpacket_pager_kl, null);
        views.add(v1);
        views.add(v2);

        // 获取EditText
        editTextPt = (EditText) v1.findViewById(R.id.edit_money);
        editTextGre = (EditText) v1.findViewById(R.id.edit_blessing);
        editTextKl = (EditText) v2.findViewById(R.id.edit_money);
        editTextPwd = (EditText) v2.findViewById(R.id.edit_password);
        editTextPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String t = s.toString().trim();
                if (s.length() != t.length()) {
                    s.replace(0, s.length(), t);
                }
            }
        });
        TextView tv_scan1 = v1.findViewById(R.id.tv_amount_of_money);
        TextView tv_scan2 = v2.findViewById(R.id.tv_amount_of_money);

        TextView jineTv, tipTv, sumjineTv, yuan1, yuan2;
        jineTv = (TextView) v1.findViewById(R.id.JinETv);
        tipTv = (TextView) v2.findViewById(R.id.textviewtishi);
        sumjineTv = (TextView) v2.findViewById(R.id.sumMoneyTv);
        yuan1 = (TextView) v1.findViewById(R.id.yuanTv);
        yuan2 = (TextView) v2.findViewById(R.id.yuanTv);
        jineTv.setText(getString(R.string.hint_money));
        tipTv.setText(getString(R.string.hint_red_packet_key));
        sumjineTv.setText(getString(R.string.total_money));
        yuan1.setText(getString(R.string.yuan));
        yuan2.setText(getString(R.string.yuan));

        editTextPt.setHint(R.string.input_gift_count);
        editTextGre.setHint(getString(R.string.auspicious));

        editTextKl.setHint(R.string.input_gift_count);
        editTextPwd.setHint(getString(R.string.want_open_gift));

        TextView koulinTv;
        koulinTv = (TextView) v2.findViewById(R.id.setKouLinTv);
        koulinTv.setText(getString(R.string.message_red_new));

        Button b1 = (Button) v1.findViewById(R.id.btn_sendRed);
        b1.setAlpha(0.6f);
        b1.setOnClickListener(this);
        Button b2 = (Button) v2.findViewById(R.id.btn_sendRed);
        b2.setAlpha(0.6f);
        b2.setOnClickListener(this);

        b1.requestFocus();
        b1.setClickable(true);
        b2.requestFocus();
        b2.setClickable(true);

        InputChangeListener inputChangeListenerPt = new InputChangeListener(editTextPt, tv_scan1, b1);
        InputChangeListener inputChangeListenerKl = new InputChangeListener(editTextKl, tv_scan2, b2);

        editTextPt.addTextChangedListener(inputChangeListenerPt);
        editTextKl.addTextChangedListener(inputChangeListenerKl);

        //设置值允许输入数字和小数点
        editTextPt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editTextKl.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        viewPager.setAdapter(new PagerAdapter());
        inflater = LayoutInflater.from(this);
        smartTabLayout.setViewPager(viewPager);

        /**
         * 为了实现点击Tab栏切换的时候不出现动画
         * 为每个Tab重新设置点击事件
         */
        for (int i = 0; i < mTitleList.size(); i++) {
            View view = smartTabLayout.getTabAt(i);
            view.setTag(i + "");
            view.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_sendRed) {
            final Bundle bundle = new Bundle();
            final Intent data = new Intent(this, ChatActivity.class);
            String money = null, words = null;
            //根据Tab的Item来判断当前发送的是那种红包
            final int item = viewPager.getCurrentItem();

            //获取金额和文字信息(口令或者祝福语)
            if (item == 0) {
                money = editTextPt.getText().toString();
                words = editTextGre.getText().toString();
                if (StringUtils.isNullOrEmpty(words)) {
                    words = editTextGre.getHint().toString();
                }
            } else if (item == 1) {
                money = editTextKl.getText().toString();
                words = editTextPwd.getText().toString();
                if (StringUtils.isNullOrEmpty(words)) {
                    words = editTextPwd.getHint().toString();
                    words = words.substring(1, words.length());
                }
            }
            if (StringUtils.isNullOrEmpty(money)) {
                ToastUtil.showToast(mContext, getString(R.string.input_gift_count));
            } else if (Double.parseDouble(money) > coreManager.getConfig().maxRedpacktAmount || Double.parseDouble(money) <= 0) {
                ToastUtil.showToast(mContext, getString(R.string.red_packet_range, coreManager.getConfig().maxRedpacktAmount));
            } else {
                money = Money.fromYuan(money);
                final String finalMoney = money;
                final String finalWords = words;
                this.type = item == 0 ? "1" : "3";
                this.words = words;
                PayTypeHelper.selectPayType(mContext, payType -> {
                    switch (payType) {
                        case DEFAULT:
                            PaySecureHelper.inputPayPassword(this, getString(R.string.chat_redpacket), finalMoney, password -> {
                                sendRed(type, finalMoney, String.valueOf(1), finalWords, password);
                            });
                            break;
                        case YEEPAY:
                            YeepayHelper.sendRed(mContext, coreManager, toUserId,
                                    type, finalMoney, String.valueOf(1), finalWords);
                            break;
                    }
                });
            }
        } else {
            // 根据Tab按钮传递的Tag来判断是那个页面，设置到相应的界面并且去掉动画
            int index = Integer.parseInt(v.getTag().toString());
            if (mCurrentItem != index) {
                mCurrentItem = index;
                hideKeyboard();
            }
            viewPager.setCurrentItem(index, false);
        }
    }

    private void result(String objectId) {
        if (success) {
            // 以免重复处理，
            return;
        }
        success = true;
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_RED);
        message.setFromUserId(coreManager.getSelf().getUserId());
        message.setFromUserName(coreManager.getSelf().getNickName());
        message.setContent(words); // 祝福语
        message.setFilePath(type); // 用FilePath来储存红包类型
        message.setFileSize(1); //用filesize来储存红包状态
        message.setObjectId(objectId); // 红包id
        Intent intent = new Intent();
        intent.putExtra(AppConstant.EXTRA_CHAT_MESSAGE, message.toJsonString());
        setResult(viewPager.getCurrentItem() == 0 ? ChatActivity.REQUEST_CODE_SEND_RED_PT : ChatActivity.REQUEST_CODE_SEND_RED_KL, intent);
        finish();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive() && this.getCurrentFocus() != null) {
            if (this.getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public void sendRed(final String type, String pMoney, String count, final String words, String payPassword) {
        if (!coreManager.isLogin()) {
            return;
        }
        DialogHelper.showDefaulteMessageProgressDialog(mContext);

        String money = Money.fromYuan(pMoney);
        Map<String, String> params = new HashMap<>();
        params.put("type", type);
        params.put("moneyStr", money);
        params.put("count", count);
        params.put("greetings", words);
        params.put("toUserId", toUserId);
        PaySecureHelper.generateParam(
                mContext, payPassword, params,
                "" + type + money + count + words + toUserId,
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(mContext, mContext.getString(R.string.tip_pay_secure_place_holder, t.getMessage()));
                }, (p, code) -> {
                    HttpUtils.get().url(coreManager.getConfig().REDPACKET_SEND)
                            .params(p)
                            .build()
                            .execute(new BaseCallback<RedPacket>(RedPacket.class) {

                                @Override
                                public void onResponse(ObjectResult<RedPacket> result) {
                                    DialogHelper.dismissProgressDialog();
                                    if (Result.checkSuccess(mContext, result)) {
                                        RedPacket redPacket = result.getData();
                                        String objectId = redPacket.getId();
                                        result(objectId);
                                    }
                                }

                                @Override
                                public void onError(Call call, Exception e) {
                                    DialogHelper.dismissProgressDialog();
                                }
                            });
                }
        );
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventYeepaySendRedSuccess message) {
        result(message.id);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventYeepayWebSuccess message) {
        YeepayHelper.queryRed(mContext, coreManager, message.data, () -> {
            DialogHelper.dismissProgressDialog();
            result(message.data);
        });
    }

    private class PagerAdapter extends androidx.viewpager.widget.PagerAdapter {

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(View container, int position) {
            ((ViewGroup) container).addView(views.get(position));
            return views.get(position);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return mTitleList.get(position);
        }
    }
}

