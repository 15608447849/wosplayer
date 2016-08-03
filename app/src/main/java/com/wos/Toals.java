package com.wos;

import android.widget.Toast;

import com.wosplayer.activity.DisplayActivity;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by user on 2016/8/2.
 */
public class Toals {
    public static void Say(final String str){
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                Toast.makeText(DisplayActivity.activityContext,str,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
