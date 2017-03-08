package com.example.zy.octopusweather.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.zy.octopusweather.db.City;
import com.example.zy.octopusweather.db.County;
import com.example.zy.octopusweather.db.Province;
import com.example.zy.octopusweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by zhangyi on 2017/3/6.
 */

public class Utility {

    private static final String TAG = "zy_Utility";

    /**
     * GPS定位数据解析
     * 如果GPS定位失败，自动返回 江苏苏州 天气状况
     */
    public static String get_CN_Number(String province, String city, String county, Context context) throws JSONException, IOException {

        //定位失败时的默认返回数据
        int id_pro = 16;
        int id_city = 116;
        String id_weather="CN101190401";

        //开启子线程获取省级Json数据
        HttpUtil hu_pro = new HttpUtil();
        hu_pro.returnJson("http://guolin.tech/api/china");
        while (HttpUtil.responseData == null) {}

        //返回的省级Json数据
        String responseProvince = HttpUtil.responseData;
        Log.d(TAG, "get_CN_Number: "+responseProvince);
        //获取省份id
        if (!TextUtils.isEmpty(responseProvince)) {
                JSONArray allProvinces = new JSONArray(responseProvince);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    if (provinceObject.getString("name").equals(province)) {
                        id_pro = provinceObject.getInt("id");
                    }
                }
        }
        //缓存清空
        HttpUtil.responseData = null;

        //开启子线程获取市级Json数据
        HttpUtil hu_city = new HttpUtil();
        hu_city.returnJson("http://guolin.tech/api/china/" + String.valueOf(id_pro));
        while (HttpUtil.responseData == null) {}

        //返回的市级Json数据
        String responseCity = HttpUtil.responseData;
        if (!TextUtils.isEmpty(responseCity)) {
            JSONArray allCities = new JSONArray(responseCity);
            for (int i = 0; i < allCities.length(); i++) {
                JSONObject cityObject = allCities.getJSONObject(i);
                if (cityObject.getString("name").equals(city)) {
                    id_city = cityObject.getInt("id");
                }
            }
        }

        //缓存清空
        HttpUtil.responseData = null;

        //开启子线程获取区级Json数据
        HttpUtil hu_county = new HttpUtil();
        hu_county.returnJson("http://guolin.tech/api/china/" + String.valueOf(id_pro) + "/" + String.valueOf(id_city));
        while (HttpUtil.responseData == null) {}

        //返回的区级Json数据
        String responseCounty = HttpUtil.responseData;
        if (!TextUtils.isEmpty(responseCounty)) {
            JSONArray allCounties = new JSONArray(responseCounty);
            for (int i = 0; i < allCounties.length(); i++) {
                JSONObject countyObject = allCounties.getJSONObject(i);
                if (countyObject.getString("name").equals(county)) {
                    id_weather = countyObject.getString("weather_id");
                }
            }
            for (int i = 0; i < allCounties.length(); i++) {
                JSONObject countyObject = allCounties.getJSONObject(i);
                if (countyObject.getString("name").equals(city)) {
                    id_weather = countyObject.getString("weather_id");
                }
            }
        }
        //缓存清空
        HttpUtil.responseData = null;
        return id_weather;
    }

    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将返回的JSON数据解析成Weather实体类
     */
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
