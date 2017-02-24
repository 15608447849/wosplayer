package com.wos.play.rootdir.model_monitor.soexcute;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.app.Logs;

/**
 * Created by 79306 on 2017/2/24.
 */

public class SystemBroads extends BroadcastReceiver {
    private static final String TAG = "系统广播";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logs.d(TAG,"接受到系统广播action : "+action);
        if (action.equals("android.intent.action.BOOT_COMPLETED")){
            //开机广播
            Logs.d(TAG,"开机广播 - 即将打开 -监听服务");
            RunJniHelper.startWatch(context);
        }
        if(action.equals("android.intent.action.MEDIA_EJECT")){
         //sdcard等媒体介质弹出

        }
        if(action.equals("android.intent.action.MEDIA_MOUNTED")){
           //sdcard等媒体介质装载
            RunJniHelper.startWatch(context);
        }
        if (action.equals("android.intent.action.PACKAGE_ADDED")){
            //一个app被安装

        }
        if (action.equals("android.intent.action.PACKAGE_REMOVED")){
            //一个app被卸载

        }
        if (action.equals("android.intent.action.PACKAGE_REPLACED")){
            //一个app被替换
        }

        if (action.equals("android.intent.action.PACKAGE_RESTARTED")) {
            //清除应用数据...PACKAGE_RESTARTED

        }



        if (action.equals("android.intent.action.PACKAGE_DATA_CLEARED")) {
            //清除应用数据...PACKAGE_DATA_CLEARED

        }


        if (action.equals("android.intent.action.PACKAGE_RESTARTED")) {
            //点击了 强行停止 ...PACKAGE_RESTARTED 意思为 应用进程重启");

        }
    }

}
