package com.wosplayer.Ui.uiBroadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wosplayer.Ui.performer.UiExcuter;
import com.wosplayer.app.log;
import com.wosplayer.broadcast.Command.Schedule.correlation.XmlNodeEntity;

/**
 * Created by Administrator on 2016/7/24.
 */

public class UibrocdCastReceive extends BroadcastReceiver{
    public static final String action = "com.wos.ui.refresh";
    public static final String key = "ui";
    private static final String TAG = UibrocdCastReceive.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {


        XmlNodeEntity entity = intent.getExtras().getParcelable(key);
        log.i(TAG, "onReceive: "+entity.toString());

        if (entity==null){


        }else {
            UiExcuter.getInstancs(entity);
//            UiExcuter.StartExcuter();
        }


    }
}
