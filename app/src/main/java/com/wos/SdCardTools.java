package com.wos;

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2016/10/31.
 * lzp
 * 获取 手机 内置卡 路径
 *          外置卡 路径
 *          反射调用
 StorageList list = new StorageList(this);
 TextView textView = new TextView(this);

 if (list.getVolumePaths()[0].equals(Environment.MEDIA_MOUNTED)) {
 textView.setText("有外部存储卡"+list.getVolumePaths()[0]);
 } else {
 textView.setText("有外部存储卡"+list.getVolumePaths()[1]);
 }

 //	textView.setText(list.getVolumePaths()[0].toString()+list.getVolumePaths()[1].toString());
 textView.setTextSize(50);
 setContentView(textView);








 类  Environment 是一个提供访问环境变量的类
 MEDIA_BAD_REMOVAL      解释：返回getExternalStorageState() ，表明SDCard 被卸载前己被移除
 MEDIA_CHECKING      解释：返回getExternalStorageState() ，表明对象正在磁盘检查。
 MEDIA_MOUNTED      解释：返回getExternalStorageState() ，表明对象是否存在并具有读/写权限
 MEDIA_MOUNTED_READ_ONLY      解释：返回getExternalStorageState() ，表明对象权限为只读
 MEDIA_NOFS      解释：返回getExternalStorageState() ，表明对象为空白或正在使用不受支持的文件系统
 MEDIA_REMOVED      解释：返回getExternalStorageState() ，如果不存在 SDCard 返回
 MEDIA_SHARED      解释：返回getExternalStorageState() ，如果 SDCard 未安装 ，并通过 USB 大容量存储共享 返回

 MEDIA_UNMOUNTABLE      解释：返回getExternalStorageState() ，返回 SDCard 不可被安装 如果 SDCard 是存在但不可以被安装
 MEDIA_UNMOUNTED      解释：返回getExternalStorageState() ，返回 SDCard 已卸掉如果 SDCard  是存在但是没有被安装

 getDataDirectory()      解释：返回 File ，获取 Android 数据目录
 getDownloadCacheDirectory()      解释：返回 File ，获取 Android 下载/缓存内容目录
 getExternalStorageDirectory()      解释：返回 File ，获取外部存储目录即 SDCard
 getExternalStoragePublicDirectory(String type)      解释：返回 File ，取一个高端的公用的外部存储器目录来摆放某些类型的文件
 getExternalStorageState()      解释：返回 File ，获取外部存储设备的当前状态
 getRootDirectory()      解释：返回 File ，获取 Android 的根目录

 StatFs 类  StatFs 一个模拟linux的df命令的一个类,获得SD卡和手机内存的使用情况  StatFs 常用方法
 getAvailableBlocks()      解释：返回 Int ，获取当前可用的存储空间
 getBlockCount()      解释：返回 Int ，获取该区域可用的文件系统数
 getBlockSize()      解释：返回 Int ，大小，以字节为单位，一个文件系统
 getFreeBlocks()      解释：返回 Int ，该块区域剩余的空间
 restat(String path)      解释：执行一个由该对象所引用的文件系统

 */
public class SdCardTools {

    public static final String app_dir ="/wosplayer";
    public static  final String Construction_Bank_dir_source ="/construction_bank/source/";
    public static  final String Construction_Bank_dir_xmlfile ="/construction_bank/xml/";
    private static String appSourcePath = null;
    public static void setAppSourceDir(String path){

        appSourcePath = path;
        Log.i("","设置app 根目录 - ["+ appSourcePath+"]");
    }
    public static String getAppSourceDir(Context context){
        return appSourcePath==null?getDataDataAppDir(context):appSourcePath;
    }
    private static String getDataDataAppDir(Context context){
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
        }  catch (NoSuchMethodException e) {
            e.printStackTrace();
        }catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return paths;
    }

    /**
     * 重新挂载sd card 指向的路径
     * 无效方法
     */
    public static void remountSdcardPath(Context c,String path){
        File dir = new File(path);
        if (!dir.exists()){
            new NullPointerException("path is not find file , "+path);
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
     *
     */
    public static String getSDPath(){
        File sdDir = null;
        if (existSDCard()){
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        if (sdDir.exists()){
            Log.i("",sdDir.toString());
            return sdDir.toString();
        }
        return "mnt/sdcard";
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

    /**查看sd剩余空间
     */
    public static long getSDFreeSize(){
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
        return (freeBlocks * blockSize)/1024 /1024; //单位MB
    }

    /**
     * 查看总空间
     */
    public static long getSDAllSize(){
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
        return (allBlocks * blockSize)/1024/1024; //单位MB
    }


    /**
     * 截取 uri 下面 的 文件名
     */
    public static String cutUrlTanslationFilename(String url){
        url = url.substring(url.lastIndexOf("/")+1);
        return url;
    }

    /**
     * scope 空闲容量百分比
     * 判断当前 路径 容量
     *  大于scope true
     *  小于或者等于 scope false
     *  目录不存在 false
     */
    public static boolean justFileBlockVolume(String dirpath,String scopetxt){

        boolean isClear = false;
        try {
            double scope = 0;
            scope = Double.valueOf(scopetxt);
            scope = scope * (0.01);

            long blockSize; //块大小
            long totalBlocks;// 总块数
            long availableBlocks;//有效块数

            File dir = new File(dirpath);
            if (!dir.exists()){
                Log.e(""," justFileBlockVolume() is err ,bacause dir is not exists");
                return false;
            }

            //得到总大小
            StatFs stat = new StatFs(dir.getPath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            {
                blockSize = stat.getBlockSizeLong();
                totalBlocks = stat.getBlockCountLong();
                availableBlocks = stat.getAvailableBlocksLong();
            }
            else
            {
                blockSize = stat.getBlockSize();
                totalBlocks = stat.getBlockCount();
                availableBlocks = stat.getAvailableBlocks();
            }

            double totalText = blockSize * totalBlocks;//总大小
            double availableText = blockSize * availableBlocks;//可用空间大小

            double scale = availableText / totalText;// 比值

            Log.d("","总大小 :"+totalText+"\n有效大小 :"+availableText+"\n文件容量 比值 :"+ scale +"\n目标阔值 :"+scope);

            if (scope>scale){
                Log.e("","准备清理");
                isClear =  true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            isClear =false;
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
    public static void clearTargetDir(String dir_path, List<String> compList){


        List<String> fileList = parseList(compList);

        File dir = new File(dir_path);
        if (!dir.exists()){
            Log.e("","file dir is not exists !!");
            return;
        }


        if (dir.isDirectory()) {
            String[] children = dir.list();
            File subFile = null;
            Log.d(""," 资源文件 总数量:"+children.length+"\n 保留文件列表数量:"+fileList.size());
            for (int i=0; i<children.length; i++) {

                if (fileList.contains(children[i])){
                    //log.d("","保留 - "+children[i]);
                        continue;
                }
                subFile = new File(dir_path+children[i]);
                if (subFile.exists()){
                  //  log.d("","准备删除 - "+children[i]);
                    if(justFileLastModified(subFile)){
                        subFile.delete();
                    };
                }

            }

        }
               }

    /**
     * 如果最后修改时间 在 三天内 不删除 返回 false
     * @param subFile
     * @return
     */
    private static boolean justFileLastModified(File subFile) {
        long lastmodifiedTime = subFile.lastModified();
        long currentTime = System.currentTimeMillis();

        if (lastmodifiedTime<(currentTime - (1000 * 60 * 60 * 24 * 3))){
            return true;
        }else{
            return false;
        }
    }


    /**
     * 获取所有准备下载的文件的文件名
     * @param compList
     * @return
     */
    private static List<String> parseList(List<String> compList) {
        List<String> list = new ArrayList<String>();

        for (String str : compList){
            list.add(cutUrlTanslationFilename(str));
        }

        Log.i("","准备下载的文件的文件名集合:\n" + list.toString());
        return list;
    }


    /**
     * 检测sd card
     *
     # /mnt/internal_sd
     # /mnt/external_sd
     # /mnt/usb_storage
     */
    public static void checkSdCard(Context context) {
        File dir = null;
        String tags = "#file_sdcard";
        if(!SdCardTools.existSDCard()){
            Log.e(tags," sdcard is no exist ! ");
            Log.e(tags," application store dir-> "+getAppSourceDir(context));
//            checkSdCard(context);
        }else{
            SdCardTools.setAppSourceDir(getSDPath());
            String [] paths = SdCardTools.getVolumePaths(context);
            if (paths!=null && paths.length>0){
                Log.i(tags,"---------------------------------- sd card path info ------------------------------------");
                Log.i(tags," 当前 sdcard path:"+ getSDPath());
                for (String path : paths){
                    Log.i(tags," # "+ path);
                    if (path.equals("/mnt/external_sd")){
                       dir= new File(path);
                        if (dir.exists()){
                            if(MkDir(dir.toString()+"/test")){
                                SdCardTools.setAppSourceDir(path);
                                break;
                            }
                        }
                    }
                    if (path.contains("usb")){
                        dir = new File(path);
                        if (dir.exists()){
                            if(MkDir(dir.toString()+"/test")){
                                SdCardTools.setAppSourceDir(path);
                                break;
                            }
                        }
                    }
                }
            }

        }


        SdCardTools.setAppSourceDir(getAppSourceDir(context)+SdCardTools.app_dir);
        MkDir(appSourcePath);
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
                file.mkdirs();
            }
            if (file.exists()){
                return true;
            }
        } catch (Exception e) {
            Log.i("MkDir", e.getMessage());
        }
        return false;
    }
}
