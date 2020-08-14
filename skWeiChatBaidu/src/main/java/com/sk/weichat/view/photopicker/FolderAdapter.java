package com.sk.weichat.view.photopicker;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.helper.ImageLoadHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件夹Adapter
 * Created by Nereo on 2015/4/7.
 */
public class FolderAdapter extends BaseAdapter {
    int mImageSize;
    int lastSelected = 0;
    private Context mContext;
    private LayoutInflater mInflater;
    private List<Folder> mFolders = new ArrayList<>();
    private int videoSize;
    private String videoPath;
    private boolean isLoadVideo;

    public FolderAdapter(Context context, boolean isLoadVideo) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageSize = mContext.getResources().getDimensionPixelOffset(R.dimen.folder_cover_size);
        this.isLoadVideo = isLoadVideo;
    }

    public int getVideoSize() {
        return videoSize;
    }

    public void setVideoSize(int videoSize) {
        this.videoSize = videoSize;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    /**
     * 设置数据集
     *
     * @param folders
     */
    public void setData(List<Folder> folders) {
        if (folders != null && folders.size() > 0) {
            mFolders = folders;
        } else {
            mFolders.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFolders.size() + 1;
    }

    @Override
    public Folder getItem(int i) {
        if (i == 0)
            return null;
        return mFolders.get(i - 1);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.item_folder, viewGroup, false);
            holder = new ViewHolder(view);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (holder != null) {
            if (i == 0) {
                holder.name.setText(mContext.getResources().getString(R.string.all_image));
                holder.size.setText(getTotalImageSize() + MyApplication.getContext().getString(R.string.page));
                if (mFolders.size() > 0) {
                    Folder f = mFolders.get(0);
                    ImageLoadHelper.showFileCenterCropWithSizeError(
                            mContext,
                            new File(f.cover.path),
                            R.mipmap.default_error,
                            mImageSize, mImageSize,
                            holder.cover
                    );
                }
            } else if (i == 1 && isLoadVideo) {
                holder.name.setText(MyApplication.getContext().getString(R.string.all_video));
                holder.size.setText(getVideoSize() + MyApplication.getContext().getString(R.string.individual));
                if (!TextUtils.isEmpty(getVideoPath())) {
                    // 可能一个视频都没有，videoPath就为空，new File会崩溃，
                    ImageLoadHelper.showFileCenterCropWithSizeError(
                            mContext,
                            new File(getVideoPath()),
                            R.mipmap.default_error,
                            mImageSize, mImageSize,
                            holder.cover
                    );
                }
            } else {
                holder.bindData(getItem(isLoadVideo ? i - 1 : i));
            }
            if (lastSelected == i) {
                holder.indicator.setVisibility(View.VISIBLE);
            } else {
                holder.indicator.setVisibility(View.INVISIBLE);
            }
        }
        return view;
    }

    private int getTotalImageSize() {
        int result = 0;
        if (mFolders != null && mFolders.size() > 0) {
            for (Folder f : mFolders) {
                result += f.images.size();
            }
        }
        return result;
    }

    public int getSelectIndex() {
        return lastSelected;
    }

    public void setSelectIndex(int i) {
        if (lastSelected == i)
            return;

        lastSelected = i;
        notifyDataSetChanged();
    }

    class ViewHolder {
        ImageView cover;
        TextView name;
        TextView size;
        ImageView indicator;

        ViewHolder(View view) {
            cover = (ImageView) view.findViewById(R.id.cover);
            name = (TextView) view.findViewById(R.id.name);
            size = (TextView) view.findViewById(R.id.size);
            indicator = (ImageView) view.findViewById(R.id.indicator);
            view.setTag(this);
        }

        void bindData(Folder data) {
            name.setText(data.name);
            size.setText(data.images.size() + MyApplication.getContext().getString(R.string.page));
            // 显示图片
            ImageLoadHelper.showFileCenterCropWithSizePlaceHolder(
                    mContext,
                    new File(data.cover.path),
                    R.mipmap.default_error,
                    R.mipmap.default_error,
                    mImageSize, mImageSize,
                    cover
            );
        }
    }

}
