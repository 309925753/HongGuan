package com.redchamber.radio.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.bar.adapter.CommBarPhotoAdapter;
import com.redchamber.bean.PageDataBean;
import com.redchamber.bean.PhotoBean;
import com.redchamber.util.RedAvatarUtils;
import com.sk.weichat.R;
import com.sk.weichat.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 个人中心 节目详情-报名
 */
public class ProgramDetailSignAdapter extends BaseQuickAdapter<PageDataBean.SignUpsBean, ProgramDetailSignAdapter.SignViewHolder> {


    private BtnOnClick btnOnClick;

    public interface BtnOnClick {
        void btnOnClick(PageDataBean.SignUpsBean _signUpsBean);
    }

    public void setBtnOnClice(BtnOnClick btnOnClick) {
        this.btnOnClick = btnOnClick;

    }

    private BtReportnOnClick btnReportOnClick;

    public interface BtReportnOnClick {
        void btnOnClick(PageDataBean.SignUpsBean _signUpsBean);
    }

    public void setBtnOnClice(BtReportnOnClick btnReportOnClick) {
        this.btnReportOnClick = btnReportOnClick;

    }


    public ProgramDetailSignAdapter(@Nullable List<PageDataBean.SignUpsBean> data) {
        super(R.layout.red_item_rv_sign_list, data);
    }

    @Override
    protected void convert(@NonNull SignViewHolder helper, PageDataBean.SignUpsBean item) {
        if (item != null) {

            if (!TextUtils.isEmpty(item.getUserLevel()) && item.getUserLevel().length() == 5) {
                char[] arr = item.getUserLevel().toCharArray();
                helper.tvVip.setVisibility('1' == arr[1] ? View.VISIBLE : View.GONE);
                helper.tvGirl.setVisibility('1' == arr[2] ? View.VISIBLE : View.GONE);
                helper.ivDebutante.setVisibility('1' == arr[4] ? View.VISIBLE : View.GONE);
            }

            Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext, String.valueOf(item.getUserId()))).into(helper.ivAvatar);
            helper.tvNickname.setText(item.getNickName() == null ? "" : item.getNickName());
            helper.tvTime.setText(TimeUtils.getFriendlyTimeDesc(mContext, item.getJoinTime()));
            //Glide.with(mContext).load(item.getJoinImage()).into(helper.ivPhoto);

            List<PhotoBean> data = new ArrayList<>();
            if (item.getJoinImage() != null && item.getJoinImage().length() > 0) {
                List<String> likeMeBeanList = new ArrayList<String>();
                if (item.getJoinImage().contains(";")) {
                    String[] imageData = item.getJoinImage().split(";");
                    for (int i = 0; i < imageData.length; i++) {
                        likeMeBeanList.add(imageData[i]);
                        PhotoBean photoBean = new PhotoBean();
                        photoBean.photoUrl = imageData[i];
                        data.add(photoBean);

                    }
                } else {
                    PhotoBean photoBean = new PhotoBean();
                    photoBean.photoUrl = item.getJoinImage();
                    data.add(photoBean);
                    likeMeBeanList.add(item.getJoinImage());
                }
                CommBarPhotoAdapter mPhotoAdapter = new CommBarPhotoAdapter(mContext, data);
                helper.rclPhot.setLayoutManager(new GridLayoutManager(mContext, 4));
                helper.rclPhot.setAdapter(mPhotoAdapter);

            }
        }

        helper.tvChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOnClick.btnOnClick(item);
            }
        });
        helper.tvReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnReportOnClick.btnOnClick(item);
            }
        });

    }

    public static class SignViewHolder extends BaseViewHolder {
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
        @BindView(R.id.tv_time)
        TextView tvTime;

        @BindView(R.id.tv_report)
        TextView tvReport;
        @BindView(R.id.tv_chat)
        TextView tvChat;
        @BindView(R.id.rcl_phot)
        RecyclerView rclPhot;

        public SignViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
