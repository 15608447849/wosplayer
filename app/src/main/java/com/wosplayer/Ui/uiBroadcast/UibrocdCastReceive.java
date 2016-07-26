package com.wosplayer.Ui.uiBroadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.Ui.performer.UiExcuter;
import com.wosplayer.activity.counts;
import com.wosplayer.app.log;
import com.wosplayer.broadcast.Command.Schedule.correlation.XmlNodeEntity;

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


        final XmlNodeEntity entity = intent.getExtras().getParcelable(key);
        log.i(TAG, "onReceive: "+entity.toString());

        log.i(TAG,"当前线程:"+Thread.currentThread().getName()+";线程数:"+Thread.getAllStackTraces());
        if (entity==null){


        }else {
            helper.schedule(new Action0() {
                @Override
                public void call() {
                    log.i(TAG,Thread.currentThread().getName()+"RXJAVA :" + counts.i++ );
                    UiExcuter.getInstancs().StartExcuter(entity);
                }
            });

        }


    }
}