package com.sk.weichat.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.bean.event.MessageEventHongdian;
import com.sk.weichat.broadcast.OtherBroadcast;
import com.sk.weichat.course.LocalCourseActivity;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.base.EasyFragment;
import com.sk.weichat.ui.circle.BusinessCircleActivity;
import com.sk.weichat.ui.circle.SelectPicPopupWindow;
import com.sk.weichat.ui.circle.range.NewZanActivity;
import com.sk.weichat.ui.circle.range.SendAudioActivity;
import com.sk.weichat.ui.circle.range.SendFileActivity;
import com.sk.weichat.ui.circle.range.SendShuoshuoActivity;
import com.sk.weichat.ui.circle.range.SendVideoActivity;
import com.sk.weichat.ui.groupchat.SelectContactsActivity;
import com.sk.weichat.ui.live.LiveActivity;
import com.sk.weichat.ui.me.BasicInfoEditActivity;
import com.sk.weichat.ui.me.MyCollection;
import com.sk.weichat.ui.me.SettingActivity;
import com.sk.weichat.ui.me.redpacket.MyWalletActivity;
import com.sk.weichat.ui.tool.SingleImagePreviewActivity;
import com.sk.weichat.ui.trill.TrillActivity;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.UiUtils;

import de.greenrobot.event.EventBus;

public class MeFragment extends EasyFragment implements View.OnClickListener {

    private ImageView mAvatarImg;
    private TextView mNickNameTv;
    private TextView mPhoneNumTv;
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, OtherBroadcast.SYNC_SELF_DATE_NOTIFY)) {
                updateUI();
            }
        }
    };
    private SelectPicPopupWindow menuWindow;
    // 为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            if (menuWindow != null) {
                // 顶部一排按钮复用这个listener, 没有menuWindow,
                menuWindow.dismiss();
            }
            Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.btn_send_picture:
                    // 发表图文，
                    intent.setClass(getActivity(), SendShuoshuoActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_send_voice:
                    // 发表语音
                    intent.setClass(getActivity(), SendAudioActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_send_video:
                    // 发表视频
                    intent.setClass(getActivity(), SendVideoActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_send_file:
                    // 发表文件
                    intent.setClass(getActivity(), SendFileActivity.class);
                    startActivity(intent);
                    break;
                case R.id.new_comment:
                    // 最新评论&赞
                    Intent intent2 = new Intent(getActivity(), NewZanActivity.class);
                    intent2.putExtra("OpenALL", true);
                    startActivity(intent2);
                    EventBus.getDefault().post(new MessageEventHongdian(0));
                    break;
                default:
                    break;
            }
        }
    };

    public MeFragment() {
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_me;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            initTitleBackground();
            initView();
            initEvent();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mUpdateReceiver);
    }

    private void initTitleBackground() {
        SkinUtils.Skin skin = SkinUtils.getSkin(requireContext());
        findViewById(R.id.tool_bar).setBackgroundColor(skin.getAccentColor());
        findViewById(R.id.rlInfoBackground).setBackgroundColor(skin.getAccentColor());
    }

    private void initView() {
        // 关闭支付功能，隐藏我的钱包
        if (!coreManager.getConfig().enablePayModule) {
            findViewById(R.id.my_monry).setVisibility(View.GONE);
        }
        // 切换新旧两种ui对应我的页面是否显示视频会议、直播、短视频，
        if (coreManager.getConfig().newUi) {
            findViewById(R.id.ll_more).setVisibility(View.GONE);
        }

        mAvatarImg = (ImageView) findViewById(R.id.avatar_img);
        mNickNameTv = (TextView) findViewById(R.id.nick_name_tv);
        mPhoneNumTv = (TextView) findViewById(R.id.phone_number_tv);
        AvatarHelper.getInstance().displayAvatar(coreManager.getSelf().getNickName(), coreManager.getSelf().getUserId(), mAvatarImg, false);
        mNickNameTv.setText(coreManager.getSelf().getNickName());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OtherBroadcast.SYNC_SELF_DATE_NOTIFY);
        getActivity().registerReceiver(mUpdateReceiver, intentFilter);
    }

    private void initEvent() {
        findViewById(R.id.iv_title_add).setOnClickListener(v -> {
            menuWindow = new SelectPicPopupWindow(getActivity(), itemsOnClick);
            menuWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            menuWindow.showAsDropDown(v,
                    -(menuWindow.getContentView().getMeasuredWidth() - v.getWidth() / 2 - 40),
                    0);
        });

        mAvatarImg.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SingleImagePreviewActivity.class);
            intent.putExtra(AppConstant.EXTRA_IMAGE_URI, coreManager.getSelf().getUserId());
            startActivity(intent);
        });

        findViewById(R.id.info_rl).setOnClickListener(this);

        findViewById(R.id.my_monry).setOnClickListener(this);
        findViewById(R.id.my_space_rl).setOnClickListener(this);
        findViewById(R.id.my_collection_rl).setOnClickListener(this);
        findViewById(R.id.local_course_rl).setOnClickListener(this);

        findViewById(R.id.meeting_rl).setOnClickListener(this);
        findViewById(R.id.live_rl).setOnClickListener(this);
        findViewById(R.id.douyin_rl).setOnClickListener(this);

        findViewById(R.id.setting_rl).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        int id = v.getId();
        switch (id) {
            case R.id.info_rl:
                // 我的资料
                startActivityForResult(new Intent(getActivity(), BasicInfoEditActivity.class), 1);
                break;
            case R.id.my_monry:
                // 我的钱包
                MyWalletActivity.start(requireContext());
                break;
            case R.id.my_space_rl:
                // 我的动态
                Intent intent = new Intent(getActivity(), BusinessCircleActivity.class);
                intent.putExtra(AppConstant.EXTRA_CIRCLE_TYPE, AppConstant.CIRCLE_TYPE_PERSONAL_SPACE);
                startActivity(intent);
                break;
            case R.id.my_collection_rl:
                // 我的收藏
                startActivity(new Intent(getActivity(), MyCollection.class));
                break;
            case R.id.local_course_rl:
                // 我的课件
                startActivity(new Intent(getActivity(), LocalCourseActivity.class));
                break;

            case R.id.meeting_rl:
                // 视频会议
                SelectContactsActivity.startQuicklyInitiateMeeting(requireContext());
                break;
            case R.id.live_rl:
                // 我的直播
                startActivity(new Intent(getActivity(), LiveActivity.class));
                break;
            case R.id.douyin_rl:
                // 短视频
                startActivity(new Intent(getActivity(), TrillActivity.class));
                break;

            case R.id.setting_rl:
                // 设置
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 || resultCode == Activity.RESULT_OK) {// 个人资料更新了
            updateUI();
        }
    }

    /**
     * 用户的信息更改的时候，ui更新
     */
    private void updateUI() {
        if (mAvatarImg != null) {
            AvatarHelper.getInstance().displayAvatar(coreManager.getSelf().getUserId(), mAvatarImg, true);
        }
        if (mNickNameTv != null) {
            mNickNameTv.setText(coreManager.getSelf().getNickName());
        }

        if (mPhoneNumTv != null) {
            String phoneNumber = coreManager.getSelf().getTelephoneNoAreaCode();
            mPhoneNumTv.setText(phoneNumber);
        }
    }
}
