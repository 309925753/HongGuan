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
 * 评论 
 */
public class MessageCommentsAdapter extends BaseQuickAdapter<SysteMessageBean, MessageCommentsAdapter.PackageViewHolder> {


    private String messageType;
    private char _sex;
    private MessageAdapter.onPackageClickListener mOnPackageClickListener;

    public interface onPackageClickListener {

        void onPackageItemClick(SysteMessageBean systeMessageBean);

    }

    public void setOnPackageClickListener(MessageAdapter.onPackageClickListener mOnPackageClickListener) {
        this.mOnPackageClickListener = mOnPackageClickListener;
    }


    public MessageCommentsAdapter(@Nullable List<SysteMessageBean> data, String _messageType,char sex) {
        super(R.layout.red_item_system_message_center, data);
        messageType = _messageType;
        _sex=sex;
    }

    @Override
    protected void convert(@NonNull PackageViewHolder helper, SysteMessageBean item) {
        // helper.ivMeeageType.setText(String.valueOf("红豆"));  显示图片类型

        if (messageType.equals("1")) {
            helper.ivMeeageType.setImageResource(R.mipmap.message_redcast);
            helper.tvMessageCotent.setText(TimeUtils.getFriendlyTimeDesc(mContext,  item.getMessageTime()));
            if(!TextUtils.isEmpty(item.getMessage())){
                helper.tvMesageType.setText(String.valueOf(item.getMessage()));
            }

        } else if (messageType.equals("3")) {
            Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext, item.getUserId())).into(helper.ivMeeageType);
            helper.tvMesageType.setText(Html.fromHtml("<font color=\"#FB719A\">" + item.getJoinNickName() + " " + "</font>" + "评论了你" + item.getContent()));  //
            helper.tvMessageCotent.setText(TimeUtils.getFriendlyTimeDesc(mContext,  item.getJoinTime()));
            helper.tvMeeageTime.setVisibility(View.VISIBLE);
     /*
            helper.ivMeeageType.setImageResource(R.mipmap.message_comments_ncation);
            helper.tvMessageCotent.setText(TimeUtils.getFriendlyTimeDesc(mContext, (int) item.getJoinTime()));
            helper.tvMesageType.setText(Html.fromHtml("联系过你的男士用户（匿名）对你进行了评价："+"<font color=\"#FB719A\">" + "友好，有趣，爽快，耐心，高冷，暴脾气，" + " " + "</font>"+"" ));
  */
        }else if(messageType.equals("7")){
            helper.ivMeeageType.setImageResource(R.mipmap.message_wallet_rmot);
            helper.tvMessageCotent.setText(TimeUtils.getFriendlyTimeDesc10(mContext,  item.getTime()));
            helper.tvMesageType.setText(String.valueOf(item.getMsg()));
        }else if(messageType.equals("6")){
            helper.ivMeeageType.setImageResource(R.mipmap.message_comments_ncation);
            helper.tvMessageCotent.setText(TimeUtils.getFriendlyTimeDesc(mContext,  item.getCommentsTime()));
            //helper.tvMesageType.setText(String.valueOf(item.getOtherName())+item.getComments());
            if(_sex=='0'){
                helper.tvMesageType.setText(Html.fromHtml("联系过你的男士用户（匿名）对你进行了评价："+"<font color=\"#FB719A\">" + item.getComments() + " " + "</font>"+"\n如果评论不属实，你可申请上诉，我们会进行审核。" ));
            }else {
                helper.tvMesageType.setText(Html.fromHtml("联系过你的女士用户（匿名）对你进行了评价："+"<font color=\"#FB719A\">" + item.getComments() + " " + "</font>"+"\n如果评论不属实，你可申请上诉，我们会进行审核。" ));
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
        @BindView(R.id.iv_meeage_type)
        ImageView ivMeeageType;
        @BindView(R.id.tv_mesage_type)
        TextView tvMesageType;
        @BindView(R.id.tv_message_cotent)
        TextView tvMessageCotent;
        @BindView(R.id.rl_root)
        LinearLayout rlRoot;
        @BindView(R.id.tv_meeage_time)
        TextView tvMeeageTime;

        public PackageViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
