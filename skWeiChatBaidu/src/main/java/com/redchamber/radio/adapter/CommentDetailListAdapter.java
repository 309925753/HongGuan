package com.redchamber.radio.adapter;

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
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.bean.DiscussesBean;
import com.redchamber.util.RedAvatarUtils;
import com.sk.weichat.R;
import com.sk.weichat.util.TimeUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 个人中心评论列表
 */
public class CommentDetailListAdapter extends BaseQuickAdapter<DiscussesBean, CommentDetailListAdapter.CommentViewHolder> {




    public CommentDetailListAdapter(@Nullable List<DiscussesBean> data) {
        super(R.layout.red_item_rv_comment_detail_list, data);
    }

    @Override
    protected void convert(@NonNull CommentViewHolder helper, DiscussesBean item) {
        helper.tvNickname.setText(item.getNickName()==null?"":item.getNickName());

        if (!TextUtils.isEmpty(item.getUserLevel()) && item.getUserLevel().length() == 5) {
            char[] arr = item.getUserLevel().toCharArray();
            helper.tvVip.setVisibility('1' == arr[1]?View.VISIBLE:View.GONE);
            helper.tvGirl.setVisibility('1' == arr[2]?View.VISIBLE:View.GONE);
            helper.ivDebutante.setVisibility('1' == arr[4]?View.VISIBLE:View.GONE);
        }

        Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext,String.valueOf(item.getUserId()))).into(helper.ivAvatar);
        helper.tvTimes.setText(TimeUtils.getFriendlyTimeDesc(mContext, item.getJoinTime()));
        helper.tvContent.setText(item.getContent()==null?"":item.getContent());

    }

    public static class CommentViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_times)
        TextView tvTimes;
        @BindView(R.id.iv_avatar)
        RoundedImageView ivAvatar;
        @BindView(R.id.tv_nickname)
        TextView tvNickname;
        @BindView(R.id.tv_girl)
        TextView tvGirl;
        @BindView(R.id.tv_vip)
        TextView tvVip;
        @BindView(R.id.iv_debutante)
        ImageView ivDebutante;
        @BindView(R.id.ll_user)
        LinearLayout llUser;
        @BindView(R.id.tv_content)
        TextView tvContent;


        public CommentViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
