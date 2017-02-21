package com.wosplayer.tool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by user on 2016/11/7.
 */

public class SdcardBroad extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_MEDIA_EJECT)){
            Log.e("sdcardbroad","Intent.ACTION_MEDIA_EJECT");
        }else if(action.equals(Intent.ACTION_MEDIA_MOUNTED)){
            Log.e("sdcardbroad","Intent.ACTION_MEDIA_MOUNTED");
            if (context==null){
                Log.e("sdcardbroad","context is null");
                return;
            }
            SdCardTools.checkSdCard(context);
        }
    }
}
