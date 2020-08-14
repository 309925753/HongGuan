package com.redchamber.util;

import android.content.Context;
import android.content.res.AssetManager;
import com.google.gson.Gson;
import com.redchamber.bean.IndustryJobBean;
import com.redchamber.bean.ProvinceCityBean;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * 解析json
 */
public class GetJsonDataUtil {

    public static String getJson(Context context, String fileName) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static ArrayList<ProvinceCityBean> parseProvinceCityBean(String result) {
        ArrayList<ProvinceCityBean> detail = new ArrayList<>();
        try {
            JSONArray data = new JSONArray(result);
            Gson gson = new Gson();
            for (int i = 0; i < data.length(); i++) {
                ProvinceCityBean entity = gson.fromJson(data.optJSONObject(i).toString(), ProvinceCityBean.class);
                detail.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return detail;
    }

    public static ArrayList<IndustryJobBean> parseJobListBean(String result) {
        ArrayList<IndustryJobBean> detail = new ArrayList<>();
        try {
            JSONArray data = new JSONArray(result);
            Gson gson = new Gson();
            for (int i = 0; i < data.length(); i++) {
                IndustryJobBean entity = gson.fromJson(data.optJSONObject(i).toString(), IndustryJobBean.class);
                detail.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return detail;
    }

}

