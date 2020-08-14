package com.redchamber.mine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.api.RequestCode;
import com.redchamber.auth.AuthenticationCenterActivity;
import com.redchamber.auth.BuyBadgeActivity;
import com.redchamber.bean.MyHomepageBean;
import com.redchamber.event.UpdateUserInfo;
import com.redchamber.info.ImprovePersonalInfoActivity;
import com.redchamber.invite.MyInviteCodeActivity;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.PreferenceUtils;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.like.BlackListActivity;
import com.redchamber.like.MyLikeActivity;
import com.redchamber.mine.adapter.MyHomePageAlbumAdapter;
import com.redchamber.photo.RedAlbumActivity;
import com.redchamber.radio.MyRadioActivity;
import com.redchamber.request.PersonalInfoRequest;
import com.redchamber.setting.SettingActivity;
import com.redchamber.util.UserLevelUtils;
import com.redchamber.view.NonVipInviteCodeDialog;
import com.redchamber.view.PhotoPrivacyDialog;
import com.redchamber.view.ShareDialog;
import com.redchamber.view.YourTrueCommentDialog;
import com.redchamber.vip.VipCenterActivity;
import com.redchamber.wallet.WalletActivity;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.UploadFileResult;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.helper.UploadService;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.DeviceInfoUtil;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.photopicker.PhotoPickerActivity;
import com.sk.weichat.view.photopicker.SelectModel;
import com.sk.weichat.view.photopicker.intent.PhotoPickerIntent;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import top.zibin.luban.Luban;

/**
 * 个人主页面
 */
public class MinePrivateActivity extends BaseActivity {

    @BindView(R.id.tv_nickname)
    TextView mTvNickname;
    @BindView(R.id.iv_avatar)
    RoundedImageView mIvAvatar;
    @BindView(R.id.tv_age)
    TextView mTvAge;
    @BindView(R.id.tv_constellation)
    TextView mTvConstellation;
    @BindView(R.id.tv_job)
    TextView mTvJob;
    @BindView(R.id.tv_true)
    TextView mTvTrue;
    @BindView(R.id.tv_girl)
    TextView mTvGirl;
    @BindView(R.id.tv_vip)
    TextView mTvVip;
    @BindView(R.id.rv_photo)
    RecyclerView mRvPhoto;
    @BindView(R.id.ll_no_photo)
    LinearLayout mLlNoPhoto;
    @BindView(R.id.tv_pay_unlock)
    TextView mTvPayUnlock;
    @BindView(R.id.tv_version)
    TextView mTvVersion;
    @BindView(R.id.tv_red_photo)
    TextView mTvRedPhoto;
    @BindView(R.id.rl_photo_privacy)
    RelativeLayout mRlPhotoPrivacy;
    @BindView(R.id.rl_badge)
    RelativeLayout mRlBadge;
    @BindView(R.id.iv_back)
    ImageView ivBack;

    private MyHomePageAlbumAdapter mPhotoAdapter;
    private ArrayList<String> mPhotoList = new ArrayList<>();//手机相册图片文件
    private String mPhotoUrlList = "";
    private MyHomepageBean mMyHomepageBean;
    private Disposable mDisposableLuBan;

    @Override
    protected int setLayout() {
        return R.layout.red_fragment_mine_female;
    }

    @Override
    protected void initView() {
        getSupportActionBar().hide();
        EventBus.getDefault().register(this);
        mPhotoAdapter = new MyHomePageAlbumAdapter(MinePrivateActivity.this, null);
        mRvPhoto.setLayoutManager(new GridLayoutManager(MinePrivateActivity.this, 4));
        mRvPhoto.setAdapter(mPhotoAdapter);
        mTvVersion.setText(DeviceInfoUtil.getVersionName(MinePrivateActivity.this));
        ivBack.setVisibility(View.VISIBLE);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDisposableLuBan != null) {
            mDisposableLuBan.dispose();
        }
    }

    @OnClick({R.id.iv_back, R.id.tv_my_comment, R.id.iv_avatar, R.id.ll_person_info, R.id.ll_pocket, R.id.ll_my_like, R.id.rl_auth, R.id.rl_vip,
            R.id.rl_badge, R.id.rl_invite_code, R.id.rl_radio, R.id.tv_upload, R.id.ll_no_photo, R.id.tv_red_photo,
            R.id.rl_photo_privacy, R.id.rl_black_list, R.id.rl_setting, R.id.rl_share})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_my_comment:
                YourTrueCommentDialog yourTrueCommentDialog = new YourTrueCommentDialog(MinePrivateActivity.this);
                yourTrueCommentDialog.show();
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_avatar:
                //点击头像
                break;
            case R.id.ll_person_info:
                ImprovePersonalInfoActivity.startImprovePersonalInfoActivity(MinePrivateActivity.this, ImprovePersonalInfoActivity.TYPE_EDIT_INFO);
                break;
            case R.id.ll_pocket:
                WalletActivity.startWalletActivity(MinePrivateActivity.this);
                break;
            case R.id.ll_my_like:
                MyLikeActivity.startMyLikeActivity(MinePrivateActivity.this);
                break;
            case R.id.rl_auth:
                AuthenticationCenterActivity.startAuthenticationCenterActivity(MinePrivateActivity.this);
                break;
            case R.id.rl_vip:
                VipCenterActivity.startVipCenterActivity(MinePrivateActivity.this);
                break;
            case R.id.rl_badge:
                BuyBadgeActivity.startActivity(MinePrivateActivity.this);
                break;
            case R.id.rl_invite_code:
                if (mMyHomepageBean == null) {
                    return;
                }
                if (UserLevelUtils.getLevels(mMyHomepageBean.userLevel)[1]) {
                    MyInviteCodeActivity.startMyInviteCodeActivity(MinePrivateActivity.this);
                } else {
                    NonVipInviteCodeDialog nonVipInviteCodeDialog = new NonVipInviteCodeDialog(MinePrivateActivity.this);
                    nonVipInviteCodeDialog.show();
                    nonVipInviteCodeDialog.setOnConfirmListener(new NonVipInviteCodeDialog.OnConfirmListener() {
                        @Override
                        public void onConfirmClick() {
                            VipCenterActivity.startVipCenterActivity(MinePrivateActivity.this);
                        }
                    });
                }
                break;
            case R.id.rl_radio:
                MyRadioActivity.startActivity(MinePrivateActivity.this);
                break;
            case R.id.tv_upload:
            case R.id.ll_no_photo:
                selectPhoto();
                break;
            case R.id.tv_red_photo:
                RedAlbumActivity.startActivity(MinePrivateActivity.this);
                break;
            case R.id.rl_photo_privacy:
                if (mMyHomepageBean != null && mMyHomepageBean.photoAlbum != null) {
                    PhotoPrivacyDialog photoPrivacyDialog = new PhotoPrivacyDialog(MinePrivateActivity.this, mMyHomepageBean.photoAlbum.type);
                    photoPrivacyDialog.show();
                }
                break;
            case R.id.rl_black_list:
                BlackListActivity.startBlackListActivity(MinePrivateActivity.this);
                break;
            case R.id.rl_setting:
                SettingActivity.startSettingActivity(MinePrivateActivity.this);
                break;
            case R.id.rl_share:
                ShareDialog shareDialog = new ShareDialog(MinePrivateActivity.this);
                shareDialog.show();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getMyHomepage();
    }

    private void getMyHomepage() {
        PersonalInfoRequest.getInstance().getMyHomepage(MinePrivateActivity.this, new PersonalInfoRequest.PersonalInfoCallBack() {
            @Override
            public void onSuccess(MyHomepageBean myHomepageBean) {
                MyApplication.mMyHomepageBean = myHomepageBean;
                mMyHomepageBean = myHomepageBean;
                updateUserInfo(myHomepageBean);
            }

            @Override
            public void onFail(String error) {
                ToastUtils.showToast(error);
            }
        });
    }

    private void updateUserInfo(MyHomepageBean homepageBean) {
        if (homepageBean == null) {
            return;
        }
        mTvNickname.setText(homepageBean.nickname);
        AvatarHelper.getInstance().displayAvatar(coreManager.getSelf().getNickName(), coreManager.getSelf().getUserId(), mIvAvatar, false);
        if (!TextUtils.isEmpty(homepageBean.ageConstellation) && homepageBean.ageConstellation.contains(";")) {
            String[] arrAgeConstellation = homepageBean.ageConstellation.split(";");
            mTvAge.setText(String.format("%s岁", arrAgeConstellation[0]));
            mTvConstellation.setText(arrAgeConstellation[1]);
        }
        if (TextUtils.isEmpty(homepageBean.position)) {
            mTvJob.setVisibility(View.GONE);
        } else {
            mTvJob.setVisibility(View.VISIBLE);
            mTvJob.setText(homepageBean.position);
        }

        setUserLevel(homepageBean.userLevel);

        if (homepageBean.photoAlbum != null && homepageBean.photoAlbum.photos != null && homepageBean.photoAlbum.photos.size() > 0) {
            mLlNoPhoto.setVisibility(View.GONE);
            mRvPhoto.setVisibility(View.VISIBLE);
            mPhotoAdapter.setPhotoNum(homepageBean.photoAlbum.photoNum);
            mPhotoAdapter.setNewData(homepageBean.photoAlbum.photos);
            if (1 == homepageBean.photoAlbum.type) {
                mTvPayUnlock.setText("申请访问");
            } else if (2 == homepageBean.photoAlbum.type) {
                mTvPayUnlock.setText(String.format("付费解锁(%d)红豆", homepageBean.photoAlbum.coin));
            } else {
                mTvPayUnlock.setText("公开");
            }
        } else {
            mLlNoPhoto.setVisibility(View.VISIBLE);
            mRvPhoto.setVisibility(View.GONE);
        }

    }

    private void setUserLevel(String userLevel) {
        boolean[] userLevels = UserLevelUtils.getLevels(userLevel);
        if (userLevels[0]) {//女性
            PreferenceUtils.saveSex(0);
            mTvRedPhoto.setVisibility(View.VISIBLE);
            mRlPhotoPrivacy.setVisibility(View.VISIBLE);
            mRlBadge.setVisibility(View.VISIBLE);
        } else {
            PreferenceUtils.saveSex(1);
            mTvRedPhoto.setVisibility(View.GONE);
            mRlPhotoPrivacy.setVisibility(View.GONE);
            mRlBadge.setVisibility(View.GONE);
        }
        if (userLevels[1]) {//VIP
            mTvVip.setText("已成为会员");
        } else {
            mTvVip.setText("升级会员尊享特权");
        }
        if (userLevels[2]) {//女神
            mTvGirl.setVisibility(View.VISIBLE);
            mTvTrue.setVisibility(View.GONE);
        } else {
            mTvGirl.setVisibility(View.GONE);
            mTvRedPhoto.setVisibility(View.GONE);
            if (userLevels[3]) {//真人
                mTvTrue.setVisibility(View.VISIBLE);
            } else {
                mTvTrue.setVisibility(View.GONE);
            }
        }
        //            if ('1' == arr[4]) {//徽章
//                helper.mIvDebutante.setVisibility(View.VISIBLE);
//            } else {
//                helper.mIvDebutante.setVisibility(View.GONE);
//            }
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
        PhotoPickerIntent intent = new PhotoPickerIntent(MinePrivateActivity.this);
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
        DialogHelper.showMessageProgressDialog(MinePrivateActivity.this, "图片压缩中");
        mPhotoList.clear();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < stringArrayListExtra.size(); i++) {
            // Luban只处理特定后缀的图片，不满足的不处理也不走回调，
            // 只能挑出来不压缩，
            // luban支持压缩.gif图，但是压缩之后的.gif图用glide加载与转换为gifDrawable都会出问题，所以,gif图不压缩了
            List<String> lubanSupportFormatList = Arrays.asList("jpg", "jpeg", "png", "webp");
            boolean support = false;
            for (int j = 0; j < lubanSupportFormatList.size(); j++) {
                if (stringArrayListExtra.get(i).endsWith(lubanSupportFormatList.get(j))) {
                    support = true;
                    break;
                }
            }
            if (!support) {
                list.add(stringArrayListExtra.get(i));
            }
        }

        if (list.size() > 0) {
            for (String s : list) {// 不压缩的部分，直接发送
                mPhotoList.add(s);
            }
        }

        // 移除掉不压缩的图片
        stringArrayListExtra.removeAll(mPhotoList);
        mPhotoList.addAll(stringArrayListExtra);

        initLuBanRxJava(mPhotoList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @SuppressLint("StaticFieldLeak")
    private class UploadPhoto extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
            boolean success = Result.defaultParser(MinePrivateActivity.this, recordResult, true);
            if (success) {
                if (recordResult.getSuccess() != recordResult.getTotal()) {
                    // 上传丢失了某些文件
                    return 2;
                }
                if (recordResult.getData() != null) {
                    UploadFileResult.Data data = recordResult.getData();
                    if (data.getImages() != null && data.getImages().size() > 0) {
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
            } else if (result == 2) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(MinePrivateActivity.this, getString(R.string.upload_failed));
            } else {
                uploadMyPhoto();
            }
        }
    }

    private void uploadMyPhoto() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(MinePrivateActivity.this).getSelfStatus().accessToken);
        params.put("photoType", "0");
        params.put("photoUrl", mPhotoUrlList);

        HttpUtils.post().url(CoreManager.getInstance(MinePrivateActivity.this).getConfig().RED_PHOTO_UPLOAD)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();

                        if (result.getResultCode() == 1) {
                            ToastUtils.showToast("上传成功");
                            getMyHomepage();
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(MinePrivateActivity.this);
                    }
                });
    }


    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onMessageEvent(UpdateUserInfo event) {
        if (event != null) {
            getMyHomepage();
        }
    }

    private void initLuBanRxJava(List<String> photos) {
        mDisposableLuBan = Flowable.just(photos)//注意，可以单个压缩，也可以list压缩
                .subscribeOn(Schedulers.io())
                .map(new Function<List<String>, List<File>>() {
                    @Override
                    public List<File> apply(@NonNull List<String> strings) throws Exception {
                        return Luban.with(MinePrivateActivity.this).load(photos).get();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<File>>() {
                    @Override
                    public void accept(List<File> files) throws Exception {
                        mPhotoList.clear();
                        for (File file : files) {
                            mPhotoList.add(file.getPath());
                        }
                        DialogHelper.dismissProgressDialog();
                        DialogHelper.showMessageProgressDialog(MinePrivateActivity.this, "图片上传中");
                        new UploadPhoto().execute();
                    }
                });
    }


}
