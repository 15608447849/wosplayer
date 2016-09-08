package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import com.wos.Toals;
import com.wosplayer.app.log;
import com.wosplayer.cmdBroadcast.Command.iCommand;

import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by user on 2016/9/6.
 */
public class Command_SHDO implements iCommand {

    @Override
    public void Execute(String param) {

        log.e("SHDO:" + param);

        if (param.equals("false")){
            Toals.Say("close tel param :"+param);
            return;
        }

        //关机
        String [] commands = {
                "adb shell\n",
                "sleep 10 && reboot -p\n"
        };
        ShellUtils.CommandResult cr = ShellUtils.execCommand(commands,true,true);

        String strs = "即将关机: "+cr.result;
        log.e(strs);
        Toals.Say(strs);
     }
}
