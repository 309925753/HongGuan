package com.sk.weichat.view.chatHolder;

import android.view.View;
import android.widget.RelativeLayout;

import com.sk.weichat.R;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.util.DisplayUtil;
import com.sk.weichat.util.SmileyParser;

import pl.droidsonroids.gif.GifImageView;

class GifViewHolder extends AChatHolderInterface {

    GifImageView mGifView;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_gif : R.layout.chat_to_item_gif;
    }

    @Override
    public void initView(View view) {
        mGifView = view.findViewById(R.id.chat_gif_view);
        mRootView = mGifView;
    }

    @Override
    public void fillData(ChatMessage message) {
        String gifName = message.getContent();
        int resId = SmileyParser.Gifs.textMapId(gifName);
        if (resId != -1) {
            int margin = DisplayUtil.dip2px(mContext, 20);
            RelativeLayout.LayoutParams paramsL = (RelativeLayout.LayoutParams) mGifView.getLayoutParams();
            paramsL.setMargins(margin, 0, margin, 0);
            mGifView.setImageResource(resId);
        } else {
            mGifView.setImageBitmap(null);
        }
    }

    @Override
    protected void onRootClick(View v) {

    }

    @Override
    public boolean enableSendRead() {
        return true;
    }
}
