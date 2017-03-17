package com.wosplayer.command.operation.other;

import android.app.Activity;

import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;
import com.wosplayer.app.OverAppDialog;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.app.SystemConfig;
import com.wosplayer.command.operation.interfaces.CommandType;
import com.wosplayer.command.operation.interfaces.iCommand;
/**
 * Created by user on 2016/7/30.
 */
public class Command_Close_App implements iCommand {
    @Override
    public void execute(Activity activity, String param) {
        try {
            if (param.equals("false")){
                return;
            }
            if (activity!=null){
                OverAppDialog.popWind(activity,"关闭播放器,期待您的下次使用.",5);
            }

            //设置不监听activity启动
            SystemConfig.get().putOr("watchValue","1").save();
            Thread.sleep(5 * 1000);
            PlayApplication.stopCommunicationService();
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
