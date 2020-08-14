package com.sk.weichat.ui.message.multi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.ToastUtil;

import static com.sk.weichat.AppConstant.NOTICE_ID;

public class ProclamationActivity extends BaseActivity {
    private EditText et_proclamation;
    private String id, text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proclamation);

        id = getIntent().getStringExtra("noticeId");
        text = getIntent().getStringExtra("noticeText");

        initView();
        initActionBar();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(view -> finish());
        TextView mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(R.string.group_bulletin);
        TextView mTvTitleRight = (TextView) findViewById(R.id.tv_title_right);
        mTvTitleRight.setText(R.string.btn_public);
        mTvTitleRight.setOnClickListener(v -> {
            String text = et_proclamation.getText().toString();
            if (!TextUtils.isEmpty(text) && text.length() > 600) {
                ToastUtil.showToast(mContext, getString(R.string.input_most_length, 600));
                return;
            }
            if (!TextUtils.isEmpty(text)) {
                Intent intent = new Intent();
                intent.putExtra("proclamation", text);
                if (!TextUtils.isEmpty(id)) {// 编辑
                    intent.putExtra("noticeId", id);
                    setResult(NOTICE_ID, intent);
                } else {// 发布
                    setResult(RESULT_OK, intent);
                }
                finish();
            } else {
                ToastUtil.showToast(mContext, getString(R.string.notice_cannot_null));
            }
        });
    }

    private void initView() {
        et_proclamation = findViewById(R.id.et_proclamation);
        if (!TextUtils.isEmpty(text)) {
            et_proclamation.setText(text);
        }
    }
}
