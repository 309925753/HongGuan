package com.sk.weichat.ui.me.sendgroupmessage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sk.weichat.R;
import com.sk.weichat.audio.IMRecordController;
import com.sk.weichat.audio.RecordListener;
import com.sk.weichat.util.InputManager;
import com.sk.weichat.util.filter.EmojiInputFilter;
import com.sk.weichat.view.ChatFaceView;

import java.util.ArrayList;

import static com.sk.weichat.view.ChatBottomView.LIMIT_MESSAGE_LENGTH;

/**
 *
 */
public class ChatBottomForSendGroup extends LinearLayout implements View.OnClickListener {
    private static final int RIGHT_VIEW_RECORD = 0;
    private static final int RIGHT_VIEW_SNED = 1;
    private Context mContext;
    private ImageButton mEmotionBtn;
    private ImageButton mMoreBtn;
    private EditText mChatEdit;
    private Button mRecordBtn;
    private Button mSendBtn;
    private ImageButton mVoiceImgBtn;
    private ChatFaceView mChatFaceView;
    /* Tool */
    private ChatToolsForSendGroup mChatToolsView;
    private InputMethodManager mInputManager;
    private Handler mHandler = new Handler();
    private int mDelayTime = 0;
    private IMRecordController mRecordController;
    private ChatBottomListener mBottomListener;
    // 当前右边的模式，用int变量保存，效率更高点
    private int mRightView = RIGHT_VIEW_RECORD;
    private Toast toast;


    public ChatBottomForSendGroup(Context context) {
        super(context);
        init(context);
    }

    public ChatBottomForSendGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChatBottomForSendGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mInputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mDelayTime = mContext.getResources().getInteger(android.R.integer.config_shortAnimTime);

        LayoutInflater.from(mContext).inflate(R.layout.chat_bottom_for_sg, this);

        mEmotionBtn = (ImageButton) findViewById(R.id.emotion_btn);
        mMoreBtn = (ImageButton) findViewById(R.id.more_btn);
        mChatEdit = (EditText) findViewById(R.id.chat_edit);
        mRecordBtn = (Button) findViewById(R.id.record_btn);
        mSendBtn = (Button) findViewById(R.id.send_btn);
        mVoiceImgBtn = (ImageButton) findViewById(R.id.voice_img_btn);
        mChatFaceView = (ChatFaceView) findViewById(R.id.chat_face_view);
        mChatToolsView = (ChatToolsForSendGroup) findViewById(R.id.chat_tools_view);
        mChatToolsView.setOnToolsClickListener(this);

        mEmotionBtn.setOnClickListener(this);
        mMoreBtn.setOnClickListener(this);
        mVoiceImgBtn.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        mChatEdit.setOnClickListener(this);

        mChatEdit.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mChatEdit.requestFocus();
                return false;
            }
        });

        mChatEdit.setFilters(new InputFilter[]{new EmojiInputFilter(context)});
        mChatEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > LIMIT_MESSAGE_LENGTH) {
                    if (s instanceof Editable) {
                        toastTextLimit();
                        ((Editable) s).delete(start, start + count);
                    }
                }
                int currentView = 0;
                if (s.length() <= 0) {
                    currentView = RIGHT_VIEW_RECORD;
                } else {
                    currentView = RIGHT_VIEW_SNED;
                }

                if (currentView == mRightView) {
                    return;
                }
                mRightView = currentView;
                if (mRightView == 0) {
                    mMoreBtn.setVisibility(View.VISIBLE);
                    mSendBtn.setVisibility(View.GONE);
                } else {
                    mMoreBtn.setVisibility(View.GONE);
                    mSendBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mRecordController = new IMRecordController(mContext);
        mRecordController.setRecordListener(new RecordListener() {
            @Override
            public void onRecordSuccess(String filePath, int timeLen, ArrayList<String> strings) {
                // 录音成功，返回录音文件的路径
                mRecordBtn.setText(R.string.motalk_voice_chat_tip_1);
                mRecordBtn.setBackgroundResource(R.drawable.im_voice_button_normal2);
                if (timeLen < 1) {
                    Toast.makeText(mContext, mContext.getString(R.string.chat_timeless), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mBottomListener != null) {
                    mBottomListener.sendVoice(filePath, timeLen);
                }
            }

            @Override
            public void onRecordStart() {
                mBottomListener.stopVoicePlay();//停止播放聊天记录里的语音
                // 录音开始
                mRecordBtn.setText(R.string.motalk_voice_chat_tip_2);
                mRecordBtn.setBackgroundResource(R.drawable.im_voice_button_pressed2);
            }

            @Override
            public void onRecordCancel() {
                // 录音取消
                mRecordBtn.setText(R.string.motalk_voice_chat_tip_1);
                mRecordBtn.setBackgroundResource(R.drawable.im_voice_button_normal2);
            }
        });
        mRecordBtn.setOnTouchListener(mRecordController);

        mChatFaceView.setEmotionClickListener(new ChatFaceView.EmotionClickListener() {
            @Override
            public void onNormalFaceClick(SpannableString ss) {
                int index = mChatEdit.getSelectionStart();
                if ("[del]".equals(ss.toString())) {
                    InputManager.backSpaceChatEdit(mChatEdit);
                } else {
                    if (mChatEdit.hasFocus()) {
                        mChatEdit.getText().insert(index, ss);
                    } else {
                        mChatEdit.getText().insert(mChatEdit.getText().toString().length(), ss);
                    }
                }
            }

            @Override
            public void onGifFaceClick(String resName) {
                // 发送GIF图片的回调
                if (mBottomListener != null) {
                    mBottomListener.sendGif(resName);
                }
            }

            @Override
            public void onCollecionClick(String collection) {
                // 发送自定义图片的回调
                if (mBottomListener != null) {
                    mBottomListener.sendCollection(collection);
                }
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        mChatEdit.setFocusable(hasWindowFocus);
        mChatEdit.setFocusableInTouchMode(hasWindowFocus);
        super.onWindowFocusChanged(hasWindowFocus);
    }

    @SuppressLint("ShowToast")
    private void toastTextLimit() {
        if (toast == null) {
            toast = Toast.makeText(mContext, mContext.getString(R.string.tip_message_length_limit), Toast.LENGTH_LONG);
        }
        toast.show();
    }

    /**
     * 改变录音按钮的状态<br/>
     * 1、当处于非录音状态，显示录音按钮<br/>
     * true的状态 2、当处于录音状态，显示键盘按钮<br/>
     * false的状态
     */
    private void changeRecordBtn(boolean show) {
        boolean isShowing = mRecordBtn.getVisibility() != View.GONE;
        if (isShowing == show) {
            return;
        }
        if (show) {
            mChatEdit.setVisibility(View.GONE);
            mRecordBtn.setVisibility(View.VISIBLE);
            mVoiceImgBtn.setBackgroundResource(R.mipmap.tool_keyboard);
        } else {
            mChatEdit.setVisibility(View.VISIBLE);
            mRecordBtn.setVisibility(View.GONE);
            mVoiceImgBtn.setBackgroundResource(R.mipmap.voice_chat_icon);
        }
    }

    /**
     * 改变更多按钮的状态<br/>
     * 1、当更多布局显示时，显示隐藏按钮<br/>
     * false的状态 2、当更多布局隐藏时，显示更多按钮<br/>
     * true的状态
     */
    private void changeChatToolsView(boolean show) {
        boolean isShowing = mChatToolsView.getVisibility() != View.GONE;
        if (isShowing == show) {
            return;
        }

        if (show) {
            mChatToolsView.setVisibility(View.VISIBLE);
            mMoreBtn.setBackgroundResource(R.mipmap.unfold_icon);
        } else {
            mChatToolsView.setVisibility(View.GONE);
            mMoreBtn.setBackgroundResource(R.mipmap.show_one_icon);
        }
    }

    /**
     * 显示或隐藏表情布局
     *
     * @param show
     */
    private void changeChatFaceView(boolean show) {
        boolean isShowing = mChatFaceView.getVisibility() != View.GONE;
        if (isShowing == show) {
            return;
        }
        if (show) {
            mChatFaceView.setVisibility(View.VISIBLE);
            mEmotionBtn.setBackgroundResource(R.mipmap.tool_keyboard);
        } else {
            mChatFaceView.setVisibility(View.GONE);
            mEmotionBtn.setBackgroundResource(R.mipmap.expression_icon);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*************************** 控制底部栏状态变化 **************************/
            case R.id.emotion_btn:
                if (mChatFaceView.getVisibility() != View.GONE) {// 表情布局在显示,那么点击则是隐藏表情，显示键盘
                    mInputManager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                    changeChatFaceView(false);
                } else {// 表情布局没有显示,那么点击则是显示表情，隐藏键盘、录音、更多布局
                    mInputManager.hideSoftInputFromWindow(mChatEdit.getApplicationWindowToken(), 0);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            changeChatFaceView(true);
                            changeChatToolsView(false);
                            changeRecordBtn(false);
                        }
                    }, mDelayTime);
                }
                break;
            case R.id.more_btn:
                if (mChatToolsView.getVisibility() != View.GONE) {// 表情布局在显示,那么点击则是隐藏表情，显示键盘
                    mInputManager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                    changeChatToolsView(false);
                } else {// 更多布局没有显示,那么点击则是显示更多，隐藏表情、录音、键盘布局
                    mInputManager.hideSoftInputFromWindow(mChatEdit.getApplicationWindowToken(), 0);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            changeChatFaceView(false);
                            changeChatToolsView(true);
                            changeRecordBtn(false);
                        }
                    }, mDelayTime);
                }
                break;
            case R.id.chat_edit:// 隐藏其他所有布局，显示键盘
                changeChatFaceView(false);
                changeChatToolsView(false);
                changeRecordBtn(false);
                break;
            case R.id.voice_img_btn:
                if (mRecordBtn.getVisibility() != View.GONE) {// 录音布局在显示,那么点击则是隐藏录音，显示键盘
                    mInputManager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                    changeRecordBtn(false);
                } else {// 录音布局没有显示,那么点击则是显示录音，隐藏表情、更多、键盘布局
                    mInputManager.hideSoftInputFromWindow(mChatEdit.getApplicationWindowToken(), 0);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            changeChatFaceView(false);
                            changeChatToolsView(false);
                            changeRecordBtn(true);
                        }
                    }, mDelayTime);
                }
                break;

            /*************************** 底部栏事件回调 **************************/
            case R.id.send_btn:// 发送文字的回调
                if (mBottomListener != null) {
                    String msg = mChatEdit.getText().toString().trim(); // 获取文本框的内容
                    if (TextUtils.isEmpty(msg)) {
                        return;
                    }
                    mBottomListener.sendText(msg);
                }
                break;

            /********** Chat Tool View 的事件 *********/
            case R.id.im_photo_tv:
                // 相册
                if (mBottomListener != null) {
                    mBottomListener.clickPhoto();
                }
                break;
            case R.id.im_camera_tv:
                // 拍照
                if (mBottomListener != null) {
                    mBottomListener.clickCamera();
                }
                break;
            case R.id.im_video_tv:
                // 录像
                if (mBottomListener != null) {
                    mBottomListener.clickVideo();
                }
                break;
            case R.id.im_loc_tv:
                // 位置
                if (mBottomListener != null) {
                    mBottomListener.clickLocation();
                }
                break;
            case R.id.im_card_tv:
                // 名片
                if (mBottomListener != null) {
                    mBottomListener.clickCard();
                }
                break;
            case R.id.im_file_tv:
                // 文件
                if (mBottomListener != null) {
                    mBottomListener.clickFile();
                }
                break;
        }
    }

    public void reset() {
        changeChatFaceView(false);
        changeChatToolsView(false);
        changeRecordBtn(false);
        mInputManager.hideSoftInputFromWindow(mChatEdit.getApplicationWindowToken(), 0);
    }

    public void setChatBottomListener(ChatBottomListener listener) {
        mBottomListener = listener;
    }

    public interface ChatBottomListener {

        void sendText(String text);

        void sendGif(String text);

        // 发送自定义表情
        void sendCollection(String collection);

        void sendVoice(String filePath, int timeLen);

        void stopVoicePlay();

        void clickPhoto();

        void clickCamera();

        void clickVideo();

        void clickLocation();

        void clickCard();

        void clickFile();
    }
}
