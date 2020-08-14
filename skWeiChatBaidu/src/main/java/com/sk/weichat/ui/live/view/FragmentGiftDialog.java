package com.sk.weichat.ui.live.view;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.redpacket.Balance;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.base.BaseDialogFragment;
import com.sk.weichat.ui.live.LiveConstants;
import com.sk.weichat.ui.live.bean.Gift;
import com.sk.weichat.ui.me.redpacket.WxPayBlance;
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
 * 直播间-礼物列表
 */
public class FragmentGiftDialog extends BaseDialogFragment {
    // 表示刷新我的余额
    private final int REFRESH_MONEY = 1;
    public OnGridViewClickListener onGridViewClickListener;
    private Dialog dialog;
    private TextView payNum;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_MONEY: {
                    int balance = (int) coreManager.getSelf().getBalance();
                    payNum.setText(String.valueOf(balance));
                }
                break;
            }
            super.handleMessage(msg);
        }
    };
    private Button goPay;
    private ViewPager vp;
    // 礼物
    private List<Gift> mGifts;
    /**
     * 用于通知更新余额的广播
     */
    private BroadcastReceiver changeSomeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(LiveConstants.LIVE_SEND_REFRESH_MONEY)) {
                Message msg = new Message();
                msg.what = REFRESH_MONEY;
                mHandler.sendMessage(msg);
            }
        }
    };

    public static final FragmentGiftDialog newInstance() {
        FragmentGiftDialog fragment = new FragmentGiftDialog();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    public FragmentGiftDialog setOnGridViewClickListener(ArrayList<Gift> gifts, OnGridViewClickListener onGridViewClickListener) {
        this.mGifts = gifts;
        this.onGridViewClickListener = onGridViewClickListener;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.common_gift_dialog_layout, container, false);
        initView(rootView);
        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 创建Dialog时没有保证LayoutInflater可用，所以不能在这里创建View,
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LiveConstants.LIVE_SEND_REFRESH_MONEY);
        getActivity().registerReceiver(changeSomeBroadcastReceiver, intentFilter);
        // 使用不带Theme的构造器, 获得的dialog边框距离屏幕仍有几毫米的缝隙。
        initDialogStyle();
        return dialog;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 这里确保View已经添加到Dialog才能修改LayoutParams，宽全屏才会生效，
        // 设置宽度为屏宽, 靠近屏幕底部。
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        // 底部显示
        lp.gravity = Gravity.BOTTOM;
        // 宽度持平
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        initViewPager();
        // 更新余额
        updateMoney();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(changeSomeBroadcastReceiver);
    }

    private void initDialogStyle() {
        dialog = new Dialog(getActivity(), R.style.CustomGiftDialog);
        // 设置Content前设定
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 外部点击取消
        dialog.setCanceledOnTouchOutside(true);
    }

    private void initView(View rootView) {
        Bundle args = getArguments();
        if (args == null)
            return;
        payNum = (TextView) rootView.findViewById(R.id.pay_num);
        goPay = (Button) rootView.findViewById(R.id.go_pay);
        goPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 下次进来才会触发init()方法
                dialog.dismiss();
                // 充值
                startActivity(new Intent(getActivity(), WxPayBlance.class));
            }
        });
        vp = (ViewPager) rootView.findViewById(R.id.view_pager);
        ((TabLayout) rootView.findViewById(R.id.tabDots)).setupWithViewPager(vp, false);
    }

    public void initViewPager() {
        if (mGifts == null || mGifts.size() <= 0) {
            ToastUtil.showToast(MyApplication.getContext(), R.string.tip_get_gift_list_failed);
            return;
        }
        vp.setAdapter(new GridPagerAdapter(
                mGifts, 4, 2,
                data -> new PagerGridAdapter(
                        data,
                        item -> {
                            if (onGridViewClickListener != null) {
                                onGridViewClickListener.click(item);
                            }
                            updateMoney();
                        }
                )
        ));
    }

    // 更新余额
    // TODO: 马上更新没用，消费和查询同时异步进行的无法知道查到的是消费后还是消费前的，
    public void updateMoney() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        HttpUtils.get().url(coreManager.getConfig().RECHARGE_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<Balance>(Balance.class) {

                    @Override
                    public void onResponse(ObjectResult<Balance> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            Balance balance = result.getData();
                            coreManager.getSelf().setBalance(balance.getBalance());

                            Message msg = new Message();
                            msg.what = REFRESH_MONEY;
                            mHandler.sendMessage(msg);
                        } else {
                            ToastUtil.showErrorData(MyApplication.getContext());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(MyApplication.getContext());
                    }
                });
    }

    interface PagerGridAdapterFactory<T> {
        ListAdapter createPagerGridAdapter(List<T> data);
    }

    public interface OnGridViewClickListener {
        void click(Gift gift);
    }

    static class GridPagerAdapter extends PagerAdapter {
        private final int columnCount;
        private final PagerGridAdapterFactory<Gift> factory;
        private final int pageSize;
        private List<Gift> data;

        GridPagerAdapter(
                List<Gift> data,
                int columnCount,
                int rowCount,
                PagerGridAdapterFactory<Gift> factory
        ) {
            this.data = new ArrayList<>(data);
            this.columnCount = columnCount;
            this.factory = factory;

            pageSize = rowCount * columnCount;
        }

        private List<Gift> getPageData(int page) {
            return data.subList(page * pageSize, Math.min(((page + 1) * pageSize), data.size()));
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            GridView grid = new GridView(container.getContext());
            grid.setNumColumns(columnCount);
            grid.setAdapter(factory.createPagerGridAdapter(getPageData(position)));
            container.addView(grid, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            // 没有设置itemClick和item background，但还是会有点击效果，
            return grid;

        }

        @Override
        public int getCount() {
            return (data.size() + (pageSize - 1)) / pageSize;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }

    static class PagerGridAdapter extends BaseAdapter {
        private final List<Gift> data;
        private OnGridViewClickListener clickListener;

        PagerGridAdapter(List<Gift> data, OnGridViewClickListener clickListener) {
            this.data = data;
            this.clickListener = clickListener;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Gift getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View ret = convertView;
            ViewHolder viewHolder;
            if (ret == null) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gift, parent, false);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
                ret = view;
            } else {
                viewHolder = (ViewHolder) ret.getTag();
            }
            Gift gift = getItem(position);
            ret.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.click(gift);
                }
            });
            AvatarHelper.getInstance().displayUrl(gift.getPhoto(), viewHolder.grid_fragment_home_item_img);
            viewHolder.grid_fragment_home_item_txt.setText(String.valueOf(gift.getPrice()));
            return ret;
        }
    }

    static class ViewHolder {
        final ImageView grid_fragment_home_item_img;
        final TextView grid_fragment_home_item_txt;

        public ViewHolder(View itemView) {
            grid_fragment_home_item_img = itemView.findViewById(R.id.grid_fragment_home_item_img);
            grid_fragment_home_item_txt = itemView.findViewById(R.id.grid_fragment_home_item_txt);

        }
    }
}
