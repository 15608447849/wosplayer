package com.wosplayer.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wosplayer.app.log;
import com.wosplayer.broadcast.Command.CMD_INFO;
import com.wosplayer.broadcast.Command.Schedule.ScheduleSaver;
import com.wosplayer.broadcast.Command.iCommand;

import java.util.HashMap;


/**
 * Created by Administrator on 2016/7/20.
 */

public class CmdPostTaskCenter extends BroadcastReceiver {

    public static final String action = "com.post.cmd.broad";
    public static final String cmd = "toCmd";
    public static final String param = "toParam";


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
    }

    private void postCmd(String cmd,String param){

        if (commandList.containsKey(cmd)) {
            log.i("执行指令:"+cmd);
            iCommand icommand = commandList.get(cmd);
            icommand.Execute(param); // 执行~
        }
    }


















}



