package com.redchamber.message.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.redchamber.bean.SysteMessageBean;
import com.sk.weichat.R;
import com.sk.weichat.util.TimeUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 系统中心
 */
public class MessageAdapter extends BaseQuickAdapter<SysteMessageBean, MessageAdapter.PackageViewHolder> {


    private onPackageClickListener mOnPackageClickListener;

    public MessageAdapter(@Nullable List<SysteMessageBean> data) {
        super(R.layout.red_item_mg_message_center, data);
    }

    @Override
    protected void convert(@NonNull PackageViewHolder helper, SysteMessageBean item) {


        // type  0:电台  1：红馆 2：点赞 3:评论 4:报名 5:最新查看 6:最新评价
      switch (item.getType()){
            case "0":
                helper.ivMeeageType.setImageResource(R.mipmap.message_radio);
                helper.tvMesageType.setText(String.valueOf("电台"));
                helper.tvMeeageTime.setText(TimeUtils.getFriendlyTimeDesc(mContext, item.getTime()));
                helper.tvLikeTimes.setText(item.getNotReady()==0?"":String.valueOf(item.getNotReady()));   //红点数量
                if(item.getNotReady()==0){
                    helper.tvLikeTimes.setVisibility(View.INVISIBLE);
                }else {
                    helper.tvLikeTimes.setText(String.valueOf(" "+item.getNotReady()));   //红点数量
                }
                helper.tvMessageCotent.setText(item.getMessage()==null ?"":String.valueOf(item.getMessage()));

                break;
            case "1":
                helper.ivMeeageType.setImageResource(R.mipmap.message_redcast);
                helper.tvMesageType.setText(String.valueOf("红馆"));  //
                helper.tvMeeageTime.setText(TimeUtils.getFriendlyTimeDesc(mContext, item.getTime()));
                helper.tvLikeTimes.setText(item.getNotReady()==0?"":String.valueOf(item.getNotReady()));   //红点数量
                if(item.getNotReady()==0){
                    helper.tvLikeTimes.setVisibility(View.INVISIBLE);
                }else {
                    helper.tvLikeTimes.setText(String.valueOf(" "+item.getNotReady()));   //红点数量
                }
                helper.tvMessageCotent.setText(item.getMessage()==null ?"":String.valueOf(item.getMessage()));

                break;
            case "2":
                helper.ivMeeageType.setImageResource(R.mipmap.message_dianzan);
                helper.tvMesageType.setText(String.valueOf("点赞"));
                helper.tvMeeageTime.setText(TimeUtils.getFriendlyTimeDesc(mContext,  item.getTime()));
                helper.tvLikeTimes.setText(item.getNotReady()==0?"":String.valueOf(item.getNotReady()));   //红点数量
                if(item.getNotReady()==0){
                    helper.tvLikeTimes.setVisibility(View.INVISIBLE);
                }else {
                    helper.tvLikeTimes.setText(String.valueOf(" "+item.getNotReady()));   //红点数量
                }
                helper.tvMessageCotent.setText(item.getMessage()==null ?"":String.valueOf(item.getMessage()));

                break;
            case "3":
                helper.ivMeeageType.setImageResource(R.mipmap.message_comments);
                helper.tvMesageType.setText(String.valueOf("评论"));
                helper.tvMeeageTime.setText(TimeUtils.getFriendlyTimeDesc(mContext,  item.getTime()));
                helper.tvLikeTimes.setText(item.getNotReady()==0?"":String.valueOf(item.getNotReady()));   //红点数量
                if(item.getNotReady()==0){
                    helper.tvLikeTimes.setVisibility(View.INVISIBLE);
                }else {
                    helper.tvLikeTimes.setText(String.valueOf(" "+item.getNotReady()));   //红点数量
                }
                helper.tvMessageCotent.setText(item.getMessage()==null ?"":String.valueOf(item.getMessage()));

                break;
            case "4":
                helper.ivMeeageType.setImageResource(R.mipmap.message_apply);
                helper.tvMesageType.setText(String.valueOf("报名"));
                helper.tvMeeageTime.setText(TimeUtils.getFriendlyTimeDesc(mContext,  item.getTime()));
                helper.tvLikeTimes.setText(item.getNotReady()==0?"":String.valueOf(item.getNotReady()));   //红点数量
                if(item.getNotReady()==0){
                    helper.tvLikeTimes.setVisibility(View.INVISIBLE);
                }else {
                    helper.tvLikeTimes.setText(String.valueOf(" "+item.getNotReady()));   //红点数量
                }
                helper.tvMessageCotent.setText(item.getMessage()==null ?"":String.valueOf(item.getMessage()));

                break;

            case "5":
                helper.ivMeeageType.setImageResource(R.mipmap.message_check_apply);
                helper.tvMesageType.setText(String.valueOf("查看申请"));
                helper.tvMeeageTime.setText(TimeUtils.getFriendlyTimeDesc(mContext,  item.getTime()));
                helper.tvLikeTimes.setText(item.getNotReady()==0?"":String.valueOf(item.getNotReady()));   //红点数量
                if(item.getNotReady()==0){
                    helper.tvLikeTimes.setVisibility(View.INVISIBLE);
                }else {
                    helper.tvLikeTimes.setText(String.valueOf(" "+item.getNotReady()));   //红点数量
                }
                helper.tvMessageCotent.setText(item.getMessage()==null ?"":String.valueOf(item.getMessage()));

                break;
            case "6":
                helper.ivMeeageType.setImageResource(R.mipmap.message_comments_ncation);
                helper.tvMesageType.setText(String.valueOf("评价通知"));
                helper.tvMeeageTime.setText(TimeUtils.getFriendlyTimeDesc(mContext, item.getTime()));
                helper.tvLikeTimes.setText(item.getNotReady()==0?"":String.valueOf(item.getNotReady()));   //红点数量
                if(item.getNotReady()==0){
                    helper.tvLikeTimes.setVisibility(View.INVISIBLE);
                }else {
                    helper.tvLikeTimes.setText(String.valueOf(" "+item.getNotReady()));   //红点数量
                }
                helper.tvMessageCotent.setText(item.getMessage()==null ?"":String.valueOf(item.getMessage()));
                break;
          case "7":
              helper.ivMeeageType.setImageResource(R.mipmap.message_wallet_rmot);
              helper.tvMesageType.setText(String.valueOf("钱包提醒"));
              helper.tvMeeageTime.setText(TimeUtils.getFriendlyTimeDesc(mContext,  item.getTime()));
              helper.tvLikeTimes.setText(item.getNotReady()==0?"":String.valueOf(item.getNotReady()));   //红点数量
              if(item.getNotReady()==0){
                  helper.tvLikeTimes.setVisibility(View.INVISIBLE);
              }else {
                  helper.tvLikeTimes.setText(String.valueOf(" "+item.getNotReady()));   //红点数量
              }
              helper.tvMessageCotent.setText(item.getMessage()==null ?"":String.valueOf(item.getMessage()));

              break;

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
        @BindView(R.id.tv_like_times)
        TextView tvLikeTimes;
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

    public interface onPackageClickListener {

        void onPackageItemClick(SysteMessageBean systeMessageBean);

    }

    public void setOnPackageClickListener(onPackageClickListener mOnPackageClickListener) {
        this.mOnPackageClickListener = mOnPackageClickListener;
    }

}
