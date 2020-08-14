package com.sk.weichat.ui.message;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.collection.Collectiion;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.SingleImagePreviewActivity;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Administrator on 2018/3/9 0009.
 */

public class ManagerEmojiActivity extends BaseActivity {
    boolean isEditOrSee;
    private RecyclerView rcyv;
    private MyAdapter mAdapter;
    private List<Collectiion> mData;
    private List<String> mReadyData;
    private TextView tv1, tv2, tv3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_emoji);
        mData = (List<Collectiion>) getIntent().getSerializableExtra("list");
        if (!mData.isEmpty()) {
            // 静态变量不稳定，可能什么情况数组空了，有这里size 0崩溃bugly#11567
            mData.remove(0);
        }
        mReadyData = new ArrayList<>();

        initActionBar();
        initView();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.title_my_collection_emoji);
        final TextView tvTitleRight = (TextView) findViewById(R.id.tv_title_right);
        tvTitleRight.setText(R.string.edit);
        tvTitleRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditOrSee = !isEditOrSee;
                if (isEditOrSee) {
                    findViewById(R.id.rl_rl).setVisibility(View.VISIBLE);
                    tvTitleRight.setText(R.string.cancel);
                } else {
                    findViewById(R.id.rl_rl).setVisibility(View.GONE);
                    tvTitleRight.setText(R.string.edit);
                    updateData(false);
                }
            }
        });
    }

    private void initView() {
        tv1 = (TextView) findViewById(R.id.al_tv);
        tv2 = (TextView) findViewById(R.id.sl_tv);
        tv3 = (TextView) findViewById(R.id.dl_tv);

        mAdapter = new MyAdapter();
        rcyv = (RecyclerView) findViewById(R.id.emoji_recycle);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 4);
        rcyv.setLayoutManager(layoutManager);
        rcyv.setAdapter(mAdapter);
        mAdapter.setmOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Collectiion C = mData.get(position);
                if (!isEditOrSee) {
                    Intent intent = new Intent(mContext, SingleImagePreviewActivity.class);
                    intent.putExtra(AppConstant.EXTRA_IMAGE_URI, C.getUrl());
                    mContext.startActivity(intent);
                } else {
                    updateSingleData(C, position);
                }
            }
        });
    }

    private void initEvent() {
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData(true);
            }
        });

        tv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.showDefaulteMessageProgressDialog(ManagerEmojiActivity.this);
                String idList = "";
                for (int i = 0; i < mReadyData.size(); i++) {
                    if (i == mReadyData.size() - 1) {
                        // deleteMyCollection(mReadyData.get(i), true);
                        idList += mReadyData.get(i);
                    } else {
                        // deleteMyCollection(mReadyData.get(i), false);
                        idList += mReadyData.get(i) + ",";
                    }
                }
                deleteMyCollection(idList);
            }
        });
    }

    // 更新单个item
    public void updateSingleData(Collectiion C, int position) {
        if (C.getType() == 8) {
            C.setType(0);
            mReadyData.remove(C.getEmojiId());
        } else {
            C.setType(8);
            mReadyData.add(C.getEmojiId());
        }
        mData.remove(position);
        mData.add(position, C);
        mAdapter.notifyItemChanged(position);
        updateUI();
    }

    // 更新所有item 全选 || 取消
    public void updateData(boolean isAllSelectOrCancel) {
        mReadyData.clear();
        for (int i = 0; i < mData.size(); i++) {
            Collectiion C = mData.get(i);
            if (isAllSelectOrCancel) {
                C.setType(8);
                mReadyData.add(mData.get(i).getEmojiId());
            } else {
                C.setType(0);
            }
            mData.remove(i);
            mData.add(i, C);
        }
        mAdapter.notifyDataSetChanged();
        updateUI();
    }

    // 更新底部ui
    public void updateUI() {
        if (mReadyData != null) {
            if (mReadyData.size() > 0) {
                tv3.setVisibility(View.VISIBLE);
                tv2.setText("选中表情 (" + mReadyData.size() + ")");
            } else {
                tv3.setVisibility(View.GONE);
                tv2.setText("选中表情 (" + 0 + ")");
            }
        }
    }

    public void deleteMyCollection(String emojiId) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("emojiId", emojiId);

        HttpUtils.get().url(coreManager.getConfig().Collection_REMOVE)
                .params(params)
                .build()
                .execute(new BaseCallback<Collectiion>(Collectiion.class) {

                    @Override
                    public void onResponse(ObjectResult<Collectiion> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            Toast.makeText(mContext, mContext.getString(R.string.delete_all_succ), Toast.LENGTH_SHORT).show();
                            for (int i = 0; i < mReadyData.size(); i++) {
                                for (int i1 = 0; i1 < mData.size(); i1++) {
                                    if (mReadyData.get(i).equals(mData.get(i1).getEmojiId())) {
                                        mData.remove(i1);
                                    }
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                            mReadyData.clear();
                            updateUI();
                            ManagerEmojiActivity.this.sendBroadcast(new Intent(com.sk.weichat.broadcast.OtherBroadcast.CollectionRefresh));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private OnItemClickListener onItemClickListener;

        public void setmOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_ma_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Collectiion collection = mData.get(position);
            if (collection.getUrl().endsWith(".gif")) {
                holder.iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ImageLoadHelper.showGif(
                        ManagerEmojiActivity.this,
                        collection.getUrl(),
                        holder.iv
                );
            } else if (collection.getUrl().endsWith("jpg") || collection.getUrl().endsWith("png")) {
                holder.iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ImageLoadHelper.showImageDontAnimateWithPlaceHolder(
                        ManagerEmojiActivity.this,
                        collection.getUrl(),
                        R.drawable.ffb,
                        R.drawable.fez,
                        holder.iv
                );
            }

            if (collection.getType() == 8) {
                holder.ck.setVisibility(View.VISIBLE);
                holder.iv.setAlpha(0.4f);
            } else {
                holder.ck.setVisibility(View.GONE);
                holder.iv.setAlpha(1.0f);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = holder.getLayoutPosition();
                        onItemClickListener.onItemClick(holder.itemView, position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView iv;
            CheckBox ck;

            public ViewHolder(View itemView) {
                super(itemView);
                ck = (CheckBox) itemView.findViewById(R.id.cl_ck);
                iv = (ImageView) itemView.findViewById(R.id.cl_iv);
            }
        }
    }
}
