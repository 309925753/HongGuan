package com.sk.weichat.ui.tool;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.bean.TableConstant;
import com.sk.weichat.db.dao.TableConstantDao;
import com.sk.weichat.ui.base.ActionBackActivity;
import com.sk.weichat.view.PinnedSectionListView;
import com.sk.weichat.view.PinnedSectionListView.PinnedSectionListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择常量
 *
 * @author Dean Tao
 * @version 1.0
 */
public class SelectConstantSectionActivity extends ActionBackActivity {

    private PinnedSectionListView mListView;

    private String mTitle;
    private int mId;
    private TableConstant mConstant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            mId = getIntent().getIntExtra(SelectConstantActivity.EXTRA_CONSTANT_ID, 0);
            mTitle = getIntent().getStringExtra(SelectConstantActivity.EXTRA_CONSTANT_TITLE);
        }
        if (TextUtils.isEmpty(mTitle)) {
            mConstant = TableConstantDao.getInstance().getConstant(mId);
        }
        setContentView(R.layout.activity_simple_pinned_list);
        initView();
    }

    private void initView() {
        getSupportActionBar().setTitle(getFullTitle());
        mListView = (PinnedSectionListView) findViewById(R.id.list_view);

        List<Item> items = generateDataset(mId);
        if (items == null) {
            return;
        }
        mListView.setShadowVisible(true);
        mListView.setAdapter(new ConstantAdapter(items));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item item = (Item) parent.getItemAtPosition(position);
                if (item.getType() == Item.SECTION) {
                    return;
                }
                TableConstant constant = item.getTableConstant();
                if (constant.getMore() == 0) {// 没有更多直接返回
                    result(constant.getId(), constant.getName());
                } else {
                    TableConstant.select(SelectConstantSectionActivity.this, constant.getId(), getFullTitle(), 1);
                }
            }
        });
    }

    private void result(int id, String name) {
        Intent intent = new Intent();
        intent.putExtra(SelectConstantActivity.EXTRA_CONSTANT_ID, id);
        intent.putExtra(SelectConstantActivity.EXTRA_CONSTANT_NAME, name);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            int id = data.getIntExtra(SelectConstantActivity.EXTRA_CONSTANT_ID, 0);
            String name = data.getStringExtra(SelectConstantActivity.EXTRA_CONSTANT_NAME);
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

    public List<Item> generateDataset(int topNoteId) {
        List<TableConstant> subConstants = TableConstantDao.getInstance().getSubConstants(mId);
        if (subConstants == null || subConstants.size() <= 0) {
            return null;
        }
        List<Item> items = new ArrayList<Item>();
        for (TableConstant constant : subConstants) {
            List<TableConstant> subSubConstants = TableConstantDao.getInstance().getSubConstants(constant.getId());
            if (subSubConstants != null && subSubConstants.size() > 0) {
                items.add(new Item(Item.SECTION, constant));
                for (TableConstant subConstant : subSubConstants) {
                    items.add(new Item(Item.ITEM, subConstant));
                }
            }
        }
        return items;
    }

    private class Item {
        public static final int ITEM = 0;
        public static final int SECTION = 1;
        public final int type;
        public final TableConstant tableConstant;

        public Item(int type, TableConstant tableConstant) {
            this.type = type;
            this.tableConstant = tableConstant;
        }

        public int getType() {
            return type;
        }

        public TableConstant getTableConstant() {
            return tableConstant;
        }

    }

    private class ConstantAdapter extends BaseAdapter implements PinnedSectionListAdapter {
        private List<Item> items;

        public ConstantAdapter(List<Item> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
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
            return items.get(position).getType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(SelectConstantSectionActivity.this).inflate(R.layout.row_constant_select, parent, false);
            }
            TextView textView = (TextView) convertView;
            Item item = items.get(position);
            if (item.getType() == Item.SECTION) {
                textView.setTextColor(parent.getResources().getColor(R.color.dark_dark_grey));
                textView.setBackgroundColor(parent.getResources().getColor(R.color.light_grey));
            }
            textView.setText(item.getTableConstant().getName());
            return convertView;
        }

        @Override
        public boolean isItemViewTypePinned(int viewType) {
            return viewType == Item.SECTION;
        }

    }

}
