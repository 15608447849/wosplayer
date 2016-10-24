package com.wosplayer.app;

import android.content.Context;

import com.wos.Toals;

import java.util.concurrent.locks.ReentrantLock;

import cn.trinea.android.common.util.ShellUtils;
import installUtils.ApkController;
import installUtils.AppToSystem;

/**
 * Created by user on 2016/10/9.
 */
public class AdbShellCommd extends Thread{

    private static ReentrantLock mLock = new ReentrantLock();

   public Context context = null;

   public boolean isrun_1 = false;
    //预留远程端口号
   public static String [] commands = new String[]{
            "su\n",
            "setprop service.adb.tcp.port 9999\n",
            "stop adbd\n",
            "start adbd\n",
            "exit\n"
    };


    public boolean isrun_2 = false;
    public String packagepath = null;
    //放进system cmd
    public String getParam(String _packagepath){
        String paramString=// "adb push MySMS.apk /system/app" +"\n"+
                "adb shell" +"\n"+
                        "su" +"\n"+
                        // "mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system" +"\n"+
                        "mount -o remount,rw /system" +"\n"+
                        "cp "+ _packagepath +" /system/app/wosplayer.apk" +"\n"+
                        //"mount -o remount,ro -t yaffs2 /dev/block/mtdblock3 /system" +"\n"+
                        "mount -o remount,ro /system" +"\n"+
                        "reboot"+"\n"+
                        "exit" +"\n"+
                        "exit";
        return paramString;
    }

    //启动应用
    public static final String commands_startApp = "adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.wosplayer/com.wosplayer.activity.DisplayActivity";

    //安装app
    public static String[] getInstallAdb(String apkLocalPath,String filane){
        String [] param = {
                "shell remount\n",
                "adb shell\n",
                "cp "+apkLocalPath+" /data/local/tmp/"+filane+"\n",
                "chmod 777 /data/local/tmp/"+filane+"\n",
                "pm install -r /data/local/tmp/"+filane+"\n",
                // "rm /data/local/tmp.apk\n"
        };
        return param;
    }

    /**
     *
     * @param context 环境
     * @param isrun_1 控制后台端口打开true
     * @param isrun_2 放入系统目录true
     */
    public AdbShellCommd(Context context, boolean isrun_1, boolean isrun_2){
        this.context = context;
        this.packagepath = context.getApplicationInfo().sourceDir;
        this.isrun_1 = isrun_1;
        this.isrun_2 = isrun_2;
        log.d("adb shell thread is ok");
    }

    @Override
    public void run() {
        try{
//            mLock.lock();

    if(AppToSystem.haveRoot()){
        log.d("------------------------------------------拥有root权限--------------------------------------------------------------------\n");


        //卸载 旧app
        if (ApkController.uninstall("com.wos",context)){
            log.d("检测 卸载 老版本 APP - com.wos - 完成");
        }
        if (ApkController.uninstall("com.wos.tools",context)){
            log.d("检测 卸载 老版本 APP - com.wos.tools - 完成");
        }

        //放入system
        if (isrun_2){
            log.d("_APP_Path",packagepath);
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
//                          AppToSystem.execRootCmd(commands_startApp);
                           // ShellUtils.CommandResult  r = ShellUtils.execCommand(commands_startApp,true,true);
                          //  log.e("---------------- " +r.result);
                        }

                    }else{
                        log.e(packagepath + " 已经存在");
                    }
            }

        //尝试开启远程端口
        if (isrun_1){
            startRemoteport();
        }






    }else{
        log.e("没有root权限");
    }

         log.d("--------------------------------------------------------------------------------------------------------------\n");

        }catch (Exception e){
//            mLock.unlock();
           log.e(e.getMessage());
        }

    }

    public static void startRemoteport() {
        log.e("commands[] :\n"+ commands[0]+commands[1]+commands[2]+commands[3]);

        ShellUtils.CommandResult cr = ShellUtils.execCommand(commands,true,true);

        String strs = "远程端口开启结果: "+cr.result;
        log.d("Remote",strs);
        Toals.Say(strs);
        strs = "本地ip: "+ wosPlayerApp.getLocalIpAddress();
        log.d("Remote",strs);
    }


}
