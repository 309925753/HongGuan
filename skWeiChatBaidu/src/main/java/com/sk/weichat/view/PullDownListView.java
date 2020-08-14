package com.sk.weichat.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sk.weichat.R;


/**
 * 改成划到顶部自动刷新，
 */
public class PullDownListView extends ListView implements OnScrollListener, OnItemClickListener, OnItemLongClickListener {

    // 下拉刷新状态
    private final static int PULL_TO_RELEASE = 0;
    private final static int RELEASE_TO_REFRESH = 1;
    private final static int REFRESHING = 2;
    private final static int DONE = 3;
    private static final long delayMillisRefreshing = 800;// 延时delayMillisRefreshing ms才去回调onHeaderRefreshing方法，即聊天界面真正回调loadData方法
    private final Handler mHandler = new Handler();
    public boolean mNeedRefresh = true;// 需不需要继续刷新
    private int mRefreshState = DONE;
    private boolean isOurControl = false;// 是否由自己的程序来控制listview的滚动，false表示由listView控制。
    private int mLastModifyY = 0;
    private int mFirstItemIndex = 0;
    private LinearLayout mHeaderView = null;
    private ProgressBar mHeaderProgress = null;
    private TextView mHeaderTextView = null;
    private int mHeaderViewHeight;
    private boolean isBackToPullRelease = false;
    private SmoothScrollRunnable mCurrentSmoothScrollRunnable = null;
    private RefreshingListener mRefreshListener = null;
    private OnScrollListener mUserOnScrollListener = null;
    private OnItemClickListener mUserOnItemClickListener = null;
    private OnItemLongClickListener mUserOnItemLongClickListener = null;
    private boolean mShouldNotIntercept = false;
    private float mInterceptMotionX;
    private float mInterceptMotionY;
    private int mDownPosition = 0;//roamer++
    private int mScrollPosition = 0;//roamer++
    // 下拉到顶部是否自动加载上一页，要触发过滑动事件才能自动加载，否则可能因为设置Adapter设置滑动监听触发回调误判，
    private boolean autoLoadEnabled = false;

    public PullDownListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PullDownListView(Context context) {
        super(context);
        init();
    }

    /**
     * 下拉刷新完成后调用本函数
     */
    public void headerRefreshingCompleted() {
        resetStates();
    }

    /**
     * 设置刷新回调函数
     */
    public void setRefreshListener(RefreshingListener l) {
        mRefreshListener = l;
    }

    public void setNeedRefresh(boolean needRefresh) {//roamer++
        mNeedRefresh = needRefresh;
    }

    public void startHeaderRefresh() {
        if (!mNeedRefresh) {//roamer++
            return;
        }
        if (mRefreshState == REFRESHING) {
            return;
        }
        if (!autoLoadEnabled) {
            return;
        }
        // 自动加载，也需要显示load动画，
        // 同时，将delayMillisRefreshing定义的稍大一点，让动画久一点，否则查询本地数据库时可能非常快，动画效果都看不到
        smoothScrollTo(0);
        if (!mNeedRefresh) {
            mRefreshState = DONE;
            return;
        }
        mRefreshState = REFRESHING;
        changeHeaderViewByState();
        onHeaderRefreshing();
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mUserOnScrollListener = l;
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener l) {
        super.setOnItemClickListener(this);
        mUserOnItemClickListener = l;
    }

    @Override
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        super.setOnItemLongClickListener(this);
        mUserOnItemLongClickListener = listener;
    }

    private void init() {
        super.setOnScrollListener(this);
        initHeaderView(getContext());
    }

    private void initHeaderView(Context c) {
        mHeaderView = (LinearLayout) LayoutInflater.from(c).inflate(R.layout.pulldown_loading_view, null);
        mHeaderProgress = (ProgressBar) mHeaderView.findViewById(R.id.pull_to_refresh_progress);
        mHeaderTextView = (TextView) mHeaderView.findViewById(R.id.pull_to_refresh_textView);
        addHeaderView(mHeaderView, null, false);
        measureView(mHeaderView);
        mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
        setHeaderDividersEnabled(true);
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    private void resetStates() {
        mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
        mRefreshState = DONE;
        isOurControl = false;
    }

    private void changeHeaderViewByState() {
        switch (mRefreshState) {
            case PULL_TO_RELEASE:
                mHeaderProgress.setVisibility(View.GONE);
                mHeaderTextView.setVisibility(View.GONE);
                if (isBackToPullRelease) {
                    isBackToPullRelease = false;
                }
                break;
            case RELEASE_TO_REFRESH:
                mHeaderProgress.setVisibility(View.GONE);
                mHeaderTextView.setVisibility(View.GONE);
                break;
            case REFRESHING:
                mHeaderProgress.setVisibility(View.VISIBLE);
                mHeaderTextView.setVisibility(View.VISIBLE);
                break;
            case DONE:
                mHeaderProgress.setVisibility(View.GONE);
                mHeaderTextView.setVisibility(View.GONE);
                break;
        }
    }

    private void onHeaderRefreshing() {
        // 在外部刷新完成后，会调用刷新完成，如果外部刷新完成速度过快，会造成刷新结束后，smooth回弹没有结束，导致错误，
        // 所以在这个地方延迟进行刷新操作，保证smooth回弹结束后才刷新。
        mHandler.postDelayed(new DelayRefreshRunnable(), delayMillisRefreshing);
    }

    private final void smoothScrollTo(int y) {
        if (null != mCurrentSmoothScrollRunnable) {
            mCurrentSmoothScrollRunnable.stop();
        }

        int paddingTop = mHeaderView.getPaddingTop();

        if (mHeaderView.getPaddingTop() != y) {
            mCurrentSmoothScrollRunnable = new SmoothScrollRunnable(mHandler, paddingTop, y);
            mHandler.post(mCurrentSmoothScrollRunnable);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mUserOnScrollListener != null) {
            mUserOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
        mFirstItemIndex = firstVisibleItem;
        if (mHeaderProgress != null) {// 布局确保初始化完成了，
            if (firstVisibleItem < 10) {// 滑动到顶部自动加载
                startHeaderRefresh();
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mUserOnScrollListener != null) {
            mUserOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_MOVE && mShouldNotIntercept) {
            return false;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mInterceptMotionX = x;
                mInterceptMotionY = y;
                mShouldNotIntercept = false;
                break;
            case MotionEvent.ACTION_MOVE:
                final int xDiff = (int) Math.abs(mInterceptMotionX - x);
                final int yDiff = (int) Math.abs(mInterceptMotionY - y);
                // 当在x轴方向上移动的距离大于Y方向时
                if (xDiff > yDiff) {
                    mShouldNotIntercept = true;
                }
                mInterceptMotionX = x;
                mInterceptMotionY = y;
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastModifyY = (int) event.getY();
                mDownPosition = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!autoLoadEnabled) {
                    autoLoadEnabled = true;
                }
                final int dy = mLastModifyY - (int) event.getY();
                mLastModifyY = (int) event.getY();
                // 当不是item0不在第一个时，不进行处理
                if (mFirstItemIndex == 0 && !isOurControl) {
                    isOurControl = true;
                }
                if (!isOurControl) {
                    break;
                }
                // 当是DONE状态，并且是往下拉的情况, 且在列表处于最顶上，则进入了PULL_TO_RELEASE状态
                if (mRefreshState == DONE && dy < 0 && mFirstItemIndex == 0) {
                    mRefreshState = PULL_TO_RELEASE;
                    isBackToPullRelease = false;
                    changeHeaderViewByState();
                }
                if (mRefreshState == PULL_TO_RELEASE || mRefreshState == RELEASE_TO_REFRESH) {
                    // 在PULL_TO_RELEASE或者RELEASE_TO_REFRESH状态下，mFirstItemIndex一定为0
                    // 如果不为0，则需要对状态进行重置
                    if (mFirstItemIndex != 0) {
                        resetStates();
                        changeHeaderViewByState();
                        break;
                    }
                    // 在拉动过程当中，headerview的paddingTop可能成为一个很大负数，
                    // 为了防止累计效应，这里控制，最大负数为mHeaderViewHeight
                    if (mHeaderView.getPaddingTop() + mHeaderViewHeight < 0) {
                        resetStates();
                        changeHeaderViewByState();
                        break;
                    }
                    // 当下拉刷新过程中，又往上移动，需要将selection设置为0，以抵消listview本身的滚动
                    if (dy > 0) {
                        setSelection(0);
                    }
                    if (mRefreshState == PULL_TO_RELEASE && mHeaderView.getPaddingTop() > 10) {// 10控制下来多少才进入刷新状态，越大下拉越多才能进入下来状态
                        mRefreshState = RELEASE_TO_REFRESH;
                        changeHeaderViewByState();
                        break;
                    } else if (mRefreshState == RELEASE_TO_REFRESH && mHeaderView.getPaddingTop() < 10) {
                        mRefreshState = PULL_TO_RELEASE;
                        isBackToPullRelease = true;
                        changeHeaderViewByState();
                        break;
                    }
                    // 通过修改headerview的paddingTop来实现下来超出
                    int distance = mHeaderView.getPaddingTop() - dy / 2;
                    mHeaderView.setPadding(0, distance, 0, 0);
                }
                if (mRefreshState == REFRESHING) {
                    // Log.i(TAG, "top:" + mHeaderView.getPaddingTop());
                    if (mFirstItemIndex != 0) {
                        mHeaderView.setPadding(0, 0, 0, 0);
                        break;
                    }
                    int distance = mHeaderView.getPaddingTop() - dy / 2;
                    mHeaderView.setPadding(0, distance, 0, 0);
                }
                mScrollPosition = (int) event.getY();//roamer++
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isOurControl = false;
                if (mRefreshState == PULL_TO_RELEASE) {
                    smoothScrollTo(-mHeaderViewHeight);
                    mRefreshState = DONE;
                } else if (mRefreshState == RELEASE_TO_REFRESH) {
                    smoothScrollTo(0);
                    if (!mNeedRefresh) {//roamer++
                        mRefreshState = DONE;
                        break;
                    }
                    mRefreshState = REFRESHING;
                    changeHeaderViewByState();
                    onHeaderRefreshing();
                } else if (mRefreshState == REFRESHING) {
                    smoothScrollTo(0);
                }
                mScrollPosition = (int) event.getY();//roamer++
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (mUserOnItemClickListener != null) {
            mUserOnItemClickListener.onItemClick(arg0, arg1, arg2 - 1, arg3);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (mUserOnItemLongClickListener != null) {
            return mUserOnItemLongClickListener.onItemLongClick(arg0, arg1, arg2 - 1, arg3);
        }
        return false;
    }


    public interface RefreshingListener {
        public void onHeaderRefreshing();
    }

    final class DelayRefreshRunnable implements Runnable {
        @Override
        public void run() {
            if (mRefreshListener != null) {
                mRefreshListener.onHeaderRefreshing();
            }
        }
    }

    final class SmoothScrollRunnable implements Runnable {

        static final int ANIMATION_DURATION_MS = 190;
        static final int ANIMATION_FPS = 1000 / 60;

        private final Interpolator interpolator;
        private final int scrollToY;
        private final int scrollFromY;
        private final Handler handler;

        private boolean continueRunning = true;
        private long startTime = -1;
        private int currentY = -1;

        public SmoothScrollRunnable(Handler handler, int fromY, int toY) {
            this.handler = handler;
            this.scrollFromY = fromY;
            this.scrollToY = toY;
            this.interpolator = new AccelerateDecelerateInterpolator();
        }

        @Override
        public void run() {

            /**
             * Only set startTime if this is the first time we're starting, else
             * actually calculate the Y delta
             */
            if (startTime == -1) {
                startTime = System.currentTimeMillis();
            } else {

                /**
                 * We do do all calculations in long to reduce software float
                 * calculations. We use 1000 as it gives us good accuracy and
                 * small rounding errors
                 */
                long normalizedTime = (1000 * (System.currentTimeMillis() - startTime)) / ANIMATION_DURATION_MS;
                normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

                final int deltaY = Math.round((scrollFromY - scrollToY) * interpolator.getInterpolation(normalizedTime / 1000f));
                this.currentY = scrollFromY - deltaY;
                mHeaderView.setPadding(0, this.currentY, 0, 0);
            }

            // If we're not at the target Y, keep going...
            if (continueRunning && scrollToY != currentY) {
                handler.postDelayed(this, ANIMATION_FPS);
            }
        }

        public void stop() {
            this.continueRunning = false;
            this.handler.removeCallbacks(this);
        }
    }

}
