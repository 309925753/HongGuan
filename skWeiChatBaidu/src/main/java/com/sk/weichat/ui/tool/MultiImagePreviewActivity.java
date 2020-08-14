package com.sk.weichat.ui.tool;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.qrcode.utils.DecodeUtils;
import com.google.zxing.Result;
import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.message.HandleQRCodeScanUtil;
import com.sk.weichat.util.BitmapUtil;
import com.sk.weichat.util.FileUtil;
import com.sk.weichat.view.SaveWindow;
import com.sk.weichat.view.ZoomImageView;
import com.sk.weichat.view.imageedit.IMGEditActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片集的预览
 */
public class MultiImagePreviewActivity extends BaseActivity {
    public static final int REQUEST_IMAGE_EDIT = 1;

    SparseArray<View> mViews = new SparseArray<View>();
    private ArrayList<String> mImages;
    private int mPosition;
    private boolean mChangeSelected;
    private ViewPager mViewPager;
    private ImagesAdapter mAdapter;
    private CheckBox mCheckBox;
    private TextView mIndexCountTv;
    private List<Integer> mRemovePosition = new ArrayList<Integer>();
    private String imageUrl;
    private String mRealImageUrl;// 因为viewPager的预加载机制，需要记录当前页面真正的url
    private String mEditedPath;
    private SaveWindow mSaveWindow;
    private My_BroadcastReceivers my_broadcastReceiver = new My_BroadcastReceivers();

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images_preview);

        if (getIntent() != null) {
            mImages = (ArrayList<String>) getIntent().getSerializableExtra(AppConstant.EXTRA_IMAGES);
            for (String mImage : mImages) {
                Log.e("MultiImage", "dateSource:" + mImage);
            }
            mPosition = getIntent().getIntExtra(AppConstant.EXTRA_POSITION, 0);
            mChangeSelected = getIntent().getBooleanExtra(AppConstant.EXTRA_CHANGE_SELECTED, false);
        }
        if (mImages == null) {
            mImages = new ArrayList<String>();
        }

        initView();
        register();
    }

    private void doFinish() {
        if (mChangeSelected) {
            Intent intent = new Intent();
            ArrayList<String> resultImages = null;
            if (mRemovePosition.size() == 0) {
                resultImages = mImages;
            } else {
                resultImages = new ArrayList<String>();
                for (int i = 0; i < mImages.size(); i++) {
                    if (!isInRemoveList(i)) {
                        resultImages.add(mImages.get(i));
                    }
                }
            }
            intent.putExtra(AppConstant.EXTRA_IMAGES, resultImages);
            setResult(RESULT_OK, intent);
        }
        finish();
        overridePendingTransition(0, 0);// 关闭过场动画
    }

    @Override
    public void onBackPressed() {
        doFinish();
    }

    @Override
    protected boolean onHomeAsUp() {
        doFinish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (my_broadcastReceiver != null) {
            unregisterReceiver(my_broadcastReceiver);
        }
    }

    private void initView() {
        getSupportActionBar().hide();
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mIndexCountTv = (TextView) findViewById(R.id.index_count_tv);
        mCheckBox = (CheckBox) findViewById(R.id.check_box);
        mViewPager.setPageMargin(10);

        mAdapter = new ImagesAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        updateSelectIndex(mPosition);

        if (mPosition < mImages.size()) {
            mViewPager.setCurrentItem(mPosition);
        }

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                updateSelectIndex(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    public void updateSelectIndex(final int index) {
        if (mPosition >= mImages.size()) {
            mIndexCountTv.setText(null);
        } else {
            mRealImageUrl = mImages.get(index);
            mIndexCountTv.setText((index + 1) + "/" + mImages.size());
        }

        if (!mChangeSelected) {
            mCheckBox.setVisibility(View.GONE);
            return;
        }

        mCheckBox.setOnCheckedChangeListener(null);
        boolean removed = isInRemoveList(index);
        if (removed) {
            mCheckBox.setChecked(false);
        } else {
            mCheckBox.setChecked(true);
        }
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    removeFromRemoveList(index);
                } else {
                    addInRemoveList(index);
                }
            }
        });
    }

    void addInRemoveList(int position) {
        if (!isInRemoveList(position)) {
            mRemovePosition.add(Integer.valueOf(position));
        }
    }

    void removeFromRemoveList(int position) {
        if (isInRemoveList(position)) {
            mRemovePosition.remove(Integer.valueOf(position));
        }
    }

    boolean isInRemoveList(int position) {
        return mRemovePosition.indexOf(Integer.valueOf(position)) != -1;
    }

    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(com.sk.weichat.broadcast.OtherBroadcast.singledown);
        filter.addAction(com.sk.weichat.broadcast.OtherBroadcast.longpress);
        registerReceiver(my_broadcastReceiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_EDIT:
                    mRealImageUrl = mEditedPath;
                    mImages.set(mViewPager.getCurrentItem(), mEditedPath);
                    // 刷新当前页面，
                    mAdapter.refreshCurrent();
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    class My_BroadcastReceivers extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(com.sk.weichat.broadcast.OtherBroadcast.singledown)) {
                doFinish();
            } else if (intent.getAction().equals(com.sk.weichat.broadcast.OtherBroadcast.longpress)) {
                // 长按屏幕，弹出菜单
                if (TextUtils.isEmpty(imageUrl)) {
                    Toast.makeText(MultiImagePreviewActivity.this, getString(R.string.image_is_null), Toast.LENGTH_SHORT).show();
                    return;
                }
                mSaveWindow = new SaveWindow(MultiImagePreviewActivity.this, BitmapUtil.getImageIsQRcode(MultiImagePreviewActivity.this, mRealImageUrl), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSaveWindow.dismiss();
                        switch (v.getId()) {
                            case R.id.save_image:
                                FileUtil.downImageToGallery(MultiImagePreviewActivity.this, mRealImageUrl);
                                break;
                            case R.id.edit_image:
                                ImageLoadHelper.loadFile(
                                        MultiImagePreviewActivity.this,
                                        mRealImageUrl,
                                        f -> {
                                            mEditedPath = FileUtil.createImageFileForEdit().getAbsolutePath();
                                            IMGEditActivity.startForResult(MultiImagePreviewActivity.this, Uri.fromFile(f), mEditedPath, REQUEST_IMAGE_EDIT);
                                        }
                                );
                                break;
                            case R.id.identification_qr_code:
                                // 识别图中二维码
                                ImageLoadHelper.loadBitmapCenterCropDontAnimateWithError(
                                        mContext,
                                        mRealImageUrl,
                                        R.drawable.image_download_fail_icon,
                                        b -> {
                                            new Thread(() -> {
                                                final Result result = DecodeUtils.decodeFromPicture(b);
                                                mViewPager.post(() -> {
                                                    if (result != null && !TextUtils.isEmpty(result.getText())) {
                                                        HandleQRCodeScanUtil.handleScanResult(mContext, result.getText());
                                                    } else {
                                                        Toast.makeText(MultiImagePreviewActivity.this, R.string.decode_failed, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }).start();
                                        }, e -> {
                                            Toast.makeText(MultiImagePreviewActivity.this, R.string.unrecognized, Toast.LENGTH_SHORT).show();
                                        }
                                );
                                break;
                        }
                    }
                });
                mSaveWindow.show();
            }
        }
    }

    class ImagesAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mImages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public void refreshCurrent() {
            AvatarHelper.getInstance().displayUrl(mRealImageUrl, (ZoomImageView) mViews.get(mViewPager.getCurrentItem()));
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = mViews.get(position);
            if (view == null) {
                view = new ZoomImageView(MultiImagePreviewActivity.this);
                mViews.put(position, view);
            }
            // init status
            imageUrl = mImages.get(position);

            // copy from com.sk.weichat.ui.tool.SingleImagePreviewActivity.initView
            ImageView mImageView = (ImageView) view;
            String mImageUri = imageUrl;
            // 网络加载
            if (mImageUri.endsWith(".gif")) {
                ImageLoadHelper.showGifWithError(
                        mContext,
                        mImageUri,
                        R.drawable.image_download_fail_icon,
                        mImageView
                );
            } else {
                ImageLoadHelper.loadBitmapCenterCropDontAnimateWithError(
                        mContext,
                        mImageUri,
                        R.drawable.image_download_fail_icon,
                        b -> {
                            mImageView.setImageBitmap(b);
                        }, e -> {
                            mImageView.setImageResource(R.drawable.image_download_fail_icon);
                        });
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = mViews.get(position);
            if (view == null) {
                super.destroyItem(container, position, object);
            } else {
                container.removeView(view);
            }
        }
    }
}
