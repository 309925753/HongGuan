package com.sk.weichat.ui.tool;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.bean.TableConstant;
import com.sk.weichat.db.dao.TableConstantDao;
import com.sk.weichat.ui.base.ActionBackActivity;

import java.util.List;

/**
 * 选择常量
 *
 * @author Dean Tao
 * @version 1.0
 */
public class SelectConstantActivity extends ActionBackActivity {
    public static final String EXTRA_CONSTANT_ID    = "constant_id";
    public static final String EXTRA_CONSTANT_NAME  = "constant_name";
    public static final String EXTRA_CONSTANT_TITLE = "constant_title";

    private ListView mListView;

    private String mTitle;
    private int mId;
    private TableConstant mConstant;
    private List<TableConstant> mSubConstants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            mId = getIntent().getIntExtra(EXTRA_CONSTANT_ID, 0);
            mTitle = getIntent().getStringExtra(EXTRA_CONSTANT_TITLE);
        }
        if (TextUtils.isEmpty(mTitle)) {
            mConstant = TableConstantDao.getInstance().getConstant(mId);
        }
        mSubConstants = TableConstantDao.getInstance().getSubConstants(mId);
        setContentView(R.layout.activity_simple_list);
        initView();
    }

    private void initView() {
        getSupportActionBar().setTitle(getFullTitle());
        mListView = (ListView) findViewById(R.id.list_view);

        if (mSubConstants == null) {
            return;
        }
        mListView.setAdapter(new ConstantAdapter());
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TableConstant constant = mSubConstants.get(position);
                if (constant.getMore() == 0) {// 没有更多直接返回
                    result(constant.getId(), constant.getName());
                } else {
                    Intent intent = new Intent(SelectConstantActivity.this, SelectConstantActivity.class);
                    intent.putExtra(EXTRA_CONSTANT_ID, constant.getId());
                    intent.putExtra(EXTRA_CONSTANT_TITLE, getFullTitle());
                    startActivityForResult(intent, 1);
                }
            }
        });
    }

    private void result(int id, String name) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONSTANT_ID, id);
        intent.putExtra(EXTRA_CONSTANT_NAME, name);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            int id = data.getIntExtra(EXTRA_CONSTANT_ID, 0);
            String name = data.getStringExtra(EXTRA_CONSTANT_NAME);
            result(id, name);
        }
    }

    private String getFullTitle() {
        String select = getString(R.string.select);
        if (mConstant != null) {
            return select + mConstant.getName();
        } else if (mTitle != null) {
            return mTitle;
        } else {
            return select;
        }
    }

    private class ConstantAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSubConstants.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(SelectConstantActivity.this).inflate(R.layout.row_constant_select, parent, false);
            }
            TextView textView = (TextView) convertView;
            textView.setText(mSubConstants.get(position).getName());
            return convertView;
        }

    }
}
