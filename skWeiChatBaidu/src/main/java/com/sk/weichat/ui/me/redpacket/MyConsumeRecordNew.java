package com.sk.weichat.ui.me.redpacket;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.redpacket.ConsumeRecordItemNew;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.mucfile.XfileUtils;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ViewHolder;
import com.sk.weichat.view.PinnedSectionListView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * Created by zq on 2020/1/8.
 * 新账单activity
 */
public class MyConsumeRecordNew extends BaseActivity {
    private static final int PAGE_SIZE = 50;
    private SmartRefreshLayout mSmartRefreshLayout;
    private PinnedSectionListView mListView;
    private ConstantAdapter mAdapter;
    private long startTime, endTime;
    private int mMaxYear, mMaxMonth;
    private int mCurrentYear, mCurrentMonth;
    private boolean isScrollPullNextMonth;// 上拉是否加载下一月
    private boolean isScrollDropNextMonth;// 下拉是否加载上一月
    private List<ConsumeRecordItemNew.RecordDataEntity> data = new ArrayList<>();

    /**
     * 日期转换成秒
     */
    public static long getSecondsFromDate(String expireDate) {
        if (expireDate == null || expireDate.trim().equals(""))
            return 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = sdf.parse(expireDate);
            return date.getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_pinned_list);
        initActionBar();
        initData();
        initView();
        loadData(true, true, true);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.bill));
    }

    private void initData() {
        String date = TimeUtils.sk_time_s_long_2_str(TimeUtils.sk_time_current_time());
        mMaxYear = TimeUtils.getYear(date);
        mMaxMonth = TimeUtils.getMonth(date) + 1;
        mCurrentYear = mMaxYear;
        mCurrentMonth = mMaxMonth;
        changeTime();
    }

    private void initView() {
        mSmartRefreshLayout = findViewById(R.id.refreshLayout);
        mListView = findViewById(R.id.list_view);
        mAdapter = new ConstantAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            ConsumeRecordItemNew.RecordDataEntity recordDataEntity = data.get(position);
            if (recordDataEntity.getItemType() == 1) {
                select(recordDataEntity.getYear(), recordDataEntity.getMonth());
            }
        });
        mSmartRefreshLayout.setOnRefreshListener(refreshLayout -> runOnUiThread(() -> {
            if (data == null || data.size() == 0) {
                return;
            }
            int year = data.get(0).getYear();
            int month = data.get(0).getMonth();
            if (year == mMaxYear && month == mMaxMonth) {
                initData();
                loadData(true, true, true);
                return;
            }
            mCurrentYear = year;
            mCurrentMonth = month;
            if (isScrollPullNextMonth) {// 加载下一月数据
                if (mCurrentMonth == 12) {
                    mCurrentYear = mCurrentYear + 1;
                    mCurrentMonth = 1;
                } else {
                    mCurrentMonth = mCurrentMonth + 1;
                }
                changeTime();
                loadData(false, isScrollPullNextMonth, false);
            } else {
                changeTime(true);
                loadData(false, isScrollPullNextMonth, false);
            }
        }));
        mSmartRefreshLayout.setOnLoadMoreListener(refreshLayout -> runOnUiThread(() -> {
            if (data == null || data.size() == 0) {
                return;
            }
            List<ConsumeRecordItemNew.RecordDataEntity> list = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getItemType() == 1) {
                    list.add(data.get(i));
                }
            }
            mCurrentYear = list.get(list.size() - 1).getYear();
            mCurrentMonth = list.get(list.size() - 1).getMonth();
            if (isScrollDropNextMonth) {// 加载上一月数据
                if (mCurrentMonth == 1) {
                    mCurrentYear = mCurrentYear - 1;
                    mCurrentMonth = 12;
                } else {
                    mCurrentMonth = mCurrentMonth - 1;
                }
                changeTime();
                loadData(true, isScrollDropNextMonth, false);
            } else {
                changeTime(false);
                loadData(true, isScrollDropNextMonth, false);
            }
        }));
    }

    public void loadData(boolean isNext, boolean needCount, boolean isSelectMonth) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("startTime", String.valueOf(startTime));
        params.put("endTime", String.valueOf(endTime));
        params.put("pageSize", String.valueOf(PAGE_SIZE));
        params.put("isNext", String.valueOf(isNext ? 1 : 0));// 1 下拉 0上拉
        params.put("needCount", String.valueOf(needCount ? 1 : 0));// 是否需要统计数据，// 1 是 0 否
        HttpUtils.get().url(coreManager.getConfig().CONSUMERECORD_GET_NEW)
                .params(params)
                .build()
                .execute(new BaseCallback<ConsumeRecordItemNew>(ConsumeRecordItemNew.class) {

                    @Override
                    public void onResponse(ObjectResult<ConsumeRecordItemNew> result) {
                        refreshComplete();
                        if (Result.checkSuccess(mContext, result)) {
                            if (isSelectMonth) {// 选择月份加载的数据，清空之前的数据
                                data.clear();
                                isScrollPullNextMonth = true;
                                isScrollDropNextMonth = true;
                            }

                            ConsumeRecordItemNew consumeRecordItemNew = result.getData();
                            List<ConsumeRecordItemNew.RecordDataEntity> recordList = consumeRecordItemNew.getRecordList();

                            if (!isNext) {// 上拉，数据倒序排序
                                Comparator<ConsumeRecordItemNew.RecordDataEntity> comparator = (o1, o2) -> (int) (o2.getTime() - o1.getTime());
                                Collections.sort(recordList, comparator);
                            }

                            if (!isNext) {// 上拉
                                isScrollPullNextMonth = recordList.size() < PAGE_SIZE;// 如不满一页，代表当前月份数据已全部加载完成，下次触发上拉时，获取下一月份的数据
                            } else {// 下拉
                                isScrollDropNextMonth = recordList.size() < PAGE_SIZE;// 如不满一页，代表当前月份数据已全部加载完成，下次触发上拉或下拉时，获取下一月份的数据
                            }

                            if (needCount) {// 获取统计数据，将统计数据也作为一行item
                                ConsumeRecordItemNew.RecordDataEntity recordDataEntity = new ConsumeRecordItemNew.RecordDataEntity();
                                recordDataEntity.setItemType(1);
                                recordDataEntity.setExpenses(consumeRecordItemNew.getExpenses());
                                recordDataEntity.setIncome(consumeRecordItemNew.getIncome());
                                recordDataEntity.setYear(mCurrentYear);
                                recordDataEntity.setMonth(mCurrentMonth);
                                recordList.add(0, recordDataEntity);
                            }

                            if (!isNext) {// 上拉
                                data.addAll(needCount ? 0 : 1, recordList);
                            } else {// 下拉
                                data.addAll(recordList);
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        refreshComplete();
                    }
                });
    }

    private void select(int year, int month) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(new ContextThemeWrapper(this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if (year == mCurrentYear && month + 1 == mCurrentMonth) {
                    // 与当前显示时间一致，不处理
                    return;
                }
                mCurrentYear = year;
                mCurrentMonth = month + 1;
                changeTime();
                loadData(true, true, true);
            }
        }, year, month - 1, 1) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                LinearLayout mSpinners = (LinearLayout) findViewById(getContext().getResources().getIdentifier("android:id/pickers", null, null));
                if (mSpinners != null) {
                    NumberPicker mYearSpinner = (NumberPicker) findViewById(getContext().getResources().getIdentifier("android:id/year", null, null));
                    NumberPicker mMonthSpinner = (NumberPicker) findViewById(getContext().getResources().getIdentifier("android:id/month", null, null));
                    mSpinners.removeAllViews();
                    if (mYearSpinner != null) {
                        mSpinners.addView(mYearSpinner);
                    }
                    if (mMonthSpinner != null) {
                        mSpinners.addView(mMonthSpinner);
                    }
                }
                View dayPickerView = findViewById(getContext().getResources().getIdentifier("android:id/day", null, null));
                if (dayPickerView != null) {
                    dayPickerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onDateChanged(DatePicker view, int year, int month, int day) {

            }
        };
        datePickerDialog.setTitle(getString(R.string.select_date));
        datePickerDialog.getDatePicker().setMaxDate(TimeUtils.sk_time_current_time() * 1000);
        datePickerDialog.show();
    }

    private void changeTime() {
        String start = mCurrentYear + "-" + mCurrentMonth + "-" + 1;
        startTime = getSecondsFromDate(start);
        String end;
        if (mCurrentMonth == 12) {
            end = String.valueOf(mCurrentYear + 1) + "-" + String.valueOf(1) + "-" + String.valueOf(1);
        } else {
            end = String.valueOf(mCurrentYear) + "-" + String.valueOf(mCurrentMonth + 1) + "-" + String.valueOf(1);
        }
        endTime = getSecondsFromDate(end);
    }

    private void changeTime(boolean isPull) {
        if (isPull) {// 上拉
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getItemType() == 0) {
                    startTime = data.get(i).getTime();
                    break;
                }
                String end;
                if (mCurrentMonth == 12) {
                    end = String.valueOf(mCurrentYear + 1) + "-" + String.valueOf(1) + "-" + String.valueOf(1);
                } else {
                    end = String.valueOf(mCurrentYear) + "-" + String.valueOf(mCurrentMonth + 1) + "-" + String.valueOf(1);
                }
                endTime = getSecondsFromDate(end);
            }
        } else {// 下拉
            String start = String.valueOf(mCurrentYear) + "-" + String.valueOf(mCurrentMonth) + "-" + String.valueOf(1);
            startTime = getSecondsFromDate(start);
            endTime = data.get(data.size() - 1).getTime();
        }
    }

    /**
     * 停止刷新动画
     */
    private void refreshComplete() {
        mListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSmartRefreshLayout.finishRefresh();
                mSmartRefreshLayout.finishLoadMore();
            }
        }, 200);
    }

    class ConstantAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter {

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return data.get(position).getItemType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.consumerecord_item, parent, false);
            }
            LinearLayout llBackGround = ViewHolder.get(convertView, R.id.llBackGround);
            TextView tvName = ViewHolder.get(convertView, R.id.textview_name);
            ImageView ivMore = ViewHolder.get(convertView, R.id.ivMore);
            TextView tvTime = ViewHolder.get(convertView, R.id.textview_time);
            TextView tvMoney = ViewHolder.get(convertView, R.id.textview_money);
            ImageView ivScan = ViewHolder.get(convertView, R.id.audit_iv);
            ConsumeRecordItemNew.RecordDataEntity recordDataEntity = data.get(position);
            if (recordDataEntity != null) {
                if (recordDataEntity.getItemType() == 1) {
                    llBackGround.setBackgroundColor(getResources().getColor(R.color.Grey_200));
                    ivMore.setVisibility(View.VISIBLE);
                    tvName.setText(getString(R.string.bill_total_time, recordDataEntity.getYear(), recordDataEntity.getMonth()));
                    tvTime.setText(getString(R.string.bill_total_money, recordDataEntity.getExpenses(), recordDataEntity.getIncome()));
                    tvMoney.setVisibility(View.GONE);
                    ivScan.setVisibility(View.GONE);
                } else {
                    llBackGround.setBackgroundColor(getResources().getColor(R.color.app_white));
                    ivMore.setVisibility(View.GONE);
                    tvMoney.setVisibility(View.VISIBLE);
                    tvName.setText(recordDataEntity.getDesc());
                    tvTime.setText(TimeUtils.f_long_2_str(recordDataEntity.getTime() * 1000));
                    if (recordDataEntity.getChangeType() == 1) {
                        tvMoney.setTextColor(getResources().getColor(R.color.ji_jian_lan));
                        tvMoney.setText("+" + XfileUtils.fromatFloat(recordDataEntity.getMoney()));
                    } else {
                        tvMoney.setTextColor(getResources().getColor(R.color.records_of_consumption));
                        tvMoney.setText("-" + XfileUtils.fromatFloat(recordDataEntity.getMoney()));
                    }
                    switch (recordDataEntity.getType()) {
                        case AppConstant.MANUAL_PAY_RECHARGE:
                            if (recordDataEntity.getManualPay_status() == -1) {// 审核拒绝
                                tvMoney.setTextColor(getResources().getColor(R.color.records_of_consumption));
                                tvMoney.setText(XfileUtils.fromatFloat(recordDataEntity.getMoney()));
                            }
                            break;
                        case AppConstant.MANUAL_PAY_WITHDRAW:
                            if (recordDataEntity.getManualPay_status() == -1) {// 审核拒绝
                                tvMoney.setTextColor(getResources().getColor(R.color.records_of_consumption));
                                tvMoney.setText(XfileUtils.fromatFloat(recordDataEntity.getMoney()));
                            }
                            break;
                    }
                    if (recordDataEntity.getType() == AppConstant.MANUAL_PAY_RECHARGE
                            || recordDataEntity.getType() == AppConstant.MANUAL_PAY_WITHDRAW) {
                        ivScan.setImageResource(recordDataEntity.getManualPay_status() == -1
                                ? R.mipmap.audit_reject_icon : R.mipmap.audit_pass_icon);
                        ivScan.setVisibility(View.VISIBLE);
                    } else {
                        ivScan.setVisibility(View.GONE);
                    }
                }
            }
            return convertView;
        }

        @Override
        public boolean isItemViewTypePinned(int viewType) {
            return viewType == 1;
        }
    }
}
