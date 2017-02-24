package com.wosplayer.command.operation.other;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.app.Logs;

import static android.content.ContentValues.TAG;

/**
 * Created by user on 2016/12/10.
 */
    //升级广播
public class UPDCbroad extends BroadcastReceiver {
        public static final String ACTION   = "UPDC.ACTION";
        public static final String key = "updc";
        private Command_UPDC updcer;
        public UPDCbroad(Command_UPDC updcer) {
            this.updcer = updcer;
        }

    @Override
        public void onReceive(Context context, Intent intent) {
            Logs.e(TAG,"---------------------- 收到升级广播 -------------------------------");
            updcer.installApk(intent.getExtras().getString(key,"没有文件路径"));
        }
    }

