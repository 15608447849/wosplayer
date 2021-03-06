package com.wosplayer.command.operation.other;

import android.app.Activity;

import com.wosplayer.app.AdbCommand;
import com.wosplayer.app.Logs;
import com.wosplayer.app.OverAppDialog;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.command.operation.interfaces.iCommand;

import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by user on 2016/9/6.
 */
public class Command_SHDO implements iCommand {

    @Override
    public void execute(Activity activity, String param) {
        try {
            Logs.e("关闭终端","SHDO:" + param);
            if (param.equals("false")){
                return;
            }
            int time = 5;
            PlayApplication.stopCommunicationService();
            OverAppDialog.popWind(activity,"系统将在"+time+"秒后关机",time);
            ShellUtils.execCommand(AdbCommand.closeTelOnTime(time),true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
