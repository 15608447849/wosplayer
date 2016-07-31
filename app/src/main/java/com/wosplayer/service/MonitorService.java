package com.wosplayer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by user on 2016/7/30.
 *
 */
public class MonitorService extends Service {


    private Timer timer = null;
    private TimerTask timerTask = null;


    private boolean threadFlag = true;
    private long interval = 30*1000;
    private Thread canAppThread = null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        log.e("监听服务 开启");
        log.e(" DisplayActivity.class.getName()=>"+ DisplayActivity.class.getName());
        log.e(" DisplayActivity.activityContext.getComponentName().getClassName():"+DisplayActivity.activityContext.getComponentName().getClassName());
//        startTimer();
        canAppThread =  new Thread(new Runnable() {
            @Override
            public void run() {
                while(threadFlag){
                    monitor();
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        log.e("监听服务 监听线程 err:"+e.getMessage());
                    }
                    log.e("监听服务 监听线程 执行...");
                }

            }
        });
        canAppThread.start();

    }

    private void startTimer() {
        //創建定時器
        timer = new Timer();
        //創建定時任務
        timerTask = new TimerTask() {
            @Override
            public void run() {
                monitor();
            }
        };
        //定時查看 棧頂  是誰
        timer.schedule(timerTask,0,10*1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        log.e(" 监听服务 结束");
//        stopTimer();

        if (canAppThread!=null){
            threadFlag=false;
            canAppThread = null;
        }
    }

    private void stopTimer() {
        if (timerTask!=null){
            timerTask.cancel();
            timerTask = null;
        }
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
    }

    private void monitor(){
        boolean flag =  serverUtils.isAppOnForeground(MonitorService.this.getApplicationContext());
        log.e("监听结果 :"+flag);
        if (flag){
            return;
        }

        //如果不是运行在前台
        Intent intent = new Intent();
        intent.setClassName(MonitorService.this.getPackageName(), DisplayActivity.class.getName());
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                        | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

}
