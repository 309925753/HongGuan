package com.sk.weichat.view.chatHolder;

import android.util.Log;
import android.view.View;

import com.sk.weichat.R;
import com.sk.weichat.audio_x.VoiceAnimView;
import com.sk.weichat.audio_x.VoicePlayer;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.downloader.DownloadListener;
import com.sk.weichat.downloader.Downloader;
import com.sk.weichat.downloader.FailReason;
import com.sk.weichat.util.FileUtil;

public class VoiceViewHolder extends AChatHolderInterface implements DownloadListener {

    public VoiceAnimView voiceView;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_voice : R.layout.chat_to_item_voice;
    }

    @Override
    public void initView(View view) {
        voiceView = view.findViewById(R.id.chat_voice);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        voiceView.fillData(message);

        // 文件不存在 就去下载
        if (!FileUtil.isExist(message.getFilePath())) {
            Downloader.getInstance().addDownload(message.getContent(), mSendingBar, this);
        }
    }

    @Override
    protected void onRootClick(View v) {
        ivUnRead.setVisibility(View.GONE);
        VoicePlayer.instance().playVoice(voiceView);
    }

    @Override
    public void onStarted(String uri, View view) {
        mSendingBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFailed(String uri, FailReason failReason, View view) {
        Log.e("VOICE", "onFailed" + failReason.getType());
        mSendingBar.setVisibility(View.GONE);
        mIvFailed.setVisibility(View.VISIBLE);
        if (isMysend && mdata.isSendRead()) {// 服务端将文件删除了但是消息还在，漫游拉下来会显示感叹号
            mIvFailed.setVisibility(View.GONE);
        }
    }

    @Override
    public void onComplete(String uri, String filePath, View view) {
        mdata.setFilePath(filePath);
        mSendingBar.setVisibility(View.GONE);

        if (mHolderListener != null) {
            mHolderListener.onCompDownVoice(mdata);
        }

        // 更新数据库
        ChatMessageDao.getInstance().updateMessageDownloadState(mLoginUserId, mToUserId, mdata.get_id(), true, filePath);
    }

    @Override
    public void onCancelled(String uri, View view) {
        Log.e("VOICE", "onCancelled");
        mSendingBar.setVisibility(View.GONE);
        // mIvFailed.setVisibility(View.VISIBLE);
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
