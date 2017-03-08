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

    public static String responseData = null;

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

    public void returnJson(String add) {
        String address = add;
        new MyThread(address).start();
    }

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
