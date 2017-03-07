package com.example.zy.octopusweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zhangyi on 2017/3/6.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;
    }
}
