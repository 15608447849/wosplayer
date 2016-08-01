package com.wosplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;

/**
 * Created by user on 2016/8/1.
 */
public class StartSelfBroad extends BroadcastReceiver {
    static final String action_boot="android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        log.e("开机 自启动");
        if (intent.getAction().equals(action_boot)){
            Intent ootStartIntent=new Intent(context,DisplayActivity.class);
            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(ootStartIntent);
        }
    }
}
