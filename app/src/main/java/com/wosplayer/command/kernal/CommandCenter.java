package com.wosplayer.command.kernal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.command.operation.interfaces.CommandType;
import com.wosplayer.command.operation.interfaces.iCommand;
import com.wosplayer.command.operation.other.Command_CAPT;
import com.wosplayer.command.operation.other.Command_Close_App;
import com.wosplayer.command.operation.other.Command_FdRer;
import com.wosplayer.command.operation.other.Command_PASD;
import com.wosplayer.command.operation.other.Command_Reboot_App;
import com.wosplayer.command.operation.other.Command_Reboot_Sys;
import com.wosplayer.command.operation.other.Command_SHDO;
import com.wosplayer.command.operation.other.Command_SYTI;
import com.wosplayer.command.operation.other.Command_TSLT;
import com.wosplayer.command.operation.other.Command_UPDC;
import com.wosplayer.command.operation.other.Command_UPLG;
import com.wosplayer.command.operation.other.Command_VOLU;
import com.wosplayer.command.operation.schedules.ScheduleSaver;
import com.wosplayer.command.operation.schedules.correlation.StringUtils;
import com.wosplayer.service.CommunicationService;

import java.util.HashMap;

import rx.Scheduler;
import rx.functions.Action0;
import rx.schedulers.Schedulers;


/**
 * Created by Administrator on 2016/7/20.
 */
public class CommandCenter extends BroadcastReceiver {
    public static final String action = "com.post.cmd.broad";
    public static final String cmd = "toCmd";
    public static final String param = "toParam";

    public static final String COMMONICATION_LIVE = "commonicationkey";

    @Override
    public void onReceive(Context context, Intent intent) {
        //发送通讯服务 收到消息
        PlayApplication.sendMsgToServer(CommunicationService.OK);
        String msgCmd = intent.getExtras().getString(cmd);
        String msgParam =  intent.getExtras().getString(param);
        if (!StringUtils.isEmpty(msgCmd)) CommandStore.getInstands().opration(msgCmd,msgParam);
    }
}



