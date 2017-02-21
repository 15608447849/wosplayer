package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import android.content.Intent;
import android.os.Bundle;

import com.wosplayer.app.Logs;
import com.wosplayer.app.WosApplication;
import com.wosplayer.cmdBroadcast.Command.iCommand;
import com.wosplayer.service.logUploadService;

/**
 * Created by user on 2016/8/1.
 *
 */
public class Command_UPLG implements iCommand {
    @Override
    public void Execute(String param) {

        //获取 uri
        String uri = param;
        Logs.i("Command_UPLG_开启 日志上传 服务 "+ uri);
        if (uri==null || uri.equals("")) return;
        if (!uri.contains("http://")){
            Logs.e("upload log err :  uri is error ("+ uri +")");
            return;
        }

       Logs.i(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  start log upload server ...");
        //开启 日志上传服务
        Intent logIntent = new Intent(WosApplication.appContext,logUploadService.class);
        Bundle b = new Bundle();
        b.putString(logUploadService.uriKey,uri);
        b.putString("terminalNo", WosApplication.config.GetStringDefualt("terminalNo","0000"));
        logIntent.putExtras(b);
        WosApplication.appContext.startService(logIntent);

    }
}
