package com.wosuis.newperf;

import android.app.Activity;
import android.app.Service;
import android.os.Handler;

import com.wosplayer.Ui.performer.UiExcuter;
import com.wosplayer.app.DisplayActivity;
import com.wosuis.beans.ViewData;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by 79306 on 2017/3/11.
 */

public class UiViewFactory {
    private static DisplayActivity dActivity;

    public static void init(DisplayActivity activity){
        dActivity = activity;
    }

    public static void uninit(){
        dActivity = null;
    }

    public static void runingMain(Runnable run){
        if (dActivity!=null){
            dActivity.mHandler.post(run);
        }
    }




    public  static void genarateView(){
        //初始化判断
        if (dActivity == null) return;
        if (UiDataTanslation.proglist.size() == 0 || UiDataTanslation.map.size() ==0) return;

        //放入放入主线程执行
        runingMain(new Runnable() {
            @Override
            public void run() {

            }


        });
    }



}
