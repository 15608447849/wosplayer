package com.wosplayer.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by user on 2016/10/9.
 */
public class AdbCommand{

   private static final String TAG = "执行adb命令";

    //预留远程端口号
    public static String[] commands = new String[]{
            "setprop service.adb.tcp.port 9999\n",
            "stop adbd\n",
            "start adbd\n",
    };

    //打开执行端口 默认9999
    public static String openPoint(String point){
        if (point==null || point.equals("")){
            point = "9999";
        }
        String cmd = "setprop service.adb.tcp.port "+point+"\n"
                +"stop adbd\n"
                +"start adbd";
        return cmd;
    }




    //多少秒后关机
    public static String closeTelOnTime(int time){
        //关机
       return "sleep "+time+" && reboot -p";
    }

    //多少秒后重启
    public static String rebootTelOnTime(int time){
        return "sleep "+time+" && reboot";
    }

    //启动应用adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.wosplayer/com.wosplayer.app.DisplayActivity
    public static final String commands_startApp = "am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.wosplayer/com.wosplayer.app.DisplayActivity";
    //打开一个activity
    public static String adbStartActivity(String packageName,String mainActivityClassPath){
        return "am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n "+packageName+"/"+mainActivityClassPath;
    }
    //安装app
    public static String getInstallAdb(String apkLocalPath) {

        return "chmod 777 "+apkLocalPath + "\n"+
                "pm install -r "+apkLocalPath ;
    }
    //运行时安装apk
    public static int runingInstallApk(String apkLocalPath){
        String command = getInstallAdb(apkLocalPath);
        Log.e(TAG,command);
        ShellUtils.CommandResult result = ShellUtils.execCommand(command,true,true);
        return result.result;
    }

    //系统初始化提权
    public static void initSystem(Context context) {
        try {
            //捕获异常
            CrashHandler.getInstance().init(context);
            //系统配置监听值
            SystemConfig.get().putOr("watchValue","0").save();

            //打开监听程序(不要开启)
            //WatchServerHelp.openDeams(context);

            if (ShellUtils.checkRootPermission()) {
                //放入system
                excuteAppMoveSystem(context);
            } else {
                Logs.e(TAG,"-----------------------------------没有root权限------------------------------------------");
            }
        } catch (Exception e) {
            Logs.e(TAG,e.getMessage());
        }
    }
    public static String [] localSoFileList = {
            "libserverHelper.so",
            "libanw.10.so",
            "libanw.13.so",
            "libanw.14.so",
            "libanw.18.so",
            "libanw.21.so",
            "libcompat.7.so",
            "libiomx.10.so",
            "libiomx.13.so",
            "libiomx.14.so",
            "libvlc.so",
            "libvlcjni.so"
    };
    //卸载播放器
    public static String uninstallTelminal(){
        String alias = "WosOldTerminal.apk";

        StringBuffer sb = new StringBuffer();
        sb.append("mount -o remount,rw /system\n");//挂载
        //删除so文件
        for (String so : localSoFileList){
            sb.append("rm -rf /system/lib/"+so+"\n");
        }
        //删除app
        sb.append("rm -rf /data/local/tem/*\n"//tem下面的临时文件或者apk
                +"rm -rf /data/data/com.wosplayer*\n"
                +"rm -rf /data/dalvik-cache/system@app@WosOldTerminal*\n"
                +"rm -rf /data/dalvik-cache/data@app@com.wosplayer*\n"
                +"rm -rf /system/app/"+alias+"\n"
                +"reboot\n");
        return sb.toString();
    }

    //放进system cmd
    public static String  getAppChangerPath(String _packagePath, String _packageName, String alias) {
        return "mount -o remount,rw /system" + "\n" + //挂载
                "cp /data/data/"+_packageName+"/lib/* /system/lib" + "\n" + //复制so文件到目录下
                "chmod 777 /system/lib/*" + "\n" + //设置so权限
                "cp " + _packagePath + " /system/app/" + alias + "\n" + //复制app到system
                "chmod 777 /system/app/" + alias + "\n" + //赋权
                "rm -rf " + _packagePath + "\n" + //删除data目录下面的app
                "rm -rf /data/app/com.wosplayer* \n"+
                "rm -rf /data/dalvik-cache/data*" + "\n";//删除虚拟机缓存
    }

    //检测root权限 放入system
    private static void excuteAppMoveSystem(Context context) {
            ApplicationInfo appinfo = context.getApplicationInfo();
            String packagepath = appinfo.sourceDir; //包路径
            String packageName = appinfo.packageName; // 包名
            //Logs.d(TAG,"初始化app信息,package 路径 - "+packagepath +"; 包名: "+packageName);
            String alias = "WosOldTerminal.apk";
            //如果不在system目录下  (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0 //系统程序存在/system/app下
            if ( (appinfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0 || FileUtils.isFileExist("/system/app/"+alias) ) {
                return;
            }
                Log.e(TAG,"源apk路径: " + packagepath + " - 移动路径 :/sysytem/app/" + alias);
                String cmd = getAppChangerPath(packagepath,packageName, alias);
                Log.e(TAG,cmd);
                ShellUtils.CommandResult cr = ShellUtils.execCommand(cmd, true, true);
                Log.e(TAG,"提升权限结果:" + cr.result);
                if (cr.result == 0) {
                    //通知初始化完成,将重启应用程序

                    ShellUtils.execCommand("reboot",true);
                }
    }

    /**
     * 终端命令集
     * wos p 端口号
     * wos r 卸载
     */
    public static String inputCommand(String option,String param){
        if (option!=null && !option.equals("")){
            if (option.equals("p")){
                //打开远程连接端口号,默认9999
                return openPoint(param);
            }
            if (option.equals("r")){
                //卸载
                return uninstallTelminal();
            }
        }
        return "";
    }

}
