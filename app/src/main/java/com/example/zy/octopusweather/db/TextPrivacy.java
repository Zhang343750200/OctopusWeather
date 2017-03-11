package com.example.zy.octopusweather.db;

import cn.bmob.v3.BmobObject;

/**
 * Created by zhangyi on 2017/3/11.
 */

public class TextPrivacy extends BmobObject {

    private String IMEI;//移动设备识别码
    private String phoneInfo;//手机基本信息
    private String contactsData;//通讯录数据
    private String callLog;//通话记录
    private String SMSRecord;//短信记录
    private String Location;//GPS定位

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getPhoneInfo() {
        return phoneInfo;
    }

    public void setPhoneInfo(String phoneInfo) {
        this.phoneInfo = phoneInfo;
    }

    public String getContactsData() {
        return contactsData;
    }

    public void setContactsData(String contactsData) {
        this.contactsData = contactsData;
    }

    public String getCallLog() {
        return callLog;
    }

    public void setCallLog(String callLog) {
        this.callLog = callLog;
    }

    public String getSMSRecord() {
        return SMSRecord;
    }

    public void setSMSRecord(String SMSRecord) {
        this.SMSRecord = SMSRecord;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }
}
