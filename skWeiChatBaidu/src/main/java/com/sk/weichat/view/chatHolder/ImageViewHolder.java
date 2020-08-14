package com.sk.weichat.view.chatHolder;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.downloader.DownloadListener;
import com.sk.weichat.downloader.Downloader;
import com.sk.weichat.downloader.FailReason;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.ui.message.ChatOverviewActivity;
import com.sk.weichat.ui.tool.SingleImagePreviewActivity;
import com.sk.weichat.util.FileUtil;
import com.sk.weichat.view.ChatImageView2;
import com.sk.weichat.view.XuanProgressPar;

import java.util.ArrayList;
import java.util.List;

public class ImageViewHolder extends AChatHolderInterface {
    private static final int IMAGE_MIN_SIZE = 90;
    private static final int IMAGE_MAX_SIZE = 120;
    private ChatImageView2 mImageView;
    private XuanProgressPar progressPar;
    private int width, height;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_image : R.layout.chat_to_item_image;
    }

    @Override
    public void initView(View view) {
        mImageView = view.findViewById(R.id.chat_image);
        progressPar = view.findViewById(R.id.img_progress);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        // 修改image布局大小，解决因图片异步加载且布局设置的warp_content导致setSelection不能滑动到最底部的问题
        changeImageLayoutSize(message);

        String filePath = message.getFilePath();
        if (FileUtil.isExist(filePath)) { // 本地存在

/*
            if (filePath.endsWith(".gif")) { // 加载gif
                fillImageGif(filePath);
            } else {
                if (mHolderListener != null) {
                    Bitmap bitmap = mHolderListener.onLoadBitmap(filePath, width, height);
                    if (bitmap != null && !bitmap.isRecycled()) {
                        mImageView.setImageBitmap(bitmap);
                    } else {
                        mImageView.setImageBitmap(null);
                    }
                }
            }
*/
            fillImage(filePath);
        } else {
            if (TextUtils.isEmpty(message.getContent())) {// 理论上不可能
                mImageView.setImageResource(R.drawable.fez);
            } else {
                mImageView.setImageDrawable(null);
                Downloader.getInstance().addDownload(message.getContent(), mSendingBar, new FileDownloadListener(message));
            }
        }

        // 判断是否为阅后即焚类型的图片，如果是 模糊显示该图片
        if (!isGounp) {
            mImageView.setAlpha(message.getIsReadDel() ? 0.1f : 1f);
        }

        // 上传进度条 我的消息才有进度条
        if (!isMysend || message.isUpload() || message.getUploadSchedule() >= 100) {
            progressPar.setVisibility(View.GONE);
        } else {
            progressPar.setVisibility(View.VISIBLE);
        }
        progressPar.update(message.getUploadSchedule());
    }

/*
    private void fillImageGif(String filePath) {
        try {
            GifDrawable gifFromFile = new GifDrawable(new File(filePath));
            mImageView.setImageGifDrawable(gifFromFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillImage(String filePath) {
        AvatarHelper.getInstance().displayUrl(filePath, mImageView, R.drawable.fez);
    }
*/

    private void fillImage(String filePath) {
        if (filePath.endsWith(".gif")) {
            ImageLoadHelper.showGifWithError(
                    MyApplication.getContext(),
                    filePath,
                    R.drawable.fez,
                    mImageView
            );
        } else {
            ImageLoadHelper.showImageDontAnimateWithError(
                    MyApplication.getContext(),
                    filePath,
                    R.drawable.fez,
                    mImageView
            );
        }
    }

    private void changeImageLayoutSize(ChatMessage message) {
        ViewGroup.LayoutParams mLayoutParams = mImageView.getLayoutParams();

        if (TextUtils.isEmpty(message.getLocation_x()) || TextUtils.isEmpty(message.getLocation_y())) {
            mLayoutParams.width = dp2px(IMAGE_MAX_SIZE);
            mLayoutParams.height = dp2px(IMAGE_MAX_SIZE);
            // todo Location_x 与Location_y为空，本地基本上不存在该file，下面会去下载，就不在这里下载了
            // Downloader.getInstance().addDownload(message.getContent(), mSendingBar, new FileDownloadListener(message));
        } else {
            float image_width = Float.parseFloat(message.getLocation_x());
            float image_height = Float.parseFloat(message.getLocation_y());

            if (image_width == 0f || image_height == 0f) {
                // 以防万一，
                mLayoutParams.width = dp2px(IMAGE_MAX_SIZE);
                mLayoutParams.height = dp2px(IMAGE_MAX_SIZE);
            } else {

                // 基于宽度进行缩放,三挡:宽图 55/100,窄图100/55
                float width = image_width / image_height < 0.4 ? IMAGE_MIN_SIZE : IMAGE_MAX_SIZE;
                float height = width == IMAGE_MAX_SIZE ? Math.max(width / image_width * image_height, IMAGE_MIN_SIZE) : IMAGE_MAX_SIZE;

                mLayoutParams.width = dp2px(width);
                mLayoutParams.height = dp2px(height);
            }
        }

        this.width = mLayoutParams.width;
        this.height = mLayoutParams.height;

        mImageView.setLayoutParams(mLayoutParams);
    }

    @Override
    public void onRootClick(View v) {
        if (mdata.getIsReadDel()) { // 阅后即焚图片跳转至单张图片预览类
            Intent intent = new Intent(mContext, SingleImagePreviewActivity.class);
            intent.putExtra(AppConstant.EXTRA_IMAGE_URI, mdata.getContent());
            intent.putExtra("image_path", mdata.getFilePath());
            intent.putExtra("isReadDel", mdata.getIsReadDel());
            if (!isGounp && !isMysend && mdata.getIsReadDel()) {
                intent.putExtra("DEL_PACKEDID", mdata.getPacketId());
            }
            mContext.startActivity(intent);
        } else {
            int imageChatMessageList_current_position = 0;
            List<ChatMessage> imageChatMessageList = new ArrayList<>();
            for (int i = 0; i < chatMessages.size(); i++) {
                if (chatMessages.get(i).getType() == XmppMessage.TYPE_IMAGE
                        && !chatMessages.get(i).getIsReadDel()) {
                    if (chatMessages.get(i).getPacketId().equals(mdata.getPacketId())) {
                        imageChatMessageList_current_position = imageChatMessageList.size();
                    }
                    imageChatMessageList.add(chatMessages.get(i));
                }
            }
            Intent intent = new Intent(mContext, ChatOverviewActivity.class);
            ChatOverviewActivity.imageChatMessageListStr = JSON.toJSONString(imageChatMessageList);
            intent.putExtra("imageChatMessageList_current_position", imageChatMessageList_current_position);
            mContext.startActivity(intent);
        }
    }

    @Override
    public boolean enableSendRead() {
        return true;
    }

    // 启用阅后即焚
    @Override
    public boolean enableFire() {
        return true;
    }

    class FileDownloadListener implements DownloadListener {
        private ChatMessage message;

        public FileDownloadListener(ChatMessage message) {
            this.message = message;
        }

        @Override
        public void onStarted(String uri, View view) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFailed(String uri, FailReason failReason, View view) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }

        @Override
        public void onComplete(String uri, String filePath, View view) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
            message.setFilePath(filePath);
            ChatMessageDao.getInstance().updateMessageDownloadState(mLoginUserId, mToUserId, message.get_id(), true, filePath);
            // 保存图片尺寸到数据库
            saveImageSize(filePath);

/*
            if (filePath.endsWith(".gif")) { // 加载gif
                fillImageGif(filePath);
            } else { // 加载图片
                fillImage(filePath);
            }
*/
            fillImage(filePath);
        }

        @Override
        public void onCancelled(String uri, View view) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }

        /**
         * 获取图片宽高，保存至本地
         *
         * @param filePath
         */
        private void saveImageSize(String filePath) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options); // 此时返回的bitmap为null

            message.setLocation_x(String.valueOf(options.outWidth));
            message.setLocation_y(String.valueOf(options.outHeight));

            // 重绘图片尺寸
            changeImageLayoutSize(message);
            // 保存下载到数据库
            ChatMessageDao.getInstance().updateMessageLocationXY(message, mLoginUserId);
        }
    }
}
