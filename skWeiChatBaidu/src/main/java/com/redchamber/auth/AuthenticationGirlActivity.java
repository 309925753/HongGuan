package com.redchamber.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.redchamber.api.RequestCode;
import com.redchamber.event.UpdateAuthentionEvent;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.util.RedAvatarUtils;
import com.redchamber.view.ChooseVide;
import com.sk.weichat.R;
import com.sk.weichat.bean.UploadFileResult;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.helper.UploadService;
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
import okhttp3.Call;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * 女神认证
 */
public class AuthenticationGirlActivity extends BaseActivity {

    @BindView(R.id.tv_tip)
    TextView mTvTip;
    @BindView(R.id.ll_authentication)
    LinearLayout llAuthentication;
    @BindView(R.id.iv_add_mage)
    ImageView ivAddMage;
    @BindView(R.id.img_delete_pic)
    ImageView imgDeletePic;
    // 拍照和图库，获得图片的Uri
    private Uri mNewPhotoUri;
    private ChooseVide mChoosePop;
    private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;  // 拍照
    private static final int REQUEST_CODE_PICK_PHOTO = 2;     // 图库
    private static final int REQUEST_CODE_SELECT_LOCATE = 3;  // 位置
    private ArrayList<String> mPhotoList = new ArrayList<>();//手机相册图片文件
    private String mPhotoUrlList = "";
    private String mImageData;


    public int isSeLectPic = 0;

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
                    imgDeletePic.setVisibility(View.VISIBLE);
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
        PhotoPickerIntent intent = new PhotoPickerIntent(AuthenticationGirlActivity.this);
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
            //    mPhotoAdapter.addData(new PhotoBean(stringArrayListExtra.get(i)));
        }
        new UploadPhoto().execute();
        ImageLoadHelper.showImageWithSizeError(
                mContext,
                stringArrayListExtra.get(0),
                R.drawable.pic_error,
                600, 600,
                ivAddMage
        );
    }



    private class UploadPhoto extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DialogHelper.showDefaulteMessageProgressDialog(AuthenticationGirlActivity.this);
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
            boolean success = Result.defaultParser(AuthenticationGirlActivity.this, recordResult, true);
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
                ToastUtil.showToast(AuthenticationGirlActivity.this, getString(R.string.upload_failed));
            } else {
                //  uploadMyPhoto();
                DialogHelper.dismissProgressDialog();



            }
        }
    }


    @Override
    protected int setLayout() {
        return R.layout.red_activity_authentication_girl;
    }

    @Override
    protected void initView() {
        mTvTip.setText(Html.fromHtml(getString(R.string.auth_tip_a)));


        //ChooseVide
    }

    @OnClick({R.id.tv_back, R.id.tv_submit, R.id.iv_add_mage,R.id.img_delete_pic})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.tv_submit:
                if (TextUtils.isEmpty(mPhotoUrlList)) {
                    ToastUtils.showToast("提示上传图片");
                    return;
                }
                postCerifcalion(mPhotoUrlList);

                break;
            case R.id.iv_add_mage:
                //弹框
                selectPhoto();
                break;
            case R.id.img_delete_pic:
                selectPhoto();
                break;
        }
    }

    private String mVideoFilePath;
    private String mThumbPath;
    private Bitmap mThumbBmp;
    private long mTimeLen;

    public static void startAuthenticationGirlActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, AuthenticationGirlActivity.class));
    }

    // 多张图片压缩 相册
    private void album(ArrayList<String> stringArrayListExtra, boolean isOriginal) {
        if (isOriginal) {// 原图发送，不压缩
            Log.e("zq", "原图上传，不压缩，选择原文件路径");
            for (int i = 0; i < stringArrayListExtra.size(); i++) {
                mPhotoList.add(stringArrayListExtra.get(i));
            }
            return;
        }

        List<String> list = new ArrayList<>();
        for (int i = 0; i < stringArrayListExtra.size(); i++) {
            // Luban只处理特定后缀的图片，不满足的不处理也不走回调，
            // 只能挑出来不压缩，
            // todo luban支持压缩.gif图，但是压缩之后的.gif图用glide加载与转换为gifDrawable都会出问题，所以,gif图不压缩了
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

        Luban.with(this)
                .load(stringArrayListExtra)
                .ignoreBy(100)// 原图小于100kb 不压缩
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(File file) {
                        mPhotoList.add(file.getPath());

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }).launch();// 启动压缩
    }

    View.OnClickListener choose = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.select_video:
                    mChoosePop.dismiss();
                    // takePhoto();
                    mChoosePop.dismiss();

                    break;

                case R.id.select_pic:
                    mChoosePop.dismiss();
                    break;
                default:
                    break;
            }
        }
    };


    public void showDialog() {
        mChoosePop = new ChooseVide(AuthenticationGirlActivity.this, choose, isSeLectPic);
        mChoosePop.showAtLocation(llAuthentication, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    /**
     * 百度返回图片数据并请求后台
     *
     * @param base64
     */
    private void postCerifcalion(String base64) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("certificateInfo", base64);
        params.put("type", String.valueOf(1));
        HttpUtils.post().url(coreManager.getConfig().RED_MY_COMMIT_CERTIFICATE)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        DialogHelper.dismissProgressDialog();
                        // 人脸在线活体检测
                        if (Result.checkSuccess(AuthenticationGirlActivity.this, result)) {
                            EventBus.getDefault().post(new UpdateAuthentionEvent(""));
                            ToastUtil.showLongToast(AuthenticationGirlActivity.this, "认证成功");
                            finish();
                        } else {
                            ToastUtil.showLongToast(AuthenticationGirlActivity.this, "认证失败请重新认证");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(AuthenticationGirlActivity.this);
                    }
                });
    }


}
