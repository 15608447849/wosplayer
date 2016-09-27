package com.wosplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by user on 2016/9/27.
 */
public class CommunicationReconnectBroad extends BroadcastReceiver {
    public static final String ACTION = "com.communication.reconncetion.broad";
    public static final String sleepTilemKey = "sleepkey";
    private static Timer timer= null;
    private static TimerTask timerTask = null;
    @Override
    public void onReceive(Context context, Intent intent) {

        if (context!=null){

            if (intent!=null){

                log.e("*************************************通讯服务 重新连接 收到广播 *********************************");
                int time = intent.getExtras().getInt(sleepTilemKey);
                if (time == 0){
                    time = 60 * 1000;
                }

                log.e("time : " + time);

                if (timerTask!=null){
                    timerTask.cancel();
                    timerTask= null;
                }
                if (timer!=null){
                    timer.cancel();
                    timer=null;
                }
                timer = new Timer();
                timerTask = new TimerTask() {
                    @Override
                    public void run() {

                        if (DisplayActivity.activityContext!=null){
                            log.d("尝试开启 通讯服务中,请稍后 ...");
                            wosPlayerApp.startCommunicationService(DisplayActivity.activityContext);
                        }else{
                            log.e("重启连接服务器失败, activity 上下文不存在");
                        }
                    }
                };
                timer.schedule(timerTask,time);//延时多久 毫秒数
            }

        }

    }
}
