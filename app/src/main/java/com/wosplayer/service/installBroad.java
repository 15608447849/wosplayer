package com.wosplayer.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;

import java.util.Calendar;

/**
 * Created by user on 2016/8/2.
 */
public class installBroad extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        log.e(" installBroad ","["+intent.getAction()+"]");
        //接收广播：系统启动完成后运行程序
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) { //"android.intent.action.BOOT_COMPLETED"
            System.out.println("---------------开机启动---------");
         /*   Intent app_start = new Intent();
            app_start.setAction(RestartApplicationBroad.action);
            app_start.putExtra(RestartApplicationBroad.IS_START,true);
            context.sendBroadcast(app_start);*/
            Toast.makeText(context, "boot completed action has got", Toast.LENGTH_LONG).show();

            Intent intenta = new Intent(context, DisplayActivity.class);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.SECOND, 0);
            PendingIntent pi = PendingIntent.getActivity(context,0,intenta,0);
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                    .set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
            System.out.println("---------timer is complete --");

        }

        //接收广播：设备上新安装了一个应用程序包后自动启动新安装应用程序。
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {

            String packageName = intent.getDataString().substring(8);
            System.out.println("---------------" + packageName);

           /* PackageManager pageManage = context.getPackageManager();
            Intent intent1 = pageManage.getLaunchIntentForPackage(packageName);
            context.startActivity(intent1);*/
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

//            Intent app_restart = new Intent();
//            app_restart.setAction(RestartApplicationBroad.action);
//            app_restart.putExtra(RestartApplicationBroad.IS_START,true);
//            context.sendBroadcast(app_restart);

            Intent intenta = new Intent(context, DisplayActivity.class);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.SECOND, 30);
            PendingIntent pi = PendingIntent.getActivity(context,0,intenta,0);
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                    .set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
            System.out.println("---------timer is complete --");

        }
    }
}