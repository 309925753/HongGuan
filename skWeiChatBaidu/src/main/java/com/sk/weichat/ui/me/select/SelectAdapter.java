package com.sk.weichat.ui.me.select;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.sk.weichat.bean.Friend;
import com.sk.weichat.ui.base.BaseActivity;

import java.util.List;

public abstract class SelectAdapter {
    private static int sRequestCode;
    protected int mRequestCode = ++sRequestCode;
    protected TextView tvValue;

    public abstract void startSelect(Activity ctx);

    @StringRes
    public abstract int getLabel();

    public void bindValueWidget(TextView tvValue) {

        this.tvValue = tvValue;
    }

    @NonNull
    public abstract List<Friend> query(BaseActivity ctx);

    public abstract boolean consume(Context ctx, int requestCode, Intent data);
}
