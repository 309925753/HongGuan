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
 * 投诉
 */
public class ComplaintDialog extends Dialog {

    private OnReportListItemClickListener mOnReportListItemClickListener;
    private Context mContext;
    private List<Report> mReportList = new ArrayList<>();
    private ListView mReportListView;
    private ReportAdapter mReportAdapter;

    public ComplaintDialog(Context context, OnReportListItemClickListener onReportListItemClickListener) {
        super(context, R.style.BottomDialog);
        this.mContext = context;
        mReportList.add(new Report(300, context.getString(R.string.complaint_reason_1)));
        mReportList.add(new Report(301, context.getString(R.string.complaint_reason_2)));
        mReportList.add(new Report(302, context.getString(R.string.complaint_reason_3)));
        mReportList.add(new Report(303, context.getString(R.string.complaint_reason_4)));
        mReportList.add(new Report(304, context.getString(R.string.complaint_reason_5)));
        mReportList.add(new Report(305, context.getString(R.string.complaint_reason_6)));
        mReportList.add(new Report(306, context.getString(R.string.complaint_reason_7)));
        mReportList.add(new Report(307, context.getString(R.string.complaint_reason_8)));
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
