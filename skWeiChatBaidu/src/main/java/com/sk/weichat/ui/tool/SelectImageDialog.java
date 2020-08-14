package com.sk.weichat.ui.tool;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.util.ViewHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/18.
 */
public class SelectImageDialog extends Dialog {
    private Context mContext;
    private LayoutInflater mInflater;
    private View mRootView;
    private TextView mTvTitle;
    private GridView mGridView;
    private String mOriginUrl;
    private List<String> mUrls;
    private Map<Integer, String> mRealAddressMap = new HashMap<>();

    private OptionListener mlistener;

    public SelectImageDialog(Context context, String originUrl, List<String> urls, OptionListener listener) {
        this(context, 0, LayoutInflater.from(context));
        mContext = context;
        mOriginUrl = originUrl;
        mUrls = urls;
        mlistener = listener;
        initView();
    }

    private SelectImageDialog(Context context, int themeResId, LayoutInflater mInflater) {
        super(context, R.style.full_dialog_style);
    }

    private void initView() {
        mInflater = LayoutInflater.from(mContext);
        mRootView = mInflater.inflate(R.layout.dialog_select_image, null);
        setContentView(mRootView);

        mRootView.findViewById(R.id.iv_title_left).setOnClickListener(v -> dismiss());
        mTvTitle = (TextView) mRootView.findViewById(R.id.tv_title_center);
        mTvTitle.setText(R.string.tip_select_photo);
        mGridView = (GridView) mRootView.findViewById(R.id.dialog_select_gv);
        mGridView.setAdapter(new SelectImgAdapter());
        mGridView.setOnItemClickListener((parent, view, position, id) -> {
            dismiss();
            if (mRealAddressMap.containsKey(position)) {
                mlistener.option(mRealAddressMap.get(position));
            } else {
                mlistener.option(mUrls.get(position));
            }
        });
    }

    public interface OptionListener {
        void option(String url);
    }

    class SelectImgAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mUrls.size();
        }

        @Override
        public Object getItem(int position) {
            return mUrls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_image, null, false);
            }
            final ImageView iv = ViewHolder.get(convertView, R.id.iv);
            ImageLoadHelper.showImageWithError(
                    mContext,
                    mUrls.get(position),
                    R.drawable.defaultpic,
                    iv
            );

            ImageLoadHelper.loadBitmapCenterCropDontAnimate(
                    mContext,
                    mUrls.get(position),
                    b -> {
                        iv.setImageBitmap(b);
                    }, e -> {
                        // 加载失败的图片，解析出来的可能为图片名字而非真正的URL，用原地址拼接
                        String realAddress;
                        if (mUrls.get(position).contains("com")) {
                            // ex;//m.baidu.com/se/static/img/iphone/logo_web.png
                            realAddress = "https:" + mUrls.get(position);
                        } else {
                            // ex:img/logo.png
                            String prefix = mOriginUrl.substring(0, mOriginUrl.lastIndexOf("/"));
                            realAddress = prefix + "/" + mUrls.get(position);
                        }
                        mRealAddressMap.put(position, realAddress);
                        ImageLoadHelper.showImageWithError(
                                mContext,
                                realAddress,
                                R.drawable.defaultpic,
                                iv
                        );
                    }
            );
            return convertView;
        }
    }
}
