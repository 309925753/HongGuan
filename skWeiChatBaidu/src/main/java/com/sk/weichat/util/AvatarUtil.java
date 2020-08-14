package com.sk.weichat.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import androidx.core.content.ContextCompat;

import com.sk.weichat.R;

import java.util.List;

/**
 * Describe : https://github.com/NFLeo
 * Created by Leo on 2018/12/13 on 13:57.
 */
public class AvatarUtil {

    public static Builder getBuilder(Context context) {
        return new Builder(context);
    }

    public interface Shape {
        int ROUND = 0X33;
        int CIRCLE = 0X11;
        int SQUARE = 0X22;
    }

    interface DrawPosition {
        int WHOLE = 0;
        int LEFT = 1;
        int RIGHT = 2;
        int LEFT_TOP = 3;
        int LEFT_BOTTOM = 4;
        int RIGHT_TOP = 5;
        int RIGHT_BOTTOM = 6;
    }

    public static class Builder {

        public List<Object> mList;                      // 数据源
        private Context mContext;
        private int mWidth;                         // 控件宽度
        private int mHeight;                        // 控件高度

        private int mShape = Shape.CIRCLE;               // 控件形状
        private int mRoundAngel = 10;                    // 圆角大小
        private int mMarginWidth = 4;                    // 图片间隙
        private int mMarginColor = R.color.gray;         // 图片间隙颜色
        private boolean hasEdge = true;                  // 是否包含边缘

        private float mTextSize = 50;                           // 文字大小
        private int mTextColor = R.color.colorPrimary;          // 文字颜色
        private int mBackGroundColor;     // 文字背景颜色

        private Builder(Context context) {
            this.mContext = context;
        }

        /**
         * 设置展示类型（圆形、圆角、方形）
         */
        public Builder setShape(int mShape) {
            this.mShape = mShape;
            return this;
        }

        /**
         * 设置数据源
         *
         * @param mList
         */
        public Builder setList(List<Object> mList) {
            this.mList = mList;
            return this;
        }

        /**
         * 设置图片尺寸
         */
        public Builder setBitmapSize(int mWidth, int mHeight) {
            if (mWidth > 0) {
                this.mWidth = mWidth;
            }

            if (mHeight > 0) {
                this.mHeight = mHeight;
            }
            return this;
        }


        /**
         * 设置文字大小
         */
        public Builder setTextSize(int mTextSize) {
            this.mTextSize = mTextSize;
            return this;
        }

        /**
         * 设置文字颜色
         */
        public Builder setTextColor(int mTextColor) {
            this.mTextColor = mTextColor;
            return this;
        }


        public Builder setTextBgColor(int color) {
            this.mBackGroundColor = color;
            return this;
        }

        public Bitmap create() {
            final Bitmap result = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            canvas.drawPath(drawShapePath(), paint);
            float[] marginPath;

            final int listSize = mList.size();
            switch (listSize) {
                case 1:
                    startDraw(canvas, mList.get(0), DrawPosition.WHOLE);
                    break;

            }
            // 仅方形支持边缘  且单个文字不支持边缘
            if (hasEdge && mShape == Shape.SQUARE && !(mList.size() == 1 && mList.get(0) instanceof String)) {

            }

            return result;
        }

        /**
         * 根据边角配置绘制画布path
         */
        private Path drawShapePath() {
            Path mPath = new Path();
            switch (mShape) {
                case Shape.ROUND:
                    mPath.addRoundRect(new RectF(0, 0, mHeight, mWidth), mRoundAngel, mRoundAngel, Path.Direction.CCW);
                    break;
                case Shape.SQUARE:
                    mPath.addRect(new RectF(0, 0, mHeight, mWidth), Path.Direction.CCW);
                    break;
                case Shape.CIRCLE:
                    int radius = Math.max(mWidth, mHeight) / 2;
                    mPath.addCircle(mWidth / 2, mHeight / 2, radius, Path.Direction.CCW);
                    break;
            }

            return mPath;
        }

        /**
         * 根据数据源类型区分绘制图片或文字
         */
        private void startDraw(Canvas canvas, Object resource, int position) {
            if (resource instanceof Bitmap) {
                //drawBitmap(canvas, (Bitmap) resource, position);
            } else if (resource instanceof String) {
                drawText(canvas, (String) resource, position);
            }
        }


        /**
         * 绘制文字
         */
        private void drawText(Canvas canvas, String text, int mode) {
            text = text.trim();
            float bgLeft = 0, bgTop = 0, bgRight = 0, bgBottom = 0;
            float textSize = mTextSize;

            Paint textBgPaint = new Paint();
            textBgPaint.setAntiAlias(true);
            textBgPaint.setColor(mBackGroundColor);

            textBgPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            if (mode == DrawPosition.WHOLE) {
                bgLeft = 0;
                bgTop = 0;
                bgRight = mWidth;
                bgBottom = mHeight;
                textSize = mWidth / 3;
            }

            RectF rect = new RectF(bgLeft, bgTop, bgRight, bgBottom);
            canvas.drawRect(rect, textBgPaint);

            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setAntiAlias(true);
            textPaint.setColor(ContextCompat.getColor(mContext, mTextColor));
            // textPaint.setTextSize(Math.min(mTextSize, textSize));
            textPaint.setTextSize(textSize);

            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();

            int baseline = (int) ((bgBottom + bgTop - fontMetrics.bottom - fontMetrics.top) / 2);
            if (text.length() <= 2) {
                canvas.drawText(text, rect.centerX(), baseline, textPaint);
            } else {
                canvas.drawText(text.substring(text.length() - 2, text.length()), rect.centerX(), baseline, textPaint);
            }
        }

    }

}
