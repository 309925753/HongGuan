package com.redchamber.message.adapter;

import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.redchamber.bean.SysteMessageBean;
import com.redchamber.util.RedAvatarUtils;
import com.sk.weichat.R;
import com.sk.weichat.util.TimeUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 报名
 */
public class MessageApplyAdapter extends BaseQuickAdapter<SysteMessageBean, MessageApplyAdapter.PackageViewHolder> {

    private MessageAdapter.onPackageClickListener mOnPackageClickListener;
    public interface onPackageClickListener {

        void onPackageItemClick(SysteMessageBean systeMessageBean);

    }

    public void setOnPackageClickListener(MessageAdapter.onPackageClickListener mOnPackageClickListener) {
        this.mOnPackageClickListener = mOnPackageClickListener;
    }


    public MessageApplyAdapter(@Nullable List<SysteMessageBean> data) {
        super(R.layout.red_item_enroll_message_center, data);
    }

    @Override
    protected void convert(@NonNull PackageViewHolder helper, SysteMessageBean item) {
        // helper.ivMeeageType.setText(String.valueOf("红豆"));  显示图片类型
         if(item.equals("6")){
             Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext,String.valueOf(item.getOauthUserId()))).into(helper.ivMessageType);
            helper.tvMessageCotent.setText(TimeUtils.getFriendlyTimeDesc(mContext,  item.getCommentsTime()));
             helper.tvMesageType.setText(Html.fromHtml("<font color=\"#FB719A\">" + item.getOtherName() + " " + "</font>" + "" + item.getComments()));  //
         }else {
             Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext, String.valueOf(item.getOauthUserId()))).into(helper.ivMessageType);
             helper.tvMesageType.setText(Html.fromHtml("<font color=\"#FB719A\">" + item.getJoinNickName() + " " + "</font>" + "" + item.getContent()));  //
             helper.tvMessageCotent.setText(TimeUtils.getFriendlyTimeDesc(mContext,  item.getJoinTime()));
             if (!TextUtils.isEmpty(item.getMessage())) {
                 helper.tvMesageType.setText(Html.fromHtml("<font color=\"#FB719A\">" + item.getNickName() + " " + "</font>" + "" + item.getMessage()));  //
                 helper.tvMessageCotent.setText(TimeUtils.getFriendlyTimeDesc(mContext,  item.getMessageTime()));
             }
         }


        helper.rlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnPackageClickListener.onPackageItemClick(item);
            }
        });
    }

    public static class PackageViewHolder extends BaseViewHolder {
        @BindView(R.id.iv_message_type)
        ImageView ivMessageType;
        @BindView(R.id.tv_mesage_type)
        TextView tvMesageType;
        @BindView(R.id.tv_message_cotent)
        TextView tvMessageCotent;
        @BindView(R.id.tv_meeage_time)
        TextView tvMeeageTime;
        @BindView(R.id.rl_root)
        LinearLayout rlRoot;
        public PackageViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
