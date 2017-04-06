package com.wosplayer.command.kernal;

import com.wosplayer.app.BackRunner;
import com.wosplayer.app.DisplayActivity;
import com.wosplayer.app.Logs;
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

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by 79306 on 2017/3/6.
 */

public class CommandStore {
    private static final java.lang.String TAG = "命令中心" ;

    private static CommandStore instands;

    private CommandStore(){

    }
    public static CommandStore getInstands(){
        if (instands == null){
            instands = new CommandStore();
        }
        return instands;
    }

    //软引用
    private WeakReference<DisplayActivity> mActivity;
    private HashMap<String, iCommand> commandList = new HashMap<String, iCommand>();
    public void init(DisplayActivity activity){
        mActivity = new WeakReference<DisplayActivity>(activity);
        createOp();
        Logs.i(TAG,"完成初始化");
    }

    private void createOp() {
        commandList.clear();
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
        Logs.i(TAG,"存储命令操作对象成功");
    }

    /**
     * 是否存在指令操作对象
     */
    private boolean isExist(String cmd){
        return commandList.containsKey(cmd);
    }

    /**
     * 执行一个操作对象
     */
    public void opration(final String cmd,final String param){
        if (mActivity.get()==null){
            Logs.e(TAG,"软引用 activity 不存在!");
            return;
        }
        if (cmd.equals(CommandCenter.COMMONICATION_LIVE)) {
            mActivity.get().communicationLives();
            return;
        }
        if (!isExist(cmd)) return;
        BackRunner.runBackground(new Runnable() {
            @Override
            public void run() {
                Logs.d(TAG,"执行 - ["+cmd+"] - ["+ param+"]");
                commandList.get(cmd).execute(mActivity.get(),param);
            }
        });
    }

}
