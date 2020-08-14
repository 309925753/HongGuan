package com.sk.weichat.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.util.AsyncUtils;

public class ThumbnailHelper {
    private static final int KEY_TAG = R.id.thumb_request;

    /**
     * 异步显示视频缩略图，
     */
    public static void displayVideoThumb(Context ctx, final String videoFilePath, final ImageView image) {
        if (videoFilePath == null) {
            return;
        }
        image.setTag(KEY_TAG, videoFilePath);
        // 加载缩略图中填充色块，
        image.setImageDrawable(null);
        AsyncUtils.doAsync(ctx, throwable -> {
            // 加载缩略图失败时填充图片，
            image.setImageResource(R.drawable.image_download_fail_icon);
        }, contextAsyncContext -> {
            final Bitmap bitmap = makeThumb(videoFilePath);
            if (bitmap != null) {
                contextAsyncContext.uiThread(context -> {
                    // 二次验证以免视图复用导致混乱，
                    if (videoFilePath.equals(image.getTag(KEY_TAG))) {
                        // 加载缩略图成功，
                        image.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

    /**
     * 同步生成视频缩略图，
     */
    private static Bitmap makeThumb(String video) {
        if (TextUtils.isEmpty(video)) {
            return null;
        }

        // 旧代码保留，
        // 这里是内存强引用缓存，比较浪费内存，可以改用磁盘缓存，
        Bitmap bitmap = MyApplication.getInstance().getBitmapFromMemCache(video);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = ThumbnailUtils.createVideoThumbnail(video, MediaStore.Video.Thumbnails.MINI_KIND);
            // 视频格式不支持可能导致得到缩略图bitmap为空，LruCache不能接受空，
            // 主要是系统相册里的存着的视频不一定都是真实有效的，
            if (bitmap != null) {
                MyApplication.getInstance().addBitmapToMemoryCache(video, bitmap);
            }
        }
        return bitmap;
    }
}
