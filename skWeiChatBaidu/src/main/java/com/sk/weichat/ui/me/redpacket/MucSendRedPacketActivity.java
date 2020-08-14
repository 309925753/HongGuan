package com.sk.weichat.ui.me.redpacket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.sk.weichat.ui.message.MucChatActivity;
import com.sk.weichat.ui.smarttab.SmartTabLayout;
import com.sk.weichat.ui.yeepay.EventYeepaySendRedSuccess;
import com.sk.weichat.ui.yeepay.EventYeepayWebSuccess;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.EventBusHelper;
import com.sk.weichat.util.InputChangeListener;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.TimeUtils;
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
 * Created by 魏正旺 on 2016/9/8.
 */
public class MucSendRedPacketActivity extends BaseActivity implements View.OnClickListener {
    LayoutInflater inflater;
    private SmartTabLayout smartTabLayout;
    private ViewPager viewPager;
    private List<View> views;
    private List<String> mTitleList;
    private EditText edit_count_pt;
    private EditText edit_money_pt;
    private EditText edit_words_pt;

    private EditText edit_count_psq;
    private EditText edit_money_psq;
    private EditText edit_words_psq;

    private EditText edit_count_kl;
    private EditText edit_money_kl;
    private EditText edit_words_kl;

    private TextView hbgs, ge, zje, yuan, xhb;
    private Button pt;
    private Button bkl;
    private Button sq;
    //    private Button sq, pt, bkl;

    private String toUserId;
    private int mCurrentItem;
    private int memberNum;
    private String type;
    private String words;
    private boolean success = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muc_redpacket);
        toUserId = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
        memberNum = getIntent().getIntExtra(AppConstant.EXTRA_MEMBER_NUM, 0);
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
        tvTitle.setText(getString(R.string.send_red_packet));
    }

    private void initView() {
        smartTabLayout = (SmartTabLayout) findViewById(R.id.muc_smarttablayout_redpacket);
        viewPager = (ViewPager) findViewById(R.id.muc_viewpagert_redpacket);
        views = new ArrayList<View>();
        mTitleList = new ArrayList<String>();
        mTitleList.add(getString(R.string.red_envelope));
        mTitleList.add(getString(R.string.Usual_Gift));
        mTitleList.add(getString(R.string.mes_gift));

        views.add(inflater.inflate(R.layout.muc_redpacket_pager_pt, null));
        views.add(inflater.inflate(R.layout.muc_redpacket_pager_sq, null));
        views.add(inflater.inflate(R.layout.muc_redpacket_pager_kl, null));

        View temp_view = views.get(0);
        edit_count_pt = (EditText) temp_view.findViewById(R.id.edit_redcount);
        edit_count_pt.addTextChangedListener(new RemoveZeroTextWatcher(edit_count_pt));
        edit_money_pt = (EditText) temp_view.findViewById(R.id.edit_money);
        edit_words_pt = (EditText) temp_view.findViewById(R.id.edit_blessing);
        TextView tv_scan1 = temp_view.findViewById(R.id.tv_amount_of_money);
        hbgs = (TextView) temp_view.findViewById(R.id.hbgs);
        ge = (TextView) temp_view.findViewById(R.id.ge);
        zje = (TextView) temp_view.findViewById(R.id.zje);
        yuan = (TextView) temp_view.findViewById(R.id.yuan);
        xhb = (TextView) temp_view.findViewById(R.id.textviewtishi);
        sq = (Button) temp_view.findViewById(R.id.btn_sendRed);
        hbgs.setText(getString(R.string.number_of_envelopes));
        ge.setText(getString(R.string.individual));
        zje.setText(getString(R.string.total_amount));
        edit_money_pt.setHint(getString(R.string.input_amount));
        yuan.setText(getString(R.string.yuan));
        xhb.setText(getString(R.string.rondom_amount));
        edit_words_pt.setHint(getString(R.string.auspicious));
        sq.setAlpha(0.6f);
        sq.setOnClickListener(this);

        temp_view = views.get(1);
        edit_count_psq = (EditText) temp_view.findViewById(R.id.edit_redcount);
        TextView tv_scan2 = temp_view.findViewById(R.id.tv_amount_of_money);

        edit_count_psq.addTextChangedListener(new RemoveZeroTextWatcher(edit_count_psq));
        edit_money_psq = (EditText) temp_view.findViewById(R.id.edit_money);
        edit_words_psq = (EditText) temp_view.findViewById(R.id.edit_blessing);
        hbgs = (TextView) temp_view.findViewById(R.id.hbgs);
        ge = (TextView) temp_view.findViewById(R.id.ge);
        zje = (TextView) temp_view.findViewById(R.id.zje);
        yuan = (TextView) temp_view.findViewById(R.id.yuan);
        xhb = (TextView) temp_view.findViewById(R.id.textviewtishi);
        pt = (Button) temp_view.findViewById(R.id.btn_sendRed);
        hbgs.setText(getString(R.string.number_of_envelopes));
        ge.setText(getString(R.string.individual));
        zje.setText(getString(R.string.total_amount));
        edit_money_psq.setHint(getString(R.string.input_amount));
        yuan.setText(getString(R.string.yuan));
        xhb.setText(getString(R.string.same_amount));
        edit_words_psq.setHint(getString(R.string.auspicious));
        pt.setAlpha(0.6f);
        pt.setOnClickListener(this);

        temp_view = views.get(2);
        edit_count_kl = (EditText) temp_view.findViewById(R.id.edit_redcount);
        TextView tv_scan3 = temp_view.findViewById(R.id.tv_amount_of_money);
        edit_count_kl.addTextChangedListener(new RemoveZeroTextWatcher(edit_count_kl));
        edit_money_kl = (EditText) temp_view.findViewById(R.id.edit_money);
        edit_words_kl = (EditText) temp_view.findViewById(R.id.edit_password);
        edit_words_kl.addTextChangedListener(new TextWatcher() {
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
        EditText edit_compatible = (EditText) temp_view.findViewById(R.id.edit_compatible);
        edit_compatible.requestFocus();

        hbgs = (TextView) temp_view.findViewById(R.id.hbgs);
        ge = (TextView) temp_view.findViewById(R.id.ge);
        zje = (TextView) temp_view.findViewById(R.id.zje);
        yuan = (TextView) temp_view.findViewById(R.id.yuan);
        xhb = (TextView) temp_view.findViewById(R.id.textviewtishi);
        bkl = (Button) temp_view.findViewById(R.id.btn_sendRed);
        TextView kl = (TextView) temp_view.findViewById(R.id.kl);
        kl.setText(getString(R.string.message_red));
        hbgs.setText(getString(R.string.number_of_envelopes));
        ge.setText(getString(R.string.individual));
        zje.setText(getString(R.string.total_amount));
        edit_money_kl.setHint(R.string.need_input_money);
        yuan.setText(getString(R.string.yuan));
        xhb.setText(getString(R.string.reply_grab));
        edit_words_kl.setHint(getString(R.string.big_envelope));
        bkl.setAlpha(0.6f);
        bkl.setOnClickListener(this);
        InputChangeListener inputChangeListenerPt = new InputChangeListener(edit_money_pt, tv_scan1, sq);
        InputChangeListener inputChangeListenerPsq = new InputChangeListener(edit_money_psq, tv_scan2, pt);
        InputChangeListener inputChangeListenerKl = new InputChangeListener(edit_money_kl, tv_scan3, bkl);

        // 添加输入监听
        edit_money_pt.addTextChangedListener(inputChangeListenerPt);
        edit_money_psq.addTextChangedListener(inputChangeListenerPsq);
        edit_money_kl.addTextChangedListener(inputChangeListenerKl);
        // 只允许输入小数点和数字
        edit_money_pt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        edit_money_psq.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        edit_money_kl.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        viewPager.setAdapter(new PagerAdapter());
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
            final int item = viewPager.getCurrentItem();
            final Bundle bundle = new Bundle();
            final Intent intent = new Intent(this, MucChatActivity.class);
            String money = null, words = null, count = null;
            int resultCode = 0;
            switch (item) {
                case 0: {
                    money = edit_money_pt.getText().toString();
                    words = StringUtils.isNullOrEmpty(edit_words_pt.getText().toString()) ?
                            edit_words_pt.getHint().toString() : edit_words_pt.getText().toString();
                    count = edit_count_pt.getText().toString();
                    // 拼手气与普通红包位置对调  修改resultCode
                    resultCode = ChatActivity.REQUEST_CODE_SEND_RED_PSQ;
                }
                break;

                case 1: {
                    money = edit_money_psq.getText().toString();
                    words = StringUtils.isNullOrEmpty(edit_words_psq.getText().toString()) ?
                            edit_words_psq.getHint().toString() : edit_words_psq.getText().toString();
                    count = edit_count_psq.getText().toString();
                    resultCode = ChatActivity.REQUEST_CODE_SEND_RED_PT;
                }
                break;

                case 2: {
                    money = edit_money_kl.getText().toString();
                    words = StringUtils.isNullOrEmpty(edit_words_kl.getText().toString()) ?
                            edit_words_kl.getHint().toString() : edit_words_kl.getText().toString();
                    count = edit_count_kl.getText().toString();
                    resultCode = ChatActivity.REQUEST_CODE_SEND_RED_KL;
                }
                break;
            }

            if (!TextUtils.isEmpty(count) && Integer.parseInt(count) == 0) {
                Toast.makeText(this, R.string.tip_red_packet_too_slow, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!TextUtils.isEmpty(count) && Integer.parseInt(count) > memberNum) {
                Toast.makeText(this, R.string.tip_red_packet_than_member, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!TextUtils.isEmpty(count) && Integer.parseInt(count) > coreManager.getConfig().maxRedpacktNumber) {
                Toast.makeText(this, R.string.tip_red_packet_than_account, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!TextUtils.isEmpty(money) &&
                    !TextUtils.isEmpty(count) &&
                    Double.parseDouble(money) / Integer.parseInt(count) < 0.01) {
                Toast.makeText(this, R.string.tip_money_too_less, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!TextUtils.isEmpty(money) &&
                    !TextUtils.isEmpty(count) &&
                    item == 1 && Double.parseDouble(money) * 100 % Integer.parseInt(count) != 0) {
                Toast.makeText(this, R.string.normal_red_money_need_peace, Toast.LENGTH_SHORT).show();
                return;
            }

            if (eqData(money, count, words)) {
                money = Money.fromYuan(money);
                final String finalMoney = money;
                final String finalWords = words;
                final String finalCount = count;
                String type;
                if (item == 0) {
                    type = String.valueOf(2);
                } else if (item == 1) {
                    type = String.valueOf(1);
                } else {
                    type = String.valueOf((item + 1));
                }
                this.type = type;
                this.words = words;
                PayTypeHelper.selectPayType(mContext, payType -> {
                    switch (payType) {
                        case DEFAULT:
                            PaySecureHelper.inputPayPassword(this, getString(R.string.chat_redpacket), finalMoney, password -> {
                                sendRed(type, finalMoney, finalCount, finalWords, password);
                            });
                            break;
                        case YEEPAY:
                            YeepayHelper.sendMucRed(mContext, coreManager, toUserId,
                                    type, finalMoney, finalCount, finalWords);
                            break;
                    }
                });
            }
        } else {
            int index = Integer.parseInt(v.getTag().toString());
            if (mCurrentItem != index) {
                mCurrentItem = index;
                hideKeyboard();
            }
            viewPager.setCurrentItem(index, false);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive() && this.getCurrentFocus() != null) {
            if (this.getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    private boolean eqData(String money, String count, String words) {
        if (StringUtils.isNullOrEmpty(money)) {
            ToastUtil.showToast(mContext, getString(R.string.need_input_money));
            return false;
        } else if (Double.parseDouble(money) > coreManager.getConfig().maxRedpacktAmount * Integer.valueOf(count) || Double.parseDouble(money) <= 0) {
            ToastUtil.showToast(mContext, getString(R.string.red_packet_range, coreManager.getConfig().maxRedpacktAmount * Integer.valueOf(count)));
            return false;
        } else if (StringUtils.isNullOrEmpty(count)) {
            ToastUtil.showToast(mContext, getString(R.string.need_red_packet_count));
            return false;
        } else if (StringUtils.isNullOrEmpty(words)) {
            return false;
        }
        return true;
    }

    public void sendRed(String type, String pMoney, String count, String words, String payPassword) {
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
        params.put("roomJid", toUserId);

        PaySecureHelper.generateParam(
                this, payPassword, params,
                "" + type + money + count + words + toUserId,
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(this, this.getString(R.string.tip_pay_secure_place_holder, t.getMessage()));
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
                });
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
        message.setTimeSend(TimeUtils.sk_time_current_time());
        message.setContent(words); // 祝福语
        message.setFilePath(type);// 用FilePath来储存红包类型
        // 群组发送普通红包
        message.setFileSize(1);   // 用filesize来储存红包状态
        message.setObjectId(objectId); // 红包id
        Intent intent = new Intent();
        intent.putExtra(AppConstant.EXTRA_CHAT_MESSAGE, message.toJsonString());
        setResult(viewPager.getCurrentItem() == 0 ? ChatActivity.REQUEST_CODE_SEND_RED_PSQ : ChatActivity.REQUEST_CODE_SEND_RED_KL, intent);
        finish();
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

    private static class RemoveZeroTextWatcher implements TextWatcher {
        private EditText editText;

        RemoveZeroTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // 删除开头的0，
            int end = 0;
            for (int i = 0; i < editable.length(); i++) {
                char ch = editable.charAt(i);
                if (ch == '0') {
                    end = i + 1;
                } else {
                    break;
                }
            }
            if (end > 0) {
                editable.delete(0, end);
                editText.setText(editable);
            }
        }
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
        public void destroyItem(View container, int position, Object object) {
            ((ViewPager) container).removeView(views.get(position));
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