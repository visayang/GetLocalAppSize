package com.visa.getlocalappsize;


public class AppInfo
{
    public String appName;
    public String packageName;
    public String versionName;
    public int versionCode;
    public String firstInstallTime;
    public String lastUpdateTime;
    public String appSize;

    //public byte[] appIcon;  需要的自己打开

    @Override
    public String toString() {
        return "AppInfo{" +
                "appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionCode=" + versionCode +
                ", firstInstallTime='" + firstInstallTime + '\'' +
                ", lastUpdateTime='" + lastUpdateTime + '\'' +
                ", appSize='" + appSize + '\'' +
                '}';
    }
}
