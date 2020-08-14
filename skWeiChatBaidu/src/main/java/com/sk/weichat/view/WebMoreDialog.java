package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.R;
import com.sk.weichat.ui.tool.WebViewActivity;
import com.sk.weichat.util.ScreenUtil;

import java.util.Arrays;
import java.util.List;

import okhttp3.HttpUrl;

public class WebMoreDialog extends Dialog {
    private Context mContent;
    private String mUrl;

    private TextView mBrowserProvideTv;
    private RecyclerView mBrowserRecycleView;
    private BrowserActionAdapter mBrowserActionAdapter;
    private List<Item> mData;

    private BrowserActionClickListener mBrowserActionClickListener;

    public WebMoreDialog(Context context, String url, BrowserActionClickListener browserActionClickListener) {
        super(context, R.style.BottomDialog);
        this.mContent = context;
        this.mUrl = url;
        this.mBrowserActionClickListener = browserActionClickListener;
        mData = getData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_browser);

        mBrowserProvideTv = findViewById(R.id.browser_provide_tv);
        HttpUrl httpUrl = HttpUrl.parse(mUrl);
        if (httpUrl != null) {
            mBrowserProvideTv.setText("网页由 " + httpUrl.host() + " 提供");
        } else {
            mBrowserProvideTv.setText("网页由 " + mUrl + " 提供");
        }
        mBrowserRecycleView = findViewById(R.id.browser_ry);
        mBrowserRecycleView.setLayoutManager(new GridLayoutManager(mContent, 4));
        mBrowserActionAdapter = new BrowserActionAdapter();
        mBrowserRecycleView.setAdapter(mBrowserActionAdapter);

        setCanceledOnTouchOutside(true);

        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        lp.width = ScreenUtil.getScreenWidth(getContext());
        lp.height = ScreenUtil.getScreenHeight(getContext()) / 3 + ScreenUtil.dip2px(getContext(), 90);
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.BOTTOM);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }

    private List<Item> getData() {
        return Arrays.asList(
                new Item(!WebViewActivity.IS_FLOATING ? R.mipmap.floating_window : R.mipmap.floating_window_icon,
                        !WebViewActivity.IS_FLOATING ? R.string.floating_window : R.string.floating_window_cancel, () -> {
                    if (mBrowserActionClickListener != null) {
                        dismiss();
                        mBrowserActionClickListener.floatingWindow();
                    }
                }),
                new Item(R.mipmap.share_friend_icon, R.string.send_to_friend, () -> {
                    if (mBrowserActionClickListener != null) {
                        dismiss();
                        mBrowserActionClickListener.sendToFriend();
                    }
                }),
                new Item(R.mipmap.life_ircle_share_icon, R.string.share_to_life_circle, () -> {
                    if (mBrowserActionClickListener != null) {
                        dismiss();
                        mBrowserActionClickListener.shareToLifeCircle();
                    }
                }),
                new Item(R.mipmap.safari_icon, R.string.open_outside, () -> {
                    if (mBrowserActionClickListener != null) {
                        dismiss();
                        mBrowserActionClickListener.openOutSide();
                    }
                }),
                new Item(R.mipmap.send_out_wechat_icon, R.string.share_wechat, () -> {
                    if (mBrowserActionClickListener != null) {
                        dismiss();
                        mBrowserActionClickListener.shareWechat();
                    }
                }),
                new Item(R.mipmap.share_circle_of_friends, R.string.share_moments, () -> {
                    if (mBrowserActionClickListener != null) {
                        dismiss();
                        mBrowserActionClickListener.shareWechatMoments();
                    }
                }),
                new Item(R.mipmap.collection_preservation_icon, R.string.collection, () -> {
                    if (mBrowserActionClickListener != null) {
                        dismiss();
                        mBrowserActionClickListener.collection();
                    }
                }),
                new Item(R.mipmap.complain_complaint_icon, R.string.complaint, () -> {
                    if (mBrowserActionClickListener != null) {
                        dismiss();
                        mBrowserActionClickListener.complaint();
                    }
                }),
                new Item(R.mipmap.copy_link_icon, R.string.copy_link, () -> {
                    if (mBrowserActionClickListener != null) {
                        dismiss();
                        mBrowserActionClickListener.copyLink();
                    }
                }),
                new Item(R.mipmap.refresh_update_icon, R.string.refresh, () -> {
                    if (mBrowserActionClickListener != null) {
                        dismiss();
                        mBrowserActionClickListener.refresh();
                    }
                }),
                new Item(R.mipmap.search_content_icon, R.string.search_paper_content, () -> {
                    if (mBrowserActionClickListener != null) {
                        dismiss();
                        mBrowserActionClickListener.searchContent();
                    }
                }),
                new Item(R.mipmap.typeface_icon, R.string.modify_font_size, () -> {
                    if (mBrowserActionClickListener != null) {
                        dismiss();
                        mBrowserActionClickListener.modifyFontSize();
                    }
                })
        );
    }

    public interface BrowserActionClickListener {
        void floatingWindow();

        void sendToFriend();

        void shareToLifeCircle();

        void collection();

        void searchContent();

        void copyLink();

        void openOutSide();

        void modifyFontSize();

        void refresh();

        void complaint();

        void shareWechat();

        void shareWechatMoments();
    }

    class BrowserActionAdapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.item_browser, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final Item item = mData.get(position);
            holder.ivActionImage.setImageResource(item.icon);
            holder.ivActionImage.setOnClickListener(v -> item.runnable.run());
            holder.tvActionName.setText(item.text);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvActionName;
        private final ImageView ivActionImage;

        public ViewHolder(View itemView) {
            super(itemView);
            tvActionName = itemView.findViewById(R.id.tvActionName);
            ivActionImage = itemView.findViewById(R.id.ivActionImage);
        }
    }

    class Item {
        @StringRes
        int text;
        @DrawableRes
        int icon;
        Runnable runnable;

        public Item(int icon, int text, Runnable runnable) {
            this.icon = icon;
            this.text = text;
            this.runnable = runnable;
        }
    }
}
