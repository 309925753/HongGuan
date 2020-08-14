package com.sk.weichat.ui.share;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.Area;
import com.sk.weichat.bean.SKShareBean;
import com.sk.weichat.bean.UploadFileResult;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.MainActivity;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.circle.range.AtSeeCircleActivity;
import com.sk.weichat.ui.circle.range.SeeCircleActivity;
import com.sk.weichat.ui.circle.util.SendTextFilter;
import com.sk.weichat.ui.map.MapPickerActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.ui.tool.WebViewActivity;
import com.sk.weichat.util.DeviceInfoUtil;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.LoadFrame;
import com.sk.weichat.view.TipDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static com.sk.weichat.ui.tool.WebViewActivity.EXTRA_URL;

/**
 * 分享链接 至 生活圈
 */
public class ShareLifeCircleActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_SELECT_LOCATE = 3;  // 位置
    private static final int REQUEST_CODE_SELECT_TYPE = 4;    // 谁可以看
    private static final int REQUEST_CODE_SELECT_REMIND = 5;  // 提醒谁看
    private EditText mTextEdit;
    // 所在位置
    private TextView mTVLocation;
    // 谁可以看
    private TextView mTVSee;
    // 提醒谁看
    private TextView mTVAt;
    // 发布
    private Button mReleaseBtn;
    // 部分可见 || 不给谁看 有值 用于恢复谁可以看的界面
    private String str1;
    private String str2;
    private String str3;
    // 默认为公开
    private int visible = 1;
    // 谁可以看 || 不给谁看
    private String lookPeople;
    // 提醒谁看
    private String atlookPeople;
    // 默认不发位置
    private double latitude;
    private double longitude;
    private String address;

    // SHARE ITEM
    private LinearLayout mShareLl;
    private ImageView mShareIv;
    private TextView mShareTv;
    private LinearLayout mShareImageLl;
    private ImageView mShareImageIv;

    private LoadFrame mLoadFrame;

    private String mShareContent;
    private SKShareBean mSKShareBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_shuoshuo);

        mShareContent = getIntent().getStringExtra(ShareConstant.EXTRA_SHARE_CONTENT);
        Log.e("zq", mShareContent);
        mSKShareBean = JSON.parseObject(mShareContent, SKShareBean.class);
        if (mSKShareBean == null) {
            finish();
            return;
        }
        initActionBar();
        initView();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.public_a_file));
    }

    private void initView() {
        mTextEdit = (EditText) findViewById(R.id.text_edit);
        mTextEdit.setHint(getString(R.string.add_msg_mind));
        // 所在位置
        mTVLocation = (TextView) findViewById(R.id.tv_location);
        // 谁可以看
        mTVSee = (TextView) findViewById(R.id.tv_see);
        // 提醒谁看
        mTVAt = (TextView) findViewById(R.id.tv_at);

        mShareLl = findViewById(R.id.link_ll);
        mShareIv = findViewById(R.id.link_iv);
        mShareTv = findViewById(R.id.link_text_tv);
        mShareImageLl = findViewById(R.id.image_ll);
        mShareImageIv = findViewById(R.id.image_iv);
        if (mSKShareBean.getShareType() == 0) {
            mShareLl.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(mSKShareBean.getAppIcon()) && TextUtils.isEmpty(mSKShareBean.getImageUrl())) {
                mShareIv.setImageResource(R.drawable.browser);
            } else if (TextUtils.isEmpty(mSKShareBean.getImageUrl())) {
                AvatarHelper.getInstance().displayUrl(mSKShareBean.getAppIcon(), mShareIv);
            } else {
                AvatarHelper.getInstance().displayUrl(mSKShareBean.getImageUrl(), mShareIv);
            }
            mShareTv.setText(mSKShareBean.getTitle());
        } else if (mSKShareBean.getShareType() == 1) {
            mTextEdit.setText(mSKShareBean.getTitle());
        } else if (mSKShareBean.getShareType() == 2) {
            mShareImageLl.setVisibility(View.VISIBLE);
            AvatarHelper.getInstance().displayUrl(mSKShareBean.getImageUrl(), mShareImageIv);
        } else {
            ToastUtil.showToast(mContext, getString(R.string.tip_share_type_not_supported));
            finish();
            return;
        }

        mReleaseBtn = (Button) findViewById(R.id.release_btn);
        ButtonColorChange.colorChange(this, mReleaseBtn);
    }

    private void initEvent() {
        findViewById(R.id.rl_location).setOnClickListener(this);
        findViewById(R.id.rl_see).setOnClickListener(this);
        findViewById(R.id.rl_at).setOnClickListener(this);

        mShareLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShareLifeCircleActivity.this, WebViewActivity.class);
                intent.putExtra(EXTRA_URL, mSKShareBean.getUrl());
                mContext.startActivity(intent);
            }
        });

        mReleaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_location:
                // 所在位置
                Intent intent1 = new Intent(this, MapPickerActivity.class);
                startActivityForResult(intent1, REQUEST_CODE_SELECT_LOCATE);
                break;
            case R.id.rl_see:
                // 谁可以看
                Intent intent2 = new Intent(this, SeeCircleActivity.class);
                intent2.putExtra("THIS_CIRCLE_TYPE", visible - 1);
                intent2.putExtra("THIS_CIRCLE_PERSON_RECOVER1", str1);
                intent2.putExtra("THIS_CIRCLE_PERSON_RECOVER2", str2);
                intent2.putExtra("THIS_CIRCLE_PERSON_RECOVER3", str3);
                startActivityForResult(intent2, REQUEST_CODE_SELECT_TYPE);
                break;
            case R.id.rl_at:
                // 提醒谁看
                if (visible == 2) {
                    ToastUtil.showToast(ShareLifeCircleActivity.this, R.string.tip_private_cannot_use_this);
                } else {
                    Intent intent3 = new Intent(this, AtSeeCircleActivity.class);
                    intent3.putExtra("REMIND_TYPE", visible);
                    intent3.putExtra("REMIND_PERSON", lookPeople);
                    intent3.putExtra("REMIND_SELECT_PERSON", atlookPeople);
                    startActivityForResult(intent3, REQUEST_CODE_SELECT_REMIND);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_LOCATE) {
            // 选择位置返回
            latitude = data.getDoubleExtra(AppConstant.EXTRA_LATITUDE, 0);
            longitude = data.getDoubleExtra(AppConstant.EXTRA_LONGITUDE, 0);
            address = data.getStringExtra(AppConstant.EXTRA_ADDRESS);
            if (latitude != 0 && longitude != 0 && !TextUtils.isEmpty(address)) {
                Log.e("zq", "纬度:" + latitude + "   经度：" + longitude + "   位置：" + address);
                mTVLocation.setText(address);
            } else {
                ToastUtil.showToast(mContext, getString(R.string.loc_startlocnotice));
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_TYPE) {
            // 谁可以看返回
            int mOldVisible = visible;
            visible = data.getIntExtra("THIS_CIRCLE_TYPE", 1);
            // 当前可见范围与上一次不一致时，情况提醒谁看列表
            if (mOldVisible != visible
                    || visible == 3 || visible == 4) {
                // 清空提醒谁看列表 3/4 为可选范围，可能和已经选择好的提醒谁看冲突
                atlookPeople = "";
                mTVAt.setText("");
            }
            if (visible == 1) {
                mTVSee.setText(R.string.publics);
            } else if (visible == 2) {
                mTVSee.setText(R.string.privates);
                if (!TextUtils.isEmpty(atlookPeople)) {
                    final TipDialog tipDialog = new TipDialog(this);
                    tipDialog.setmConfirmOnClickListener(getString(R.string.tip_private_cannot_notify), new TipDialog.ConfirmOnClickListener() {
                        @Override
                        public void confirm() {
                            tipDialog.dismiss();
                        }
                    });
                    tipDialog.show();
                }
            } else if (visible == 3) {
                lookPeople = data.getStringExtra("THIS_CIRCLE_PERSON");
                String looKenName = data.getStringExtra("THIS_CIRCLE_PERSON_NAME");
                mTVSee.setText(looKenName);
            } else if (visible == 4) {
                lookPeople = data.getStringExtra("THIS_CIRCLE_PERSON");
                String lookName = data.getStringExtra("THIS_CIRCLE_PERSON_NAME");
                mTVSee.setText(getString(R.string.not_allow, lookName));
            }
            str1 = data.getStringExtra("THIS_CIRCLE_PERSON_RECOVER1");
            str2 = data.getStringExtra("THIS_CIRCLE_PERSON_RECOVER2");
            str3 = data.getStringExtra("THIS_CIRCLE_PERSON_RECOVER3");
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_REMIND) {
            // 提醒谁看返回
            atlookPeople = data.getStringExtra("THIS_CIRCLE_REMIND_PERSON");
            String atLookPeopleName = data.getStringExtra("THIS_CIRCLE_REMIND_PERSON_NAME");
            mTVAt.setText(atLookPeopleName);
        }
    }

    public void share() {
        mLoadFrame = new LoadFrame(ShareLifeCircleActivity.this);
        mLoadFrame.setSomething(getString(R.string.back_app, mSKShareBean.getAppName()), new LoadFrame.OnLoadFrameClickListener() {
            @Override
            public void cancelClick() {
                ShareBroadCast.broadcastFinishActivity(ShareLifeCircleActivity.this);
            }

            @Override
            public void confirmClick() {
                ShareBroadCast.broadcastFinishActivity(ShareLifeCircleActivity.this);
                startActivity(new Intent(ShareLifeCircleActivity.this, MainActivity.class));
                finish();
            }
        });
        mLoadFrame.show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        // 消息类型：1=文字消息；2=图文消息；3=语音消息；4=视频消息 5=文件消息 6=分享消息；
        if (mSKShareBean.getShareType() == 0) {
            params.put("type", "6");
            params.put("sdkIcon", mSKShareBean.getImageUrl());// 参数名为sdkIcon，实际传的为链接内的图片url
            params.put("sdkTitle", mSKShareBean.getTitle());
            params.put("sdkUrl", mSKShareBean.getUrl());
        } else if (mSKShareBean.getShareType() == 1) {
            if (TextUtils.isEmpty((mTextEdit.getText().toString()))) {
                ToastUtil.showToast(mContext, getString(R.string.name_connot_null));
                mLoadFrame.dismiss();
                return;
            }
            params.put("type", "1");
        } else if (mSKShareBean.getShareType() == 2) {
            UploadFileResult.Data data = new UploadFileResult.Data();
            UploadFileResult.Sources sources = new UploadFileResult.Sources();
            sources.setOriginalUrl(mSKShareBean.getImageUrl());
            List<UploadFileResult.Sources> sourcesList = new ArrayList<>();
            sourcesList.add(sources);
            data.setImages(sourcesList);
            params.put("type", "2");
            params.put("images", JSON.toJSONString(data.getImages(), UploadFileResult.sImagesFilter));
        }
        // 消息标记：1：求职消息；2：招聘消息；3：普通消息；
        params.put("flag", "3");

        // 消息隐私范围：1=公开；2=私密；3=部分选中好友可见；4=不给谁看
        params.put("visible", String.valueOf(visible));
        if (visible == 3) {
            // 谁可以看
            params.put("userLook", lookPeople);
        } else if (visible == 4) {
            // 不给谁看
            params.put("userNotLook", lookPeople);
        }
        // 提醒谁看
        if (!TextUtils.isEmpty(atlookPeople)) {
            params.put("userRemindLook", atlookPeople);
        }

        // 消息内容
        params.put("text", SendTextFilter.filter(mTextEdit.getText().toString()));

        /**
         * 所在位置
         */
        if (!TextUtils.isEmpty(address)) {
            // 纬度
            params.put("latitude", String.valueOf(latitude));
            // 经度
            params.put("longitude", String.valueOf(longitude));
            // 位置
            params.put("location", address);
        }

        // 必传，之前删除该字段，发布说说，服务器返回接口内部异常
        Area area = Area.getDefaultCity();
        if (area != null) {
            params.put("cityId", String.valueOf(area.getId()));// 城市Id
        } else {
            params.put("cityId", "0");
        }

        /**
         * 附加信息
         */
        // 手机型号
        params.put("model", DeviceInfoUtil.getModel());
        // 手机操作系统版本号
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        if (!TextUtils.isEmpty(DeviceInfoUtil.getDeviceId(mContext))) {
            // 设备序列号
            params.put("serialNumber", DeviceInfoUtil.getDeviceId(mContext));
        }

        HttpUtils.post().url(coreManager.getConfig().MSG_ADD_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            mLoadFrame.change();
                        } else {
                            mLoadFrame.dismiss();
                            Toast.makeText(ShareLifeCircleActivity.this, getString(R.string.share_failed), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        mLoadFrame.dismiss();
                        ToastUtil.showErrorNet(ShareLifeCircleActivity.this);
                    }
                });
    }
}
