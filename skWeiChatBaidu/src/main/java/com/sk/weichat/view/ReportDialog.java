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
import com.sk.weichat.bean.Report;
import com.sk.weichat.util.CommonAdapter;
import com.sk.weichat.util.CommonViewHolder;
import com.sk.weichat.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 举报
 */
public class ReportDialog extends Dialog {

    private OnReportListItemClickListener mOnReportListItemClickListener;
    private Context mContext;
    private List<Report> mReportList = new ArrayList<>();
    private ListView mReportListView;
    private ReportAdapter mReportAdapter;

    public ReportDialog(Context context, boolean isGroup, OnReportListItemClickListener onReportListItemClickListener) {
        super(context, R.style.BottomDialog);
        this.mContext = context;
        if (isGroup) {
            mReportList.add(new Report(200, context.getString(R.string.report_reason_1)));
            mReportList.add(new Report(210, context.getString(R.string.report_reason_2)));
            mReportList.add(new Report(220, context.getString(R.string.report_reason_3)));
            mReportList.add(new Report(230, context.getString(R.string.report_reason_4)));
        } else {
            mReportList.add(new Report(100, context.getString(R.string.report_reason_5)));
            mReportList.add(new Report(101, context.getString(R.string.report_reason_6)));
            mReportList.add(new Report(102, context.getString(R.string.report_reason_7)));
            mReportList.add(new Report(103, context.getString(R.string.report_reason_8)));
            mReportList.add(new Report(104, context.getString(R.string.report_reason_9)));
            mReportList.add(new Report(105, context.getString(R.string.report_reason_10)));
            mReportList.add(new Report(106, context.getString(R.string.report_reason_11)));

            mReportList.add(new Report(120, context.getString(R.string.report_reason_12)));
            mReportList.add(new Report(130, context.getString(R.string.report_reason_13)));
            mReportList.add(new Report(140, context.getString(R.string.report_reason_14)));
            mReportList.add(new Report(150, context.getString(R.string.report_reason_15)));
        }
        this.mOnReportListItemClickListener = onReportListItemClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_dialog);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        mReportListView = (ListView) findViewById(R.id.report_list);
        mReportAdapter = new ReportAdapter(mContext, mReportList);
        mReportListView.setAdapter(mReportAdapter);

        mReportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Report report = mReportList.get(position);
                if (mOnReportListItemClickListener != null && report != null) {
                    dismiss();
                    mOnReportListItemClickListener.onReportItemClick(report);
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

    public interface OnReportListItemClickListener {
        void onReportItemClick(Report report);
    }

    class ReportAdapter extends CommonAdapter<Report> {

        public ReportAdapter(Context context, List<Report> data) {
            super(context, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.row_report, position);
            TextView mReportTv = viewHolder.getView(R.id.report_tv);
            mReportTv.setText(data.get(position).getReportContent());
            return viewHolder.getConvertView();
        }
    }
}
