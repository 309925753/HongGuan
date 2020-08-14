package com.redchamber.radio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.redchamber.bean.BannerImageBean;
import com.redchamber.bean.DiscussesBean;
import com.redchamber.bean.PageDataBean;
import com.redchamber.bean.PhotoBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.radio.adapter.MomentDetailCommentAdapter;
import com.redchamber.radio.adapter.MomentDetailThumbAdapter;
import com.redchamber.radio.adapter.ProgramDetailSignAdapter;
import com.redchamber.report.AnonymousReportActivity;
import com.redchamber.util.RedAvatarUtils;
import com.redchamber.view.ReportPopupWindow;
import com.sk.weichat.AppConfig;
import com.sk.weichat.R;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.ui.message.ChatActivity;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.cjt2325.cameralibrary.util.LogUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 节目详情 个人中心
 */
public class ProgramDetailActivity extends BaseActivity {

    @BindView(R.id.rv_thumb_list)
    RecyclerView mRvThumbList;
    @BindView(R.id.rv_comment_list)
    RecyclerView mRvCommentList;
    @BindView(R.id.rv_comment_sign)
    RecyclerView mRvSignList;
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
    @BindView(R.id.tv_radio_time)
    TextView tvRadioTime;
    @BindView(R.id.tv_radio_address)
    TextView tvRadioAddress;
    @BindView(R.id.tv_radio_expect)
    TextView tvRadioExpect;
    @BindView(R.id.tv_more_comment)
    TextView tvMoreComment;
    @BindView(R.id.tv_dianzan)
    TextView tvDianzan;
    @BindView(R.id.tv_comm)
    TextView tvComm;
    @BindView(R.id.tv_apply)
    TextView tvApply;
    @BindView(R.id.rcl_like_phot)
    RecyclerView rclLikePhot;

    private MomentDetailThumbAdapter mThumbAdapter;
    private MomentDetailCommentAdapter mCommentAdapter;
    private ProgramDetailSignAdapter mSignAdapter;
    private PageDataBean pageDataBean = new PageDataBean();


    @Override
    protected int setLayout() {
        return R.layout.red_activity_program_detail;
    }

    @Override
    protected void initView() {
        if (getIntent() != null) {
            pageDataBean = (PageDataBean) getIntent().getSerializableExtra(GlobalConstants.KEY_THEME);
        }
        getMessageList();
        mThumbAdapter = new MomentDetailThumbAdapter(this, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvThumbList.setLayoutManager(layoutManager);
        mRvThumbList.setAdapter(mThumbAdapter);

        mCommentAdapter = new MomentDetailCommentAdapter(null);
        mRvCommentList.setLayoutManager(new LinearLayoutManager(this));
        mRvCommentList.setAdapter(mCommentAdapter);

        mSignAdapter = new ProgramDetailSignAdapter(null);
        mRvSignList.setLayoutManager(new LinearLayoutManager(this));
        mRvSignList.setAdapter(mSignAdapter);


        if (pageDataBean != null) {
            tvNickname.setText(pageDataBean.getNickName());
            Glide.with(mContext).load(RedAvatarUtils.getAvatarUrl(mContext, String.valueOf(pageDataBean.getUserId()))).into(ivAvatar);
                /*viewHolderA.tvGirl.setText(item.getSex()==0?"男神":"女神");
                viewHolderA.tvVip.setText(item.get);*/

            if (!TextUtils.isEmpty(pageDataBean.getUserLevel()) && pageDataBean.getUserLevel().length() == 5) {
                char[] arr = pageDataBean.getUserLevel().toCharArray();
                tvVip.setVisibility('1' == arr[1] ? View.VISIBLE : View.GONE);
                tvGirl.setVisibility('1' == arr[2] ? View.VISIBLE : View.GONE);
                ivDebutante.setVisibility('1' == arr[4] ? View.VISIBLE : View.GONE);
            }

            tvReleaseTime.setText(TimeUtils.getFriendlyTimeDesc(mContext, pageDataBean.getPubTime()));
            tvRadioTheme.setText(pageDataBean.getTitle() == null ? "" : pageDataBean.getTitle());
            tvRadioTime.setText(TimeUtils.s_long_2_str(pageDataBean.getProgramDate()) + "" + pageDataBean.getProgramTime());//programDate programTime
            tvRadioAddress.setText(pageDataBean.getPlaceName() == null ? "" : pageDataBean.getPlaceName());
            tvRadioExpect.setText(pageDataBean.getExpectFriend() == null ? "" : pageDataBean.getExpectFriend());
            tvDianzan.setText(pageDataBean.getLikeNum() == 0 ? "" : String.valueOf(pageDataBean.getLikeNum()));
            tvComm.setText(pageDataBean.getDiscussNum() == 0 ? "评论" : "评论(" + String.valueOf(pageDataBean.getDiscussNum()) + ")");
            tvApply.setText(pageDataBean.getSignUpNums() == 0 ? "我要报名(0)" : "我要报名(" + String.valueOf(pageDataBean.getSignUpNums()) + ")");
           // mSignAdapter.setNewData(pageDataBean.getSignUps());
            if(pageDataBean.getProgramFlag()==0){
                tvApply.setText("结束报名");
            }

            if (pageDataBean.getImages() != null && pageDataBean.getImages().length() > 0) {
              /*  List<String> likeMeBeanList = new ArrayList<String>();
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
                rclLikePhot.setLayoutManager(linearLayoutManager);
                checkLikesMeAdapter.likeMeBeanList = likeMeBeanList;
                rclLikePhot.setAdapter(checkLikesMeAdapter);
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
                rclLikePhot.setLayoutManager(new GridLayoutManager(mContext, 4));
                rclLikePhot.setAdapter(mPhotoAdapter);
            }


        }


        fakeData();
        mSignAdapter.setBtnOnClice(new ProgramDetailSignAdapter.BtnOnClick() {
            @Override
            public void btnOnClick(PageDataBean.SignUpsBean _signUpsBean) {

               // chatOrLianMaiCheck(_signUpsBean);
                Intent intent = new Intent(mContext, ChatActivity.class);
                Friend friend = new Friend();
                friend.setNickName(_signUpsBean.getNickName());
                friend.setUserId(String.valueOf(_signUpsBean.getUserId()));
                intent.putExtra(ChatActivity.FRIEND, friend);
                mContext.startActivity(intent);
            }
        });
        mSignAdapter.setBtnOnClice(new ProgramDetailSignAdapter.BtReportnOnClick() {
            @Override
            public void btnOnClick(PageDataBean.SignUpsBean _signUpsBean) {
                /**
                 * to do  举报  接口
                 */
                AnonymousReportActivity.startActivity(ProgramDetailActivity.this, String.valueOf(pageDataBean.getUserId()));
                /*  LogUtil.e("_signUpsBean = " +_signUpsBean.getNickName());*/
            }
        });
    }

    private void getMessageList() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        LogUtil.e("data************************************ =" + coreManager.getSelf().getUserId());
        params.put("joinType", String.valueOf(2));
        params.put("programId", pageDataBean.getProgramId());
        params.put("pageIndex", String.valueOf(1));
        params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));
        HttpUtils.post().url(coreManager.getConfig().RED_MY_JOIN_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<PageDataBean.SignUpsBean>(PageDataBean.SignUpsBean.class) {
                    @Override
                    public void onResponse(ArrayResult<PageDataBean.SignUpsBean> result) {
                        if (Result.checkSuccess(ProgramDetailActivity.this, result) && result.getData() != null) {
                            List<PageDataBean.SignUpsBean> data = result.getData();
                      //      mAdapter.setNewData(data);
                            if(data!=null&&data.size()>0){
                                mSignAdapter.setNewData(data);
                            }
                          /*  if(isRefresh){
                                isRefresh=false;
                                mAdapter.addData(result.getData().pageData);
                            }*/
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ProgramDetailActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });

    }


    /**
     * 私聊或连麦前权限检查
     */
    private void chatOrLianMaiCheck(PageDataBean.SignUpsBean _signUpsBean) {

        List<BannerImageBean> list = new ArrayList<>();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", "005");
        params.put("otherUserId", String.valueOf(_signUpsBean.getUserId()));
        HttpUtils.post().url(coreManager.getConfig().RED_MY_CHATORLIANMAI_CHECK)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {

                        if (result.getResultCode() == 1) {

                        } else {
                         /*  showBarCom(result.getResultMsg(), result.getResultCode());*/
                            ToastUtil.showLongToast(ProgramDetailActivity.this,result.getResultMsg()+"");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ProgramDetailActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @OnClick({R.id.iv_back, R.id.iv_more, R.id.tv_more_comment})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_more:
                ReportPopupWindow reportPopupWindow = new ReportPopupWindow(ProgramDetailActivity.this, pageDataBean.getDiscussFlag() == 0 ? "开启评论" : "禁止评论", "删除节目", view);
                reportPopupWindow.setBtnOnClice(new ReportPopupWindow.BtnReportOnClick() {
                    @Override
                    public void btnReportOnClick(int type) {
                        if (type == 1) {
                            anonymousReporting(pageDataBean);
                        } else if (type == 2) {
                            Delblock(pageDataBean);
                        }
                    }
                });
                break;
            case R.id.tv_more_comment:
                // CommentDetailListActivity.startActivity(this,);
                CommentDetailListActivity.startActivity(mContext, pageDataBean);
                break;
        }
    }

    /**
     * 禁止评论
     *
     * @param pageDataBean
     */
    public void anonymousReporting(PageDataBean pageDataBean) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("programId", pageDataBean.getProgramId());
        params.put("flag", pageDataBean.getDiscussFlag() == 0 ? String.valueOf(1) : String.valueOf(0));
        HttpUtils.post().url(coreManager.getConfig().RED_MY_UPDATE_DISCUSS_FLAG)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (result.getResultCode() == 1) {
                            pageDataBean.setDiscussFlag(pageDataBean.getDiscussFlag() == 0 ? 1 : 0);
                            ToastUtil.showLongToast(ProgramDetailActivity.this, pageDataBean.getDiscussFlag() == 0 ? "禁止评论成功" : "开启评论成功");

                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ProgramDetailActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });


    }

    /**
     * 删除节目
     *
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
                            finish();
                            ToastUtil.showLongToast(ProgramDetailActivity.this, "删除节目成功");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ProgramDetailActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });


    }


    public static void startActivity(Context context, PageDataBean pageDataBean) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, ProgramDetailActivity.class);
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
        if (discussesBeanList != null && discussesBeanList.size() > 2) {
            tvMoreComment.setVisibility(View.VISIBLE);
        }
        tvMoreComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //content
                CommentDetailListActivity.startActivity(mContext, pageDataBean);
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
