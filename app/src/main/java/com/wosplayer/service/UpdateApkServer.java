package com.wosplayer.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.wosplayer.app.AdbCommand;
import com.wosplayer.app.AppUtils;
import com.wosplayer.app.Logs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.trinea.android.common.util.PackageUtils;
import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by lzp on 2017/2/27.
 */

public class UpdateApkServer extends IntentService {
    private static final String TAG = "_UpdateApkServer";

    public static final String APK_PATH ="apkpath";
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public UpdateApkServer() {
        super(TAG);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Logs.i("--------------------------------------------执行升级apk服务 onCreate--------------------------------------------------------");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.i("-------------------------------------------执行升级apk服务 onDestroy----------------------------------------------------------");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;
        String path = bundle.getString(APK_PATH,"");//终端id
        if (path == null || path.equals("")) return;
        judgeApk(path);
    }

    private synchronized void judgeApk(String apkLocalPath) {

        String time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        long timestamp = System.currentTimeMillis();
        String logFile = "update-" + time + "-" + timestamp + ".log";

        StringBuffer sb = new StringBuffer();
        File apkfile = new File(apkLocalPath);
        int flag = -1;
        if (!apkfile.exists()) {
            sb.append("install apk failt,path :"+apkLocalPath +", update package file not fount.\n");
        }else{
            //判断apk 版本号
            // 1 获取软件版本号 apk版本号
            int localVersion = AppUtils.getAppVersionCode(getApplicationContext());
            int apkVersion = AppUtils.getApkVersionCode(getApplication(),apkLocalPath);
            //2 获取软件包名 apk包名
            String localPackageName = getApplication().getApplicationInfo().packageName;
            String apkPackageName = AppUtils.getApkPackageName(getApplication(),apkLocalPath);
            sb.append("local package:"+localPackageName+";version:"+localVersion+".\n");
            sb.append("apk package:"+apkPackageName+";version:"+apkVersion+".\n");
            if (localPackageName.equals(apkPackageName)){
                if (localVersion>=apkVersion){
                    sb.append("update failt,apk version is invalid.\n");
                }
                else{
                    flag = 1;
                }
            }else{
                sb.append("the apk file is not update.apk,the apk is other App.\n");
                flag = 2;
            }
        }
        AppUtils.saveLogs(logFile, sb); //升级信息记录
        if (flag>0){
            installApk(apkLocalPath,flag);
        }
    }
    /**
     * 安装APK文件
     */
    public  void installApk(String apkLocalPath,int flag) {
        String time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        long timestamp = System.currentTimeMillis();
        String logFile = "install-" + time + "-" + timestamp + ".log";
        StringBuffer sb = new StringBuffer();
        ShellUtils.CommandResult result = ShellUtils.execCommand("chmod 777 "+apkLocalPath,true); // 赋予权限
        sb.append("apk path : "+apkLocalPath+" excute 'chmod 777' result:"+result.result+"\n");
        if (result.result == 0){
            int code =  PackageUtils.install(getApplicationContext(),apkLocalPath); //包管理器安装
            sb.append(" PackageUtils.install result code : "+code+"\n");
            if (code != PackageUtils.INSTALL_SUCCEEDED){
                sb.append("usr shell command installing.\n");
                int res = AdbCommand.runingInstallApk(apkLocalPath);
                if (res == 0){
                    flag = 3;
                    sb.append("shell command install apk success.\n");
                }else{
                    sb.append("shell command install apk failt.\n");
                }
            }else{
                flag = 3;
                sb.append("package util install apk success.\n");
            }
           AppUtils.saveLogs(logFile, sb);
        }
        if (flag == 3){
            String packageName = AppUtils.getApkPackageName(getApplicationContext(),apkLocalPath);
            String activityName = AppUtils.getTaragePackageLunchActivityName(getApplicationContext(),packageName);
            String command = "rm -rf "+apkLocalPath+"\n"+AdbCommand.adbStartActivity(packageName,activityName);
            Log.e(TAG,command);
            //成功 - 打开 app
            ShellUtils.execCommand(command,true);
        }
    }
}
