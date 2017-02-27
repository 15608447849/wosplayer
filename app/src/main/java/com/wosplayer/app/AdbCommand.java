package com.wosplayer.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.ShellUtils;
import com.installUtils.ApkController;
import com.installUtils.AppToSystem;
import com.wos.play.rootdir.model_monitor.soexcute.RunJniHelper;
import com.wosplayer.tool.SdCardTools;

/**
 * Created by user on 2016/10/9.
 */
public class AdbCommand extends Thread {
    public Context context = null;
    public Handler handler = null;
    public boolean isrun_1 = false;
    //预留远程端口号
    public static String[] commands = new String[]{
            "setprop service.adb.tcp.port 9999\n",
            "stop adbd\n",
            "start adbd\n",
    };

    public static String openPoint(String point){
        if (point==null || point.equals("")){
            point = "9999";
        }
        String cmd = "setprop service.adb.tcp.port "+point+"\n"
                +"stop adbd\n"
                +"start adbd";
        return cmd;
    }
    //卸载
    public static String uninstallTelminal(){
        String alias = "WosOldTerminal.apk";
        String cmd = "mount -o remount,rw /system\n"
        +"rm -rf /system/app/"+alias+"\n"
                +"rm -rf /data/data/com.wosplayer*\n"
                +"rm -rf /data/dalvik-cache/system@app@WosOldTerminal*\n"
                +"rm -rf /data/dalvik-cache/data@app@com.wosplayer*\n"
                +"reboot";
        return cmd;
    }
    public boolean isrun_2 = false;
    public String packagepath = null;
    //放进system cmd
    public String getParam(String _packagepath) {
        String paramString =// "adb push MySMS.apk /system/app" +"\n"+
                "adb shell" + "\n" +
                        "su" + "\n" +
                        // "mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system" +"\n"+
                        "mount -o remount,rw /system" + "\n" +
                        "cp " + _packagepath + " /system/app/wosplayer.apk" + "\n" +
                        //"mount -o remount,ro -t yaffs2 /dev/block/mtdblock3 /system" +"\n"+
                        "mount -o remount,ro /system" + "\n" +
                        "reboot" + "\n" +
                        "exit" + "\n" +
                        "exit";
        return paramString;
    }
    //启动应用adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.wosplayer/com.wosplayer.app.DisplayActivity
    public static final String commands_startApp = "am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.wosplayer/com.wosplayer.app.DisplayActivity";

    //安装app
    public static String getInstallAdb(String apkLocalPath, String fileNane) {
        String param = "";
        if (fileNane!=null && !fileNane.equals("")){
            param =         "mount -o remount,rw /system\n"+
                            "cp " + apkLocalPath + " /data/local/tmp/" + fileNane + "\n"+
                            "chmod 777 /data/local/tmp/" + fileNane + "\n"+
                            "pm install -r /data/local/tmp/" + fileNane ;
        }else{
            param = " chmod 777 "+apkLocalPath + "\n"+
                    "pm install -r "+apkLocalPath ;
        }

        return param;
    }

    public  static void RuningInstallApk(String TAG,String apkLocalPath, String fileNane){
        String command = getInstallAdb(apkLocalPath,fileNane);
        Log.e(TAG,command);
       ShellUtils.CommandResult result = ShellUtils.execCommand(command,true,true);
        if (result.result == 0){
            //成功
            Log.e(TAG,"执行成功");
            command = "rm /data/local/tem/"+fileNane+"\n"+commands_startApp;
            Log.e(TAG,command);
            ShellUtils.execCommand(command,true);
        }
        else{
            Log.e(TAG,"执行失败");
        }
    }



    /**
     * @param context 环境
     * @param isrun_1 控制后台端口打开true
     * @param isrun_2 放入系统目录true
     */
    public AdbCommand(Context context, Handler handler, boolean isrun_1, boolean isrun_2) {
        this.context = context;
        this.handler = handler;
        this.packagepath = context.getApplicationInfo().sourceDir;
        this.isrun_1 = isrun_1;
        this.isrun_2 = isrun_2;
        Log.e("执行adb命令","后台执行 - 创建成功");
    }
    public void handlerSendMsg(String msgStr){
        if (handler!=null && context!=null){
           AppTools.NotifyHandle(handler,DisplayActivity.HandleEvent.outtext.ordinal(),msgStr);
        }
    }
    @Override
    public void run() {
        try {
            if (ShellUtils.checkRootPermission()) {
                handlerSendMsg("拥有root权限,执行初始化.");
//                //卸载 旧app
//                if (ApkController.uninstall("com.wos", context)) {
//                    Logs.d("检测 卸载 老版本 APP - com.wos - 完成");
//                }
//                if (ApkController.uninstall("com.wos.tools", context)) {
//                    Logs.d("检测 卸载 老版本 APP - com.wos.tools - 完成");
//                }
                //放入system
                if (isrun_2) {
                    excuteAppMoveSystem(context);
                }
                //尝试开启远程端口
                if (isrun_1) {
                   // startRemoteport();
                }
            } else {
                Logs.d("执行adb命令","没有root权限");
                handlerSendMsg("没有root权限.");
            }
            ;
        } catch (Exception e) {
            Logs.e(e.getMessage());
        }finally {
            context = null;
            handler=null;
        }
    }
    public static void startRemoteport() {
        Logs.e("commands[] :\n" + commands[0] + commands[1] + commands[2]);

        ShellUtils.CommandResult cr = ShellUtils.execCommand(commands, true, true);

        String strs = "远程端口开启结果: " + (cr.result==0?AppTools.getLocalIpAddress()+":9999 成功打开.":"未启动远程端口.");
        Logs.e("Remote", strs);

    }
    //放进system cmd
    public String genereteCommand(String _packagePath,String _packageName, String alias) {
        return "mount -o remount,rw /system" + "\n" +
                "mkdir /system/libtem" + "\n" +
                "cp /data/data/"+_packageName+"/lib/* /system/lib" + "\n" +
                "chmod 777 /system/lib/*" + "\n" +
                "cp " + _packagePath + " /system/app/" + alias + "\n" +
                "chmod 777 /system/app/" + alias + "\n" +
                "rm -rf " + _packagePath + "\n" +
                "rm -rf /data/dalvik-cache/data*" + "\n";
    }

    //检测root权限 放入system
    private void excuteAppMoveSystem(Context context) {

            ApplicationInfo appinfo = context.getApplicationInfo();
            String packagepath = appinfo.sourceDir;
            String packageName = appinfo.packageName;
            Logs.d("执行adb命令","初始化app信息,package 路径 - "+packagepath +"; 包名: "+packageName);
            //如果不在system目录下  (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0 //系统程序存在/system/app下
            boolean flag = ((appinfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
            if (!flag) {
                //packagepath.contains("/data/app/")
//                String alias = packagepath.substring(packagepath.lastIndexOf("/") + 1);

                String alias = "WosOldTerminal.apk";
                if (FileUtils.isFileExist("/system/app/"+alias)){
                    handlerSendMsg("系统应用");
                    return;
                }



                //第一次执行
                RunJniHelper.startWatch(context);//打开监听服务

                Log.e("执行adb命令","源apk路径:" + packagepath + " - 移动路径 :/sysytem/app/" + alias);
                handlerSendMsg("源apk路径:" + packagepath + " - 移动路径 :/sysytem/app/" + alias);

                String cmd = genereteCommand(packagepath,packageName, alias);
                Log.e("执行adb命令",cmd);
                ShellUtils.CommandResult cr = ShellUtils.execCommand(cmd, true, true);
                Log.e("执行adb命令","提升权限结果:" + cr.result);
                handlerSendMsg("提升权限结果:" + cr.result);
                if (cr.result == 0) {
                    //发送重启通知
                    try {
                        handlerSendMsg("初始化系统完成,在30秒后重启.");
                        sleep(30 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        ShellUtils.execCommand("reboot", true);
                    }
                }
            }
    }


//    private static HashMap<String,String> commandMap = new HashMap<>();
//
//    static{
//        commandMap.put("-p","connect");//打开远程连接端口号,默认9999
//        commandMap.put("-r","uninstall");//卸载
//    }
    /**
     * 终端命令集
     */
    public static String inputCommand(String option,String param){
        if (option!=null && !option.equals("")){
            if (option.equals("-p")){
                //打开远程连接端口号,默认9999
                return openPoint(param);
            }
            if (option.equals("-r")){
                //卸载
                return uninstallTelminal();
            }
        }
        return "";
    }

}
