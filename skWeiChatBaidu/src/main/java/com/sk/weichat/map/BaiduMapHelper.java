package com.sk.weichat.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BaiduMapHelper extends MapHelper {
    private static final String TAG = "BaiduMapHelper";
    @SuppressLint("StaticFieldLeak")
    private static BaiduMapHelper INSTANCE;
    private Context context;
    private LocationClient locationClient;
    private BDLocationListener locationListener;
    private SoftReference<Picker> softPicker = new SoftReference<>(null);

    private BaiduMapHelper(Context context) {
        this.context = context;
        locationClient = new LocationClient(context);
    }

    public static BaiduMapHelper getInstance(Context context) {
        // 单例用懒加载，为了传入context,
        if (INSTANCE == null) {
            synchronized (BaiduMapHelper.class) {
                if (INSTANCE == null) {
                    SDKInitializer.initialize(context);
                    INSTANCE = new BaiduMapHelper(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public String getStaticImage(LatLng latLng) {
        return "http://api.map.baidu.com/staticimage?width=640&height=480&center="
                + latLng.getLongitude()
                + ","
                + latLng.getLatitude()
                + "&zoom=15";// 此处需要特别注意，拼接url的时候纬度在前经度在后  = =跟google是反着的

    }

    @SuppressWarnings("deprecation")
    private void requestLocationOnce(@Nullable final OnSuccessListener<BDLocation> onSuccessListener,
                                     @Nullable final OnErrorListener onErrorListener) {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);           // 设置定位模式
        option.setCoorType("bd09ll");                                  // 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(5000);                                      // 设置发起定位请求的间隔时间为10s
        option.setIsNeedAddress(true);
        option.setNeedDeviceDirect(false);
        locationClient.setLocOption(option);
        if (locationListener != null) {
            // 以防万一，如果有已经注册了的listener，先取消，
            locationClient.unRegisterLocationListener(locationListener);
        }
        locationListener = new BDLocationListener() {
            @Override
            @SuppressWarnings("deprecation")
            public void onReceiveLocation(BDLocation location) {
                // 只定位一次就停止，
                locationClient.unRegisterLocationListener(this);
                locationClient.stop();
                int resultCode;
                if (location == null) {
                    if (onErrorListener != null) {
                        onErrorListener.onError(new RuntimeException("百度定位失败: location is null,"));
                    }
                    return;
                }
                resultCode = location.getLocType();
                // 百度定位失败
                if (resultCode != BDLocation.TypeGpsLocation && resultCode != BDLocation.TypeCacheLocation
                        && resultCode != BDLocation.TypeOffLineLocation && resultCode != BDLocation.TypeNetWorkLocation) {
                    Log.d(TAG, "百度定位失败");
                    if (onErrorListener != null) {
                        onErrorListener.onError(new RuntimeException("百度定位失败: " + location.getLocationDescribe()));
                    }
                    return;
                }

                // 百度定位成功
                if (onSuccessListener != null) {
                    onSuccessListener.onSuccess(location);
                }
            }

        };
        locationClient.registerLocationListener(locationListener);
        locationClient.start();
    }

    @Override
    public void requestLatLng(@Nullable final OnSuccessListener<LatLng> onSuccessListener,
                              @Nullable final OnErrorListener onErrorListener) {
        requestLocationOnce(new OnSuccessListener<BDLocation>() {
            @Override
            public void onSuccess(BDLocation location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (onSuccessListener != null) {
                    onSuccessListener.onSuccess(latLng);
                }
            }
        }, onErrorListener);
    }

    private void requestPoiList(LatLng latLng,
                                final OnSuccessListener<List<PoiInfo>> onSuccessListener,
                                final OnErrorListener onErrorListener) {
        if (latLng == null) {
            if (onErrorListener != null) {
                onErrorListener.onError(new RuntimeException("百度获取周边位置失败，"));
            }
            return;
        }
        GeoCoder geoSearch = GeoCoder.newInstance();
        geoSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
            }

            /**
             * 反地理编码，由地点经纬度获取地点名称
             */
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                List<PoiInfo> poiList = result.getPoiList();
                if (poiList == null || poiList.isEmpty()) {
                    if (onErrorListener != null) {
                        onErrorListener.onError(new RuntimeException("百度获取周边位置失败，"));
                    }
                    return;
                }
                if (onSuccessListener != null) {
                    onSuccessListener.onSuccess(poiList);
                }
            }
        });
        /* 加载定位数据 */
        com.baidu.mapapi.model.LatLng bdLatLng = new com.baidu.mapapi.model.LatLng(latLng.getLatitude(), latLng.getLongitude());
        ReverseGeoCodeOption reverseGeoCodeOption = new ReverseGeoCodeOption();
        reverseGeoCodeOption.location(bdLatLng);
        geoSearch.reverseGeoCode(reverseGeoCodeOption);
    }

    @Override
    public void requestPlaceList(LatLng latLng,
                                 @Nullable final OnSuccessListener<List<Place>> onSuccessListener,
                                 @Nullable final OnErrorListener onErrorListener) {
        requestPoiList(latLng, new OnSuccessListener<List<PoiInfo>>() {
            @Override
            public void onSuccess(List<PoiInfo> poiList) {
                List<Place> placeList = new ArrayList<>(poiList.size());
                for (PoiInfo poi : poiList) {
                    // 以防万一避免空指针，
                    String name = "" + poi.name;
                    String address = "" + poi.address;
                    LatLng placeLatLng = new LatLng(poi.location.latitude, poi.location.longitude);
                    Place place = new Place(name, address, placeLatLng);
                    placeList.add(place);
                }
                if (onSuccessListener != null) {
                    onSuccessListener.onSuccess(placeList);
                }

            }
        }, onErrorListener);
    }

    @Override
    public void requestCityName(final LatLng latLng,
                                @Nullable final OnSuccessListener<String> onSuccessListener,
                                @Nullable final OnErrorListener onErrorListener) {
        requestPoiList(latLng, new OnSuccessListener<List<PoiInfo>>() {
            @Override
            public void onSuccess(List<PoiInfo> poiList) {
                String city = null;
                for (PoiInfo poi : poiList) {
                    city = poi.city;
                    if (city != null) {
                        break;
                    }
                }
                if (city == null) {
                    if (onErrorListener != null) {
                        onErrorListener.onError(new RuntimeException(
                                String.format(Locale.CHINA, "地址<%f, %f>找不到城市名，",
                                        latLng.getLatitude(), latLng.getLongitude())));
                    }
                    return;
                }
                if (onSuccessListener != null) {
                    onSuccessListener.onSuccess(city);
                }
            }
        }, onErrorListener);
    }

    @Override
    public Picker getPicker(Context context) {
        Picker picker = softPicker.get();
        if (picker == null || picker.isBusy()) {
            picker = new BaiduMapPicker(context);
            softPicker = new SoftReference<>(picker);
        }
        return picker;
    }

    private class BaiduMapPicker extends Picker implements BaiduMap.OnMapStatusChangeListener {
        private MapView mapView;
        private BaiduMap mBaiduMap;
        private Context context;
        private com.baidu.mapapi.map.Marker centerMarker;

        private BaiduMapPicker(Context context) {
            this.context = context;
        }

        private void createMapView() {
            if (mapView == null) {
                mapView = new MapView(context);
                mapView.setClickable(true);
                mapView.setFocusable(true);
            }
            if (mBaiduMap == null) {
                mBaiduMap = mapView.getMap();
            }
        }

        @Override
        public void attack(FrameLayout container, @Nullable OnMapReadyListener listener) {
            Log.d(TAG, "attack: ");
            createMapView();
            mBaiduMap.clear();
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(mapView, params);
            init();
            if (listener != null) {
                listener.onMapReady();
            }
        }

        private void init() {
            UiSettings settings = mBaiduMap.getUiSettings();
            settings.setOverlookingGesturesEnabled(false);
            settings.setRotateGesturesEnabled(false);
            // 好像是百度Logo,
            // mapView.getChildAt(1).setVisibility(View.GONE);
            mapView.showZoomControls(false);
            mBaiduMap.setOnMapStatusChangeListener(this);
            mBaiduMap.setOnMarkerClickListener(markerBaidu -> {
                if (onMarkerClickListener != null) {
                    Marker marker = new Marker();
                    marker.setBitmap(markerBaidu.getIcon().getBitmap());
                    marker.setLatLng(new LatLng(markerBaidu.getPosition().latitude, markerBaidu.getPosition().longitude));
                    marker.setInfo(markerBaidu.getTitle());
                    onMarkerClickListener.onMarkerClick(marker);
                }
                return false;
            });
        }

        @Override
        public MapView getMapView() {
            return mapView;
        }

        @Override
        public void snapshot(Rect rect, final SnapshotReadyCallback callback) {
            mBaiduMap.snapshotScope(rect, new BaiduMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap bitmap) {
                    callback.onSnapshotReady(bitmap);
                }
            });
        }

        @Override
        public void showMyLocation(LatLng latLng) {
            MyLocationConfiguration config =
                    new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null);
            mBaiduMap.setMyLocationConfigeration(config);
            mBaiduMap.setMyLocationEnabled(true);
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(20.0f)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(0).latitude(latLng.getLatitude())
                    .longitude(latLng.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
        }

        @Override
        public void addCenterMarker(Bitmap bitmap, @Nullable String info) {
            createMapView();
            com.baidu.mapapi.model.LatLng bdLatLng = mBaiduMap.getMapStatus().target;
            // 构建Marker图标
            BitmapDescriptor bdBitmap = BitmapDescriptorFactory.fromBitmap(bitmap);
            // 构建MarkerOption，用于在地图上添加Marker
            MarkerOptions option = new MarkerOptions().position(bdLatLng).icon(bdBitmap).title(info);
            // 在地图上添加Marker，并显示
            centerMarker = (com.baidu.mapapi.map.Marker) mBaiduMap.addOverlay(option);
        }

        @Override
        public void addMarker(LatLng latLng, Bitmap bitmap, @Nullable String info) {
            createMapView();
            com.baidu.mapapi.model.LatLng bdLatLng = new com.baidu.mapapi.model.LatLng(latLng.getLatitude(), latLng.getLongitude());
            // 构建Marker图标
            BitmapDescriptor bdBitmap = BitmapDescriptorFactory.fromBitmap(bitmap);
            // 构建MarkerOption，用于在地图上添加Marker
            MarkerOptions option = new MarkerOptions().position(bdLatLng).icon(bdBitmap).title(info);
            // 在地图上添加Marker，并显示
            mBaiduMap.addOverlay(option);

            // TODO: Info小框展示，
/*
            com.baidu.mapapi.map.MapStatus mapStatus = new com.baidu.mapapi.map.MapStatus.Builder().zoom(mBaiduMap.getMaxZoomLevel() - 3).target(bdLatLng).build();
            MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(mapStatus);

            mapView.getMap().setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker arg0) {
                    String name = arg0.getTitle();
                    InfoWindow mInfoWindow;
                    if (mInfoWindow == null) {
                        // 创建InfoWindow展示的view
                        Button button = new Button(MapActivity.this);
                        button.setBackgroundResource(R.drawable.popup);
                        button.setText(name);
                        // 创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
                        mInfoWindow = new InfoWindow(button, arg0.getPosition(), -47);// 显示InfoWindow
                        mMapView.getMap().showInfoWindow(mInfoWindow);
                    } else {
                        mInfoWindow = null;
                        mMapView.getMap().hideInfoWindow();
                    }
                    return false;
                }
            });
*/
        }

        @Override
        public void clearMarker() {
            mBaiduMap.clear();
            if (centerMarker != null) {
                addCenterMarker(centerMarker.getIcon().getBitmap(), centerMarker.getTitle());
            }
        }

        @Override
        public LatLng currentLatLng() {
            com.baidu.mapapi.map.MapStatus mapStatus = mBaiduMap.getMapStatus();
            com.baidu.mapapi.model.LatLng target = mapStatus.target;
            return new LatLng(target.latitude, target.longitude);
        }

        @Override
        public void moveMap(LatLng latLng, boolean anim) {
            createMapView();
            moveMap(latLng.getLatitude(), latLng.getLongitude(), anim);
        }

        /**
         * @param x    lat,
         * @param y    lng,
         * @param anim 移动过程是否显示动画，
         */
        private void moveMap(double x, double y, boolean anim) {
            //  LogUtils.e("x  " + x + "  y  " + y);
            // TODO: 干嘛要大于0.1,0.1，人家刚好穿过0,0会怎样，
            if (mBaiduMap != null && x > 0.1 && y > 0.1) {
                //设定中心点坐标
                com.baidu.mapapi.model.LatLng cenpt = new com.baidu.mapapi.model.LatLng(x, y);
                //定义地图状态
                com.baidu.mapapi.map.MapStatus mMapStatus = new com.baidu.mapapi.map.MapStatus.Builder()
                        //要移动的点
                        .target(cenpt)
                        //放大地图到20倍
                        .zoom(16)
                        .build();
                // 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                // 改变地图状态,
                if (anim) {
                    mBaiduMap.animateMapStatus(mMapStatusUpdate);
                } else {
                    mBaiduMap.setMapStatus(mMapStatusUpdate);
                }
            } else {
                Log.d(TAG, String.format(Locale.CHINA, "moveMap: 数据异常<%f, %f, %s>", x, y, mBaiduMap));
            }
        }

        @Override
        void create() {
            super.create();
            createMapView();
        }

        @Override
        void resume() {
            super.resume();
            mapView.onResume();
        }

        @Override
        void pause() {
            super.pause();
            mapView.onPause();
        }

        @Override
        void destroy() {
            super.destroy();
            Log.e("zx", "destroy: removeView");
            // 不销毁，以便复用，
//            mapView.onDestroy();
            if (mapView.getParent() != null) {
                // 从parent移除，以便复用，
                ((ViewGroup) mapView.getParent()).removeView(mapView);
            }
        }

        @Override
        public void onMapStatusChangeStart(com.baidu.mapapi.map.MapStatus bdMapStatus) {
            MapStatus mapStatus = new MapStatus();
            mapStatus.target = new LatLng(bdMapStatus.target.latitude, bdMapStatus.target.longitude);
            if (onMapStatusChangeListener != null) {
                onMapStatusChangeListener.onMapStatusChangeStart(mapStatus);
            }
        }

        // 低版本百度地图没有这个方法，
        // @Override
        public void onMapStatusChangeStart(com.baidu.mapapi.map.MapStatus bdMapStatus, int i) {

        }

        @Override
        public void onMapStatusChange(com.baidu.mapapi.map.MapStatus bdMapStatus) {
            MapStatus mapStatus = new MapStatus();
            mapStatus.target = new LatLng(bdMapStatus.target.latitude, bdMapStatus.target.longitude);
            if (onMapStatusChangeListener != null) {
                onMapStatusChangeListener.onMapStatusChange(mapStatus);
            }
            if (centerMarker != null) {
                // 锁定在中心的marker移到中心，
                centerMarker.setPosition(bdMapStatus.target);
            }
        }

        @Override
        public void onMapStatusChangeFinish(com.baidu.mapapi.map.MapStatus bdMapStatus) {
            MapStatus mapStatus = new MapStatus();
            mapStatus.target = new LatLng(bdMapStatus.target.latitude, bdMapStatus.target.longitude);
            if (onMapStatusChangeListener != null) {
                onMapStatusChangeListener.onMapStatusChangeFinish(mapStatus);
            }
            if (centerMarker != null) {
                // 锁定在中心的marker移到中心，
                centerMarker.setPosition(bdMapStatus.target);
            }
        }
    }
}
