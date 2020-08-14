package com.sk.weichat.ui.me;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.widget.ImageViewCompat;

import com.sk.weichat.AppConfig;
import com.sk.weichat.R;
import com.sk.weichat.broadcast.OtherBroadcast;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.CommonAdapter;
import com.sk.weichat.util.CommonViewHolder;
import com.sk.weichat.util.LocaleHelper;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.view.SelectionFrame;
import com.sk.weichat.view.window.WindowShowService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zq on 2017/8/26 0026.
 * <p>
 * 切换语言
 */
public class SwitchLanguage extends BaseActivity {
    private ListView mListView;
    private LanguageAdapter languageAdapter;
    private List<Language> languages;
    private String currentLanguage;
    private TextView tvTitle;

    public static Map<String, String> getLanguageFullnameMap() {
        // 初始化语言数据, 语言名称本身不做国际化，
        Map<String, String> lMap = new LinkedHashMap<>();
        lMap.put("zh", "简体中文");
        lMap.put("TW", "繁體中文");
        lMap.put("en", "English");
        lMap.put("vi", "ViệtName");
        lMap.put("tl", "Pilipino");
        lMap.put("in", "Indonesia");
        return lMap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_activity_switch_language);
        initView();
    }

    protected void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.switch_language));

        // 当前语言
        currentLanguage = LocaleHelper.getLanguage(this);
        Log.e("zq", "当前语言:" + currentLanguage);
        languages = new ArrayList<>();
        Map<String, String> lMap = getLanguageFullnameMap();
        for (String key : lMap.keySet()) {
            String value = lMap.get(key);
            Language l = new Language();
            l.setFullName(value);
            l.setAbbreviation(key);
            languages.add(l);
        }
        initUI();
    }

    void initUI() {
        mListView = (ListView) findViewById(R.id.lg_lv);
        languageAdapter = new LanguageAdapter(this, languages);
        mListView.setAdapter(languageAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 切换选中语言
                switchLanguage(languages.get(position).getAbbreviation());
            }
        });
    }

    private void switchLanguage(String language) {
        SelectionFrame selectionFrame = new SelectionFrame(this);
        selectionFrame.setSomething(null, getString(R.string.tip_change_language_success), new SelectionFrame.OnSelectionFrameClickListener() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void confirmClick() {
                stopService(new Intent(mContext, WindowShowService.class));
                // 切换语言
                LocaleHelper.setLocale(mContext, language);
                // 设置语言
                LocaleHelper.onAttach(mContext, "zh");

                //发送广播  重新拉起app
                Intent intent = new Intent(OtherBroadcast.BROADCASTTEST_ACTION);
                intent.setComponent(new ComponentName(AppConfig.sPackageName, AppConfig.myBroadcastReceiverClass));
                sendBroadcast(intent);
                currentLanguage = LocaleHelper.getLanguage(SwitchLanguage.this);
                languageAdapter.notifyDataSetInvalidated();
            }
        });
        selectionFrame.show();
    }


    class LanguageAdapter extends CommonAdapter<Language> {
        LanguageAdapter(Context context, List<Language> data) {
            super(context, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommonViewHolder viewHolder = CommonViewHolder.get(mContext, convertView, parent,
                    R.layout.item_switch_language, position);
            TextView language = viewHolder.getView(R.id.language);
            language.setText(data.get(position).getFullName());
            ImageView check = viewHolder.getView(R.id.check);
            if (data.get(position).getAbbreviation().equals(currentLanguage)) {
                check.setVisibility(View.VISIBLE);
                ImageViewCompat.setImageTintList(check, ColorStateList.valueOf(SkinUtils.getSkin(mContext).getAccentColor()));
            } else {
                check.setVisibility(View.GONE);
            }
            return viewHolder.getConvertView();
        }
    }

    class Language {
        String FullName;    // 全称
        String Abbreviation;// 简称

        public String getFullName() {
            return FullName;
        }

        public void setFullName(String fullName) {
            FullName = fullName;
        }

        public String getAbbreviation() {
            return Abbreviation;
        }

        public void setAbbreviation(String abbreviation) {
            Abbreviation = abbreviation;
        }
    }
}
