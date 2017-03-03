package com.wosplayer.command.operation.other;

import android.content.Intent;
import android.os.Bundle;

import com.wosplayer.app.Logs;
import com.wosplayer.app.PlayApplication;
import com.wosplayer.command.operation.interfaces.iCommand;
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
        Intent logIntent = new Intent(PlayApplication.appContext,logUploadService.class);
        Bundle b = new Bundle();
        b.putString(logUploadService.uriKey,uri);
        b.putString("terminalNo", PlayApplication.config.GetStringDefualt("terminalNo","0000"));
        logIntent.putExtras(b);
        PlayApplication.appContext.startService(logIntent);

    }
}
