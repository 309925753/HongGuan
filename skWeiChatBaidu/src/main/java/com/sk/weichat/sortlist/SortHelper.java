package com.sk.weichat.sortlist;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.sk.weichat.util.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SortHelper {

    public static void setSortCondition(BaseSortModel<?> mode, String name) {
        String wholeSpell = PingYinUtil.getPingYin(name);
        if (!TextUtils.isEmpty(wholeSpell)) {
            String firstLetter = Character.toString(wholeSpell.charAt(0));
            mode.setWholeSpell(wholeSpell);
            mode.setFirstLetter(firstLetter);
        } else {// 如果全拼为空，理论上是一种错误情况，因为这代表着昵称为空
            mode.setWholeSpell("#");
            mode.setFirstLetter("#");
        }
    }

    public static <T> List<BaseSortModel<T>> toSortedModelList(List<T> beanList, Map<String, Integer> existMap, NameMapping<T> mapping) {
        LogUtils.log("sort: size: " + beanList.size());
        List<BaseSortModel<T>> ret = new ArrayList<>(beanList.size());
        for (int i = 0; i < beanList.size(); i++) {
            BaseSortModel<T> mode = new BaseSortModel<>();
            mode.setBean(beanList.get(i));
            String name = mapping.getName(mode.getBean());
            if (name == null) {
                continue;
            }
            setSortCondition(mode, name);
            Integer exists = existMap.get(mode.firstLetter);
            if (exists == null) {
                exists = 0;
            }
            ++exists;
            existMap.put(mode.firstLetter, exists);
            ret.add(mode);
        }
        Collections.sort(ret, (o1, o2) -> {
            if (o1.getFirstLetter().equals("#")) {
                if (o2.getFirstLetter().equals("#")) {
                    return o1.getWholeSpell().compareTo(o2.getWholeSpell());
                } else {
                    return -1;
                }
            } else {
                if (o2.getFirstLetter().equals("#")) {
                    return 1;
                } else {
                    return o1.getWholeSpell().compareTo(o2.getWholeSpell());
                }
            }
        });
        LogUtils.log("sorted: size: " + beanList.size());
        return ret;
    }

    public interface NameMapping<T> {
        /**
         * 返回null表示过滤掉这个元素，
         */
        @Nullable
        String getName(T bean);
    }
}
