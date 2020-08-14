package com.redchamber.photo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.redchamber.api.GlobalConstants;
import com.redchamber.bean.PhotoBean;
import com.redchamber.lib.base.BaseActivity;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.photo.adapter.PreviewFriendPhotosAdapter;
import com.redchamber.util.UserLevelUtils;
import com.redchamber.view.widget.CountDownView;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 预览他人相册
 */
public class PreviewFriendPhotosActivity extends BaseActivity {

    @BindView(R.id.countDownView)
    CountDownView mCountDownView;
    @BindView(R.id.vp_photo)
    ViewPager mVpPhoto;
    @BindView(R.id.tv_index)
    TextView mTvIndex;

    private PreviewFriendPhotosAdapter mAdapter;
    private int mIndex = 0; //当前图片下标
    private String mUserId;

    private List<PhotoBean> mPhotoBeans;
    private int mCountTime = 2; //vip6秒，非vip2秒
    private boolean mIsVip = false;

    @Override
    protected int setLayout() {
        return R.layout.red_activity_preview_friend_photos;
    }

    @Override
    protected void initView() {
        if (getIntent() != null) {
            mIndex = getIntent().getIntExtra(GlobalConstants.KEY_INDEX, 0);
            mUserId = getIntent().getStringExtra(GlobalConstants.KEY_USER_ID);
            mPhotoBeans = (List<PhotoBean>) getIntent().getSerializableExtra(GlobalConstants.KEY_PHOTO_LIST);
        }

        if (MyApplication.mMyHomepageBean != null && UserLevelUtils.getLevels(MyApplication.mMyHomepageBean.userLevel)[1]) {
            mCountTime = 6;
            mIsVip = true;
        } else {
            mCountTime = 2;
            mIsVip = false;
        }
        initViewPager();

        mCountDownView.setCountdownTime(mCountTime);
    }

    @OnClick(R.id.iv_back)
    void onClick(View view) {
        finish();
    }

    public static void startActivity(Context context, List<PhotoBean> photoBeans, String userId, int index) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, PreviewFriendPhotosActivity.class);
        intent.putExtra(GlobalConstants.KEY_INDEX, index);
        intent.putExtra(GlobalConstants.KEY_PHOTO_LIST, (Serializable) photoBeans);
        intent.putExtra(GlobalConstants.KEY_USER_ID, userId);
        context.startActivity(intent);
    }

    @SuppressLint("DefaultLocale")
    private void initViewPager() {

        mTvIndex.setText(String.format("1/%d", mPhotoBeans.size()));
        mAdapter = new PreviewFriendPhotosAdapter(this, mPhotoBeans, mUserId, mIsVip);
        mVpPhoto.setAdapter(mAdapter);
        mVpPhoto.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mTvIndex.setVisibility(View.VISIBLE);
                mTvIndex.setText(position + 1 + "/" + mPhotoBeans.size());
                mIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mVpPhoto.setCurrentItem(mIndex);
        mAdapter.setOnFirePhotoPressListener(new PreviewFriendPhotosAdapter.onFirePhotoPressListener() {
            @Override
            public void onPressStart() {
                mTvIndex.setVisibility(View.GONE);
                mCountDownView.setVisibility(View.VISIBLE);
                mCountDownView.startCountDown();
            }

            @Override
            public void onPressEnd() {
                mTvIndex.setVisibility(View.VISIBLE);
                mCountDownView.setVisibility(View.GONE);
                mCountDownView.stopCountDown();
                firePhotoRequest();
            }
        });
    }

    private void firePhotoRequest() {
        if (TextUtils.isEmpty(mUserId)) {
            ToastUtils.showToast("用户id为空");
            return;
        }
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.getInstance(this).getSelfStatus().accessToken);
        params.put("userId", mUserId);
        params.put("photoId", mPhotoBeans.get(mIndex).photoId);

        HttpUtils.post().url(CoreManager.getInstance(this).getConfig().RED_FIRE_PHOTO)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            mPhotoBeans.get(mIndex).status = 1;
                        } else {
                            ToastUtils.showToast(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(PreviewFriendPhotosActivity.this);
                    }
                });
    }
}