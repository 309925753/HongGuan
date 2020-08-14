package com.sk.weichat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sk.weichat.R;

public class DataLoadView extends LinearLayout {

    public DataLoadView(Context context) {
        super(context);
        initView();
    }

    public DataLoadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DataLoadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private ProgressBar mLoadProgressBar;
    private TextView mLoadFailedTv;
    private static final int STATUS_NONE = 0;
    private static final int STATUS_LOADING = 1;
    private static final int STATUS_FIALED = 2;
    private static final int STATUS_ERROR = 3;
    private int status;

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_data_load, this);
        mLoadProgressBar = (ProgressBar) findViewById(R.id.load_progress_bar);
        mLoadFailedTv = (TextView) findViewById(R.id.load_failed_tv);

        status = STATUS_NONE;
        mLoadProgressBar.setVisibility(View.GONE);
        mLoadFailedTv.setVisibility(View.GONE);

        mLoadFailedTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status != STATUS_FIALED) {
                    return;
                }
                if (mLoadingEvent != null) {
                    showLoading();
                    mLoadingEvent.load();
                }
            }
        });
    }

    public static interface LoadingEvent {
        void load();
    }

    private LoadingEvent mLoadingEvent;

    public void setLoadingEvent(LoadingEvent loadingEvent) {
        mLoadingEvent = loadingEvent;
    }

    public void showLoading() {
        status = STATUS_LOADING;
        mLoadProgressBar.setVisibility(View.VISIBLE);
        mLoadFailedTv.setVisibility(View.GONE);
    }

    public void showSuccess() {
        status = STATUS_NONE;
        setVisibility(View.GONE);
        mLoadProgressBar.setVisibility(View.GONE);
        mLoadFailedTv.setVisibility(View.GONE);
    }

    public void showFailed() {
        status = STATUS_FIALED;
        mLoadProgressBar.setVisibility(View.GONE);
        mLoadFailedTv.setVisibility(View.VISIBLE);
    }

    public void showError(String errorMsg) {
        status = STATUS_ERROR;
        mLoadProgressBar.setVisibility(View.GONE);
        mLoadFailedTv.setVisibility(View.VISIBLE);
        mLoadFailedTv.setText(errorMsg);
    }
}
