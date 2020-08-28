package com.visa.getlocalappsize;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;
import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

public class FileUtils
{

    /**
     * 获取内置sd卡路径
     *
     * @return
     */
    private static String getSdCardPath() {
        String path = Environment.getExternalStorageDirectory().getPath();
        return path;
    }

    /**
     * 获取外置SD卡路径
     *
     * @param context
     * @return
     */
    private static String getExternalSdCardPath(Context context) {
        String[] paths = getSdcardPaths(context);
        String path = "";
        if (paths.length < 2) {
            return "";
        } else {
            path = paths[1];
        }
        return path;
    }

    private static String[] getSdcardPaths(Context context) {
        WeakReference<Context> weakContextRef = new WeakReference<Context>(
                context);
        Context ctx = weakContextRef.get();
        String[] paths = null;
        if (ctx != null) {
            StorageManager stManager = (StorageManager) ctx
                    .getSystemService(Context.STORAGE_SERVICE);
            try {
                Method mtdGetVolumePath = stManager.getClass()
                        .getDeclaredMethod("getVolumePaths");
                paths = (String[]) mtdGetVolumePath.invoke(stManager);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return paths;
    }


    public static long fileLen = 0;
    public static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

    /**
     * 传入文件夹
     * @param filePath
     * @return
     */
    private static long getFileLenFromPath(File filePath)
    {
        File[] files = filePath.listFiles();
        if (files==null){
            return 0;
        }

        for (int i = 0; i < files.length; i++)
        {
            if (files[i].isFile())
            {
                fileLen += files[i].length();
            }
            else
            {
                getFileLenFromPath(files[i]);
            }
        }
        return fileLen;
    }

    /**
     * 4、将文件大小显示为GB,MB等形式
     *
     * @param size
     *            文件大小
     * @return
     */
    public static String size(long size)
    {
        if (size / (1024 * 1024 * 1024) > 0)
        {
            float tmpSize = (float) (size) / (float) (1024 * 1024 * 1024);
            DecimalFormat df = new DecimalFormat("#.##");
            return "" + df.format(tmpSize) + "GB";
        }
        else if (size / (1024 * 1024) > 0)
        {
            float tmpSize = (float) (size) / (float) (1024 * 1024);
            DecimalFormat df = new DecimalFormat("#.##");
            return "" + df.format(tmpSize) + "MB";
        }
        else if (size / 1024 > 0)
        {
            return "" + (size / (1024)) + "KB";
        }
        else
            return "" + size + "B";
    }

    public static String size2(long size)
    {
        if (size / (1000* 1000 * 1000) > 0)
        {
            float tmpSize = (float) (size) / (float) (1000 * 1000 * 1000);
            DecimalFormat df = new DecimalFormat("#.##");
            return "" + df.format(tmpSize) + "GB";
        }
        else if (size / (1000 * 1000) > 0)
        {
            float tmpSize = (float) (size) / (float) (1000 * 1000);
            DecimalFormat df = new DecimalFormat("#.##");
            return "" + df.format(tmpSize) + "MB";
        }
        else if (size / 1000 > 0)
        {
            return "" + (size / (1000)) + "KB";
        }
        else
            return "" + size + "B";
    }

}
