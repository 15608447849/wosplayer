package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import android.util.Log;

import com.wosplayer.app.log;
import com.wosplayer.app.WosApplication;
import com.wosplayer.cmdBroadcast.Command.iCommand;

import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by user on 2016/7/30.
 */
public class Command_Reboot_Sys implements iCommand {
    @Override
    public void Execute(String param) {
        log.e("-----------------------------------重启终端-----------------------------------------");

//        Intent intent = new Intent(DisplayActivity.activityContext, MonitorService.class);
//        DisplayActivity.activityContext.stopService(intent);

        //Command_CAPT.executeLiunx("reboot");//
//        AppToSystem.execRootCmdSilent("reboot");

        WosApplication.sendMsgToServer("OFLI:" + WosApplication.config.GetStringDefualt("terminalNo","0000"));

        String cmd = "reboot";
        Log.e("#####","\n"+cmd);
        ShellUtils.CommandResult cr = ShellUtils.execCommand(cmd,true,true);
        log.e(" reboot result:"+cr.result);
    }
}
