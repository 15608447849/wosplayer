package com.wosplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/7/31.
 */

public class RestartApplicationBroad extends BroadcastReceiver {
    public static String action ="com.wos.restartbroad";
    private long delay = 10*1000;
    @Override
    public void onReceive(final Context context, final Intent intent) {
        log.e("收到 重启 广播");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent mintent = new Intent(context,DisplayActivity.class);
                mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mintent);
            }
        },delay);
    }
}
