package com.wosplayer.command.operation.other;

import android.app.Activity;

import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;
import com.wosplayer.app.OverAppDialog;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.command.operation.interfaces.iCommand;

/**
 * Created by user on 2016/7/30.
 */
public class Command_Reboot_App implements iCommand {
    @Override
    public void execute(Activity activity, String param) {
        try {
            if (activity!=null){
                OverAppDialog.popWind(activity,"即将重启播放器,请稍等片刻将自动开启",2);
                Thread.sleep(2 * 1000);
                PlayApplication.stopCommunicationService();
                System.exit(0);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        } catch (Exception e) {
        }
    }
}
