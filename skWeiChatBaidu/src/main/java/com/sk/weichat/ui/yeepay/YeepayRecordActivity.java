package com.sk.weichat.ui.yeepay;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.R;
import com.sk.weichat.bean.redpacket.ConsumeRecordItem;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseListActivity;
import com.sk.weichat.ui.mucfile.XfileUtils;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * Created by wzw on 2016/9/26.
 */
public class YeepayRecordActivity extends BaseListActivity<YeepayRecordActivity.MyConsumeHolder> {
    private static final String TAG = "YeepayRecordActivity";
    List<ConsumeRecordItem.PageDataEntity> datas = new ArrayList<>();

    public static void start(Context context) {
        Intent starter = new Intent(context, YeepayRecordActivity.class);
        context.startActivity(starter);
    }

    @Nullable
    @Override
    protected Integer getMiddleDivider() {
        return R.drawable.divider_consume_record;
    }

    @Override
    public void initView() {
        super.initView();
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getResources().getString(R.string.bill));
    }

    @Override
    public void initDatas(int pager) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        // 如果是下拉刷新就重新加载第一页
        params.put("pageIndex", String.valueOf(pager));
        params.put("pageSize", String.valueOf(PAGE_SIZE));
        HttpUtils.get().url(coreManager.getConfig().YOP_RECORD_RED)
                .params(params)
                .build()
                .execute(new BaseCallback<ConsumeRecordItem>(ConsumeRecordItem.class) {

                    @Override
                    public void onResponse(ObjectResult<ConsumeRecordItem> result) {
                        if (result.getData() != null
                                && result.getData().getPageData() != null) {
                            if (pager == 0) {
                                datas.clear();
                            }
                            for (ConsumeRecordItem.PageDataEntity data : result.getData().getPageData()) {
                                final double money = data.getMoney();
                                boolean isZero = Double.toString(money).equals("0.0");
                                Log.d(TAG, "bool : " + isZero + " \t" + money);
                                if (!isZero) {
                                    datas.add(data);
                                }
                            }
                            if (result.getData().getPageData().size() != PAGE_SIZE) {
                                more = false;
                            } else {
                                more = true;
                            }
                        } else {
                            more = false;
                        }
                        runOnUiThread(() -> update(datas));
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(YeepayRecordActivity.this);
                    }
                });
    }

    @Override
    public MyConsumeHolder initHolder(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.consumerecord_item, parent, false);
        MyConsumeHolder holder = new MyConsumeHolder(v);
        return holder;
    }

    @Override
    public void fillData(MyConsumeHolder holder, int position) {
        ConsumeRecordItem.PageDataEntity info = datas.get(position);
        if (info != null) {
            String StrTime = TimeUtils.f_long_2_str(info.getTime() * 1000);
            holder.timeTv.setText(StrTime);
            // 1.充值 2.提现 3.转账 4.接收转账 5.发一对一红包 6.发群普通红包 7.发拼手气红包 8.收红包 9, 提现服务费
            String[] stringArray = getResources().getStringArray(R.array.yeepay_record_type);
            holder.nameTv.setText(stringArray[info.getType() - 1]);
            switch (info.getType()) {
                case 1:
                case 4:
                case 8:
                    holder.moneyTv.setTextColor(getResources().getColor(R.color.ji_jian_lan));
                    holder.moneyTv.setText("+" + XfileUtils.fromatFloat(info.getMoney()));
                    break;
                default:
                    holder.moneyTv.setTextColor(getResources().getColor(R.color.records_of_consumption));
                    holder.moneyTv.setText("-" + XfileUtils.fromatFloat(info.getMoney()));
                    break;
            }
        }
    }

    class MyConsumeHolder extends RecyclerView.ViewHolder {
        private TextView nameTv, timeTv, moneyTv;

        MyConsumeHolder(View itemView) {
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.textview_name);
            timeTv = (TextView) itemView.findViewById(R.id.textview_time);
            moneyTv = (TextView) itemView.findViewById(R.id.textview_money);
        }
    }
}
