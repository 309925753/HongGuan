package com.sk.weichat.ui.mucfile;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.google.android.material.tabs.TabLayout;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.DataLoadView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

import static com.sk.weichat.R.id.tv_group;


/**
 * 上传群共享专用，
 */
public class MucSelectFileDialog extends Dialog {
    private static final String TAG = "SelectFileDialog";
    public int maxOpt = 3; // 默认最大选择
    Map<String, ArrayList> imgDatas = new LinkedHashMap<>();
    Map<String, ArrayList> vidDatas = new LinkedHashMap<>();
    Map<String, ArrayList> wodDatas = new LinkedHashMap<>();
    Map<String, ArrayList> apkDatas = new LinkedHashMap<>();
    Map<String, ArrayList> txtDatas = new LinkedHashMap<>();
    private LayoutInflater mInflater;
    private Context mContext;
    private View mRootView;
    private Button mBtn;
    private TextView mTextView;
    private TabLayout mTabLayout;
    private List<Map<String, ArrayList>> mDataList = new ArrayList<>();
    private ExpandableListView expandableListView;
    private MyExpandableListView myExpandableListView;
    private Map<String, ArrayList> cuttDatas;
    private List<String> dirs = new ArrayList<>();
    private long cuttSize = 0;
    private Map<String, UpFileBean> opt = new LinkedHashMap<>();
    private OptionFileBeanListener mBeanListener;
    private DataLoadView dataLoadView;
    private SparseArray<Future<?>> taskList = new SparseArray<>(5);

    public MucSelectFileDialog(Context context, OptionFileBeanListener listener) {
        this(context, 0, LayoutInflater.from(context));
        mBeanListener = listener;
    }

    private MucSelectFileDialog(Context context, int themeResId, LayoutInflater mInflater) {
        super(context, R.style.full_dialog_style);
        this.mInflater = mInflater;
        mContext = context;

        mRootView = mInflater.inflate(R.layout.activity_add_muc_file, null, false);

        initView();
        initData();
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 原始图片大小
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            //计算最大的压缩比例值都是2的幂次方，而且宽和高大于被要求
            //的宽和高。
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public void initData() {
        dataLoadView = mRootView.findViewById(R.id.load);
        dataLoadView.setVisibility(View.GONE);
        // 默认打开一个最快的标签，图片，
        Objects.requireNonNull(mTabLayout.getTabAt(1)).select();
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            // 全部开始查询，
            query(i);
        }
    }

    private void syncQueryVideo(List<FileType> typeList) {
        syncQuery(typeList, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
    }

    private void syncQueryAudio(List<FileType> typeList) {
        syncQuery(typeList, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    }

    private void syncQueryImage(List<FileType> typeList) {
        syncQuery(typeList, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    private void syncQueryFiles(List<FileType> typeList) {
        syncQuery(typeList, MediaStore.Files.getContentUri("external"));
    }

    @WorkerThread
    private void syncQuery(List<FileType> typeList, Uri uri) {
        Log.d(TAG, "syncQueryFiles() called with: typeList = [" + typeList + "]");
        Map<String, FileType> typeMap = new HashMap<>(typeList.size());
        for (int i = 0; i < typeList.size(); i++) {
            FileType fileType = typeList.get(i);
            typeMap.put(fileType.suffix, fileType);
        }
        ContentResolver resolver = mContext.getContentResolver();
        // 要查的列
        String[] projection = {
                MediaStore.MediaColumns.DATA
        };
        // 条件
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (FileType fileType : typeList) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(" or ");
            }
            sb.append(String.format(Locale.ENGLISH, "(%s like '%%.%s' and %s > %d)", MediaStore.MediaColumns.DATA, fileType.suffix, MediaStore.MediaColumns.SIZE, fileType.limitSize));
        }
        String selection = sb.toString();
        // 排序
        String sortOrder = MediaStore.MediaColumns.DATE_MODIFIED + " desc";

        Log.d(TAG, "query: ready, " + selection);
        Cursor cursor = resolver.query(uri, projection, selection, null, sortOrder);
        if (cursor != null) {
            Log.d(TAG, "query: done");
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                // int size = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                String suffix = path.substring(path.lastIndexOf('.') + 1)
                        // 查询时是忽略大小写的，拿出来对比要转小写，
                        .toLowerCase();
                FileType fileType = typeMap.get(suffix);
                String[] dirs = path.split("/");
                String dir = dirs[dirs.length - 2];
                //noinspection unchecked
                ArrayList<UpFileBean> fs = fileType.dataMap.get(dir);
                if (fs == null) {
                    fs = new ArrayList<>();
                    fileType.dataMap.put(dir, fs);
                }

                UpFileBean bean = new UpFileBean();
                bean.type = fileType.typeId;
                bean.file = new File(path);
                if (bean.file.exists() && bean.file.length() > 0) {
                    // 系统数据库里的文件不一定真实存在，
                    fs.add(bean);
                }
            }
            cursor.close();
        }
        Log.d(TAG, "query: loaded");
    }

    private Future<?> queryAudioVideo() {
        Log.d(TAG, "queryAudioVideo() called");
        dataLoadView.showLoading();
        dataLoadView.setVisibility(View.VISIBLE);
        return AsyncUtils.doAsync(this, e -> {
            Reporter.post("查询影音失败，", e);
            dataLoadView.post(() -> {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.tip_query_audio_video_error_place_holder, e.getMessage()));
                dataLoadView.setVisibility(View.GONE);
            });
        }, context -> {
            //noinspection ArraysAsListWithZeroOrOneArgument
            List<FileType> audioTypeList = Arrays.asList(
                    new FileType("mp3", vidDatas, 2, 1024 * 1024)
            );
            syncQueryAudio(audioTypeList);
            List<FileType> videoTypeList = Arrays.asList(
                    new FileType("mp4", vidDatas, 3, 1024 * 1024 * 5),
                    new FileType("avi", vidDatas, 3, 1024 * 1024 * 10)
            );
            syncQueryVideo(videoTypeList);

            context.uiThread(ctx -> {
                onQueryDone(vidDatas);
            });
        });
    }

    private Future<?> queryImage() {
        Log.d(TAG, "queryImage() called");
        dataLoadView.showLoading();
        dataLoadView.setVisibility(View.VISIBLE);
        return AsyncUtils.doAsync(this, e -> {
            Reporter.post("查询图片失败，", e);
            dataLoadView.post(() -> {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.tip_query_image_error_place_holder, e.getMessage()));
                dataLoadView.setVisibility(View.GONE);
            });
        }, context -> {
            List<FileType> typeList = Arrays.asList(
                    new FileType("png", imgDatas, 1, 1024 * 10),
                    new FileType("jpg", imgDatas, 1, 1024 * 10)
            );

            syncQueryImage(typeList);
            context.uiThread(ctx -> {
                onQueryDone(imgDatas);
            });
        });
    }

    private Future<?> queryDocument() {
        Log.d(TAG, "queryDocument() called");
        dataLoadView.showLoading();
        dataLoadView.setVisibility(View.VISIBLE);
        return AsyncUtils.doAsync(this, e -> {
            Reporter.post("查询文档失败，", e);
            dataLoadView.post(() -> {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.tip_query_document_error_place_holder, e.getMessage()));
                dataLoadView.setVisibility(View.GONE);
            });
        }, context -> {
            List<FileType> typeList = Arrays.asList(
                    new FileType("doc", wodDatas, 6, 1024 * 10),
                    new FileType("xls", wodDatas, 5, 1024 * 10),
                    new FileType("ppt", wodDatas, 4, 1024 * 10),
                    new FileType("pdf", wodDatas, 10, 1024 * 10)
            );

            syncQueryFiles(typeList);
            context.uiThread(ctx -> {
                onQueryDone(wodDatas);
            });
        });
    }

    private Future<?> queryApp() {
        Log.d(TAG, "queryApp() called");
        dataLoadView.showLoading();
        dataLoadView.setVisibility(View.VISIBLE);
        return AsyncUtils.doAsync(this, e -> {
            Reporter.post("查询应用失败，", e);
            dataLoadView.post(() -> {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.tip_query_application_error_place_holder, e.getMessage()));
                dataLoadView.setVisibility(View.GONE);
            });
        }, context -> {
            //noinspection ArraysAsListWithZeroOrOneArgument
            List<FileType> typeList = Arrays.asList(
                    new FileType("apk", apkDatas, 11, 1024 * 100)
            );

            syncQueryFiles(typeList);
            context.uiThread(ctx -> {
                onQueryDone(apkDatas);
            });
        });
    }

    private Future<?> queryOther() {
        Log.d(TAG, "queryOther() called");
        dataLoadView.showLoading();
        dataLoadView.setVisibility(View.VISIBLE);
        return AsyncUtils.doAsync(this, e -> {
            Reporter.post("查询其他失败，", e);
            dataLoadView.post(() -> {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.tip_query_other_error_place_holder, e.getMessage()));
                dataLoadView.setVisibility(View.GONE);
            });
        }, context -> {
            List<FileType> typeList = Arrays.asList(
                    new FileType("txt", txtDatas, 8, 1024 * 10),
                    new FileType("rar", txtDatas, 7, 1024 * 20),
                    new FileType("zip", txtDatas, 7, 1024 * 20)
            );

            syncQueryFiles(typeList);
            context.uiThread(ctx -> {
                onQueryDone(txtDatas);
            });
        });
    }

    private void onQueryDone(Map<String, ArrayList> data) {
        int position = -1;
        for (int i = 0; i < mDataList.size(); i++) {
            // 不能用equals，空map会误判，
            // 同理不能用indexOf,
            //noinspection ObjectEquality
            if (mDataList.get(i) == data) {
                position = i;
            }
        }
        if (position < 0) {
            return;
        }
        if (mTabLayout.getSelectedTabPosition() == position) {
            changeDataSource(data);
        }
    }

    @MainThread
    public void query(int position) {
        Log.d(TAG, "query: begin");
        Future<?> future = taskList.get(position);
        if (future == null || future.isDone()) {
            switch (position) {
                case 0:
                    future = queryAudioVideo();
                    break;
                case 1:
                    future = queryImage();
                    break;
                case 2:
                    future = queryDocument();
                    break;
                case 3:
                    future = queryApp();
                    break;
                case 4:
                    future = queryOther();
                    break;
            }
            taskList.put(position, future);
        } else {
            Log.d(TAG, "querying: " + position);
            // 遮盖错乱，
            dataLoadView.showLoading();
            dataLoadView.setVisibility(View.VISIBLE);
        }
    }

    private void initView() {
        // 设置SelectRoomMemberPopupWindow的View
        this.setContentView(mRootView);
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        ImageView mRight = findViewById(R.id.iv_title_right);
        mRight.setImageResource(R.drawable.mgr);
        mRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBeanListener != null) {
                    mBeanListener.intent();
                }
            }
        });

        TextView mTitle = (TextView) findViewById(R.id.tv_title_center);
        mTitle.setText(mContext.getString(R.string.my_filevc_selfile));

        mBtn = (Button) mRootView.findViewById(R.id.muc_file_select_btn);
        mTextView = (TextView) mRootView.findViewById(R.id.muc_file_select_tv);
        mTabLayout = (TabLayout) mRootView.findViewById(R.id.tab1_layout);
        expandableListView = (ExpandableListView) mRootView.findViewById(R.id.el_expandableListView);
        myExpandableListView = new MyExpandableListView();
        expandableListView.setAdapter(myExpandableListView);
        mBtn.setText(mContext.getString(R.string.confirm) + "(0)");
        mTextView.setText(mContext.getString(R.string.selecting) + " 0B");

        mTabLayout.addTab(mTabLayout.newTab().setText(mContext.getString(R.string.video_audio)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mContext.getString(R.string.image)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mContext.getString(R.string.file)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mContext.getString(R.string.application)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mContext.getString(R.string.other)));
        // 按顺序排列，和tab顺序一致，
        mDataList.add(vidDatas);
        mDataList.add(imgDatas);
        mDataList.add(wodDatas);
        mDataList.add(apkDatas);
        mDataList.add(txtDatas);

        mTabLayout.setTabMode(TabLayout.MODE_FIXED);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.e("xuan", "onTabSelected: " + tab.getText());
                onTabChanged(mTabLayout.getSelectedTabPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.e("xuan", "onTabUnselected: " + tab.getText());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.e("xuan", "onTabReselected: " + tab.getText());
            }
        });

        mBtn.setEnabled(false);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBeanListener != null) {
                    // 回调还要做些什么，所以在外面dismiss,
                    mBeanListener.option(new ArrayList<>(opt.values()));
                }
            }
        });
    }

    private void onTabChanged(int position) {
        Map<String, ArrayList> data = mDataList.get(position);
        if (data.size() == 0) {
            // 空数据就重新查询，
            query(position);
        } else {
            changeDataSource(data);
        }
    }

    private void changeDataSource(Map<String, ArrayList> data) {
        dataLoadView.setVisibility(View.GONE);
        dirs = new ArrayList<>(data.keySet());
        cuttDatas = data;
        for (int i = 0; i < myExpandableListView.getGroupCount(); i++) {
            expandableListView.collapseGroup(i);
        }
        myExpandableListView.notifyDataSetChanged();
        // 还是空数据就弹Toast,
        if (cuttDatas.size() == 0) {
            Toast.makeText(mContext, mContext.getString(R.string.no_data_now), Toast.LENGTH_SHORT).show();
        }
    }

    public void fillFileIcon(int type, ImageView v, File f) {
        switch (type) {
            case 1:  // 图片
                ImageLoadHelper.showFile(
                        getContext(), f, v
                );
                break;
            case 2: // music
                v.setImageResource(R.drawable.ic_muc_flie_type_y);
                break;
            case 3: // 视屏
                v.setImageResource(R.drawable.ic_muc_flie_type_v);
                break;
            case 5: // xls
                v.setImageResource(R.drawable.ic_muc_flie_type_x);
                break;
            case 6: // doc
                v.setImageResource(R.drawable.ic_muc_flie_type_w);
                break;
            case 4: // ppt
                v.setImageResource(R.drawable.ic_muc_flie_type_p);
                break;
            case 10: // pdf
                v.setImageResource(R.drawable.ic_muc_flie_type_f);
                break;
            case 11: // apk
                v.setImageResource(R.drawable.ic_muc_flie_type_a);
                break;
            case 8: // txt
                v.setImageResource(R.drawable.ic_muc_flie_type_t);
                break;
            case 7: // rar of zip
                v.setImageResource(R.drawable.ic_muc_flie_type_z);
                break;
        }
    }

    private Bitmap decodeBitmapFromFile(String path, int width, int height) {
        // 第一步inJustDecodeBounds=true 检查尺寸大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // 计算样本尺寸
        options.inSampleSize = calculateInSampleSize(options, width, height);

        // 进行采样
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public interface OptionFileBeanListener {
        void option(List<UpFileBean> beans);

        void intent();
    }

    private static class FileType {
        String suffix;
        Map<String, ArrayList> dataMap;
        int typeId;
        long limitSize;

        FileType(String suffix, Map<String, ArrayList> dataMap, int typeId, long limitSize) {
            this.suffix = suffix;
            this.dataMap = dataMap;
            this.typeId = typeId;
            this.limitSize = limitSize;
        }
    }

    public static class UpFileBean {
        public File file;
        public int type;
        public boolean aCase = false;
    }

    //为ExpandableListView自定义适配器
    class MyExpandableListView extends BaseExpandableListAdapter {

        //返回一级列表的个数
        @Override
        public int getGroupCount() {
            return dirs.size();
        }

        //返回每个二级列表的个数
        @Override
        public int getChildrenCount(int groupPosition) { //参数groupPosition表示第几个一级列表
            Log.d("smyhvae", "-->" + groupPosition);

            return cuttDatas.get(dirs.get(groupPosition)).size();
        }

        //返回一级列表的单个item（返回的是对象）
        @Override
        public Object getGroup(int groupPosition) {
            return dirs.size();
        }

        //返回二级列表中的单个item（返回的是对象）
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return cuttDatas.get(dirs.get(groupPosition)).get(childPosition);  //不要误写成groups[groupPosition][childPosition]
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        //每个item的id是否是固定？一般为true
        @Override
        public boolean hasStableIds() {
            return true;
        }

        //【重要】填充一级列表
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_muc_file_title, null);
                holder = new GroupHolder();
                holder.tv = (TextView) convertView.findViewById(tv_group);
                convertView.setTag(holder);
            } else {
                holder = (GroupHolder) convertView.getTag();
            }

            holder.tv.setText(dirs.get(groupPosition));
            return convertView;
        }

        //【重要】填充二级列表
        @Override
        public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            FileHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_muc_file_bean, null);
                holder = new FileHolder();
                holder.linearLayout = (LinearLayout) convertView.findViewById(R.id.select_ll);
                holder.ivInco = (ImageView) convertView.findViewById(R.id.iv_file_inco);
                holder.tvName = (TextView) convertView.findViewById(R.id.tv_file_name);
                holder.tvSize = (TextView) convertView.findViewById(R.id.tv_file_size);
                holder.cbCase = (CheckBox) convertView.findViewById(R.id.cb_file_case);
                convertView.setTag(holder);
            } else {
                holder = (FileHolder) convertView.getTag();
            }

            String key = dirs.get(groupPosition);
            final ArrayList<UpFileBean> list = cuttDatas.get(key);
            File f = list.get(childPosition).file;

            if (f != null) {
                holder.tvName.setText(f.getName());
                holder.tvSize.setText(XfileUtils.fromatSize(f.length()));
                holder.cbCase.setChecked(list.get(childPosition).aCase);
                fillFileIcon(list.get(childPosition).type, holder.ivInco, f);

                holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!list.get(childPosition).aCase) {
                            if (opt.size() > maxOpt - 1) {
                                Toast.makeText(mContext, mContext.getString(R.string.select_file_count_place_holder, maxOpt), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        UpFileBean bean = list.get(childPosition);
                        if (!list.get(childPosition).aCase) {
                            cuttSize = cuttSize + bean.file.length();
                            opt.put(bean.file.getAbsolutePath(), bean);
                        } else {
                            cuttSize = cuttSize - bean.file.length();
                            opt.remove(bean.file.getAbsolutePath());
                        }

                        list.get(childPosition).aCase = !list.get(childPosition).aCase;
                        if (opt.size() > 0) {
                            mBtn.setEnabled(true);
                        } else {
                            mBtn.setEnabled(false);
                        }

                        mBtn.setText(mContext.getString(R.string.upload) + "(" + opt.size() + ")");
                        mTextView.setText(mContext.getString(R.string.selecting) + XfileUtils.fromatSize(cuttSize));

                        notifyDataSetChanged();
                    }
                });
            }

            return convertView;
        }

        // 二级列表中的item是否能够被选中？可以改为true
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }

    class GroupHolder {
        TextView tv;
    }

    class FileHolder {
        LinearLayout linearLayout;
        ImageView ivInco;
        TextView tvName;
        TextView tvSize;
        CheckBox cbCase;
    }
}
