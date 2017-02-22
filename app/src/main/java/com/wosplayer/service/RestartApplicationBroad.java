package com.wosplayer.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;

import java.util.Calendar;

/**
 * Created by Administrator on 2016/7/31.
 */

public class RestartApplicationBroad extends BroadcastReceiver {
    public static String action ="com.wos.restartbroad";
    private long delay = 10*1000;
    public static final String KEYS ="com.dalay";
    public static final String IS_START ="com.start";
    @Override
    public void onReceive(final Context context, final Intent intent) {

        boolean flag = intent.getBooleanExtra(IS_START,true);
        Logs.d("重启广播 flag - "+flag);
        if (flag){
            Intent mintent = new Intent(context,DisplayActivity.class);
            mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mintent);

        }else{
            String var = intent.getStringExtra(KEYS);
            try{
                delay =  Long.parseLong(var) ;//* 1000;
            }catch(Exception e){
                Logs.e("","" + e.getMessage());
                delay = 0 ;
            }

            Logs.e("收到 重启app 广播,执行时间: "+var+" 秒");
            Intent intenta = new Intent();
            intenta.putExtra(RestartApplicationBroad.IS_START,true);
            intenta.setAction(RestartApplicationBroad.action);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.SECOND, (int)delay);

            PendingIntent pi = PendingIntent.getBroadcast(context,0,intenta,0);
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                    .set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        }












//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                Intent mintent = new Intent(context,DisplayActivity.class);
//                mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(mintent);
//            }
//        },delay);
    }
}
