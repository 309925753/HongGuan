package com.sk.weichat.ui.map;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.AppConstant;
import com.sk.weichat.BuildConfig;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.adapter.NearPositionAdapter;
import com.sk.weichat.map.MapHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.FileUtil;
import com.sk.weichat.util.PermissionUtil;
import com.sk.weichat.util.ScreenUtil;
import com.sk.weichat.util.SoftKeyBoardListener;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.ClearEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/20.
 */
public class MapPickerActivity extends BaseActivity {
    private boolean isChat;
    private LinearLayout ll_map;
    private FrameLayout container;
    private TextView tvTitleRight;
    private ImageView ivReturn;
    private MapHelper mapHelper;
    private MapHelper.Picker picker;
    private MapHelper.LatLng beginLatLng;
    private MapHelper.LatLng currentLatLng;
    private ClearEditText ce_map_position;
    private RecyclerView rv_map_position;
    private NearPositionAdapter nearPositionAdapter;
    private List<MapHelper.Place> placesSeach = new ArrayList<>();
    private List<MapHelper.Place> seachPlace = new ArrayList<>();
    private Map<String, MapHelper.Place> placeMap = new HashMap<>();
    private boolean showTitle = true;
    private NearPositionAdapter.OnRecyclerItemClickListener itemClickListener = new NearPositionAdapter.OnRecyclerItemClickListener() {
        @Override
        public void onItemClick(int Position, MapHelper.Place dataBean) {
            placeMap.clear();
            placeMap.put("place", dataBean);
            picker.moveMap(dataBean.getLatLng());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtil.requestLocationPermissions(this, 0x01);
        setContentView(R.layout.activity_map_picker);
        isChat = getIntent().getBooleanExtra(AppConstant.EXTRA_FORM_CAHT_ACTIVITY, false);
        initActionBar();
        initView();
        initMap();
        initEvent();
        if (BuildConfig.DEBUG) {
            com.sk.weichat.util.LogUtils.log("after create");
        }
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.location));
        tvTitleRight = findViewById(R.id.tv_title_right);
        tvTitleRight.setText(isChat ? getString(R.string.send) : getResources().getString(R.string.sure));
        tvTitleRight.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_grey_circle));
        ButtonColorChange.colorChange(mContext, tvTitleRight);
        tvTitleRight.setTextColor(getResources().getColor(R.color.white));
        tvTitleRight.setOnClickListener(v -> {
            if (picker != null) {
                View mapView = picker.getMapView();
                int dw = mapView.getWidth();
                int dh = mapView.getHeight();
                // 截取宽度一半，
                int width = dw / 2;
                // 图片宽高比要和视图一样，
                int height = (int) (width * 1f / 672 * 221);
                // 以防万一，等比例缩小至全屏，
                float scale = Math.max(1, Math.min(width * 1.0f / dw, height * 1.0f / dh));
                final int rw = (int) (width / scale);
                final int rh = (int) (height / scale);

                int left = (dw - rw) / 2;
                int right = (dw + rw) / 2;
                int top = (dh - rh) / 2;
                int bottom = (dh + rh) / 2;
                Rect rect = new Rect(left, top, right, bottom);
                picker.snapshot(rect, bitmap -> {
                    MapHelper.Place place = placeMap.get("place");
                    // 部分截图保存本地，
                    String snapshot = FileUtil.saveBitmap(bitmap);
                    if (place == null) {
                        if (placesSeach.size() > 0) {// 默认选中第一个
                            place = placesSeach.get(0);
                        }
                    }
                    // 位置获取失败也返回空，外面有判断和提示，不能直接return没反应，
                    String address;
                    if (place != null) {
                        address = place.getName();
                    } else {
                        address = "";
                    }
                    if (TextUtils.isEmpty(address)) {
                        address = MyApplication.getInstance().getBdLocationHelper().getAddress();
                    }
                    Intent intent = new Intent();
                    intent.putExtra(AppConstant.EXTRA_LATITUDE, currentLatLng.getLatitude());
                    intent.putExtra(AppConstant.EXTRA_LONGITUDE, currentLatLng.getLongitude());
                    intent.putExtra(AppConstant.EXTRA_ADDRESS, address);
                    intent.putExtra(AppConstant.EXTRA_SNAPSHOT, snapshot);
                    setResult(RESULT_OK, intent);
                    finish();
                });
            }
        });
        // 一切准备就绪在显示发送按钮
        tvTitleRight.setVisibility(View.GONE);
    }

    public void initView() {
        ll_map = findViewById(R.id.ll_map);
        ivReturn = findViewById(R.id.iv_location);
        // 一切准备就绪在显示回到当前位置按钮
        ivReturn.setVisibility(View.GONE);
        ce_map_position = findViewById(R.id.ce_map_position);
        ce_map_position.clearFocus();
        rv_map_position = findViewById(R.id.rv_map_position);
/*
        rv_map_position.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
*/
        nearPositionAdapter = new NearPositionAdapter(this);
        nearPositionAdapter.setRecyclerItemClickListener(itemClickListener);
    }

    private void initMap() {
        mapHelper = MapHelper.getInstance();
        picker = mapHelper.getPicker(this);
        getLifecycle().addObserver(picker);
        container = findViewById(R.id.map_view_container);
        picker.attack(container, () -> {
            // 初始化底部周边相关动画，
            // 中心打上图标，
            picker.addCenterMarker(R.drawable.ic_position, "pos");
            mapHelper.requestLatLng(latLng -> {
                // 记录开始时定位的位置，用来点击按钮跳回来，
                beginLatLng = latLng;
                picker.moveMap(latLng);
                // 加载周边位置信息，
                // 记录当前位置也在这个方法里，
                loadMapDatas(latLng);
            }, t -> {
                ToastUtil.showToast(MapPickerActivity.this, getString(R.string.tip_auto_location_failed) + t.getMessage());
                // 总有个默认的经纬度，拿出来，
                beginLatLng = picker.currentLatLng();
                picker.moveMap(beginLatLng);
                loadMapDatas(beginLatLng);
            });
        });
        picker.setOnMapStatusChangeListener(new MapHelper.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapHelper.MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChange(MapHelper.MapStatus mapStatus) {
            }

            @Override
            public void onMapStatusChangeFinish(MapHelper.MapStatus mapStatus) {
                loadMapDatas(mapStatus.target);
            }
        });
    }

    private void initEvent() {
        ivReturn.setOnClickListener(v -> {
            currentLatLng = beginLatLng;
            picker.moveMap(beginLatLng);
            loadMapDatas(currentLatLng);
            ce_map_position.setText("");
        });

        ce_map_position.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                seachPlace.clear();
                if (TextUtils.isEmpty(s.toString()))
                    loadMapDatas(currentLatLng);
                for (int i = 0; i < placesSeach.size(); i++) {
                    if (placesSeach.get(i).getName().contains(s.toString())) {
                        seachPlace.add(placesSeach.get(i));
                    }
                }
                nearPositionAdapter.setData(seachPlace);
            }
        });

        findViewById(R.id.rl_map_position).setOnClickListener(v -> {
            // 点击空白区域隐藏软键盘
            InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(findViewById(R.id.rl_map_position).getWindowToken(), 0); //强制隐藏键盘
            }
        });

        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {

            @Override
            public void keyBoardShow(int height) {
                startTranslateAnim(false);
                findViewById(R.id.tv_keyboard).setVisibility(View.VISIBLE);
            }

            @Override
            public void keyBoardHide(int height) {
                startTranslateAnim(true);
                findViewById(R.id.tv_keyboard).setVisibility(View.GONE);
            }
        });
    }

    private void loadMapDatas(MapHelper.LatLng latLng) {
        currentLatLng = latLng;
        tvTitleRight.setVisibility(View.VISIBLE);
        ivReturn.setVisibility(View.VISIBLE);

        mapHelper.requestPlaceList(latLng, places -> {
            nearPositionAdapter.setData(places);
            placesSeach.clear();
            placesSeach.addAll(places);
            LinearLayoutManager layoutManager = new LinearLayoutManager(MapPickerActivity.this);
            rv_map_position.setLayoutManager(layoutManager);
            rv_map_position.setAdapter(nearPositionAdapter);
        }, t -> ToastUtil.showToast(MapPickerActivity.this, getString(R.string.tip_places_around_failed) + t.getMessage()));
    }

    public void startTranslateAnim(boolean show) {
        if (showTitle == show) {
            return;
        }
        showTitle = show;
        float fromy = -(ScreenUtil.getScreenHeight(mContext) / 3);
        float toy = 0;

        if (!show) {
            fromy = 0;
            toy = -(ScreenUtil.getScreenHeight(mContext) / 3);
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(ll_map, "translationY", fromy, toy);
        animator.setDuration(300);
        animator.start();
    }

    public void cancelKeyBoard(View view) {
        // 点击空白区域隐藏软键盘
        InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(findViewById(R.id.tv_keyboard).getWindowToken(), 0); //强制隐藏键盘
        }
    }
}
