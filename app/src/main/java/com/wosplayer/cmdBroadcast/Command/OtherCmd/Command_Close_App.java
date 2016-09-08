package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import android.content.Intent;

import com.wos.Toals;
import com.wosplayer.activity.DisplayActivity;
import com.wosplayer.app.log;
import com.wosplayer.cmdBroadcast.Command.iCommand;
import com.wosplayer.service.MonitorService;

/**
 * Created by user on 2016/7/30.
 */
public class Command_Close_App implements iCommand {
    @Override
    public void Execute(String param) {

        if (param.equals("false")){
            Toals.Say("close app param :"+param);
            return;
        }
        log.i("stop runing app...");
        // 停止監聽服務
        Intent intent = new Intent(DisplayActivity.activityContext, MonitorService.class);
        DisplayActivity.activityContext.stopService(intent);
        //不發送重啟廣播
        DisplayActivity.isSendRestartBroad = false;
     //   DisplayActivity.activityContext.finish();
        System.exit(0);
    }
}
