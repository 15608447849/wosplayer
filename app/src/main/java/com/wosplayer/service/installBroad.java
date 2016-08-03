package com.wosplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * Created by user on 2016/8/2.
 */
public class installBroad extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //接收广播：系统启动完成后运行程序
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent restart = new Intent();
            restart.setAction(RestartApplicationBroad.action);
            context.sendBroadcast(restart);
            System.out.println("---------------重启");

        }
        //接收广播：设备上新安装了一个应用程序包后自动启动新安装应用程序。
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {

            String packageName = intent.getDataString().substring(8);
            System.out.println("---------------" + packageName);

            PackageManager pageManage = context.getPackageManager();
            Intent intent1 = pageManage.getLaunchIntentForPackage(packageName);
            context.startActivity(intent1);
        }
        //接收广播：设备上删除了一个应用程序包。
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageName = intent.getData().getSchemeSpecificPart();
            System.out.println("---------------删除了..."+packageName);
        }




        //替换
        if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
            String packageName = intent.getData().getSchemeSpecificPart();
            System.out.println("---------------替换了..."+packageName);
        }


        if (intent.getAction().equals("android.intent.action.PACKAGE_RESTARTED")) {
            System.out.println("---------------清除应用数据...PACKAGE_RESTARTED");
        }



        if (intent.getAction().equals("android.intent.action.PACKAGE_DATA_CLEARED")) {
            System.out.println("---------------清除应用数据...PACKAGE_DATA_CLEARED");
        }


        if (intent.getAction().equals("android.intent.action.PACKAGE_RESTARTED")) {
            System.out.println("---------------点击了 强行停止 ...PACKAGE_RESTARTED 意思为 应用进程重启");
        }
    }
}