package com.wosplayer.command.kernal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.app.Logs;
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
    private static final java.lang.String TAG =CommandCenter.class.getName() ;
    private static final Scheduler.Worker helper1 =  Schedulers.newThread().createWorker();
    private static final Scheduler.Worker helper2 =  Schedulers.newThread().createWorker();
    @Override
    public void onReceive(Context context, Intent intent) {
        String msgCmd = intent.getExtras().getString(cmd);
        String msgParam =  intent.getExtras().getString(param);

        if (msgCmd==null) return;

        Logs.i("收到一个命令 => "+msgCmd+ " 参数:"+ msgParam);

        postCmd(msgCmd,msgParam);
    }


    private static HashMap<String, iCommand> commandList = new HashMap<String, iCommand>();
    static {
        // 更新排期
        commandList.put(CommandType.UPSC, new ScheduleSaver());
        //syncTime
        commandList.put(CommandType.SYTI,new Command_SYTI());
        // 抓图
        commandList.put(CommandType.SCRN, new Command_CAPT());
        // 抓图
        commandList.put(CommandType.CAPT, new Command_CAPT());
        // 音量控制
        commandList.put(CommandType.VOLU, new Command_VOLU());
//        // 更新apk
        commandList.put(CommandType.UPDC, new Command_UPDC());
//        // 上传日志
        commandList.put(CommandType.UPLG, new Command_UPLG());
        // 重启程序
        commandList.put(CommandType.UIRE, new Command_Reboot_App());
        // 重启终端
        commandList.put(CommandType.REBO, new Command_Reboot_Sys());
        // 关闭播放器
        commandList.put(CommandType.SHDP, new Command_Close_App());
        //关闭终端
        commandList.put(CommandType.SHDO, new Command_SHDO());
        //设置密码
        commandList.put(CommandType.PASD,new Command_PASD());
        //建行对接接口
        commandList.put(CommandType.TSLT,new Command_TSLT(commandList.get(CommandType.UPSC)));

        //富滇银行
        commandList.put(CommandType.FFBK,new Command_FdRer());
    }

    private void postCmd(final String cmd, final String param){

        if (commandList.containsKey(cmd)) {
            //Logs.i("准备 执行指令:"+cmd +" 所在线程:"+Thread.currentThread().getName()+"- 当前线程数:"+Thread.getAllStackTraces().size());
            if (cmd.equals(CommandType.REBO) || cmd.equals(CommandType.UIRE) || cmd.equals(CommandType.UPDC)
                    || cmd.equals(CommandType.SHDO) || cmd.equals(CommandType.SHDP) || cmd.equals(CommandType.UPLG)
                    || cmd.equals(CommandType.SCRN) || cmd.equals(CommandType.CAPT) || cmd.equals(CommandType.TSLT)
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



