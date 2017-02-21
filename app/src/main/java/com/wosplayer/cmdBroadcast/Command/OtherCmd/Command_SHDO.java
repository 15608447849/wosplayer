package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import com.wosplayer.app.Logs;
import com.wosplayer.cmdBroadcast.Command.iCommand;

import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by user on 2016/9/6.
 */
public class Command_SHDO implements iCommand {

    @Override
    public void Execute(String param) {

        try {
            Logs.e("SHDO:" + param);

            if (param.equals("false")){
                return;
            }

            //关机
            String [] commands = {
                    "adb shell\n",
                    "sleep 10 && reboot -p\n"
            };
            ShellUtils.CommandResult cr = ShellUtils.execCommand(commands,true,true);

            String strs = "即将关机: "+cr.result;
            Logs.e(strs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
