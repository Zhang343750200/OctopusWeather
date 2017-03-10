package com.example.zy.octopusweather.util;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zhangyi on 2017/3/6.
 */

public class HttpUtil {

    private static final String TAG = "zy_HttpUtil";

    //全局静态标志位，同步主线程
    public static String responseData = null;

    /**
     * http请求封装
     */
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * 返回http请求的json数据
     * GPS模块
     */
    public void returnJson(String add) {
        String address = add;
        new MyThread(address).start();
    }

    /**
     * 自定义线程，实现参数传递
     * GPS模块
     */
    class MyThread extends Thread {
        private String address;

        public MyThread(String address) {
            this.address = address;
        }

        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(address).build();
                Response response = client.newCall(request).execute();
                responseData = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
