package com.visa.getlocalappsize;

import android.app.AppOpsManager;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.util.Log;
import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommonUtils
{
    public final String TAG = "visa";
    List<AppInfo> myAppInfos = new ArrayList<AppInfo>();
    private boolean isGetAccess = false;
    private static Object obj = new Object();
    public AppInfoCallbackListener appInfoCallbackListener;
    public Gson gson;
    private static CommonUtils commonUtils;

    public static CommonUtils getInstance(){
        if(commonUtils == null){
            commonUtils = new CommonUtils();
        }
        return commonUtils;
    }


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isGetAccess = false;
            switch (msg.what){
                case 0x00:
                    Log.e(TAG, "handler  0x00 接收到 应用size 数据");
                    String appListJson = GetGson().toJson(myAppInfos);

                    if (appInfoCallbackListener != null)
                    {
                        appInfoCallbackListener.OnAppInfoCallback(appListJson.toString(),true);
                    }
                    break;
                case 0x01:
                    Log.e(TAG, "handler  0x01 没有接收到 应用size 数据");

                    String appListJson2 = GetGson().toJson(myAppInfos);

                    if (appInfoCallbackListener != null)
                    {
                        appInfoCallbackListener.OnAppInfoCallback(appListJson2.toString(),false);
                    }
                    break;
            }
        }
    };

    /**
     * 获取 应用size 的权限
     * @param context
     */
    public void getApplicationSizeAccess(Context context){
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }


    /*
    * ① 获取本地应用所有包名
    * ② 获取应用的大小
    * */
    public void GetInstallAppInfo(final Context context)
    {
        //同步锁 防止多次调用
        synchronized (obj){
            if (!isGetAccess){
                isGetAccess = true;
            }else {
                return;
            }
        }

//① 获取本地应用所有包名
        myAppInfos.clear();
        final PackageManager packageManager = context.getPackageManager();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
                    for (int i = 0; i < packageInfos.size(); i++) {

                        PackageInfo packageInfo = packageInfos.get(i);
                        //过滤掉系统app
                        if ((ApplicationInfo.FLAG_SYSTEM & packageInfos.get(i).applicationInfo.flags) != 0) {
                            Log.e(TAG,"systemPackageName:" + packageInfo.packageName);
                                continue;
                        }
                        //过滤掉自身app
                        if (packageInfo.packageName.equals(context.getPackageName()))
                        {
                            continue;
                        }

                        AppInfo appInfo = new AppInfo();
                        appInfo.appName = (String)packageInfo.applicationInfo.loadLabel(packageManager);
                        appInfo.packageName = packageInfo.packageName;
                        appInfo.versionName = packageInfo.versionName;
                        appInfo.versionCode = packageInfo.versionCode;
                        appInfo.firstInstallTime = Long.toString(packageInfo.firstInstallTime);
                        appInfo.lastUpdateTime = Long.toString(packageInfo.lastUpdateTime);
                        if (packageInfo.applicationInfo.loadIcon(packageManager) == null) {
                            continue;
                        }
                        //appInfo.appIcon = drawableToBytes(packageInfo.applicationInfo.loadIcon(packageManager));
                        myAppInfos.add(appInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    Log.i(TAG, myAppInfos.toString());
                }
//② 获取应用的大小
                String packageName = "";
                main: for (int i = 0;i<myAppInfos.size();i++) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Log.i(TAG, "targetAPI >= 26");
                        packageName = myAppInfos.get(i).packageName;

                        handler.removeMessages(0x00);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            StorageStatsManager storageStatsManager = null;
                            storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
                            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                            //获取所有应用的StorageVolume列表
                            List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();
                            //通过包名获取uid
                            StorageStats storageStats = null;
                            for (StorageVolume item : storageVolumes) {
                                String uuidStr = item.getUuid();
                                UUID uuid;
                                if (uuidStr == null) {
                                    uuid = StorageManager.UUID_DEFAULT;
                                } else {
                                    uuid = UUID.fromString(uuidStr);
                                }
                                int uid = getUid(context, packageName);
                                try {
                                    storageStats = storageStatsManager.queryStatsForUid(uuid, uid);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }catch (SecurityException e) {
                                    handler.removeMessages(0x00);
                                    handler.sendEmptyMessageDelayed(0x01, 1000);
                                    break main;
                                }
                                if (storageStats != null) {
                                    break;
                                }
                            }
                            if (storageStats != null) {
                                long appsize = storageStats.getCacheBytes() + storageStats.getDataBytes() + storageStats.getAppBytes();
                                Log.i(TAG, packageName+"已用空间" + FileUtils.size2(appsize));

                                boolean flag = false;
                                for (int j = 0; j < myAppInfos.size(); j++) {
                                    if (myAppInfos.get(j).packageName.equals(packageName)) {
                                        flag = true;
                                        myAppInfos.get(j).appSize = FileUtils.size2(appsize);
                                        break;
                                    }
                                }
                                if(!flag){
                                    Log.e(TAG, "没有找到对应包名应用" );
                                }
                                handler.sendEmptyMessageDelayed(0x00, 1000);
                            }else {
                                Log.e(TAG, " storageStats 为 null " );
                            }
                        }
                    } else {
                        Log.i(TAG, "targetAPI < 26");
                        getAppsize(context,myAppInfos.get(i).packageName);
                    }
                }
            }
        });
        thread.start();
    }

    public Gson GetGson()
    {
        if (gson == null)
        {
            gson = new Gson();
        }
        return gson;
    }

    /**
     * 获取应用大小8.0以下
     */
    public void getAppsize(final Context context, final String packageName) {
        try {
            handler.removeMessages(0x00);
            Method method = PackageManager.class.getMethod("getPackageSizeInfo", new Class[]{String.class, IPackageStatsObserver.class});
            // 调用 getPackageSizeInfo 方法，需要两个参数：1、需要检测的应用包名；2、回调
            method.invoke(context.getPackageManager(), packageName, new IPackageStatsObserver.Stub() {
                @Override
                public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) {
                    if (pStats != null) {

                        long appsize = pStats.cacheSize + pStats.dataSize + pStats.codeSize+pStats.externalCacheSize+pStats.externalCodeSize+pStats.externalDataSize+pStats.externalMediaSize+pStats.externalObbSize;
                        long sdCardSize = SDCardUtils.getSDCardSize();
                        float cound = ((float) appsize / sdCardSize) * 100;
                        Log.i(TAG, "size===" + appsize);
                        Log.i(TAG, "已用空间" + FileUtils.size(appsize));

                        boolean flag = false;
                        for (int i = 0; i < myAppInfos.size(); i++) {
                            if (myAppInfos.get(i).packageName.equals(pStats.packageName)) {
                                flag = true;
                                myAppInfos.get(i).appSize = FileUtils.size(appsize);
                                break;
                            }
                        }
                        if(!flag){
                            Log.e(TAG, "没有找到对应包名应用2" );
                        }
                        handler.removeMessages(0x00);
                        handler.sendEmptyMessageDelayed(0x00, 1000);
                    }else {
                        Log.e(TAG, packageName+"未获取到内存对象" );
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据应用包名获取对应uid
     */
    public static int getUid(Context context, String pakName) {
        try {
            return context.getPackageManager().getApplicationInfo(pakName, PackageManager.GET_META_DATA).uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static byte[] drawableToBytes(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * 是否获取应用大小权限
     */
    public static boolean isGetAccess(Context context){
        boolean granted = false;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppOpsManager appOps = null;
            appOps = (AppOpsManager) context
                    .getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                    android.os.Process.myUid(), context.getPackageName());
            granted = mode == AppOpsManager.MODE_ALLOWED;
        }else{
            return true;
        }
        return granted;
    }

    public void setAppInfoCallbackListener(AppInfoCallbackListener appInfoCallbackListenerProxy)
    {
        appInfoCallbackListener = appInfoCallbackListenerProxy;
    }

}
