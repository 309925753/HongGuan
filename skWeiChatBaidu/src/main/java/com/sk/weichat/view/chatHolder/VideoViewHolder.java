package com.sk.weichat.view.chatHolder;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.downloader.DownloadListener;
import com.sk.weichat.downloader.DownloadProgressListener;
import com.sk.weichat.downloader.Downloader;
import com.sk.weichat.downloader.FailReason;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.UploadEngine;
import com.sk.weichat.util.FileUtil;
import com.sk.weichat.util.HttpUtil;
import com.sk.weichat.video.ChatVideoPreviewActivity;
import com.sk.weichat.view.SelectionFrame;
import com.sk.weichat.view.XuanProgressPar;
import com.sk.weichat.xmpp.listener.ChatMessageListener;

public class VideoViewHolder extends AChatHolderInterface implements DownloadListener, DownloadProgressListener {

    // JVCideoPlayerStandardforchat mVideo;
    ImageView mVideo;
    ImageView ivStart;
    XuanProgressPar progressPar;
    TextView tvInvalid;
    ImageView ivUploadCancel;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_video : R.layout.chat_to_item_video;
    }

    @Override
    public void initView(View view) {
        mVideo = view.findViewById(R.id.chat_jcvideo);
        ivStart = view.findViewById(R.id.iv_start);
        progressPar = view.findViewById(R.id.img_progress);
        tvInvalid = view.findViewById(R.id.tv_invalid);
        ivUploadCancel = view.findViewById(R.id.chat_upload_cancel_iv);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        tvInvalid.setVisibility(View.GONE);

        String filePath = message.getFilePath();
        boolean isExist = FileUtil.isExist(filePath);

        if (!isExist) {
            AvatarHelper.getInstance().asyncDisplayOnlineVideoThumb(message.getContent(), mVideo);
        } else {
            AvatarHelper.getInstance().displayVideoThumb(filePath, mVideo);
            ivStart.setImageResource(fm.jiecao.jcvideoplayer_lib.R.drawable.jc_click_play_selector);
        }

        if (isMysend) { // 判断是否上传
            // 没有上传或者 进度小于100
            boolean show = !message.isUpload() && message.getUploadSchedule() < 100
                    && message.getMessageState() == ChatMessageListener.MESSAGE_SEND_ING;
            changeVisible(progressPar, show);
            changeVisible(ivStart, !show);

            if (show) {
                if (ivUploadCancel != null) {
                    ivUploadCancel.setVisibility(View.VISIBLE);
                }
            } else {
                if (ivUploadCancel != null) {
                    ivUploadCancel.setVisibility(View.GONE);
                }
            }
        }

        progressPar.update(message.getUploadSchedule());
        mSendingBar.setVisibility(View.GONE);

        if (ivUploadCancel != null) {
            ivUploadCancel.setOnClickListener(v -> {
                SelectionFrame selectionFrame = new SelectionFrame(mContext);
                selectionFrame.setSomething(getString(R.string.cancel_upload), getString(R.string.sure_cancel_upload), new SelectionFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                        // 用户可能在弹窗弹起后停留很久，所以点击确认的时候还需要判断一下
                        if (!mdata.isUpload()) {
                            UploadEngine.cancel(mdata.getPacketId());
                        }
                    }
                });
                selectionFrame.show();
            });
        }
    }

    @Override
    protected void onRootClick(View v) {
        if (tvInvalid.getVisibility() == View.VISIBLE) {
            return;
        }

        String filePath = mdata.getFilePath();
        if (!FileUtil.isExist(filePath)) {
            filePath = mdata.getContent();
            // 本地不存在，传网络路径进去播放，下载。。。
            if (HttpUtil.isConnectedGprs(mContext)) {
                SelectionFrame selectionFrame = new SelectionFrame(mContext);
                String finalFilePath = filePath;
                selectionFrame.setSomething(null, getString(R.string.tips_not_wifi), new SelectionFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                        Downloader.getInstance().addDownload(finalFilePath, mSendingBar, VideoViewHolder.this, VideoViewHolder.this);
                    }
                });
                selectionFrame.show();
            } else {
                Downloader.getInstance().addDownload(filePath, mSendingBar, VideoViewHolder.this, VideoViewHolder.this);
            }
        } else {
            startPlay(filePath);
        }
    }

    private void startPlay(String filePath) {
        Intent intent = new Intent(mContext, ChatVideoPreviewActivity.class);
        intent.putExtra(AppConstant.EXTRA_VIDEO_FILE_PATH, filePath);
        if (mdata.getIsReadDel()) {
            intent.putExtra("DEL_PACKEDID", mdata.getPacketId());
        }

        ivUnRead.setVisibility(View.GONE);
        mContext.startActivity(intent);
    }

    @Override
    public void onStarted(String uri, View view) {
        changeVisible(progressPar, true);
        changeVisible(ivStart, false);
    }

    @Override
    public void onFailed(String uri, FailReason failReason, View view) {
        changeVisible(progressPar, false);
        ivStart.setImageResource(fm.jiecao.jcvideoplayer_lib.R.drawable.jc_click_error_selector);
        tvInvalid.setVisibility(View.VISIBLE);
        ivStart.setVisibility(View.VISIBLE);
    }

    @Override
    public void onComplete(String uri, String filePath, View view) {
        mdata.setFilePath(filePath);
        changeVisible(progressPar, false);
        changeVisible(ivStart, true);
        ivStart.setImageResource(fm.jiecao.jcvideoplayer_lib.R.drawable.jc_click_play_selector);

        // 更新数据库
        ChatMessageDao.getInstance().updateMessageDownloadState(mLoginUserId, mToUserId, mdata.get_id(), true, filePath);
        AvatarHelper.getInstance().displayVideoThumb(filePath, mVideo);
        startPlay(filePath);
    }

    @Override
    public void onCancelled(String uri, View view) {
        changeVisible(progressPar, false);
        changeVisible(ivStart, true);
    }

    @Override
    public void onProgressUpdate(String imageUri, View view, int current, int total) {
        int pro = (int) (current / (float) total * 100);
        progressPar.update(pro);
    }

    @Override
    public boolean enableUnRead() {
        return true;
    }

    @Override
    public boolean enableFire() {
        return true;
    }

}
