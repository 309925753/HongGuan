package com.sk.weichat.view.imageedit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.sk.weichat.R;

import me.kareluo.imaging.view.IMGStickerView;

/**
 * Created by felix on 2017/12/21 下午10:58.
 */

public class IMGStickerImageView extends IMGStickerView {
    public final int[] ids = {
            R.mipmap.oushi1,
            R.mipmap.oushi2,
            R.mipmap.oushi3,
            R.mipmap.oushi4,
            R.mipmap.oushi5,
            R.mipmap.oushi6,
            R.mipmap.oushi7,
            R.mipmap.oushi8,
            R.mipmap.oushi9,
            R.mipmap.oushi10,
            R.mipmap.oushi11,
            R.mipmap.oushi12,
            R.mipmap.oushi13,
            R.mipmap.oushi14,
            R.mipmap.oushi15,
            R.mipmap.oushi16,
            R.mipmap.oushi17,
            R.mipmap.oushi18
    };
    private ImageView mImageView;

    public IMGStickerImageView(Context context) {
        super(context);
    }

    public IMGStickerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IMGStickerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public View onCreateContentView(Context context) {
        mImageView = new ImageView(context);
        return mImageView;
    }

    public void setStickerImageView(int i) {
        mImageView.setImageResource(ids[i]);
    }
}
