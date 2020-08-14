package com.sk.weichat.ui.nearby;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.sk.weichat.AppConstant;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.adapter.MarkerPagerAdapter;
import com.sk.weichat.bean.User;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.ImageLoadHelper;
import com.sk.weichat.map.MapHelper;
import com.sk.weichat.ui.base.EasyFragment;
import com.sk.weichat.ui.other.BasicInfoActivity;
import com.sk.weichat.util.AppUtils;
import com.sk.weichat.util.AsyncUtils;
import com.sk.weichat.util.AvatarUtil;
import com.sk.weichat.util.DisplayUtil;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.CircleImageView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;

/**
 * 附近的人-地图模式
 */
public class NearbyMapFragment extends EasyFragment implements View.OnClickListener {
    private static final String TAG = "map";
    ImageView ivLocation;
    ViewPager mViewPager;
    NearbyCardAdapter mAdapter;
    Map<String, User> hashMap = new HashMap<>();
    // 解决头像闪与OOM的问题 addMaker是取这里的数据，而不是hashMap内的数据
    Map<String, User> mNewMakerMap = new HashMap<>();
    private MapHelper mapHelper;
    private MapHelper.Picker picker;
    private MapHelper.LatLng beginLatLng;
    private MapHelper.LatLng currentLatLng;
    private String sex = null;
    private ImageView daohang;
    private List<User> mCurrentData = new ArrayList<>();

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_nearby_map;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            ivLocation = (ImageView) findViewById(R.id.iv_location);
            mViewPager = (ViewPager) findViewById(R.id.vp_nearby);
            daohang = (ImageView) findViewById(R.id.daohang);
            daohang.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 弹出底部抽屉
                    final Dialog bottomDialog = new Dialog(getActivity(), R.style.BottomDialog);
                    View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.map_dialog, null);
                    bottomDialog.setContentView(contentView);
                    ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
                    layoutParams.width = getResources().getDisplayMetrics().widthPixels;
                    contentView.setLayoutParams(layoutParams);
                    bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
                    bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
                    bottomDialog.show();
                    bottomDialog.findViewById(R.id.bdmap).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (AppUtils.isAppInstalled(getActivity(), "com.baidu.BaiduMap")) {
                                try {
                                    Intent intent01 = Intent.getIntent("intent://map/direction?" +
                                            //"origin=latlng:"+"34.264642646862,108.95108518068&" +   //起点  此处不传值默认选择当前位置
                                            "destination=latlng:" + beginLatLng.getLatitude() + "," + beginLatLng.getLongitude() + "|name:我的目的地" +        // 终点
                                            "&mode=driving&" +        // 导航路线方式
                                            "region=北京" +           //
                                            "&src=慧医#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
                                    startActivity(intent01); //启动调用
                                } catch (URISyntaxException e) {
                                }
                            } else {// 未安装
                                // market为路径，id为包名
                                // 显示手机上所有的market商店
                                Toast.makeText(getActivity(), R.string.tip_no_baidu_map, Toast.LENGTH_LONG).show();
                                try {// Nokia N1 平板测试 此处崩溃ActivityNotFoundException try catch 处理
                                    Uri uri = Uri.parse("market://details?id=com.baidu.BaiduMap");
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    bottomDialog.findViewById(R.id.gdmap).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (AppUtils.isAppInstalled(getActivity(), "com.autonavi.minimap")) {
                                try {
                                    Intent intent = Intent.getIntent("androidamap://navi?sourceApplication=慧医&poiname=我的目的地&lat=" + beginLatLng.getLatitude() + "&lon=" + beginLatLng.getLongitude() + "&dev=0");
                                    startActivity(intent);
                                } catch (URISyntaxException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getActivity(), R.string.tip_no_amap, Toast.LENGTH_LONG).show();
                                try {
                                    Uri uri = Uri.parse("market://details?id=com.autonavi.minimap");
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    bottomDialog.findViewById(R.id.ggmap).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (AppUtils.isAppInstalled(getActivity(), "com.google.android.apps.maps")) {
                                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + beginLatLng.getLatitude() + "," + beginLatLng.getLongitude() + ", + Sydney +Australia");
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                mapIntent.setPackage("com.google.android.apps.maps");
                                startActivity(mapIntent);
                            } else {
                                Toast.makeText(getActivity(), R.string.tip_no_google_map, Toast.LENGTH_LONG).show();
                                try {
                                    Uri uri = Uri.parse("market://details?id=com.google.android.apps.maps");
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            });
            ivLocation.setOnClickListener((v) -> {
                picker.moveMap(beginLatLng);
            });

            mapHelper = MapHelper.getInstance();
            picker = mapHelper.getPicker(requireContext());
            getLifecycle().addObserver(picker);
            FrameLayout container = findViewById(R.id.map_view_container);
            picker.attack(container, new MapHelper.OnMapReadyListener() {
                @Override
                public void onMapReady() {
                    mapHelper.requestLatLng(new MapHelper.OnSuccessListener<MapHelper.LatLng>() {
                        @Override
                        public void onSuccess(MapHelper.LatLng latLng) {
                            // 自己所在位置打上图标，
                            picker.showMyLocation(latLng);
                            // 记录开始时定位的位置，用来点击按钮跳回来，
                            beginLatLng = latLng;
                            currentLatLng = beginLatLng;
                            picker.moveMap(latLng);
                            /** 加载数据 */
                            loadDatas(sex);
                        }
                    }, new MapHelper.OnErrorListener() {
                        @Override
                        public void onError(Throwable t) {
                            ToastUtil.showToast(requireContext(), getString(R.string.tip_auto_location_failed) + t.getMessage());
                            // 总有个默认的经纬度，拿出来，
                            beginLatLng = picker.currentLatLng();
                            currentLatLng = beginLatLng;
                            picker.moveMap(beginLatLng);
                            loadDatas(sex);
                        }
                    });
                }
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
                    // 在这里地图改变完成后就获取经纬度,
                    // 每次移动地图后隐藏信息ViewPager
                    hideViewPager();
                    MapHelper.LatLng latLng = mapStatus.target;
                    currentLatLng = latLng;
/* TODO:
                    double distance = DistanceUtil.getDistance(latLng, cuttLatLng);
                    if (distance > 8000) {
                        mBaiduMap.clear();
                        hashMap.clear();
                    }
*/
                    loadDatas(sex);
                }
            });
            /** marker 点击监听 */
            picker.setOnMarkerClickListener(marker -> {
                String userId = marker.getInfo();
                int index = 0;
                if (!TextUtils.isEmpty(userId)) {
                    for (int i = 0; i < mCurrentData.size(); i++) {
                        if (mCurrentData.get(i).getUserId().equals(userId)) {
                            index = i;
                        }
                    }
                }
                mViewPager.setCurrentItem(index);
                showViewPager();
            });
            mAdapter = new NearbyCardAdapter();
            mViewPager.setAdapter(mAdapter);
        }
    }

    public void loadDatas(String sexUser) {
        if (currentLatLng == null) {
            // 没成功定位没有经纬度就不加载数据了，
            return;
        }
        double latitude = currentLatLng.getLatitude();
        double longitude = currentLatLng.getLongitude();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", "0");
        params.put("pageSize", "20");
        params.put("latitude", String.valueOf(latitude));
        params.put("longitude", String.valueOf(longitude));
        if (!TextUtils.isEmpty(sexUser)) {
            params.put("sex", sexUser);
        }
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.get().url(coreManager.getConfig().NEARBY_USER)
                .params(params)
                .build()
                .execute(new ListCallback<User>(User.class) {
                    @Override
                    public void onResponse(ArrayResult<User> result) {

                        List<User> datas = result.getData();
                        if (datas != null && datas.size() > 0) {
                            update(datas);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }

    public void refreshData(String mSex) {
        sex = mSex;
        loadDatas(mSex);
    }

    private void update(List<User> datas) {
        AsyncUtils.doAsync(this, new AsyncUtils.Function<AsyncUtils.AsyncContext<NearbyMapFragment>>() {
            @Override
            public void apply(AsyncUtils.AsyncContext<NearbyMapFragment> nearbyMapFragmentAsyncContext) throws Exception {
                mNewMakerMap.clear();// 清空之前的
                hashMap.clear();
                for (User user : datas) {
                    if (hashMap.containsKey(user.getUserId())) {
                        hashMap.clear();
                        mNewMakerMap.clear();
                        hashMap.put(user.getUserId(), user);
                        mNewMakerMap.put(user.getUserId(), user);// 添加新数据
                        // 重复数据
                    } else {
                        // 新数据
                        hashMap.put(user.getUserId(), user);
                        mNewMakerMap.put(user.getUserId(), user);// 添加新数据
                    }
                }
                AsyncUtils.runOnUiThread(this, new AsyncUtils.Function<AsyncUtils.Function<AsyncUtils.AsyncContext<NearbyMapFragment>>>() {
                    @Override
                    public void apply(AsyncUtils.Function<AsyncUtils.AsyncContext<NearbyMapFragment>> asyncContextFunction) throws Exception {
                        picker.clearMarker();
                        mAdapter.setData(hashMap);
                    }
                });
            }
        });
    }

    public void showViewPager() {
        if (mViewPager.getVisibility() == View.GONE) {
            mViewPager.setVisibility(View.VISIBLE);
            ivLocation.setVisibility(View.GONE);
        }
    }

    public void hideViewPager() {
        if (mViewPager.getVisibility() == View.VISIBLE) {
            mViewPager.setVisibility(View.GONE);
            ivLocation.setVisibility(View.VISIBLE);
        }
    }

    public void addMarker(String nickName, final double lat, final double lng, final String id) {
        AsyncUtils.doAsync(this, t -> {
            Reporter.post("设置附近人头像失败", t);
        }, c -> {
            try {
                String url = AvatarHelper.getAvatarUrl(id, true);
                Bitmap mBitmap = ImageLoadHelper.getBitmapCenterCrop(
                        getActivity(),
                        url,
                        id,
                        DisplayUtil.dip2px(getActivity(), 40), DisplayUtil.dip2px(getActivity(), 40)
                );
                c.uiThread(r -> {
                    picker.addMarker(new MapHelper.LatLng(lat, lng), getRoundedCornerBitmap(mBitmap), id);
                });
            } catch (ExecutionException executionException) {// 部分用户为默认头像，URL为空，导致ExecutionException
                try {
                    List<Object> bitmapList = new ArrayList();
                    bitmapList.add(nickName);
                    Bitmap avatar = AvatarUtil.getBuilder(getActivity())
                            .setShape(AvatarUtil.Shape.ROUND)
                            .setList(bitmapList)
                            .setTextSize(DisplayUtil.dip2px(getActivity(), 40))
                            .setTextColor(R.color.white)
                            .setTextBgColor(SkinUtils.getSkin(getActivity()).getAccentColor())
                            .setBitmapSize(DisplayUtil.dip2px(getActivity(), 40), DisplayUtil.dip2px(getActivity(), 40))
                            .create();
                    c.uiThread(r -> {
                        picker.addMarker(new MapHelper.LatLng(lat, lng), getRoundedCornerBitmap(avatar), id);
                    });
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            } catch (Exception exception) {
                Reporter.post("设置附近人头像失败", exception);
            }
        });
    }

    public Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outBitmap);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPX = bitmap.getWidth() / 2;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return outBitmap;
    }

    class NearbyCardAdapter extends MarkerPagerAdapter {


        private List<User> data = new ArrayList<>();

        @Override
        public View getView(View convertView, final int position) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.item_nearby_card, null);
                viewHolder = new ViewHolder();
                viewHolder.layout = (LinearLayout) convertView.findViewById(R.id.layout);
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.job_name_tv);
                viewHolder.ivHead = (CircleImageView) convertView.findViewById(R.id.iv_head);
                viewHolder.tvPhone = (TextView) convertView.findViewById(R.id.job_money_tv);
                viewHolder.tvDist = (TextView) convertView.findViewById(R.id.juli_tv);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final User user = data.get(position);
            AvatarHelper.getInstance().displayAvatar(user.getNickName(), user.getUserId(), viewHolder.ivHead, true);
            viewHolder.tvName.setText(user.getNickName());
            viewHolder.tvPhone.setText(user.getTelephone());
            String distance = DisplayUtil.getDistance(beginLatLng.getLatitude(), beginLatLng.getLongitude(), user);
            viewHolder.tvDist.setText(distance);
            viewHolder.layout.setOnClickListener(v -> {
                String userId = user.getUserId();
                Intent intent = new Intent(getActivity(), BasicInfoActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, userId);
                startActivity(intent);
            });
            return convertView;
        }

        @Override
        public int myGetCount() {
            return data.size();
        }

        /**
         * 偶现崩溃
         * line 546 addMarker()...
         * Caused by: java.util.ConcurrentModificationException
         * 添加 synchronized 进行同步
         *
         * @param hashMap
         */
        public synchronized void setData(Map<String, User> hashMap) {
            data.clear();
            mCurrentData.clear();
            Iterator iterator = hashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                User user = (User) entry.getValue();
                if (user != null && user.getLoc() != null) {
                    data.add(user);
                    // Todo 头像闪动的原因在这里，即界面内已经有该maker了，还在里面不停的add(有时OOM可能也是这里引起的)
                    // addMarker(user.getLoc().getLat(), user.getLoc().getLng(), user.getUserId(), index++);
                }
            }

            mCurrentData = data;

            Iterator mNewIterator = mNewMakerMap.entrySet().iterator();
            while (mNewIterator.hasNext()) {
                Map.Entry entry = (Map.Entry) mNewIterator.next();
                User user = (User) entry.getValue();
                if (user != null && user.getLoc() != null) {
                    // 已添加的maker不会重复添加了，即index已经不起做用了，现记录当前data(mCurrentData)，最后通过对比userId获取其index
                    // addMarker(user.getLoc().getLat(), user.getLoc().getLng(), user.getUserId(), index++);
                    addMarker(user.getNickName(), user.getLoc().getLat(), user.getLoc().getLng(), user.getUserId());
                }
            }
            notifyDataSetChanged();
        }
    }

    class ViewHolder {
        LinearLayout layout;
        TextView tvName;
        ImageView ivHead;
        TextView tvPhone;
        TextView tvDist;
    }
}
