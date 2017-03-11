package com.example.zy.octopusweather;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.zy.octopusweather.gson.Forecast;
import com.example.zy.octopusweather.gson.Weather;
import com.example.zy.octopusweather.service.AutoUpdateService;
import com.example.zy.octopusweather.service.NotifyService;
import com.example.zy.octopusweather.util.HttpUtil;
import com.example.zy.octopusweather.util.QuerryPicture;
import com.example.zy.octopusweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by zhangyi on 2017/3/6.
 */

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "zy_WeatherActivity";

    //记录NotifyService是否在工作，1工作，0不工作
    private static int is_notifyService_ok;
    private NotifyService.NotifyBinder notifyBinder;

    //控件定义
    public DrawerLayout drawerLayout;
    public SwipeRefreshLayout swipeRefresh;
    private ScrollView weatherLayout;
    private Button navButton;
    private TextView titleCity;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private TextView sourceFromText;
    private ImageView bingPicImg;
    private String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        is_notifyService_ok = 0;
        //融合背景与状态栏（Android5.0以上系统支持此功能）
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        // 初始化各控件
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        sourceFromText = (TextView) findViewById(R.id.source_from_text);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);

        //获取默认SharedPreferences
        final SharedPreferences prefs = getDefaultSharedPreferences(this);
        //获取SharedPreferences中缓存的天气数据
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
    }

    /**
     * bindService回调方法
     * return mBinder
     */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "[info]onServiceDisconnected执行完毕");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "[info] onServiceConnected开始执行");
            notifyBinder = (NotifyService.NotifyBinder) service;
            //使用notifyBinder调用方法处理相应逻辑
            SharedPreferences p = getDefaultSharedPreferences(WeatherActivity.this);
            Weather weather = Utility.handleWeatherResponse(p.getString("weather", "error"));
            String cityName = weather.basic.cityName;//城市名称
            String weatherInfo = weather.now.more.info;//天气状态
            String degree = weather.now.temperature + "℃";//当前气温
            //数据传输
            notifyBinder.transData(cityName,weatherInfo,degree);
            //最后显示通知
            notifyBinder.showNotify();
            Log.d(TAG, "[info] onServiceConnected执行完毕");
        }
    };
    /**
     * 根据天气id请求城市天气信息。
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=a6871788be42424d9aca8f7c6048137a";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                            Toast.makeText(WeatherActivity.this, "获取天气信息成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(WeatherActivity.this, "此城市天气情况不详", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        sourceFromText.setText("天气数据来源：和风天气\n服务器数据更新时间：" + updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            ImageView weatherLogo = (ImageView) view.findViewById(R.id.weather_logo);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            weatherLogo.setImageResource(QuerryPicture.querryPic(forecast.more.info));
            maxText.setText(forecast.temperature.max + "℃");
            minText.setText(forecast.temperature.min + "℃");
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        } else {
            aqiText.setText(" ");
            pm25Text.setText(" ");
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);


        if (is_notifyService_ok == 0) {
            is_notifyService_ok = 1;
            Intent start = new Intent(this, NotifyService.class);
            Log.d(TAG, "[info]000开始启动NotifyService");
            startService(start); // 启动服务
            bindService(start, connection, BIND_AUTO_CREATE); // 绑定服务
            Log.d(TAG, "[info]000启动成功NotifyService");
        } else {
            Intent stop = new Intent(this,NotifyService.class);
            Log.d(TAG, "[info]111开始停止NotifyService");
            stopService(stop); // 停止服务
            unbindService(connection); // 解绑服务
            Log.d(TAG, "[info]111停止成功NotifyService");
            Intent start = new Intent(this, NotifyService.class);
            Log.d(TAG, "[info]111开始启动新NotifyService");
            startService(start); // 重新启动服务
            bindService(start, connection, BIND_AUTO_CREATE); // 重新绑定服务
            Log.d(TAG, "[info]111新服务启动成功NotifyService");
        }

        //开启后台定时更新服务
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
        Log.d(TAG, "[info]AutoUpdateService后台定时更新已启动");
    }

}
