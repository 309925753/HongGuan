package com.sk.weichat.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.List;


@SuppressWarnings("WeakerAccess")
public abstract class MapHelper {
    private static final String TAG = "MapHelper";

    // 默认百度，必须要是能用的地图作为默认，其他地图无法使用时会改用这个地图，
    // 可以在Application中初始化这个type，
    private static final MapType DEFAULT_MAP_TYPE = MapType.BAIDU;
    private static MapType sMapType = DEFAULT_MAP_TYPE;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static MapType getMapType() {
        return sMapType;
    }

    public static void setMapType(MapType type) {
        MapHelper.sMapType = type;
    }

    public static void initContext(Context context) {
        MapHelper.context = context.getApplicationContext();
    }

    public static MapHelper getInstance() {
        return getInstance(sMapType);
    }

    @MainThread
    public static MapHelper getInstance(MapType mapType) {
        MapHelper result = null;
        switch (mapType) {
            case BAIDU:
                result = BaiduMapHelper.getInstance(context);
                break;
            case GOOGLE:
                result = GoogleMapHelper.getInstance(context);
                break;
        }
        if (!result.isAvailability()) {
            Log.d(TAG, "getInstance: 设备不支持该地图, " + mapType);
            result = getInstance(DEFAULT_MAP_TYPE);
        }
        return result;
    }

    /**
     * 判断该地图是否可用，主要是谷歌地图依赖谷歌框架，
     * 子类中判断，
     *
     * @return 返回该地图是否可用，
     */
    public boolean isAvailability() {
        return true;
    }

    /**
     * TODO: 经纬度标准没有统一，百度的经纬度给谷歌不准，
     * TODO: 没做几次重试，
     *
     * @param onSuccessListener 成功回调，可空，
     * @param onErrorListener   成功回调，可空，
     * @throws SecurityException 没有位置权限，
     */
    abstract public void requestLatLng(@Nullable OnSuccessListener<LatLng> onSuccessListener,
                                       @Nullable OnErrorListener onErrorListener);

    /**
     * 请求周边位置信息，
     *
     * @param onSuccessListener 成功回调，可空，
     * @param onErrorListener   失败回调，可空，
     */
    public final void requestPlaceList(@Nullable final OnSuccessListener<List<Place>> onSuccessListener,
                                       @Nullable final OnErrorListener onErrorListener) throws SecurityException {
        requestLatLng(new OnSuccessListener<LatLng>() {
            @Override
            public void onSuccess(LatLng latLng) {
                requestPlaceList(latLng, onSuccessListener, onErrorListener);
            }
        }, onErrorListener);
    }

    /**
     * 请求周边位置信息，
     *
     * @param latLng            经纬度，不可空，
     * @param onSuccessListener 成功回调，可空，
     * @param onErrorListener   失败回调，可空，
     */
    abstract public void requestPlaceList(LatLng latLng,
                                          @Nullable OnSuccessListener<List<Place>> onSuccessListener,
                                          @Nullable OnErrorListener onErrorListener);

    /**
     * TODO: 城市名称字符串不能保证和百度的一致，语言和是否包含“市”字样都没测试，
     *
     * @param latLng            经纬度，不可空，
     * @param onSuccessListener 成功回调，可空，
     * @param onErrorListener   失败回调，可空，
     */
    abstract public void requestCityName(LatLng latLng,
                                         @Nullable OnSuccessListener<String> onSuccessListener,
                                         @Nullable OnErrorListener onErrorListener);

    abstract public Picker getPicker(Context context);

    abstract public String getStaticImage(LatLng latLng);

    public enum MapType {
        BAIDU, GOOGLE
    }

    /**
     * 转换所有地图sdk中拖动地图的监听，
     * <p>
     * TODO: 暂不完全，只转换了用到的部分，也就是拖动目的地经纬度，
     */
    public interface OnMapStatusChangeListener {
        // 改变开始
        void onMapStatusChangeStart(MapStatus mapStatus);

        void onMapStatusChange(MapStatus mapStatus);

        void onMapStatusChangeFinish(MapStatus mapStatus);
    }

    public interface OnMapReadyListener {
        /**
         * 等地图准备好了才能开始一些操作，
         */
        void onMapReady();
    }

    public interface OnSuccessListener<T> {
        void onSuccess(T t);
    }

    public interface OnErrorListener {
        void onError(Throwable t);
    }

    public interface SnapshotReadyCallback {
        void onSnapshotReady(Bitmap bitmap);
    }

    public interface OnMarkerClickListener {
        void onMarkerClick(Marker marker);
    }

    public static class Place {
        private String name;
        private String address;
        private LatLng latLng;

        public Place(String name, String address, LatLng latLng) {
            this.name = name;
            this.address = address;
            this.latLng = latLng;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public LatLng getLatLng() {
            return latLng;
        }
    }

    public static class LatLng {
        private double latitude;
        private double longitude;

        public LatLng(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        @Override
        public String toString() {
            return "LatLng{" +
                    "latitude=" + latitude +
                    ", longitude=" + longitude +
                    '}';
        }
    }

    public static class MapStatus {
        public LatLng target;

        @Override
        public String toString() {
            return "MapStatus{" +
                    "target=" + target +
                    '}';
        }
    }

    /**
     * 子类继承封装选择位置视图相关一切，
     * 实现了移动地图，显示标记，
     * TODO: 没实现百度的InfoWindow, 点击标记显示信息，
     */
    public static abstract class Picker implements LifecycleObserver {
        @Nullable
        protected OnMapStatusChangeListener onMapStatusChangeListener;
        @Nullable
        protected OnMarkerClickListener onMarkerClickListener;

        public abstract void attack(FrameLayout container, @Nullable OnMapReadyListener listener);

        public void setOnMapStatusChangeListener(@Nullable OnMapStatusChangeListener listener) {
            this.onMapStatusChangeListener = listener;
        }

        public abstract View getMapView();

        /**
         * 截取指定宽高，如果过大就按比例缩小，
         */
        public abstract void snapshot(Rect rect, SnapshotReadyCallback callback);

        public abstract void showMyLocation(LatLng latLng);

        public boolean isBusy() {
            View mapView = getMapView();
            if (mapView == null) {
                return false;
            }
            return mapView.getParent() != null;
        }

        public void addCenterMarker(@DrawableRes int res, @Nullable String info) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), res);
            addCenterMarker(bitmap, info);
        }

        public abstract void addCenterMarker(Bitmap bitmap, @Nullable String info);

        public void addMarker(LatLng latLng, @DrawableRes int res, @Nullable String info) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), res);
            addMarker(latLng, bitmap, info);
        }

        /**
         * 谷歌地图要求在主线程，
         */
        @MainThread
        public abstract void addMarker(LatLng latLng, Bitmap bitmap, @Nullable String info);

        /**
         * 谷歌地图要求在主线程，
         */
        @MainThread
        public abstract void clearMarker();

        public void moveMap(LatLng latLng) {
            // 默认不要动画，闪现过去，
            moveMap(latLng, false);
        }

        public void setOnMarkerClickListener(OnMarkerClickListener onMarkerClickListener) {
            this.onMarkerClickListener = onMarkerClickListener;
        }

        public abstract LatLng currentLatLng();

        public abstract void moveMap(LatLng latLng, boolean anim);

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        void create() {
            Log.d(TAG, "create: ");
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        void start() {
            Log.d(TAG, "start: ");
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        void resume() {
            Log.d(TAG, "resume: ");
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        void pause() {
            Log.d(TAG, "pause: ");
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        void stop() {
            Log.d(TAG, "stop: ");
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        void destroy() {
            Log.d(TAG, "destroy: ");
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
        void any() {
            Log.d(TAG, "any: ");
        }
    }

    public static class Marker {
        private LatLng latLng;
        private Bitmap bitmap;
        @Nullable
        private String info;

        public LatLng getLatLng() {
            return latLng;
        }

        public void setLatLng(LatLng latLng) {
            this.latLng = latLng;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Nullable
        public String getInfo() {
            return info;
        }

        public void setInfo(@Nullable String info) {
            this.info = info;
        }
    }
}
