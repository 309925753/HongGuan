package com.sk.weichat.ui.trill;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.ui.base.BaseActivity;

public class TagPickerActivity extends BaseActivity implements View.OnClickListener {

    SparseArray<ImageView> mImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_picker);

        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(this);
        TextView title = (TextView) findViewById(R.id.tv_title_center);
        title.setText(getString(R.string.title_select_tag));

        initView();
    }

    private void initView() {
        mImages = new SparseArray<>();

        ImageView image = null;
        image = findViewById(R.id.iv_tag1);
        mImages.put(1, image);
        image = findViewById(R.id.iv_tag2);
        mImages.put(2, image);
        image = findViewById(R.id.iv_tag3);
        mImages.put(3, image);
        image = findViewById(R.id.iv_tag4);
        mImages.put(4, image);
        image = findViewById(R.id.iv_tag5);
        mImages.put(5, image);
        image = findViewById(R.id.iv_tag6);
        mImages.put(6, image);
        image = findViewById(R.id.iv_tag7);
        mImages.put(7, image);
        image = findViewById(R.id.iv_tag8);
        mImages.put(8, image);

        LinearLayout layout = null;
        layout = findViewById(R.id.ll_trill_tag1);
        layout.setOnClickListener(this);
        layout.setTag(1);

        findViewById(R.id.ll_trill_tag2);
        layout = findViewById(R.id.ll_trill_tag2);
        layout.setOnClickListener(this);
        layout.setTag(2);

        layout = findViewById(R.id.ll_trill_tag3);
        layout.setOnClickListener(this);
        layout.setTag(3);

        layout = findViewById(R.id.ll_trill_tag4);
        layout.setOnClickListener(this);
        layout.setTag(4);

        layout = findViewById(R.id.ll_trill_tag5);
        layout.setOnClickListener(this);
        layout.setTag(5);


        layout = findViewById(R.id.ll_trill_tag6);
        layout.setOnClickListener(this);
        layout.setTag(6);

        layout = findViewById(R.id.ll_trill_tag7);
        layout.setOnClickListener(this);
        layout.setTag(7);

        layout = findViewById(R.id.ll_trill_tag8);
        layout.setOnClickListener(this);
        layout.setTag(8);

        int key = getIntent().getIntExtra("THIS_CIRCLE_LABLE", 8);
        mImages.get(key).setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_title_left) {
            finish();
            return;
        }

        int key = (int) v.getTag();
        mImages.get(key).setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, ReleasexActivity.class);
        intent.putExtra("THIS_CIRCLE_LABLE", key);
        setResult(RESULT_OK, intent);
        finish();
    }
}
