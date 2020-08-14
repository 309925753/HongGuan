package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.util.CommonAdapter;
import com.sk.weichat.util.CommonViewHolder;
import com.sk.weichat.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 选择网址弹窗 Copy for URLDialog
 */
public class SelectURLDialog extends Dialog {

    private OnURLListItemClickListener mOnURLListItemClickListener;
    private Context mContext;
    private List<String> mURLList = new ArrayList<>();
    private ListView mURLListView;
    private URLAdapter mURLAdapter;

    public SelectURLDialog(Context context, List<String> data, OnURLListItemClickListener onURLListItemClickListener) {
        super(context, R.style.BottomDialog);
        this.mContext = context;
        mURLList = data;
        this.mOnURLListItemClickListener = onURLListItemClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_dialog);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        mURLListView = (ListView) findViewById(R.id.report_list);
        mURLAdapter = new URLAdapter(mContext, mURLList);
        mURLListView.setAdapter(mURLAdapter);

        mURLListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String URL = mURLList.get(position);
                if (mOnURLListItemClickListener != null && URL != null) {
                    dismiss();
                    mOnURLListItemClickListener.onURLItemClick(URL);
                }
            }
        });

        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        lp.width = ScreenUtil.getScreenWidth(getContext());
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.BOTTOM);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }

    public interface OnURLListItemClickListener {
        void onURLItemClick(String URL);
    }

    class URLAdapter extends CommonAdapter<String> {

        public URLAdapter(Context context, List<String> data) {
            super(context, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.row_report, position);
            TextView mURLTv = viewHolder.getView(R.id.report_tv);
            mURLTv.setText(data.get(position));
            return viewHolder.getConvertView();
        }
    }
}
