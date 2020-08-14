package com.sk.weichat.view.chatHolder;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.sk.weichat.R;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.tool.WebViewActivity;

class LinkViewHolder extends AChatHolderInterface {

    TextView tvLinkTitle;
    ImageView ivLinkImage;
    ImageView ivLinkInco;
    TextView tvLinkContent;
    TextView tvLinkAppName;

    String mIntentUrl;
    String mDownloadUrl;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_link : R.layout.chat_to_item_link;
    }

    @Override
    public void initView(View view) {
        tvLinkTitle = view.findViewById(R.id.link_title_tv);
        ivLinkImage = view.findViewById(R.id.link_iv);
        ivLinkInco = view.findViewById(R.id.link_app_icon_iv);
        tvLinkContent = view.findViewById(R.id.link_text_tv);
        tvLinkAppName = view.findViewById(R.id.link_app_name_tv);

        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        JSONObject json = JSONObject.parseObject(message.getObjectId());
        String appName = json.getString("appName");
        String appIcon = json.getString("appIcon");
        String title = json.getString("title");
        String subTitle = json.getString("subTitle");
        mIntentUrl = json.getString("url");
        mDownloadUrl = json.getString("downloadUrl");
        String imageUrl = json.getString("imageUrl");

        tvLinkAppName.setText(appName);
        AvatarHelper.getInstance().displayUrl(appIcon, ivLinkInco);
        tvLinkTitle.setText(title);
        tvLinkContent.setText(subTitle);

        if (TextUtils.isEmpty(appIcon) && TextUtils.isEmpty(imageUrl)) {
            ivLinkImage.setImageResource(R.drawable.browser);
        } else if (TextUtils.isEmpty(imageUrl)) {
            AvatarHelper.getInstance().displayUrl(appIcon, ivLinkImage);
        } else {
            AvatarHelper.getInstance().displayUrl(imageUrl, ivLinkImage);
        }
    }

    @Override
    public void showTime(String time) {

    }

    @Override
    protected void onRootClick(View v) {
        Intent intent = new Intent(mContext, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, mIntentUrl);
        intent.putExtra(WebViewActivity.EXTRA_DOWNLOAD_URL, mDownloadUrl);
        mContext.startActivity(intent);
    }

    @Override
    public boolean enableSendRead() {
        return true;
    }
}
