package com.redchamber.release;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.alibaba.fastjson.JSON;
import com.redchamber.api.GlobalConstants;
import com.redchamber.api.RequestCode;
import com.redchamber.bean.QueryFreeAuthBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.map.MapAddressListActivity;
import com.redchamber.view.ChoosePop;
import com.redchamber.view.DateTimeObjectDialog;
import com.redchamber.view.ExpectObjectDialog;
import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.UploadFileResult;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.helper.UploadService;
import com.sk.weichat.ui.account.LoginActivity;
import com.sk.weichat.util.DateUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.video.EasyCameraActivity;
import com.sk.weichat.video.MessageEventGpu;
import com.sk.weichat.view.SquareCenterFrameLayout;
import com.sk.weichat.view.cjt2325.cameralibrary.util.LogUtil;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * 公共 发布节目
 */
public class ReleaseProgramActivity extends BaseActivity {

    @BindView(R.id.tv_theme)
    TextView mTvTheme;
    @BindView(R.id.tv_address)
    TextView mTvAddress;
    @BindView(R.id.tv_time)
    TextView mTvTime;
    @BindView(R.id.tv_expect)
    TextView mTvExpect;
    @BindView(R.id.rcv_img)
    RecyclerView rcvImg;
    @BindView(R.id.cb_comment)
    CheckBox cbComment;
    @BindView(R.id.iv_add_mage)
    ImageView ivAddMage;
    @BindView(R.id.rl_main)
    LinearLayout rlMain;
    @BindView(R.id.tv_times)
    TextView tvTimes;
    @BindView(R.id.tv_free_times)
    TextView tvFreeTimes;

    private ArrayList<String> mPhotoList;
    // 拍照和图库，获得图片的Uri
    private Uri mNewPhotoUri;
    PostArticleImgAdapter postArticleImgAdapter;
    private ChoosePop mChoosePop;
    private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;  // 拍照
    private static final int REQUEST_CODE_PICK_PHOTO = 2;     // 图库
    private static final int REQUEST_CODE_SELECT_LOCATE = 3;  // 位置

    public int isSeLectPic = 0;
    private String mImageData = "";
    private double latitude;
    private double longitude;
    private String address;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAPTURE_PHOTO) {
            // 拍照返回 Todo 已更换拍照方式
            if (resultCode == Activity.RESULT_OK) {
                if (mNewPhotoUri != null) {
                    photograph(new File(mNewPhotoUri.getPath()));
                } else {
                    ToastUtil.showToast(this, R.string.c_take_picture_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_PICK_PHOTO) {
            // 选择图片返回
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    boolean isOriginal = data.getBooleanExtra(PhotoPickerActivity.EXTRA_RESULT_ORIGINAL, false);
                    album(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT), isOriginal);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        } else if (requestCode == RequestCode.REQUEST_CODE_SELECT_PROGRAM_ADDRESS) {
            if (data == null) {
                return;
            }
            if (!TextUtils.isEmpty(data.getStringExtra(GlobalConstants.KEY_ADDRESS))) {
                String address = data.getStringExtra(GlobalConstants.KEY_ADDRESS);
                mTvAddress.setText(address);
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_LOCATE) {

            if (TextUtils.isEmpty(data.getStringExtra(AppConstant.EXTRA_ADDRESS))) {
                return;
            }
            // 选择位置返回
            latitude = data.getDoubleExtra(AppConstant.EXTRA_LATITUDE, 0);
            longitude = data.getDoubleExtra(AppConstant.EXTRA_LONGITUDE, 0);
            address = data.getStringExtra(AppConstant.EXTRA_ADDRESS);
            if (latitude != 0 && longitude != 0 && !TextUtils.isEmpty(address)) {
                Log.e("zq", "纬度:" + latitude + "   经度：" + longitude + "   位置：" + address);
                mTvAddress.setText(address);
            } else {
                ToastUtil.showToast(mContext, getString(R.string.loc_startlocnotice));
            }
        }

    }

    @Override
    protected int setLayout() {
        return R.layout.red_activity_release_program;
    }

    @Override
    protected void initView() {
        if (getIntent() != null) {
            mTvTheme.setText(getIntent().getStringExtra(GlobalConstants.KEY_THEME));
        }
        mPhotoList = new ArrayList<>();
        postArticleImgAdapter = new PostArticleImgAdapter(this, mPhotoList);
        initRcv();
        EventBus.getDefault().register(this);

        queryFreeAuthTimes();
        cbComment.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    discussFlag="0";
                    LogUtil.e("discussFlag= " +discussFlag);
                } else {
                    discussFlag="1";
                    LogUtil.e("discussFlag= " +discussFlag);
                }
            }
        });
    }

    /**
     * 获取免费权限的次数
     */
    private void queryFreeAuthTimes() {
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
                            tvFreeTimes.setText("今天还可免费发布" + result.getData().getFreeTimes() + "条");

                        } else {
                            LogUtil.e(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(ReleaseProgramActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initRcv() {
        rcvImg.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        rcvImg.setAdapter(postArticleImgAdapter);

    }

    @OnClick({R.id.iv_back, R.id.tv_address, R.id.tv_expect, R.id.tv_time, R.id.tv_release, R.id.iv_add_mage, R.id.tv_times})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_address:
             //  SelectProgramAddressActivity.startActivityForResult(this, RequestCode.REQUEST_CODE_SELECT_PROGRAM_ADDRESS);
                Intent intent1 = new Intent(this, MapAddressListActivity.class);
                startActivityForResult(intent1, REQUEST_CODE_SELECT_LOCATE);
                break;
            case R.id.tv_expect:
                ExpectObjectDialog expectObjectDialog = new ExpectObjectDialog(this);
                expectObjectDialog.show();
                expectObjectDialog.setOnConfirmListener(new ExpectObjectDialog.OnConfirmListener() {
                    @Override
                    public void onConfirmClick(String object) {
                        mTvExpect.setText(object);
                    }
                });
                break;
            case R.id.tv_times:
                DateTimeObjectDialog dateTimeObjectDialog = new DateTimeObjectDialog(this);
                dateTimeObjectDialog.show();
                dateTimeObjectDialog.setOnConfirmListener(new DateTimeObjectDialog.OnConfirmListener() {
                    @Override
                    public void onConfirmClick(String object) {
                        tvTimes.setText(object);
                    }
                });
                break;
            case R.id.tv_time:
                showDatePicker();
                break;
            case R.id.iv_add_mage:
                /* showDialog();*/
                selectPhoto();
                break;
            case R.id.tv_release:
                if (mPhotoList.size() > 0) {
                    // 发布图片+文字
                    new UploadPhoto().execute();
                } else {
                    sendShuoshuo();
                }
                break;
        }
    }

    public static void startActivity(Context context, String theme) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, ReleaseProgramActivity.class);
        intent.putExtra(GlobalConstants.KEY_THEME, theme);
        context.startActivity(intent);
    }

    private void showDatePicker() {
        Calendar ca = Calendar.getInstance();
        int mYear = ca.get(Calendar.YEAR);
        int mMonth = ca.get(Calendar.MONTH);
        int mDay = ca.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(ReleaseProgramActivity.this,
                (view, year, month, dayOfMonth) -> {
                    String birth = String.format(getString(R.string.birth_format), year, month + 1, dayOfMonth);
                    mTvTime.setText(birth);
                },
                mYear, mMonth, mDay);
        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setMinDate(System.currentTimeMillis() + 86400000L);//明天
        datePickerDialog.show();
    }

    //发表图文
    private class UploadPhoto extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DialogHelper.showDefaulteMessageProgressDialog(ReleaseProgramActivity.this);
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
            boolean success = Result.defaultParser(ReleaseProgramActivity.this, recordResult, true);
            if (success) {
                if (recordResult.getSuccess() != recordResult.getTotal()) {
                    // 上传丢失了某些文件
                    return 2;
                }
                if (recordResult.getData() != null) {
                    UploadFileResult.Data data = recordResult.getData();
                    if (data.getImages() != null && data.getImages().size() > 0) {
                        if (data.getImages().size() == 1) {
                            mImageData = data.getImages().get(0).getOriginalUrl();
                        } else {
                            for (int i = 0; i < data.getImages().size(); i++) {
                                if (i == data.getImages().size() - 1) {
                                    mImageData += data.getImages().get(i).getOriginalUrl();
                                } else {
                                    mImageData += data.getImages().get(i).getOriginalUrl() + ";";
                                }
                            }
                        }


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
                startActivity(new Intent(ReleaseProgramActivity.this, LoginActivity.class));
            } else if (result == 2) {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(ReleaseProgramActivity.this, getString(R.string.upload_failed));
            } else {
                sendShuoshuo();
            }
        }
    }

    private String discussFlag = "1";

    // 发布一条说说
    public void sendShuoshuo() {
        double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("loc.lat", String.valueOf(latitude));
        params.put("loc.lng", String.valueOf(longitude));

        if (TextUtils.isEmpty(mTvAddress.getText().toString().trim())) {
            ToastUtil.showLongToast(ReleaseProgramActivity.this, "请选择地点");
            return;
        }
        params.put("placeName", mTvAddress.getText().toString().trim() == null ? "" : mTvAddress.getText().toString().trim());//地点

        if (TextUtils.isEmpty(mTvTheme.getText().toString().trim())) {
            ToastUtil.showLongToast(ReleaseProgramActivity.this, "请选择节目主题");
            return;
        }
        params.put("title", mTvTheme.getText().toString().trim() == null ? "" : mTvTheme.getText().toString().trim());//节目主题
        params.put("cityId", "1");//城市ID
        params.put("cityName", MyApplication.getInstance().getBdLocationHelper().getCityName());
        if (TextUtils.isEmpty(mTvExpect.getText().toString().trim())) {
            ToastUtil.showLongToast(ReleaseProgramActivity.this, "请选择期望对象");
            return;
        }
        params.put("expectFriend", mTvExpect.getText().toString().trim() == null ? "" : mTvExpect.getText().toString().trim());//期望对s象

        String time = DateUtils.data(mTvTime.getText().toString().trim() + "00时00分00秒");

        if (TextUtils.isEmpty(mTvTime.getText().toString().trim())) {
            ToastUtil.showLongToast(ReleaseProgramActivity.this, "请选择日期");
            return;
        }
        params.put("programDate", time);//日期

        if (TextUtils.isEmpty(tvTimes.getText().toString().trim())) {
            ToastUtil.showLongToast(ReleaseProgramActivity.this, "请选择时间");
            return;
        }
        params.put("programTime", tvTimes.getText().toString().trim() == null ? "" : tvTimes.getText().toString().trim());//时间
        if (!TextUtils.isEmpty(mImageData)) {
            // 图片
            params.put("images", mImageData);
        }
       /* String etContents = etContent.getText().toString().trim();
        if (TextUtils.isEmpty(etContents)) {
            ToastUtil.showLongToast(getApplicationContext(), "你输入的信息不能为空");
            return;
        } else {
            params.put("content", etContents); //动态内容
        }*/

        params.put("discussFlag", discussFlag);//是否可以评论 0禁止评论 1开启评论
        params.put("programType", "0");//类型 0节目 1动态

        HttpUtils.post().url(coreManager.getConfig().RED_PUB_PROGRAM)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                        /*    Intent intent = new Intent();
                            intent.putExtra(AppConstant.EXTRA_MSG_ID, result.getData());
                            setResult(RESULT_OK, intent);
                            EventBus.getDefault().post(new SendTextSucc());
*/
                            ToastUtil.showLongToast(ReleaseProgramActivity.this, "发布节目成功");
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(ReleaseProgramActivity.this);
                    }
                });
    }


    public static void startReleaseMomentActivity(Context context) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, ReleaseMomentActivity.class));
    }


    // 单张图片压缩 拍照
    private void photograph(final File file) {
        Log.e("zq", "压缩前图片路径:" + file.getPath() + "压缩前图片大小:" + file.length() / 1024 + "KB");
        // 拍照出来的图片Luban一定支持，
        Luban.with(this)
                .load(file)
                .ignoreBy(100)     // 原图小于100kb 不压缩
                // .putGear(2)     // 设定压缩档次，默认三挡
                // .setTargetDir() // 指定压缩后的图片路径
                .setCompressListener(new OnCompressListener() { //设置回调
                    @Override
                    public void onStart() {
                        Log.e("zq", "开始压缩");
                    }

                    @Override
                    public void onSuccess(File file) {
                        Log.e("zq", "压缩成功，压缩后图片位置:" + file.getPath() + "压缩后图片大小:" + file.length() / 1024 + "KB");
                        mPhotoList.add(file.getPath());
                        postArticleImgAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("zq", "压缩失败,原图上传");
                        mPhotoList.add(file.getPath());
                        postArticleImgAdapter.notifyDataSetChanged();
                    }
                }).launch();// 启动压缩
    }

    // 多张图片压缩 相册
    private void album(ArrayList<String> stringArrayListExtra, boolean isOriginal) {
        if (isOriginal) {// 原图发送，不压缩
            Log.e("zq", "原图上传，不压缩，选择原文件路径");
            for (int i = 0; i < stringArrayListExtra.size(); i++) {
                mPhotoList.add(stringArrayListExtra.get(i));
                postArticleImgAdapter.notifyDataSetChanged();
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
                postArticleImgAdapter.notifyDataSetChanged();

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
                        postArticleImgAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }).launch();// 启动压缩
    }


    public void showDialog() {
        mChoosePop = new ChoosePop(ReleaseProgramActivity.this, choose, isSeLectPic);
        mChoosePop.showAtLocation(rlMain, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    /**
     * 相册
     * 可以多选的图片选择器
     */
    private void selectPhoto() {
        ArrayList<String> imagePaths = new ArrayList<>();
        PhotoPickerIntent intent = new PhotoPickerIntent(ReleaseProgramActivity.this);
        intent.setSelectModel(SelectModel.MULTI);
        // 是否显示拍照， 默认false
        intent.setShowCarema(false);
        // 最多选择照片数量，默认为9
        intent.setMaxTotal(6 - mPhotoList.size());
        // 已选中的照片地址， 用于回显选中状态
        intent.setSelectedPaths(imagePaths);
        // intent.setImageConfig(config);
        // 是否加载视频，默认true
        intent.setLoadVideo(false);
        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
    }

    // 拍照
    private void takePhoto() {
        Intent intent = new Intent(this, EasyCameraActivity.class);
        startActivity(intent);
    }

    View.OnClickListener choose = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.select_video:
                    mChoosePop.dismiss();
                    takePhoto();
                    break;

                case R.id.select_pic:
                    selectPhoto();
                    mChoosePop.dismiss();
                    break;


                default:
                    break;
            }
        }
    };


    class PostArticleImgAdapter extends RecyclerView.Adapter<PostArticleImgAdapter.MyViewHolder> {
        private final LayoutInflater mLayoutInflater;
        private final Context mContext;
        private List<String> mDatas;

        public PostArticleImgAdapter(Context context, List<String> datas) {
            this.mDatas = datas;
            this.mContext = context;
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            return new MyViewHolder(mLayoutInflater.inflate(R.layout.item_post_activity, parent, false));
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            holder.squareCenterFrameLayout.setVisibility(View.GONE);
            ImageLoadHelper.showImageWithSizeError(
                    mContext,
                    mDatas.get(position),
                    R.drawable.pic_error,
                    300, 300,
                    holder.imageView
            );

            holder.img_delete_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPhotoList.remove(position);
                    notifyDataSetChanged();


                }
            });
        }

        @Override
        public int getItemCount() {
            if (mDatas.size() >= 6) {
                return 6;
            }
            return mDatas.size();
        }


        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ImageView img_delete_pic;
            SquareCenterFrameLayout squareCenterFrameLayout;

            MyViewHolder(View itemView) {
                super(itemView);
                squareCenterFrameLayout = itemView.findViewById(R.id.add_sc);
                imageView = itemView.findViewById(R.id.sdv);
                img_delete_pic = itemView.findViewById(R.id.img_delete_pic);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventGpu message) {
        photograph(new File(message.event));
    }
}
