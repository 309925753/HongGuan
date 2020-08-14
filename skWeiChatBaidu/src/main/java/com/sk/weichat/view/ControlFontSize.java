package com.sk.weichat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.sk.weichat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/6 0006.
 */

public class ControlFontSize extends View {

    private int defaultLineColor = Color.rgb(33, 33, 33);
    private int defaultLineWidth;
    private int defaultTotalCount = 5;
    private int defaultCircleColor = Color.WHITE;
    private int defaultCircleRadius;

    // 格数
    private int lattice;
    // 线条颜色
    private int lineColor;
    // 线条宽度
    private int lineWidth;
    // 线条高度
    private int lineHeight;
    // 圆半径
    private int circleRadius;
    private int circleColor;
    // 一段的宽度，根据总宽度和总格数计算得来
    private int itemWidth;
    // 控件的宽高
    private int height;
    private int width;
    // 当前所在位置
    private int currentProgress = 0;
    // 画笔
    private Paint mLinePaint;
    private Paint mCirclePaint;
    // 滑动过程中x坐标
    private float currentX = 0;
    // 有效数据点
    private List<Point> points = new ArrayList<>();

    private float circleX;
    private float circleY;

    public ControlFontSize(Context context) {
        this(context, null);
    }

    public ControlFontSize(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    private void init(Context context, AttributeSet attrs) {
        // initDefault
        defaultLineWidth = dp2px(context, 2);
        defaultCircleRadius = dp2px(context, 35);

        lineColor = Color.rgb(33, 33, 33);
        lineWidth = dp2px(context, 2);
        circleColor = Color.WHITE;

        // initCustomAttrs
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ControlFontSize);
        final int N = typedArray.getIndexCount();
        for (int i = 0; i < N; i++) {
            initCustomAttr(typedArray.getIndex(i), typedArray);
        }
        typedArray.recycle();

        // 初始化画笔
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(lineColor);
        mLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mLinePaint.setStrokeWidth(lineWidth);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(circleColor);
        mCirclePaint.setStyle(Paint.Style.FILL);
        // 阴影效果
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mCirclePaint.setShadowLayer(2, 0, 0, Color.rgb(33, 33, 33));
    }

    private void initCustomAttr(int attr, TypedArray typedArray) {
        if (attr == R.styleable.ControlFontSize_lineColor) {
            lineColor = typedArray.getColor(attr, defaultLineColor);
        } else if (attr == R.styleable.ControlFontSize_circleColor) {
            circleColor = typedArray.getColor(attr, defaultCircleColor);
        } else if (attr == R.styleable.ControlFontSize_lineWidth) {
            lineWidth = typedArray.getDimensionPixelSize(attr, defaultLineWidth);
        } else if (attr == R.styleable.ControlFontSize_circleRadius) {
            circleRadius = typedArray.getDimensionPixelSize(attr, defaultCircleRadius);
        } else if (attr == R.styleable.ControlFontSize_totalCount) {
            lattice = typedArray.getInteger(attr, defaultTotalCount);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
        width = w;
        circleY = height / 2;
        lineHeight = height / 8;
        // 横线宽度是总宽度-2个圆的半径
        itemWidth = (w - 2 * circleRadius) / lattice;
        // 把可点击点保存起来
        for (int i = 0; i <= lattice; i++) {
            points.add(new Point(circleRadius + i * itemWidth, height / 2));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 先画中间的横线
        canvas.drawLine(points.get(0).x, height / 2, points.get(points.size() - 1).x, height / 2, mLinePaint);
        // 绘制刻度
        for (Point point : points) {
            canvas.drawLine(point.x, height / 2 - lineHeight, point.x, height / 2 + lineHeight, mLinePaint);
        }
        // 画圆
        if (canMove) {
            // 随手指滑动过程
            if (currentX < circleRadius) {
                currentX = circleRadius;
            }
            if (currentX > width - circleRadius) {
                currentX = width - circleRadius;
            }
            circleX = currentX;
        } else {
            // 最终
            circleX = points.get(currentProgress).x;
        }
        // 实体圆
        canvas.drawCircle(circleX, circleY, circleRadius, mCirclePaint);
    }

    float downX = 0;
    private boolean canMove = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 判断是否是数据点
                downX = event.getX();
                canMove = isDownOnCircle(downX);
                break;
            case MotionEvent.ACTION_MOVE:
                if (canMove) {
                    currentX = event.getX();
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                // 手指抬起之后就圆就不能在非有效点
                currentX = 0;
                float upX = event.getX();
                if (canMove) {
                    // 是滑动过来的，要判断距离哪个有效点最近，就滑动到哪个点
                    Point targetPoint = getNearestPoint(upX);
                    if (targetPoint != null) {
                        invalidate();
                    }
                } else {
                    if (Math.abs(downX - upX) < 30) {
                        Point point = isValidPoint(upX);
                        if (point != null) {
                            invalidate();
                        }
                    }
                }
                if (onPointResultListener != null) {
                    onPointResultListener.onPointResult(currentProgress);
                }
                downX = 0;
                canMove = false;
                break;
        }
        return true;
    }

    /**
     * 滑动抬起之后，要滑动到最近的一个点那里
     */
    private Point getNearestPoint(float x) {
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (Math.abs(point.x - x) < itemWidth / 2) {
                currentProgress = i;
                return point;
            }
        }
        return null;
    }

    /**
     * 判断是否点击到圆上
     */
    private boolean isDownOnCircle(float x) {
        return Math.abs(points.get(currentProgress).x - x) < circleRadius;
    }

    /**
     * 判断是否是有效的点击点
     */
    private Point isValidPoint(float x) {
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (Math.abs(point.x - x) < 30) {
                currentProgress = i;
                return point;
            }
        }
        return null;
    }

    private static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public interface OnPointResultListener {
        void onPointResult(int position);
    }

    private OnPointResultListener onPointResultListener;

    public void setOnPointResultListener(OnPointResultListener onPointResultListener) {// 回调
        this.onPointResultListener = onPointResultListener;
    }
}
