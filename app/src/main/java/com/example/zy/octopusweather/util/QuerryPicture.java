package com.example.zy.octopusweather.util;

import com.example.zy.octopusweather.R;

/**
 * Created by zhangyi on 2017/3/7.
 */

public class QuerryPicture {

    public static int querryPic(String info) {

        //容错处理
        if (info == "" || info == null) {
            return R.drawable.w999;
        }

        //图标映射
        if(info.equals("晴"))
            return R.drawable.w100;
        if(info.equals("多云"))
            return R.drawable.w101;
        if(info.equals("晴间多云"))
            return R.drawable.w103;
        if(info.equals("阴"))
            return R.drawable.w104;
        if(info.equals("有风"))
            return R.drawable.w200;
        if(info.equals("阵雨"))
            return R.drawable.w300;
        if(info.equals("雷阵雨"))
            return R.drawable.w302;
        if(info.equals("小雨"))
            return R.drawable.w305;
        if(info.equals("中雨"))
            return R.drawable.w306;
        if(info.equals("大雨"))
            return R.drawable.w307;
        if(info.equals("毛毛雨/细雨"))
            return R.drawable.w309;
        if(info.equals("小雪"))
            return R.drawable.w400;
        if(info.equals("中雪"))
            return R.drawable.w401;
        if(info.equals("大雪"))
            return R.drawable.w402;
        if(info.equals("雨夹雪"))
            return R.drawable.w404;
        if(info.equals("雾"))
            return R.drawable.w501;
        if(info.equals("霾"))
            return R.drawable.w502;
        if(info.equals("沙尘暴"))
            return R.drawable.w507;

        //批处理
        if (info.indexOf("风") >= 0)
            return R.drawable.w200;
        if (info.indexOf("雨") >= 0)
            return R.drawable.w306;
        if (info.indexOf("风") >= 0)
            return R.drawable.w200;
        if (info.indexOf("沙") >= 0)
            return R.drawable.w507;

        //默认返回未知图标
        return R.drawable.w999;
    }

}
