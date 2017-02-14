package com.wosplayer.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Message;

import java.util.concurrent.locks.ReentrantLock;

import cn.trinea.android.common.util.ShellUtils;
import installUtils.ApkController;
import installUtils.AppToSystem;

/**
 * Created by user on 2016/10/9.
 */
public class AdbShellCommd extends Thread {


    private static ReentrantLock mLock = new ReentrantLock();

    public Context context = null;
    public Handler handler = null;

    public boolean isrun_1 = false;
    //预留远程端口号
    public static String[] commands = new String[]{
            "setprop service.adb.tcp.port 9999\n",
            "stop adbd\n",
            "start adbd\n",
    };


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

    //启动应用
    public static final String commands_startApp = "adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.wosplayer/com.wosplayer.activity.DisplayActivity";

    //安装app
    public static String[] getInstallAdb(String apkLocalPath, String filane) {
        String[] param = {
                "shell remount\n",
                "adb shell\n",
                "cp " + apkLocalPath + " /data/local/tmp/" + filane + "\n",
                "chmod 777 /data/local/tmp/" + filane + "\n",
                "pm install -r /data/local/tmp/" + filane + "\n",
                // "rm /data/local/tmp.apk\n"
        };
        return param;
    }

    /**
     * @param context 环境
     * @param isrun_1 控制后台端口打开true
     * @param isrun_2 放入系统目录true
     */
    public AdbShellCommd(Context context, Handler handler,boolean isrun_1, boolean isrun_2) {
        this.context = context;
        this.handler = handler;
        this.packagepath = context.getApplicationInfo().sourceDir;
        this.isrun_1 = isrun_1;
        this.isrun_2 = isrun_2;
        log.i("adb shell thread is create ");
    }

    public void handlerSendMsg(String msgStr){
        if (handler!=null && context!=null){
            Message msg = handler.obtainMessage();
            msg.obj = msgStr;
            msg.arg1 = 0x11;
            handler.sendMessage(msg);
        }
    }

    @Override
    public void run() {
        try {
//            mLock.lock();

            if (AppToSystem.haveRoot()) {
                handlerSendMsg("拥有root权限,执行初始化.");
                //卸载 旧app
                if (ApkController.uninstall("com.wos", context)) {
                    log.d("检测 卸载 老版本 APP - com.wos - 完成");
                }
                if (ApkController.uninstall("com.wos.tools", context)) {
                    log.d("检测 卸载 老版本 APP - com.wos.tools - 完成");
                }

                //放入system
                if (isrun_2) {
            /*log.d("_APP_Path",packagepath);
                    if (packagepath.contains("/data/app")){
                        String paramString = getParam(packagepath);
                        log.e("执行 copy App ,/data/app -> /system/app 请稍后...\n" + paramString);
                        //ShellUtils.CommandResult cmdResult = ShellUtils.execCommand(paramString,true,true);
                        int cmdResult = AppToSystem.execRootCmdSilent(paramString);
                        log.e("执行回执 : "+ cmdResult);
                        if(cmdResult == -1){
                            log.e("执行不成功");
                        }else{
                            log.e("执行成功 即将重启 ... ");
                            AppToSystem.execRootCmdSilent("reboot");
                        }
                    }else{
                        log.e(packagepath + " 已经存在");
                    }*/
                    excuteAppMoveSystem(context);
                }

                //尝试开启远程端口
                if (isrun_1) {
                    startRemoteport();
                }

            } else {
                handlerSendMsg("没有root权限.");
            }

            log.d("--------------------------------------------------------------------------------------------------------------\n");

        } catch (Exception e) {
//            mLock.unlock();
            log.e(e.getMessage());
        }finally {
            context = null;
            handler=null;
        }
    }

    public static void startRemoteport() {
        log.e("commands[] :\n" + commands[0] + commands[1] + commands[2]);

        ShellUtils.CommandResult cr = ShellUtils.execCommand(commands, true, true);

        String strs = "远程端口开启结果: " + (cr.result==0?WosApplication.getLocalIpAddress()+":9999 成功打开.":"未启动远程端口.");
        log.e("Remote", strs);

    }

    //放进system cmd
    public String genereteCommand(String _packagepath, String alias) {
        return "mount -o remount,rw /system" + "\n" +
                "mkdir /system/libtem" + "\n" +
                "cp /data/data/com.wos.play.rootdir/lib/* /system/lib" + "\n" +
                "chmod 777 /system/lib/*" + "\n" +
                "cp " + _packagepath + " /system/app/" + alias + "\n" +
                "chmod 777 /system/app/" + alias + "\n" +
                "rm -rf " + _packagepath + "\n" +
                "rm -rf /data/dalvik-cache/data*" + "\n";
    }

    //检测root权限 放入system
    private void excuteAppMoveSystem(Context context) {
        System.out.println("excuteAppMoveSystem >>> ");
        if (ShellUtils.checkRootPermission()) {
            ApplicationInfo appinfo = context.getApplicationInfo();
            String packagepath = appinfo.sourceDir;
            //如果不在system目录下  (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0 //系统程序存在/system/app下
            boolean flag = ((appinfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
            if (!flag) {
                //packagepath.contains("/data/app/")
//                String alias = packagepath.substring(packagepath.lastIndexOf("/") + 1);

                String alias = "WosOldTerminal.apk";
                System.out.println("源apk路径:" + packagepath + " - 移动路径 :/sysytem/app/" + alias);
                handlerSendMsg("源apk路径:" + packagepath + " - 移动路径 :/sysytem/app/" + alias);
                String cmd = genereteCommand(packagepath, alias);
                System.out.println("adb shell >>>[\n" + cmd + "]");
                ShellUtils.CommandResult cr = ShellUtils.execCommand(cmd, true, true);
                System.out.println("提升权限结果:" + cr.result);
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
    }
}
