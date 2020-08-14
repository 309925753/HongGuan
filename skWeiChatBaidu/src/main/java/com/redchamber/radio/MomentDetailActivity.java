package com.redchamber.radio;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.api.GlobalConstants;
import com.redchamber.bar.adapter.CommBarPhotoAdapter;
import com.redchamber.bean.PageDataBean;
import com.redchamber.bean.PhotoBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.radio.adapter.MomentDetailCommentAdapter;
import com.redchamber.radio.adapter.MomentDetailThumbAdapter;
import com.redchamber.util.RedAvatarUtils;
import com.redchamber.view.ReportPopupWindow;
import com.sk.weichat.R;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 个人中心 动态详情
 */
public class MomentDetailActivity extends BaseActivity {

    @BindView(R.id.rv_thumb_list)
    RecyclerView mRvThumbList;
    @BindView(R.id.rv_comment_list)
    RecyclerView mRvCommentList;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.rl_title)
    RelativeLayout rlTitle;
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
    @BindView(R.id.iv_more)
    ImageView ivMore;
    @BindView(R.id.tv_release_time)
    TextView tvReleaseTime;
    @BindView(R.id.tv_radio_theme)
    TextView tvRadioTheme;
    @BindView(R.id.rv_photo)
    RecyclerView rvPhoto;
    @BindView(R.id.tv_my_dianzan)
    TextView tvMyDianzan;
    @BindView(R.id.tv_my_comm)
    TextView tvMyComm;
    @BindView(R.id.tv_show_more)
    TextView tvShowMore;

    private MomentDetailThumbAdapter mThumbAdapter;
    private MomentDetailCommentAdapter mCommentAdapter;
    private PageDataBean pageDataBean = new PageDataBean();

    @Override
    protected int setLayout() {
        return R.layout.red_activity_moment_detail;
    }

    @Override
    protected void initView() {

        if (getIntent() != null) {
            pageDataBean = (PageDataBean) getIntent().getSerializableExtra(GlobalConstants.KEY_THEME);
        }

        mThumbAdapter = new MomentDetailThumbAdapter(this, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvThumbList.setLayoutManager(layoutManager);
        mRvThumbList.setAdapter(mThumbAdapter);

        mCommentAdapter = new MomentDetailCommentAdapter(null);
        mRvCommentList.setLayoutManager(new LinearLayoutManager(this));
        mRvCommentList.setAdapter(mCommentAdapter);

        mThumbAdapter.setBtnOnClice(new MomentDetailThumbAdapter.BtnOnClick() {
            @Override
            public void btnOnClick() {
                ThumbDetailListActivity.startActivity(mContext,pageDataBean);
            }
        });
        if(pageDataBean!=null){
            initData();
        }
    }

    private void initData() {
        tvNickname.setText(pageDataBean.getNickName());
        Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext,String.valueOf(pageDataBean.getUserId()))).into(ivAvatar);
        tvReleaseTime.setText(TimeUtils.getFriendlyTimeDesc(mContext,  pageDataBean.getPubTime()));
        tvRadioTheme.setText(pageDataBean.getContent());
        tvMyDianzan.setText(pageDataBean.getLikeNum() == 0 ? "点赞" : "点赞(" + pageDataBean.getLikeNum() + ")");
        tvMyComm.setText(pageDataBean.getDiscussNum() == 0 ? "评论" : "评论(" + pageDataBean.getDiscussNum() + ")");

        if (pageDataBean.getImages() != null && pageDataBean.getImages().length() > 0) {
         /*   List<String> likeMeBeanList = new ArrayList<String>();
            if (pageDataBean.getImages().contains(";")) {
                String[] imageData = pageDataBean.getImages().split(";");
                for (int i = 0; i < imageData.length; i++) {
                    likeMeBeanList.add(imageData[i]);
                }
            } else {
                likeMeBeanList.add(pageDataBean.getImages());
            }
            CheckLikesMeAdapter checkLikesMeAdapter = new CheckLikesMeAdapter(mContext);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            rvPhoto.setLayoutManager(linearLayoutManager);
            checkLikesMeAdapter.likeMeBeanList = likeMeBeanList;
            rvPhoto.setAdapter(checkLikesMeAdapter);
            checkLikesMeAdapter.notifyDataSetChanged();*/
            List<PhotoBean> data = new ArrayList<>();

                List<String> likeMeBeanList = new ArrayList<String>();
                if (pageDataBean.getImages().contains(";")) {
                    String[] imageData = pageDataBean.getImages().split(";");
                    for (int i = 0; i < imageData.length; i++) {
                        likeMeBeanList.add(imageData[i]);
                        PhotoBean photoBean = new PhotoBean();
                        photoBean.photoUrl = imageData[i];
                        data.add(photoBean);

                    }
                } else {
                    PhotoBean photoBean = new PhotoBean();
                    photoBean.photoUrl = pageDataBean.getImages();
                    data.add(photoBean);
                    likeMeBeanList.add(pageDataBean.getImages());
                }

                  /*  CheckLikesMeAdapter checkLikesMeAdapter = new CheckLikesMeAdapter(mContext);
                    viewHolderB.rclPhot.setLayoutManager(new GridLayoutManager(mContext, 4));
                    checkLikesMeAdapter.likeMeBeanList = likeMeBeanList;
                    viewHolderB.rclPhot.setAdapter(checkLikesMeAdapter);
                    checkLikesMeAdapter.notifyDataSetChanged();*/
                CommBarPhotoAdapter mPhotoAdapter = new CommBarPhotoAdapter(mContext, data);
                rvPhoto.setLayoutManager(new GridLayoutManager(mContext, 4));
                rvPhoto.setAdapter(mPhotoAdapter);

        }

        if (!TextUtils.isEmpty(pageDataBean.getUserLevel()) && pageDataBean.getUserLevel().length() == 5) {
            char[] arr = pageDataBean.getUserLevel().toCharArray();
            tvVip.setVisibility('1' == arr[1]?View.VISIBLE:View.GONE);
            tvGirl.setVisibility('1' == arr[2]?View.VISIBLE:View.GONE);
            tvReleaseTime.setVisibility('1' == arr[4]?View.VISIBLE:View.GONE);
        }
        fakeData();
    }

    @OnClick({R.id.iv_back, R.id.iv_more})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_more:
                ReportPopupWindow reportPopupWindow=new ReportPopupWindow(MomentDetailActivity.this,pageDataBean.getDiscussFlag()==0?"开启评论":"禁止评论","删除动态",view);
                reportPopupWindow.setBtnOnClice(new ReportPopupWindow.BtnReportOnClick() {
                    @Override
                    public void btnReportOnClick(int type) {
                        if(type==1){
                            anonymousReporting(pageDataBean);
                        }else if(type==2){
                            Delblock(pageDataBean);
                        }
                    }
                });
                break;
        }
    }

    /**
     * 禁止评论
     * @param pageDataBean
     */
    public void anonymousReporting(PageDataBean pageDataBean) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("programId", pageDataBean.getProgramId());
        params.put("flag", pageDataBean.getDiscussFlag()==0?String.valueOf(1):String.valueOf(0));
        HttpUtils.post().url(coreManager.getConfig().RED_MY_UPDATE_DISCUSS_FLAG)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (result.getResultCode() == 1) {
                            pageDataBean.setDiscussFlag(pageDataBean.getDiscussFlag()==0?1:0);
                            ToastUtil.showLongToast(MomentDetailActivity.this,pageDataBean.getDiscussFlag()==0?"禁止评论成功":"开启评论成功");

                        }
                    }
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(MomentDetailActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });



    }
    /**
     * 删除节目
     * @param pageDataBean
     */
    public void Delblock(PageDataBean pageDataBean) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("programId", pageDataBean.getProgramId());
        HttpUtils.post().url(coreManager.getConfig().RED_MY_UPDATE_DELETE)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (result.getResultCode() == 1) {
                            ToastUtil.showLongToast(MomentDetailActivity.this,"删除动态成功");
                            finish();
                        }
                    }
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(MomentDetailActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });



    }


    public static void startActivity(Context context, PageDataBean pageDataBean) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, MomentDetailActivity.class);
        intent.putExtra(GlobalConstants.KEY_THEME, pageDataBean);
        context.startActivity(intent);

    }

    private void fakeData() {

        List<PageDataBean.LikesBean> likes = pageDataBean.getLikes();
        mThumbAdapter.setNewData(likes);
        List<PageDataBean.DiscussesBean> discussesBeanList = pageDataBean.getDiscusses();
        List<String> dataComment = new ArrayList<>();
        for (int i = 0; i < discussesBeanList.size(); i++) {
            dataComment.add(discussesBeanList.get(i).getContent());
        }
        mCommentAdapter.setNewData(discussesBeanList);
        if(discussesBeanList!=null && discussesBeanList.size()>2){
            tvShowMore.setVisibility(View.VISIBLE);
        }
        tvShowMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //content
                CommentDetailListActivity.startActivity(mContext,pageDataBean);
            }
        });
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
            }

        }

        @Override
        public int getItemCount() {
            return likeMeBeanList.size();
        }
    }
}
