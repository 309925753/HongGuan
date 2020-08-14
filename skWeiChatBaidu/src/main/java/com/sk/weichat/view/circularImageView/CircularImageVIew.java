//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.sk.weichat.view.circularImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.sk.weichat.R;
import com.sk.weichat.helper.ImageLoadHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 用于组合头像得到群组头像，
 */
public class CircularImageVIew extends View {
    protected int viewWidth;
    protected int viewHeight;
    protected ArrayList<Bitmap> bmps;
    int urlSize;
    ArrayList<Bitmap> mBitmaps;
    Map<Integer, Bitmap> mSortMap = new HashMap<Integer, Bitmap>();
    private Context mContext;

    public CircularImageVIew(Context context) {
        super(context);
        mContext = context;
    }

    public CircularImageVIew(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public CircularImageVIew(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = this.getMeasuredWidth();
        int height = this.getMeasuredHeight();
        int dimen = Math.min(width, height);
        this.setMeasuredDimension(dimen, dimen);
    }

    public void setImageBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmaps can not be Null");
        } else {
            ArrayList<Bitmap> bits = new ArrayList<>();
            bits.add(bitmap);
            this.bmps = bits;
            this.invalidate();
        }
    }

    public void setImageBitmaps(ArrayList<Bitmap> bitmaps) {
        if (bitmaps == null) {
            throw new IllegalArgumentException("bitmaps can not be Null");
        } else if (bitmaps.size() > JoinLayout.max()) {
            throw new IllegalArgumentException("bitmaps size can not be greater than " + JoinLayout.max());
        } else {
            this.bmps = bitmaps;
            this.invalidate();
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.viewWidth = w;
        this.viewHeight = h;
        this.viewWidth = this.viewHeight = Math.min(w, h);
    }

    public void onDraw(Canvas canvas) {
        if (this.viewWidth > 0 && this.viewHeight > 0) {
            JoinBitmaps.join(canvas, this.viewWidth, this.bmps, 0.15F);
        }
    }

    // 群组默认头像
    public void setImageResource(int rid) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), rid);
        setImageBitmap(bitmap);
    }

    public void addUrl(List<String> urls) {
        // 当前item项的头像个数
        urlSize = urls.size();
        mBitmaps = new ArrayList<>();
        // 这里url的顺序是对的
        for (int i = 0; i < urls.size(); i++) {
            final int ai = i;
            ImageLoadHelper.loadBitmapDontAnimateWithPlaceHolder(
                    mContext,
                    urls.get(i),
                    R.drawable.avatar_normal,
                    R.drawable.avatar_normal,
                    b -> {
                        mBitmaps.add(b);
                        mSortMap.put(ai, b);
                        sendMessage(mBitmaps.size());
                    }, e -> {
                        // 使用默认图片
                        Bitmap resource = BitmapFactory.decodeResource(getResources(), R.drawable.avatar_normal);
                        mBitmaps.add(resource);
                        mSortMap.put(ai, resource);
                        sendMessage(mBitmaps.size());
                    }
            );
        }
    }

    private void sendMessage(int bitmapSize) {
        // mBitmaps.size==urls.size
        if (bitmapSize == urlSize) {
            // 此时bitmapS的顺序并不会与urls的顺序一一对应,会导致头像闪动
            // 首先排序  然后加载图片
            Map<Integer, Bitmap> resultMap = sortMapByKey(mSortMap);  //按Key进行排序
            mBitmaps.clear();
            for (Map.Entry<Integer, Bitmap> entry : resultMap.entrySet()) {
                mBitmaps.add(entry.getValue());
            }
            setImageBitmaps(mBitmaps);
            resultMap.clear();
            mSortMap.clear();
        }
    }

    /**
     * 使用 Map按key进行排序
     *
     * @param map
     * @return
     */
    public Map<Integer, Bitmap> sortMapByKey(Map<Integer, Bitmap> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<Integer, Bitmap> sortMap = new TreeMap<Integer, Bitmap>(
                new MapKeyComparator());
        sortMap.putAll(map);
        return sortMap;
    }

    class MapKeyComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer str1, Integer str2) {
            return str1.compareTo(str2);
        }
    }
}
