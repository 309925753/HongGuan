package com.sk.weichat.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.sk.weichat.Reporter;
import com.sk.weichat.util.LogUtils;

/**
 * @author Dean Tao
 * @version 1.0
 */
public abstract class EasyFragment extends BaseLoginFragment implements View.OnClickListener {
    private View mRootView;
    // 是否重建了视图，重建了才需要重新初始化子视图，
    private boolean createView = false;

    /**
     * 是否缓存视图
     *
     * @return
     */
    protected boolean cacheView() {
        return true;
    }

    /**
     * 指定该Fragment的Layout id
     *
     * @return
     */
    protected abstract int inflateLayoutId();

    /**
     * 代替onActivityCreated的回调
     * 使用onActivityCreated初始化而不是onCreateView,
     * 因为onActivityCreated时有确保rootView, Context, Activity, CoreManager可用，
     * 而onCreateView只适用于初始化根视图，
     *
     * @param savedInstanceState anceState
     * @param createView         是否重新创建了视图，如果是，那么你需要重新findView来初始化子视图的引用等。
     */
    protected abstract void onActivityCreated(Bundle savedInstanceState, boolean createView);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // TODO: 出现过主页后三个fragment都是空白的问题，打个日志排查一下，
        LogUtils.log("onCreateView fragment: " + this.toString());
        boolean createView = true;
        if (cacheView() && mRootView != null) {
            // 缓存的mRootView需要判断是否已经在一个ViewGroup中， 如果在，就先移除自己，要不然会发生mRootView已经有parent的错误。
            ViewGroup parent = (ViewGroup) mRootView.getParent();
            if (parent != null) {
                parent.removeView(mRootView);
            }
            createView = false;
        } else {
            mRootView = inflater.inflate(inflateLayoutId(), container, false);
        }
        this.createView = createView;
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!createView) {
            LogUtils.log("复用了fragment: " + this.toString());
            Reporter.post("复用了fragment");
        }
        onActivityCreated(savedInstanceState, createView);
    }

    public <T extends View> T findViewById(int id) {
        if (mRootView != null) {
            return mRootView.findViewById(id);
        }
        return null;
    }

    public void appendClick(View v) {
        v.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }
}
