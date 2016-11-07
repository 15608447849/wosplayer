package com.wosplayer.cmdBroadcast.Command.OtherCmd;

import android.content.Intent;
import android.os.Bundle;

import com.wosplayer.app.log;
import com.wosplayer.app.wosPlayerApp;
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
        log.i("Command_UPLG_开启 日志上传 服务 "+ uri);
        if (uri==null || uri.equals("")) return;
        if (!uri.contains("http://")){
            log.e("upload log err :  uri is error ("+ uri +")");
            return;
        }

       log.i(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  start log upload server ...");
        //开启 日志上传服务
        Intent logIntent = new Intent(wosPlayerApp.appContext,logUploadService.class);
        Bundle b = new Bundle();
        b.putString(logUploadService.uriKey,uri);
        b.putString("terminalNo", wosPlayerApp.config.GetStringDefualt("terminalNo","0000"));
        logIntent.putExtras(b);
        wosPlayerApp.appContext.startService(logIntent);

    }
}
