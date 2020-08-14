package com.sk.weichat.ui.trill;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sk.weichat.R;
import com.sk.weichat.bean.User;
import com.sk.weichat.bean.circle.Comment;
import com.sk.weichat.fragment.TrillFragment;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.ui.base.BaseRecAdapter;
import com.sk.weichat.ui.base.BaseRecViewHolder;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.ScreenUtil;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.TrillCommentInputDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;


/**
 * 抖音视频评论模块
 */
public class TrillCommDialog extends BottomSheetDialogFragment {
    private OnUpdateCommListener mListener;
    private List<Comment> mCommentData;
    private String mToken;
    private User mLoginUser;
    private String mCommAddUrl;
    private String mMessageId;

    private String mReplyId;
    private String mReplyName;

    private RecyclerView mListView;
    private CommAvatarAdapter mAdapter;
    private TextView tvTitle;
    private TextView tvTip;
    private int mPagerIndex, mPagerSize = 20;

    private TrillCommentInputDialog.OnSendCommentListener mOnSendCommentListener = new TrillCommentInputDialog.OnSendCommentListener() {
        @Override
        public void sendComment(String str) {
            if (TrillFragment.OPEN_COMM) {
                addComment(str);
            } else {
                ToastUtil.showToast(getContext(), R.string.tip_comment_disabled);
            }
        }
    };
    private LinearLayoutManager mLayoutManager;
    private boolean more, isLoad;

    public static TrillCommDialog getInstance() {
        TrillCommDialog trillCommDialog = new TrillCommDialog();
        Bundle bundle = new Bundle();
        trillCommDialog.setArguments(bundle);
        return trillCommDialog;
    }

    public void setOnUpdateCommListener(Context context, List<Comment> data, String token, User user, String url, String msgid, OnUpdateCommListener listener) {
        mCommentData = data;
        if (mCommentData == null) {
            mCommentData = new ArrayList<>();
        }
        mToken = token;
        mLoginUser = user;
        mCommAddUrl = url;
        mMessageId = msgid;
        mListener = listener;
    }

    private void showInputDialog(String text) {
        // 弹出评论输入框
        TrillCommentInputDialog trillCommentInputDialog = new TrillCommentInputDialog(getActivity(), text, mOnSendCommentListener);
        Window window = trillCommentInputDialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); // 软键盘弹起
        }
        trillCommentInputDialog.show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 给dialog设置主题为透明背景 不然会有默认的白色背景
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDialog);
    }

    /*
    不进行onStart内的操作，dialog会显示不全
    网上一大堆解决方法都不靠谱，这样处理就可以了
     */
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();

        if (dialog != null) {
            View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
            // Cannot set MATCH_PARENT, if Settings, dialog can move up
            // bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            bottomSheet.getLayoutParams().height = ScreenUtil.getScreenHeight(requireContext()) * 2 / 3;
        }
        final View view = getView();
        if (view != null)
            view.post(() -> {
                View parent = (View) view.getParent();
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) (parent).getLayoutParams();
                CoordinatorLayout.Behavior behavior = params.getBehavior();
                BottomSheetBehavior mBottomSheetBehavior = (BottomSheetBehavior) behavior;
                int height = ScreenUtil.getScreenHeight(requireContext()) * 2 / 3;
                if (mBottomSheetBehavior != null) {
                    mBottomSheetBehavior.setPeekHeight(height);
                }
                parent.setBackgroundColor(Color.TRANSPARENT);
            });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(true);
        // 设置使软键盘弹出的时候dialog不会被顶起
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();
        dialogWindow.setSoftInputMode(params.SOFT_INPUT_ADJUST_NOTHING);
        // 动画
        dialogWindow.setWindowAnimations(R.style.BottomDialog_Animation);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 在这里将view的高度设置为精确高度，即可屏蔽向上滑动不占全屏的手势。
        // 如果不设置高度的话 会默认向上滑动时dialog覆盖全屏
        View view = inflater.inflate(R.layout.dialog_trill_comm, container, false);
        // 如不进行该步骤，上面的setPeekHeight好像无效果
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (ScreenUtil.getScreenHeight(requireContext()) * 2 / 3)));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        tvTitle = view.findViewById(R.id.tv_title);
        tvTip = view.findViewById(R.id.tv_null_tip);
        mListView = view.findViewById(R.id.rv_comm);

        mLayoutManager = new LinearLayoutManager(requireContext());
        mListView.setLayoutManager(mLayoutManager);
        mAdapter = new CommAvatarAdapter(mCommentData);
        mListView.setAdapter(mAdapter);
        if (mCommentData.size() == 0) {
            tvTitle.setVisibility(View.GONE);
            tvTip.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setText(mCommentData.size() + " " + getString(R.string.text_comment_num));
        }

        view.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        view.findViewById(R.id.comment_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReplyId = "";
                mReplyName = "";
                showInputDialog(null);
            }
        });

        if (mCommentData.size() >= 20) {
            more = true;
            mPagerIndex = 1;
            addListener();
        }
    }

    private void addComment(final String content) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", mToken);
        params.put("messageId", mMessageId);
        if (!TextUtils.isEmpty(mReplyId)) {// 代表回复
            params.put("toUserId", mReplyId);
            params.put("toNickname", mReplyName);
            params.put("toBody", content);
        }
        params.put("body", content);

        HttpUtils.get().url(mCommAddUrl)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        if (Result.checkSuccess(requireContext(), result)) {
                            Comment comment = new Comment();
                            comment.setBody(content);
                            comment.setNickName(mLoginUser.getNickName());
                            if (!TextUtils.isEmpty(mReplyId)) {
                                comment.setToUserId(mReplyId);
                                comment.setToNickname(mReplyName);
                            }
                            comment.setTime(System.currentTimeMillis() / 1000);
                            comment.setUserId(mLoginUser.getUserId());
                            mCommentData.add(comment);
                            mListener.updateCommCount();
                            if (mCommentData.size() == 1) {// 说明之前没有评论
                                tvTitle.setVisibility(View.VISIBLE);
                                tvTip.setVisibility(View.GONE);
                            }
                            tvTitle.setText(mCommentData.size() + " " + getString(R.string.text_comment_num));
                            mAdapter.notifyDataSetChanged();
                        }
                        mReplyId = "";
                        mReplyName = "";
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Log.e("xuan", "抖音模块评论失败");
                        mReplyId = "";
                        mReplyName = "";
                    }
                });
    }

    private void addListener() {

        mListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int firstVisibleItem, visibleItemCount, totalItemCount;
            private int previousTotal = 0;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (!more) {
                    // 外界不让加载数据了
                    return;
                }
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (isLoad) {
                    if (totalItemCount > previousTotal) {
                        isLoad = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!isLoad && (totalItemCount - visibleItemCount) <= firstVisibleItem) {
                    mPagerIndex++;
                    findCommDataList(mPagerIndex);
                    isLoad = true;
                }
            }
        });
    }

    private void findCommDataList(int index) {

        Log.e("xuan", "findCommDataList: 加载数据" + index);
        isLoad = true;

        Map<String, String> params = new HashMap<>();
        params.put("access_token", mToken);
        params.put("pageIndex", String.valueOf(index));
        params.put("pageSize", String.valueOf(mPagerSize));
        params.put("messageId", mMessageId);

        String url = CoreManager.requireConfig(getContext()).MSG_COMMENT_LIST;
        HttpUtils.get().url(url)
                .params(params)
                .build()
                .execute(new ListCallback<Comment>(Comment.class) {
                    @Override
                    public void onResponse(ArrayResult<Comment> result) {
                        List<Comment> data = result.getData();
                        Log.e("xuan", "onResponse: COMMMMM " + data.size());
                        if (data.size() > 0) {
                            mCommentData.addAll(data);
                            mAdapter.notifyDataSetChanged();
                        }

                        more = data.size() == 20;
                        isLoad = false;
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        more = false;
                        isLoad = false;
                    }
                });

    }

    public interface OnUpdateCommListener {
        void updateCommCount();
    }

    class CommAvatarAdapter extends BaseRecAdapter<Comment, CommAvatarHolder> {
        public CommAvatarAdapter(List<Comment> list) {
            super(list);
        }

        @Override
        public void onHolder(CommAvatarHolder holder, Comment bean, int position) {
            String title = "@" + bean.getNickName();
            if (!TextUtils.isEmpty(bean.getToNickname())) {
                title += " " + getString(R.string.replay) + " @" + bean.getToNickname();
            }
            holder.tvName.setText(title);
            holder.tvContent.setText(bean.getBody());
            // holder.tvTime.setText(TimeUtils.getTimeMMdd(bean.getTime()));
            holder.tvTime.setText(TimeUtils.getFriendlyTimeDesc(requireContext(), bean.getTime()));// 这种转换更人性化一点
            AvatarHelper.getInstance().displayAvatar(bean.getUserId(), holder.ivAvatar, false);
        }

        @Override
        public CommAvatarHolder onCreateHolder() {
            return new CommAvatarHolder(getViewByRes(R.layout.item_trill_comm));
        }
    }

    class CommAvatarHolder extends BaseRecViewHolder {
        public ImageView ivAvatar;
        public LinearLayout mLlWrap;
        public TextView tvName;
        public TextView tvContent;
        public TextView tvTime;

        CommAvatarHolder(View rootView) {
            super(rootView);
            ivAvatar = rootView.findViewById(R.id.iv_avater);
            mLlWrap = rootView.findViewById(R.id.ll_comm);
            tvName = rootView.findViewById(R.id.tv_name);
            tvContent = rootView.findViewById(R.id.tv_content);
            tvTime = rootView.findViewById(R.id.tv_time);

            mLlWrap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onReply(getAdapterPosition());
                }
            });
        }

        void onReply(int position) {
            Comment comm = mCommentData.get(position);
            if (comm.getUserId().equals(mLoginUser.getUserId())) {// 不能回复自己
                return;
            }

            mReplyId = comm.getUserId();
            mReplyName = comm.getNickName();

            String text = getString(R.string.replay) + " @" + comm.getNickName() + ": ";
            showInputDialog(text);
        }
    }
}
