package com.sk.weichat.ui.me.redpacket.scan;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.R;
import com.sk.weichat.bean.redpacket.ScanWithDrawSelectType;
import com.sk.weichat.ui.base.BaseListActivity;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * 选择提现方式
 */
public class ScanWithdrawListActivity extends BaseListActivity {
    List<ScanWithDrawSelectType> scanWithDrawSelectTypes = new ArrayList<>();
    private boolean isEdit;
    private boolean isResumed;

    @Override
    protected void onResume() {
        super.onResume();
        if (isResumed) {
            initDatas(0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = true;
    }

    @Override
    public void initView() {
        // 添加两个空数据，表示去添加item
        scanWithDrawSelectTypes.add(new ScanWithDrawSelectType());
        scanWithDrawSelectTypes.add(new ScanWithDrawSelectType());
        initActionBar();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.select_withdraw_type));
        TextView mTvRightTitle = findViewById(R.id.tv_title_right);
        mTvRightTitle.setText(getString(R.string.edit));
        mTvRightTitle.setOnClickListener(v -> {
            isEdit = !isEdit;
            mTvRightTitle.setText(isEdit ? getString(R.string.cancel) : getString(R.string.edit));
            notifyDataSetChanged();
        });
    }

    @Override
    public void initDatas(int pager) {
        HashMap<String, String> params = new HashMap<>();
        params.put("pageIndex", String.valueOf(pager));
        params.put("pageSize", String.valueOf(PAGE_SIZE));
        HttpUtils.get().url(coreManager.getConfig().MANUAL_PAY_GET_WITHDRAW_ACCOUNT_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<ScanWithDrawSelectType>(ScanWithDrawSelectType.class) {
                    @Override
                    public void onResponse(ArrayResult<ScanWithDrawSelectType> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            if (pager == 0) {
                                scanWithDrawSelectTypes.clear();
                                scanWithDrawSelectTypes.add(new ScanWithDrawSelectType());
                                scanWithDrawSelectTypes.add(new ScanWithDrawSelectType());
                            }
                            scanWithDrawSelectTypes.addAll(result.getData());
                            update(scanWithDrawSelectTypes);
                            if (result.getData().size() != PAGE_SIZE) {
                                more = false;
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        update(scanWithDrawSelectTypes);
                    }
                });
    }

    @Override
    public RecyclerView.ViewHolder initHolder(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.item_scan_withdraw_add, parent, false);
        return new ScanWithDrawSelectTypeViewHolder(v);
    }

    @Override
    public void fillData(RecyclerView.ViewHolder holder, int position) {
        ScanWithDrawSelectTypeViewHolder drawSelectTypeViewHolder = (ScanWithDrawSelectTypeViewHolder) holder;
        if (position == 0 || position == 1) {
            drawSelectTypeViewHolder.addTv.setVisibility(View.VISIBLE);
            drawSelectTypeViewHolder.typeIv.setVisibility(View.GONE);
            drawSelectTypeViewHolder.typeTv.setVisibility(View.GONE);
            drawSelectTypeViewHolder.nextIv.setVisibility(View.GONE);
            drawSelectTypeViewHolder.addTv.setText(position == 0 ? getString(R.string.select_withdraw_add_alipay_account) : getString(R.string.select_withdraw_add_band_card_account));
            drawSelectTypeViewHolder.item.setOnClickListener(v -> ScanWithdrawAddActivity.start(mContext, position + 1));
        } else {
            ScanWithDrawSelectType drawSelectType = scanWithDrawSelectTypes.get(position);
            drawSelectTypeViewHolder.addTv.setVisibility(View.GONE);
            drawSelectTypeViewHolder.typeIv.setVisibility(View.VISIBLE);
            drawSelectTypeViewHolder.typeTv.setVisibility(View.VISIBLE);
            drawSelectTypeViewHolder.nextIv.setVisibility(isEdit ? View.VISIBLE : View.GONE);
            if (drawSelectType.getType() == 1) {//支付宝
                drawSelectTypeViewHolder.typeIv.setImageResource(R.mipmap.ic_alipay_small);
                drawSelectTypeViewHolder.typeTv.setText(drawSelectType.getAliPayAccount());
            } else {// 银行卡
                drawSelectTypeViewHolder.typeIv.setImageResource(R.mipmap.ic_band_small);
                drawSelectTypeViewHolder.typeTv.setText(drawSelectType.getBankName() + "(" + drawSelectType.getBankCardNo() + ")");
            }
            drawSelectTypeViewHolder.item.setOnClickListener(v -> {
                if (isEdit) {
                    ScanWithdrawUpdateActivity.start(mContext, JSON.toJSONString(drawSelectType));
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("drawSelectType", JSON.toJSONString(drawSelectType));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }
    }

    public class ScanWithDrawSelectTypeViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout item;
        public TextView addTv;
        public ImageView typeIv;
        public TextView typeTv;
        public ImageView nextIv;

        public ScanWithDrawSelectTypeViewHolder(View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.item_add);
            addTv = itemView.findViewById(R.id.add_tv);
            typeIv = itemView.findViewById(R.id.type_iv);
            typeTv = itemView.findViewById(R.id.type_tv);
            nextIv = itemView.findViewById(R.id.next_iv);
        }
    }
}
