package com.redchamber.message.adapter;

import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.bar.adapter.BarRadioAdapter;
import com.redchamber.bean.PhotoBean;
import com.redchamber.bean.SysteMessageBean;
import com.redchamber.util.RedAvatarUtils;
import com.sk.weichat.R;
import com.sk.weichat.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 报名
 */
public class CheckMeApplyAdapter extends BaseQuickAdapter<SysteMessageBean, CheckMeApplyAdapter.PackageViewHolder> {



    private onPackageClickListener mOnPackageClickListener;

    private onCheckMeClickListener mOnCheckMeClickListener;
    private char typeSex;

    public interface onCheckMeClickListener {

        void OnCheckMeClickListener(SysteMessageBean systeMessageBean, int type);

    }

    public void setOnPackageClickListener(onCheckMeClickListener monCheckMeClickListener) {
        this.mOnCheckMeClickListener = monCheckMeClickListener;
    }


    public interface onPackageClickListener {

        void onPackageItemClick(SysteMessageBean systeMessageBean);

    }

    public void setOnPackageClickListener(onPackageClickListener mOnPackageClickListener) {
        this.mOnPackageClickListener = mOnPackageClickListener;
    }


    public CheckMeApplyAdapter(@Nullable List<SysteMessageBean> data, char sex) {
        super(R.layout.red_item_check_message_center, data);
        typeSex = sex;
    }

    @Override
    protected void convert(@NonNull PackageViewHolder helper, SysteMessageBean item) {
        // helper.ivMeeageType.setText(String.valueOf("红豆"));  显示图片类型
        //1  通过   0 是提交申请
        if (typeSex == '0') {
            Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext, String.valueOf(item.getApplyUserId()))).into(helper.ivMessageType);
        } else {
            Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext, String.valueOf(item.getUserId()))).into(helper.ivMessageType);
        }
        if (item.getFlag() == 1) {
            helper.tvMesageType.setText(Html.fromHtml("<font color=\"#FB719A\">" + item.getNickname() + " " + "</font>" + "已通过你的查看申请"));  //
            helper.tvMessageCotent.setText(TimeUtils.s_long_2_str(item.getApplyTime()));
            helper.rlNowPhoto.setVisibility(View.GONE);
            helper.rlNow.setVisibility(View.VISIBLE);
        } else if (item.getFlag() == 0) {
            helper.rlNowPhoto.setVisibility(View.VISIBLE);
            helper.rlNow.setVisibility(View.GONE);
            helper.tvMesageName.setText(item.getNickname());
            helper.tvMessageTime.setText(TimeUtils.s_long_2_str(item.getApplyTime()));
            List<PhotoBean> data=new ArrayList<>();
            if (item.getImageUrl() != null && item.getImageUrl().length() > 0) {
                List<String> likeMeBeanList = new ArrayList<String>();
                PhotoBean photoBean = new PhotoBean();
                photoBean.photoUrl = item.getImageUrl();
                data.add(photoBean);
                likeMeBeanList.add(item.getImageUrl());
                BarRadioAdapter.FriendPhotoAdapter mPhotoAdapter = new BarRadioAdapter.FriendPhotoAdapter(mContext, data);
                helper.rclPhot.setLayoutManager(new GridLayoutManager(mContext, 4));
                helper.rclPhot.setAdapter(mPhotoAdapter);
            }


        } else {
            helper.tvMesageType.setText(Html.fromHtml("<font color=\"#FB719A\">" + item.getNickname() + " " + "</font>" + "已拒绝你的查看申请"));  //
            helper.tvMessageCotent.setText(TimeUtils.s_long_2_str(item.getApplyTime()));
            helper.rlNowPhoto.setVisibility(View.GONE);
            helper.rlNow.setVisibility(View.VISIBLE);
        }

        helper.rlNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnPackageClickListener.onPackageItemClick(item);
            }
        });
        helper.rlNowPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnPackageClickListener.onPackageItemClick(item);
            }
        });
        helper.tvAllow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnCheckMeClickListener.OnCheckMeClickListener(item, 1);
            }
        });
        helper.tvRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnCheckMeClickListener.OnCheckMeClickListener(item, 2);
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
        @BindView(R.id.tv_mesage_name)
        TextView tvMesageName;
        @BindView(R.id.tv_true)
        TextView tvTrue;
        @BindView(R.id.tv_message_time)
        TextView tvMessageTime;

        @BindView(R.id.tv_allow)
        TextView tvAllow;
        @BindView(R.id.tv_meeage_select)
        TextView tvMeeageSelect;
        @BindView(R.id.rl_now_photo)
        RelativeLayout rlNowPhoto;
        @BindView(R.id.rl_now)
        RelativeLayout rlNow;
        @BindView(R.id.tv_refuse)
        TextView tvRefuse;
        @BindView(R.id.rcl_phot)
        RecyclerView rclPhot;

        public PackageViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
