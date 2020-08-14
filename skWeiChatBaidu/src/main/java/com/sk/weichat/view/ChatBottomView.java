package com.sk.weichat.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;

import com.makeramen.roundedimageview.RoundedImageView;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.audio.IMRecordController;
import com.sk.weichat.audio.RecordListener;
import com.sk.weichat.audio_x.VoicePlayer;
import com.sk.weichat.bean.PublicMenu;
import com.sk.weichat.bean.assistant.GroupAssistantDetail;
import com.sk.weichat.bean.event.MessageEventRequert;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.course.ChatRecordHelper;
import com.sk.weichat.db.InternationalizationHelper;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.mucfile.XfileUtils;
import com.sk.weichat.ui.tool.WebViewActivity;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.DisplayUtil;
import com.sk.weichat.util.HtmlUtils;
import com.sk.weichat.util.InputManager;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.SmileyParser;
import com.sk.weichat.util.UiUtils;
import com.sk.weichat.util.filter.EmojiInputFilter;
import com.sk.weichat.util.input.KeyboardPanelUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.sk.weichat.ui.tool.WebViewActivity.EXTRA_URL;

/**
 * @项目名称: SkWeiChat-Baidu
 * @包名: com.sk.weichat.view
 * @作者:王阳
 * @创建时间: 2015年10月15日 下午5:59:56
 * @描述: 聊天界面下面输入操作的view
 * @SVN版本号: $Rev$
 * @修改人: $Author$
 * @修改时间: $Date$
 * @修改的内容: 修改录音问题
 */
public class ChatBottomView extends LinearLayout implements View.OnClickListener {
    public static final int LIMIT_MESSAGE_LENGTH = 1500;
    private static final int RIGHT_VIEW_RECORD = 0;
    private static final int RIGHT_VIEW_SNED = 1;
    // 正在输入...
    boolean inputState = true;
    List<PublicMenu> mMenuDatas;
    private Context mContext;
    private LinearLayout mShotsLl;
    private RoundedImageView mShotsIv;
    // 主菜单
    private RelativeLayout rlChatMenu;
    private FrameLayout flPublicChatMenu;// 切换至公众号菜单
    private ImageButton mVoiceImgBtn;
    private ImageButton btnCancelReplay;
    private EditText mChatEdit;
    private Button mRecordBtn;
    private ImageButton mEmotionBtn;
    private ImageButton mMoreBtn;
    private Button mSendBtn;
    // 公众号菜单
    private LinearLayout lLTextMenu;
    private ViewStub lLTextMenuStub;
    private ImageView meunImg1;
    private ImageView meunImg2;
    private ImageView meunImg3;
    private TextView meunText1, meunText2, meunText3;
    private View light1, light2;
    // 多选菜单
    private LinearLayout lLMoreSelect;
    private ViewStub lLMoreSelectStub;
    /* Tool */
    private ChatFaceView mChatFaceView;
    private View vBottomPanel;
    private ViewStub mChatFaceViewStub;
    private ChatToolsView mChatToolsView;
    private ViewStub mChatToolsViewStub;
    private ChatBottomListener mBottomListener;
    private MoreSelectMenuListener mMoreSelectMenuListener;
    private IMRecordController mRecordController;
    private InputMethodManager mInputManager;
    private Handler mHandler = new Handler();

    private boolean isGroup;
    private String roomId;
    private String roomJid;
    private boolean isBanned, isLost;

    // 当前右边的模式，用int变量保存，效率更高点
    private int mRightView = RIGHT_VIEW_RECORD;
    private LayoutInflater mInflater;
    private boolean isEquipment;
    private boolean replayMode;
    private ChatMessage replayMessage;
    private Window window;
    OnClickListener publicMenuClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ll_public_meun_1:
                    publicMenuClick(0);
                    break;
                case R.id.ll_public_meun_2:
                    publicMenuClick(1);
                    break;
                case R.id.ll_public_meun_3:
                    publicMenuClick(2);
                    break;
            }
        }
    };
    private Runnable hideBottomTask;

    public ChatBottomView(Context context) {
        super(context);
        init(context);
    }

    public ChatBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChatBottomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void publicMenuClick(int position) {
        if (XfileUtils.isNotEmpty(mMenuDatas.get(position).getMenuList())) {
            if (position == 0) {
                showPpWindow(mMenuDatas.get(position).getMenuList(), meunText1);
            } else if (position == 1) {
                showPpWindow(mMenuDatas.get(position).getMenuList(), meunText2);
            } else if (position == 2) {
                showPpWindow(mMenuDatas.get(position).getMenuList(), meunText3);
            }
            darkenBackground(0.6f);
        } else {
            String url = mMenuDatas.get(position).getUrl();
            try {
                url = HttpUtils.get().url(url).buildUrl(true, false);
            } catch (Exception e) {
                // 非法url，会抛Exception
            }
            Intent intent = new Intent(getContext(), WebViewActivity.class);
            intent.putExtra(EXTRA_URL, url);
            getContext().startActivity(intent);
        }
    }

    public void setEquipment(boolean isEquipment) {
        this.isEquipment = isEquipment;
        if (mChatToolsView != null) {
            mChatToolsView.setEquipment(isEquipment);
        }
    }

    public void setGroup(boolean isGroup, String roomId, String roomJid) {
        this.isGroup = isGroup;
        this.roomId = roomId;
        this.roomJid = roomJid;
        if (mChatToolsView != null) {
            mChatToolsView.setGroup(isGroup);
        }
    }

    public void setChatBottomListener(ChatBottomListener listener) {
        mBottomListener = listener;
    }

    public void setMoreSelectMenuListener(MoreSelectMenuListener moreSelectMenuListener) {
        mMoreSelectMenuListener = moreSelectMenuListener;
    }

    public LinearLayout getmShotsLl() {
        return mShotsLl;
    }

    public EditText getmChatEdit() {
        return mChatEdit;
    }

    public void notifyAssistant() {
        if (mChatToolsView != null) {
            mChatToolsView.notifyAssistant();
        }
    }

    private void init(Context context) {
        mContext = context;
        mInputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        LayoutInflater.from(mContext).inflate(R.layout.chat_bottom, this);

        mShotsLl = (LinearLayout) findViewById(R.id.b_shots_ll);
        mShotsIv = (RoundedImageView) findViewById(R.id.b_shots_iv);
        mVoiceImgBtn = (ImageButton) findViewById(R.id.voice_img_btn);
        btnCancelReplay = (ImageButton) findViewById(R.id.btnCancelReplay);
        mChatEdit = (EditText) findViewById(R.id.chat_edit);
        mRecordBtn = (Button) findViewById(R.id.record_btn);// 按住说话
        mEmotionBtn = (ImageButton) findViewById(R.id.emotion_btn);
        mMoreBtn = (ImageButton) findViewById(R.id.more_btn);
        mSendBtn = (Button) findViewById(R.id.send_btn);
        ViewCompat.setBackgroundTintList(mSendBtn, ColorStateList.valueOf(getResources().getColor(R.color.dan_gu_lv)));
        vBottomPanel = findViewById(R.id.vBottomPanel);
        mChatFaceViewStub = findViewById(R.id.chat_face_view_stub);
        mChatToolsViewStub = findViewById(R.id.chat_tools_view_stub);

        // 主菜单
        rlChatMenu = (RelativeLayout) findViewById(R.id.rl_chat_meun);
        // 切换至公众号菜单
        flPublicChatMenu = (FrameLayout) findViewById(R.id.fl_public_menu);
        // 公众号菜单
        lLTextMenuStub = findViewById(R.id.ll_show_public_meun_stub);
        flPublicChatMenu.setVisibility(GONE);

        lLMoreSelectStub = findViewById(R.id.more_select_ll_stub);

        // 切换到公众号菜单
        flPublicChatMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextMeun();
            }
        });

        mVoiceImgBtn.setOnClickListener(this);
        btnCancelReplay.setOnClickListener(this);
        mChatEdit.setOnClickListener(this);
        mEmotionBtn.setOnClickListener(this);
        mMoreBtn.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);

        mChatEdit.setOnTouchListener((v, event) -> {
            mChatEdit.requestFocus();
            return false;
        });

        mChatEdit.setFilters(new InputFilter[]{new EmojiInputFilter(context)});
        mChatEdit.addTextChangedListener(new TextWatcher() {

            private Toast toast;

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
                if (count == 1 && start == s.length() - 1 && s.charAt(start) == '@') {
                    // 判断输入框内最后一位s为@时跳转
                    mBottomListener.sendAt();
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
                inputText();
            }

            @SuppressLint("ShowToast")
            private void toastTextLimit() {
                if (toast == null) {
                    toast = Toast.makeText(mContext, mContext.getString(R.string.tip_message_length_limit), Toast.LENGTH_LONG);
                }
                toast.show();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (replayMode) {
                    if (TextUtils.isEmpty(mChatEdit.getText().toString())) {
                        setReplay(replayMessage);
                    } else {
                        mChatEdit.setHint("");
                    }
                }
            }
        });

        mRecordController = new IMRecordController(mContext);
        mRecordController.setRecordListener(new RecordListener() {
            @Override
            public void onRecordSuccess(String filePath, int timeLen, ArrayList<String> arrayList) {
                // 录音成功，返回录音文件的路径
                mRecordBtn.setText(R.string.motalk_voice_chat_tip_1);
                mRecordBtn.setBackgroundResource(R.drawable.im_voice_button_normal2);
                if (timeLen < 1) {
                    Toast.makeText(mContext, mContext.getString(R.string.chat_timeless), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mBottomListener != null) {
                    mBottomListener.sendVoice(filePath, timeLen, arrayList);
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

        if (context instanceof Activity) {
            bindWindow(((Activity) context).getWindow());
        } else {
            throw new IllegalStateException("ChatBottomView必须传入window, 如果不能通过activity获取，就要修改相关逻辑");
        }
    }

    private void inputText() {
        if (inputState) {
            inputState = false;
            if (mBottomListener != null) {
                mBottomListener.onInputState();
            }

        } else {
            new CountDownTimer(1000, 30 * 1000) {

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    inputState = true;
                    inputText();
                }
            }.start();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        mChatEdit.setFocusable(hasWindowFocus);
        mChatEdit.setFocusableInTouchMode(hasWindowFocus);
        super.onWindowFocusChanged(hasWindowFocus);
    }

    /**
     * 改变屏幕背景色
     */
    public void darkenBackground(Float alpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = alpha;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(lp);
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

    private boolean isToolsShown() {
        return mChatToolsView != null && mChatToolsView.getVisibility() != View.GONE;
    }

    /**
     * 改变更多按钮的状态<br/>
     * 1、当更多布局显示时，显示隐藏按钮<br/>
     * false的状态 2、当更多布局隐藏时，显示更多按钮<br/>
     * true的状态
     */
    private void changeChatToolsView(boolean show) {
        boolean isShowing = isToolsShown();
        if (isShowing == show) {
            return;
        }

        if (show) {
            if (mChatToolsView == null) {
                mChatToolsView = (ChatToolsView) mChatToolsViewStub.inflate();
                mChatToolsView.init(mBottomListener, roomId, roomJid, isEquipment, isGroup, CoreManager.requireConfig(getContext()).disableLocationServer);
            }
            mChatToolsView.setVisibility(View.VISIBLE);
            mMoreBtn.setBackgroundResource(R.mipmap.unfold_icon);
        } else {
            mChatToolsView.setVisibility(View.GONE);
            mMoreBtn.setBackgroundResource(R.mipmap.show_one_icon);
        }
    }

    private boolean isFaceShown() {
        return mChatFaceView != null && mChatFaceView.getVisibility() != View.GONE;
    }

    /**
     * 显示或隐藏表情布局
     */
    private void changeChatFaceView(boolean show) {
        boolean isShowing = isFaceShown();
        if (isShowing == show) {
            return;
        }
        if (show) {
            if (mChatFaceView == null) {
                mChatFaceView = (ChatFaceView) mChatFaceViewStub.inflate();
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
            mChatFaceView.setVisibility(View.VISIBLE);
            androidx.core.view.ViewCompat.setBackgroundTintList(mEmotionBtn, ColorStateList.valueOf(getResources().getColor(R.color.dan_gu_lv)));
        } else {
            mChatFaceView.setVisibility(View.GONE);
            androidx.core.view.ViewCompat.setBackgroundTintList(mEmotionBtn, null);
        }
    }

    @Override
    public void onClick(View v) {
        if (mChatToolsView != null && mChatToolsView.isGroupAssistant()) {
            mChatToolsView.changeGroupAssistant();
            if (v.getId() != R.id.chat_edit) {
                return;
            }
        }
        if (v.getId() == R.id.send_btn) {// 发送文字与戳一戳，不受过快点击限制影响，
            if (mBottomListener != null) {
                String msg = mChatEdit.getText().toString().trim(); // 获取文本框的内容
                if (TextUtils.isEmpty(msg)) {
                    return;
                }
                //"\[([^]*)\]"
                if (msg.contains("[") && msg.contains("]")) {
                    SmileyParser parser = SmileyParser.getInstance(mContext);
                    List<String> emojiName = parser.findEmojiName(msg);
                    for (int i = 0; i < emojiName.size(); i++) {
                        InternationalizationHelper.getEmojiString(emojiName.get(i));
                    }
                }
                if (msg.contains("@")) {
                    mBottomListener.sendAtMessage(msg);
                } else {
                    mBottomListener.sendText(msg);
                }
                mChatEdit.setText("");
            }
        } else if (UiUtils.isNormalClick(v)) {
            // UiUtils.isNormalClick防止点击过快，
            switch (v.getId()) {
                /*************************** 主菜单 Event **************************/
                case R.id.voice_img_btn:
                    if (mRecordBtn.getVisibility() != View.GONE) {// 录音布局在显示,那么点击则是隐藏录音，显示键盘
                        changeRecordBtn(false);
                        // editText隐藏时无法获得焦点，所以在要visible后调用这个弹出软键盘，
                        showKeyboard();
                    } else {// 录音布局没有显示,那么点击则是显示录音，隐藏表情、更多、键盘布局
                        postHideBottomView();
                        closeKeyboard();
                        changeRecordBtn(true);
                    }
                    break;
                case R.id.btnCancelReplay:
                    resetReplay();
                    mBottomListener.cancelReplay();
                case R.id.chat_edit:// 隐藏其他所有布局，显示键盘
                    if (isBottomViewShowing()) {
                        postHideBottomView();
                    }
                    changeRecordBtn(false);
                    inputText();
                    break;
                case R.id.emotion_btn:
                    if (isBottomViewShowing() && isFaceShown()) {// 表情布局在显示,那么点击则是隐藏表情，显示键盘
                        showKeyboard();
                        postHideBottomView();
                    } else {// 表情布局没有显示,那么点击则是显示表情，隐藏键盘、录音、更多布局
                        callShowBottomView();
                        changeChatFaceView(true);
                        changeChatToolsView(false);
                        changeRecordBtn(false);
                        closeKeyboard();
                    }
                    break;
                case R.id.more_btn:
                    // 点击加号取消回复，
                    if (replayMode) {
                        resetReplay();
                        mBottomListener.cancelReplay();
                    }

                    if (isBottomViewShowing() && isToolsShown()) {// 表情布局在显示,那么点击则是隐藏表情，显示键盘
//                        showKeyboard();
                        postHideBottomView();
                        mMoreBtn.setBackgroundResource(R.mipmap.show_one_icon);
                        showMoreSelectMenu(false);
                    } else {// 更多布局没有显示,那么点击则是显示更多，隐藏表情、录音、键盘布局
                        callShowBottomView();
                        changeChatFaceView(false);
                        changeChatToolsView(true);
                        changeRecordBtn(false);
                        closeKeyboard();
                        mMoreBtn.setBackgroundResource(R.mipmap.unfold_icon);
                    }
                    String shots = PreferenceUtils.getString(mContext, Constants.SCREEN_SHOTS, "No_Shots");
                    if (!shots.equals("No_Shots")) {// 有截图
                        try {
                            File file = new File(shots);
                            mShotsLl.setVisibility(View.VISIBLE);
                            ImageLoadHelper.showFile(mContext, file, mShotsIv);

                            new CountDownTimer(5000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {

                                }

                                @Override
                                public void onFinish() {
                                    mShotsLl.setVisibility(View.GONE);
                                    PreferenceUtils.putString(mContext, Constants.SCREEN_SHOTS, "No_Shots");
                                }
                            }.start();
                        } catch (Exception e) {
                            Log.e("TAG", "截图地址异常");
                        }
                    }
                    break;

                /********** MoreSelectMenu Event *********/
                case R.id.more_select_forward_iv:
                    if (mMoreSelectMenuListener != null) {
                        mMoreSelectMenuListener.clickForwardMenu();
                    }
                    break;
                case R.id.more_select_collection_iv:
                    if (mMoreSelectMenuListener != null) {
                        mMoreSelectMenuListener.clickCollectionMenu();
                    }
                    break;
                case R.id.more_select_delete_iv:
                    if (mMoreSelectMenuListener != null) {
                        mMoreSelectMenuListener.clickDeleteMenu();
                    }
                    break;
                case R.id.more_select_email_iv:
                    if (mMoreSelectMenuListener != null) {
                        mMoreSelectMenuListener.clickEmailMenu();
                    }
                    break;
            }
        }
    }

    private void closeKeyboard() {
        mInputManager.hideSoftInputFromWindow(mChatEdit.getApplicationWindowToken(), 0);
    }

    private void showKeyboard() {
        mChatEdit.requestFocus();
        mInputManager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
    }

    public void reset() {
        postHideBottomView();
        closeKeyboard();
    }

    // 显示聊天输入框
    private void showChatBottom() {
        rlChatMenu.setVisibility(VISIBLE);
        if (lLTextMenu != null) {
            lLTextMenu.setVisibility(GONE);
        }
        if (lLMoreSelect != null) {
            lLMoreSelect.setVisibility(GONE);
        }
    }

    // 显示公众号菜单
    private void showTextMeun() {
        rlChatMenu.setVisibility(GONE);
        if (lLTextMenu != null) {
            lLTextMenu.setVisibility(VISIBLE);
        }
        if (lLMoreSelect != null) {
            lLMoreSelect.setVisibility(GONE);
        }
    }

    // 显示 || 隐藏 多选菜单
    public void showMoreSelectMenu(boolean isShow) {
        if (lLMoreSelect == null) {
            lLMoreSelect = (LinearLayout) lLMoreSelectStub.inflate();
            lLMoreSelect.findViewById(R.id.more_select_forward_iv).setOnClickListener(this);
            lLMoreSelect.findViewById(R.id.more_select_collection_iv).setOnClickListener(this);
            lLMoreSelect.findViewById(R.id.more_select_delete_iv).setOnClickListener(this);
            lLMoreSelect.findViewById(R.id.more_select_email_iv).setOnClickListener(this);
        }
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.translate_dialog_in);
        if (isShow) {
            reset();
            rlChatMenu.setVisibility(GONE);
            if (lLTextMenu != null) {
                lLTextMenu.setVisibility(GONE);
            }
            lLMoreSelect.startAnimation(animation);
            lLMoreSelect.setVisibility(VISIBLE);
        } else {
            rlChatMenu.startAnimation(animation);
            rlChatMenu.setVisibility(VISIBLE);
            if (lLTextMenu != null) {
                lLTextMenu.setVisibility(GONE);
            }
            lLMoreSelect.setVisibility(GONE);
        }
    }

    // 全员禁言
    public void isAllBanned(boolean isBanned) {
        isBanned(isBanned, R.string.hint_all_ban);
        this.isBanned = isBanned;
        if (!isBanned && isLost) {
            isBanned(true, R.string.wait_muc_member_send_chat_key_group);
        } else {
            isBanned(isBanned, R.string.hint_all_ban);
        }
    }

    // 标记是否为丢失chatKeyGroup
    public void isLostChatKeyGroup(boolean isLost) {
        isBanned(isLost, R.string.wait_muc_member_send_chat_key_group);
        this.isLost = isLost;
        if (!isLost && isBanned) {
            isBanned(true, R.string.hint_all_ban);
        } else {
            isBanned(isLost, R.string.wait_muc_member_send_chat_key_group);
        }
    }

    public void isBanned(boolean isBanned, @StringRes int hint) {
        if (isBanned) {
            rlChatMenu.setAlpha(0.5f);
            mVoiceImgBtn.setClickable(false);
            mChatEdit.setEnabled(false);
            mEmotionBtn.setClickable(false);
            mMoreBtn.setClickable(false);
            mSendBtn.setClickable(false);
            mChatEdit.setTextSize(12);
            mChatEdit.setText("");// 需要清空EditText,否则Hint不会显示出来
            mChatEdit.setHint(hint);
            mChatEdit.setGravity(Gravity.CENTER);
            reset();
        } else {
            rlChatMenu.setAlpha(1.0f);
            mVoiceImgBtn.setClickable(true);
            mChatEdit.setEnabled(true);
            mEmotionBtn.setClickable(true);
            mMoreBtn.setClickable(true);
            mSendBtn.setClickable(true);
            mChatEdit.setTextSize(15);
            mChatEdit.setHint("");
            mChatEdit.setGravity(Gravity.LEFT);
        }
    }

    public void recordCancel() {
        if (mRecordController != null) {
            mRecordController.cancel();
        }

        VoicePlayer.instance().stop();
        ChatRecordHelper.instance().reset();
    }

    public void fillRoomMenu(List<PublicMenu> datas) {
        if (lLTextMenu == null) {
            lLTextMenu = (LinearLayout) lLTextMenuStub.inflate();
            meunImg1 = lLTextMenu.findViewById(R.id.meun_left_img1);
            meunImg2 = lLTextMenu.findViewById(R.id.meun_left_img2);
            meunImg3 = lLTextMenu.findViewById(R.id.meun_left_img3);
            meunText1 = lLTextMenu.findViewById(R.id.meunText1);
            meunText2 = lLTextMenu.findViewById(R.id.meunText2);
            meunText3 = lLTextMenu.findViewById(R.id.meunText3);
            light1 = lLTextMenu.findViewById(R.id.meun_light1);
            light2 = lLTextMenu.findViewById(R.id.meun_light2);
            lLTextMenu.findViewById(R.id.ll_public_meun_1).setOnClickListener(publicMenuClickListener);
            lLTextMenu.findViewById(R.id.ll_public_meun_2).setOnClickListener(publicMenuClickListener);
            lLTextMenu.findViewById(R.id.ll_public_meun_3).setOnClickListener(publicMenuClickListener);
            // 切换到主菜单
            lLTextMenu.findViewById(R.id.fl_text_meun).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChatBottom();
                }
            });
        }
        if (datas != null && datas.size() > 0) {
            mMenuDatas = datas;
            showTextMeun();
            flPublicChatMenu.setVisibility(VISIBLE);
            switch (datas.size()) {
                case 1:
                    meunText1.setText(datas.get(0).getName());
                    findViewById(R.id.ll_public_meun_2).setVisibility(GONE);
                    findViewById(R.id.ll_public_meun_3).setVisibility(GONE);
                    light1.setVisibility(GONE);
                    light2.setVisibility(GONE);

                    if (XfileUtils.isNotEmpty(datas.get(0).getMenuList())) {
                        meunImg1.setVisibility(VISIBLE);
                    }
                    break;
                case 2:
                    meunText1.setText(datas.get(0).getName());
                    meunText2.setText(datas.get(1).getName());
                    findViewById(R.id.ll_public_meun_3).setVisibility(GONE);
                    light2.setVisibility(GONE);

                    if (XfileUtils.isNotEmpty(datas.get(0).getMenuList())) {
                        meunImg1.setVisibility(VISIBLE);
                    }

                    if (XfileUtils.isNotEmpty(datas.get(1).getMenuList())) {
                        meunImg2.setVisibility(VISIBLE);
                    }
                    break;
                default: // 不管有多少个只显示三个
                    meunText1.setText(datas.get(0).getName());
                    meunText2.setText(datas.get(1).getName());
                    meunText3.setText(datas.get(2).getName());

                    if (XfileUtils.isNotEmpty(datas.get(1).getMenuList())) {
                        meunImg2.setVisibility(VISIBLE);
                    }
                    if (XfileUtils.isNotEmpty(datas.get(0).getMenuList())) {
                        meunImg1.setVisibility(VISIBLE);
                    }
                    if (XfileUtils.isNotEmpty(datas.get(2).getMenuList())) {
                        meunImg3.setVisibility(VISIBLE);
                    }
                    break;
            }
        } else {
            flPublicChatMenu.setVisibility(GONE);
            showChatBottom();
        }
    }

    private void showPpWindow(final List<PublicMenu.MenuListBean> menuList, View view) {
        mInflater = LayoutInflater.from(getContext());
        View list = mInflater.inflate(R.layout.dialog_list_menu, null);
        MyListView listView = (MyListView) list.findViewById(R.id.dialog_menu_lv);
        listView.setAdapter(new MyMenuAdapter(menuList));

        final PopupWindow popupWindow = new PopupWindow(list, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PublicMenu.MenuListBean bean = menuList.get(position);
                String url = HttpUtils.get().url(menuList.get(position).getUrl())
                        .buildUrl(true, false);
                if (!TextUtils.isEmpty(bean.getMenuId())) {
                    // 域名+menuId+token
                    url = HttpUtils.get().url(CoreManager.requireConfig(MyApplication.getInstance()).apiUrl + bean.getMenuId())
                            .buildUrl(true, false);
                    EventBus.getDefault().post(new MessageEventRequert(url));
                    popupWindow.dismiss();
                    return;
                }
                Intent intent = new Intent(getContext(), WebViewActivity.class);
                intent.putExtra(EXTRA_URL, url);
                getContext().startActivity(intent);
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                darkenBackground(1f);
            }
        });
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        popupWindow.getContentView().measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        // +x右,-x左,+y下,-y上
        int xoff = popupWindow.getContentView().getMeasuredHeight();
        int yoff = popupWindow.getContentView().getMeasuredWidth();

        int hegiht = view.getHeight();
        int width = view.getWidth();

        width = (int) ((width - yoff) / 2.0 + 0.5f);
        popupWindow.showAsDropDown(view, width, -xoff - hegiht - 35);
    }

    public void resetReplay() {
        replayMode = false;
        mChatEdit.setHint("");
        mVoiceImgBtn.setVisibility(VISIBLE);
        btnCancelReplay.setVisibility(GONE);
    }

    public void setReplay(ChatMessage chatMessage) {
        replayMode = true;
        replayMessage = chatMessage;
        String hint = getContext().getString(R.string.replay_label) + chatMessage.getFromUserName() + ": " + chatMessage.getSimpleContent(getContext());
        mChatEdit.setHint(HtmlUtils.addSmileysToMessage(hint, false));
        mVoiceImgBtn.setVisibility(GONE);
        btnCancelReplay.setVisibility(VISIBLE);
        // 取消发送语音状态，
        changeRecordBtn(false);
    }

    private Window getWindow() {
        return window;
    }

    /**
     * 优化底部面板和软键盘切换的体验，
     * <p>
     * 总之通过控制底部表情和工具面板的父布局vBottomPanel的显示隐藏实现切换，
     * 隐藏时直接隐藏vBottomPanel，显示时显示vBottomPanel以及打开的面板，
     * <p>
     * 另外通过SoftInputMethod解决切换时跳闪的问题，
     */
    private void bindWindow(Window window) {
        this.window = window;
        KeyboardPanelUtil.updateSoftInputMethod(window, WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        KeyboardPanelUtil.setKeyboardListener(window);
        hideBottomTask = () -> {
            hideBottomView();
            KeyboardPanelUtil.updateSoftInputMethod(window, WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        };
    }

    private void postHideBottomView() {
        if (hideBottomTask == null) {
            return;
        }
        // 必须延迟一点，否则键盘会先收起布局大小会先变化，
        vBottomPanel.postDelayed(hideBottomTask, 100);
    }

    private void removeHideBottomTalk() {
        if (hideBottomTask == null) {
            return;
        }
        vBottomPanel.removeCallbacks(hideBottomTask);
    }

    private void hideBottomView() {
        vBottomPanel.setVisibility(View.GONE);
        // 这封装了个修改表情图标的操作，所以要调用，
        changeChatFaceView(false);
        // 工具面板要主动隐藏以切换按钮图标，
        changeChatToolsView(false);
    }

    private void showBottomView() {
        vBottomPanel.setVisibility(View.VISIBLE);
        int height = KeyboardPanelUtil.getKeyboardHeight(getContext());
        // 最小高度，
        int minHeight = DisplayUtil.dip2px(getContext(), 230);
        if (height < minHeight) {
            height = minHeight;
        }
        if (vBottomPanel.getHeight() != height) {
            ViewGroup.LayoutParams p = vBottomPanel.getLayoutParams();
            p.height = height;
            vBottomPanel.setLayoutParams(p);
        }
    }

    private boolean isBottomViewShowing() {
        return vBottomPanel.getVisibility() == View.VISIBLE;
    }

    private void callShowBottomView() {
        removeHideBottomTalk();
        KeyboardPanelUtil.updateSoftInputMethod(getWindow(), WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        showBottomView();
    }

    public interface ChatBottomListener {
        void sendAt();

        void sendAtMessage(String text);

        void sendText(String text);

        void sendGif(String text);

        // 发送 自定义表情
        void sendCollection(String collection);

        void sendVoice(String filePath, int timeLen, ArrayList<String> strings);

        void stopVoicePlay();

        void clickPhoto();

        void clickCamera();

        void clickAudio();

        void clickStartRecord();

        void clickLocalVideo();

        void clickVideoChat();

        default void clickScreenChat() {
        }

        default void clickTalk() {
        }

        void clickLocation();

        void clickRedpacket();

        void clickTransferMoney();

        void clickCollection();

        void clickCard();

        void clickFile();

        void clickContact();

        void clickShake();

        void clickGroupAssistant(GroupAssistantDetail groupAssistantDetail);

        void onInputState();

        default void cancelReplay() {
        }
    }

    public interface MoreSelectMenuListener {
        void clickForwardMenu();

        void clickCollectionMenu();

        void clickDeleteMenu();

        void clickEmailMenu();
    }

    class MyMenuAdapter extends BaseAdapter {

        List<PublicMenu.MenuListBean> menuList;

        public MyMenuAdapter(List<PublicMenu.MenuListBean> list) {
            menuList = list;
        }

        @Override
        public int getCount() {
            return menuList == null ? 0 : menuList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = mInflater.inflate(R.layout.item_menu_text, null);
            TextView tv = (TextView) convertView.findViewById(R.id.tv_item_number);
            tv.setText(menuList.get(position).getName());
            return convertView;
        }
    }
}
