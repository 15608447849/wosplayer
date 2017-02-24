package com.wosplayer.command.operation.other;

import android.util.Log;

import com.wosplayer.app.Logs;
import com.wosplayer.app.DisplayerApplication;
import com.wosplayer.command.kernal.iCommand;

import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by user on 2016/7/30.
 */
public class Command_Reboot_Sys implements iCommand {
    @Override
    public void Execute(String param) {
        try {
            Logs.e("-----------------------------------重启终端-----------------------------------------");

//        Intent intent = new Intent(DisplayActivity.activityContext, MonitorService.class);
//        DisplayActivity.activityContext.stopService(intent);

            //Command_CAPT.executeLiunx("reboot");//
//        AppToSystem.execRootCmdSilent("reboot");

            DisplayerApplication.sendMsgToServer("OFLI:" + DisplayerApplication.config.GetStringDefualt("terminalNo","0000"));

            String cmd = "reboot";
            Log.e("#####","\n"+cmd);
            ShellUtils.CommandResult cr = ShellUtils.execCommand(cmd,true,true);
            Logs.e(" reboot result:"+cr.result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
