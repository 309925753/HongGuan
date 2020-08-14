package com.sk.weichat.ui.search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.nearby.UserListGatherActivity;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.view.NoLastDividerItemDecoration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class SearchAllActivity extends BaseActivity {
    private List<SearchResultAdapter<?, ?>> adapterList = new ArrayList<>();
    private View searchNew;
    private TextView tvSearchNewKey;
    private String historyKey;
    private View llSearchHistory;
    private List<String> searchHistoryList;
    private EditText mSearchEdit;
    private SearchHistoryAdapter searchHistoryAdapter;

    public static void start(Context ctx, String historyKey) {
        Intent intent = new Intent(ctx, SearchAllActivity.class);
        intent.putExtra("historyKey", historyKey);
        ctx.startActivity(intent);
    }

    public static List<String> loadSearchHistory(Context ctx, String historyKey) {
        if (TextUtils.isEmpty(historyKey)) {
            return new ArrayList<>();
        }
        SharedPreferences sp = getSp(ctx);
        List<String> ret = null;
        try {
            String jsonList = sp.getString(historyKey, null);
            ret = JSON.parseArray(jsonList, String.class);
        } catch (Exception e) {
            Reporter.unreachable(e);
        }
        if (ret == null) {
            ret = new ArrayList<>();
        }
        return ret;
    }

    public static void saveSearchHistory(Context ctx, String historyKey, List<String> list) {
        if (TextUtils.isEmpty(historyKey)) {
            return;
        }
        SharedPreferences sp = getSp(ctx);
        sp.edit().putString(historyKey, JSON.toJSONString(new LinkedHashSet<>(list)))
                .apply();
    }

    private static SharedPreferences getSp(Context ctx) {
        return ctx.getSharedPreferences("search_history", Context.MODE_PRIVATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_all);
        initActionBar();
        historyKey = getIntent().getStringExtra("historyKey");
        loadData();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadData() {
        SearchResultAdapter.OnSearchResultClickListener listener = () -> {
            searchHistoryList.add(0, mSearchEdit.getText().toString());
            saveSearchHistory(this, historyKey, searchHistoryList);
            searchHistoryAdapter.setData(SearchHistoryAdapter.toData(searchHistoryList));
        };
        adapterList.add(new ContactsSearchResultAdapter(this, coreManager.getSelf().getUserId(), listener));
        adapterList.add(new RoomSearchResultAdapter(this, coreManager.getSelf().getUserId(), listener));
        adapterList.add(new ChatHistorySearchResultAdapter(this, coreManager.getSelf().getUserId(), listener));
        searchHistoryList = loadSearchHistory(this, historyKey);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // 可能在单类型搜索结果页记录了新的搜索历史，
        searchHistoryList = loadSearchHistory(this, historyKey);
        searchHistoryAdapter.setData(SearchHistoryAdapter.toData(searchHistoryList));
    }

    private void initView() {
        mSearchEdit = findViewById(R.id.search_edit);
        findViewById(R.id.tvClearSearchHistory).setOnClickListener(v -> {
            searchHistoryList.clear();
            saveSearchHistory(this, historyKey, searchHistoryList);
            searchHistoryAdapter.setData(SearchHistoryAdapter.toData(searchHistoryList));
        });
        llSearchHistory = findViewById(R.id.llSearchHistory);
        RecyclerView rvSearchHistory = findViewById(R.id.rvSearchHistory);
        NoLastDividerItemDecoration dividerSearchHistory = new NoLastDividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerSearchHistory.setDrawable(getResources().getDrawable(R.drawable.common_divider));
        rvSearchHistory.addItemDecoration(dividerSearchHistory);
        rvSearchHistory.setLayoutManager(new LinearLayoutManager(this));
        searchHistoryAdapter = new SearchHistoryAdapter(SearchHistoryAdapter.toData(searchHistoryList), new SearchHistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SearchHistoryAdapter.Item item) {
                mSearchEdit.setText(item.chatHistory);
            }

            @Override
            public void onItemDelete(SearchHistoryAdapter.Item item) {
                searchHistoryList.remove(item.chatHistory);
                saveSearchHistory(mContext, historyKey, searchHistoryList);
                searchHistoryAdapter.setData(SearchHistoryAdapter.toData(searchHistoryList));
            }
        });
        rvSearchHistory.setAdapter(searchHistoryAdapter);
        ViewGroup llSearchResult = findViewById(R.id.llSearchResult);
        for (SearchResultAdapter<?, ?> adapter : adapterList) {
            View block = LayoutInflater.from(this).inflate(R.layout.block_search_result, llSearchResult, false);
            View more = block.findViewById(R.id.rlMore);
            adapter.attach(block, more);
            more.setOnClickListener(v -> {
                SearchSingleTypeActivity.start(this, adapter, mSearchEdit.getText().toString(), historyKey);
            });
            TextView tvMore = more.findViewById(R.id.tvMore);
            tvMore.setText(getString(R.string.search_result_more_place_holder, getString(adapter.getSearchType())));
            tvMore.setTextColor(SkinUtils.getSkin(this).getAccentColor());
            TextView ivResultType = block.findViewById(R.id.ivResultType);
            ivResultType.setText(adapter.getSearchType());
            RecyclerView recyclerView = block.findViewById(R.id.recyclerView);
            NoLastDividerItemDecoration divider = new NoLastDividerItemDecoration(this, DividerItemDecoration.VERTICAL);
            divider.setDrawable(getResources().getDrawable(R.drawable.divider_search_result_item));
            recyclerView.addItemDecoration(divider);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            llSearchResult.addView(block);
        }

        searchNew = LayoutInflater.from(this).inflate(R.layout.block_search_new_friend, llSearchResult, false);
        llSearchResult.addView(searchNew);
        searchNew.setOnClickListener(v -> {
            if (TextUtils.isEmpty(mSearchEdit.getText().toString().trim())) {
                return;
            }
            UserListGatherActivity.start(this, mSearchEdit.getText().toString());
            searchHistoryList.add(0, mSearchEdit.getText().toString());
            saveSearchHistory(this, historyKey, searchHistoryList);
        });
        tvSearchNewKey = searchNew.findViewById(R.id.tvSearchNewKey);
        tvSearchNewKey.setTextColor(SkinUtils.getSkin(this).getAccentColor());

        mSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                for (SearchResultAdapter<?, ?> adapter : adapterList) {
                    adapter.search(s.toString());
                }
                if (TextUtils.isEmpty(s)) {
                    searchNew.setVisibility(View.GONE);
                    llSearchHistory.setVisibility(View.VISIBLE);
                } else {
                    searchNew.setVisibility(View.VISIBLE);
                    tvSearchNewKey.setText(s);
                    llSearchHistory.setVisibility(View.GONE);
                }
            }
        });
    }
}
