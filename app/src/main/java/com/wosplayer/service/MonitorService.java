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

        log.e("--------------------------------------------------monitor server onCreate----------------------------------------");

       // if (!f){
          /*  log.e("monitor server - app not task top");
            Intent mintent  = new Intent();
            mintent.setAction(RestartApplicationBroad.action);
            sendBroadcast(mintent);*/
     //   }
//        startTimer();
        if (canAppThread!=null){
            threadFlag = false;
            canAppThread = null;
        }

        canAppThread =  new Thread(new Runnable() {
            @Override
            public void run() {
                while(threadFlag){
                    //log.e("------------------------------------------監聽中---------------------------------");
                    monitor();
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        //log.e("监听服务 监听线程 err:"+e.getMessage());
                    }
                    //   log.i("`````监听服务 监听线程 over..`````");
                }

            }
        });
        threadFlag = true;
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



        log.e("- -监听服务-------------------------------- 开启- -");


       /* canAppThread =  new Thread(new Runnable() {
            @Override
            public void run() {
                while(threadFlag){
                    log.e("------------------------------------------監聽中---------------------------------");
                    monitor();
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        log.e("监听服务 监听线程 err:"+e.getMessage());
                    }
                 //   log.i("`````监听服务 监听线程 over..`````");
                }

            }
        });
        threadFlag = true;
        canAppThread.start();*/


        return super.onStartCommand(intent, flags, startId);

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        log.i(" 监听服务 结束");
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
        boolean f = serverUtils.isAppOnForeground(MonitorService.this.getApplicationContext());
      //  log.d("isAppOnForeground():"+f);
        f = serverUtils.isRunningForeground(this.getApplicationContext(),this.getApplicationContext().getPackageName());
      //  log.d("isRunningForeground():"+f);
        f = serverUtils.isRunningToTaskTop(this.getApplicationContext(),DisplayActivity.class.getName());
     //   log.d("isRunningToTaskTop():"+f);

        boolean flag =  serverUtils.isAppOnForeground(MonitorService.this.getApplicationContext());
        //log.i("监听服务 监听结果 :"+flag);
        if (!flag){
            log.e("app not foreground...");
            Intent intent  = new Intent();
            intent.setAction(RestartApplicationBroad.action);
            intent.putExtra(RestartApplicationBroad.IS_START,false);
            intent.putExtra(RestartApplicationBroad.KEYS, "0");
            sendBroadcast(intent);
            //            return;
        }


        //如果不是运行在前台
      /*  Intent intent = new Intent();
        intent.setClassName(MonitorService.this.getPackageName(), DisplayActivity.class.getName());
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                        | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);*/
      /*  log.e("******************");
        DisplayActivity.activityContext.finish();
        log.e("-----******************-----");*/

    }

}
