package com.wos.play.rootdir.model_monitor.soexcute;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.app.AdbCommand;
import com.wosplayer.app.AppTools;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;

import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by 79306 on 2017/2/24.
 */

public class SystemBroads extends BroadcastReceiver {
    private static final String TAG = "系统广播";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logs.e(TAG,"接受到系统广播action: "+action);
//        WatchServerHelp.openDeams(context);
        if (action.equals("android.intent.action.BOOT_COMPLETED")){
            //开机广播
            Logs.e(TAG,"========================================================================================================================\n" +
                    "========================================================================================================================\n" +
                    "\n\n\n\n"+
                    "######################################################################################################################\n"+
                    "|                深圳颖网科技长沙研发部开发 多媒体终端播放器 开发人员:LiShiPing - 微信:lzp793065165  欢迎使用.           |\n"+
                    "######################################################################################################################\n"+
                    "\n\n\n\n" +
                    "========================================================================================================================\n" +
                    "========================================================================================================================\n");
            AppTools.LongToals(context,"[欢迎使用多媒体播放器,技术热线:4008-166-128]");
            WatchServerHelp.openDeams(context);
        }
        if(action.equals("android.intent.action.MEDIA_MOUNTED")){
           //sdcard等媒体介质装载
            //打开调试接口
            ShellUtils.execCommand(AdbCommand.openPoint(null),true);
        }
        if(action.equals("android.intent.action.MEDIA_EJECT")){
            //sdcard等媒体介质弹出
        }
    }

}
