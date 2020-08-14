package com.sk.weichat.ui.map;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.sk.weichat.R;
import com.sk.weichat.map.MapHelper;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * This shows how to create a simple activity with a map and a marker on the map.
 */
public class MapActivity extends FragmentActivity {
    private double latitude;
    private double longitude;
    private String address;

    private MapHelper.Picker mPicker;

    public static boolean isExistence(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        List<String> packageNames = new ArrayList<String>();
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        return packageNames.contains(packageName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        if (getIntent() != null) {
            latitude = getIntent().getDoubleExtra("latitude", 0);
            longitude = getIntent().getDoubleExtra("longitude", 0);
            address = getIntent().getStringExtra("address");
        }
        initView();
        initEvent();
    }

    private void initView() {
        mPicker = MapHelper.getInstance().getPicker(this);
        getLifecycle().addObserver(mPicker);
        FrameLayout container = findViewById(R.id.map_view_container);
        final MapHelper.LatLng latLng = new MapHelper.LatLng(latitude, longitude);
        mPicker.attack(container, new MapHelper.OnMapReadyListener() {
            @Override
            public void onMapReady() {
                mPicker.moveMap(latLng);
                mPicker.addMarker(latLng, R.drawable.icon_gcoding, "");
            }
        });

        TextView mAddressTv = findViewById(R.id.address_tv);
        if (!TextUtils.isEmpty(address)) {
            mAddressTv.setText(address);
        }
    }

    private void initEvent() {
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.dh_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog bottomDialog = new Dialog(MapActivity.this, R.style.BottomDialog);
                View contentView = LayoutInflater.from(MapActivity.this).inflate(R.layout.map_dialog, null);
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
                        if (isExistence(getApplicationContext(), "com.baidu.BaiduMap")) {//传入指定应用包名
                            try {
                                Intent intent = Intent.getIntent("intent://map/direction?" + "destination=latlng:" + latitude + "," + longitude + "|name:我的目的地"
                                        + "&mode=driving&" + "region=北京" + "&src=慧医#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
                                startActivity(intent);
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                        } else {// 未安装
                            Toast.makeText(getApplicationContext(), R.string.tip_no_baidu_map, Toast.LENGTH_LONG).show();
                            try {
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
                        if (isExistence(getApplicationContext(), "com.autonavi.minimap")) {
                            try {
                                Intent intent = Intent.getIntent("androidamap://navi?sourceApplication=慧医&poiname=我的目的地&lat=" +
                                        latitude + "&lon=" + longitude + "&dev=0");
                                startActivity(intent);
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.tip_no_amap, Toast.LENGTH_LONG).show();
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
                        if (isExistence(getApplicationContext(), "com.google.android.apps.maps")) {
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude + ", + Sydney +Australia");
                            Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            intent.setPackage("com.google.android.apps.maps");
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.tip_no_google_map, Toast.LENGTH_LONG).show();
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
    }
}
