package com.sk.weichat.ui.live.view;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.live.bean.Member;
import com.sk.weichat.ui.other.BasicInfoActivity;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.ToastUtil;


/**
 * 直播间-修改
 */
public class FragmentEditDialog extends DialogFragment {
    public OnEditListener listener;
    /**
     * 头像
     */
    private ImageView imageIv;
    /**
     * 昵称
     */
    private TextView titleTv;
    private String roomName;
    private String roomNotice;
    private TextView tvName, tvNotice;
    private EditText etName, etNotice;
    private TextView ivEditName;
    private TextView ivNoticeName;
    private TextView positiveBn;

    private Member self;
    private Member member;
    private Dialog dialog;

    private boolean enabled1, enabled2;

    public static FragmentEditDialog newInstance(
            Member self, Member member, String roomName, String roomNotice,
            OnEditListener onEditListener
    ) {
        FragmentEditDialog fragment = new FragmentEditDialog();
        fragment.init(self, member, roomName, roomNotice, onEditListener);
        return fragment;
    }

    private void init(Member self, Member member, String roomName, String roomNotice, OnEditListener onEditListener) {
        this.self = self;
        this.member = member;
        this.roomName = roomName;
        this.roomNotice = roomNotice;
        this.listener = onEditListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_live_edit, null, false);
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
        imageIv = (ImageView) rootView.findViewById(R.id.image);
        titleTv = (TextView) rootView.findViewById(R.id.title);
        rootView.findViewById(R.id.ivClose).setOnClickListener(v -> {
            dismiss();
        });
        SkinUtils.Skin skin = SkinUtils.getSkin(requireContext());
        tvName = rootView.findViewById(R.id.tvName);
        tvName.setText(roomName);
        etName = rootView.findViewById(R.id.etName);
        etName.setText(roomName);
        tvNotice = rootView.findViewById(R.id.tvNotice);
        tvNotice.setText(roomNotice);
        etNotice = rootView.findViewById(R.id.etNotice);
        etNotice.setText(roomNotice);

        ivEditName = rootView.findViewById(R.id.ivEditName);
        ivEditName.setTextColor(skin.getAccentColor());
        ivNoticeName = rootView.findViewById(R.id.ivNoticeName);
        ivNoticeName.setTextColor(skin.getAccentColor());

        positiveBn = rootView.findViewById(R.id.positive);
        positiveBn.setTextColor(skin.getAccentColor());
    }

    private void initEvent() {
        ivEditName.setOnClickListener(v -> {
            if (!enabled1) {
                enabled1 = true;
                ivEditName.setText(R.string.save);
                ivNoticeName.setVisibility(View.GONE);
                tvName.setVisibility(View.GONE);
                etName.setVisibility(View.VISIBLE);
            } else {
                if (TextUtils.isEmpty(etName.getText())) {
                    ToastUtil.showToast(requireContext(), R.string.name_cannot_ull);
                    return;
                }
                enabled1 = false;
                ivEditName.setText(R.string.transfer_modify);
                ivNoticeName.setVisibility(View.VISIBLE);
                tvName.setVisibility(View.VISIBLE);
                etName.setVisibility(View.GONE);
                if (listener != null) {
                    tvName.setText(etName.getText().toString());
                    listener.onNameEdit(etName.getText().toString());
                }
            }
        });

        ivNoticeName.setOnClickListener(v -> {
            if (!enabled2) {
                enabled2 = true;
                ivNoticeName.setText(R.string.save);
                ivEditName.setVisibility(View.GONE);
                tvNotice.setVisibility(View.GONE);
                etNotice.setVisibility(View.VISIBLE);
            } else {
                if (TextUtils.isEmpty(etNotice.getText())) {
                    ToastUtil.showToast(requireContext(), R.string.notice_cannot_null);
                    return;
                }
                enabled2 = false;
                ivNoticeName.setText(R.string.transfer_modify);
                ivEditName.setVisibility(View.VISIBLE);
                tvNotice.setVisibility(View.VISIBLE);
                etNotice.setVisibility(View.GONE);
                if (listener != null) {
                    tvNotice.setText(etNotice.getText().toString());
                    listener.onNoticeEdit(etNotice.getText().toString());
                }
            }
        });
        positiveBn.setOnClickListener(v -> {
            dismiss();
            // 查看详情
            Intent intent = new Intent(getActivity(), BasicInfoActivity.class);
            intent.putExtra(AppConstant.EXTRA_USER_ID, String.valueOf(member.getUserId()));
            startActivity(intent);
        });
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void refreshView() {
        if (self.getType() != Member.TYPE_OWNER) {
            ivNoticeName.setVisibility(View.GONE);
            ivEditName.setVisibility(View.GONE);
        }
        AvatarHelper.getInstance().displayAvatar(String.valueOf(member.getUserId()), imageIv, false);
        titleTv.setText(member.getNickName());
    }

    public interface OnEditListener {
        void onNameEdit(String name);

        void onNoticeEdit(String notice);
    }
}
