package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.app.log;

import static android.content.ContentValues.TAG;

/**
 * Created by user on 2016/12/10.
 */



    //升级广播
public class UPDCbroad extends BroadcastReceiver {

        public static final String ACTION   = "UPDC.ACTION";

        private Command_UPDC updcer;

        public UPDCbroad(Command_UPDC updcer) {
            this.updcer = updcer;
        }

    @Override
        public void onReceive(Context context, Intent intent) {
            log.e(TAG," 升级广播 收到  ");
            updcer.installApk(intent.getExtras().getString("updc","null file"));
        }
    }
