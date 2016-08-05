package com.wosplayer.Ui.uiBroadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.Ui.performer.UiExcuter;
import com.wosplayer.app.log;
import com.wosplayer.cmdBroadcast.Command.Schedule.correlation.XmlNodeEntity;

import rx.Scheduler;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/7/24.
 */

public class UibrocdCastReceive extends BroadcastReceiver{
    public static final String action = "com.wos.ui.refresh";
    public static final String key = "ui";
    private static final String TAG = UibrocdCastReceive.class.getName();
    private static final Scheduler.Worker helper =  Schedulers.newThread().createWorker();
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null){
            log.e(TAG,"intent is null");
            return;
        }


        final XmlNodeEntity entity = intent.getExtras().getParcelable(key);

        if (entity==null){
            log.e(TAG,"无排期");
            return;

        }else {
            log.i(TAG, "onReceive: "+entity.getXmldata().get("type")+","+entity.getXmldata().get("id"));
            helper.schedule(new Action0() {
                @Override
                public void call() {
                    log.e(TAG,"当前线程:"+Thread.currentThread().getName()+";线程数:"+Thread.getAllStackTraces().size());
                    UiExcuter.getInstancs().StartExcuter(entity);
                }
            });

        }


    }
}
