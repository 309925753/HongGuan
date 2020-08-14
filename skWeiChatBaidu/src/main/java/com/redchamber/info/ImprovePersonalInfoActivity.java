package com.redchamber.info;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.makeramen.roundedimageview.RoundedImageView;
import com.redchamber.api.GlobalConstants;
import com.redchamber.api.RequestCode;
import com.redchamber.bean.CheckPhotoBean;
import com.redchamber.bean.MyHomepageBean;
import com.redchamber.bean.RegisterInfoBean;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.login.InvitationCodeActivity;
import com.redchamber.request.UserLevelRequest;
import com.redchamber.util.GlideUtils;
import com.redchamber.util.UserLevelUtils;
import com.redchamber.view.DateShowDialog;
import com.redchamber.view.ExpectObjectDialog;
import com.redchamber.view.SelectHeightDialog;
import com.redchamber.view.SelectWeightDialog;
import com.sk.weichat.R;
import com.sk.weichat.bean.UploadFileResult;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.UploadService;
import com.sk.weichat.map.MapHelper;
import com.sk.weichat.ui.MainActivity;
import com.sk.weichat.ui.base.BaseLoginActivity;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.CameraUtil;
import com.sk.weichat.util.DateUtils;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 完善资料
 */
public class ImprovePersonalInfoActivity extends BaseLoginActivity implements View.OnClickListener {

    RoundedImageView mIvAvatar;
    EditText mEtNickname;
    TextView mTvCity;
    TextView mTvBirth;
    TextView mTvJob;
    TextView mTvProgram;
    TextView mTvExpect;
    TextView mTvHeight;
    TextView mTvWeight;
    EditText mEtIntro;
    Button mBtnNext;
    TextView mTvHint;

    private RegisterInfoBean mRegisterInfoBean;
    private Uri mNewPhotoUri;
    private File mCurrentFile;
    private boolean isSelectAvatar;

    public final static int TYPE_REGISTER_INFO = 0;
    public final static int TYPE_EDIT_INFO = 1;
    private int mPageType = 1;
    private ArrayList<String> mPhotoList = new ArrayList<>();//手机相册图片文件
    private String mPhotoUrlList = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_improve_personal_info);
        initView();
        MapHelper.setMapType(MapHelper.MapType.BAIDU);
    }

    public ImprovePersonalInfoActivity() {
        noLoginRequired();
    }

    protected void initView() {
        mIvAvatar = findViewById(R.id.iv_avatar);
        mEtNickname = findViewById(R.id.et_nickname);
        mTvCity = findViewById(R.id.tv_city);
        mTvBirth = findViewById(R.id.tv_birth);
        mTvJob = findViewById(R.id.tv_job);
        mTvProgram = findViewById(R.id.tv_program);
        mTvExpect = findViewById(R.id.tv_expect);
        mTvHeight = findViewById(R.id.tv_height);
        mTvWeight = findViewById(R.id.tv_weight);
        mEtIntro = findViewById(R.id.et_intro);
        mBtnNext = findViewById(R.id.btn_next);
        mTvHint = findViewById(R.id.tv_hint);
        findViewById(R.id.iv_back).setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mIvAvatar.setOnClickListener(this);
        mTvCity.setOnClickListener(this);
        mTvBirth.setOnClickListener(this);
        mTvJob.setOnClickListener(this);
        mTvProgram.setOnClickListener(this);
        mTvExpect.setOnClickListener(this);
        mTvHeight.setOnClickListener(this);
        mTvWeight.setOnClickListener(this);

        if (getIntent() != null) {
            mRegisterInfoBean = (RegisterInfoBean) getIntent().getSerializableExtra(GlobalConstants.KEY_REGISTER_INFO);
            mPageType = getIntent().getIntExtra(GlobalConstants.KEY_TYPE, TYPE_REGISTER_INFO);
        }
        if (mRegisterInfoBean == null) {
            mRegisterInfoBean = new RegisterInfoBean();
        }

        if (TYPE_EDIT_INFO == mPageType) {
            mBtnNext.setText("完成");
            mTvHint.setVisibility(View.GONE);
            getMyHomepage();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_avatar:
                selectPhoto();
                break;
            case R.id.tv_city:
                startActivityForResult(new Intent(this, ResidentCityActivity.class), RequestCode.REQUEST_CODE_SELECT_CITY);
                break;
            case R.id.tv_birth:
                showDatePicker();
                break;
            case R.id.tv_job:
                startActivityForResult(new Intent(this, JobSelectActivity.class), RequestCode.REQUEST_CODE_SELECT_JOB);
                break;
            case R.id.tv_program:
                DateShowDialog dateShowDialog = new DateShowDialog(this);
                dateShowDialog.show();
                dateShowDialog.setOnConfirmListener(new DateShowDialog.OnConfirmListener() {
                    @Override
                    public void onConfirmClick(String show) {
                        mTvProgram.setText(show);
                        mRegisterInfoBean.program = show;
                    }
                });
                break;
            case R.id.tv_expect:
                ExpectObjectDialog expectObjectDialog = new ExpectObjectDialog(this);
                expectObjectDialog.show();
                expectObjectDialog.setOnConfirmListener(new ExpectObjectDialog.OnConfirmListener() {
                    @Override
                    public void onConfirmClick(String object) {
                        mTvExpect.setText(object);
                        mRegisterInfoBean.expectFriend = object;
                    }
                });
                break;
            case R.id.tv_height:
                SelectHeightDialog selectHeightDialog = new SelectHeightDialog(this);
                selectHeightDialog.show();
                selectHeightDialog.setOnConfirmListener(new SelectHeightDialog.OnConfirmListener() {
                    @Override
                    public void onConfirmClick(String height) {
                        mTvHeight.setText(height);
                        mRegisterInfoBean.height = height;
                    }
                });
                break;
            case R.id.tv_weight:
                SelectWeightDialog selectWeightDialog = new SelectWeightDialog(this);
                selectWeightDialog.show();
                selectWeightDialog.setOnConfirmListener(new SelectWeightDialog.OnConfirmListener() {
                    @Override
                    public void onConfirmClick(String weight) {
                        mTvWeight.setText(weight);
                        mRegisterInfoBean.weight = weight;
                    }
                });
                break;
            case R.id.btn_next:
                if (!isSelectAvatar || mNewPhotoUri == null) {
                    ToastUtils.showToast(getString(R.string.must_select_avatar_can_register));
                    return;
                }
                String nickName = mEtNickname.getText().toString().trim();
                if (TextUtils.isEmpty(nickName)) {
                    ToastUtils.showToast("请填写昵称");
                    return;
                }
                if (TextUtils.isEmpty(mRegisterInfoBean.residentCity)) {
                    ToastUtils.showToast("请选择城市");
                    return;
                }
                if (TextUtils.isEmpty(mRegisterInfoBean.birthDay)) {
                    ToastUtils.showToast("请选择生日");
                    return;
                }
                if (TextUtils.isEmpty(mRegisterInfoBean.position)) {
                    ToastUtils.showToast("请选择职位");
                    return;
                }
                if (TextUtils.isEmpty(mRegisterInfoBean.program)) {
                    ToastUtils.showToast("请选择交友节目");
                    return;
                }
                if (TextUtils.isEmpty(mRegisterInfoBean.expectFriend)) {
                    ToastUtils.showToast("请选择期望对象");
                    return;
                }
                mRegisterInfoBean.nickname = nickName;
                mRegisterInfoBean.selfDesc = mEtIntro.getText().toString().trim();
                mRegisterInfoBean.headImage = mNewPhotoUri;
                if (TYPE_EDIT_INFO == mPageType) {
                    updateUserInfo();
                } else {
                    InvitationCodeActivity.startActivity(this, mRegisterInfoBean);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RequestCode.REQUEST_CODE_SELECT_CITY:
                if (data != null) {
                    String city = data.getStringExtra(GlobalConstants.KEY_CITY);
                    mTvCity.setText(city);
                    mRegisterInfoBean.residentCity = city;
                }
                break;
            case RequestCode.REQUEST_CODE_SELECT_JOB:
                if (data != null) {
                    String job = data.getStringExtra(GlobalConstants.KEY_JOB);
                    mTvJob.setText(job);
                    mRegisterInfoBean.position = job;
                }
                break;
            case RequestCode.REQUEST_CODE_CROP_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    isSelectAvatar = true;
                    if (mNewPhotoUri != null) {
                        mCurrentFile = new File(mNewPhotoUri.getPath());
                        mPhotoList.clear();
                        mPhotoList.add(mNewPhotoUri.getPath());
                        new UploadPhoto().execute();//先上传，再鉴黄
                    } else {
                        ToastUtil.showToast(this, R.string.c_crop_failed);
                    }
                }
                break;
            case RequestCode.REQUEST_CODE_PICK_CROP_PHOTO:
                // 选择一张图片,然后立即调用裁减
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Uri o = Uri.fromFile(new File(CameraUtil.parsePickImageResult(data)));
                        mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
                        CameraUtil.cropImage(this, o, mNewPhotoUri, RequestCode.REQUEST_CODE_CROP_PHOTO, 1, 1, 300, 300);
                    } else {
                        ToastUtil.showToast(this, R.string.c_photo_album_failed);
                    }
                }
                break;
        }
    }

    private void showDatePicker() {
        Calendar ca = Calendar.getInstance();
        int mYear = ca.get(Calendar.YEAR);
        int mMonth = ca.get(Calendar.MONTH);
        int mDay = ca.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(ImprovePersonalInfoActivity.this,
                (view, year, month, dayOfMonth) -> {
                    String birth = String.format(getString(R.string.birth_format), year, month + 1, dayOfMonth);
                    mTvBirth.setText(birth);
                    mRegisterInfoBean.birthDay = birth;
                },
                mYear, mMonth, mDay);
        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    public static void startImprovePersonalInfoActivity(Context context, RegisterInfoBean registerInfoBean) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, ImprovePersonalInfoActivity.class);
        intent.putExtra(GlobalConstants.KEY_REGISTER_INFO, (Serializable) registerInfoBean);
        context.startActivity(intent);
    }

    public static void startImprovePersonalInfoActivity(Context context, int type) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, ImprovePersonalInfoActivity.class);
        intent.putExtra(GlobalConstants.KEY_TYPE, type);
        context.startActivity(intent);
    }

    private void selectPhoto() {
        CameraUtil.pickImageSimple(this, RequestCode.REQUEST_CODE_PICK_CROP_PHOTO);
    }

    private void getMyHomepage() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_USER_INFO_MY_HOMEPAGE)
                .params(params)
                .build()
                .execute(new BaseCallback<MyHomepageBean>(MyHomepageBean.class) {

                    @Override
                    public void onResponse(ObjectResult<MyHomepageBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            updateUserInfo(result.getData());
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtils.showToast(e.getMessage());
                    }
                });
    }

    private void updateUserInfo(MyHomepageBean homepageBean) {
        if (homepageBean == null) {
            return;
        }
        GlideUtils.loadAvatar(this, coreManager.getSelf().getUserId(), mIvAvatar);
        mEtNickname.setText(homepageBean.nickname);
        mTvCity.setText(homepageBean.residentCity);
        mTvBirth.setText(DateUtils.birthDay2TimeStamp(homepageBean.birthday));
        mTvJob.setText(homepageBean.position);
        mTvProgram.setText(homepageBean.program);
        mTvExpect.setText(homepageBean.expectFriend);
        if (TextUtils.isEmpty(homepageBean.height)) {
            mTvHeight.setText("未填写");
        } else {
            mTvHeight.setText(homepageBean.height + "CM");
        }
        if (TextUtils.isEmpty(homepageBean.weight)) {
            mTvWeight.setText("未填写");
        } else {
            mTvWeight.setText(homepageBean.weight + "KG");
        }
        mEtIntro.setText(homepageBean.description);

        isSelectAvatar = true;
        mNewPhotoUri = Uri.fromFile(new File(""));
        mRegisterInfoBean.residentCity = homepageBean.residentCity;
        mRegisterInfoBean.birthDay = DateUtils.birthDay2TimeStamp(homepageBean.birthday);
        mRegisterInfoBean.position = homepageBean.position;
        mRegisterInfoBean.program = homepageBean.program;
        mRegisterInfoBean.expectFriend = homepageBean.expectFriend;
        mRegisterInfoBean.weight = homepageBean.weight;
        mRegisterInfoBean.height = homepageBean.height;
    }

    private void updateUserInfo() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);
        params.put("nickname", mRegisterInfoBean.nickname);
        params.put("residentCity", mRegisterInfoBean.residentCity);
        params.put("birthday", String.valueOf(DateUtils.birthDay2TimeStamp(mRegisterInfoBean.birthDay)));
        params.put("position", mRegisterInfoBean.position);
        params.put("program", mRegisterInfoBean.program);
        params.put("expectFriend", mRegisterInfoBean.expectFriend);
        params.put("height", mRegisterInfoBean.height);
        params.put("weight", mRegisterInfoBean.weight);
        params.put("description", mRegisterInfoBean.selfDesc);

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_UPDATE_USER_INFO)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            ToastUtils.showToast("修改成功");
                            finish();
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtils.showToast(e.getMessage());
                    }
                });
    }

    private void uploadAvatar(File file) {
        if (!file.exists()) {
            // 文件不存在
            return;
        }
        // 显示正在上传的ProgressDialog
        DialogHelper.showMessageProgressDialog(this, getString(R.string.upload_avataring));
        RequestParams params = new RequestParams();
        String loginUserId = coreManager.getSelf().getUserId();
        params.put("userId", loginUserId);
        try {
            params.put("file1", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(coreManager.getConfig().AVATAR_UPLOAD_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                boolean success = false;
                if (arg0 == 200) {
                    Result result = null;
                    try {
                        result = JSON.parseObject(new String(arg2), Result.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (result != null && result.getResultCode() == Result.CODE_SUCCESS) {
                        success = true;
                    }
                }

                DialogHelper.dismissProgressDialog();
                if (success) {
                    ToastUtil.showToast(ImprovePersonalInfoActivity.this, R.string.upload_avatar_success);
                } else {
                    ToastUtil.showToast(ImprovePersonalInfoActivity.this, R.string.upload_avatar_failed);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(ImprovePersonalInfoActivity.this, R.string.upload_avatar_failed);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class UploadPhoto extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Map<String, String> mapParams = new HashMap<>();
            mapParams.put("validTime", "-1");// 文件有效期

            String result = new UploadService().uploadFile(coreManager.getConfig().UPLOAD_URL, mapParams, mPhotoList);
            if (TextUtils.isEmpty(result)) {
                return 2;
            }

            UploadFileResult recordResult = JSON.parseObject(result, UploadFileResult.class);
            boolean success = Result.defaultParser(ImprovePersonalInfoActivity.this, recordResult, true);
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
                ToastUtil.showToast(ImprovePersonalInfoActivity.this, getString(R.string.upload_failed));
            } else {
                checkPhoto();
            }
        }
    }

    //鉴黄
    private void checkPhoto() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);
        params.put("photoUrl", mPhotoUrlList);

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_CHECK_PHOTO)
                .params(params)
                .build()
                .execute(new BaseCallback<CheckPhotoBean>(CheckPhotoBean.class) {

                    @Override
                    public void onResponse(ObjectResult<CheckPhotoBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            if (1 != result.getData().status) {
                                ToastUtils.showToast("请上传合法图片");
                                isSelectAvatar = false;
                            } else {
                                AvatarHelper.getInstance().displayUrl(mNewPhotoUri.toString(), mIvAvatar);
                                if (TYPE_EDIT_INFO == mPageType) {
                                    uploadAvatar(mCurrentFile);
                                }
                            }
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtils.showToast(e.getMessage());
                    }
                });


    }

}
