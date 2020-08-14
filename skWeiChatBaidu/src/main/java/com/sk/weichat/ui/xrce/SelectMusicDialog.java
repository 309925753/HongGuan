package com.sk.weichat.ui.xrce;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.audio_x.VoiceManager;
import com.sk.weichat.bean.MusicInfo;
import com.sk.weichat.downloader.DownloadListener;
import com.sk.weichat.downloader.Downloader;
import com.sk.weichat.downloader.FailReason;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.ui.base.BaseRecAdapter;
import com.sk.weichat.ui.base.BaseRecViewHolder;
import com.sk.weichat.util.ScreenUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;


/**
 * 抖音音乐选择模块
 */
public class SelectMusicDialog extends Dialog implements View.OnClickListener {
    private final String mDown;
    LinearLayoutManager mLayoutManager;
    private Context mContext;
    private OnMusicItemClick mListener;
    private List<MusicInfo> mdatas;
    private List<MusicInfo> searchDatas;
    private RecyclerView mListView;
    private CommAvatarAdapter mAdapter;
    private String mToken;
    private String musicListUrl;
    private TextView hint;
    private int mPagerIndex;
    private boolean isLoad;
    private int mCurrPlay = -1; // 当前选择的索引
    private MusicInfo mCurrMusic;
    private EditText mEditText;
    private boolean more = true; // 是否可加载更多
    private Timer SEARCH_TIMER;
    private SearchTimerTask mSearchTask;
    private boolean isSearch; // 是否正在搜索
    TextWatcher changeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                mAdapter.setNewData(null);
                startSearchTimer(s.toString());
            } else {
                cancelSearchTimer();
                mAdapter.setNewData(mdatas);
                hint.setVisibility(View.GONE);
                more = true;
            }
        }
    };

    public SelectMusicDialog(Context context, OnMusicItemClick listener, String token, String url, String down) {
        super(context, R.style.TrillDialog);
        this.mContext = context;
        mListener = listener;
        mdatas = new ArrayList<>();
        searchDatas = new ArrayList<>();

        mToken = token;
        musicListUrl = url;
        mDown = down;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_trill_music);
        setCanceledOnTouchOutside(true);

        initView();
        initListview();

        findMusicDataList(0, null);
    }

    private void initListview() {
        mListView = findViewById(R.id.rv_comm);

        mLayoutManager = new LinearLayoutManager(mContext);
        mListView.setLayoutManager(mLayoutManager);
        mAdapter = new CommAvatarAdapter(mdatas);
        mListView.setAdapter(mAdapter);

        addListener();
    }

    private void initView() {
        hint = findViewById(R.id.tv_null_tip);
        mEditText = findViewById(R.id.search_edit);
        findViewById(R.id.iv_close).setOnClickListener(this);

        mEditText.addTextChangedListener(changeListener);
        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        lp.width = ScreenUtil.getScreenWidth(getContext());
        lp.height = ScreenUtil.getScreenHeight(getContext());
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.BOTTOM);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_close) {
            this.dismiss();
            VoiceManager.instance().stop();
        }
    }

    private void playMusic(int position) {
        List<MusicInfo> data = isSearch ? searchDatas : mdatas;
        if (!isSearch && mCurrPlay == position) {
            MusicInfo clickMusic = data.get(position);
            if (clickMusic.state == 1) {
                VoiceManager.instance().stop();
                clickMusic.state = 3;
                mAdapter.notifyItemChanged(position);
                return;
            } else if (clickMusic.state == 3) {
                // 暂停中就正常播放，
            } else {
                return;
            }
        }

        VoiceManager.instance().stop();

        if (mCurrPlay > -1 && data.size() > mCurrPlay) {
            data.get(mCurrPlay).state = 0;
            mAdapter.notifyItemChanged(mCurrPlay);
        }

        mCurrPlay = position;
        mCurrMusic = data.get(position);

        MusicInfo info = data.get(position);

        // 去下载路径找
        Downloader.getInstance().init(MyApplication.getInstance().mFilesDir);
        File file = Downloader.getInstance().getFile(info.getPath());

        Log.e("xuan", "down : " + info.getPath());
        if (file.exists()) {
            data.get(mCurrPlay).state = 1;
            mAdapter.notifyItemChanged(mCurrPlay);

            VoiceManager.instance().play(file);
        } else {
            data.get(mCurrPlay).state = 2;
            mAdapter.notifyItemChanged(mCurrPlay);
            Downloader.getInstance().addDownload(info.getPath(), new DownloadListener() {
                @Override
                public void onStarted(String uri, View view) {
                    Log.e("xuan", "开始下载");
                }

                @Override
                public void onFailed(String uri, FailReason failReason, View view) {
                    Log.e("xuan", "onFailed: 下载失败" + mCurrMusic.getPath());
                    Toast.makeText(mContext, mContext.getString(R.string.tip_file_download_failed), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onComplete(String uri, String filePath, View view) {
                    Log.e("xuan", "comp: " + filePath);
                    if (Downloader.getInstance().getFile(mCurrMusic.getPath()).getAbsolutePath().equals(filePath)) {
                        mCurrMusic.setPath(filePath);
                        VoiceManager.instance().play(filePath);
                        data.get(mCurrPlay).state = 1;
                        mAdapter.notifyItemChanged(mCurrPlay);
                    }
                }

                @Override
                public void onCancelled(String uri, View view) {
                    Log.e("xuan", "取消下载");
                }
            });
        }
    }

    private void findMusicDataList(int index, String keyword) {
        Log.e("xuan", "findMusicDataList: 加载数据" + index);
        isLoad = true;

        Map<String, String> params = new HashMap<>();
        params.put("access_token", mToken);
        params.put("pageIndex", String.valueOf(index));
        params.put("pageSize", String.valueOf(20));
        if (!TextUtils.isEmpty(keyword)) {
            params.put("keyword", keyword);
        }

        HttpUtils.get().url(musicListUrl)
                .params(params)
                .build()
                .execute(new ListCallback<MusicInfo>(MusicInfo.class) {
                    @Override
                    public void onResponse(ArrayResult<MusicInfo> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            List<MusicInfo> data = result.getData();
                            if (data.size() > 0) {
                                for (int i = 0; i < data.size(); i++) {
                                    MusicInfo info = data.get(i);
                                    info.appendDown(mDown);
                                }

                                if (isSearch) {
                                    searchDatas.addAll(data);

                                    mAdapter.setNewData(searchDatas);
                                } else {
                                    mdatas.addAll(data);
                                    mAdapter.setNewData(mdatas);
                                }

                                if (data.size() < 20) {
                                    more = false;
                                }
                                hint.setVisibility(View.GONE);
                            } else {
                                more = false;
                                hint.setVisibility(View.VISIBLE);
                            }
                            isLoad = false;
                        } else {
                            isLoad = false;
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        isLoad = false;
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
                    findMusicDataList(mPagerIndex, "");
                    isLoad = true;
                }
            }
        });
    }

    public void startSearchTimer(String key) {
        cancelSearchTimer();
        isSearch = true;
        searchDatas.clear();
        SEARCH_TIMER = new Timer();
        mSearchTask = new SearchTimerTask();
        mSearchTask.setKey(key);
        SEARCH_TIMER.schedule(mSearchTask, 400);
    }

    public void cancelSearchTimer() {
        isSearch = false;
        if (SEARCH_TIMER != null) {
            SEARCH_TIMER.cancel();
        }
        if (mSearchTask != null) {
            mSearchTask.cancel();
        }
    }

    public interface OnMusicItemClick {
        void onItemClick(MusicInfo info);
    }

    class CommAvatarAdapter extends BaseRecAdapter<MusicInfo, CommAvatarHolder> {
        public CommAvatarAdapter(List<MusicInfo> list) {
            super(list);
        }

        @Override
        public void onHolder(CommAvatarHolder holder, MusicInfo bean, int position) {
            holder.tvName.setText(bean.getName());
            holder.tvContent.setText(bean.getNikeName());
            ImageLoadHelper.showImageWithPlaceHolder(
                    mContext,
                    bean.getCover(),
                    R.drawable.defaultpic,
                    R.drawable.image_download_fail_icon,
                    holder.ivAvatar
            );

            if (bean.state == 0) {
                holder.mLlWrap.setVisibility(View.GONE);
                holder.ivState.setImageResource(R.drawable.ic_music_state0);
            } else if (bean.state == 1) {
                holder.mLlWrap.setVisibility(View.VISIBLE);
                holder.ivState.setImageResource(R.drawable.ic_music_state2);
            } else if (bean.state == 2) {
                holder.mLlWrap.setVisibility(View.GONE);
                holder.ivState.setImageResource(R.drawable.ic_music_state1);
            } else if (bean.state == 3) {
                holder.mLlWrap.setVisibility(View.VISIBLE);
                holder.ivState.setImageResource(R.drawable.ic_music_state0);
            }
        }

        @Override
        public CommAvatarHolder onCreateHolder() {
            return new CommAvatarHolder(getViewByRes(R.layout.item_trill_music));
        }
    }

    public class CommAvatarHolder extends BaseRecViewHolder {
        public TextView tvName;
        public TextView tvContent;
        public ImageView ivAvatar;
        public ImageView ivState;
        public LinearLayout mLlWrap;

        View.OnClickListener listener = v -> playMusic(getAdapterPosition());

        public CommAvatarHolder(View rootView) {
            super(rootView);

            tvName = rootView.findViewById(R.id.tv_name);
            tvContent = rootView.findViewById(R.id.tv_nike_name);
            ivAvatar = rootView.findViewById(R.id.iv_cover);
            mLlWrap = rootView.findViewById(R.id.ll_next);
            ivState = rootView.findViewById(R.id.iv_play_state);
            ivAvatar.setOnClickListener(listener);
            rootView.findViewById(R.id.rl_content).setOnClickListener(listener);

            mLlWrap.setOnClickListener(v -> {
                VoiceManager.instance().stop();
                SelectMusicDialog.this.dismiss();
                if (mListener != null) {

                    List<MusicInfo> data = isSearch ? searchDatas : mdatas;
                    MusicInfo info = data.get(getAdapterPosition());
                    File file = Downloader.getInstance().getFile(info.getPath());
                    if (file.exists()) { // 动态设置一下
                        info.setPath(file.getAbsolutePath());
                    }
                    mListener.onItemClick(info);
                }
            });
        }
    }

    public class SearchTimerTask extends TimerTask {
        private String keyword;

        @Override
        public void run() {
            findMusicDataList(0, keyword);
        }

        public void setKey(String key) {
            this.keyword = key;
        }
    }
}
