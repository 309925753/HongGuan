package com.sk.weichat.ui.tool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sk.weichat.BdLocationHelper;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.Area;
import com.sk.weichat.db.dao.AreasDao;
import com.sk.weichat.map.MapHelper;
import com.sk.weichat.ui.base.ActionBackActivity;
import com.sk.weichat.util.PermissionUtil;
import com.sk.weichat.util.ViewHolder;
import com.sk.weichat.view.PinnedSectionListView;
import com.sk.weichat.view.PinnedSectionListView.PinnedSectionListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 居住地
 */
// TODO: 统一使用BaseActivity, 注意这个Activity不要求登录，
public class SelectAreaActivity extends ActionBackActivity {
    /**
     * 选择国家、省份、城市、县区等区域信息
     */
    public static final String EXTRA_AREA_TYPE = "area_type";
    public static final String EXTRA_AREA_DEEP = "area_deep";// 选择的深度，即选择到哪一个级别终止选择，和area_type类型一样。如果类型为城市，那么选择城市后，就直接返回，不再选择县区
    public static final String EXTRA_AREA_PARENT_ID = "area_parent_id";
    public static final String EXTRA_COUNTRY_ID = "country_id";// 国家Id
    public static final String EXTRA_PROVINCE_ID = "province_id";// 省份Id
    public static final String EXTRA_CITY_ID = "city_id";// 城市Id
    public static final String EXTRA_COUNTY_ID = "county_id";// 区县Id
    public static final String EXTRA_COUNTRY_NAME = "country_name";// 国家Id
    public static final String EXTRA_PROVINCE_NAME = "province_name";// 省份Id
    public static final String EXTRA_CITY_NAME = "city_name";// 城市Id
    public static final String EXTRA_COUNTY_NAME = "county_name";// 区县Id
    private static final int LOCATION_ING = 0;// 定位中
    private static final int LOCATION_FIALED = 1;// 定位失败
    private static final int LOCATION_SUCCESS = 2;// 定位成功
    private PinnedSectionListView mListView;
    private int mParentId;// 当前父id
    private int mAreaType;// 类型
    private int mAreaDeep;// 选择深度
    private ConstantAdapter mAdapter;
    private int mLocationStatus = 0;
    private BroadcastReceiver mLocationUpdateReceiver;
    private Handler mHandler;

    private List<Item> mItems = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            mParentId = getIntent().getIntExtra(EXTRA_AREA_PARENT_ID, 0);
            mAreaType = getIntent().getIntExtra(EXTRA_AREA_TYPE, 0);
            mAreaDeep = getIntent().getIntExtra(EXTRA_AREA_DEEP, 0);
        }

        if (mAreaType != Area.AREA_TYPE_COUNTRY && mAreaType != Area.AREA_TYPE_PROVINCE && mAreaType != Area.AREA_TYPE_CITY
                && mAreaType != Area.AREA_TYPE_COUNTY) {// 没有传递数据进来，或传的数据有问题，默认为1，即选择国家
            mAreaType = Area.AREA_TYPE_COUNTRY;
        }
        if ((mAreaDeep != Area.AREA_TYPE_COUNTRY && mAreaDeep != Area.AREA_TYPE_PROVINCE && mAreaDeep != Area.AREA_TYPE_CITY && mAreaDeep != Area.AREA_TYPE_COUNTY)
                || mAreaDeep < mAreaType) {// 没有传递数据进来，或传的数据有问题,或者深度的数据小于本类型。比如类型是选省份，深度是国家，即数据错误
            mAreaDeep = mAreaType;// 即赋值为当前类型的深度，即选择就终止并返回
        }

        prepareData();
        setContentView(R.layout.activity_simple_pinned_list);
        PermissionUtil.requestLocationPermissions(this, 0x01);
        initView();

        if (hasAddtionCity()) {
            mHandler = new Handler();
            mLocationUpdateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(BdLocationHelper.ACTION_LOCATION_UPDATE)) {
                        mHandler.removeCallbacksAndMessages(null);
                        updateLocationCity();
                    }
                }
            };
            registerReceiver(mLocationUpdateReceiver, new IntentFilter(BdLocationHelper.ACTION_LOCATION_UPDATE));
            requestLocation();
        }
    }

    private void prepareData() {
        mItems = new ArrayList<Item>();
        if (hasAddtionCity()) {
            // 添加当前城市
            mItems.add(new Item(getString(R.string.current_location)));
            mItems.add(new Item(new Area()));// 显示定位的那个city

            // 添加热门城市
            mItems.add(new Item(getString(R.string.hot_city)));
            for (Area area : Area.HOT_CITYS) {
                mItems.add(new Item(area));
            }

            // 添加按省份选择城市
            mItems.add(new Item(getString(R.string.select_city_by_province)));
        }
        List<Area> areas = AreasDao.getInstance().getAreasByTypeAndParentId(mAreaType, mParentId);
        if (areas != null) {
            for (Area area : areas) {
                mItems.add(new Item(area));
            }
        }
    }

    private boolean hasAddtionCity() {
        return mAreaType == Area.AREA_TYPE_PROVINCE;
    }

    private void updateLocationCity() {
        mLocationStatus = LOCATION_ING;
        MapHelper.getInstance().requestLatLng(new MapHelper.OnSuccessListener<MapHelper.LatLng>() {
            @Override
            public void onSuccess(MapHelper.LatLng latLng) {
                MapHelper.getInstance().requestCityName(latLng, new MapHelper.OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        String cityName = MyApplication.getInstance().getBdLocationHelper().getCityName();
                        Area area = null;
                        if (!TextUtils.isEmpty(cityName)) {
                            area = AreasDao.getInstance().searchByName(cityName);
                        }
                        if (area != null) {
                            mLocationStatus = LOCATION_SUCCESS;
                            try {
                                mItems.get(1).setArea(area);
                            } catch (Exception e) {
                                Log.e(TAG, "设置地区失败，", e);
                                mLocationStatus = LOCATION_FIALED;
                            }
                        } else {
                            Log.e(TAG, "获取地区失败，", new RuntimeException("找不到城市：" + cityName));
                            mLocationStatus = LOCATION_FIALED;
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                }, new MapHelper.OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "获取城市名称失败，", t);
                        mLocationStatus = LOCATION_FIALED;
                    }
                });
            }
        }, new MapHelper.OnErrorListener() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "定位经纬度失败，", t);
                mLocationStatus = LOCATION_FIALED;
            }
        });
    }

    private void requestLocation() {
        if (MyApplication.getInstance().getBdLocationHelper().isLocationUpdate() && !TextUtils.isEmpty(MyApplication.getInstance().getBdLocationHelper().getCityName())) {
            updateLocationCity();
        } else {
            MyApplication.getInstance().getBdLocationHelper().requestLocation();
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLocationStatus = LOCATION_FIALED;
                    mAdapter.notifyDataSetChanged();
                }
            }, 5000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hasAddtionCity()) {
            mHandler.removeCallbacksAndMessages(null);
            unregisterReceiver(mLocationUpdateReceiver);
        }
    }

    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.select_provincevc_selprovince));

        mListView = (PinnedSectionListView) findViewById(R.id.list_view);
        if (mItems == null) {
            return;
        }
        mAdapter = new ConstantAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item item = (Item) parent.getItemAtPosition(position);
                if (item.getType() == Item.SECTION) {
                    return;
                }
                if (hasAddtionCity() && position == 1) {// 点击的是定位的那个城市
                    if (mLocationStatus == LOCATION_ING) {
                        return;
                    } else if (mLocationStatus == LOCATION_FIALED) {
                        requestLocation();
                        return;
                    } else {
                        Area area = item.getArea();
                        if (area.getId() == 0 || TextUtils.isEmpty(area.getName())) {
                            requestLocation();
                            return;
                        }
                    }
                }
                Area area = item.getArea();
                if (mAreaDeep > area.getType() && AreasDao.getInstance().hasSubAreas(area.getId())) {// 继续向下选择
                    Intent intent = new Intent(SelectAreaActivity.this, SelectAreaActivity.class);
                    intent.putExtra(EXTRA_AREA_PARENT_ID, area.getId());
                    int nextAreaType = area.getType() + 1;
                    intent.putExtra(EXTRA_AREA_TYPE, nextAreaType);
                    intent.putExtra(EXTRA_AREA_DEEP, mAreaDeep);
                    startActivityForResult(intent, 1);
                } else {// 返回
                    result(area);
                }
            }
        });
    }

    private void result(Area area) {
        if (area == null) {
            setResult(RESULT_CANCELED);
            finish();
        }
        Area countryArea = null;
        Area provinceArea = null;
        Area cityArea = null;
        Area countyArea = null;
        switch (area.getType()) {
            case Area.AREA_TYPE_COUNTRY:
                countryArea = area;
                break;
            case Area.AREA_TYPE_PROVINCE:
                provinceArea = area;
                break;
            case Area.AREA_TYPE_CITY:
                cityArea = area;
                break;
            case Area.AREA_TYPE_COUNTY:
            default:
                countyArea = area;
                break;
        }

        Intent intent = new Intent();
        if (countyArea != null) {
            intent.putExtra(EXTRA_COUNTY_ID, countyArea.getId());
            intent.putExtra(EXTRA_COUNTY_NAME, countyArea.getName());
            cityArea = AreasDao.getInstance().getArea(countyArea.getParent_id());
        }

        if (cityArea != null) {
            intent.putExtra(EXTRA_CITY_ID, cityArea.getId());
            intent.putExtra(EXTRA_CITY_NAME, cityArea.getName());
            provinceArea = AreasDao.getInstance().getArea(cityArea.getParent_id());
        }

        if (provinceArea != null) {
            intent.putExtra(EXTRA_PROVINCE_ID, provinceArea.getId());
            intent.putExtra(EXTRA_PROVINCE_NAME, provinceArea.getName());
            countryArea = AreasDao.getInstance().getArea(provinceArea.getParent_id());
        }

        if (countryArea != null) {
            intent.putExtra(EXTRA_COUNTRY_ID, countryArea.getId());
            intent.putExtra(EXTRA_COUNTRY_NAME, countryArea.getName());
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private String getFullTitle() {
        String select = getString(R.string.select);
        String text = "";
        if (mAreaType == Area.AREA_TYPE_COUNTRY) {
            text = getString(R.string.country);
        } else if (mAreaType == Area.AREA_TYPE_PROVINCE) {
            text = getString(R.string.province);
        } else if (mAreaType == Area.AREA_TYPE_CITY) {
            text = getString(R.string.city);
        } else if (mAreaType == Area.AREA_TYPE_COUNTY) {
            text = getString(R.string.county);
        }
        return select + text;
    }

    private class Item {
        public static final int ITEM = 0;
        public static final int SECTION = 1;
        public int type;
        public Area area;
        public String text;

        public Item(Area area) {  // 如果对area赋值，那么type为Item
            this.type = ITEM;
            this.area = area;
        }

        public Item(String text) {// 如果对text赋值，那么type为SECTION
            this.type = SECTION;
            this.text = text;
        }

        public int getType() {
            return type;
        }

        public Area getArea() {
            return area;
        }

        public void setArea(Area area) {
            this.area = area;
        }

        public String getText() {
            return text;
        }
    }

    private class ConstantAdapter extends BaseAdapter implements PinnedSectionListAdapter {
        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return mItems.get(position).getType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(SelectAreaActivity.this).inflate(R.layout.row_constant_select, parent, false);
            }
            TextView textView = ViewHolder.get(convertView, R.id.textview);
            View view = ViewHolder.get(convertView, R.id.view);
            Item item = mItems.get(position);
            if (item.getType() == Item.SECTION) {
                textView.setTextSize(13);
                textView.setTextColor(parent.getResources().getColor(R.color.text_value));
                textView.setBackgroundColor(getResources().getColor(R.color.normal_bg));
                textView.setText(item.getText());
                view.setVisibility(View.GONE);
            } else {
                if (hasAddtionCity() && position == 1) {// 正在定位的那个城市
                    switch (mLocationStatus) {
                        case LOCATION_FIALED:
                            textView.setText(getString(R.string.location_failed));
                            break;
                        case LOCATION_SUCCESS:
                            if (item.getArea() != null) {
                                textView.setText(item.getArea().getName());
                            }
                            break;
                        case LOCATION_ING:
                        default:
                            textView.setText(getString(R.string.locationing));
                            break;
                    }
                } else {
                    textView.setText(item.getArea().getName());
                }
                view.setVisibility(View.VISIBLE);
            }
            if (position == 0 || mItems.get(position - 1).getType() == Item.SECTION) {
                view.setVisibility(View.GONE);
            }
            return convertView;
        }

        @Override
        public boolean isItemViewTypePinned(int viewType) {
            return viewType == Item.SECTION;
        }
    }
}
