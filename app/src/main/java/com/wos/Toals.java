package com.wos;

import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by user on 2016/8/2.
 */
public class Toals {
    public static void Say(final String str){
        if (str==null || str.equals("")){
            log.e("toals err message is null");
            return;
        }
        if (DisplayActivity.activityContext ==null){
            log.e("toals err activity is null");
            return;
        }

      if (!DisplayActivity.isShowDialog){
          return;
      }


      AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
//                Toast.makeText(DisplayActivity.activityContext,str,Toast.LENGTH_SHORT).show();

            }
        });
    }
}
