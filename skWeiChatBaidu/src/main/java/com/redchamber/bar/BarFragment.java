package com.redchamber.bar;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.fastjson.JSON;
import com.google.android.material.appbar.AppBarLayout;
import com.redchamber.api.RequestCode;
import com.redchamber.auth.AuthenticationCenterActivity;
import com.redchamber.bar.adapter.BannerImageAdapter;
import com.redchamber.bar.adapter.BarRadioAdapter;
import com.redchamber.bean.BannerImageBean;
import com.redchamber.bean.BarHomeBean;
import com.redchamber.bean.PageDataBean;
import com.redchamber.bean.QueryFreeAuthBean;
import com.redchamber.lib.base.BaseFragment;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.release.ReleaseMomentActivity;
import com.redchamber.report.AnonymousReportActivity;
import com.redchamber.request.BlackRequest;
import com.redchamber.view.BarTimeAreaSelectDialog;
import com.redchamber.view.CommCodeDialog;
import com.redchamber.view.EvenWheatCodeDialog;
import com.redchamber.view.ReleaseCodeDialog;
import com.redchamber.view.ReleaseRadioSelectDialog;
import com.redchamber.view.ReleaseTimeAreaSelectDialog;
import com.redchamber.view.ReportPopupWindow;
import com.redchamber.view.SelectProgramThemeDialog;
import com.redchamber.vip.VipCenterActivity;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sk.weichat.AppConfig;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.UploadFileResult;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.helper.UploadService;
import com.sk.weichat.ui.tool.WebViewActivity;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.TrillCommentInputDialog;
import com.sk.weichat.view.cjt2325.cameralibrary.util.LogUtil;
import com.sk.weichat.view.photopicker.PhotoPickerActivity;
import com.sk.weichat.view.photopicker.SelectModel;
import com.sk.weichat.view.photopicker.intent.PhotoPickerIntent;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 首页-  约吧
 */
public class BarFragment extends BaseFragment {

    @BindView(R.id.appbar)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.banner)
    Banner mBanner;
    @BindView(R.id.rv)
    RecyclerView mRvRadio;
    @BindView(R.id.swipeRefreshLayout)
    com.scwang.smartrefresh.layout.SmartRefreshLayout  swipeRefreshLayout;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.tv_area)
    TextView tvArea;
    private boolean isRefresh = false;

    private BarRadioAdapter mAdapter;
    private ArrayList<String> mPhotoList = new ArrayList<>();//手机相册图片文件
    private String mPhotoUrlList = "";


    @Override
    protected int setLayout() {
        return R.layout.red_fragment_bar;
    }

    private PageDataBean pageApplyDataBean = new PageDataBean();
    private int fuctionPositio = 0;

    @Override
    protected void initView() {
        initBanner();
        mAdapter = new BarRadioAdapter(null);
        mRvRadio.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvRadio.setAdapter(mAdapter);
        mRvRadio.setHasFixedSize(true);

        mAdapter.setBtnOnClice(new BarRadioAdapter.BtnOnClick() {
            @Override
            public void btnOnClick(PageDataBean pageDataBean, int fuctionType, int Position) {

                if (MyApplication.mMyHomepageBean != null) {
                    char[] arr = MyApplication.mMyHomepageBean.userLevel.toCharArray();
                    //用户级别 第一位为性别、第二位为是否VIP认证、第三位为是否女神男神认证、第四位为是否真人认证、第五位为是否有徽章
                    //男  当前需要开通VIPstatic final int OpenVip = 100500;//  女姓  需要真人认证或VIPstatic final int AuthenticationOrOpenVip = 100600;
                    if(arr[0]=='1'&&arr[1]=='0'){
                        CommCodeDialog commCodeDialog = new CommCodeDialog(getContext(), "当前需要开通VIP", "办理会员");
                        commCodeDialog.show();
                        commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
                            @Override
                            public void onConfirmClick() {
                                VipCenterActivity.startVipCenterActivity(getContext());
                            }
                        });
                        return;
                    }else if((arr[0]=='0')){
                        if(arr[3]=='0'&& arr[1]=='0') {
                            ReleaseCodeDialog commCodeDialog = new ReleaseCodeDialog(getContext(), "需要真人认证或VIP", "真人认证", "办理会员");
                            commCodeDialog.show();
                            commCodeDialog.setOnConfirmListener(new ReleaseCodeDialog.OnConfirmListener() {
                                @Override
                                public void onConfirmClick(int type) {
                                    if (type == 1) {
                                        AuthenticationCenterActivity.startAuthenticationCenterActivity(getContext());
                                    } else if (type == 2) {
                                        VipCenterActivity.startVipCenterActivity(getContext());
                                    }
                                }
                            });
                            return;
                        }

                    }

                    switch (fuctionType) {
                        case 1:
                            puJoinProgram(pageDataBean, 0, "", Position);
                            break;
                        case 2:
                            if(pageDataBean.getDiscussFlag()==0){
                                ToastUtil.showLongToast(getContext(),"用户评论已经关闭");
                                return;
                            }
                            if (arr[3] == '1'|| arr[1]=='1') {
                                TrillCommentInputDialog trillCommentInputDialog = new TrillCommentInputDialog(getContext(), getString(R.string.enter_pinlunt),
                                        str -> {
                                            LogUtil.e("***********" + str);
                                            puJoinProgram(pageDataBean, 1, str, Position);
                                        });
                                Window window = trillCommentInputDialog.getWindow();
                                if (window != null) {
                                    //OnSendCommentListener
                                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);// 软键盘弹起
                                    trillCommentInputDialog.show();
                                }
                            } else {
                                CommCodeDialog commCodeDialog = new CommCodeDialog(getContext(), "真人认证后才可以评论", "去真人认证");
                                commCodeDialog.show();
                                commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
                                    @Override
                                    public void onConfirmClick() {
                                        AuthenticationCenterActivity.startAuthenticationCenterActivity(getContext());
                                    }
                                });
                            }

                            break;
                        case 3:
                            if(pageDataBean.getProgramFlag()==0){
                                ToastUtil.showLongToast(getContext(),"节目已停止报名");
                                return;
                            }
                            if (arr[3] == '0'&& arr[1]=='0') {
                                EvenWheatCodeDialog evenWheatCodeDialog = new EvenWheatCodeDialog(getContext(), "认证你的真实性后，才能报名", "马上认证", null);
                                evenWheatCodeDialog.show();
                                evenWheatCodeDialog.setOnConfirmListener(new EvenWheatCodeDialog.OnConfirmListener() {
                                    @Override
                                    public void onConfirmClick() {
                                        AuthenticationCenterActivity.startAuthenticationCenterActivity(getContext());
                                    }
                                });
                                return;
                            }

                            CommCodeDialog commCodeDialog = new CommCodeDialog(getContext(), "报名需发送你的正脸照片（只有Ta能看到", "选择照片");
                            commCodeDialog.show();
                            commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
                                @Override
                                public void onConfirmClick() {
                                    //报名 相机 图片
                                    pageApplyDataBean = pageDataBean;
                                    fuctionPositio = Position;
                                    selectPhoto();
                                }
                            });

                            break;
                    }
                }
            }
        });
        mAdapter.setBtnOnClice(new BarRadioAdapter.BtnOnCommClick() {
            @Override
            public void btnOnCommClick(PageDataBean pageDataBean, int fuctionType, int Position, View view) {
                ReportPopupWindow reportPopupWindow = new ReportPopupWindow(getActivity(), "匿名举报", "拉黑", view);
                reportPopupWindow.setBtnOnClice(new ReportPopupWindow.BtnReportOnClick() {
                    @Override
                    public void btnReportOnClick(int type) {
                        if (type == 1) {
                            // anonymousReporting(pageDataBean,Position);
                            AnonymousReportActivity.startActivity(getContext(), String.valueOf(pageDataBean.getUserId()));
                        } else if (type == 2) {
                            block(pageDataBean, Position);
                        }
                    }
                });

            }
        });
        initRefreshLayout();
        getMessageList();
    }

    /**
     * 匿名举报
     *
     * @param pageDataBean
     * @param Position
     */
    public void anonymousReporting(PageDataBean pageDataBean, int Position) {
        LogUtil.e("匿名举报");

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
        PhotoPickerIntent intent = new PhotoPickerIntent(getContext());
        intent.setSelectModel(SelectModel.MULTI);
        // 是否显示拍照， 默认false
        intent.setShowCarema(false);
        // 最多选择照片数量，默认为9
        intent.setMaxTotal(1);
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
            DialogHelper.showDefaulteMessageProgressDialog(getContext());
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
            boolean success = Result.defaultParser(getContext(), recordResult, true);
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
                ToastUtil.showToast(getContext(), getString(R.string.upload_failed));
            } else {
                DialogHelper.dismissProgressDialog();
                puJoinProgram(pageApplyDataBean, 2, mPhotoUrlList, fuctionPositio);
            }
        }
    }


    /**
     * 拉黑
     *
     * @param pageDataBean
     * @param position
     */
    public void block(PageDataBean pageDataBean, int position) {
        BlackRequest.getInstance().addBlackList(getContext(), String.valueOf(pageDataBean.getUserId()),
                "0", new BlackRequest.AddBlackListCallBack() {
                    @Override
                    public void onSuccess() {
                      /*  mAdapter.remove(position);
                        mAdapter.notifyDataSetChanged();*/
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
    private void puJoinProgram(PageDataBean pageDataBean, int type, String contnet, int position) {
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
                                    ToastUtil.showLongToast(getActivity(), "点赞成功");
                                    pageDataBean.setLikeNum(pageDataBean.getLikeNum() + 1);
                                    mAdapter.notifyItemChanged(position, pageDataBean);
                                    mAdapter.notifyDataSetChanged();
                                    break;
                                case 1:
                                    ToastUtil.showLongToast(getActivity(), "评论成功");
                                    pageDataBean.setDiscussNum(pageDataBean.getDiscussNum() + 1);
                                    mAdapter.notifyItemChanged(position, pageDataBean);
                                    mAdapter.notifyDataSetChanged();
                                    break;
                                case 2:
                                    pageDataBean.setSignUpNums(pageDataBean.getSignUpNums() + 1);
                                    mAdapter.notifyItemChanged(position, pageDataBean);
                                    ToastUtil.showLongToast(getActivity(), "报名成功");
                                    //报名成功，
                                    CommCodeDialog commCodeDialog = new CommCodeDialog(getContext(), "报名成功，如果对方觉得合适将会联系你", "知道了");
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
                        Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initRefreshLayout() {

        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageIndex = 1;
                getMessageList();
            }
        });
        swipeRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                pageIndex++;
                getMessageList();
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        if (mBanner != null) {
            mBanner.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBanner != null) {
            mBanner.stop();
        }
    }

    @OnClick({R.id.tv_release, R.id.iv_ktv, R.id.iv_travel, R.id.iv_movie, R.id.iv_food, R.id.iv_game,
            R.id.tv_time, R.id.tv_area})
    void onClick(View view) {
        List<PageDataBean> dataBeanList = new ArrayList<PageDataBean>();
        switch (view.getId()) {
            case R.id.tv_release:
                pubCheck();
                break;
            case R.id.iv_ktv:
                AppointmentComActivity.startActivity(getContext(), "约K歌");
                break;
            case R.id.iv_travel:
                AppointmentComActivity.startActivity(getContext(), "约旅游");
                break;
            case R.id.iv_movie:
                AppointmentComActivity.startActivity(getContext(), "约电影");
                break;
            case R.id.iv_food:
                AppointmentComActivity.startActivity(getContext(), "约美食");
                break;
            case R.id.iv_game:
                AppointmentComActivity.startActivity(getContext(), "约游戏");

                break;
            case R.id.tv_time:
                mAppBarLayout.setExpanded(false);
                BarTimeAreaSelectDialog timeSelectDialog = new BarTimeAreaSelectDialog(getContext(),
                        ReleaseTimeAreaSelectDialog.TYPE_TIME);
                timeSelectDialog.show();
                timeSelectDialog.setBtnOnClice(new BarTimeAreaSelectDialog.BtnReleaseOnClick() {
                    @Override
                    public void btnOnClick(String data) {
                        if (!TextUtils.isEmpty(data)) {
                            pageIndex = 1;
                            type = data;
                            mAdapter.setNewData(dataBeanList);
                            getMessageList();

                          tvTime.setText(data.equals("1")?"活动时间":"发布时间");
                        }
                    }
                });
                break;
            case R.id.tv_area:
                mAppBarLayout.setExpanded(false);
                BarTimeAreaSelectDialog areaSelectDialog = new BarTimeAreaSelectDialog(getContext(),
                        ReleaseTimeAreaSelectDialog.TYPE_AREA);
                areaSelectDialog.show();
                areaSelectDialog.setBtnOnClice(new BarTimeAreaSelectDialog.BtnCityOnClick() {
                    @Override
                    public void btnCityOnClick(String data) {
                        if (!TextUtils.isEmpty(data)) {
                            pageIndex = 1;
                            cityName = data;
                            mAdapter.setNewData(dataBeanList);
                            getMessageList();
                            tvArea.setText(data);
                        }
                    }
                });
                break;
        }
    }

    /**
     * 显示发布
     */
    private void showRelease(String programId) {
        ReleaseRadioSelectDialog radioSelectDialog = new ReleaseRadioSelectDialog(getContext());
        radioSelectDialog.show();
        radioSelectDialog.setOnConfirmListener(new ReleaseRadioSelectDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick(int type) {
                if (0 == type) {
                    if(!TextUtils.isEmpty(programId)){
                        ToastUtil.showLongToast(getContext(),"当前已有进行中的节目");
                    }else {
                        SelectProgramThemeDialog selectProgramThemeDialog = new SelectProgramThemeDialog(getContext());
                        selectProgramThemeDialog.show();
                    }
                } else {
                    ReleaseMomentActivity.startReleaseMomentActivity(getContext());
                }
            }
        });
    }

    /**
     * 发布前校验数据
     */
    private void pubCheck() {
        List<BannerImageBean> list = new ArrayList<>();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        HttpUtils.post().url(coreManager.getConfig().RED_PUB_CHECK)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {

                        if (result.getResultCode() == 1) {
                            String  programData=result.getData();
                            JSONObject jsonObject= null;
                            try {
                                jsonObject = new JSONObject(programData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String   programId= jsonObject.optString("programId");
                                showRelease(programId);
                        } else {
                            showBarCom(result.getResultMsg(), result.getResultCode());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int REAL_PEOPLE_VERIFY_FIRST = 100439;
    private int HAVENOLEGAL_POWER = 100440;
    private int TIMES_HAVE_USED = 100441;

    private void showBarCom(String title, int resultCode) {
        String confirm = null;

        if (resultCode == REAL_PEOPLE_VERIFY_FIRST) {
            confirm = "真人认证后才可以发布";

        } else if (resultCode == HAVENOLEGAL_POWER) {
            confirm = "会员才可以评论哦";
            title = "成为会员，免费评论";

        } else if (resultCode == TIMES_HAVE_USED) {
            confirm = "次数已用完";
            queryFreeAuthTimes(resultCode);
            return;
        } else if (resultCode == 100445) {
            //没有配置次数，提示升级到VIP或则余额购买
          /*  confirm = "确认";
            ReleaseCodeDialog commCodeDialog = new ReleaseCodeDialog(getContext(), "次数已用完", "真人认证", "办理会员");
            commCodeDialog.show();
            commCodeDialog.setOnConfirmListener(new ReleaseCodeDialog.OnConfirmListener() {
                @Override
                public void onConfirmClick(int type) {
                    if (type == 1) {
                        AuthenticationCenterActivity.startAuthenticationCenterActivity(getContext());
                    } else if (type == 2) {
                        VipCenterActivity.startVipCenterActivity(getContext());
                    }
                }
            });*/
            queryFreeAuthTimes(resultCode);
            return;
        }else if(resultCode==100600){
            ReleaseCodeDialog commCodeDialog = new ReleaseCodeDialog(getContext(), "需要真人认证或VIP", "真人认证", "办理会员");
            commCodeDialog.show();
            commCodeDialog.setOnConfirmListener(new ReleaseCodeDialog.OnConfirmListener() {
                @Override
                public void onConfirmClick(int type) {
                    if (type == 1) {
                        AuthenticationCenterActivity.startAuthenticationCenterActivity(getContext());
                    } else if (type == 2) {
                        VipCenterActivity.startVipCenterActivity(getContext());
                    }
                }
            });
            return;

        } else {
            //没有配置次数，提示升级到VIP或则余额购买
            confirm = "确认";
        }

        if (resultCode == 100445) {
            // 没有配置次数，提示升级到VIP或则余额购买 100445
            queryFreeAuthTimes(resultCode);
            return;
        }
        CommCodeDialog commCodeDialog = new CommCodeDialog(getContext(), title, confirm);
        commCodeDialog.show();
        commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick() {
                if (resultCode == REAL_PEOPLE_VERIFY_FIRST) {
                    AuthenticationCenterActivity.startAuthenticationCenterActivity(getContext());
                } else if (resultCode == HAVENOLEGAL_POWER) {
                    VipCenterActivity.startVipCenterActivity(getContext());
                } else if (resultCode == TIMES_HAVE_USED) {
                    queryFreeAuthTimes(resultCode);
                } else if (resultCode == 100445) {
                    //    VipCenterActivity.startVipCenterActivity(getContext());
                    //   queryFreeAuthTimes(resultCode);
                } else if (resultCode == 100500) {
                    VipCenterActivity.startVipCenterActivity(getContext());
                }

            }
        });

    }

    /**
     * 获取免费权限的次数
     */
    private void queryFreeAuthTimes(int resultCode) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", "001");
        HttpUtils.post().url(coreManager.getConfig().RED_MY_QUERY_FREEAUTH_TIMES)
                .params(params)
                .build()
                .execute(new BaseCallback<QueryFreeAuthBean>(QueryFreeAuthBean.class) {

                    @Override
                    public void onResponse(ObjectResult<QueryFreeAuthBean> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            if (resultCode == TIMES_HAVE_USED) {
                                payRed(result.getData(), TIMES_HAVE_USED);
                            } else if (resultCode == 100445) {
                                // 没有配置次数，提示升级到VIP或则余额购买 100445
                                VipOrPay(result.getData(), resultCode);
                            }

                        } else {
                            LogUtil.e(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void VipOrPay(QueryFreeAuthBean queryFreeAuthBean, int resultCode) {

        ReleaseCodeDialog commCodeDialog = new ReleaseCodeDialog(getContext(), "发布动态或节目", "支付" + queryFreeAuthBean.getPrice() + "红豆", "会员免费发布"+queryFreeAuthBean.getVipSumTimes()+"次");
        commCodeDialog.show();
        commCodeDialog.setOnConfirmListener(new ReleaseCodeDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick(int type) {
                if (type == 1) {
                    payPublish(queryFreeAuthBean.getPrice());
                } else if (type == 2) {
                    VipCenterActivity.startVipCenterActivity(getContext());
                }
            }
        });
    }

    /**
     * 红豆支付
     */
    private void payRed(QueryFreeAuthBean queryFreeAuthBean, int resultCode) {
        CommCodeDialog commCodeDialog = new CommCodeDialog(getContext(), "次数已用完", "支付" + queryFreeAuthBean.getPrice() + "红豆");
        commCodeDialog.show();
        commCodeDialog.setOnConfirmListener(new CommCodeDialog.OnConfirmListener() {
            @Override
            public void onConfirmClick() {
                payPublish(queryFreeAuthBean.getPrice());
            }
        });
    }

    /**
     * 付费发布
     */
    private void payPublish(int payPublish) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("gold", String.valueOf(payPublish));
        HttpUtils.post().url(coreManager.getConfig().RED_MY_PAY_FORPUB)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (result.getResultCode() == 1) {
                            showRelease(null);
                        } else {
                            ToastUtil.showLongToast(getContext(), result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void initBanner() {
        List<BannerImageBean> list = new ArrayList<>();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        //  params.put("title","");
        params.put("type", "1");
        //params.put("cityId","0000");
        HttpUtils.post().url(coreManager.getConfig().RED_GETPLANT_WHELL)
                .params(params)
                .build()
                .execute(new ListCallback<BannerImageBean>(BannerImageBean.class) {
                    @Override
                    public void onResponse(ArrayResult<BannerImageBean> result) {
                        if (Result.checkSuccess(getContext(), result)) {
                            if (result.getData() != null && result.getData().size() > 0) {
                                BannerImageAdapter bannerImageAdapter=new BannerImageAdapter(getContext(), result.getData());
                                mBanner.setAdapter(bannerImageAdapter)
                                        .setIndicator(new CircleIndicator(getContext()))
                                        .setBannerRound(50)
                                        .start();

                                bannerImageAdapter.setBtnOnClice(new BannerImageAdapter.BtnOnClick() {
                                    @Override
                                    public void btnOnClick(BannerImageBean bannerImageBean) {
                                        //
                                        if(!TextUtils.isEmpty(bannerImageBean.getJumpUrl())){
                                            WebViewActivity.start(getContext(),bannerImageBean.getJumpUrl());
                                        }
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int pageIndex = 1;
    private String type = "0";
    private String cityName = "上海市";

    private void getMessageList() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        //  params.put("title","");
        params.put("type", type);//活动时间 0，1
        //params.put("cityId","0000");
        if(!cityName.equals("不限地区")){
            params.put("cityName", cityName);
        }
        params.put("pageIndex", String.valueOf(pageIndex));
        params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));
       // pageIndex++;
        DialogHelper.showDefaulteMessageProgressDialog(getActivity());
        HttpUtils.post().url(coreManager.getConfig().RED_YUEBA_INDEX)
                .params(params)
                .build()
                .execute(new BaseCallback<BarHomeBean>(BarHomeBean.class) {
                    @Override
                    public void onResponse(ObjectResult<BarHomeBean> result) {
                        DialogHelper.dismissProgressDialog();
                        swipeRefreshLayout.finishRefresh(true);
                        swipeRefreshLayout.finishLoadMore(true);
                        if (result != null && result.getData() != null && result.getData().pageData != null
                                && result.getData().pageData.size() > 0) {
//                            Yuba data=JSON.parseObject(result.getData(), Yuba.class);
//                            if(data.getPageData()!=null){
////                              mAdapter.setNewData(data.getPageData());
//                                Log.e("e",data.getPageData().size()+"");
//                            }
                            if (pageIndex > 1) {
                                mAdapter.addData(result.getData().pageData);
                            } else {
                                mAdapter.setNewData(result.getData().pageData);
                            }
                          /*  mAdapter.setNewData(result.getData().pageData);
                            if (isRefresh) {
                                mAdapter.setNewData(result.getData().pageData);
                                isRefresh = false;
                            }*/
                        } else {
//                            ToastUtils.showToast("暂无数据");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(getActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.finishRefresh(false);
                        swipeRefreshLayout.finishLoadMore(false);
                    }
                });

    }

}
