package com.wos.play.rootdir.model_monitor.soexcute;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;


import com.wosplayer.app.AdbCommand;
import com.wosplayer.app.SystemConfig;

import java.util.ArrayList;
import java.util.List;

import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by 79306 on 2017/2/24.
 */

public class WatchServer extends Service {
    private static final String TAG = "播放器守护监听";
    private static List<String> activityList = null;
    static {
        //需要监听的activity
        activityList = new ArrayList<>();
        activityList.add("com.wosplayer.app.DisplayActivity");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        android.util.Log.i(TAG, "创建服务 - pid: "+android.os.Process.myPid());

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        android.util.Log.i(TAG, "销毁服务 - pid: "+android.os.Process.myPid());
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        android.util.Log.i(TAG, "onStartCommand() - pid : "+android.os.Process.myPid()+"");
        int isWatch = SystemConfig.get().read().GetIntDefualt("watchValue",0);
        if (isWatch==0){
            if (!serverUtils.isRunningForeground(getApplicationContext(), activityList)) {
                //尝试打开
                android.util.Log.e(TAG, "APP不在栈顶端-尝试打开");
                    /*Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), DisplayActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);*/
                ShellUtils.execCommand(AdbCommand.commands_startApp,true);
            }
        }
        return START_NOT_STICKY;
    }

}
