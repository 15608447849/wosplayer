package com.wosplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;

/**
 * Created by user on 2016/8/1.
 */
public class StartSelfBroad extends BroadcastReceiver {
    static final String action_boot="android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        log.e("-----------------------开机 -----------------------   自启动---------------------------------------");
        log.e(" installBroad ","["+intent.getAction()+"]");
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){//"android.intent.action.BOOT_COMPLETED";

//            Toast.makeText(context, "start remote port !!!", Toast.LENGTH_LONG).show();
//            AdbShellCommd.startRemoteport();

           //打开app
            Toast.makeText(context, "boot_completed action has got !!!", Toast.LENGTH_LONG).show();
            Intent ootStartIntent=new Intent(context,DisplayActivity.class);
            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(ootStartIntent);




            log.e("-----------------------------开机启动任务完成--------------------------------");

        }
    }
}
