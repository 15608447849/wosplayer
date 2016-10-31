package com.wosplayer.cmdBroadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.app.log;
import com.wosplayer.cmdBroadcast.Command.CMD_INFO;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_CAPT;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_Close_App;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_PASD;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_Reboot_App;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_Reboot_Sys;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_SHDO;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_SYTI;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_TSLT;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_UPDC;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_UPLG;
import com.wosplayer.cmdBroadcast.Command.OtherCmd.Command_VOLU;
import com.wosplayer.cmdBroadcast.Command.Schedule.ScheduleSaver;
import com.wosplayer.cmdBroadcast.Command.iCommand;

import java.util.HashMap;

import rx.Scheduler;
import rx.functions.Action0;
import rx.schedulers.Schedulers;


/**
 * Created by Administrator on 2016/7/20.
 */

public class CmdPostTaskCenter extends BroadcastReceiver {

    public static final String action = "com.post.cmd.broad";
    public static final String cmd = "toCmd";
    public static final String param = "toParam";
    private static final java.lang.String TAG =CmdPostTaskCenter.class.getName() ;
    private static final Scheduler.Worker helper1 =  Schedulers.newThread().createWorker();
    private static final Scheduler.Worker helper2 =  Schedulers.newThread().createWorker();
    @Override
    public void onReceive(Context context, Intent intent) {
        String msgCmd = intent.getExtras().getString(cmd);
        String msgParam =  intent.getExtras().getString(param);

        if (msgCmd==null) return;

        log.i("收到一个命令 => "+msgCmd+ " 参数:"+ msgParam);

        postCmd(msgCmd,msgParam);
    }


    private static HashMap<String, iCommand> commandList = new HashMap<String, iCommand>();
    static {
        // 更新排期
        commandList.put(CMD_INFO.UPSC, new ScheduleSaver());
        //syncTime
        commandList.put(CMD_INFO.SYTI,new Command_SYTI());
        // 抓图
        commandList.put(CMD_INFO.SCRN, new Command_CAPT());
        // 抓图
        commandList.put(CMD_INFO.CAPT, new Command_CAPT());
        // 音量控制
        commandList.put(CMD_INFO.VOLU, new Command_VOLU());
//        // 更新apk
        commandList.put(CMD_INFO.UPDC, new Command_UPDC());
//        // 上传日志
        commandList.put(CMD_INFO.UPLG, new Command_UPLG());
        // 重启程序
        commandList.put(CMD_INFO.UIRE, new Command_Reboot_App());
        // 重启终端
        commandList.put(CMD_INFO.REBO, new Command_Reboot_Sys());
        // 关闭播放器
        commandList.put(CMD_INFO.SHDP, new Command_Close_App());
        //关闭终端
        commandList.put(CMD_INFO.SHDO, new Command_SHDO());
        //设置密码
        commandList.put(CMD_INFO.PASD,new Command_PASD());
        //建行对接接口
        commandList.put(CMD_INFO.TSLT,new Command_TSLT());
    }

    private void postCmd(final String cmd, final String param){

        if (commandList.containsKey(cmd)) {
            log.i("准备 执行指令:"+cmd +" 所在线程:"+Thread.currentThread().getName()+"- 当前线程数:"+Thread.getAllStackTraces().size());
            if (cmd.equals(CMD_INFO.REBO) || cmd.equals(CMD_INFO.UIRE) || cmd.equals(CMD_INFO.UPDC)
                    || cmd.equals(CMD_INFO.SHDO) || cmd.equals(CMD_INFO.SHDP) || cmd.equals(CMD_INFO.UPLG)
                    || cmd.equals(CMD_INFO.SCRN) || cmd.equals(CMD_INFO.CAPT) || cmd.equals(CMD_INFO.TSLT)
                    ){

                helper1.schedule(new Action0() {
                    @Override
                    public void call() {
                        commandList.get(cmd).Execute(param);
                    }
                });
            }else{
                helper2.schedule(new Action0() {
                    @Override
                    public void call() {

                        commandList.get(cmd).Execute(param);
                    }
                });
            }

        }

    }


















}



