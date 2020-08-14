package com.redchamber.photo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import com.github.chrisbanes.photoview.PhotoView;
import com.redchamber.bean.PhotoBean;
import com.redchamber.lib.utils.ToastUtils;
import com.redchamber.request.UnlockPhotoRequest;
import com.redchamber.util.BitmapBlurUtil;
import com.redchamber.util.GlideUtils;
import com.redchamber.util.LongClickUtils;
import com.sk.weichat.R;

import java.util.List;

/**
 * 预览他人相册
 */
public class PreviewFriendPhotosAdapter extends PagerAdapter {

    private Context mContext;
    private List<PhotoBean> mImgList;
    private String mUserId;
    private boolean mIsVip;
    private long mCountTime = 2000;

    private onFirePhotoPressListener mOnFirePhotoPressListener;

    public PreviewFriendPhotosAdapter(Context context, List<PhotoBean> imgList, String userId, boolean isVip) {
        this.mContext = context;
        this.mImgList = imgList;
        this.mUserId = userId;
        this.mIsVip = isVip;
        if (isVip) {
            mCountTime = 6000;
        } else {
            mCountTime = 2000;
        }
    }

    @Override
    public int getCount() {
        return mImgList != null && mImgList.size() > 0 ? mImgList.size() : 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.red_item_vp_preview_photo, null);
        container.addView(view);

        bindView(view, position);

        return view;
    }

    private void bindView(View view, int position) {
        PhotoView photoView = view.findViewById(R.id.photo_view);
        RelativeLayout mRlFire = view.findViewById(R.id.rl_fire);
        RelativeLayout mRlFired = view.findViewById(R.id.rl_fired);
        RelativeLayout mRlRed = view.findViewById(R.id.rl_red);
        TextView mTvBeanNum = view.findViewById(R.id.tv_bean_num);
        TextView mTvHintVip = view.findViewById(R.id.tv_hint_vip);
        photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        PhotoBean photoBean = mImgList.get(position);

        final Bitmap[] bitmapResource = new Bitmap[1];     //原始
        final Drawable[] drawableBlur = new Drawable[1];   //模糊

        GlideUtils.getBitmap(photoBean.photoUrl, new GlideUtils.ImageLoadCallback() {
            @Override
            public void onResourceReady(Bitmap resource) {
                bitmapResource[0] = resource;
                if (1 == photoBean.visitType || 2 == photoBean.visitType) {
                    BitmapBlurUtil.addTask(resource, new Handler() {
                        @SuppressLint("HandlerLeak")
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            Drawable drawable = (Drawable) msg.obj;
                            drawableBlur[0] = drawable;
                            photoView.setImageDrawable(drawable);
                        }
                    });
                    if (1 == photoBean.visitType) {//阅后即焚
                        if (1 == photoBean.status) {//已焚毁
                            mRlFired.setVisibility(View.VISIBLE);
                            if (mIsVip) {
                                mTvHintVip.setVisibility(View.GONE);
                            } else {
                                mTvHintVip.setVisibility(View.VISIBLE);
                            }
                            mRlFire.setVisibility(View.GONE);
                        } else {
                            mRlFired.setVisibility(View.GONE);
                            mRlFire.setVisibility(View.VISIBLE);
                        }
                        mRlRed.setVisibility(View.GONE);
                    } else {//红包照片
                        if (1 == photoBean.status) {//已解锁
                            mRlRed.setVisibility(View.GONE);
                            mRlFired.setVisibility(View.GONE);
                            mRlFire.setVisibility(View.GONE);
                            photoView.setImageBitmap(bitmapResource[0]);
                        } else {
                            mRlRed.setVisibility(View.VISIBLE);
                            mTvBeanNum.setText(String.valueOf(photoBean.coin));
                            mRlFired.setVisibility(View.GONE);
                            mRlFire.setVisibility(View.GONE);
                        }
                    }
                } else {//正常图片
                    photoView.setImageBitmap(resource);
                }
            }

            @Override
            public void onException(Exception e) {

            }
        });

        LongClickUtils.setLongClick(new Handler(), mRlFire, mCountTime, new LongClickUtils.onPressListener() {
            @Override
            public void onPressStart() {
                mRlFire.setVisibility(View.GONE);
                mRlFired.setVisibility(View.GONE);
                photoView.setImageBitmap(bitmapResource[0]);
                if (mOnFirePhotoPressListener != null) {
                    mOnFirePhotoPressListener.onPressStart();
                }
            }

            @Override
            public void onPressEnd() {
                mRlFire.setVisibility(View.GONE);
                mRlFired.setVisibility(View.VISIBLE);
                photoView.setImageDrawable(drawableBlur[0]);
                if (mOnFirePhotoPressListener != null) {
                    mOnFirePhotoPressListener.onPressEnd();
                }
            }
        });

        view.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UnlockPhotoRequest.getInstance().unlockPhoto(mContext, mUserId, photoBean.photoId,
                        String.valueOf(photoBean.coin), new UnlockPhotoRequest.UnlockCallBack() {
                            @Override
                            public void onSuccess() {
                                photoBean.status = 1;
                                notifyDataSetChanged();
                                mRlRed.setVisibility(View.GONE);
                                mRlFired.setVisibility(View.GONE);
                                mRlFire.setVisibility(View.GONE);
                                photoView.setImageBitmap(bitmapResource[0]);
                            }

                            @Override
                            public void onFail(String error) {
                                ToastUtils.showToast(error);
                            }
                        });
            }
        });

    }

    public interface onFirePhotoPressListener {

        void onPressStart();

        void onPressEnd();

    }

    public void setOnFirePhotoPressListener(onFirePhotoPressListener mOnFirePhotoPressListener) {
        this.mOnFirePhotoPressListener = mOnFirePhotoPressListener;
    }

}
