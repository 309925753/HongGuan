package com.sk.weichat.ui.nearby;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.MyFragmentManager;

public class UserListGatherActivity extends BaseActivity {
    private MyFragmentManager mMyFragmentManager;
    private UserListGatherFragment userListGatherFragment;

    public static void start(Context ctx, String key) {
        Intent intent = new Intent(ctx, UserListGatherActivity.class);
        intent.putExtra("key_word", key);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list_gather);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.search));
        mMyFragmentManager = new MyFragmentManager(this, R.id.fl_fragments);
        userListGatherFragment = new UserListGatherFragment();
        mMyFragmentManager.add(userListGatherFragment);
        mMyFragmentManager.show(0);

    }
}
