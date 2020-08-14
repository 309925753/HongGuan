package com.sk.weichat.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.bean.Prefix;
import com.sk.weichat.db.InternationalizationHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.DisableEnterListener;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;


/**
 * Created by Administrator on 2017/4/21 0021.
 * 选择国家区号
 */
public class SelectPrefixActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    public static final int RESULT_MOBILE_PREFIX_SUCCESS = 110;
    public static final int REQUEST_MOBILE_PREFIX_LOGIN = 11123;
    private List<Prefix> prefixList;// 所有的国家
    private List<Prefix> searchResult;// 搜索的结果集合
    private boolean isSearch = false;// 当前的状态
    private TextView tv;

    private EditText searchEdt;
    private ImageView searchIv, titleIv;
    private ListView mLv;
    private PrefixAdapter prefixAdapter;
    private int mobilePrefix = 86;

    public SelectPrefixActivity() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_act_selectaddr);
        initView();
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
        tvTitle.setText(getString(R.string.select_county_area));

        searchIv = (ImageView) findViewById(R.id.search_iv);
        searchIv.setOnClickListener(this);

        searchEdt = (EditText) findViewById(R.id.search_edit);
        searchEdt.addTextChangedListener(new DisableEnterListener(searchEdt) {
            @Override
            public void shield() {
                search();
            }
        });

        mLv = (ListView) findViewById(R.id.lv_addr);
        prefixList = InternationalizationHelper.getPrefixList();
        prefixAdapter = new PrefixAdapter(prefixList);
        mLv.setAdapter(prefixAdapter);

        mLv.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Prefix prefixbean = (Prefix) prefixAdapter.getItem(i);
        int prefix = prefixbean.getPrefix();
        sendResult(prefix);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_iv_back:
                back();
                break;
            case R.id.search_iv:
                search();
                break;
        }
    }

    private void back() {
        if (isSearch) {
            isSearch = false;
            prefixAdapter.setData(prefixList);
        } else {
            finish();
        }
    }

    private void search() {
        String country = searchEdt.getText().toString().trim();
        if (!TextUtils.isEmpty(country)) {
            // 变为搜索状态
            isSearch = true;
            searchResult = InternationalizationHelper.getSearchPrefix(country);
            prefixAdapter.setData(searchResult);
        }
    }

    private void sendResult(int mobliePrefix) {
        Intent intent = new Intent();
        intent.putExtra(Constants.MOBILE_PREFIX, mobliePrefix);
        setResult(RESULT_MOBILE_PREFIX_SUCCESS, intent);
        finish();
    }

    private class PrefixAdapter extends BaseAdapter {
        private List<Prefix> data;

        public PrefixAdapter(List<Prefix> data) {
            sort(data);
            this.data = data;
        }

        private void sort(List<Prefix> list) {
            Object[] a = list.toArray();
            Comparator<Prefix> c = new Comparator<Prefix>() {
                @Override
                public int compare(Prefix o1, Prefix o2) {
                    return o1.getEnName().compareTo(o2.getEnName());
                }
            };
            Arrays.sort(a, (Comparator) c);
            ListIterator<Prefix> i = list.listIterator();
            for (Object e : a) {
                i.next();
                i.set((Prefix) e);
            }
        }


        public void setData(List<Prefix> data) {
            sort(data);
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(mContext, R.layout.a_item_resume_fnid, null);
                holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title_fnid_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (Locale.getDefault().getLanguage().equals("en")) {
                holder.tvTitle.setText(data.get(position).getEnName());
            } else {
                holder.tvTitle.setText(data.get(position).getCountry());
            }
            return convertView;
        }

        private class ViewHolder {
            TextView tvTitle;
        }
    }
}

