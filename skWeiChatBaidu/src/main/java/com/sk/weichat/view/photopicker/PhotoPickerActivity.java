package com.sk.weichat.view.photopicker;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.VideoFile;
import com.sk.weichat.bean.event.MessageLocalVideoFile;
import com.sk.weichat.db.dao.VideoFileDao;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.base.SwipeBackActivity;
import com.sk.weichat.ui.me.LocalVideoActivity;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.photopicker.intent.PhotoPreviewIntent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;


public class PhotoPickerActivity extends SwipeBackActivity {

    public static final String TAG = PhotoPickerActivity.class.getName();
    /**
     * 图片选择模式，int类型
     */
    public static final String EXTRA_SELECT_MODE = "select_count_mode";
    /**
     * 单选
     */
    public static final int MODE_SINGLE = 0;
    /**
     * 多选
     */
    public static final int MODE_MULTI = 1;
    /**
     * 最大图片选择次数，int类型
     */
    public static final String EXTRA_SELECT_COUNT = "max_select_count";
    /**
     * 默认最大照片数量
     */
    public static final int DEFAULT_MAX_TOTAL = 6;
    /**
     * 是否显示相机，boolean类型
     */
    public static final String EXTRA_SHOW_CAMERA = "show_camera";
    /**
     * 默认选择的数据集
     */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_result";
    /**
     * 筛选照片配置信息
     */
    public static final String EXTRA_IMAGE_CONFIG = "image_config";
    /**
     * 是否显示选择原图，
     */
    public static final String EXTRA_LOAD_VIDEO = "load_video";
    /**
     * 是否加载视频
     */
    public static final String EXTRA_SHOW_ORIGINAL = "show_original";
    /**
     * 选择结果，返回为 ArrayList&lt;String&gt; 图片路径集合
     */
    public static final String EXTRA_RESULT = "select_result";
    public static final String EXTRA_RESULT_ORIGINAL = "select_result_Original";
    // 不同loader定义
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;
    private static final int REQUEST_CODE_SELECT_VIDEO = 3;
    private Context mCxt;
    // 结果数据
    private ArrayList<String> resultList = new ArrayList<>();
    // 真正的如果数据，用于返回，
    private ArrayList<String> realResultList = new ArrayList<>();
    // 是否为原图
    private boolean isOriginal;
    // 文件夹数据
    private ArrayList<Folder> mResultFolder = new ArrayList<>();
    private MenuItem menuDoneItem;
    private GridView mGridView;
    private View mPopupAnchorView;
    private Button btnAlbum;
    private Button btnPreview;
    // 最大照片数量
    private ImageCaptureManager captureManager;
    private int mDesireImageCount;
    private ImageConfig imageConfig; // 照片配置
    private List<Item> mItemList;
    // 是否加载视频
    private boolean isLoadVideo;

    private ImageGridAdapter mImageAdapter;
    private FolderAdapter mFolderAdapter;
    private ListPopupWindow mFolderPopupWindow;

    private boolean hasFolderGened = false;
    private boolean mIsShowCamera = false;
    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media._ID};

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // 根据图片设置参数新增验证条件
            StringBuilder selectionArgs = new StringBuilder();

            if (imageConfig != null) {
                if (imageConfig.minWidth != 0) {
                    selectionArgs.append(MediaStore.Images.Media.WIDTH + " >= " + imageConfig.minWidth);
                }

                if (imageConfig.minHeight != 0) {
                    selectionArgs.append("".equals(selectionArgs.toString()) ? "" : " and ");
                    selectionArgs.append(MediaStore.Images.Media.HEIGHT + " >= " + imageConfig.minHeight);
                }

                if (imageConfig.minSize != 0f) {
                    selectionArgs.append("".equals(selectionArgs.toString()) ? "" : " and ");
                    selectionArgs.append(MediaStore.Images.Media.SIZE + " >= " + imageConfig.minSize);
                }

                if (imageConfig.mimeType != null) {
                    selectionArgs.append(" and (");
                    for (int i = 0, len = imageConfig.mimeType.length; i < len; i++) {
                        if (i != 0) {
                            selectionArgs.append(" or ");
                        }
                        selectionArgs.append(MediaStore.Images.Media.MIME_TYPE + " = '" + imageConfig.mimeType[i] + "'");
                    }
                    selectionArgs.append(")");
                }
            }

            if (id == LOADER_ALL) {
                CursorLoader cursorLoader = new CursorLoader(mCxt,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        selectionArgs.toString(), null, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            } else if (id == LOADER_CATEGORY) {
                String selectionStr = selectionArgs.toString();
                if (!"".equals(selectionStr)) {
                    selectionStr += " and" + selectionStr;
                }
                CursorLoader cursorLoader = new CursorLoader(mCxt,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'" + selectionStr, null,
                        IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                List<Image> images = new ArrayList<>();
                int count = data.getCount();
                if (count > 0) {
                    data.moveToFirst();
                    do {
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(name)) {
                            // 以防万一，个别数据异常时不影响其他查询，
                            continue;
                        }
                        Image image = new Image(path, name, dateTime);
                        images.add(image);
                        if (!hasFolderGened) {
                            // 获取文件夹名称
                            File imageFile = new File(path);
                            File folderFile = imageFile.getParentFile();
                            if (folderFile == null) {
                                // 实际出现过folderFile为空导致的崩溃，原因不明，
                                continue;
                            }
                            Folder folder = new Folder();
                            folder.name = folderFile.getName();
                            folder.path = folderFile.getAbsolutePath();
                            folder.cover = image;
                            if (!mResultFolder.contains(folder)) {
                                List<Image> imageList = new ArrayList<>();
                                imageList.add(image);
                                folder.images = imageList;
                                mResultFolder.add(folder);
                            } else {
                                // 更新
                                Folder f = mResultFolder.get(mResultFolder.indexOf(folder));
                                f.images.add(image);
                            }
                        }
                    } while (data.moveToNext());

                    mImageAdapter.setData(images);

                    // 设定默认选择
                    if (resultList != null && resultList.size() > 0) {
                        mImageAdapter.setDefaultSelected(resultList);
                    }

                    mFolderAdapter.setData(mResultFolder);
                    hasFolderGened = true;

                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photopicker);

        isLoadVideo = getIntent().getBooleanExtra(EXTRA_LOAD_VIDEO, true);// 默认加载
        initViews();
        if (isLoadVideo) {
            loadData();
        }
        mItemList = new ArrayList<Item>();

        // 照片属性
        imageConfig = getIntent().getParcelableExtra(EXTRA_IMAGE_CONFIG);

        // 首次加载所有图片
        getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);

        // 选择图片数量
        mDesireImageCount = getIntent().getIntExtra(EXTRA_SELECT_COUNT, DEFAULT_MAX_TOTAL);

        // 图片选择模式
        final int mode = getIntent().getExtras().getInt(EXTRA_SELECT_MODE, MODE_SINGLE);

        // 默认选择
        if (mode == MODE_MULTI) {
            ArrayList<String> tmp = getIntent().getStringArrayListExtra(EXTRA_DEFAULT_SELECTED_LIST);
            if (tmp != null && tmp.size() > 0) {
                resultList.addAll(tmp);
            }
        }

        // 是否显示照相机
        mIsShowCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, false);
        mImageAdapter = new ImageGridAdapter(mCxt, mIsShowCamera, getItemImageWidth());
        // 是否显示选择指示器
        mImageAdapter.showSelectIndicator(mode == MODE_MULTI);
        mGridView.setAdapter(mImageAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mImageAdapter.isShowCamera()) {
                    // 如果显示照相机，则第一个Grid显示为照相机，处理特殊逻辑
                    if (i == 0) {
                        if (mode == MODE_MULTI) {
                            // 判断选择数量问题
                            if (mDesireImageCount == resultList.size()) {
                                Toast.makeText(mCxt, R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        showCameraAction();
                    } else {
                        // 正常操作
                        Image image = (Image) adapterView.getAdapter().getItem(i);
                        selectImageFromGrid(image, mode);
                    }
                } else {
                    // 正常操作
                    Image image = (Image) adapterView.getAdapter().getItem(i);
                    selectImageFromGrid(image, mode);
                }
            }
        });

        mFolderAdapter = new FolderAdapter(mCxt, isLoadVideo);

        // 打开相册列表
        btnAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFolderPopupWindow == null) {
                    createPopupFolderList();
                }

                if (mFolderPopupWindow.isShowing()) {
                    mFolderPopupWindow.dismiss();
                } else {
                    mFolderPopupWindow.show();
                    int index = mFolderAdapter.getSelectIndex();
                    if (index == 0) {
                        mFolderPopupWindow.getListView().setSelection(index);
                    } else if (index == 1) {
                        mFolderPopupWindow.getListView().setSelection(0);
                    } else {
                        index = index - 2;
                        mFolderPopupWindow.getListView().setSelection(index);
                    }
                    mFolderPopupWindow.getListView().setSelection(index);
                }
            }
        });

        boolean isShowOriginal = getIntent().getBooleanExtra(EXTRA_SHOW_ORIGINAL, true);// 默认显示
        if (!isShowOriginal) {
            findViewById(R.id.cell).setVisibility(View.INVISIBLE);
        } else {
            CheckBox mOriginalCb = findViewById(R.id.original_cb);
            mOriginalCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isOriginal = isChecked;
                }
            });
        }

        if (mode == MODE_MULTI) {
            // 预览
            btnPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PhotoPreviewIntent intent = new PhotoPreviewIntent(mCxt);
                    intent.setCurrentItem(0);
                    intent.setPhotoPaths(resultList);
                    startActivityForResult(intent, PhotoPreviewActivity.REQUEST_PREVIEW);
                }
            });
        } else {
            btnPreview.setVisibility(View.GONE);
        }

    }

    private void initViews() {
        mCxt = this;
        captureManager = new ImageCaptureManager(mCxt);
        // ActionBar Setting
        Toolbar toolbar = (Toolbar) findViewById(R.id.pickerToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.image));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGridView = (GridView) findViewById(R.id.grid);
        mGridView.setNumColumns(getNumColnums());

        mPopupAnchorView = findViewById(R.id.photo_picker_footer);
        btnAlbum = (Button) findViewById(R.id.btnAlbum);
        if (!isLoadVideo) {
            btnAlbum.setText(getString(R.string.all_image));
        }
        btnPreview = (Button) findViewById(R.id.btnPreview);
    }

    private void createPopupFolderList() {
        mFolderPopupWindow = new ListPopupWindow(mCxt);
        mFolderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mFolderPopupWindow.setAdapter(mFolderAdapter);
        mFolderPopupWindow.setContentWidth(ListPopupWindow.MATCH_PARENT);
        mFolderPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);

        // 计算ListPopupWindow内容的高度(忽略mPopupAnchorView.height)，R.layout.item_foloer
        int folderItemViewHeight =
                // 图片高度
                getResources().getDimensionPixelOffset(R.dimen.folder_cover_size) +
                        // Padding Top
                        getResources().getDimensionPixelOffset(R.dimen.folder_padding) +
                        // Padding Bottom
                        getResources().getDimensionPixelOffset(R.dimen.folder_padding);
        int folderViewHeight = mFolderAdapter.getCount() * folderItemViewHeight;

        int screenHeigh = getResources().getDisplayMetrics().heightPixels;
        if (folderViewHeight >= screenHeigh) {
            mFolderPopupWindow.setHeight(Math.round(screenHeigh * 0.6f));
        } else {
            mFolderPopupWindow.setHeight(ListPopupWindow.WRAP_CONTENT);
        }

        mFolderPopupWindow.setAnchorView(mPopupAnchorView);
        mFolderPopupWindow.setModal(true);
        mFolderPopupWindow.setAnimationStyle(R.style.Animation_AppCompat_DropDownUp);
        mFolderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mFolderAdapter.setSelectIndex(position);

                final int index = position;
                final AdapterView v = parent;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFolderPopupWindow.dismiss();
                        if (index == 0) {
                            getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
                            btnAlbum.setText(R.string.all_image);
                            mImageAdapter.setShowCamera(mIsShowCamera);
                        } else if (index == 1 && isLoadVideo) {
                            btnAlbum.setText(R.string.all_video);
                            Intent intent = new Intent(PhotoPickerActivity.this, LocalVideoActivity.class);
                            intent.putExtra(AppConstant.EXTRA_ACTION, AppConstant.ACTION_SELECT);
                            intent.putExtra(AppConstant.EXTRA_MULTI_SELECT, true);
                            startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
                        } else {
                            Folder folder = (Folder) v.getAdapter().getItem(isLoadVideo ? index - 1 : index);
                            if (null != folder) {
                                mImageAdapter.setData(folder.images);
                                btnAlbum.setText(folder.name);
                                // 设定默认选择
                                if (resultList != null && resultList.size() > 0) {
                                    mImageAdapter.setDefaultSelected(resultList);
                                }
                            }
                            mImageAdapter.setShowCamera(false);
                        }

                        // 滑动到最初始位置
                        mGridView.smoothScrollToPosition(0);
                    }
                }, 100);
            }
        });
    }

    public void onSingleImageSelected(String path) {
        Intent data = new Intent();
        resultList.add(path);
        data.putStringArrayListExtra(EXTRA_RESULT, resultList);
        data.putExtra(EXTRA_RESULT_ORIGINAL, isOriginal);
        setResult(RESULT_OK, data);
        finish();
    }

    public void onImageSelected(String path) {
        if (!resultList.contains(path)) {
            resultList.add(path);
        }
        refreshActionStatus();
    }

    public void onImageUnselected(String path) {
        if (resultList.contains(path)) {
            resultList.remove(path);
        }
        refreshActionStatus();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                // 相机拍照完成后，返回图片路径
                case ImageCaptureManager.REQUEST_TAKE_PHOTO:
                    if (captureManager.getCurrentPhotoPath() != null) {
                        captureManager.galleryAddPic();
                        resultList.add(captureManager.getCurrentPhotoPath());
                    }
                    complete();
                    break;
                // 预览照片
                case PhotoPreviewActivity.REQUEST_PREVIEW:
                    List<PhotoPagerAdapter.Item> resultItemList = JSON.parseArray(data.getStringExtra(PhotoPreviewActivity.EXTRA_RESULT), PhotoPagerAdapter.Item.class);
                    // 刷新页面
                    // 过于愚蠢，这代码没救了，realResultList是为了支持编辑功能，
                    // 如果还需要修改，建议重构Adapter和Loader，
                    ArrayList<String> pathArr = new ArrayList<>(resultItemList.size());
                    realResultList.clear();
                    for (int i = 0; i < resultItemList.size(); i++) {
                        PhotoPagerAdapter.Item item = resultItemList.get(i);
                        if (item.changed) {
                            mImageAdapter.change(item.path, item.resultPath);
                            pathArr.add(item.path);
                            realResultList.add(item.resultPath);
                        } else {
                            pathArr.add(item.path);
                        }
                    }
                    resultList = pathArr;
                    refreshActionStatus();
                    mImageAdapter.setDefaultSelected(pathArr);
                    break;
                case REQUEST_CODE_SELECT_VIDEO:
                    // 选择视频返回
                    if (data == null) {
                        return;
                    }
                    String json = data.getStringExtra(AppConstant.EXTRA_VIDEO_LIST);
                    List<VideoFile> fileList = JSON.parseArray(json, VideoFile.class);
                    if (fileList == null || fileList.size() == 0) {
                        // 不可到达，列表里有做判断，
                        Reporter.unreachable();
                    } else {
                        for (VideoFile videoFile : fileList) {
                            String filePath = videoFile.getFilePath();
                            if (TextUtils.isEmpty(filePath)) {
                                // 不可到达，列表里有做过滤，
                                Reporter.unreachable();
                            } else {
                                File file = new File(filePath);
                                if (!file.exists()) {
                                    // 不可到达，列表里有做过滤，
                                    Reporter.unreachable();
                                } else {
                                    EventBus.getDefault().post(new MessageLocalVideoFile(file));
                                }
                            }
                        }
                        PhotoPickerActivity.this.finish();
                    }
                    break;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "on change");

        // 重置列数
        mGridView.setNumColumns(getNumColnums());
        // 重置Item宽度
        mImageAdapter.setItemSize(getItemImageWidth());

        if (mFolderPopupWindow != null) {
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            }

            // 重置PopupWindow高度
            int screenHeigh = getResources().getDisplayMetrics().heightPixels;
            mFolderPopupWindow.setHeight(Math.round(screenHeigh * 0.6f));
        }

        super.onConfigurationChanged(newConfig);
    }

    /**
     * 选择相机
     */
    private void showCameraAction() {
        try {
            Intent intent = captureManager.dispatchTakePictureIntent();
            startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
        } catch (IOException e) {
            Toast.makeText(mCxt, R.string.msg_no_camera, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * 选择图片操作
     *
     * @param image
     */
    private void selectImageFromGrid(Image image, int mode) {
        if (image != null) {
            // 多选模式
            if (mode == MODE_MULTI) {
                if (resultList.contains(image.path)) {
                    resultList.remove(image.path);
                    onImageUnselected(image.path);
                } else {
                    // 判断选择数量问题
                    if (mDesireImageCount == resultList.size()) {
                        Toast.makeText(mCxt, R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    resultList.add(image.path);
                    onImageSelected(image.path);
                }
                mImageAdapter.select(image);
            } else if (mode == MODE_SINGLE) {
                // 单选模式
                onSingleImageSelected(image.path);
            }
        }
    }

    /**
     * 刷新操作按钮状态
     */
    private void refreshActionStatus() {
        String text = getString(R.string.done_with_count, resultList.size(), mDesireImageCount);
        menuDoneItem.setTitle(text);
        boolean hasSelected = resultList.size() > 0;
        menuDoneItem.setVisible(hasSelected);
        btnPreview.setEnabled(hasSelected);
        if (hasSelected) {
            btnPreview.setText(getResources().getString(R.string.preview) + "(" + resultList.size() + ")");
        } else {
            btnPreview.setText(getResources().getString(R.string.preview));
        }
    }

    /**
     * 获取GridView Item宽度
     *
     * @return
     */
    private int getItemImageWidth() {
        int cols = getNumColnums();
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
        return (screenWidth - columnSpace * (cols - 1)) / cols;
    }

    /**
     * 根据屏幕宽度与密度计算GridView显示的列数， 最少为三列
     *
     * @return
     */
    private int getNumColnums() {
        int cols = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().densityDpi;
        return cols < 3 ? 3 : cols;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picker, menu);
        menuDoneItem = menu.findItem(R.id.action_picker_done);
        menuDoneItem.setVisible(false);
        refreshActionStatus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.action_picker_done) {
            complete();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // 返回已选择的图片数据
    private void complete() {
        Intent data = new Intent();
        if (realResultList.isEmpty()) {
            data.putStringArrayListExtra(EXTRA_RESULT, resultList);
        } else {
            data.putStringArrayListExtra(EXTRA_RESULT, realResultList);
        }
        data.putExtra(EXTRA_RESULT_ORIGINAL, isOriginal);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        captureManager.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        captureManager.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void loadData() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long startTime = System.currentTimeMillis();
                    // app里录制的视频，
                    List<VideoFile> userVideos = VideoFileDao.getInstance().getVideoFiles(CoreManager.getSelf(PhotoPickerActivity.this).getUserId());
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

                    mItemList.clear();
                    for (Map.Entry<String, VideoFile> entry : uniqueMap.entrySet()) {
                        // 过滤掉异常的视频文件，系统数据库里存的数据可能过期无用，
                        if (!TextUtils.isEmpty(entry.getKey()) && new File(entry.getKey()).exists()) {
                            Item item = videoFileToItem(entry.getValue());
                            mItemList.add(item);
                        }
                    }

                    if (mItemList.size() > 0) {
                        mFolderAdapter.setVideoPath(mItemList.get(0).getFilePath());
                    }
                    mFolderAdapter.setVideoSize(mItemList.size());
                    DialogHelper.dismissProgressDialog();
                } catch (Throwable t) {
                    Reporter.post("加载视频列表失败，", t);
                    // 无论如何dialog都要取消掉，
                    DialogHelper.dismissProgressDialog();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showErrorData(PhotoPickerActivity.this);
                        }
                    });
                }
            }
        }).start();
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
            videoFile.setOwnerId(CoreManager.getSelf(PhotoPickerActivity.this).getUserId());
            list.add(videoFile);
        }
        cursor.close();
        return list;
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
        return item;
    }

    private static class Item extends VideoFile {

    }
}
