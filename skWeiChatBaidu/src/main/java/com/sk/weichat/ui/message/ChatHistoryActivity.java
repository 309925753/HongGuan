package com.sk.weichat.ui.message;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.makeramen.roundedimageview.RoundedImageView;
import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.audio_x.VoiceAnimView;
import com.sk.weichat.audio_x.VoicePlayer;
import com.sk.weichat.audio_zx.AudioView;
import com.sk.weichat.audio_zx.VoiceManager;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.db.dao.ChatMessageDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.map.MapActivity;
import com.sk.weichat.ui.tool.SingleImagePreviewActivity;
import com.sk.weichat.util.CommonAdapter;
import com.sk.weichat.util.CommonViewHolder;
import com.sk.weichat.util.HtmlUtils;
import com.sk.weichat.util.SmileyParser;
import com.sk.weichat.util.StringUtils;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import fm.jiecao.jcvideoplayer_lib.JVCideoPlayerStandardSecond;
import fm.jiecao.jcvideoplayer_lib.JVCideoPlayerStandardforchat;
import pl.droidsonroids.gif.GifImageView;

import static com.sk.weichat.bean.message.XmppMessage.TYPE_IMAGE_TEXT;
import static com.sk.weichat.bean.message.XmppMessage.TYPE_IMAGE_TEXT_MANY;

/**
 * 合并转发 查看聊天记录
 */
public class ChatHistoryActivity extends BaseActivity {
    public AudioView av_chat;
    private ListView mChatHistoryListView;
    private ChatHistoryAdapter mChatHistoryAdapter;
    private List<ChatMessage> mChatHistoryChatMessageList;
    private String mLoginUserId;
    private String mFriendId;
    private String mMsgId;
    private ChatMessage mChatMessage;
    private View view_audio;
    private int lastX;
    private int lastY;
    private double seek;
    public View.OnTouchListener myTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {   // 获取当前触摸的绝对坐标
            int rawX = (int) event.getRawX();
            int rawY = (int) event.getRawY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.e("zx", "onTouch: myTouch");
                    // 上一次离开时的坐标
                    lastX = rawX;
                    lastY = rawY;
                    break;
                case MotionEvent.ACTION_MOVE:
//                    VoiceManager.instance().pause();
                    // 两次的偏移量
                    int offsetX = rawX - lastX;
                    int offsetY = rawY - lastY;
                    av_chat.moveView(offsetX, view_audio);

                    // 不断修改上次移动完成后坐标
                    lastX = rawX;
                    lastY = rawY;
                    break;
                case MotionEvent.ACTION_UP:
                    Log.e("ACTION_UP", "onTouch: myTouch ACTION_MOVE: " + seek + " rawX: " + rawX);
                    Message message = new Message();
                    //不是播放状态 进入
                    seek = view_audio.getLeft() / 1080.0;

//                    VoiceManager.instance().play(FileUtil.getRandomAudioAmrFilePath());
                    av_chat.start();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    VoiceManager.instance().seek(seek);

                    long duration = VoiceManager.instance().getDuration();
                    double d = Double.valueOf(duration);
                    double db = d * (1 - seek);
                    long last = (long) Math.round(db);
                    duration = last;
                    Log.e("zxzx", "onCreate: " + duration);
                 /*   message.what = 1;
                    message.obj = duration;
                    handler.sendMessage(message);*/
//                    av_chat.setAni(duration, view_audio);
                    view_audio.performClick();

                    break;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);
        mLoginUserId = coreManager.getSelf().getUserId();
        mFriendId = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
        mMsgId = getIntent().getStringExtra(AppConstant.EXTRA_MSG_ID);
        mChatMessage = ChatMessageDao.getInstance().findMsgById(mLoginUserId, mFriendId, mMsgId);
        if (mChatMessage == null) {
            ToastUtil.showErrorData(this);
            finish();
        }
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView mTvTitle = findViewById(R.id.tv_title_center);
        mTvTitle.setText(mChatMessage.getObjectId());
    }

    private void initView() {
        mChatHistoryChatMessageList = new ArrayList<>();
        String detail = mChatMessage.getContent();
        try {
            List<String> mStringHistory = JSON.parseArray(detail, String.class);
            for (int i = 0; i < mStringHistory.size(); i++) {
                ChatMessage chatMessage = new ChatMessage(mStringHistory.get(i));// 解析json,还原ChatMessage
                mChatHistoryChatMessageList.add(chatMessage);
            }
        } catch (Exception e) {

        }


        // 只显示 文字、图片、语音、视频、地图,其余消息类型全部当做文字处理
        for (int i = 0; i < mChatHistoryChatMessageList.size(); i++) {
            if (mChatHistoryChatMessageList.get(i).getType() == XmppMessage.TYPE_CARD) {
                mChatHistoryChatMessageList.get(i).setType(XmppMessage.TYPE_TEXT);
                mChatHistoryChatMessageList.get(i).setContent(getString(R.string.chat_card));
            } else if (mChatHistoryChatMessageList.get(i).getType() >= XmppMessage.TYPE_IS_CONNECT_VOICE
                    && mChatHistoryChatMessageList.get(i).getType() <= XmppMessage.TYPE_EXIT_VOICE) {
                mChatHistoryChatMessageList.get(i).setType(XmppMessage.TYPE_TEXT);
                mChatHistoryChatMessageList.get(i).setContent(getString(R.string.msg_video_voice));
            } else if (mChatHistoryChatMessageList.get(i).getType() == XmppMessage.TYPE_FILE) {
                mChatHistoryChatMessageList.get(i).setType(XmppMessage.TYPE_TEXT);
                mChatHistoryChatMessageList.get(i).setContent(getString(R.string.msg_file));
            } else if (mChatHistoryChatMessageList.get(i).getType() == XmppMessage.TYPE_RED) {
                mChatHistoryChatMessageList.get(i).setType(XmppMessage.TYPE_TEXT);
                mChatHistoryChatMessageList.get(i).setContent(getString(R.string.msg_red_packet));
            } else if (mChatHistoryChatMessageList.get(i).getType() == XmppMessage.TYPE_SHAKE) {
                mChatHistoryChatMessageList.get(i).setType(XmppMessage.TYPE_TEXT);
                mChatHistoryChatMessageList.get(i).setContent(getString(R.string.msg_shake));
            } else if (mChatHistoryChatMessageList.get(i).getType() == XmppMessage.TYPE_REPLAY) {
                mChatHistoryChatMessageList.get(i).setType(XmppMessage.TYPE_TEXT);
                mChatHistoryChatMessageList.get(i).setContent("[" + getString(R.string.replay) + "]");
            } else if (mChatHistoryChatMessageList.get(i).getType() == XmppMessage.TYPE_CHAT_HISTORY) {
                mChatHistoryChatMessageList.get(i).setType(XmppMessage.TYPE_TEXT);
                mChatHistoryChatMessageList.get(i).setContent(getString(R.string.msg_chat_history));
            } else if (mChatHistoryChatMessageList.get(i).getType() == XmppMessage.TYPE_LINK || mChatHistoryChatMessageList.get(i).getType() == XmppMessage.TYPE_SHARE_LINK) {
                mChatHistoryChatMessageList.get(i).setContent("[" + getString(R.string.link) + "]");
            } else if (mChatHistoryChatMessageList.get(i).getType() == TYPE_IMAGE_TEXT || mChatHistoryChatMessageList.get(i).getType() == TYPE_IMAGE_TEXT_MANY) {
                mChatHistoryChatMessageList.get(i).setContent("[" + getString(R.string.graphic) +
                        getString(R.string.message) + "]");
            } else if (mChatHistoryChatMessageList.get(i).getType() == XmppMessage.TYPE_TRANSFER) {
                mChatHistoryChatMessageList.get(i).setType(XmppMessage.TYPE_TEXT);
                mChatHistoryChatMessageList.get(i).setContent(getString(R.string.tip_transfer_money));
            } else if (mChatHistoryChatMessageList.get(i).getType() == XmppMessage.TYPE_SECURE_LOST_KEY) {
                mChatHistoryChatMessageList.get(i).setType(XmppMessage.TYPE_TEXT);
                mChatHistoryChatMessageList.get(i).setContent(getString(R.string.request_chat_key_group));
            }
        }
        mChatHistoryListView = (ListView) findViewById(R.id.chat_history_lv);
        mChatHistoryAdapter = new ChatHistoryAdapter(this, mChatHistoryChatMessageList);
        mChatHistoryListView.setAdapter(mChatHistoryAdapter);

        mChatHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatMessage chatMessage = mChatHistoryChatMessageList.get(position);
                if (chatMessage != null) {
                    if (chatMessage.getType() == XmppMessage.TYPE_IMAGE) {
                        Intent intent = new Intent(mContext, SingleImagePreviewActivity.class);
                        intent.putExtra(AppConstant.EXTRA_IMAGE_URI, chatMessage.getContent());
                        mContext.startActivity(intent);
                    } else if (chatMessage.getType() == XmppMessage.TYPE_LOCATION) {
                        Intent intent = new Intent(mContext, MapActivity.class);
                        intent.putExtra("latitude", Double.valueOf(chatMessage.getLocation_x()));
                        intent.putExtra("longitude", Double.valueOf(chatMessage.getLocation_y()));
                        intent.putExtra("address", chatMessage.getObjectId());
                        mContext.startActivity(intent);
                    }
                }
            }
        });
    }

    class ChatHistoryAdapter extends CommonAdapter<ChatMessage> {

        public ChatHistoryAdapter(Context context, List<ChatMessage> data) {
            super(context, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.row_chat_history, position);
            ImageView avatar_iv = viewHolder.getView(R.id.avatar_iv);
            TextView name_tv = viewHolder.getView(R.id.name_tv);
            TextView time_tv = viewHolder.getView(R.id.time_tv);
            // 文字类型
            TextView content_tv = viewHolder.getView(R.id.content_tv);
            // Gif类型
            GifImageView content_gif = viewHolder.getView(R.id.content_gif);
            // 图片类型
            RoundedImageView content_iv = viewHolder.getView(R.id.content_iv);
            // 语音类型
            VoiceAnimView content_voice = viewHolder.getView(R.id.content_va);
/*
            RelativeLayout content_voice_new_rl = viewHolder.getView(R.id.content_va_new_rl);
            AudioView content_Audio = viewHolder.getView(R.id.av_chat);
            view_audio = viewHolder.getView(R.id.view_audio);
*/

            // 视频类型
            JVCideoPlayerStandardforchat content_jvc = viewHolder.getView(R.id.content_jvc);
            // 位置类型
            RelativeLayout content_rl = viewHolder.getView(R.id.content_rl);
            TextView content_rl_tv = viewHolder.getView(R.id.content_rl_tv);
            ChatMessage chatMessage = data.get(position);
            if (chatMessage != null) {
                AvatarHelper.getInstance().displayAvatar(chatMessage.getFromUserName(), chatMessage.getFromUserId(), avatar_iv, false);
                time_tv.setText(TimeUtils.getFriendlyTimeDesc10(ChatHistoryActivity.this, chatMessage.getTimeSend()));
                name_tv.setText(chatMessage.getFromUserName());
                if (chatMessage.getType() == XmppMessage.TYPE_TEXT
                        || chatMessage.getType() == XmppMessage.TYPE_CHAT_HISTORY) {
                    content_tv.setVisibility(View.VISIBLE);
                    content_gif.setVisibility(View.GONE);
                    content_iv.setVisibility(View.GONE);
                    content_voice.setVisibility(View.GONE);
                    // content_voice_new_rl.setVisibility(View.GONE);
                    content_jvc.setVisibility(View.GONE);
                    content_rl.setVisibility(View.GONE);
                    // 支持显示本地表情
                    String s = StringUtils.replaceSpecialChar(chatMessage.getContent());
                    CharSequence charSequence = HtmlUtils.transform200SpanString(s, true);
                    content_tv.setText(charSequence);
                } else if (chatMessage.getType() == XmppMessage.TYPE_GIF) {
                    content_tv.setVisibility(View.GONE);
                    content_gif.setVisibility(View.VISIBLE);
                    content_iv.setVisibility(View.GONE);
                    content_voice.setVisibility(View.GONE);
                    // content_voice_new_rl.setVisibility(View.GONE);
                    content_jvc.setVisibility(View.GONE);
                    content_rl.setVisibility(View.GONE);
                    String gifName = chatMessage.getContent();
                    int resId = SmileyParser.Gifs.textMapId(gifName);
                    if (resId != -1) {
                        content_gif.setImageResource(resId);
                    } else {
                        content_gif.setImageBitmap(null);
                    }
                } else if (chatMessage.getType() == XmppMessage.TYPE_IMAGE) {
                    content_tv.setVisibility(View.GONE);
                    content_gif.setVisibility(View.GONE);
                    content_iv.setVisibility(View.VISIBLE);
                    content_voice.setVisibility(View.GONE);
                    // content_voice_new_rl.setVisibility(View.GONE);
                    content_jvc.setVisibility(View.GONE);
                    content_rl.setVisibility(View.GONE);
                    ImageLoadHelper.showImageWithPlaceHolder(
                            mContext,
                            chatMessage.getContent(),
                            R.drawable.ffb,
                            R.drawable.fez,
                            content_iv
                    );
                } else if (chatMessage.getType() == XmppMessage.TYPE_VOICE) {
                    content_tv.setVisibility(View.GONE);
                    content_gif.setVisibility(View.GONE);
                    content_iv.setVisibility(View.GONE);
                    content_voice.setVisibility(View.VISIBLE);
                    // content_voice_new_rl.setVisibility(View.VISIBLE);
                    content_jvc.setVisibility(View.GONE);
                    content_rl.setVisibility(View.GONE);
                    content_voice.fillData(chatMessage);
                    content_voice.setOnClickListener(v -> {
                        VoicePlayer.instance().playVoice(content_voice);
                    });
                    // content_Audio.fillData(chatMessage);
                    // content_Audio.setOnTouchListener(myTouch);
                } else if (chatMessage.getType() == XmppMessage.TYPE_VIDEO) {
                    content_tv.setVisibility(View.GONE);
                    content_gif.setVisibility(View.GONE);
                    content_iv.setVisibility(View.GONE);
                    content_voice.setVisibility(View.GONE);
                    // content_voice_new_rl.setVisibility(View.GONE);
                    content_jvc.setVisibility(View.VISIBLE);
                    content_rl.setVisibility(View.GONE);
                    content_jvc.setUp(chatMessage.getContent(), JVCideoPlayerStandardSecond.SCREEN_LAYOUT_NORMAL, "");
                } else if (chatMessage.getType() == XmppMessage.TYPE_LOCATION) {
                    content_tv.setVisibility(View.GONE);
                    content_gif.setVisibility(View.GONE);
                    content_iv.setVisibility(View.GONE);
                    content_voice.setVisibility(View.GONE);
                    // content_voice_new_rl.setVisibility(View.GONE);
                    content_jvc.setVisibility(View.GONE);
                    content_rl.setVisibility(View.VISIBLE);
                    content_rl_tv.setText(chatMessage.getObjectId());
                } else {// 不允许在出现其他类型，如出现，隐藏所有控件
                    content_tv.setVisibility(View.GONE);
                    content_gif.setVisibility(View.GONE);
                    content_iv.setVisibility(View.GONE);
                    content_voice.setVisibility(View.GONE);
                    // content_voice_new_rl.setVisibility(View.GONE);
                    content_jvc.setVisibility(View.GONE);
                    content_rl.setVisibility(View.GONE);
                }
            }
            return viewHolder.getConvertView();
        }
    }
}
