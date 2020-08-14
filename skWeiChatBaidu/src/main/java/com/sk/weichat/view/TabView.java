package com.sk.weichat.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.ui.tool.ButtonColorChange;

public class TabView implements View.OnClickListener {
    private Context mContext;
    private View mView;
    private TextView attention_each_tv;
    private TextView attention_single_tv;
    private boolean isfriend;
    private int index = 0;
    private OnTabSelectedLisenter onTabSelectedLisenter;

    public TabView(Context context) {
        mContext = context;
        initView();
    }

    public TabView(Context context, boolean isnotgroup) {
        mContext = context;
        isfriend = isnotgroup;
        initView();
    }

    public TextView getAttention_each_tv() {
        return attention_each_tv;
    }

    public TextView getAttention_single_tv() {
        return attention_single_tv;
    }

    private void initView() {
        mView = LayoutInflater.from(mContext).inflate(R.layout.actionbar_tag_second, null);
        View view1 = findviewbyid(R.id.tag1);
//        view1.setBackgroundColor(SkinUtils.getSkin(mContext).getAccentColor());
        View view2 = findviewbyid(R.id.tag2);
//        view2.setBackgroundColor(SkinUtils.getSkin(mContext).getAccentColor());
        attention_each_tv = findviewbyid(R.id.attention_each_tv);
        attention_single_tv = findviewbyid(R.id.attention_single_tv);
        if (isfriend) {
            attention_each_tv.setText(getString(R.string.bisniss));
            attention_single_tv.setText(getString(R.string.black_list_all));
        } else {
            attention_each_tv.setText(getString(R.string.my_group));
            attention_single_tv.setText(getString(R.string.all_group));
        }
        attention_each_tv.setOnClickListener(this);
        attention_single_tv.setOnClickListener(this);
        hideViewByTag(1);
        showViewByTag(0);
    }

    public View getView() {
        return mView;
    }

    @Override
    public void onClick(View v) {
        int beforeIndex = index;
        switch (v.getId()) {
            case R.id.attention_each_tv:
                index = 0;
                break;
            case R.id.attention_single_tv:
                index = 1;
                break;
        }
        if (beforeIndex == index)
            return;
        hideViewByTag(index == 0 ? 1 : 0);
        showViewByTag(index);
        onTabSelectedLisenter.onSelected(index);
    }

    public void callOnSelect(int index) {
        View view;
        switch (index) {
            case 0:
                view = findviewbyid(R.id.attention_each_tv);
                break;
            case 1:
                view = findviewbyid(R.id.attention_single_tv);
                break;
            default:
                view = findviewbyid(R.id.attention_each_tv);
        }
        onClick(view);
    }

    public void setOnTabSelectedLisenter(OnTabSelectedLisenter onTabSelectedLisenter) {
        this.onTabSelectedLisenter = onTabSelectedLisenter;
    }

    public <T> T findviewbyid(int id) {
        return (T) mView.findViewById(id);
    }

    public String getString(int rid) {
        return mContext.getResources().getString(rid);
    }

    public void hideViewByTag(int tag) {

        if (tag == 0) {
            getAttention_each_tv().setTextColor(mContext.getResources().getColor(R.color.text_color));
        } else {
            getAttention_single_tv().setTextColor(mContext.getResources().getColor(R.color.text_color));
        }
    }

    public void showViewByTag(int tag) {
        if (tag == 0) {
            ButtonColorChange.textChange(mContext, getAttention_each_tv());
        } else {
            ButtonColorChange.textChange(mContext, getAttention_single_tv());
        }
    }

    public interface OnTabSelectedLisenter {
        void onSelected(int index);
    }
}
