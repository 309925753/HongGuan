package com.sk.weichat.ui.xrce;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.sk.weichat.R;
import com.sk.weichat.helper.ImageLoadHelper;


/**
 * 封面选择器
 * create xuan
 * time 2018-11-30 09:32:21
 */

public class Xcoverbar extends FrameLayout implements View.OnClickListener {

    private String mCoverPath;
    private LinearLayout llBg;

    private OnChangeListener mListener;

    public Xcoverbar(Context context) {
        this(context, null);
    }

    public Xcoverbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Xcoverbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        View.inflate(context, R.layout.view_cover, this);

        llBg = findViewById(R.id.ll_bg);

        findViewById(R.id.cover_iv1).setOnClickListener(this);
        findViewById(R.id.cover_iv2).setOnClickListener(this);
        findViewById(R.id.cover_iv3).setOnClickListener(this);
        findViewById(R.id.cover_iv4).setOnClickListener(this);
        findViewById(R.id.cover_iv5).setOnClickListener(this);
        findViewById(R.id.cover_iv6).setOnClickListener(this);
        findViewById(R.id.cover_iv7).setOnClickListener(this);
        findViewById(R.id.cover_iv8).setOnClickListener(this);
    }

    /**
     * 设置背景图
     *
     * @param path 路径格式为xxx%03d.xx
     */
    public void setCoverBackground(String path) {
        mCoverPath = path;
        for (int i = 0; i < 8; i++) {
            String url = String.format(path, i + 1);
            ImageView image = (ImageView) llBg.getChildAt(i);
            ImageLoadHelper.showImage(getContext(), url, image);
        }
        String url = String.format(mCoverPath, 1);
        if (mListener != null) {
            mListener.change(url);
        }
    }

    @Override
    public void onClick(View v) {
        int position = 1;
        switch (v.getId()) {
            case R.id.cover_iv1:
                position = 1;
                break;
            case R.id.cover_iv2:
                position = 2;
                break;
            case R.id.cover_iv3:
                position = 3;
                break;
            case R.id.cover_iv4:
                position = 4;
                break;
            case R.id.cover_iv5:
                position = 5;
                break;
            case R.id.cover_iv6:
                position = 6;
                break;
            case R.id.cover_iv7:
                position = 7;
                break;
            case R.id.cover_iv8:
                position = 8;
                break;
        }
        String url = String.format(mCoverPath, position);
        if (mListener != null) {
            mListener.change(url);
        }
    }

    public void addOnChangeListener(OnChangeListener listener) {
        this.mListener = listener;
    }

    public interface OnChangeListener {
        void change(String curr);

        void confirm(String curr);

        void cancel();
    }
}

