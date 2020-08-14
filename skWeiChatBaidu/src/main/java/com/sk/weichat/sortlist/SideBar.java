package com.sk.weichat.sortlist;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.util.DisplayUtil;

import java.util.HashMap;
import java.util.Map;

public class SideBar extends View {
    // 26个字母和#,首字母不是英文字母的放到#分类
    public static String[] b = {"#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
            "X", "Y", "Z"};
    // 触摸事件
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    private int choose = -1;// 选中
    private Paint paint = new Paint();

    private TextView mTextDialog;

    private Map<String, Integer> isExistMap;

    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SideBar(Context context) {
        super(context);
        init();
    }

    public void setExistMap(Map<String, Integer> existMap) {
        isExistMap = existMap;
        invalidate();
    }

    public void addExist(String alphaet) {// 存在的Count+1
        int count = 0;
        if (isExistMap.containsKey(alphaet)) {
            count = isExistMap.get(alphaet);
        }
        count++;
        isExistMap.put(alphaet, count);
    }

    public void removeExist(String alphaet) {// 存在的Count-1,存在才减1，不存在则移除
        int count = 0;
        if (isExistMap.containsKey(alphaet)) {
            count = isExistMap.get(alphaet);
        }
        if (count > 0) {
            count--;
        }
        if (count > 0) {
            isExistMap.put(alphaet, count);
        } else {
            isExistMap.remove(alphaet);
        }
    }

    public void clearExist() {
        isExistMap.clear();
    }

    public void setTextView(TextView mTextDialog) {
        this.mTextDialog = mTextDialog;
    }

    private void init() {
        isExistMap = new HashMap<String, Integer>();
        paint.setTypeface(Typeface.DEFAULT);
        paint.setAntiAlias(true);
        paint.setTextSize(35);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


        // 获取宽-测量规则的模式和大小
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        // 获取高-测量规则的模式和大小
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // 设置wrap_content的默认宽 / 高值
        // 默认宽/高的设定并无固定依据,根据需要灵活设置
        // 类似TextView,ImageView等针对wrap_content均在onMeasure()对设置默认宽 / 高值有特殊处理,具体读者可以自行查看
        int mWidth = DisplayUtil.dip2px(getContext(), 20);
        int mHeight = (paint.getFontMetricsInt(null) + DisplayUtil.dip2px(getContext(), 2)) * b.length;

        // 当布局参数设置为wrap_content时，设置默认值
        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT && getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mWidth, mHeight);
            // 宽 / 高任意一个布局参数为= wrap_content时，都设置默认值
        } else if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mWidth, heightSize);
        } else if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthSize, mHeight);
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 获取焦点改变背景颜色.
        int height = getHeight();// 获取对应高度
        int width = getWidth(); // 获取对应宽度
        int singleHeight = height / b.length;// 获取每一个字母的高度

        for (int i = 0; i < b.length; i++) {
            // 选中的状态
            if (i == choose) {
                paint.setColor(Color.parseColor("#4FC557"));
                paint.setFakeBoldText(true);
            } else {
                paint.setColor(Color.parseColor("#555555"));
                paint.setFakeBoldText(false);
            }
            // x坐标等于中间-字符串宽度的一半.
            float xPos = width / 2 - paint.measureText(b[i]) / 2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(b[i], xPos, yPos, paint);
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();// 点击y坐标
        final int oldChoose = choose;
        final int c = (int) (y / getHeight() * b.length);// 点击y坐标所占总高度的比例*b数组的长度就等于点击b中的个数.

        switch (action) {
            case MotionEvent.ACTION_UP:
                // setBackgroundDrawable(new ColorDrawable(0x00000000));
                setBackgroundDrawable(null);
                choose = -1;//
                invalidate();
                if (mTextDialog != null) {
                    mTextDialog.setVisibility(View.INVISIBLE);
                }
                break;
            default:
                setBackgroundResource(R.drawable.sidebar_background);
                if (oldChoose != c) {
                    if (c >= 0 && c < b.length) {
                        if (onTouchingLetterChangedListener != null) {
                            int count = 0;
                            if (isExistMap.containsKey(b[c])) {
                                count = isExistMap.get(b[c]);
                            }
                            if (count > 0) {
                                onTouchingLetterChangedListener.onTouchingLetterChanged(b[c]);
                                if (mTextDialog != null) {
                                    mTextDialog.setText(b[c]);
                                    mTextDialog.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                        choose = c;
                        invalidate();
                    }
                }

                break;
        }
        return true;
    }

    /**
     * 向外公开的方法
     *
     * @param onTouchingLetterChangedListener
     */
    public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    /**
     * 接口
     *
     * @author coder
     */
    public interface OnTouchingLetterChangedListener {
        public void onTouchingLetterChanged(String s);
    }

}