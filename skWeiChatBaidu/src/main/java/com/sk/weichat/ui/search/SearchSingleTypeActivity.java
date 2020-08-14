package com.sk.weichat.ui.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.view.NoLastDividerItemDecoration;

import java.lang.reflect.Constructor;
import java.util.List;

public class SearchSingleTypeActivity extends BaseActivity {
    private SearchResultAdapter<?, ?> adapter;
    private String searchKey;
    private String historyKey;
    private List<String> searchHistoryList;
    private TextView mSearchEdit;

    public static void start(Context ctx, SearchResultAdapter<?, ?> adapter, String str, String historyKey) {
        Intent intent = new Intent(ctx, SearchSingleTypeActivity.class);
        intent.putExtra("adapterName", adapter.getClass().getName());
        intent.putExtra("searchKey", str);
        intent.putExtra("historyKey", historyKey);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_single_type);
        historyKey = getIntent().getStringExtra("historyKey");
        searchHistoryList = SearchAllActivity.loadSearchHistory(this, historyKey);
        try {
            String adapterName = getIntent().getStringExtra("adapterName");
            Class<?> adapterClazz = Class.forName(adapterName);
            Constructor<?> constructor = adapterClazz.getDeclaredConstructor(Context.class, String.class, int.class, SearchResultAdapter.OnSearchResultClickListener.class);
            SearchResultAdapter.OnSearchResultClickListener listener = () -> {
                searchHistoryList.add(0, mSearchEdit.getText().toString());
                SearchAllActivity.saveSearchHistory(this, historyKey, searchHistoryList);
            };
            adapter = (SearchResultAdapter<?, ?>) constructor.newInstance(this, coreManager.getSelf().getUserId(), 0, listener);
        } catch (Exception e) {
            Reporter.unreachable(e);
            finish();
            return;
        }
        searchKey = getIntent().getStringExtra("searchKey");
        if (TextUtils.isEmpty(searchKey)) {
            Reporter.unreachable();
            finish();
            return;
        }
        initActionBar();

        mSearchEdit = findViewById(R.id.search_edit);
        mSearchEdit.setHint(adapter.getSearchType());
        View block = findViewById(R.id.llSearchResultBlock);
        adapter.attach(block, null);
        TextView ivResultType = block.findViewById(R.id.ivResultType);
        ivResultType.setText(adapter.getSearchType());
        RecyclerView recyclerView = block.findViewById(R.id.recyclerView);
        NoLastDividerItemDecoration divider = new NoLastDividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(getResources().getDrawable(R.drawable.divider_search_result_item));
        recyclerView.addItemDecoration(divider);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        mSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.search(s.toString());
            }
        });
        mSearchEdit.setText(searchKey);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
    }
}
