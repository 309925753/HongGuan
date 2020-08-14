package com.redchamber.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.signature.StringSignature;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class GlideUtils {

    public static void load(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.mipmap.red_ic_avatar_female)
                .error(R.mipmap.red_ic_avatar_female)
                .into(imageView);
    }

    public static void load(Context context, String url, ImageView imageView, int width, int height) {
        Glide.with(context)
                .load(url)
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.mipmap.red_ic_avatar_female)
                .error(R.mipmap.red_ic_avatar_female)
                .into(imageView);
    }

    public static void load(Context context, int resId, ImageView imageView) {
        Glide.with(context)
                .load(resId)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.mipmap.red_ic_avatar_female)
                .error(R.mipmap.red_ic_avatar_female)
                .into(imageView);
    }

    public static void load(Context context, Bitmap bitmap, ImageView imageView) {
        Glide.with(context)
                .load(bitmap)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.mipmap.red_ic_avatar_female)
                .error(R.mipmap.red_ic_avatar_female)
                .into(imageView);
    }

    public static void loadBlur(Context context, String url, ImageView imageView, int width, int height) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(width, height)
                .placeholder(R.mipmap.red_ic_avatar_female)
                .error(R.mipmap.red_ic_avatar_female)
                .bitmapTransform(new BlurTransformation(context, 100))
                .into(new ViewTarget<ImageView, GlideDrawable>(imageView) {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        //拿到glide的Drawable
                        Drawable drawable = resource.getCurrent();
                        //使用适配类进行包装
                        drawable = DrawableCompat.wrap(drawable);
                        //设置着色的效果和颜色，蒙版模式
                        drawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                        //设置给ImageView
                        imageView.setImageDrawable(drawable);
                    }
                });
    }

    public static void loadBlurGift(Context context, String url, ImageView imageView, int width, int height) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(width, height)
                .placeholder(R.mipmap.red_ic_avatar_female)
                .error(R.mipmap.red_ic_avatar_female)
                .bitmapTransform(new BlurTransformation(context, 100))
                .into(new ViewTarget<ImageView, GlideDrawable>(imageView) {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        //拿到glide的Drawable
                        Drawable drawable = resource.getCurrent();
                        //使用适配类进行包装
                        drawable = DrawableCompat.wrap(drawable);
                        //设置着色的效果和颜色，蒙版模式
                    //    drawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                        //设置给ImageView
                        imageView.setImageDrawable(drawable);
                    }
                });
    }


    public static void getBitmap(String url, int width, int height, ImageLoadCallback imageLoadCallback) {
        Glide.with(MyApplication.getContext()).load(url).asBitmap().error(R.mipmap.red_ic_avatar_female).override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                if (resource != null) {
                    imageLoadCallback.onResourceReady(resource);
                } else {
                    imageLoadCallback.onException(null);
                }
            }

        });
    }

    public static void getBitmap(String url, ImageLoadCallback imageLoadCallback) {
        Glide.with(MyApplication.getContext()).load(url).asBitmap().error(R.mipmap.red_ic_avatar_female)
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                if (resource != null) {
                    imageLoadCallback.onResourceReady(resource);
                } else {
                    imageLoadCallback.onException(null);
                }
            }

        });
    }

    public static void loadBlur(Bitmap bitmap, ImageView imageView) {
        Glide.with(MyApplication.getContext())
                .load(bitmap)
                .placeholder(R.mipmap.red_ic_avatar_female)
                .error(R.mipmap.red_ic_avatar_female)
                .bitmapTransform(new BlurTransformation(MyApplication.getContext(), 100))
                .into(new ViewTarget<ImageView, GlideDrawable>(imageView) {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        //拿到glide的Drawable
                        Drawable drawable = resource.getCurrent();
                        //使用适配类进行包装
                        drawable = DrawableCompat.wrap(drawable);
                        //设置着色的效果和颜色，蒙版模式
                        drawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                        //设置给ImageView
                        imageView.setImageDrawable(drawable);
                    }
                });
    }

    public static void loadAvatar(Context context, String userId, ImageView imageView) {
        String url = RedAvatarUtils.getAvatarUrl(context, userId);
        String updateTime = String.valueOf(System.currentTimeMillis());
        Glide.with(context)
                .load(url)
                .dontAnimate()
                .signature(new StringSignature(updateTime))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(imageView.getDrawable())
                .error(R.mipmap.red_ic_avatar_female)
                .into(imageView);
    }

    public interface ImageLoadCallback {
        void onResourceReady(Bitmap resource);

        void onException(Exception e);
    }

}
