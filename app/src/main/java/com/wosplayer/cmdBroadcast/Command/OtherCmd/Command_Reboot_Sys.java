package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import android.content.Intent;

import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.cmdBroadcast.Command.iCommand;
import com.wosplayer.service.MonitorService;

import installUtils.AppToSystem;

/**
 * Created by user on 2016/7/30.
 */
public class Command_Reboot_Sys implements iCommand {
    @Override
    public void Execute(String param) {
        log.i("重启终端");

        Intent intent = new Intent(DisplayActivity.activityContext, MonitorService.class);
        DisplayActivity.activityContext.stopService(intent);

        //Command_CAPT.executeLiunx("reboot");//
        AppToSystem.execRootCmdSilent("reboot");
        wosPlayerApp.sendMsgToServer("OFLI:" + wosPlayerApp.config.GetStringDefualt("terminalNo","0000"));
    }
}
