package com.visa.getlocalappsize;

import android.os.Environment;
import android.os.StatFs;
import java.io.File;

/**
 * TODO SD卡相关的辅助类
 */
public class SDCardUtils
{
    private SDCardUtils()
    {
		/* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 1判断SDCard是否可用
     *
     * @return
     */
    public static boolean isSDCardEnable()
    {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);

    }

    /**
     * 9获取SD卡大小
     * @return
     * SD卡存在返回大小；SD卡不存在返回-1
     */
    public static long getSDCardSize(){
        if (isSDCardEnable()) {
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator);
            if (android.os.Build.VERSION.SDK_INT < 18) {
                int blockSize = statFs.getBlockSize();
                int blockCount = statFs.getBlockCount();
                return blockSize * blockCount;
            } else {
                long blockSize = statFs.getBlockSizeLong();
                long blockCount = statFs.getBlockCountLong();
                return blockSize * blockCount;
            }
        }
        return -1;
    }

    /**
     * 10获取SD卡可用大小
     * @return
     * SD卡存在返回大小；SD卡不存在返回-1
     */
    public static long getSDCardAvailableSize(){
        if (isSDCardEnable()) {
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator);
            if (android.os.Build.VERSION.SDK_INT < 18) {
                int blockSize = statFs.getBlockSize();
                int blockCount = statFs.getAvailableBlocks();
                return blockSize * blockCount;
            } else {
                long blockSize = statFs.getBlockSizeLong();
                long blockCount = statFs.getAvailableBlocksLong();
                return blockSize * blockCount;
            }
        }
        return -1;
    }
}
