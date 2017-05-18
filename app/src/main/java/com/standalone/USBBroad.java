package com.standalone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by lzp on 2017/4/26.
 * usb 广播
 * 动态注册
 */
public class USBBroad extends BroadcastReceiver{
    OnPlayed notify;
    public USBBroad(OnPlayed notify) {
        this.notify  = notify;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String path = intent.getData().getPath();
        Log.e("单机广播",action+" 目录:"+ path);
        if (intent.getAction().equals("android.intent.action.MEDIA_EJECT")){
            notify.onBroad("out#"+path);
        }
        if(intent.getAction().equals("android.intent.action.MEDIA_MOUNTED")){
            notify.onBroad("in#"+path);
        }
    }
}
