package com.wosplayer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by user on 2016/7/30.
 *  监听服务
 */
public class MonitorService extends Service {
    private Timer timer = null;
    private TimerTask timerTask = null;
    private boolean threadFlag = true;
    private long interval = 10*1000;
    private Thread canAppThread = null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Logs.e("--------------------------------------------------monitor server onCreate----------------------------------------");
        if (canAppThread!=null){
            threadFlag = false;
            canAppThread = null;
        }

        canAppThread =  new Thread(new Runnable() {
            @Override
            public void run() {
                while(threadFlag){
                    monitor();
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
        threadFlag = true;
        canAppThread.start();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logs.e(">>>监听服务 ---------------------------------------------------------------------------------------- 开启。");
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.i(" ################################################################监听服务 结束#######################################################################");
        if (canAppThread!=null){
            threadFlag=false;
            canAppThread = null;
        }
    }

    /**
     * 监听 -
     */
    private void monitor(){
        boolean f = serverUtils.isAppOnForeground(MonitorService.this.getApplicationContext());
      //  log.d("isAppOnForeground():"+f);
        f = serverUtils.isRunningForeground(this.getApplicationContext(),this.getApplicationContext().getPackageName());
      //  log.d("isRunningForeground():"+f);
        f = serverUtils.isRunningToTaskTop(this.getApplicationContext(),DisplayActivity.class.getName());
     //   log.d("isRunningToTaskTop():"+f);
        boolean flag =  serverUtils.isAppOnForeground(MonitorService.this.getApplicationContext());
        //log.i("监听服务 监听结果 :"+flag);
        if (!flag){
            Logs.e("----- app not foreground ，app不在前台 -----");
            Intent intent  = new Intent();
            intent.setAction(RestartApplicationBroad.action);
            intent.putExtra(RestartApplicationBroad.IS_START,false);
            intent.putExtra(RestartApplicationBroad.KEYS, "0");
            sendBroadcast(intent);
        }
    }
}
