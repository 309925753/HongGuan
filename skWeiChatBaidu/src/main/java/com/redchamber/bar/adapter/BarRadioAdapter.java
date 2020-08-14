package com.redchamber.bar.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.bean.PageDataBean;
import com.redchamber.bean.PhotoBean;
import com.redchamber.friend.FriendHomePageActivity;
import com.redchamber.friend.adapter.FriendPhotoAdapter;
import com.redchamber.photo.BarPhotoActivity;
import com.redchamber.photo.FriendAlbumActivity;
import com.redchamber.photo.PreviewFriendPhotosActivity;
import com.redchamber.photo.PreviewPhotosActivity;
import com.redchamber.util.GlideUtils;
import com.redchamber.util.RedAvatarUtils;
import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.view.cjt2325.cameralibrary.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 约吧广播、动态 公共页面
 */
public class BarRadioAdapter extends BaseMultiItemQuickAdapter<PageDataBean, BaseViewHolder> {



    private BtnOnClick btnOnClick;

    public BarRadioAdapter(List<PageDataBean> data) {
        super(data);
    }

    public interface BtnOnClick {
        void btnOnClick(PageDataBean pageDataBean, int fuctionType, int Position);
    }

    public void setBtnOnClice(BtnOnClick btnOnClick) {
        this.btnOnClick = btnOnClick;

    }

    private BtnOnCommClick btnOnCommClick;

    public interface BtnOnCommClick {
        void btnOnCommClick(PageDataBean pageDataBean, int fuctionType, int Position, View view);
    }

    public void setBtnOnClice(BtnOnCommClick btnOnCommClick) {
        this.btnOnCommClick = btnOnCommClick;

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
                viewHolderA.tvVip.setText(item.get);
                tvReleaseTime
                */
                viewHolderA.mIvAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FriendHomePageActivity.startFriendHomePageActivity(mContext, String.valueOf(item.getUserId()));
                    }
                });

                //用户级别 第一位为性别、第二位为是否VIP认证、第三位为是否女神男神认证、第四位为是否真人认证、第五位为是否有徽章
                if (!TextUtils.isEmpty(item.getUserLevel()) && item.getUserLevel().length() == 5) {
                    char[] arr = item.getUserLevel().toCharArray();
                    viewHolderA.tvVip.setVisibility('1' == arr[1] ? View.VISIBLE : View.GONE);
                    viewHolderA.tvGirl.setVisibility('1' == arr[2] ? View.VISIBLE : View.GONE);
                     viewHolderA.ivDebutante.setVisibility('1' == arr[4]?View.VISIBLE:View.GONE);
                }

                viewHolderA.tvReleaseTime.setText(TimeUtils.getFriendlyTimeDesc(mContext,  item.getPubTime()));
                LogUtil.e("item.getPubTime() = "+item.getPubTime());
                viewHolderA.tvRadioTheme.setText(item.getTitle() == null ? "" : item.getTitle());
                viewHolderA.tvRadioTime.setText(TimeUtils.s_long_2_str(item.getProgramDate()) + "" + item.getProgramTime());//programDate programTime
                viewHolderA.tvRadioAddress.setText(item.getPlaceName() == null ? "" : item.getPlaceName());
                viewHolderA.tvRadioExpect.setText(item.getExpectFriend() == null ? "" : item.getExpectFriend());
                viewHolderA.tvRadioDianzan.setText(item.getLikeNum() == 0 ? "点赞" : String.valueOf(item.getLikeNum()));
                if (item.getDiscussFlag() == 1) {
                    viewHolderA.tvRadioComm.setText(item.getDiscussNum() == 0 ? "评论" : "评论(" + String.valueOf(item.getDiscussNum()) + ")");
                } else if (item.getDiscussFlag() == 0) {
                    viewHolderA.tvRadioComm.setText("禁止评论");
                }
                viewHolderA.tvRadioApply.setText(item.getSignUpNums() == 0 ? "我要报名(0)" : "我要报名(" + String.valueOf(item.getSignUpNums()) + ")");

                if(item.getProgramFlag()==0){
                    viewHolderA.tvRadioApply.setText("报名结束");
                }
                List<PhotoBean> data=new ArrayList<>();
                if (item.getImages() != null && item.getImages().length() > 0) {
                    List<String> likeMeBeanList = new ArrayList<String>();
                    if (item.getImages().contains(";")) {
                        String[] imageData = item.getImages().split(";");
                        for (int i = 0; i < imageData.length; i++) {
                            likeMeBeanList.add(imageData[i]);
                            PhotoBean photoBean=new PhotoBean();
                            photoBean.photoUrl=imageData[i];
                            data.add(photoBean);

                        }
                    } else {
                        PhotoBean photoBean=new PhotoBean();
                        photoBean.photoUrl=item.getImages();
                        data.add(photoBean);
                        likeMeBeanList.add(item.getImages());
                    }

                    FriendPhotoAdapter    mPhotoAdapter = new FriendPhotoAdapter(mContext, data);
                    viewHolderA.rclPhot.setLayoutManager(new GridLayoutManager(mContext, 4));
                    viewHolderA.rclPhot.setAdapter(mPhotoAdapter);

                }


                viewHolderA.llDianzan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // ProgramDetailActivity.startActivity(mContext);
                        btnOnClick.btnOnClick(item, 1, helper.getPosition());

                    }
                });
                viewHolderA.ivMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        btnOnCommClick.btnOnCommClick(item, 1, helper.getPosition(), view);

                    }
                });

                viewHolderA.llComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // ProgramDetailActivity.startActivity(mContext);
                        btnOnClick.btnOnClick(item, 2, helper.getPosition());
                    }
                });

                viewHolderA.llSign.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // ProgramDetailActivity.startActivity(mContext);
                        btnOnClick.btnOnClick(item, 3, helper.getPosition());
                        LogUtil.e(" helper.getPosition();  " + helper.getPosition());
                    }
                });
                break;
            case PageDataBean.TYPE_B:
                RadioBViewHolder viewHolderB = (RadioBViewHolder) helper;
                viewHolderB.mTvNickName.setText(item.getNickName() == null ? "" : item.getNickName());

                viewHolderB.mIvAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FriendHomePageActivity.startFriendHomePageActivity(mContext, String.valueOf(item.getUserId()));
                    }
                });
                if (!TextUtils.isEmpty(item.getUserLevel()) && item.getUserLevel().length() == 5) {
                    char[] arr = item.getUserLevel().toCharArray();
                    viewHolderB.tvVip.setVisibility('1' == arr[1] ? View.VISIBLE : View.GONE);
                    viewHolderB.tvGirl.setVisibility('1' == arr[2] ? View.VISIBLE : View.GONE);
                    viewHolderB.ivDebutante.setVisibility('1' == arr[4]?View.VISIBLE:View.GONE);
                }

                Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext, String.valueOf(item.getUserId()))).into(viewHolderB.mIvAvatar);
                //   viewHolderB.tvReleaseTime.setText(TimeUtils.getFriendlyTimeDesc(mContext, (int) item.getPubTime()));
                viewHolderB.tvRadioTheme.setText(item.getContent()==null?"":item.getContent());
                viewHolderB.tvRadioDianzan.setText(item.getLikeNum() == 0 ? "点赞" : String.valueOf(item.getLikeNum()));
                if (item.getDiscussFlag() == 1) {
                    viewHolderB.tvRadioComm.setText(item.getDiscussNum() == 0 ? "评论" : "评论(" + String.valueOf(item.getDiscussNum()) + ")");
                } else if (item.getDiscussFlag() == 0) {
                    viewHolderB.tvRadioComm.setText("禁止评论");
                }



               // viewHolderB.tvRadioComm.setText(item.getDiscussNum() == 0 ? "" : String.valueOf(item.getDiscussNum()));
                viewHolderB.tvReleaseTime.setText(TimeUtils.getFriendlyTimeDesc(mContext,  item.getPubTime()));
                LogUtil.e("item.getPubTime() = "+item.getPubTime());
                  List<PhotoBean> dataA=new ArrayList<>();
                if (item.getImages() != null && item.getImages().length() > 0) {
                    if (item.getImages().contains(";")) {
                        String[] imageData = item.getImages().split(";");
                        for (int i = 0; i < imageData.length; i++) {
                            PhotoBean photoBean=new PhotoBean();
                            photoBean.photoUrl=imageData[i];
                            dataA.add(photoBean);

                        }
                    } else {
                        PhotoBean photoBean=new PhotoBean();
                        photoBean.photoUrl=item.getImages();
                        dataA.add(photoBean);
                    }

                  /*  CheckLikesMeAdapter checkLikesMeAdapter = new CheckLikesMeAdapter(mContext);
                    viewHolderB.rclPhot.setLayoutManager(new GridLayoutManager(mContext, 4));
                    checkLikesMeAdapter.likeMeBeanList = likeMeBeanList;
                    viewHolderB.rclPhot.setAdapter(checkLikesMeAdapter);
                    checkLikesMeAdapter.notifyDataSetChanged();*/

                    FriendPhotoAdapter    mPhotoAdapter = new FriendPhotoAdapter(mContext, dataA);
                    viewHolderB.rclPhot.setLayoutManager(new GridLayoutManager(mContext, 4));
                    viewHolderB.rclPhot.setAdapter(mPhotoAdapter);

                }

                viewHolderB.llDianzan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // ProgramDetailActivity.startActivity(mContext);
                        btnOnClick.btnOnClick(item, 1, helper.getPosition());
                        helper.getPosition();
                        LogUtil.e(" helper.getPosition();  " + helper.getPosition());
                    }
                });

                viewHolderB.llComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // ProgramDetailActivity.startActivity(mContext);
                        btnOnClick.btnOnClick(item, 2, helper.getPosition());
                        LogUtil.e(" helper.getPosition();  " + helper.getPosition());
                    }
                });
                viewHolderB.ivMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        btnOnCommClick.btnOnCommClick(item, 1, helper.getPosition(), view);

                    }
                });

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

        @BindView(R.id.ll_dianzan)
        LinearLayout llDianzan;
        @BindView(R.id.ll_comment)
        LinearLayout llComment;
        @BindView(R.id.iv_more)
        ImageView ivMore;
        @BindView(R.id.iv_debutante)
        ImageView ivDebutante;

        @BindView(R.id.tv_radio_theme)
        TextView tvRadioTheme;


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
                iv_avatar = (ImageView) view.findViewById(R.id.iv_avatar);
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
                holder.iv_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    //    PreviewPhotosActivity.startActivity(context, getData(), holder.getLayoutPosition());
                    }
                });


            }

        }

        @Override
        public int getItemCount() {
            return likeMeBeanList.size();
        }
    }

    public static class FriendPhotoAdapter extends BaseQuickAdapter<PhotoBean, FriendPhotoAdapter.PhotoViewHolder> {

        private Context mContext;
        private int mWidth;
        private int mPhotoNum = getItemCount();

        public FriendPhotoAdapter(Context context,  List<PhotoBean> data) {
            super(R.layout.red_item_rv_friend_photo, data);
            this.mContext = context;
            this.mWidth = (ScreenUtil.getScreenWidth(mContext) - ScreenUtil.dip2px(mContext, 40)) / 4;
        }

        @Override
        public int getItemCount() {
            if (getData().size() > 8) {
                return 8;
            }
            return super.getItemCount();
        }

        @Override
        protected void convert(@NonNull PhotoViewHolder helper, PhotoBean item) {
            ViewGroup.LayoutParams lp = helper.mRlRoot.getLayoutParams();
            lp.width = lp.height = mWidth;

            if (7 == helper.getAdapterPosition() && mPhotoNum > 8) {
                helper.mTvRest.setVisibility(View.VISIBLE);
                helper.mTvRest.setText(String.format("+%d", mPhotoNum - 8));
            } else {
                helper.mTvRest.setVisibility(View.GONE);
            }
                Glide.with(mContext).load(item.photoUrl).into(helper.mIvPhoto);
                helper.mTvStatus.setVisibility(View.GONE);

            helper.mIvPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BarPhotoActivity.startActivity(mContext, getData(), helper.getLayoutPosition());
                }
            });

        }

        public static class PhotoViewHolder extends BaseViewHolder {

            @BindView(R.id.rl_root)
            RelativeLayout mRlRoot;
            @BindView(R.id.iv_photo)
            RoundedImageView mIvPhoto;
            @BindView(R.id.tv_status)
            TextView mTvStatus;
            @BindView(R.id.tv_rest)
            TextView mTvRest;

            public PhotoViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

        }

        public void setPhotoNum(int photoNum) {
            this.mPhotoNum = photoNum;
        }

    }
}
