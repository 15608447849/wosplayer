package com.wosplayer.service;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import java.util.List;

/**
 * Created by user on 2016/6/27.
 */
public class serverUtils {

    public static boolean isRunningToTaskTop(Context c,String appName){//判断  app 是不是 运行 的

        try {
            ActivityManager am = (ActivityManager) c.getSystemService(c.ACTIVITY_SERVICE);//得到包管理器
            /**
             * 运行中的栈信息你可以获取系统当前正在运行的任务的信息。
             * 注意：正在运行的任务不是一个任务的进程真正运行；
             * 它简单的说就是用户运行它并且没有关闭它，但是，系统可能会kill它的进程并且系统为了可以重启它而保留它最新的状态。
             * 之前，使用该接口需要 android.permission.GET_TASKS
             *  android L开始,应用要使用该接口必须声明权限android.permission.REAL_GET_TASKS
             */
            List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(1);//系统当前正在运行的Task列表，用maxNum限制要获取的数量（最近使用的最先取出）

            for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                ComponentName baseActivity = taskInfo.baseActivity;
                String className = baseActivity.getClassName();
                if (className.contains(appName)) { // 说明它已经启动了
                    return true;
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 判断APP是不是在在上面运行
     *
     * @param packageName
     * @return
     */
    public static boolean isRunningForeground(Context c,String packageName) {
        try {
            ActivityManager am = (ActivityManager) c
                    .getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            String currentPackageName = cn.getPackageName();
            if (currentPackageName.equals(packageName)) {
                return true;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return false;
    }


    //在进程中去寻找指定APP的信息，判断是否在前台运行
    public static boolean isAppOnForeground(Context c) {
        try {
            ActivityManager activityManager =(ActivityManager) c.getSystemService(  //c = > getApplicationContext()
                    Context.ACTIVITY_SERVICE);//activity 管理器
            String packageName =c.getPackageName();//包名
            List<ActivityManager.RunningAppProcessInfo>appProcesses = activityManager.getRunningAppProcesses();//当前运行中的app列表
            if (appProcesses == null)
                return false;
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                //当前app 进程名 和 指定的包名 是否一致 , importance [ɪm'pɔːt(ə)ns] 重要性 是不是 属于 IMPORTANCE_FOREGROUND 前台进程
                if (appProcess.processName.equals(packageName)
                        && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
