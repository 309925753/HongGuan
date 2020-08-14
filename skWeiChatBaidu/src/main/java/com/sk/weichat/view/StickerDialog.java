package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;


public class StickerDialog extends Dialog {
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
    private Callback mCallback;

    public StickerDialog(Context context, Callback callback) {
        super(context, R.style.BottomDialog);
        mCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sticker_layout);
        setCanceledOnTouchOutside(true);
        initView();
        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();

        lp.width = ScreenUtil.getScreenWidth(getContext());
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.BOTTOM);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }

    private void initView() {
        List<Drawable> drawableList = new ArrayList<>();

        RecyclerView rl_sticker = findViewById(R.id.rl_sticker);
        rl_sticker.setLayoutManager(new GridLayoutManager(getContext(), 4));
        for (int i = 0; i < 18; i++) {
            drawableList.add(getContext().getResources().getDrawable(ids[i]));
        }
        rl_sticker.setAdapter(new Sticker(drawableList, mCallback));
    }

    public interface Callback {

        void onSticker(int image);
    }
}

class Sticker extends RecyclerView.Adapter<StickerHolde> {
    List<Drawable> drawables = new ArrayList<>();
    StickerDialog.Callback mCallback;

    public Sticker(List<Drawable> drawables, StickerDialog.Callback callback) {
        this.drawables = drawables;
        mCallback = callback;
    }

    @NonNull
    @Override
    public StickerHolde onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sticker_adapter, parent, false);
        return new StickerHolde(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StickerHolde holder, int position) {
        Drawable drawable = drawables.get(position);
        holder.bind(drawable);
        holder.iv_sticker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onSticker(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return drawables.size();
    }

}

class StickerHolde extends RecyclerView.ViewHolder {
    ImageView iv_sticker;

    public StickerHolde(@NonNull View itemView) {
        super(itemView);
        iv_sticker = itemView.findViewById(R.id.iv_sticker);

    }

    void bind(Drawable drawable) {
        iv_sticker.setImageDrawable(drawable);
    }
}

