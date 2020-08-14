package com.redchamber.friend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.api.GlobalConstants;
import com.redchamber.api.RequestCode;
import com.redchamber.auth.AuthenticationCenterActivity;
import com.redchamber.bar.AppointmentComActivity;
import com.redchamber.bar.BarFragment;
import com.redchamber.bean.BannerImageBean;
import com.redchamber.bean.PageDataBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.radio.ProgramDetailActivity;
import com.redchamber.report.AnonymousReportActivity;
import com.redchamber.request.BlackRequest;
import com.redchamber.util.RedAvatarUtils;
import com.redchamber.view.CommCodeDialog;
import com.redchamber.view.EvenWheatCodeDialog;
import com.redchamber.view.ReleaseCodeDialog;
import com.redchamber.view.ReportPopupWindow;
import com.redchamber.vip.VipCenterActivity;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.UploadFileResult;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.helper.UploadService;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.TrillCommentInputDialog;
import com.sk.weichat.view.cjt2325.cameralibrary.util.LogUtil;
import com.sk.weichat.view.photopicker.PhotoPickerActivity;
import com.sk.weichat.view.photopicker.SelectModel;
import com.sk.weichat.view.photopicker.intent.PhotoPickerIntent;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

import static com.sk.weichat.MyApplication.getContext;

/**
 * 用户广播
 */
public class HeProgramOnLineActivity extends BaseActivity {


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
    @BindView(R.id.tv_dianzan)
    TextView tvDianzan;
    @BindView(R.id.tv_comm)
    TextView tvComm;
    @BindView(R.id.tv_apply)
    TextView tvApply;
    @BindView(R.id.rcl_like_phot)
    RecyclerView rclLikePhot;
    private String mUserId;
    private String programId;
    private PageDataBean pageDataBean = new PageDataBean();
    private ArrayList<String> mPhotoList = new ArrayList<>();//手机相册图片文件
    private String mPhotoUrlList = "";


    @Override
    protected int setLayout() {
        return R.layout.activity_he_program_on_line;
    }

    @Override
    protected void initView() {
        getSupportActionBar().hide();
        if (getIntent() != null) {
            mUserId = getIntent().getStringExtra(GlobalConstants.KEY_THEME);
            programId = getIntent().getStringExtra(GlobalConstants.KEY_PROGRAM_ID);
        }
        userProgramDetails();
        initOnclick();
    }

    private void initOnclick() {



    }

    private void userProgramDetails() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        LogUtil.e("data************************************ =" + coreManager.getSelf().getUserId());
        params.put("programId", programId);
        params.put("userId", mUserId);
        HttpUtils.post().url(coreManager.getConfig().RED_MY_USER_PROGRAM_DETAILS)
                .params(params)
                .build()
                .execute(new BaseCallback<PageDataBean>(PageDataBean.class) {
                    @Override
                    public void onResponse(ObjectResult<PageDataBean> result) {
                        if (Result.checkSuccess(HeProgramOnLineActivity.this, result) && result.getData() != null) {
                            pageDataBean = result.getData();
                            setUiData();

                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(HeProgramOnLineActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void setUiData() {
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
            tvDianzan.setText(pageDataBean.getLikeNum() == 0 ? "点赞" : String.valueOf(pageDataBean.getLikeNum()));
            tvComm.setText(pageDataBean.getDiscussNum() == 0 ? "评论" : "评论(" + String.valueOf(pageDataBean.getDiscussNum()) + ")");
            tvApply.setText(pageDataBean.getSignUpNums() == 0 ? "我要报名(0)" : "我要报名(" + String.valueOf(pageDataBean.getSignUpNums()) + ")");

            if(pageDataBean.getDiscussFlag()==0){
                tvApply.setText("报名结束");
            }
            if (pageDataBean.getImages() != null && pageDataBean.getImages().length() > 0) {
                List<String> likeMeBeanList = new ArrayList<String>();
                if (pageDataBean.getImages().contains(";")) {
                    String[] imageData = pageDataBean.getImages().split(";");
                    for (int i = 0; i < imageData.length; i++) {
                        likeMeBeanList.add(imageData[i]);
                    }
                } else {
                    likeMeBeanList.add(pageDataBean.getImages());
                }
                HeProgramOnLineActivity.CheckLikesMeAdapter checkLikesMeAdapter = new HeProgramOnLineActivity.CheckLikesMeAdapter(mContext);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                rclLikePhot.setLayoutManager(linearLayoutManager);
                checkLikesMeAdapter.likeMeBeanList = likeMeBeanList;
                rclLikePhot.setAdapter(checkLikesMeAdapter);
                checkLikesMeAdapter.notifyDataSetChanged();
            }
        }

    }



    @OnClick({R.id.iv_more, R.id.ll_dianzan, R.id.ll_comm, R.id.ll_apply,R.id.iv_back})
    void onClick(View view) {
        if (MyApplication.mMyHomepageBean != null) {
            char[] arr = MyApplication.mMyHomepageBean.userLevel.toCharArray();

        switch (view.getId()) {
            case R.id.iv_more:
                ReportPopupWindow reportPopupWindow = new ReportPopupWindow(HeProgramOnLineActivity.this, "匿名举报", "拉黑", view);
                reportPopupWindow.setBtnOnClice(new ReportPopupWindow.BtnReportOnClick() {
                    @Override
                    public void btnReportOnClick(int type) {
                        if (type == 1) {
                            // anonymousReporting(pageDataBean,Position);
                            AnonymousReportActivity.startActivity(HeProgramOnLineActivity.this, String.valueOf(pageDataBean.getUserId()));
                        } else if (type == 2) {
                            block();
                        }
                    }
                });

                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.ll_dianzan:
                if(arr[0]=='1'&&arr[1]=='0'){
                    CommCodeDialog commCodeDialog = new CommCodeDialog(HeProgramOnLineActivity.this, "当前需要开通VIP", "办理会员");
                    commCodeDialog.show();
                    commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
                        @Override
                        public void onConfirmClick() {
                            VipCenterActivity.startVipCenterActivity(HeProgramOnLineActivity.this);
                        }
                    });
                    return;
                }else if((arr[0]=='0')){
                    if(arr[1]=='0'&& arr[3]=='0') {
                        ReleaseCodeDialog commCodeDialog = new ReleaseCodeDialog(HeProgramOnLineActivity.this, "需要真人认证或VIP", "真人认证", "办理会员");
                        commCodeDialog.show();
                        commCodeDialog.setOnConfirmListener(new ReleaseCodeDialog.OnConfirmListener() {
                            @Override
                            public void onConfirmClick(int type) {
                                if (type == 1) {
                                    AuthenticationCenterActivity.startAuthenticationCenterActivity(HeProgramOnLineActivity.this);
                                } else if (type == 2) {
                                    VipCenterActivity.startVipCenterActivity(HeProgramOnLineActivity.this);
                                }
                            }
                        });
                        return;
                    }
                }

                puJoinProgram(pageDataBean, 0, "");
                break;
            case R.id.ll_comm:
                if(arr[0]=='1'&&arr[1]=='0'){
                    CommCodeDialog commCodeDialog = new CommCodeDialog(HeProgramOnLineActivity.this, "当前需要开通VIP", "办理会员");
                    commCodeDialog.show();
                    commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
                        @Override
                        public void onConfirmClick() {
                            VipCenterActivity.startVipCenterActivity(HeProgramOnLineActivity.this);
                        }
                    });
                    return;
                }else if((arr[0]=='0')&&arr[1]=='0'||arr[3]=='0'){
                    ReleaseCodeDialog commCodeDialog = new ReleaseCodeDialog(HeProgramOnLineActivity.this, "需要真人认证或VIP", "真人认证", "办理会员");
                    commCodeDialog.show();
                    commCodeDialog.setOnConfirmListener(new ReleaseCodeDialog.OnConfirmListener() {
                        @Override
                        public void onConfirmClick(int type) {
                            if (type == 1) {
                                AuthenticationCenterActivity.startAuthenticationCenterActivity(HeProgramOnLineActivity.this);
                            } else if (type == 2) {
                                VipCenterActivity.startVipCenterActivity(HeProgramOnLineActivity.this);
                            }
                        }
                    });
                    return;
                }
                if (arr[3] == '1') {
                    if(pageDataBean.getDiscussFlag()==0){
                        ToastUtil.showLongToast(HeProgramOnLineActivity.this,"用户评论已经关闭");
                        return;
                    }
                    TrillCommentInputDialog trillCommentInputDialog = new TrillCommentInputDialog(HeProgramOnLineActivity.this, getString(R.string.enter_pinlunt),
                            str -> {
                                LogUtil.e("***********" + str);
                                puJoinProgram(pageDataBean, 1, str);
                            });
                    Window window = trillCommentInputDialog.getWindow();
                    if (window != null) {
                        //OnSendCommentListener
                        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);// 软键盘弹起
                        trillCommentInputDialog.show();
                    }
                } else {
                    CommCodeDialog commCodeDialog = new CommCodeDialog(HeProgramOnLineActivity.this, "真人认证后才可以评论", "去真人认证");
                    commCodeDialog.show();
                    commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
                        @Override
                        public void onConfirmClick() {
                            AuthenticationCenterActivity.startAuthenticationCenterActivity(HeProgramOnLineActivity.this);
                        }
                    });
                }
                break;
            case R.id.ll_apply:
                if(arr[0]=='1'&&arr[1]=='0'){
                    CommCodeDialog commCodeDialog = new CommCodeDialog(HeProgramOnLineActivity.this, "当前需要开通VIP", "办理会员");
                    commCodeDialog.show();
                    commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
                        @Override
                        public void onConfirmClick() {
                            VipCenterActivity.startVipCenterActivity(HeProgramOnLineActivity.this);
                        }
                    });
                    return;
                }else if((arr[0]=='0')&&arr[1]=='0'||arr[3]=='0'){
                    ReleaseCodeDialog commCodeDialog = new ReleaseCodeDialog(HeProgramOnLineActivity.this, "需要真人认证或VIP", "真人认证", "办理会员");
                    commCodeDialog.show();
                    commCodeDialog.setOnConfirmListener(new ReleaseCodeDialog.OnConfirmListener() {
                        @Override
                        public void onConfirmClick(int type) {
                            if (type == 1) {
                                AuthenticationCenterActivity.startAuthenticationCenterActivity(HeProgramOnLineActivity.this);
                            } else if (type == 2) {
                                VipCenterActivity.startVipCenterActivity(HeProgramOnLineActivity.this);
                            }
                        }
                    });
                    return;
                }
                if(pageDataBean.getProgramFlag()==0){
                    ToastUtil.showLongToast(HeProgramOnLineActivity.this,"节目已停止报名");
                    return;
                }
                if (arr[3] == '0') {
                    EvenWheatCodeDialog evenWheatCodeDialog = new EvenWheatCodeDialog(HeProgramOnLineActivity.this, "认证你的真实性后，才能报名", "马上认证", null);
                    evenWheatCodeDialog.show();
                    evenWheatCodeDialog.setOnConfirmListener(new EvenWheatCodeDialog.OnConfirmListener() {
                        @Override
                        public void onConfirmClick() {
                            AuthenticationCenterActivity.startAuthenticationCenterActivity(HeProgramOnLineActivity.this);
                        }
                    });
                    return;
                }

                CommCodeDialog commCodeDialog = new CommCodeDialog( HeProgramOnLineActivity.this, "报名需发送你的正脸照片（只有Ta能看到", "选择照片");
                commCodeDialog.show();
                commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
                    @Override
                    public void onConfirmClick() {

                        selectPhoto();
                    }
                });


                break;
        }

        }
    }


    /**
     * 拉黑
     *
     */
    public void block() {
            BlackRequest.getInstance().addBlackList(HeProgramOnLineActivity.this, String.valueOf(pageDataBean.getUserId()),
                    "0", new BlackRequest.AddBlackListCallBack() {
                        @Override
                        public void onSuccess() {
                               // finish();
                        }

                        @Override
                        public void onFail(String error) {
                            ToastUtils.showToast(error);
                        }
                    });

    }


    /**
     * 参与节目/动态(点赞、评论、报名等)
     */
    private void puJoinProgram(PageDataBean pageDataBean, int type, String contnet) {
        List<BannerImageBean> list = new ArrayList<>();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("programId", pageDataBean.getProgramId());
        params.put("type", String.valueOf(type));
        params.put("content", contnet);
        HttpUtils.post().url(coreManager.getConfig().RED_JOIN_PROGRAM)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (result.getResultCode() == 1) {
                            switch (type) {
                                case 0:
                                    ToastUtil.showLongToast(HeProgramOnLineActivity.this, "点赞成功");
                                    pageDataBean.setLikeNum(pageDataBean.getLikeNum() + 1);
                                    tvDianzan.setText(pageDataBean.getLikeNum() == 0 ? "" : String.valueOf(pageDataBean.getLikeNum()+1));
                                    break;
                                case 1:
                                    ToastUtil.showLongToast(HeProgramOnLineActivity.this, "评论成功");
                                    pageDataBean.setDiscussNum(pageDataBean.getDiscussNum() + 1);
                                    tvComm.setText(pageDataBean.getDiscussNum() == 0 ? "评论" : "评论(" + String.valueOf(pageDataBean.getDiscussNum()+1) + ")");
                                    break;
                                case 2:
                                    pageDataBean.setSignUpNums(pageDataBean.getSignUpNums() + 1);
                                    tvApply.setText(pageDataBean.getSignUpNums() == 0 ? "我要报名(0)" : "我要报名(" + String.valueOf(pageDataBean.getSignUpNums()+1) + ")");
                                    //报名成功，
                                    CommCodeDialog commCodeDialog = new CommCodeDialog(HeProgramOnLineActivity.this, "报名成功，如果对方觉得合适将会联系你", "知道了");
                                    commCodeDialog.show();
                                    commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
                                        @Override
                                        public void onConfirmClick() {

                                        }
                                    });
                                    break;
                            }

                        } else {
                         showBarCom(result.getResultMsg(), result.getResultCode());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(HeProgramOnLineActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void showBarCom(String title, int resultCode) {
        String confirm = null;

        if (resultCode == REAL_PEOPLE_VERIFY_FIRST) {
            confirm = "真人认证后才可以发布";

        } else if (resultCode == HAVENOLEGAL_POWER) {
            confirm = "会员才可以评论哦";
            title = "成为会员，免费评论";

        } else if (resultCode == TIMES_HAVE_USED) {
            confirm = "次数已用完";
        } else if (resultCode == 100445) {
            //没有配置次数，提示升级到VIP或则余额购买
            confirm = "确认";
        } else {
            //没有配置次数，提示升级到VIP或则余额购买
            confirm = "确认";
        }


        CommCodeDialog commCodeDialog = new CommCodeDialog(HeProgramOnLineActivity.this, title, confirm);
        commCodeDialog.show();
        commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick() {
                if (resultCode == REAL_PEOPLE_VERIFY_FIRST) {
                    AuthenticationCenterActivity.startAuthenticationCenterActivity(HeProgramOnLineActivity.this);
                } else if (resultCode == HAVENOLEGAL_POWER) {
                    VipCenterActivity.startVipCenterActivity(HeProgramOnLineActivity.this);
                } else if (resultCode == TIMES_HAVE_USED) {

                } else if (resultCode == 100445) {
                    //    VipCenterActivity.startVipCenterActivity(getContext());
                    //   queryFreeAuthTimes(resultCode);
                }

            }
        });

    }
    private int REAL_PEOPLE_VERIFY_FIRST = 100439;
    private int HAVENOLEGAL_POWER = 100440;
    private int TIMES_HAVE_USED = 10441;

    public static void startActivity(Context context, String mUserId, String programId) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, HeProgramOnLineActivity.class);
        intent.putExtra(GlobalConstants.KEY_THEME, mUserId);
        intent.putExtra(GlobalConstants.KEY_PROGRAM_ID, programId);
        context.startActivity(intent);
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
        public CheckLikesMeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_barradio_adapter, null);
          CheckLikesMeAdapter.ViewHolder viewHolder = new CheckLikesMeAdapter.ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final CheckLikesMeAdapter.ViewHolder holder, final int position) {
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



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.REQUEST_CODE_PICK_PHOTO) {
            // 选择图片返回
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
//                    boolean isOriginal = data.getBooleanExtra(PhotoPickerActivity.EXTRA_RESULT_ORIGINAL, false);
//                    album(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT), isOriginal);
                    album(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT));
                } else {
                    ToastUtils.showToast(getString(R.string.c_photo_album_failed));
                }
            }
        }
    }

    /**
     * 相册
     * 可以多选的图片选择器
     */
    private void selectPhoto() {
        ArrayList<String> imagePaths = new ArrayList<>();
        PhotoPickerIntent intent = new PhotoPickerIntent(HeProgramOnLineActivity.this);
        intent.setSelectModel(SelectModel.MULTI);
        // 是否显示拍照， 默认false
        intent.setShowCarema(false);
        // 最多选择照片数量，默认为9
        intent.setMaxTotal(9);
        // 已选中的照片地址， 用于回显选中状态
        intent.setSelectedPaths(imagePaths);
        // intent.setImageConfig(config);
        // 是否加载视频，默认true
        intent.setLoadVideo(false);
        startActivityForResult(intent, RequestCode.REQUEST_CODE_PICK_PHOTO);
    }

    // 多张图片压缩 相册
    private void album(ArrayList<String> stringArrayListExtra) {
        Log.e("zq", "原图上传，不压缩，选择原文件路径");
        for (int i = 0; i < stringArrayListExtra.size(); i++) {
            mPhotoList.add(stringArrayListExtra.get(i));
        }
        new UploadPhoto().execute();
    }

    private class UploadPhoto extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DialogHelper.showDefaulteMessageProgressDialog(HeProgramOnLineActivity.this);
        }

        /**
         * 上传的结果： <br/>
         * return 1 Token过期，请重新登陆 <br/>
         * return 2 上传出错<br/>
         * return 3 上传成功<br/>
         */
        @Override
        protected Integer doInBackground(Void... params) {
            if (!LoginHelper.isTokenValidation()) {
                return 1;
            }
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("access_token", coreManager.getSelfStatus().accessToken);
            mapParams.put("userId", coreManager.getSelf().getUserId() + "");
            mapParams.put("validTime", "-1");// 文件有效期

            String result = new UploadService().uploadFile(coreManager.getConfig().UPLOAD_URL, mapParams, mPhotoList);
            if (TextUtils.isEmpty(result)) {
                return 2;
            }

            UploadFileResult recordResult = JSON.parseObject(result, UploadFileResult.class);
            boolean success = Result.defaultParser(HeProgramOnLineActivity.this, recordResult, true);
            if (success) {
                if (recordResult.getSuccess() != recordResult.getTotal()) {
                    // 上传丢失了某些文件
                    return 2;
                }
                if (recordResult.getData() != null) {
                    UploadFileResult.Data data = recordResult.getData();
                    if (data.getImages() != null && data.getImages().size() > 0) {
//                        mImageData = JSON.toJSONString(data.getImages(), UploadFileResult.sImagesFilter);
                        //recordResult.getData().getImages().get(0).originalUrl
                        mPhotoUrlList = "";
                        for (int i = 0; i < data.getImages().size(); i++) {
                            mPhotoUrlList += data.getImages().get(i).getOriginalUrl() + ";";
                        }
                        mPhotoUrlList = mPhotoUrlList.substring(0, mPhotoUrlList.length() - 1);
                    } else {
                        return 2;
                    }
                    return 3;
                } else {
                    // 没有文件数据源，失败
                    return 2;
                }
            } else {
                return 2;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == 1) {
                DialogHelper.dismissProgressDialog();
//                startActivity(new Intent(getContext(), LoginHistoryActivity.class));
            } else if (result == 2) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(HeProgramOnLineActivity.this, getString(R.string.upload_failed));
            } else {
                DialogHelper.dismissProgressDialog();
                puJoinProgram(pageDataBean, 2, mPhotoUrlList);
            }
        }
    }

}
