package com.redchamber.report;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.redchamber.api.GlobalConstants;
import com.redchamber.api.RequestCode;
import com.redchamber.bean.PhotoBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.report.adapter.ReportPhotoAdapter;
import com.redchamber.view.AnonymousReportSelectPhotoDialog;
import com.sk.weichat.R;
import com.sk.weichat.bean.UploadFileResult;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.helper.UploadService;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.ToastUtil;
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
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 匿名举报
 */
public class AnonymousReportActivity extends BaseActivity {

    @BindView(R.id.rg_reason)
    RadioGroup mRgReason;
    @BindView(R.id.rv_photo)
    RecyclerView mRvPhoto;
    @BindView(R.id.et_desc)
    EditText mEtDesc;

    private ReportPhotoAdapter mPhotoAdapter;

    private String mReason;
    private String mOthersUserId;
    private String mImgUrls;
    private ArrayList<String> mPhotoList = new ArrayList<>();//手机相册图片文件
    private List<PhotoBean> mPhotoBeanList = new ArrayList<>();

    @Override
    protected int setLayout() {
        return R.layout.red_activity_anonymous_report;
    }

    @Override
    protected void initView() {
        initRadioGroup();
        mPhotoAdapter = new ReportPhotoAdapter(mPhotoBeanList);
        mRvPhoto.setLayoutManager(new GridLayoutManager(this, 3));
        mRvPhoto.setAdapter(mPhotoAdapter);
        mPhotoAdapter.addData(new PhotoBean("add"));
        mPhotoAdapter.setAddPhotoListener(new ReportPhotoAdapter.addPhotoListener() {
            @Override
            public void addPhoto() {
                selectPhoto();
            }
        });

        if (getIntent() != null) {
            mOthersUserId = getIntent().getStringExtra(GlobalConstants.KEY_USER_ID);
        }
    }

    @OnClick({R.id.iv_back, R.id.tv_submit})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_submit:
                if (TextUtils.isEmpty(mReason)) {
                    ToastUtils.showToast("请选择举报原因");
                    return;
                }
                if (mPhotoList == null || mPhotoList.size() == 0) {
                    AnonymousReportSelectPhotoDialog dialog = new AnonymousReportSelectPhotoDialog(this);
                    dialog.show();
                    return;
                }

                new UploadPhoto().execute();

                break;
        }
    }

    public static void startActivity(Context context, String userId) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, AnonymousReportActivity.class);
        intent.putExtra(GlobalConstants.KEY_USER_ID, userId);
        context.startActivity(intent);
    }

    private void initRadioGroup() {
        mRgReason.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                RadioButton radioButton = findViewById(radioGroup.getCheckedRadioButtonId());
                mReason = radioButton.getText().toString();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.REQUEST_CODE_PICK_PHOTO) {
            // 选择图片返回
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
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
        PhotoPickerIntent intent = new PhotoPickerIntent(this);
        intent.setSelectModel(SelectModel.MULTI);
        // 是否显示拍照， 默认false
        intent.setShowCarema(false);
        // 最多选择照片数量
        intent.setMaxTotal(6 - mPhotoBeanList.size() + 1);
        // 已选中的照片地址， 用于回显选中状态
        intent.setSelectedPaths(imagePaths);
        // intent.setImageConfig(config);
        // 是否加载视频，默认true
        intent.setLoadVideo(false);
        startActivityForResult(intent, RequestCode.REQUEST_CODE_PICK_PHOTO);
    }

    // 多张图片压缩 相册
    private void album(ArrayList<String> stringArrayListExtra) {
        for (int i = 0; i < stringArrayListExtra.size(); i++) {
            mPhotoList.add(stringArrayListExtra.get(i));
            mPhotoAdapter.addData(0, new PhotoBean(stringArrayListExtra.get(i)));
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UploadPhoto extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DialogHelper.showDefaulteMessageProgressDialog(AnonymousReportActivity.this);
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
            boolean success = Result.defaultParser(AnonymousReportActivity.this, recordResult, true);
            if (success) {
                if (recordResult.getSuccess() != recordResult.getTotal()) {
                    // 上传丢失了某些文件
                    return 2;
                }
                if (recordResult.getData() != null) {
                    UploadFileResult.Data data = recordResult.getData();
                    if (data.getImages() != null && data.getImages().size() > 0) {
                        mImgUrls = "";
                        for (int i = 0; i < data.getImages().size(); i++) {
                            mImgUrls += data.getImages().get(i).getOriginalUrl() + ";";
                        }
                        mImgUrls = mImgUrls.substring(0, mImgUrls.length() - 1);
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
                ToastUtil.showToast(AnonymousReportActivity.this, getString(R.string.upload_failed));
            } else {
                sendReportRequest();
            }
        }
    }

    private void sendReportRequest() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);
        params.put("othersId", mOthersUserId);
        params.put("reason", mReason);
        params.put("imgUrl", mImgUrls);
        params.put("description", mEtDesc.getText().toString().trim());

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_USER_REPORT)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            ToastUtils.showToast("举报成功");
                            finish();
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(AnonymousReportActivity.this);
                    }
                });
    }

}
