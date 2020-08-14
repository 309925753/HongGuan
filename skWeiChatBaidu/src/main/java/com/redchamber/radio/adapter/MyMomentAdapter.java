package com.redchamber.radio.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.bar.adapter.CommBarPhotoAdapter;
import com.redchamber.bean.PageDataBean;
import com.redchamber.bean.PhotoBean;
import com.redchamber.radio.MomentDetailActivity;
import com.redchamber.util.RedAvatarUtils;
import com.sk.weichat.R;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * red_item_rv_my_moment
 * 个人中心 我的动态
 */
public class MyMomentAdapter extends BaseMultiItemQuickAdapter<PageDataBean, BaseViewHolder> {



    private BtnOnCommClick btnOnCommClick;

    public interface BtnOnCommClick {
        void btnOnCommClick(PageDataBean pageDataBean, int fuctionType, int Position, View view);
    }

    public void setBtnOnClice(BtnOnCommClick btnOnCommClick) {
        this.btnOnCommClick = btnOnCommClick;

    }


    public MyMomentAdapter(List<PageDataBean> data) {
        super(data);
    }

    private BtnOnClick btnOnClick;

    public interface BtnOnClick {
        void btnOnClick(PageDataBean pageDataBean, int fuctionType, int Position);
    }

    public void setBtnOnClice(BtnOnClick btnOnClick) {
        this.btnOnClick = btnOnClick;

    }

    @Override
    protected BaseViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder viewHolder = null;
        switch (viewType) {
            case PageDataBean.TYPE_A:
                viewHolder = new RadioAViewHolder(LayoutInflater.from(mContext).inflate(R.layout.red_item_rv_bar_radio_type_a, parent, false));
                break;
            case PageDataBean.TYPE_B:
                viewHolder = new RadioBViewHolder(LayoutInflater.from(mContext).inflate(R.layout.red_item_rv_bar_radio_type_b, parent, false));
                break;
        }
        return viewHolder;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, PageDataBean item) {
        switch (helper.getItemViewType()) {
            case PageDataBean.TYPE_A:
                RadioAViewHolder viewHolderA = (RadioAViewHolder) helper;
                viewHolderA.mTvNickName.setText(item.getNickName());
                Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext, String.valueOf(item.getUserId()))).into(viewHolderA.mIvAvatar);
                /*viewHolderA.tvGirl.setText(item.getSex()==0?"男神":"女神");
                viewHolderA.tvVip.setText(item.get);*/

                if (!TextUtils.isEmpty(item.getUserLevel()) && item.getUserLevel().length() == 5) {
                    char[] arr = item.getUserLevel().toCharArray();
                    viewHolderA.tvVip.setVisibility(CoreManager.getInstance(mContext).getConfig().enablePayModule && '1' == arr[1] ? View.VISIBLE : View.GONE);
                    viewHolderA.tvGirl.setVisibility('1' == arr[2] ? View.VISIBLE : View.GONE);
                    viewHolderA.ivDebutante.setVisibility('1' == arr[4] ? View.VISIBLE : View.GONE);
                }

                viewHolderA.tvReleaseTime.setText(TimeUtils.getFriendlyTimeDesc(mContext, item.getPubTime()));
                viewHolderA.tvRadioTheme.setText(item.getTitle() == null ? "" : item.getTitle());
                viewHolderA.tvRadioTime.setText(TimeUtils.s_long_2_str(item.getProgramDate()) + "" + item.getProgramTime());//programDate programTime
                viewHolderA.tvRadioAddress.setText(item.getPlaceName() == null ? "" : item.getPlaceName());
                viewHolderA.tvRadioExpect.setText(item.getExpectFriend() == null ? "" : item.getExpectFriend());
                viewHolderA.tvRadioDianzan.setText(item.getLikeNum() == 0 ? "点赞" : "点赞(" + item.getLikeNum() + ")");
                viewHolderA.tvRadioComm.setText(item.getDiscussNum() == 0 ? "评论" : "评论(" + item.getDiscussNum() + ")");
                viewHolderA.tvRadioApply.setText(item.getSignUpNums() == 0 ? "我要报名(0)" : "我要报名(" + item.getSignUpNums() + ")");
               /* //动态显示图片

                }*/


                viewHolderA.mLlRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MomentDetailActivity.startActivity(mContext, item);

                    }
                });
                viewHolderA.ivMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        btnOnCommClick.btnOnCommClick(item, 1, helper.getPosition(), view);

                    }
                });
                break;
            case PageDataBean.TYPE_B:
                RadioBViewHolder viewHolderB = (RadioBViewHolder) helper;
                viewHolderB.mTvNickName.setText(item.getNickName());
                if (!TextUtils.isEmpty(item.getUserLevel()) && item.getUserLevel().length() == 5) {
                    char[] arr = item.getUserLevel().toCharArray();
                    viewHolderB.tvVip.setVisibility(CoreManager.getInstance(mContext).getConfig().enablePayModule && '1' == arr[1] ? View.VISIBLE : View.GONE);
                    viewHolderB.tvGirl.setVisibility('1' == arr[2] ? View.VISIBLE : View.GONE);
                    viewHolderB.ivDebutante.setVisibility('1' == arr[4] ? View.VISIBLE : View.GONE);
                }

                Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext, String.valueOf(item.getUserId()))).into(viewHolderB.mIvAvatar);
                // viewHolderB.tvReleaseTime.setText(TimeUtils.getFriendlyTimeDesc(mContext, (int) item.getProgramDate()));
                viewHolderB.tvReleaseTime.setText(TimeUtils.getFriendlyTimeDesc(mContext, item.getPubTime()));
                viewHolderB.tvRadioDianzan.setText(item.getLikeNum() == 0 ? "点赞" : "点赞(" + item.getLikeNum() + ")");
                viewHolderB.tvRadioComm.setText(item.getDiscussNum() == 0 ? "评论" : "评论(" + item.getDiscussNum() + ")");
                //programDate
                viewHolderB.tvRadioTheme.setText(item.getContent());

                List<PhotoBean> data = new ArrayList<>();
                if (item.getImages() != null && item.getImages().length() > 0) {
                    List<String> likeMeBeanList = new ArrayList<String>();
                    if (item.getImages().contains(";")) {
                        String[] imageData = item.getImages().split(";");
                        for (int i = 0; i < imageData.length; i++) {
                            likeMeBeanList.add(imageData[i]);
                            PhotoBean photoBean = new PhotoBean();
                            photoBean.photoUrl = imageData[i];
                            data.add(photoBean);

                        }
                    } else {
                        PhotoBean photoBean = new PhotoBean();
                        photoBean.photoUrl = item.getImages();
                        data.add(photoBean);
                        likeMeBeanList.add(item.getImages());
                    }

                  /*  CheckLikesMeAdapter checkLikesMeAdapter = new CheckLikesMeAdapter(mContext);
                    viewHolderB.rclPhot.setLayoutManager(new GridLayoutManager(mContext, 4));
                    checkLikesMeAdapter.likeMeBeanList = likeMeBeanList;
                    viewHolderB.rclPhot.setAdapter(checkLikesMeAdapter);
                    checkLikesMeAdapter.notifyDataSetChanged();*/
                    CommBarPhotoAdapter mPhotoAdapter = new CommBarPhotoAdapter(mContext, data);
                    viewHolderB.rclPhot.setLayoutManager(new GridLayoutManager(mContext, 4));
                    viewHolderB.rclPhot.setAdapter(mPhotoAdapter);


                    viewHolderB.llDianorcom.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            MomentDetailActivity.startActivity(mContext, item);

                        }
                    });
                    viewHolderB.ivMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            btnOnCommClick.btnOnCommClick(item, 1, helper.getPosition(), view);

                        }
                    });


                }


                break;
        }
    }

    static class RadioAViewHolder extends BaseViewHolder {

        @BindView(R.id.ll_root)
        LinearLayout mLlRoot;
        @BindView(R.id.iv_avatar)
        RoundedImageView mIvAvatar;
        @BindView(R.id.tv_nickname)
        TextView mTvNickName;
        @BindView(R.id.tv_girl)
        TextView tvGirl;
        @BindView(R.id.tv_vip)
        TextView tvVip;
        @BindView(R.id.tv_release_time)
        TextView tvReleaseTime;
        @BindView(R.id.tv_radio_theme)
        TextView tvRadioTheme;
        @BindView(R.id.tv_radio_time)
        TextView tvRadioTime;
        @BindView(R.id.tv_radio_address)
        TextView tvRadioAddress;
        @BindView(R.id.tv_radio_expect)
        TextView tvRadioExpect;
        @BindView(R.id.tv_radio_dianzan)
        TextView tvRadioDianzan;
        @BindView(R.id.tv_radio_comm)
        TextView tvRadioComm;
        @BindView(R.id.tv_radio_apply)
        TextView tvRadioApply;
        @BindView(R.id.rcl_phot)
        RecyclerView rclPhot;
        @BindView(R.id.ll_dianzan)
        LinearLayout llDianzan;
        @BindView(R.id.ll_comment)
        LinearLayout llComment;
        @BindView(R.id.ll_sign)
        LinearLayout llSign;
        @BindView(R.id.iv_more)
        ImageView ivMore;
        @BindView(R.id.iv_debutante)
        ImageView ivDebutante;

        RadioAViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    static class RadioBViewHolder extends BaseViewHolder {

        @BindView(R.id.ll_root)
        LinearLayout mLlRoot;
        @BindView(R.id.iv_avatar)
        RoundedImageView mIvAvatar;
        @BindView(R.id.tv_nickname)
        TextView mTvNickName;
        @BindView(R.id.tv_girl)
        TextView tvGirl;
        @BindView(R.id.tv_vip)
        TextView tvVip;
        @BindView(R.id.tv_release_time)
        TextView tvReleaseTime;
        @BindView(R.id.tv_radio_dianzan)
        TextView tvRadioDianzan;
        @BindView(R.id.tv_radio_comm)
        TextView tvRadioComm;
        @BindView(R.id.rcl_phot)
        RecyclerView rclPhot;
        @BindView(R.id.tv_radio_theme)
        TextView tvRadioTheme;
        @BindView(R.id.ll_dianzan)
        LinearLayout llDianzan;
        @BindView(R.id.ll_comment)
        LinearLayout llComment;
        @BindView(R.id.iv_more)
        ImageView ivMore;
        @BindView(R.id.iv_debutante)
        ImageView ivDebutante;
        @BindView(R.id.ll_dianorcom)
        LinearLayout llDianorcom;


        RadioBViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public static class CheckLikesMeAdapter extends RecyclerView.Adapter<CheckLikesMeAdapter.ViewHolder> {
        private LayoutInflater mInflater;
        private Context context;
        private List<String> likeMeBeanList = new ArrayList<String>();

        public CheckLikesMeAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            this.context = context;
        }


        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView iv_avatar;

            public ViewHolder(View view) {
                super(view);
                iv_avatar = view.findViewById(R.id.iv_avatar);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_barradio_adapter, null);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (likeMeBeanList != null && likeMeBeanList.size() > 0) {
                final String likeMeBean = likeMeBeanList.get(position);
                Glide.with(context).load(String.valueOf(likeMeBean)).into(holder.iv_avatar);
            }

        }

        @Override
        public int getItemCount() {
            return likeMeBeanList.size();
        }
    }

}
