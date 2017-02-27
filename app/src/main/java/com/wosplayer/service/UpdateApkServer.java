package com.wosplayer.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.wosplayer.app.AdbCommand;
import com.wosplayer.app.Logs;

import java.io.File;

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
        Bundle bundle = intent.getExtras();
        String path = bundle.getString(APK_PATH,"");//终端id
        if (path == null || path.equals("")) return;
        installApk(path);
    }



    /**
     * 安装APK文件
     */
    public synchronized void installApk(String apkLocalPath) {
        File apkfile = new File(apkLocalPath);
        if (!apkfile.exists()) {
            Logs.e(TAG,"安装apk失败, "+apkLocalPath +" 升级包不存在.");
            return;
        }
        apkfile.setExecutable(true);//设置可执行权限
        apkfile.setReadable(true);//设置可读权限
        apkfile.setWritable(true);//设置可写权限
        ShellUtils.CommandResult result = ShellUtils.execCommand("chmod 777 "+apkLocalPath,true);

        Logs.i(TAG," 安装包赋予权限结果执行结果:"+result.result+",路径 :"+apkLocalPath);
        int code =  PackageUtils.install(getApplicationContext(),apkLocalPath);
        Logs.e(TAG," PackageUtils.install 安装结果 返回值 : "+code);
        if (code == PackageUtils.INSTALL_SUCCEEDED){
            ShellUtils.execCommand(AdbCommand.commands_startApp,true);//尝试打开app
        }
        else{
            AdbCommand.RuningInstallApk(TAG,apkLocalPath,null);
        }
    }
















}
