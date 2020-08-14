package com.sk.weichat.view;

import android.content.Context;
import android.os.Handler;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.sk.weichat.R;
import com.sk.weichat.util.InputManager;

/**
 * 公共消息的输入消息框
 */
public class PMsgBottomView extends LinearLayout implements View.OnClickListener {

    private Context mContext;
    private ImageButton mEmotionBtn;
    private EditText mChatEdit;
    private Button mSendBtn;

    private ChatFaceView mChatFaceView;

    private InputMethodManager mInputManager;
    private Handler mHandler = new Handler();

    private int mDelayTime = 0;

    private PMsgBottomListener mListener;

    public PMsgBottomView(Context context) {
        super(context);
        init(context);
    }

    public PMsgBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PMsgBottomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mInputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mDelayTime = mContext.getResources().getInteger(android.R.integer.config_shortAnimTime);
        LayoutInflater.from(mContext).inflate(R.layout.p_msg_bottom_view, this);
        mEmotionBtn = (ImageButton) findViewById(R.id.emotion_btn);
        mChatEdit = (EditText) findViewById(R.id.chat_edit);
        mSendBtn = (Button) findViewById(R.id.send_btn);
        mChatFaceView = (ChatFaceView) findViewById(R.id.chat_face_view);

        // mChatVoiceBtn
        mEmotionBtn.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        mChatEdit.setOnClickListener(this);
        mChatEdit.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mChatEdit.requestFocus();
                return false;
            }
        });

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
            }

            @Override
            public void onCollecionClick(String collection) {

            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        mChatEdit.setFocusable(hasWindowFocus);
        mChatEdit.setFocusableInTouchMode(hasWindowFocus);
        super.onWindowFocusChanged(hasWindowFocus);
    }

    private void changeChatFaceView(boolean show) {
        boolean isShowing = mChatFaceView.getVisibility() != View.GONE;
        if (isShowing == show) {
            return;
        }
        if (show) {
            mChatFaceView.setVisibility(View.VISIBLE);
            mEmotionBtn.setBackgroundResource(R.drawable.im_btn_keyboard_bg);
        } else {
            mChatFaceView.setVisibility(View.GONE);
            mEmotionBtn.setBackgroundResource(R.drawable.im_btn_emotion_bg);
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
                } else {// 表情布局没有显示,那么点击则是显示表情，隐藏键盘
                    mInputManager.hideSoftInputFromWindow(mChatEdit.getApplicationWindowToken(), 0);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            changeChatFaceView(true);
                        }
                    }, mDelayTime);
                }
                break;
            case R.id.chat_edit:
                changeChatFaceView(false);
                break;
            case R.id.send_btn:
                if (mListener != null) {
                    String msg = mChatEdit.getText().toString();
                    if (TextUtils.isEmpty(msg)) {
                        return;
                    }
                    mListener.sendText(msg);
                    mChatEdit.setText("");
                }
                break;
        }
    }

    public void reset() {
        changeChatFaceView(false);
        mInputManager.hideSoftInputFromWindow(mChatEdit.getApplicationWindowToken(), 0);
    }

    public void show() {
        setVisibility(View.VISIBLE);
        //必须延迟显示，不延迟的话不会显示
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mChatEdit.requestFocus();
                mInputManager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
            }
        }, mDelayTime);
    }

    public void hide() {
        reset();
        setVisibility(View.GONE);
    }

    public void setPMsgBottomListener(PMsgBottomListener listener) {
        mListener = listener;
    }

    public void setHintText(String text) {
        mChatEdit.setHint(text);
    }

    public static interface PMsgBottomListener {
        public void sendText(String text);
    }
}
