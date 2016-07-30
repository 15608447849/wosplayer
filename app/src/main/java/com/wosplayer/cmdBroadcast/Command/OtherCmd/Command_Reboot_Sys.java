package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
import com.wosplayer.cmdBroadcast.Command.iCommand;

/**
 * Created by user on 2016/7/30.
 */
public class Command_Reboot_Sys implements iCommand {
    @Override
    public void Execute(String param) {
        log.i("重启终端");
        wosPlayerApp.sendMsgToServer("OFLI:" + wosPlayerApp.config.GetStringDefualt("terminalNo","0000"));
        Command_CAPT.executeLiunx("reboot");//（）
    }
}
