package com.sk.weichat.ui.me;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.sk.weichat.R;
import com.sk.weichat.ui.MainActivity;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.base.BaseRecAdapter;
import com.sk.weichat.ui.base.BaseRecViewHolder;
import com.sk.weichat.util.RecyclerSpace;
import com.sk.weichat.util.SkinUtils;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import java.util.List;

/**
 * Created by zq on 2017/8/26 0026.
 * <p>
 * 更换皮肤
 */
public class SkinStore extends BaseActivity {
    private ListView mListView;
    private SkinAdapter skinAdapter;
    private List<SkinUtils.Skin> skins;
    private SkinUtils.Skin currentSkin;

    private boolean isClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_activity_switch_skin);
        isClick = false;
        initView();

    }

    protected void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.change_skin));

        // 当前皮肤
        currentSkin = SkinUtils.getSkin(this);
        // 初始化皮肤
        skins = SkinUtils.defaultSkins;
        initUI();
    }

    void initUI() {
        SwipeRecyclerView swipeRecyclerView = findViewById(R.id.rv_pager);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        swipeRecyclerView.setLayoutManager(layoutManager);
        skinAdapter = new SkinAdapter(skins);
        swipeRecyclerView.setAdapter(skinAdapter);
        swipeRecyclerView.addItemDecoration(new RecyclerSpace(8, Color.TRANSPARENT, 1));
    }

    class SkinAdapter extends BaseRecAdapter<SkinUtils.Skin, SkiinViewHolder> {
        SkinAdapter(List<SkinUtils.Skin> data) {
            super(data);
        }


        @Override
        public void onHolder(SkiinViewHolder holder, SkinUtils.Skin bean, int position) {

            holder.skinName.setText(bean.getColorName());
            ViewCompat.setBackgroundTintList(holder.skinName, ColorStateList.valueOf(bean.getPrimaryColor()));
            if (bean.isLight()) {
                holder.skinName.setTextColor(holder.skinName.getContext().getResources().getColor(R.color.black));
            }
            ImageViewCompat.setImageTintList(holder.skinIv, ColorStateList.valueOf(bean.getAccentColor()));
            if (currentSkin == bean) {
                holder.skinCheck.setVisibility(View.VISIBLE);
            } else {
                holder.skinCheck.setVisibility(View.GONE);
            }
        }

        @Override
        public SkiinViewHolder onCreateHolder() {
            return new SkiinViewHolder(getViewByRes(R.layout.item_switch_skin_new));
        }
    }

    public class SkiinViewHolder extends BaseRecViewHolder implements View.OnClickListener {
        public TextView skinName;
        public ImageView skinIv;
        public ImageView skinCheck;


        public SkiinViewHolder(View rootView) {
            super(rootView);

            skinName = rootView.findViewById(R.id.tv_color_name);
            skinIv = rootView.findViewById(R.id.iv_color);
            skinCheck = rootView.findViewById(R.id.check);
            rootView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition(); // 减1一个头布局
            if (isClick) {
                return;
            }

            isClick = true;
            // 这里重复点击会有问题
            currentSkin = skins.get(position);
            SkinUtils.setSkin(SkinStore.this, currentSkin);
            skinAdapter.notifyDataSetChanged();
            Toast.makeText(SkinStore.this, getString(R.string.tip_change_skin_success), Toast.LENGTH_SHORT).show();
            MainActivity.isInitView = true;
            Intent intent = new Intent(SkinStore.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
