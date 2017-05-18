package com.wos.play.rootdir.model_monitor.soexcute;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wosplayer.app.Logs;

/**
 * Created by 79306 on 2017/2/24.
 *
 */
public class SystemBroads extends BroadcastReceiver {
    private final String TAG = "SystemBroads";
    private final String INFO =
            "[深圳颖网科技长沙研发部开发 多媒体终端播放器 开发人员:LiShiPing - 微信:lzp793065165  欢迎使用]\n"+
            "[欢迎使用多媒体播放器,技术热线:4008-166-128]";
   private static Thread thread = null;
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.e(TAG,"颖网科技应用程序接受到安卓系统广播 - "+ intent.getAction());
        if (thread == null){
            thread =  new Thread(new Runnable() {
                @Override
                public void run() {
                    Logs.e(TAG,"线程启动");
                    startMonitor(context);
                    Logs.e(TAG,"线程关闭");
                }
            });
            thread.start();
        }
    }
    private void startMonitor(Context context) {
        Logs.i(TAG,INFO);
        WatchServerHelp.openDeams(context);
    }

}












//        WatchServerHelp.openDeams(context);
//        if (action.equals("android.intent.action.BOOT_COMPLETED")){
//
//        }
//        if(action.equals("android.intent.action.MEDIA_MOUNTED")){
//           //sdcard等媒体介质装载
//        }
//        if(action.equals("android.intent.action.MEDIA_EJECT")){
//            //sdcard等媒体介质弹出
//        }