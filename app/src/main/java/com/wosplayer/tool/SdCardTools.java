package com.wosplayer.tool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2016/10/31.
 * lzp
 * 获取 手机 内置卡 路径
 * 外置卡 路径
 * 反射调用
 * StorageList list = new StorageList(this);
 * TextView textView = new TextView(this);
 * <p>
 * if (list.getVolumePaths()[0].equals(Environment.MEDIA_MOUNTED)) {
 * textView.setText("有外部存储卡"+list.getVolumePaths()[0]);
 * } else {
 * textView.setText("有外部存储卡"+list.getVolumePaths()[1]);
 * }
 * <p>
 * //	textView.setText(list.getVolumePaths()[0].toString()+list.getVolumePaths()[1].toString());
 * textView.setTextSize(50);
 * setContentView(textView);
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * 类  Environment 是一个提供访问环境变量的类
 * MEDIA_BAD_REMOVAL      解释：返回getExternalStorageState() ，表明SDCard 被卸载前己被移除
 * MEDIA_CHECKING      解释：返回getExternalStorageState() ，表明对象正在磁盘检查。
 * MEDIA_MOUNTED      解释：返回getExternalStorageState() ，表明对象是否存在并具有读/写权限
 * MEDIA_MOUNTED_READ_ONLY      解释：返回getExternalStorageState() ，表明对象权限为只读
 * MEDIA_NOFS      解释：返回getExternalStorageState() ，表明对象为空白或正在使用不受支持的文件系统
 * MEDIA_REMOVED      解释：返回getExternalStorageState() ，如果不存在 SDCard 返回
 * MEDIA_SHARED      解释：返回getExternalStorageState() ，如果 SDCard 未安装 ，并通过 USB 大容量存储共享 返回
 * <p>
 * MEDIA_UNMOUNTABLE      解释：返回getExternalStorageState() ，返回 SDCard 不可被安装 如果 SDCard 是存在但不可以被安装
 * MEDIA_UNMOUNTED      解释：返回getExternalStorageState() ，返回 SDCard 已卸掉如果 SDCard  是存在但是没有被安装
 * <p>
 * getDataDirectory()      解释：返回 File ，获取 Android 数据目录
 * getDownloadCacheDirectory()      解释：返回 File ，获取 Android 下载/缓存内容目录
 * getExternalStorageDirectory()      解释：返回 File ，获取外部存储目录即 SDCard
 * getExternalStoragePublicDirectory(String type)      解释：返回 File ，取一个高端的公用的外部存储器目录来摆放某些类型的文件
 * getExternalStorageState()      解释：返回 File ，获取外部存储设备的当前状态
 * getRootDirectory()      解释：返回 File ，获取 Android 的根目录
 * <p>
 * StatFs 类  StatFs 一个模拟linux的df命令的一个类,获得SD卡和手机内存的使用情况  StatFs 常用方法
 * getAvailableBlocks()      解释：返回 Int ，获取当前可用的存储空间
 * getBlockCount()      解释：返回 Int ，获取该区域可用的文件系统数
 * getBlockSize()      解释：返回 Int ，大小，以字节为单位，一个文件系统
 * getFreeBlocks()      解释：返回 Int ，该块区域剩余的空间
 * restat(String path)      解释：执行一个由该对象所引用的文件系统
 */
public class SdCardTools {
    private static final String TAG = "储存卡设置";
    public static final String app_dir = "/wosplayer";
    public static final String Construction_Bank_dir_source = "/consbank/source/";
    public static final String Construction_Bank_dir_xmlfile = "/consbank/xml/";
    private static String appSourcePath = null;

    public static void setAppSourceDir(String path) {
        if (path == null) return;
        appSourcePath = path + SdCardTools.app_dir;
        Log.i(TAG, "设置播放器存储根目录 - [" + appSourcePath + "]");
    }

    public static String
    getAppSourceDir(Context context) {
        return appSourcePath == null ? appSourcePath = getDataDataAppDir(context) : appSourcePath;
    }

    private static String getDataDataAppDir(Context context) {
        return context.getFilesDir().getAbsolutePath();
    }

    //Volume 体积 量
    public static String[] getVolumePaths(Context mContext) {
        String[] paths = null;
        StorageManager mStorageManager;
        // 被调用 方法名
        Method mMethodGetPaths;
        try {
            if (mContext != null) {
                mStorageManager = (StorageManager) mContext
                        .getSystemService(Activity.STORAGE_SERVICE);
                mMethodGetPaths = mStorageManager.getClass()
                        .getMethod("getVolumePaths");
                paths = (String[]) mMethodGetPaths.invoke(mStorageManager);

            }
        } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return paths;
    }

    /**
     * 重新挂载sd card 指向的路径
     * 无效方法
     */
    public static void remountSdcardPath(Context c, String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            throw new NullPointerException("path is not find file , " + path);
        }
        Intent intent = new Intent();
        // 重新挂载的动作
        intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
        // 要重新挂载的路径
        intent.setData(Uri.fromFile(dir));
        c.sendBroadcast(intent);

    }


    /**
     * 获取当前 sdcard 路径
     */
    public static String getSDPath() {
        File sdDir = null;
        if (existSDCard()) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        assert sdDir != null;
        if (sdDir.exists()) {
//            Log.i(TAG, "获取当前sd卡路径 : "+sdDir.toString());
            return sdDir.getPath();
        }
        return "/mnt/sdcard";
    }

    /**
     * 判断sdcard 是否存在
     */
    public static boolean existSDCard() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }

    /**
     * 查看sd剩余空间
     */
    public static long getSDFreeSizeBytes() {
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //空闲的数据块的数量
        long freeBlocks = sf.getAvailableBlocks();
        //返回SD卡空闲大小
        //return freeBlocks * blockSize;  //单位Byte
        //return (freeBlocks * blockSize)/1024;   //单位KB
        return (freeBlocks * blockSize);/// 1024 / 1024; //单位MB
    }

    /**
     * 查看sd剩余空间
     */
    public static long getSDFreeSize() {
        return getSDFreeSizeBytes() / 1024 / 1024; //单位MB
    }


    /**
     * 查看总空间
     */
    public static long getSDAllSize() {
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //获取所有数据块数
        long allBlocks = sf.getBlockCount();
        //返回SD卡大小
        //return allBlocks * blockSize; //单位Byte
        //return (allBlocks * blockSize)/1024; //单位KB
        return (allBlocks * blockSize) / 1024 / 1024; //单位MB
    }


    /**
     * 截取 uri 下面 的 文件名
     */
    public static String cutUrlTanslationFilename(String url) {
        url = url.substring(url.lastIndexOf("/") + 1);
        return url;
    }

    /**
     * scope 空闲容量百分比
     * 判断当前 路径 容量
     * 大于scope true
     * 小于或者等于 scope false
     * 目录不存在 false
     */
    public static boolean justFileBlockVolume(String dirpath, String scopetxt) {

        boolean isClear = false;
        try {
            double scope = 0;
            scope = Double.valueOf(scopetxt);
            scope = scope * (0.01);

            long blockSize; //块大小
            long totalBlocks;// 总块数
            long availableBlocks;//有效块数

            File dir = new File(dirpath);
            if (!dir.exists()) {
                Log.e(TAG, " justFileBlockVolume() is err ,bacause dir is not exists");
                return false;
            }

            //得到总大小
            StatFs stat = new StatFs(dir.getPath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSizeLong();
                totalBlocks = stat.getBlockCountLong();
                availableBlocks = stat.getAvailableBlocksLong();
            } else {
                blockSize = stat.getBlockSize();
                totalBlocks = stat.getBlockCount();
                availableBlocks = stat.getAvailableBlocks();
            }

            double totalText = blockSize * totalBlocks;//总大小
            double availableText = blockSize * availableBlocks;//可用空间大小

            double scale = availableText / totalText;// 比值

            Log.d(TAG, "总大小 :" + totalText + "\n有效大小 :" + availableText + "\n文件容量 比值 :" + scale + "\n目标阔值 :" + scope);

            if (scope > scale) {
                Log.e(TAG, "准备清理");
                isClear = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            isClear = false;
        }
        return isClear;
    }

    private static double formatSize(double size) {
        double result = size;
        if (result > 900) {
            result = result / 1024;
        }
        if (result > 900) {
            result = result / 1024;
        }
        if (result > 900) {
            result = result / 1024;
        }
        if (result > 900) {
            result = result / 1024;
        }
        if (result > 900) {
            result = result / 1024;
        }
        return result;
    }


    /**
     * 清理 资源
     */
    public static void clearTargetDir(String dir_path, List<String> compList) {
        File dir = new File(dir_path);
        if (!dir.exists()) {
            Log.e(TAG, "file dir is not exists !!");
            return;
        }
        List<String> fileList = null;
        if (compList != null) {
            fileList = parseList(compList);
        }

        if (dir.isDirectory()) {
            String[] children = dir.list();
            File subFile = null;
//            Log.d(TAG, " 资源文件 总数量:" + children.length + "\n 保留文件列表数量:" + fileList.size());
            for (int i = 0; i < children.length; i++) {

                if (fileList != null && fileList.contains(children[i])) {
                    //log.d("","保留 - "+children[i]);
                    continue;
                }
                subFile = new File(dir_path + children[i]);
                if (subFile.exists()) {
                    //  log.d("","准备删除 - "+children[i]);
                    if (justFileLastModified(subFile)) {
                        subFile.delete();
                    }
                }

            }

        }
    }

    /**
     * 如果最后修改时间 在 三天内 不删除 返回 false
     *
     * @param subFile
     * @return
     */
    private static boolean justFileLastModified(File subFile) {
        long lastmodifiedTime = subFile.lastModified();
        long currentTime = System.currentTimeMillis();

        if (lastmodifiedTime < (currentTime - (1000 * 60 * 60 * 24 * 3))) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 获取所有准备下载的文件的文件名
     *
     * @param compList
     * @return
     */
    private static List<String> parseList(List<String> compList) {
        List<String> list = new ArrayList<String>();

        for (String str : compList) {
            list.add(cutUrlTanslationFilename(str));
        }

        Log.i(TAG, "准备下载的文件的文件名集合:\n" + list.toString());
        return list;
    }


    /**
     * 检测sd card
     * <p>
     * # /mnt/internal_sd
     * # /mnt/external_sd
     * # /mnt/usb_storage
     */
    public static boolean checkSdCard(Context context) {
        if (!SdCardTools.existSDCard()) {
            Log.e(TAG, "sdcard不存在,应用存储目录: " + getAppSourceDir(context));
        } else {
            if (testDirc(getSDPath(), true)) {
                MkDir(appSourcePath);//创建目录
                return true;
            }
        }
        return false;
    }

    /**
     * 测试目录
     */
    public static boolean testDirc(String path, boolean isSetting) {
        try {
            String testpath = path + "/test";
            if (MkDir(testpath)) {
                org.apache.commons.io.FileUtils.deleteDirectory(new File(testpath));
                if (isSetting) SdCardTools.setAppSourceDir(path);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取 可用的存储路径
     */
    public static ArrayList<String> getAllStorePath(Context context) {
        String[] paths = SdCardTools.getVolumePaths(context);
        if (paths != null && paths.length > 0) {
            ArrayList<String> list = new ArrayList<>();
//            Log.i(TAG, "可用存储列表:");
            for (int i = 0; i < paths.length; i++) {

                if (testDirc(paths[i], false)) {
                    list.add(paths[i]);
//                    Log.i(TAG, i+ " - " + paths[i]);
                }
            }
            return list.size() > 0 ? list : null;
        }
        return null;
    }


    /**
     * 创建文件
     *
     * @param pathdir
     */
    public static boolean MkDir(String pathdir) {
        try {
            File file = new File(pathdir);
            if (!file.exists()) {
                boolean isSuccess = file.mkdirs();
                if (!isSuccess) {
                    throw new Exception("创建文件夹失败:" + pathdir);
                }
                //Log.i(TAG, "创建文件夹 - " + pathdir + (isSuccess ? " 成功" : " 失败"));
            }
            if (file.exists() && file.isDirectory()) {
                return true;
            }
        } catch (Exception e) {
            //Log.e(TAG, "MkDir() >> " + e.getMessage());
        }
        return false;
    }

    //文件夹下循环遍历指定后缀的文件
    public static void getTaggerPrefixOnFiles(String dirPath, ArrayList<String> list, String... prefix) {
        if (list == null) return;
        File file = new File(dirPath);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    //继续遍历
                    for (File file2 : files) {
                        getTaggerPrefixOnFiles(file2.getAbsolutePath(), list, prefix);
                    }
                }
            }
            if (file.isFile()) {
                if (justFileSuffix(file.getName(), prefix, 0, prefix.length)) {
                    list.add(file.getAbsolutePath());
                }
            }
        }
    }

    public static String justPath(ArrayList<String> list, String param) {
        for (String var : list) {
            if (var.contains(param)) {
                if (isEntityDirs(new File(var))) {
                    return var;
                }
            }
        }
        return null;
    }

    public static boolean justFileSuffix(String filePath, String[] suffixArray, int start, int length) {
        try {
            for (int i = start; i < length; i++) {
                if (filePath.endsWith(suffixArray[i])) return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //判断文件夹是否为空
    public static boolean isEntityDirs(File file) {
        return file.exists() && file.isDirectory() && file.list().length > 0;
    }


    public static void copyFile(String src, String dec) {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new RandomAccessFile(src, "rw").getChannel();
            outChannel = new RandomAccessFile(dec, "rw").getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    /**
     * 清空目录
     * @param dirs
     */
    public static void clearDir(String dirs) {
        File home = new File(dirs);
        if (home.exists() && home.exists()){
           for(File file : home.listFiles()) {
               file.delete();
           }
        }
    }
}
