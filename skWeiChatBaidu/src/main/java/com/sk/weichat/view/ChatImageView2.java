package com.sk.weichat.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.sk.weichat.R;

/**
 * 这个ImageView用作于聊天发送图片
 * 做了一个类似QQ发送图片的背景效果
 * 此控件由Liuxuan大神编写
 * 2017-7-19
 */
public class ChatImageView2 extends ImageView {
    private Paint mBitPaint;
    private int mTotalWidth;
    private int mTotalHeight;
    private RectF mSrcRect;
    private Path path;
    private int triangleY;
    private int radius;
    private boolean direction;
    private boolean mFreezesAnimation;

    public ChatImageView2(Context context) {
        this(context, null, 0);
    }

    public ChatImageView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatImageView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, new int[]{R.attr.chat_direction});
        direction = ta.getBoolean(0, false);
        initPaint();
    }

    private void initPaint() {
        mBitPaint = new Paint();
        mBitPaint.setAntiAlias(true);               //设置画笔为无锯齿
        mBitPaint.setColor(Color.parseColor("#f2f2f2"));    //设置画笔颜色  EBEBEB
        mBitPaint.setStrokeWidth(1f);             //线宽
        mBitPaint.setStyle(Paint.Style.FILL); //填充
        triangleY = 30;
        radius = 10;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        invalidate();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(path, mBitPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTotalWidth = w;
        mTotalHeight = h;

        if (direction) {
            /* 圆角矩形 */
            Path p = new Path();
            RectF oval = new RectF(0, 0, mTotalWidth - 15, mTotalHeight);
            p.addRoundRect(oval, radius, radius, Path.Direction.CCW);

            /* 三角形 */
            Path sanP = new Path();
            sanP.moveTo(mTotalWidth - 15, triangleY);
            sanP.lineTo(mTotalWidth, triangleY + 10);
            sanP.lineTo(mTotalWidth - 15, triangleY + 20);
            sanP.close();

            /* 整个ImageView矩形 */
            Path sizP = new Path();
            RectF oo = new RectF(0, 0, mTotalWidth, mTotalHeight);
            sizP.addRect(oo, Path.Direction.CCW);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                p.op(sanP, Path.Op.XOR);
                sizP.op(p, Path.Op.DIFFERENCE);
            }
            path = sizP;
        } else {
            /* 圆角矩形 */
            Path p = new Path();
            RectF oval = new RectF(15, 0, mTotalWidth, mTotalHeight);
            p.addRoundRect(oval, radius, radius, Path.Direction.CCW);

            /* 三角形 */
            Path sanP = new Path();
            sanP.moveTo(15, triangleY);
            sanP.lineTo(0, triangleY + 10);
            sanP.lineTo(15, triangleY + 20);
            sanP.close();

            /* 整个ImageView矩形 */
            Path sizP = new Path();
            RectF oo = new RectF(0, 0, mTotalWidth, mTotalHeight);
            sizP.addRect(oo, Path.Direction.CCW);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                p.op(sanP, Path.Op.XOR);
                sizP.op(p, Path.Op.DIFFERENCE);
            }
            path = sizP;
        }
    }

    /**
     * @param y 三角形箭头与顶部的距离
     */
    public void setTriangleY(int y) {
        if (y < 30) {
            triangleY = 30;
        }

        triangleY = y;
        invalidate();
    }

    public void setRadius(int r) {
        if (r > 100 || r < 5) {
            r = 10;
        }

        radius = r;
        invalidate();
    }

    public void setChatBackground(int color) {
        mBitPaint.setColor(color);

        invalidate();
    }

    /**
     * @param b true = 右边
     */
    public void setChatDirection(boolean b) {
        direction = b;

        invalidate();
    }

    public void setImageGifDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
    }
}
