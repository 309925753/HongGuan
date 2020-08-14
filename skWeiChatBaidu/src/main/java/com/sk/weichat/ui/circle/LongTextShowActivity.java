package com.sk.weichat.ui.circle;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.sk.weichat.R;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.HtmlUtils;
import com.sk.weichat.util.StringUtils;

public class LongTextShowActivity extends BaseActivity {

    private TextView mBodyTv;
    private String body;

    public static void start(Context ctx, String body) {
        Intent intent = new Intent(ctx, LongTextShowActivity.class);
        intent.putExtra("body", body);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_text_show);
        body = getIntent().getStringExtra("body");
        iniActionBar();
        initView();
    }

    private void iniActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.weibo_cell_all_text));
        findViewById(R.id.iv_title_left).setOnClickListener(v -> {
            finish();
        });
    }

    private void initView() {
        mBodyTv = findViewById(R.id.body_tv);
        String content = StringUtils.replaceSpecialChar(body);
        CharSequence charSequence = HtmlUtils.transform200SpanString(content, true);
        mBodyTv.setText(charSequence);

        mBodyTv.setOnClickListener(v -> {
            ClipboardManager cmb = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setText(charSequence);
            Toast.makeText(mContext, getString(R.string.tip_copied_to_clipboard), Toast.LENGTH_SHORT).show();
        });
    }
}
