package com.sk.weichat.view.chatHolder;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.audio_zx.AudioView;
import com.sk.weichat.audio_zx.VoiceManager;
import com.sk.weichat.audio_zx.VoicePlayer;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.downloader.DownloadListener;
import com.sk.weichat.downloader.Downloader;
import com.sk.weichat.downloader.FailReason;
import com.sk.weichat.util.FileUtil;
import com.sk.weichat.util.ScreenUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class VoiceViewHolder1 extends AChatHolderInterface implements DownloadListener {

    public View view_audio;
    public AudioView av_chat;
    private double seek;
    private int lastX;
    private float dpValue;
    private TextView tv_duration;
    private ChatMessage chatMessage;
    public View.OnTouchListener myTouch = new View.OnTouchListener() {

        private long duration;
        private int offsetSX;
        private int offsetSY;
        private int startX;
        private int startY;
        private long lastDownTime;

        @Override
        public boolean onTouch(View v, MotionEvent event) {   // 获取当前触摸的绝对坐标
            int rawX = (int) event.getRawX();
            int rawY = (int) event.getRawY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view_audio.setVisibility(View.VISIBLE);
                    // 上一次离开时的坐标
                    lastX = rawX;
                    startX = (int) event.getX();
                    startY = (int) event.getY();
                    lastDownTime = event.getDownTime();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (VoiceManager.mCurrtState == VoiceManager.STATE_PLAY) {
                        int offsetX = rawX - lastX;
                        av_chat.moveView(offsetX, view_audio);
                    }
                    offsetSX = (int) (event.getX() - startX);
                    offsetSY = (int) (event.getY() - startY);
                    // 不断修改上次移动完成后坐标
                    lastX = rawX;
                    Log.e("ACTION_MOVE", "onTouch: " + offsetSX);
                    break;
                case MotionEvent.ACTION_UP:
                    Log.e("zx", "onTouch: " + event.getRawX() + "  " + view_audio.getLeft() + "  " + av_chat.getRight());
                    if (event.getEventTime() - lastDownTime > 600 && offsetSX < 10) {
                        mHolderListener.onItemLongClick(v, VoiceViewHolder1.this, mdata);
                        view_audio.setVisibility(View.GONE);
                        return true;
                    } else {
                        //不是播放状态 进入
                        duration = chatMessage.getTimeLen() * 1000;
                        if (VoiceManager.mCurrtState == VoiceManager.STATE_PLAY) {
                            seek = Double.valueOf(view_audio.getLeft()) / Double.valueOf(av_chat.getRight());
                            double doubleDuration = Double.valueOf(duration);
                            double overSeek = doubleDuration * (1 - seek);
                            long last = (long) Math.round(overSeek);
                            duration = last;
                            if (seek >= 1) seek = 0;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            VoiceManager.instance().seek(seek);
                        }
                        VoicePlayer.instance().playVoice(av_chat, duration, view_audio, isMysend);
                        view_audio.performClick();
                        offsetSX = 0;
                    }
                    break;
            }
            return true;
        }
    };

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_voice1 : R.layout.chat_to_item_voice1;
    }

    @Override
    public void initView(View view) {
        av_chat = view.findViewById(R.id.av_chat);
        view_audio = view.findViewById(R.id.view_audio);
        view_audio.setVisibility(View.GONE);
        tv_duration = view.findViewById(R.id.tv_duration);
    }

    @Override
    public void fillData(ChatMessage message) {
        this.chatMessage = message;
        av_chat.fillData(message);

        String[] stringsx = message.getObjectId().split(",");
        ArrayList strings = new ArrayList(Arrays.asList(stringsx));
        int size = strings.size();
        dpValue = size * 5;
        if (dpValue > 200) dpValue = 200;
        ScreenUtil.setLayoutWidth(av_chat, ScreenUtil.dip2px(mContext, dpValue));
        try {
            tv_duration.setText(message.getTimeLen() + "''");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        av_chat.setOnTouchListener(myTouch);

        // 文件不存在 就去下载
        if (!FileUtil.isExist(message.getFilePath())) {
            Downloader.getInstance().addDownload(message.getContent(), mSendingBar, this);
        }
    }

    @Override
    protected void onRootClick(View v) {
        ivUnRead.setVisibility(View.GONE);
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
