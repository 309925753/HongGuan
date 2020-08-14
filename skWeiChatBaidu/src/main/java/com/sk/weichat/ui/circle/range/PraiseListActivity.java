package com.sk.weichat.ui.circle.range;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.circle.Praise;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.base.BaseListActivity;
import com.sk.weichat.ui.other.BasicInfoActivity;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.HeadView;
import com.sk.weichat.view.NoLastDividerItemDecoration;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;

/**
 * 点赞列表单独一页做分页加载，
 */
public class PraiseListActivity extends BaseListActivity<PraiseListActivity.MyViewHolder> {
    private String messageId;
    private List<Praise> praises = new ArrayList<>();

    public static void start(Context ctx, String messageId) {
        Intent intent = new Intent(ctx, PraiseListActivity.class);
        intent.putExtra("messageId", messageId);
        ctx.startActivity(intent);
    }

    @Override
    public void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.praise_list);

        messageId = getIntent().getStringExtra("messageId");

        // 添加分割线
        NoLastDividerItemDecoration dividerSearchHistory = new NoLastDividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerSearchHistory.setDrawable(getResources().getDrawable(R.drawable.message_divider));
        mRecyclerView.addItemDecoration(dividerSearchHistory);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void initDatas(int pager) {
        loadData(pager);
    }

    private void loadData(int pager) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageIndex", String.valueOf(pager));
        params.put("pageSize", String.valueOf(PAGE_SIZE));
        params.put("messageId", messageId);

        String url = coreManager.getConfig().MSG_PRAISE_LIST;
        HttpUtils.get().url(url)
                .params(params)
                .build()
                .execute(new ListCallback<Praise>(Praise.class) {
                    @Override
                    public void onResponse(ArrayResult<Praise> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            if (pager == 0) {
                                praises = result.getData();
                            } else {
                                praises.addAll(result.getData());
                            }
                            update(praises);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Reporter.post("点赞分页加载失败，", e);
                        ToastUtil.showToast(getApplicationContext(), getString(R.string.tip_praise_load_error));
                    }
                });
    }

    @Override
    public PraiseListActivity.MyViewHolder initHolder(ViewGroup parent) {
        View view = mInflater.inflate(R.layout.item_praise, parent, false);
        PraiseListActivity.MyViewHolder holder = new PraiseListActivity.MyViewHolder(view);
        return holder;
    }

    @Override
    public void fillData(MyViewHolder holder, int position) {
        Praise item = praises.get(position);
        AvatarHelper.getInstance().displayAvatar(item.getNickName(), item.getUserId(), holder.hvHead.getHeadImage(), false);
        holder.tvName.setText(item.getNickName());
        holder.tvTime.setText(DateUtils.getRelativeTimeSpanString(
                item.getTime() * TimeUnit.SECONDS.toMillis(1),
                System.currentTimeMillis(),
                TimeUnit.SECONDS.toMillis(1))
        );
        holder.itemView.setOnClickListener(v -> {
            BasicInfoActivity.start(v.getContext(), item.getUserId());
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private HeadView hvHead;
        private TextView tvName;
        private TextView tvTime;

        MyViewHolder(View itemView) {
            super(itemView);
            hvHead = itemView.findViewById(R.id.hvHead);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
