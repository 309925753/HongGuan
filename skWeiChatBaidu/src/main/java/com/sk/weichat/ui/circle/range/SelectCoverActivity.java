package com.sk.weichat.ui.circle.range;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.FileUtil;
import com.sk.weichat.util.RecorderUtils;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class SelectCoverActivity extends BaseActivity {
    private int coverCount = 6;
    private CoverAdapter adapter;
    private String currentCover;
    private int selectedIndex;
    private String thumb;
    private String out;
    private MediaMetadataRetriever mmr;
    private long time;

    public static void start(Activity ctx, int requestCode, String videoPath) {
        if (TextUtils.isEmpty(videoPath)) {
            throw new IllegalStateException("videoPath empty");
        }
        Intent intent = new Intent(ctx, SelectCoverActivity.class);
        intent.putExtra("videoPath", videoPath);
        ctx.startActivityForResult(intent, requestCode);
    }

    public static String parseResult(Intent data) {
        return data.getStringExtra("coverPath");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_cover);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.select_cover));
        TextView tvTitleRight = (TextView) findViewById(R.id.tv_title_right);
        tvTitleRight.setText(getString(R.string.finish));
        tvTitleRight.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(currentCover)) {
                Intent intent = new Intent();
                intent.putExtra("coverPath", currentCover);
                setResult(RESULT_OK, intent);
            }
            finish();
        });

        String videoPath = getIntent().getStringExtra("videoPath");

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.getItemAnimator().setChangeDuration(0);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        adapter = new CoverAdapter();
        recyclerView.setAdapter(adapter);

        DialogHelper.showMessageProgressDialog(this, getString(R.string.tip_load_cover));
        AsyncUtils.doAsync(this, t -> {
            Reporter.post("生成封面失败", t);
        }, c -> {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.prepare();
            long duration = mediaPlayer.getDuration();
            mediaPlayer.release();
            if (duration <= 16 * 1000) {
                coverCount = 16;
            } else if (duration >= 32 * 1000) {
                coverCount = 32;
            } else {
                coverCount = (int) (duration / 1000);
            }
            String out = loadCover(videoPath, duration);
            c.uiThread(r -> {
                updateCover(out);
            });
        });
    }

    private String loadCover(String inPath, long time) {
        this.time = time;
        // 帧提取器
        thumb = RecorderUtils.getThumb(inPath);
        out = RecorderUtils.getThumbPath(inPath);
        mmr = new MediaMetadataRetriever();
        mmr.setDataSource(inPath);

        DialogHelper.dismissProgressDialog();
        return out;
    }

    @Override
    protected void onDestroy() {
        if (mmr != null) {
            mmr.release();
        }
        super.onDestroy();
    }

    private void updateCover(String out) {
        List<Item> data = new ArrayList<>(coverCount);
        for (int i = 0; i < coverCount; i++) {
            Item item = new Item();
            item.coverPath = String.format(out, i);
            data.add(item);
        }
        adapter.setData(data);
        updateCurrent(0, data.get(0).coverPath);
    }

    private void updateCurrent(int index, String coverPath) {
        this.currentCover = coverPath;
        int oldIndex = selectedIndex;
        if (oldIndex == index) {
            return;
        }
        selectedIndex = index;
        adapter.selectNew(oldIndex, index);
    }

    class CoverAdapter extends RecyclerView.Adapter<CoverHolder> {
        private List<Item> data = new ArrayList<>();

        public CoverAdapter() {
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setData(List<Item> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CoverHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_video_cover, viewGroup, false);
            return new CoverHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull CoverHolder coverHolder, int i) {
            Item item = data.get(i);
            coverHolder.itemView.setOnClickListener(v -> {
                updateCurrent(i, item.coverPath);
            });
            loadImage(coverHolder.imageView, item, i);
            if (selectedIndex == i) {
                coverHolder.ivFrame.setVisibility(View.VISIBLE);
            } else {
                coverHolder.ivFrame.setVisibility(View.GONE);
            }
        }

        private void loadImage(@NonNull ImageView imageView, Item item, int index) {
            String coverPath = item.coverPath;
            imageView.setTag(R.id.key_avatar, coverPath);
            AsyncUtils.doAsync(this, t -> {
                Reporter.post("获取视频帧失败", t);
            }, c -> {
                if (item.bitmap == null || item.bitmap.get() == null) {
                    Bitmap frameBitmap = null;
                    // 加载本地图片会错误，莫名，
                    if (new File(currentCover).exists()) {
                        frameBitmap = BitmapFactory.decodeFile(coverPath);
                    }
                    if (frameBitmap == null) {
                        frameBitmap = mmr.getFrameAtTime(time / coverCount * index * 1000);
                    }
                    item.bitmap = new SoftReference<>(frameBitmap);
                    FileUtil.saveFileByBitmap(frameBitmap, thumb, coverPath);
                }
                Bitmap b = item.bitmap.get();
                c.uiThread(r -> {
                    if (imageView.getTag(R.id.key_avatar) != coverPath) {
                        return;
                    }
                    imageView.setImageBitmap(b);
                });
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        private void selectNew(int oldIndex, int index) {
            notifyItemChanged(oldIndex);
            notifyItemChanged(index);
        }
    }

    class CoverHolder extends RecyclerView.ViewHolder {
        private ImageView imageView = itemView.findViewById(R.id.imageView);
        private View ivFrame = itemView.findViewById(R.id.ivFrame);

        public CoverHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class Item {
        String coverPath;
        SoftReference<Bitmap> bitmap;
    }
}

