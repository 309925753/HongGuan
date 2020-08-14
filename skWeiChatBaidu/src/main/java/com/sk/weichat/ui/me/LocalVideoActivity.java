package com.sk.weichat.ui.me;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.roamer.slidelistview.SlideBaseAdapter;
import com.roamer.slidelistview.SlideListView;
import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.VideoFile;
import com.sk.weichat.bean.event.MessageVideoFile;
import com.sk.weichat.db.dao.VideoFileDao;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.ThumbnailHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.PermissionUtil;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.ViewHolder;
import com.sk.weichat.video.VideoRecorderActivity;
import com.sk.weichat.view.NoDoubleClickListener;
import com.sk.weichat.view.PullToRefreshSlideListView;
import com.sk.weichat.view.TipDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JVCideoPlayerStandardSecond;

/**
 * 本地视频选择界面
 *
 * @author Dean Tao
 * @version 1.0
 */
public class LocalVideoActivity extends BaseActivity {
    private PullToRefreshSlideListView mPullToRefreshListView;
    private List<Item> mItemList;
    private LocalVideoAdapter mAdapter;
    // 没用的标识，目前进来的都是选择视频，
    private int mAction = AppConstant.ACTION_NONE;
    // 是否支持多选，
    private boolean mMultiSelect = false;
    private Handler mHandler;
    // 右上角发送按钮显示选择数量，
    private TextView tvRight;
    private int countCurrent = 0;
    // 多选最多几个，
    private int countMax = 3;

    private static String parserFileSize(long size) {
        float temp = size / (float) 1024;
        if (temp < 1024) {
            return (int) temp + "KB";
        }
        temp = temp / 1024;
        if (temp < 1024) {
            return ((int) (temp * 100)) / (float) 100 + "M";
        }
        temp = temp / 1024;
        return ((int) (temp * 100)) / (float) 100 + "G";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            mAction = getIntent().getIntExtra(AppConstant.EXTRA_ACTION, AppConstant.ACTION_NONE);
            mMultiSelect = getIntent().getBooleanExtra(AppConstant.EXTRA_MULTI_SELECT, false);
        }
        setContentView(R.layout.layout_pullrefresh_list_slide);
        EventBus.getDefault().register(this);
        mHandler = new Handler();
        mItemList = new ArrayList<Item>();
        mAdapter = new LocalVideoAdapter(this);
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.local_video);
        tvRight = findViewById(R.id.tv_title_right);
        if (mMultiSelect) {
            tvRight.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View view) {
                    if (mAction == AppConstant.ACTION_SELECT) {
                        List<VideoFile> selectedFiles = new ArrayList<>();
                        // filePath都是判断过文件存在的，
                        for (Item item : mItemList) {
                            if (item.isSelected()) {
                                selectedFiles.add(item);
                            }
                        }
                        result(selectedFiles);
                    }
                }
            });
        } else {
            // 单选情况也就是朋友圈进来的情况，右上按钮点击录制视频，
            ImageView ivTitleRight = (ImageView) findViewById(R.id.iv_title_right);
            ivTitleRight.setImageResource(R.mipmap.more_icon);
            ivTitleRight.setOnClickListener(v -> {
                VideoRecorderActivity.start(this, true);
            });
        }
    }

    private void result(VideoFile videoFile) {
        result(Collections.singletonList(videoFile));
    }

    private void result(List<VideoFile> selectedFiles) {
        // filePath都是判断过文件存在的，
        // 这个一定大于0， 因为等于0时不显示文本，也无法点击，
        if (selectedFiles.size() > 0) {
            Intent intent = new Intent();
            intent.putExtra(AppConstant.EXTRA_VIDEO_LIST, JSON.toJSONString(selectedFiles));
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void initView() {
        boolean checkSelfPermissions = PermissionUtil.checkSelfPermissions(this, new String[]{Manifest.permission.CAMERA});
        if (!checkSelfPermissions) {
            TipDialog tipDialog = new TipDialog(this);
            tipDialog.setmConfirmOnClickListener(getString(R.string.tip_no_camera_permission), new TipDialog.ConfirmOnClickListener() {
                @Override
                public void confirm() {
                    finish();
                }
            });
            tipDialog.show();
        }

        mPullToRefreshListView = (PullToRefreshSlideListView) findViewById(R.id.pull_refresh_list);
        View emptyView = LayoutInflater.from(mContext).inflate(R.layout.layout_list_empty_view, null);
        mPullToRefreshListView.setEmptyView(emptyView);
        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);
        mPullToRefreshListView.setShowIndicator(false);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<SlideListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<SlideListView> refreshView) {
                loadData();
            }
        });

        if (!mMultiSelect) {
            // 不支持多选才初始化单击事件，
            // 多选时拦截itemView点击事件在adapter中转化成改checkBox选择状态,
            mPullToRefreshListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mAction == AppConstant.ACTION_SELECT) {
                        result(mItemList.get(position - 1));
                    }
                }
            });
        }
        mPullToRefreshListView.setAdapter(mAdapter);
        loadData();
    }

    /**
     * @return 返回是否成功添加删除选择，如果到达上限数量再选择就返回false,
     */
    private boolean onSelectedChange(boolean isSelected) {
        if (isSelected && countCurrent == countMax) {
            ToastUtil.showToast(this, getString(R.string.tip_send_video_limit, countMax));
            return false;
        }
        if (isSelected) {
            countCurrent++;
        } else {
            countCurrent--;
        }
        tvRight.setText(getString(R.string.btn_send_video_place_holder, countCurrent, countMax));
        return true;
    }

    private void loadData() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long startTime = System.currentTimeMillis();
                    // app里录制的视频，
                    List<VideoFile> userVideos = VideoFileDao.getInstance().getVideoFiles(coreManager.getSelf().getUserId());
                    // 系统相册里的视频，
                    List<VideoFile> albumVideos = videoInAlbum();
                    // 合成一个列表，去重，
                    Map<String, VideoFile> uniqueMap = new LinkedHashMap<>();
                    if (userVideos != null) {
                        Log.d(TAG, "loadData: userVideos.size = " + userVideos.size());
                        for (VideoFile videoFile : userVideos) {
                            uniqueMap.put(videoFile.getFilePath(), videoFile);
                        }
                    }
                    if (albumVideos != null) {
                        Log.d(TAG, "loadData: albumVideos.size = " + albumVideos.size());
                        for (VideoFile videoFile : albumVideos) {
                            uniqueMap.put(videoFile.getFilePath(), videoFile);
                        }
                    }
                    // 缓存旧的数据，以保存选择状态，
                    // mItemList初始也不为null,
                    Map<String, Item> oldItems = new HashMap<String, Item>();
                    for (Item item : mItemList) {
                        oldItems.put(item.getFilePath(), item);
                    }
                    mItemList.clear();
                    for (Map.Entry<String, VideoFile> entry : uniqueMap.entrySet()) {
                        // 过滤掉异常的视频文件，系统数据库里存的数据可能过期无用，
                        if (!TextUtils.isEmpty(entry.getKey()) && new File(entry.getKey()).exists()) {
                            Item item = videoFileToItem(entry.getValue());
                            Item oldItem = oldItems.get(item.getFilePath());
                            item.setSelected(oldItem != null && oldItem.isSelected());
                            mItemList.add(item);
                        }
                    }
                    Log.d(TAG, "loadData: mItemList.size = " + mItemList.size());
                    long delayTime = 200 - (startTime - System.currentTimeMillis());
                    if (delayTime < 0) {
                        delayTime = 0; // 并不需要，postDelayed可以接受负数，
                    }
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                            mPullToRefreshListView.onRefreshComplete();
                            // 第一次到这里取消了dialog并没有马上显示列表，
                            // 因为视频缩略图是在主线程生成的，内存LruCache缓存的，
                            DialogHelper.dismissProgressDialog();
                        }
                    }, delayTime);
                } catch (Throwable t) {
                    Reporter.post("加载视频列表失败，", t);
                    // 无论如何dialog都要取消掉，
                    DialogHelper.dismissProgressDialog();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showErrorData(LocalVideoActivity.this);
                        }
                    });
                }
            }
        }).start();
    }

    private Item videoFileToItem(VideoFile videoFile) {
        Item item = new Item();
        item.set_id(videoFile.get_id());
        item.setCreateTime(videoFile.getCreateTime());
        item.setDesc(videoFile.getDesc());
        item.setFileLength(videoFile.getFileLength());
        item.setFilePath(videoFile.getFilePath());
        item.setFileSize(videoFile.getFileSize());
        item.setOwnerId(videoFile.getOwnerId());
        item.setSelected(false);
        return item;
    }

    /**
     * 查系统相册中的视频，
     *
     * @return 查询失败返回null, 与空结果分开表示，
     */
    @Nullable
    private List<VideoFile> videoInAlbum() {
        String[] projection = new String[]{
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DURATION
        };
        // 只支持mp4, 据测flv可能被搜到并出现问题，
        String selection = MediaStore.Video.Media.MIME_TYPE + " = ?";
        String[] selectionArgs = new String[]{
                "video/mp4"
        };
        Cursor cursor = getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs,
                MediaStore.Video.Media.DATE_ADDED + " DESC");
        if (cursor == null) {
            return null;
        }
        List<VideoFile> list = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
            if (!new File(filePath).exists()) {
                // 系统相册数据库中可能没有及时同步，可能存在已经被删除了的视频的记录，
                continue;
            }
            Long createTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
            Long fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
            Long timeLen = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
            if (timeLen == 0) {
                continue;
            }
            VideoFile videoFile = new VideoFile();
            // 系统相册查出来的时间单位是秒，这里要的是毫秒，所以乘以一千，
            videoFile.setCreateTime(TimeUtils.f_long_2_str(1000 * createTime));
            // 系统相册查出来的时长单位是毫秒，这里要的是秒，所以除以一千，
            videoFile.setFileLength(timeLen / 1000);
            videoFile.setFileSize(fileSize);
            videoFile.setFilePath(filePath);
            videoFile.setOwnerId(coreManager.getSelf().getUserId());
            list.add(videoFile);
        }
        cursor.close();
        return list;
    }

    private boolean delete(Item item) {
        boolean success = true;
        String filePath = item.getFilePath();
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            if (file.exists()) {
                success = file.delete();
            }
        }
        if (success) {
            mItemList.remove(item);
            VideoFileDao.getInstance().deleteVideoFile(item);
            mAdapter.notifyDataSetChanged();
        }
        try {
            MediaScannerConnection.scanFile(this, new String[]{filePath},
                    null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            getContentResolver()
                                    .delete(uri, null, null);
                        }
                    });
        } catch (Exception e) {
            Reporter.post("通知系统相册已经删除了视频失败，", e);
            success = false;
        }
        return success;
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageVideoFile message) {
        VideoFile videoFile = new VideoFile();
        videoFile.setCreateTime(TimeUtils.f_long_2_str(System.currentTimeMillis()));
        videoFile.setFileLength(message.timelen);
        videoFile.setFileSize(message.length);
        videoFile.setFilePath(message.path);
        videoFile.setOwnerId(coreManager.getSelf().getUserId());
        VideoFileDao.getInstance().addVideoFile(videoFile);
        mItemList.add(0, videoFileToItem(videoFile));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!JCVideoPlayer.backPress()) {
                // 调用JCVideoPlayer.backPress()
                // true : 当前正在全屏播放视频
                // false: 当前未在全屏播放视频
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JCVideoPlayer.releaseAllVideos();
        EventBus.getDefault().unregister(this);
    }

    private String parserTimeLength(long length) {
        int intLength = (int) (length / 1000);// 毫秒级转换为秒
        int hour = intLength / 3600;
        int temp = intLength - (hour * 3600);
        int minute = temp / 60;
        temp = temp - (minute * 60);
        int second = temp;

        StringBuilder sb = new StringBuilder();
        if (hour != 0) {
            sb.append(hour < 10 ? ("0" + hour) : hour).append(getString(R.string.hour));
        }
        if (minute != 0) {
            sb.append(minute < 10 ? ("0" + minute) : minute).append(getString(R.string.minute));
        }
        if (second != 0) {
            sb.append(second < 10 ? ("0" + second) : second).append(getString(R.string.second));
        }
        return sb.toString();
    }

    private static class Item extends VideoFile {
        private boolean selected = false;

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    private class LocalVideoAdapter extends SlideBaseAdapter {
        public LocalVideoAdapter(Context context) {
            super(context);
        }

        @Override
        public int getCount() {
            return mItemList.size();
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
        @SuppressLint("SetTextI18n")
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = createConvertView(position);
            }
            final Item item = mItemList.get(position);
            final CheckBox select_cb = ViewHolder.get(convertView, R.id.select_cb);
            TextView des_tv = ViewHolder.get(convertView, R.id.des_tv);
            TextView create_time_tv = ViewHolder.get(convertView, R.id.create_time_tv);
            TextView length_tv = ViewHolder.get(convertView, R.id.length_tv);
            TextView size_tv = ViewHolder.get(convertView, R.id.size_tv);
            JVCideoPlayerStandardSecond pal = ViewHolder.get(convertView, R.id.play_video);
            TextView delete_tv = ViewHolder.get(convertView, R.id.delete_tv);
            TextView top_tv = ViewHolder.get(convertView, R.id.top_tv);
            top_tv.setVisibility(View.GONE);

            // 是否已经选择，
            select_cb.setChecked(item.isSelected());
            if (!mMultiSelect) {
                // 不支持多选就直接隐藏复选框，
                select_cb.setVisibility(View.GONE);
            } else {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 多选时点击效果为选择，
                        boolean isChecked = !item.isSelected();
                        // 通知页面更新右上角的已选择数量，
                        // 没超过数量上限才继续，
                        if (onSelectedChange(isChecked)) {
                            item.setSelected(isChecked);
                            select_cb.setChecked(item.isSelected());
                        }
                    }
                });
            }
            select_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.isPressed()) {
                        // 通知页面更新右上角的已选择数量，
                        // 没超过数量上限才继续，
                        if (onSelectedChange(isChecked)) {
                            // 判断是用户点击才生效，
                            // 否则视图复用时setChecked也会到这里，
                            item.setSelected(isChecked);
                        } else {
                            // 撤销选择，
                            buttonView.setChecked(!isChecked);
                        }
                    }
                }
            });
            /* 获取缩略图显示 */
            String videoUrl = item.getFilePath();
            ThumbnailHelper.displayVideoThumb(mContext, videoUrl, pal.thumbImageView);
            pal.setUp(videoUrl, JVCideoPlayerStandardSecond.SCREEN_LAYOUT_NORMAL, "");

            /* 其他信息 */
            String des = item.getDesc();
            if (TextUtils.isEmpty(des)) {
                des_tv.setVisibility(View.GONE);
            } else {
                des_tv.setVisibility(View.VISIBLE);
                des_tv.setText(des);
            }
            create_time_tv.setText(item.getCreateTime());
            length_tv.setText(String.valueOf(item.getFileLength()) + " " + getString(R.string.second));
            size_tv.setText(parserFileSize(item.getFileSize()));
            delete_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!delete(item)) {
                        ToastUtil.showToast(LocalVideoActivity.this, R.string.delete_failed);
                    }
                }
            });
            return convertView;
        }

        @Override
        public int getFrontViewId(int position) {
            return R.layout.row_local_video;
        }

        @Override
        public int getLeftBackViewId(int position) {
            return 0;
        }

        @Override
        public int getRightBackViewId(int position) {
            return R.layout.row_item_delete;
        }
    }
}
