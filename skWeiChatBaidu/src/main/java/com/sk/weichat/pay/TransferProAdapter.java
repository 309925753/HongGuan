package com.sk.weichat.pay;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.TransferRecord;
import com.sk.weichat.ui.base.BaseRecViewHolder;
import com.sk.weichat.util.DateFormatUtil;
import com.sk.weichat.util.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// 增加头部的recyaler适配器
public class TransferProAdapter extends RecyclerView.Adapter {
    private static final int HEADER_TYPE = 1;
    private static final int NORMAL_TYPE = 0;

    private List<View> mHeaderViews;
    private Context mContext;
    private List<TransferRecord.DataBean.PageDataBean> mTransfers;

    public TransferProAdapter(Context context, List<TransferRecord.DataBean.PageDataBean> transfers) {
        this.mContext = context;
        this.mTransfers = transfers;
        mHeaderViews = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        if (viewType == HEADER_TYPE) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.transter_head, viewGroup, false);
            viewHolder = new HeadViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.transter_adapter, viewGroup, false);
            viewHolder = new TransferViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof HeadViewHolder) {
            ((HeadViewHolder) viewHolder).onBind(mTransfers, position);
        } else if (viewHolder instanceof TransferViewHolder) {
            ((TransferViewHolder) viewHolder).onBind(mTransfers, position - mHeaderViews.size());
        }
    }

    @Override
    public int getItemCount() {
        return mTransfers.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mTransfers.get(position).isTitle())
            return HEADER_TYPE;
        else
            return NORMAL_TYPE;
    }

    /**
     * 增加转账头部记录总数
     */
    class HeadViewHolder extends BaseRecViewHolder {
        TextView tv_in_sum, tv_to_sum, tv_month;

        public HeadViewHolder(View itemView) {
            super(itemView);
            tv_month = itemView.findViewById(R.id.tv_month);
            tv_to_sum = itemView.findViewById(R.id.tv_to_sum);
            tv_in_sum = itemView.findViewById(R.id.tv_in_sum);
        }

        public void onBind(List<TransferRecord.DataBean.PageDataBean> list, int position) {
            TransferRecord.DataBean.PageDataBean bean = list.get(position);
            int month = list.get(position).getMonth();
            if ((month - 1) == Calendar.getInstance().get(Calendar.MONTH)) {
                tv_month.setText(MyApplication.getContext().getString(R.string.this_month));
            } else {
                tv_month.setText(MyApplication.getContext().getString(R.string.month, month));
            }

            String inP = StringUtils.getMoney(bean.getTotalInMoney());
            String toP = StringUtils.getMoney(bean.getTotalOutMoney());

            tv_in_sum.setText(inP);
            tv_to_sum.setText(toP);
        }
    }

    class TransferViewHolder extends BaseRecViewHolder {
        public TextView tv_date;
        public TextView tv_balance;
        public TextView tv_title_transfer;
        public TextView tv_receipt;
        public View view;

        public TransferViewHolder(View itemView) {
            super(itemView);
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_balance = itemView.findViewById(R.id.tv_balance);
            tv_receipt = itemView.findViewById(R.id.tv_receipt);
            tv_title_transfer = itemView.findViewById(R.id.tv_title_transfer);
            view = itemView.findViewById(R.id.view);
        }

        public void onBind(List<TransferRecord.DataBean.PageDataBean> list, int position) {
            tv_title_transfer.setText(list.get(position).getDesc());//类型
            tv_receipt.setText(list.get(position).getStatus() == 1 ? MyApplication.getContext().getResources().getString(R.string.jiao_yi_c_g) :
                    MyApplication.getContext().getResources().getString(R.string.jiao_yi_s_b));//状态

            String data = DateFormatUtil.timedate(list.get(position).getTime());//时间
            tv_date.setText(data);
            switch (list.get(position).getType()) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 7:
                case 10:
                case 12:
                    tv_balance.setTextColor(MyApplication.getContext().getResources().getColor(R.color.records_of_consumption));
                    tv_balance.setText(String.valueOf("-" + list.get(position).getMoney()));//支出金额
                    break;
                default:
                    tv_balance.setTextColor(MyApplication.getContext().getResources().getColor(R.color.records));
                    tv_balance.setText(String.valueOf("+" + list.get(position).getMoney()));//收入金额
                    break;
            }

            // 控制底部横线显示/隐藏
            if (position == mTransfers.size() - 1) {
                view.setVisibility(View.GONE);
            } else if (mTransfers.get(position + 1).isTitle()) {
                view.setVisibility(View.GONE);
            } else {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

}
