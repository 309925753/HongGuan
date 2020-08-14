package com.sk.weichat.ui.me;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.view.ControlFontSize;

/**
 * Created by Administrator on 2017/12/5 0005.
 */

public class FontSizeActivity extends BaseActivity {

    private ControlFontSize mControlFontSize;
    private TextView tv1, tv2;
    private int size = 1;// 默认选中标准

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_size);
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.font_size));
        TextView tvRight = (TextView) findViewById(R.id.tv_title_right);
        tvRight.setText(getString(R.string.finish));
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceUtils.putInt(FontSizeActivity.this, Constants.FONT_SIZE, size);
                finish();
            }
        });
    }

    private void initView() {
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        mControlFontSize = (ControlFontSize) findViewById(R.id.control_font);
        size = PreferenceUtils.getInt(FontSizeActivity.this, Constants.FONT_SIZE, size);

        tv1.setTextSize(13 + size);
        tv2.setTextSize(13 + size);
        mControlFontSize.setCurrentProgress(size);
        mControlFontSize.setOnPointResultListener(new ControlFontSize.OnPointResultListener() {
            @Override
            public void onPointResult(int position) {
                switch (position) {
                    case 0:
                        size = 0;
                        setTextSize(13);
                        break;
                    case 1:
                        size = 1;
                        setTextSize(14);
                        break;
                    case 2:
                        size = 2;
                        setTextSize(15);
                        break;
                    case 3:
                        size = 3;
                        setTextSize(16);
                        break;
                    case 4:
                        size = 4;
                        setTextSize(17);
                        break;
                    case 5:
                        size = 5;
                        setTextSize(18);
                        break;
                }
            }
        });
    }

    private void setTextSize(int size) {
        tv1.setTextSize(size);
        tv2.setTextSize(size);
    }
}
