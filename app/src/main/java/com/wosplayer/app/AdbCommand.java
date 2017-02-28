package com.wosplayer.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.util.Log;

import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.ShellUtils;

import com.wos.play.rootdir.model_monitor.soexcute.RunJniHelper;
import com.wosplayer.R;

/**
 * Created by user on 2016/10/9.
 */
public class AdbCommand implements Runnable {


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

    //卸载播放器
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


    public String packagepath = null;
    public Context context = null;
    public Handler handler = null;

    //启动应用adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.wosplayer/com.wosplayer.app.DisplayActivity
    public static final String commands_startApp = "am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.wosplayer/com.wosplayer.app.DisplayActivity";
    //打开一个activity
    public static String adbStartActivity(String packageName,String mainActivityClassPath){
        return "am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n "+packageName+"/"+mainActivityClassPath;
    }
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
    //运行时安装apk
    public  static void runingInstallApk(String TAG, String apkLocalPath, String fileNane){
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
     */
    public AdbCommand(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        this.packagepath = context.getApplicationInfo().sourceDir;
    }

    //发送信息到ui界面
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
                //放入system
                 excuteAppMoveSystem(context);
            } else {
                Logs.d("执行adb命令","没有root权限");
                handlerSendMsg("没有root权限.");
            }
        } catch (Exception e) {
            Logs.e(e.getMessage());
        }finally {
            context = null;
            handler=null;
        }
    }

    //放进system cmd
    public String getAppChangerPath(String _packagePath, String _packageName, String alias) {
        return "mount -o remount,rw /system" + "\n" +
                "cp /data/data/"+_packageName+"/lib/* /system/lib" + "\n" + //复制so文件到目录下
                "chmod 777 /system/lib/*" + "\n" + //设置so权限
                "cp " + _packagePath + " /system/app/" + alias + "\n" + //复制app到system
                "chmod 777 /system/app/" + alias + "\n" + //赋权
                "rm -rf " + _packagePath + "\n" + //删除data目录下面的app
                "rm -rf /data/dalvik-cache/data*" + "\n";//删除虚拟机缓存
    }

    //检测root权限 放入system
    private void excuteAppMoveSystem(Context context) {
            ApplicationInfo appinfo = context.getApplicationInfo();
            String packagepath = appinfo.sourceDir; //包路径
            String packageName = appinfo.packageName; // 包名
            //Logs.d("执行adb命令","初始化app信息,package 路径 - "+packagepath +"; 包名: "+packageName);

            String TAG = "执行adb命令";
            String alias = "WosOldTerminal.apk";
            //如果不在system目录下  (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0 //系统程序存在/system/app下
            if ( (appinfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0 || FileUtils.isFileExist("/system/app/"+alias) ) {
                handlerSendMsg("系统应用");
                return;
            }
                //第一次执行
                RunJniHelper.startWatch(context);//打开监听服务
                Log.e(TAG,"源apk路径:" + packagepath + " - 移动路径 :/sysytem/app/" + alias);
                String cmd = getAppChangerPath(packagepath,packageName, alias);
                Log.e(TAG,cmd);
                ShellUtils.CommandResult cr = ShellUtils.execCommand(cmd, true, true);
                Log.e(TAG,"提升权限结果:" + cr.result);
                handlerSendMsg("提升权限结果:" + (cr.result==0?" 成功":" 失败"));
                if (cr.result == 0) {
                    //发送重启通知
                    try {
                        handlerSendMsg("请注意,初始化系统完成,将在30秒后重启.");
                        Thread.sleep(30 * 1000);
                        handlerSendMsg(" [ 即将重新启动 ] ");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        ShellUtils.execCommand("reboot", true);
                    }
                }
    }

    /**
     * 终端命令集
     * tcmp -p 端口号
     * tcmp -r 卸载
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
