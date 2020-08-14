package com.sk.weichat.view.chatHolder;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sk.weichat.R;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.map.MapHelper;
import com.sk.weichat.ui.map.MapActivity;
import com.sk.weichat.ui.tool.WebViewActivity;
import com.sk.weichat.util.DisplayUtil;
import com.sk.weichat.view.RoundView;

import org.json.JSONObject;

import static com.sk.weichat.ui.tool.WebViewActivity.EXTRA_URL;

/**
 * 地图、链接 Holder
 */
class LocationViewHolder extends AChatHolderInterface {
    RoundView ivAddress;
    TextView tvAddress;

    double mLatitude;
    double mLongitude;

    String mCurrtUrl;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_location : R.layout.chat_to_item_location;
    }

    @Override
    public void initView(View view) {
        ivAddress = view.findViewById(R.id.chat_address_image);
        tvAddress = view.findViewById(R.id.chat_address_tv);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        ivAddress.setRadius(new float[]{DisplayUtil.dip2px(mContext, 7), DisplayUtil.dip2px(mContext, 7),
                DisplayUtil.dip2px(mContext, 7), DisplayUtil.dip2px(mContext, 7),
                0.0f, 0.0f, 0.0f, 0.0f});

        if (message.getType() == XmppMessage.TYPE_LOCATION) {
            // 加载地图缩略图与位置
            // 直接展示消息里带的地址，也就是发送方地图截图上传的url,
            AvatarHelper.getInstance().displayUrl(message.getContent(), ivAddress);
            tvAddress.setText(message.getObjectId());

            if (!TextUtils.isEmpty(message.getLocation_x()) && !TextUtils.isEmpty(message.getLocation_y())) {
                MapHelper.LatLng latLng = new MapHelper.LatLng(Double.valueOf(message.getLocation_x()), Double.valueOf(message.getLocation_y()));
                mLatitude = latLng.getLatitude();
                mLongitude = latLng.getLongitude();
            }
        } else {
            fillLinkData(message.getContent());
        }
    }

    @Override
    protected void onRootClick(View v) {
        sendReadMessage(mdata);
        ivUnRead.setVisibility(View.GONE);
        if (mdata.getType() == XmppMessage.TYPE_LOCATION) {
            if (mLatitude != 0 && mLongitude != 0) {
                Intent intent = new Intent(mContext, MapActivity.class);
                intent.putExtra("latitude", mLatitude);
                intent.putExtra("longitude", mLongitude);
                intent.putExtra("address", mdata.getObjectId());
                mContext.startActivity(intent);
            } else {
                Toast.makeText(mContext, getString(R.string.tip_location_xy_null), Toast.LENGTH_SHORT).show();
            }
        } else {
            Intent intent = new Intent(mContext, WebViewActivity.class);
            intent.putExtra(EXTRA_URL, mCurrtUrl);
            mContext.startActivity(intent);
        }
    }

    private void fillLinkData(String content) {
        try {
            JSONObject json = new JSONObject(content);
            String tile = json.getString("title");
            mCurrtUrl = json.getString("url");
            String img = json.getString("img");
            tvAddress.setText(getString(R.string.msg_link) + " " + tile);
            AvatarHelper.getInstance().displayUrl(img, ivAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean enableUnRead() {
        return true;
    }
}
