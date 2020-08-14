package com.redchamber.map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.redchamber.api.GlobalConstants;
import com.redchamber.api.RequestCode;
import com.redchamber.bean.mapBean;
import com.redchamber.info.ResidentCityActivity;
import com.redchamber.view.ClearEditText;
import com.sk.weichat.AppConstant;
import com.sk.weichat.BuildConfig;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.adapter.NearPositionAdapter;
import com.sk.weichat.map.MapHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.PermissionUtil;
import com.sk.weichat.util.ToastUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapAddressListActivity extends BaseActivity {

    @BindView(R.id.tv_location)
    TextView tvLocation;
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

    MapHelper.LatLng lan = null;
    GeoCoder gc;
    private  String  city_address=null;


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
        setContentView(R.layout.activity_map_address_list);
        ButterKnife.bind(this);
        initActionBar();
        initView();
        initMap();
        initEvent();
        if (BuildConfig.DEBUG) {

        }
    }

    @OnClick({R.id.tv_location})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_location:
                ResidentCityActivity.startActivityForResult(this, 1, RequestCode.REQUEST_CODE_SELECT_CITY);
                break;

        }
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title_center);
        //tvTitle.setText(getString(R.string.location));
        tvTitleRight = findViewById(R.id.tv_title_right);
        tvTitleRight.setText(getResources().getString(R.string.sure));
        tvTitleRight.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_grey_circle));
        ButtonColorChange.colorChange(mContext, tvTitleRight);
        tvTitleRight.setTextColor(getResources().getColor(R.color.white));

        tvTitleRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapHelper.Place place = placeMap.get("place");
                if (place == null) {
                    if (placesSeach.size() > 0) {// 默认选中第一个
                        place = placesSeach.get(0);
                    }
                }
                String address;
                if (place != null) {
                    address = place.getName();
                } else {
                    address = "";
                }

                Intent intent = new Intent();
                intent.putExtra(AppConstant.EXTRA_LATITUDE, currentLatLng.getLatitude());
                intent.putExtra(AppConstant.EXTRA_LONGITUDE, currentLatLng.getLongitude());
                intent.putExtra(AppConstant.EXTRA_ADDRESS, address);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    public void initView() {
        ll_map = findViewById(R.id.ll_map);
        ivReturn = findViewById(R.id.iv_location);
        ce_map_position = findViewById(R.id.ce_map_position);
        ce_map_position.clearFocus();
        rv_map_position = findViewById(R.id.rv_map_position);
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
                ToastUtil.showToast(MapAddressListActivity.this, getString(R.string.tip_auto_location_failed) + t.getMessage());
                // 总有个默认的经纬度，拿出来，
                beginLatLng = picker.currentLatLng();
                picker.moveMap(beginLatLng);
                loadMapDatas(beginLatLng);
            });
        });
    }

    private void initEvent() {


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
                if (TextUtils.isEmpty(s.toString())) {
                    if(lan!=null){
                        lan = new MapHelper.LatLng(lat_data, lng_data);
                        loadMapDatas(lan);
                    }
                } else {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try  {
                                getCoordinate(city_name+s.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }});
                    thread.start();
                  //  getGeoPointBystr(s.toString());
                }

            }
        });

        findViewById(R.id.rl_map_position).setOnClickListener(v -> {
            // 点击空白区域隐藏软键盘
            InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(findViewById(R.id.rl_map_position).getWindowToken(), 0); //强制隐藏键盘
            }
        });


    }


    String  city_name="上海";
    public void getGeoPointBystr(String str) {


        gc = GeoCoder.newInstance();
        gc.setOnGetGeoCodeResultListener(listener);
        String city = MyApplication.getInstance().getBdLocationHelper().getCityName();
        if (city_name == null || TextUtils.isEmpty(city_name)) {
            city = "上海";
        }
        gc.geocode(new GeoCodeOption()
                .city(city)
                .address(str));
    }


    OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
        @Override
        public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
            if (null != geoCodeResult && null != geoCodeResult.getLocation()) {
                if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    //没有检索到结果
                    return;
                } else {
                    double latitude = geoCodeResult.getLocation().latitude;
                    double longitude = geoCodeResult.getLocation().longitude;
                    lan = new MapHelper.LatLng(latitude, longitude);
                    loadMapDatas(lan);
                }
            }
        }

        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

        }
    };

    private void loadMapDatas(MapHelper.LatLng latLng) {
        currentLatLng = latLng;
        tvTitleRight.setVisibility(View.VISIBLE);
        ivReturn.setVisibility(View.VISIBLE);


        mapHelper.requestPlaceList(latLng, (List<MapHelper.Place> places) -> {
            nearPositionAdapter.setData(places);
            placesSeach.clear();
            placesSeach.addAll(places);
            LinearLayoutManager layoutManager = new LinearLayoutManager(MapAddressListActivity.this);
            rv_map_position.setLayoutManager(layoutManager);
            rv_map_position.setAdapter(nearPositionAdapter);
        }, t -> ToastUtil.showToast(MapAddressListActivity.this, getString(R.string.tip_places_around_failed) + t.getMessage()));
    }


    public void cancelKeyBoard(View view) {
        // 点击空白区域隐藏软键盘
        InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(findViewById(R.id.tv_keyboard).getWindowToken(), 0); //强制隐藏键盘
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RequestCode.REQUEST_CODE_SELECT_CITY:
                if (data != null) {
                    String address = data.getStringExtra(GlobalConstants.KEY_CITY);
                    tvLocation.setText(address);
                    city_address=address;
                  //  handler.sendEmptyMessage(0);
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try  {
                                city_name=address;
                                getCoordinate(address);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }});
                    thread.start();

                }
                break;
        }
    }

    private static double lat_data;
    private static double lng_data;
    // 调用百度地图API根据地址，获取坐标
    public void getCoordinate(String address) {
        if (address != null && !"".equals(address)) {
            address = address.replaceAll("\\s*", "").replace("#", "栋");
            String url = "http://api.map.baidu.com/geocoder?address="+address+"&output=json&key=lUTrxpAlfACpijwlkOLmKffPmn2xnRcY";
            String json = loadJSON(url);
            if (json != null && !"".equals(json)) {
                mapBean mapBean= JSON.parseObject(json, mapBean.class);
                if(mapBean.getStatus().equals("OK")){
                    double latitude = mapBean.getResult().getLocation().getLat();
                    double longitude = mapBean.getResult().getLocation().getLng();
                    lan = new MapHelper.LatLng(latitude, longitude);
                    lat_data=latitude;
                    lng_data=longitude;
                    loadMapDatas(lan);

                }
               /* if ("0".equals(obj.getString("status"))) {
                    double lng = obj.getJSONObject("result").getJSONObject("location").getDouble("lng"); // 经度
                    double lat = obj.getJSONObject("result").getJSONObject("location").getDouble("lat"); // 纬度

                    DecimalFormat df = new DecimalFormat("#.######");


                }*/
            }
        }

    }

    public static String loadJSON(String url) {
        StringBuilder json = new StringBuilder();
        try {
            URL oracle = new URL(url);
            URLConnection yc = oracle.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
            String inputLine = null;
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
        } catch (MalformedURLException e) {} catch (IOException e) {}
        return json.toString();
    }


}
