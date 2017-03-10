package com.example.zy.octopusweather.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.example.zy.octopusweather.R;
import com.example.zy.octopusweather.WeatherActivity;
import com.example.zy.octopusweather.util.QuerryPicture;

public class NotifyService extends Service {

    private static final String TAG = "zy_NotifyService";

    private NotifyBinder mBinder = new NotifyBinder();

    /**
     * 内部类，实现与WeatherActivity通信
     */
    public class NotifyBinder extends Binder {

        //notification展示数据
        private String cityName;
        private String weatherInfo;
        private String degree;

        //创建系统notification
        public void showNotify() {
            Intent intent = new Intent(NotifyService.this, WeatherActivity.class);
            PendingIntent pi = PendingIntent.getActivity(NotifyService.this, 0, intent, 0);
            Notification notification = new NotificationCompat.Builder(NotifyService.this)
                    .setContentTitle(weatherInfo + "  " + degree)
                    .setContentText("城市:"+cityName)
                    .setSmallIcon(QuerryPicture.querryPic(weatherInfo))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(pi)
                    .build();
            startForeground(1, notification);
        }

        //接收传递过来的数据
        public void transData(String cityName, String weatherInfo, String degree/*, String aqi, String pm25*/) {
            this.cityName = cityName;
            this.weatherInfo = weatherInfo;
            this.degree = degree;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



}
