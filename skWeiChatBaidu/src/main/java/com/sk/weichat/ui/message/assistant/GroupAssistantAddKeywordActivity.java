package com.sk.weichat.ui.message.assistant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.R;
import com.sk.weichat.bean.assistant.GroupAssistantDetail;
import com.sk.weichat.bean.assistant.KeyWord;
import com.sk.weichat.bean.event.EventNotifyByTag;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

public class GroupAssistantAddKeywordActivity extends BaseActivity {
    private TagFlowLayout mFlowLayout;
    private KeyWordAdapter mKeyWordAdapter;
    private List<KeyWord> mKeyWordData = new ArrayList<>();

    private EditText mKeywordEdit, mValueEdit;
    private Button mChangeBtn;

    private boolean isKeywordEmpty = true;
    private boolean isValueEmpty = true;
    private boolean isChange;
    private String mCurrentEditKeyWordId;

    private String roomId;
    private GroupAssistantDetail groupAssistantDetail;

    public static void start(Context context, String roomId, String groupAssistantDetail) {
        Intent intent = new Intent(context, GroupAssistantAddKeywordActivity.class);
        intent.putExtra("roomId", roomId);
        intent.putExtra("groupAssistantDetail", groupAssistantDetail);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_assistant_add_keyword);

        roomId = getIntent().getStringExtra("roomId");
        String str = getIntent().getStringExtra("groupAssistantDetail");
        groupAssistantDetail = JSON.parseObject(str, GroupAssistantDetail.class);
        if (groupAssistantDetail == null) {
            Toast.makeText(mContext, getString(R.string.tip_get_detail_error), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mKeyWordData = groupAssistantDetail.getKeyWords();

        initActionBar();
        initView();
    }

    private void initActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.keyword_manager));
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
    }

    private void initView() {
        mFlowLayout = findViewById(R.id.id_flowlayout);
        mKeyWordAdapter = new KeyWordAdapter(mKeyWordData);
        mFlowLayout.setAdapter(mKeyWordAdapter);

        mKeywordEdit = findViewById(R.id.et1);
        mValueEdit = findViewById(R.id.et2);
        mChangeBtn = findViewById(R.id.btn);
//        mChangeBtn.setBackgroundColor(SkinUtils.getSkin(mContext).getAccentColor());
        ButtonColorChange.colorChange(this, mChangeBtn);
        mFlowLayout.setOnTagClickListener((view, position, parent) -> {
            KeyWord keyWord = mKeyWordData.get(position);
            if (keyWord != null) {
                isChange = true;
                mCurrentEditKeyWordId = keyWord.getId();

                mKeywordEdit.setText(keyWord.getKeyword());
                mValueEdit.setText(keyWord.getValue());
                mChangeBtn.setText(getString(R.string.transfer_modify));
            }
            return false;
        });

        mKeywordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isKeywordEmpty = TextUtils.isEmpty(mKeywordEdit.getText().toString());
                if (isKeywordEmpty && isValueEmpty) {
                    isChange = false;
                    mChangeBtn.setText(getString(R.string.add_to));
                }
            }
        });
        mValueEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isValueEmpty = TextUtils.isEmpty(mValueEdit.getText().toString());
                if (isKeywordEmpty && isValueEmpty) {
                    isChange = false;
                    mChangeBtn.setText(getString(R.string.add_to));
                }
            }
        });

        mChangeBtn.setOnClickListener(v -> {
            if (isKeywordEmpty || isValueEmpty) {
                Toast.makeText(mContext, getString(R.string.name_connot_null), Toast.LENGTH_SHORT).show();
                return;
            }
            String keyword = mKeywordEdit.getText().toString();
            String value = mValueEdit.getText().toString();
            if (isChange) {
                updateKeyWord(keyword, value);
            } else {
                addKeyWord(keyword, value);
            }
        });
    }

    private void addKeyWord(String keyword, String value) {
        DialogHelper.showDefaulteMessageProgressDialog(mContext);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(mContext).accessToken);
        params.put("roomId", roomId);
        params.put("helperId", groupAssistantDetail.getHelperId());
        params.put("keyword", keyword);
        params.put("value", value);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).ROOM_ADD_AUTO_RESPONSE)
                .params(params)
                .build()
                .execute(new BaseCallback<KeyWord>(KeyWord.class) {

                    @Override
                    public void onResponse(ObjectResult<KeyWord> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result != null && result.getResultCode() == 1) {
                            mKeyWordData.add(result.getData());
                            mKeyWordAdapter.notifyDataChanged();

                            mKeywordEdit.setText("");
                            mValueEdit.setText("");
                            EventBus.getDefault().post(new EventNotifyByTag(EventNotifyByTag.GroupAssistantKeyword));
                        } else {
                            if (result != null && !TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(mContext, result.getResultMsg());
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void updateKeyWord(String keyword, String value) {
        DialogHelper.showDefaulteMessageProgressDialog(mContext);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(mContext).accessToken);
        params.put("groupHelperId", groupAssistantDetail.getId());
        params.put("keyWordId", mCurrentEditKeyWordId);
        params.put("keyword", keyword);
        params.put("value", value);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).ROOM_UPDATE_AUTO_RESPONSE)
                .params(params)
                .build()
                .execute(new BaseCallback<KeyWord>(KeyWord.class) {

                    @Override
                    public void onResponse(ObjectResult<KeyWord> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result != null && result.getResultCode() == 1) {
                            for (int i = 0; i < mKeyWordData.size(); i++) {
                                if (TextUtils.equals(mKeyWordData.get(i).getId(), mCurrentEditKeyWordId)) {
                                    KeyWord keyWord = mKeyWordData.get(i);
                                    keyWord.setKeyword(keyword);
                                    keyWord.setValue(value);
                                    mKeyWordData.remove(i);
                                    mKeyWordData.add(i, keyWord);
                                    mKeyWordAdapter.notifyDataChanged();

                                    isChange = false;
                                    mChangeBtn.setText(getString(R.string.add_to));
                                    mKeywordEdit.setText("");
                                    mValueEdit.setText("");
                                    EventBus.getDefault().post(new EventNotifyByTag(EventNotifyByTag.GroupAssistantKeyword));
                                }
                            }
                        } else {
                            if (result != null && !TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(mContext, result.getResultMsg());
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void deleteKeyWord(int position, String keyWordId) {
        DialogHelper.showDefaulteMessageProgressDialog(mContext);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(mContext).accessToken);
        params.put("groupHelperId", groupAssistantDetail.getId());
        params.put("keyWordId", keyWordId);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).ROOM_DELETE_AUTO_RESPONSE)
                .params(params)
                .build()
                .execute(new BaseCallback<KeyWord>(KeyWord.class) {

                    @Override
                    public void onResponse(ObjectResult<KeyWord> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result != null && result.getResultCode() == 1) {
                            mKeyWordData.remove(position);
                            mKeyWordAdapter.notifyDataChanged();
                            if (TextUtils.equals(mCurrentEditKeyWordId, keyWordId)) {
                                mKeywordEdit.setText("");
                                mValueEdit.setText("");
                                isChange = false;
                            }
                            EventBus.getDefault().post(new EventNotifyByTag(EventNotifyByTag.GroupAssistantKeyword));
                        } else {
                            if (result != null && !TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(mContext, result.getResultMsg());
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    class KeyWordAdapter extends TagAdapter<KeyWord> {

        KeyWordAdapter(List<KeyWord> data) {
            super(data);
        }

        @Override
        public View getView(FlowLayout parent, int position, KeyWord keyWord) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.row_keyword,
                    mFlowLayout, false);
            TextView tv = view.findViewById(R.id.tv);
            tv.setBackgroundColor(SkinUtils.getSkin(mContext).getAccentColor());
            ImageView iv = view.findViewById(R.id.iv);
            tv.setText(keyWord.getKeyword());
            iv.setOnClickListener(v -> {
                deleteKeyWord(position, keyWord.getId());
            });
            return view;
        }
    }
}
