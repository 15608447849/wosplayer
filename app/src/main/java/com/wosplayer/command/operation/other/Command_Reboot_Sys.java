package com.wosplayer.command.operation.other;

import android.app.Activity;

import com.wosplayer.app.AdbCommand;
import com.wosplayer.app.Logs;
import com.wosplayer.app.OverAppDialog;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.command.operation.interfaces.iCommand;

import cn.trinea.android.common.util.ShellUtils;

/**
 * Created by user on 2016/7/30.
 */
public class Command_Reboot_Sys implements iCommand {
    @Override
    public void execute(Activity activity, String param) {

        try {
            Logs.e("重启终端","-----------------------------------重启终端-----------------------------------------");
            int time = 5;
            PlayApplication.stopCommunicationService();
            OverAppDialog.popWind(activity,"警告:系统将在"+time+"秒后重启",time);
            ShellUtils.execCommand(AdbCommand.rebootTelOnTime(time),true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
