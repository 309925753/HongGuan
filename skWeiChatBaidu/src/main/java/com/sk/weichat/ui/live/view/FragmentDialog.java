package com.sk.weichat.ui.live.view;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.DialogFragment;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.live.bean.Member;
import com.sk.weichat.ui.other.BasicInfoActivity;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.TimeUtils;


/**
 * 直播间-用户详情
 */
public class FragmentDialog extends DialogFragment {
    public OnClickBottomListener listener;
    /**
     * 头像
     */
    private ImageView imageIv;
    /**
     * 昵称
     */
    private TextView titleTv;
    /**
     * 取消和确认按钮
     */
    private TextView positiveBn;
    private View llManagerButton;
    private Member self;
    private Member member;
    private Dialog dialog;
    private View.OnClickListener onClickListener = v -> {
        if (listener != null) {
            listener.onManagerClick(v);
        }
        dismiss();
    };

    public static FragmentDialog newInstance(
            Member self, Member member,
            OnClickBottomListener onClickBottomListener
    ) {
        FragmentDialog fragment = new FragmentDialog();
        fragment.init(self, member, onClickBottomListener);
        return fragment;
    }

    private void init(Member self, Member member, OnClickBottomListener onClickBottomListener) {
        this.self = self;
        this.member = member;
        this.listener = onClickBottomListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.common_dialog_layout, null, false);
        // 使用不带Theme的构造器, 获得的dialog边框距离屏幕仍有几毫米的缝隙
        initDialogStyle(rootView);
        initView(rootView);
        initEvent();
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshView();
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void refreshView() {
        if (isManager()) {
            llManagerButton.setVisibility(View.VISIBLE);
        } else {
            llManagerButton.setVisibility(View.GONE);
        }
        AvatarHelper.getInstance().displayAvatar(String.valueOf(member.getUserId()), imageIv, false);
        titleTv.setText(member.getNickName());
    }

    private boolean isManager() {
        // 主播或者管理员可以对成员进行管理，
        // 主播可以对管理员进行管理，
        if (self.getType() == Member.TYPE_OWNER) {// 除自己以外均返回true
            return !TextUtils.equals(self.getUserId(), member.getUserId());
        }
        return self.getType() == Member.TYPE_MANAGER
                && member.getType() == Member.TYPE_MEMBER;// 除管理员外均返回true
    }

    private void initDialogStyle(View view) {
        dialog = new Dialog(getActivity(), R.style.CustomDialog);
        // 设置Content前设定,(自定义标题,当需要自定义标题时必须指定)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        // 外部点击取消
        dialog.setCanceledOnTouchOutside(true);
        // 设置宽度为屏宽, 靠近屏幕底部。
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.Buttom_Popwindow);
        WindowManager.LayoutParams lp = window.getAttributes();
        // 中间显示
        lp.gravity = Gravity.CENTER;
        //  宽度持平
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
    }

    private void initView(View rootView) {
        SkinUtils.Skin skin = SkinUtils.getSkin(requireContext());

        imageIv = (ImageView) rootView.findViewById(R.id.image);
        titleTv = (TextView) rootView.findViewById(R.id.title);
        positiveBn = rootView.findViewById(R.id.positive);
        positiveBn.setTextColor(skin.getAccentColor());
        llManagerButton = rootView.findViewById(R.id.llManagerButton);

        TextView shut_up = rootView.findViewById(R.id.shut_up);
        ViewCompat.setBackgroundTintList(shut_up, ColorStateList.valueOf(skin.getAccentColor()));
        shut_up.setOnClickListener(onClickListener);
        TextView kick_room = rootView.findViewById(R.id.kick_room);
        ViewCompat.setBackgroundTintList(kick_room, ColorStateList.valueOf(skin.getAccentColor()));
        kick_room.setOnClickListener(onClickListener);
        TextView set_manager = rootView.findViewById(R.id.set_manager);
        ViewCompat.setBackgroundTintList(set_manager, ColorStateList.valueOf(skin.getAccentColor()));
        set_manager.setOnClickListener(onClickListener);
        shut_up.setText(member.getTalkTime() < TimeUtils.sk_time_current_time() ? getString(R.string.live_vc_setgag) : getString(R.string.live_gag_cancel));
        set_manager.setText(member.getType() == Member.TYPE_MANAGER ? getString(R.string.cancel_admin) : getString(R.string.live_set_manager));

        rootView.findViewById(R.id.ivClose).setOnClickListener(v -> {
            dismiss();
        });
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        positiveBn.setOnClickListener(v -> {
            dismiss();
            // 查看详情
            Intent intent = new Intent(getActivity(), BasicInfoActivity.class);
            intent.putExtra(AppConstant.EXTRA_USER_ID, String.valueOf(member.getUserId()));
            startActivity(intent);
        });
    }

    public interface OnClickBottomListener {
        void onManagerClick(View v);
    }
}
